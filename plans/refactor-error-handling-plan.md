# Kế hoạch Refactor Error Handling — Spring Boot

> **Agent:** Antigravity + Claude Opus  
> **Ưu tiên:** Chuẩn, làm đúng từ đầu  
> **Ước tính:** 1.5–2 ngày làm việc  
> **Trạng thái hiện tại:** Có rải rác try/catch và ResponseEntity

---

## Tổng quan kiến trúc đích

```
Request → Controller (@Valid)
              ↓ gọi Service
         Service → ném AppException
              ↓
    GlobalExceptionHandler
              ↓
    ErrorResponse { traceId, code, message, status, path, timestamp }
              ↓ log với MDC
         Response 4xx / 5xx
```

---

## Phase 1 — Audit & Scan (~2 giờ)

**Mục tiêu:** Biết mình đang có gì, hỏng ở đâu — KHÔNG sửa gì cả.

### Prompt cho agent

```
Scan toàn bộ project, tìm và liệt kê:
1. Tất cả try/catch block trong package com.example (bỏ qua @Test)
2. Tất cả chỗ trả về ResponseEntity với status code cụ thể trong controller
3. Tất cả Exception class hiện đang được dùng

Format output:
- File path
- Dòng số
- Loại vấn đề (try/catch | ResponseEntity | custom exception)

Chỉ scan, KHÔNG sửa gì cả.
```

### Checklist

- [ ] Liệt kê xong tất cả file có try/catch
- [ ] Liệt kê xong tất cả controller trả ResponseEntity thủ công
- [ ] Có danh sách exception types đang dùng
- [ ] Xác định module nào cần migrate đầu tiên (ưu tiên service layer lớn nhất)

---

## Phase 2 — Xây nền tảng (~3 giờ)

**Mục tiêu:** Tạo scaffolding hoàn chỉnh, KHÔNG đụng code cũ.

### Cấu trúc package cần tạo

```
com.example.common.exception/
├── ErrorCode.java
├── AppException.java
├── ResourceNotFoundException.java
├── BusinessException.java
├── ExternalServiceException.java
├── ValidationException.java
├── ErrorResponse.java
└── TraceIdFilter.java
```

### Prompt cho agent

```
Tạo các file sau trong package com.example.common.exception:

1. ErrorCode.java — enum với các mã lỗi: [liệt kê từ kết quả Phase 1]
2. AppException.java — abstract base class extends RuntimeException
3. ResourceNotFoundException.java, BusinessException.java,
   ExternalServiceException.java, ValidationException.java
4. ErrorResponse.java — record gồm: traceId, code, message, status, path, timestamp
5. TraceIdFilter.java — OncePerRequestFilter lấy X-Trace-Id header, set vào MDC

KHÔNG sửa bất kỳ file service hay controller hiện tại.
```

### Code mẫu tham khảo

```java
// ErrorCode.java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    RESOURCE_NOT_FOUND("ERR_4001", "Không tìm thấy {0} với id={1}", HttpStatus.NOT_FOUND),
    VALIDATION_FAILED ("ERR_4002", "Dữ liệu không hợp lệ",          HttpStatus.BAD_REQUEST),
    DUPLICATE_ENTRY   ("ERR_4003", "{0} đã tồn tại",                 HttpStatus.CONFLICT),
    UNAUTHORIZED      ("ERR_4004", "Không có quyền truy cập",        HttpStatus.FORBIDDEN),
    EXTERNAL_ERROR    ("ERR_5001", "Lỗi kết nối {0}",               HttpStatus.BAD_GATEWAY),
    INTERNAL_ERROR    ("ERR_5000", "Lỗi hệ thống",                  HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}

// AppException.java
public abstract class AppException extends RuntimeException {
    @Getter private final ErrorCode errorCode;

    protected AppException(ErrorCode errorCode, Object... args) {
        super(MessageFormat.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }
}

// ErrorResponse.java
@Builder
public record ErrorResponse(
    String traceId,
    String code,
    String message,
    int    status,
    String path,
    Instant timestamp,
    Map<String, String> fieldErrors
) { }

// TraceIdFilter.java
@Component
public class TraceIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String traceId = Optional.ofNullable(req.getHeader("X-Trace-Id"))
            .orElse(UUID.randomUUID().toString());
        MDC.put("traceId", traceId);
        res.setHeader("X-Trace-Id", traceId);
        try { chain.doFilter(req, res); }
        finally { MDC.clear(); }
    }
}
```

### Checklist

- [ ] `ErrorCode` enum có đầy đủ mã lỗi thực tế của project
- [ ] `AppException` và các class con compile thành công
- [ ] `ErrorResponse` build được với builder pattern
- [ ] `TraceIdFilter` đã được register là Spring bean
- [ ] Chạy `./mvnw compile` — không có lỗi

---

## Phase 3 — Migrate từng module (~1 ngày)

**Mục tiêu:** Thay thế dần, test sau mỗi module — không làm hỏng module khác.

### Thứ tự migrate (ưu tiên)

1. Service layer lớn nhất (thường là `OrderService`, `UserService`)
2. Các service nhỏ hơn
3. Controller (bỏ ResponseEntity thủ công)
4. Feign Client / WebClient (thêm ErrorDecoder)

### Prompt cho agent (lặp lại cho mỗi module)

```
Refactor module [TÊN_MODULE] theo các bước:

1. Thay tất cả:
   orElseThrow(RuntimeException::new)
   → orElseThrow(() -> new ResourceNotFoundException("TÊN_ENTITY", id))

2. Thay try/catch Exception chung → ném exception phù hợp từ ErrorCode

3. Xoá try/catch ở controller nếu có, để GlobalExceptionHandler xử lý

4. KHÔNG thay đổi business logic, chỉ thay phần xử lý lỗi

5. Sau khi xong, chạy existing test để verify không có regression
```

### Quy tắc theo tầng

| Tầng | Hành động đúng |
|---|---|
| **Repository** | Trả `Optional<T>`, KHÔNG ném exception |
| **Service** | Ném `ResourceNotFoundException`, `BusinessException` |
| **Feign/WebClient** | Ném `ExternalServiceException` qua ErrorDecoder |
| **Controller** | KHÔNG ném, KHÔNG try/catch — chỉ gọi service |

### Code mẫu tham khảo

```java
// Service — đúng cách
public Order getOrder(Long id) {
    return orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order", id));
}

public Order createOrder(CreateOrderRequest req) {
    if (orderRepository.existsByCode(req.getCode())) {
        throw new BusinessException(ErrorCode.DUPLICATE_ENTRY, "Order code");
    }
    return orderRepository.save(mapper.toEntity(req));
}

// Feign ErrorDecoder
public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return new ExternalServiceException(
            ErrorCode.EXTERNAL_ERROR, "PaymentService"
        );
    }
}
```

### Checklist (mỗi module)

- [ ] Không còn `try/catch` trong service
- [ ] Không còn `ResponseEntity` thủ công trong controller
- [ ] Existing test vẫn pass
- [ ] Log có `traceId` khi ném exception

---

## Phase 4 — GlobalExceptionHandler + Test (~4 giờ)

**Mục tiêu:** Không lọt lỗi nào, test đầy đủ trước khi merge.

### Prompt cho agent

```
Tạo GlobalExceptionHandler trong package com.example.common.exception:

- Handle AppException → trả ErrorResponse với đúng HttpStatus
- Handle MethodArgumentNotValidException → tổng hợp field errors
- Handle Exception (catch-all) → log full stack, trả 500 KHÔNG lộ message thực
- Mỗi handler phải log kèm traceId từ MDC

Sau đó viết:
- Unit test cho từng handler với MockMvc
- 1 integration test gọi endpoint thật, verify response body có đủ:
  code, traceId, path, timestamp
```

### Code mẫu tham khảo

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex, HttpServletRequest req) {
        log.warn("[{}] {} — path={}",
            ex.getErrorCode().getCode(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity
            .status(ex.getErrorCode().getStatus())
            .body(buildResponse(ex.getErrorCode(), ex.getMessage(), req, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "invalid")
            ));
        ErrorResponse body = ErrorResponse.builder()
            .traceId(MDC.get("traceId"))
            .code(ErrorCode.VALIDATION_FAILED.getCode())
            .message(ErrorCode.VALIDATION_FAILED.getMessage())
            .status(400)
            .path(req.getRequestURI())
            .timestamp(Instant.now())
            .fieldErrors(fieldErrors)
            .build();
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}", req.getRequestURI(), ex);
        return ResponseEntity.internalServerError()
            .body(buildResponse(ErrorCode.INTERNAL_ERROR, "Lỗi hệ thống", req, null));
    }

    private ErrorResponse buildResponse(ErrorCode code, String message,
                                         HttpServletRequest req,
                                         Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
            .traceId(MDC.get("traceId"))
            .code(code.getCode())
            .message(message)
            .status(code.getStatus().value())
            .path(req.getRequestURI())
            .timestamp(Instant.now())
            .fieldErrors(fieldErrors)
            .build();
    }
}
```

### Checklist

- [ ] `GlobalExceptionHandler` handle đủ 3 loại: `AppException`, `Validation`, `Exception`
- [ ] Response body KHÔNG bao giờ lộ stack trace
- [ ] Unit test cover cả happy path và edge case
- [ ] Integration test verify `traceId` xuất hiện trong response
- [ ] Log format có `traceId` — dễ grep trên production

---

## Mẹo khi dùng với Antigravity + Opus

1. **Mỗi phase = 1 session riêng** — tránh context quá dài làm Opus bị nhiễu
2. **Luôn mở đầu bằng:** *"Đây là cấu trúc package của project: [paste tree]"*
3. **Phase 3:** Feed từng module một, đừng cho agent migrate tất cả cùng lúc
4. **Sau mỗi module:** Chạy `./mvnw test -pl module-name` trước khi sang module tiếp
5. **Commit nhỏ:** 1 commit per phase, dễ rollback nếu có vấn đề

---

## Định nghĩa "Done"

- [ ] Không còn `try/catch` nào trong service layer
- [ ] Không còn `ResponseEntity` thủ công trong controller
- [ ] Mọi lỗi đều có `code`, `traceId`, `path` trong response
- [ ] Stack trace KHÔNG bao giờ xuất hiện trong response body
- [ ] All existing tests pass
- [ ] Log có thể grep theo `traceId` để trace 1 request end-to-end

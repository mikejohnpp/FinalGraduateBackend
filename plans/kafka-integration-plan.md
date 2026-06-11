# Kế hoạch tích hợp Spring Kafka

## 1. Mục tiêu & Bối cảnh

Project hiện gồm 3 service Spring Boot 4.0.6 (`api-gateway`, `user-service`, `chat-service`) giao tiếp đồng bộ qua REST. Mục tiêu hiện tại của Kafka là **dựng một ví dụ ping/pong đơn giản giữa `chat-service` và `user-service`** để xác nhận luồng message bất đồng bộ chạy thông qua cluster Strimzi đã có. Các use-case mở rộng (auth event, chat fan-out, notification, audit) sẽ làm sau, dựa trên cùng nền tảng `common/kafka` đã xây.

## 2. Bức tranh kiến trúc hiện tại (demo ping/pong)

```
   ┌──────────────┐  GET /ping?msg=...
   │   client     │ ───────────────────────┐
   └──────────────┘                        ▼
                                   ┌──────────────────┐
                                   │   chat-service    │
                                   │ PingController    │
                                   │ PingPublisher ──┐ │
                                   │ PongListener ◀──┼─┐
                                   └─────────────────┼─┼┘
                                                     │ │
                                       publish demo.ping  consume demo.pong
                                                     ▼ ▲
                ┌────────────────────────────────────┴─┴───┐
                │ Strimzi Kafka cluster (K8s, namespace kafka) │
                │ external bootstrap: 100.106.249.45:31835      │
                │ in-cluster:                                   │
                │   final-graduate-cluster-kafka-bootstrap      │
                │   .kafka.svc.cluster.local:9092               │
                └────────────────────────────────────┬─┬───────┘
                                                     ▲ ▼
                                       publish demo.pong  consume demo.ping
                                                     │ │
                                   ┌─────────────────┼─┼─┐
                                   │   user-service   │ │ │
                                   │ PingListener ◀──┘ │ │
                                   │   (publishes Pong)│ │
                                   └────────────────────┘
```

Hạ tầng Kafka đang chạy:
- **Cluster**: Strimzi `final-graduate-cluster`, namespace `kafka`.
- **Truy cập từ máy dev (ngoài K8s, qua Tailscale)**: `100.106.249.45:31835` (NodePort bootstrap).
- **Truy cập từ trong K8s**: `final-graduate-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092`.

Nguyên tắc:
- **Mỗi service có Spring Kafka riêng**, dùng chung helper / config / event record trong `common`.
- **Topic là contract**, payload là `record` Java đặt trong `common/events/` (top-level package, tách khỏi `dto`) → tránh drift schema.
- **Producer fire-and-forget** với callback log lỗi; **consumer idempotent** (retry 3 lần → DLQ qua `KafkaErrorHandlingConfig` mặc định).

## 3. Vị trí code thực tế hiện tại

### `common` (shared kernel)

```
common/src/main/java/org/social/common/
├── events/                       // event records (top-level, NOT under dto/)
│   ├── PingEvent.java            // record(from, message, sentAt)
│   └── PongEvent.java            // record(from, replyTo, message, sentAt)
└── kafka/
    ├── KafkaTopics.java          // hằng số tên topic
    ├── KafkaHeaders.java         // hằng số custom header (eventId, ...)
    ├── config/
    │   ├── KafkaCommonProperties.java   // @ConfigurationProperties("app.kafka")
    │   └── KafkaErrorHandlingConfig.java // DefaultErrorHandler + DLT recoverer (auto-config)
    └── support/
        ├── EventEnvelope.java    // record bao ngoài mọi event
        └── EventPublisher.java   // wrapper KafkaTemplate (set headers, log)
```

Lưu ý:
- Event records nằm ở **top-level `events/`**, không nằm dưới `dto/` — vì chúng là contract của Kafka transport, khác với DTO REST.
- Class config trong `common/kafka/config/KafkaErrorHandlingConfig` được auto-import qua `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

### `chat-service`
- Producer: `messaging/PingPublisher` publish `PingEvent` vào `demo.ping`.
- Listener: `messaging/PongListener` consume `PongEvent` từ `demo.pong`.
- Config: `messaging/ChatKafkaConfig` cung cấp bean `EventPublisher` với `source = "chat-service"`.
- REST: `PingController` (GET `/ping?msg=...`) để trigger thủ công.

### `user-service`
- Listener + producer: `messaging/PingListener` consume `demo.ping`, log payload, sau đó publish `PongEvent` vào `demo.pong`.
- Config: `messaging/UserKafkaConfig` cung cấp bean `EventPublisher` với `source = "user-service"`.

### `api-gateway`
- **Không** dùng Kafka ở giai đoạn này. Có thể bổ sung khi cần phát auth event sau.

## 4. Quy ước topic

Tên topic theo dạng `<domain>.<entity>.<action>` hoặc `<domain>.<action>`, kebab-case, số ít. Hiện tại chỉ có 2 topic demo:

| Topic | Producer | Consumer | Key | Mục đích |
|-------|----------|----------|-----|----------|
| `demo.ping` | chat-service | user-service | `from` (tên service) | Demo gửi ping |
| `demo.pong` | user-service | chat-service | `replyTo` | Demo phản hồi pong |
| `*.dlt` | (auto) | — | giữ nguyên | Dead-letter của topic tương ứng (chưa có consumer DLQ riêng) |

Quy ước key:
- Dùng key có ý nghĩa partition (ví dụ id user / conversation) khi mở rộng để đảm bảo **thứ tự event của cùng entity**.
- Số partition mặc định **3** (cluster Strimzi đã set).

## 5. Cấu trúc payload event (envelope)

Mọi event bọc trong `EventEnvelope<T>` để chuẩn hoá metadata:

```java
public record EventEnvelope<T>(
    String eventId,        // UUID, idempotency key
    String eventType,      // ví dụ: "user.registered"
    String traceId,        // gắn từ MDC (sau khi tích hợp Sleuth/Micrometer)
    Instant occurredAt,
    String source,         // tên service phát event
    int version,           // schema version
    T payload
) {}
```

Lợi ích:
- Consumer có thể bỏ qua event đã xử lý (lưu `eventId` vào table `processed_event` để idempotent).
- Khi đổi schema payload, tăng `version` thay vì breaking change.
- Header Kafka cũng set `eventId`, `eventType` để filter mà không cần deserialize.

## 6. Cấu hình Spring Kafka

### 6.1 Dependencies (thêm vào root `pom.xml` `<dependencyManagement>`)

```xml
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.kafka</groupId>
  <artifactId>spring-kafka-test</artifactId>
  <scope>test</scope>
</dependency>
```

Spring Boot 4.0.6 đã quản version `spring-kafka` qua BOM nên không cần khai báo version cụ thể. Thêm `spring-kafka` vào `pom.xml` của `common`, `api-gateway`, `chat-service`, `user-service` (cần consume/produce).

### 6.2 application.yaml (mỗi service)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP:100.106.249.45:31835}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
    consumer:
      group-id: ${spring.application.name}
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer
        spring.json.trusted.packages: org.social.common.events,org.social.common.kafka.support
        spring.json.use.type.headers: false
        spring.json.value.default.type: org.social.common.kafka.support.EventEnvelope
      auto-offset-reset: earliest
      enable-auto-commit: false
    listener:
      ack-mode: manual_immediate
      concurrency: 3
      missing-topics-fatal: false

app:
  kafka:
    topics:
      demo-ping: demo.ping
      demo-pong: demo.pong
```

### 6.3 Profile prod
- `KAFKA_BOOTSTRAP` từ env var.
- Khi service chạy **trong K8s** (cùng cluster với Strimzi): set `KAFKA_BOOTSTRAP=final-graduate-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092`.
- Khi service chạy **ngoài K8s** (dev local qua Tailscale): set `KAFKA_BOOTSTRAP=100.106.249.45:31835`.
- Bật SASL/SSL nếu cluster bật authentication (hiện listener external đang PLAINTEXT — cần cân nhắc cho prod).
- Tăng `concurrency` consumer theo số partition.

## 7. Error handling & DLQ

Trong `common/kafka/config/KafkaErrorHandlingConfig.java`:

- `DefaultErrorHandler` với `FixedBackOff(1s, 3 lần)`.
- `DeadLetterPublishingRecoverer` đẩy record fail vào topic `<original>.dlt`.
- Consumer DLQ riêng (vd `chatservice/messaging/dlq/ChatDlqListener.java`) chỉ log + lưu DB để inspect, **không** retry tự động.
- `BusinessException` ném từ listener → vào DLQ ngay (no retry) bằng `addNotRetryableExceptions`.

## 8. Idempotency & Outbox (giai đoạn sau)

- **Phase 1**: producer fire-and-forget, consumer dùng `processed_event(event_id PK, processed_at)` table để skip duplicate.
- **Phase 2** (khi cần consistency cao): áp dụng **Transactional Outbox**:
  - Service ghi event vào table `outbox` cùng transaction nghiệp vụ.
  - Một worker (Debezium hoặc job nội bộ) đọc `outbox` → publish Kafka.
  - Tránh mất event khi DB commit nhưng Kafka publish fail.

## 9. Testing

- **Unit**: mock `KafkaTemplate` cho publisher; gọi listener trực tiếp với envelope giả.
- **Integration**: `@EmbeddedKafka` + `@SpringBootTest` cho mỗi service. Helper test `KafkaTestSupport` đặt trong `common/src/test/java`.
- **Hurl** (đã dùng) chỉ kiểm REST; thêm test contract Kafka bằng `spring-kafka-test`.

## 10. Lộ trình triển khai

| Giai đoạn | Phạm vi | Trạng thái |
|-----------|---------|-----------|
| 0 — Hạ tầng | Đã có sẵn cluster Strimzi trên K8s. External bootstrap NodePort `100.106.249.45:31835` (Tailscale). | ✅ ready |
| 1 — Common | Tạo package `kafka/`, `events/` (top-level, không under `dto/`), `EventEnvelope`, error config. Thêm dependency `spring-kafka` (optional). | ✅ done |
| 2 — Demo ping/pong | `chat-service` publish `demo.ping`; `user-service` consume rồi publish `demo.pong`; `chat-service` consume `demo.pong`. REST `/ping` để trigger. | ✅ done |
| 3 — Auth events (api-gateway) | Phát `user.registered`, `user.activated` sau register/active. | ⏳ chưa làm (dành cho sau khi demo chạy ổn) |
| 4 — Chat business events | Đổi `demo.ping/pong` thành `chat.message.sent/read`, gắn DB write + fan-out. | ⏳ |
| 5 — Observability | Micrometer Kafka metrics, log `eventId/traceId`. | ⏳ |
| 6 — Outbox (optional) | Khi cần SLA cao. | ⏳ |

## 11. Convention cập nhật `AGENTS.md`

Đã bổ sung mục **"Kafka conventions"** vào `AGENTS.md`:
- Vị trí event record: `common/events/` (top-level, không under `dto/`).
- Tên topic theo bảng ở mục 4.
- Mọi event đi qua `EventEnvelope`.
- Listener phải idempotent + có DLQ (`<topic>.dlt`) sẵn từ `KafkaErrorHandlingConfig`.
- Property prefix `app.kafka.topics.*` để inject tên topic.

## 12. Rủi ro & cách giảm thiểu

| Rủi ro | Giảm thiểu |
|--------|-----------|
| Schema event drift giữa services | Đặt event DTO trong `common`, version trong envelope |
| Mất event khi service crash giữa DB commit và publish | Outbox pattern (phase 6) |
| Consumer xử lý trùng | `processed_event` table + `eventId` |
| Topic ngày càng nhiều, khó quản | File `KafkaTopics.java` là single source; doc trong `AGENTS.md` |
| Local dev nặng | Tận dụng cluster Strimzi sẵn có qua NodePort `100.106.249.45:31835` (Tailscale), không cần Kafka local |

---

Sau khi bạn duyệt plan, tôi sẽ bắt đầu **Phase 0 + Phase 1** (hạ tầng docker-compose và package `common/kafka`) trước, rồi tiếp tục các phase còn lại theo thứ tự.

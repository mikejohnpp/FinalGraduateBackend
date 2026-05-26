# Post API Documentation (cho Frontend)

## Base URL

- **Development**: `http://localhost:8080/users/posts`
- Tất cả endpoint Post đều được proxy qua API Gateway (`/users/**` → `user-service`)

## Cấu trúc Response chung

Mọi response đều bọc trong `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Thông báo kết quả",
  "code": 200,
  "data": { ... }
}
```

Khi lỗi:

```json
{
  "success": false,
  "message": "Mô tả lỗi",
  "code": 404,
  "data": null
}
```

Lỗi validation (400):

```json
{
  "success": false,
  "message": "Dữ liệu đầu vào không hợp lệ",
  "code": 400,
  "data": {
    "content": "Nội dung bài viết không được để trống",
    "userId": "ID người dùng không được để trống"
  }
}
```

---

## 1. Tạo bài viết

```
POST /users/posts
```

### Mục đích
Tạo một bài viết mới.

### Request Body

```json
{
  "userId": 1,
  "content": "Nội dung bài viết (tối đa 500 ký tự)",
  "isGroupPosted": false,
  "groupId": null
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `userId` | Integer | ✅ | ID người tạo bài viết |
| `content` | String | ✅ | Nội dung bài viết (max 500 ký tự, không được rỗng) |
| `isGroupPosted` | Boolean | ❌ | `true` nếu đăng trong nhóm, mặc định `false` |
| `groupId` | Integer | ❌ | ID nhóm (nếu `isGroupPosted = true`) |

### Response — `201 Created`

```json
{
  "success": true,
  "message": "Tạo bài viết thành công!",
  "code": 201,
  "data": {
    "id": 10,
    "authorName": "Nguyễn Văn A",
    "isGroupPosted": false,
    "createdAt": "2026-05-26T10:00:00Z",
    "content": "Nội dung bài viết",
    "likeCount": 0
  }
}
```

### Lỗi có thể xảy ra

| HTTP Status | Điều kiện | Response `message` |
|-------------|-----------|-------------------|
| 400 | `userId` là null | `"ID người dùng không được để trống"` |
| 400 | `content` rỗng hoặc null | `"Nội dung bài viết không được để trống"` |
| 404 | `userId` không tồn tại trong DB | `"Không tìm thấy User với id: {userId}"` |

---

## 2. Lấy toàn bộ bài viết

```
GET /users/posts
```

### Mục đích
Lấy toàn bộ bài viết đang active (không phân trang). Dùng cho trường hợp đơn giản, không khuyến khích khi dữ liệu lớn.

### Request
Không có tham số.

### Response — `200 OK`

```json
{
  "success": true,
  "message": "Lấy danh sách bài viết thành công!",
  "code": 200,
  "data": [
    {
      "id": 10,
      "authorName": "Nguyễn Văn A",
      "isGroupPosted": false,
      "createdAt": "2026-05-26T10:00:00Z",
      "commentCount": 5,
      "content": "Nội dung bài viết",
      "likeCount": 12
    }
  ]
}
```

### Lỗi có thể xảy ra
Không có lỗi đặc biệt. Danh sách rỗng trả về mảng `[]`.

---

## 3. Lấy bài viết đề xuất (Infinite Scroll) ⭐

```
GET /users/posts/suggested?userId={userId}&cursor={cursor}&size={size}
```

### Mục đích
Endpoint chính cho **newsfeed / infinite scroll**. Hiện tại trả về tất cả bài viết mới nhất, trong tương lai sẽ có thuật toán đề xuất theo sở thích người dùng.

### Query Params

| Param | Kiểu | Bắt buộc | Default | Mô tả |
|-------|------|----------|---------|-------|
| `userId` | Integer | ✅ | — | ID user hiện tại (dùng cho thuật toán đề xuất sau này) |
| `cursor` | String | ❌ | `null` → lấy từ thời điểm hiện tại | ISO-8601 timestamp (`2026-05-26T10:00:00Z`). Lấy từ `nextCursor` của response trước |
| `size` | int | ❌ | `10` | Số bài viết mỗi lần load |

### Cách sử dụng (Frontend)

**Lần load đầu tiên** (khi vào trang):
```
GET /users/posts/suggested?userId=1&size=10
```

**Lần load tiếp theo** (khi scroll xuống):
```
GET /users/posts/suggested?userId=1&size=10&cursor=2026-05-26T09:00:00Z
```
→ Lấy `cursor` từ trường `nextCursor` của response trước đó.

**Dừng load** khi `hasMore = false`.

### Response — `200 OK`

```json
{
  "success": true,
  "message": "Lấy danh sách bài viết đề xuất thành công!",
  "code": 200,
  "data": {
    "data": [
      {
        "id": 10,
        "authorName": "Nguyễn Văn A",
        "isGroupPosted": false,
        "createdAt": "2026-05-26T10:00:00Z",
        "commentCount": 5,
        "content": "Nội dung bài viết",
        "likeCount": 12
      }
    ],
    "nextCursor": "2026-05-26T09:00:00Z",
    "hasMore": true
  }
}
```

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `data.data` | Array | Danh sách bài viết |
| `data.nextCursor` | String \| null | Cursor cho lần request tiếp theo. `null` nếu hết bài |
| `data.hasMore` | boolean | `false` = đã hết bài, dừng scroll |

### Lỗi có thể xảy ra

| HTTP Status | Điều kiện | Response `message` |
|-------------|-----------|-------------------|
| 400 | Thiếu `userId` | Spring sẽ trả lỗi `Required parameter 'userId' is not present` |

---

## 4. Tìm kiếm & Lọc bài viết (Standard Pagination)

```
GET /users/posts/search?keyword={keyword}&userId={userId}&isGroupPosted={bool}&groupId={groupId}&page={page}&size={size}&sortDir={sortDir}
```

### Mục đích
Tìm kiếm bài viết với bộ lọc động và phân trang thông thường (có tổng số trang, nhảy trang). Dùng cho trang quản trị, trang tìm kiếm.

### Query Params

| Param | Kiểu | Bắt buộc | Default | Mô tả |
|-------|------|----------|---------|-------|
| `keyword` | String | ❌ | — | Tìm kiếm trong nội dung bài viết (LIKE, không phân biệt hoa thường) |
| `userId` | Integer | ❌ | — | Lọc theo tác giả |
| `isGroupPosted` | Boolean | ❌ | — | `true` = bài trong nhóm, `false` = bài cá nhân |
| `groupId` | Integer | ❌ | — | Lọc theo nhóm cụ thể |
| `page` | int | ❌ | `0` | Số trang (bắt đầu từ 0) |
| `size` | int | ❌ | `10` | Số bài mỗi trang |
| `sortDir` | String | ❌ | `desc` | Sắp xếp theo `createdAt`: `"asc"` hoặc `"desc"` |

> [!TIP]
> Tất cả filter đều optional. Không truyền gì = lấy toàn bộ bài viết active.

### Ví dụ sử dụng

```
# Lọc bài viết của user 5, trang đầu tiên
GET /users/posts/search?userId=5&page=0&size=10

# Tìm kiếm theo keyword, sắp xếp cũ nhất trước
GET /users/posts/search?keyword=hello&sortDir=asc

# Lọc bài trong nhóm có ID = 3
GET /users/posts/search?isGroupPosted=true&groupId=3
```

### Response — `200 OK`

```json
{
  "success": true,
  "message": "Tìm kiếm bài viết thành công!",
  "code": 200,
  "data": {
    "data": [
      {
        "id": 10,
        "authorName": "Nguyễn Văn A",
        "isGroupPosted": false,
        "createdAt": "2026-05-26T10:00:00Z",
        "commentCount": 5,
        "content": "Nội dung bài viết",
        "likeCount": 12
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 42,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `data.data` | Array | Danh sách bài viết trang hiện tại |
| `data.page` | int | Trang hiện tại (0-indexed) |
| `data.size` | int | Kích thước trang |
| `data.totalElements` | long | Tổng số bài viết khớp filter |
| `data.totalPages` | int | Tổng số trang |
| `data.hasNext` | boolean | Có trang tiếp theo không |
| `data.hasPrevious` | boolean | Có trang trước không |

### Lỗi có thể xảy ra
Không có lỗi đặc biệt. Kết quả rỗng trả về `data.data = []` với `totalElements = 0`.

---

## 5. Xem chi tiết bài viết

```
GET /users/posts/{id}
```

### Mục đích
Lấy thông tin chi tiết một bài viết theo ID.

### Path Params

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `id` | Integer | ID bài viết |

### Response — `200 OK`

```json
{
  "success": true,
  "message": "Lấy chi tiết bài viết thành công!",
  "code": 200,
  "data": {
    "id": 10,
    "authorName": "Nguyễn Văn A",
    "isGroupPosted": false,
    "createdAt": "2026-05-26T10:00:00Z",
    "content": "Nội dung bài viết",
    "likeCount": 12
  }
}
```

### Lỗi có thể xảy ra

| HTTP Status | Điều kiện | Response `message` |
|-------------|-----------|-------------------|
| 404 | `id` không tồn tại hoặc đã bị soft delete | `"Không tìm thấy Bài viết với id: {id}"` |

---

## 6. Cập nhật bài viết

```
PUT /users/posts/{id}
```

### Mục đích
Cập nhật nội dung một bài viết.

### Path Params

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `id` | Integer | ID bài viết cần cập nhật |

### Request Body

```json
{
  "content": "Nội dung mới (tối đa 500 ký tự)"
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `content` | String | ✅ | Nội dung mới (không được rỗng, max 500 ký tự) |

### Response — `200 OK`

```json
{
  "success": true,
  "message": "Cập nhật bài viết thành công!",
  "code": 200,
  "data": {
    "id": 10,
    "authorName": "Nguyễn Văn A",
    "isGroupPosted": false,
    "createdAt": "2026-05-26T10:00:00Z",
    "content": "Nội dung mới",
    "likeCount": 12
  }
}
```

### Lỗi có thể xảy ra

| HTTP Status | Điều kiện | Response `message` |
|-------------|-----------|-------------------|
| 400 | `content` rỗng hoặc null | `"Nội dung bài viết không được để trống"` |
| 404 | `id` không tồn tại hoặc đã bị soft delete | `"Không tìm thấy Bài viết với id: {id}"` |

---

## 7. Xóa bài viết (Soft Delete)

```
DELETE /users/posts/{id}
```

### Mục đích
Xóa mềm bài viết (đặt `isActive = false`). Bài viết vẫn còn trong DB nhưng không hiển thị trong API nữa.

### Path Params

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `id` | Integer | ID bài viết cần xóa |

### Response — `200 OK`

```json
{
  "success": true,
  "message": "Xóa bài viết thành công!",
  "code": 200,
  "data": null
}
```

### Lỗi có thể xảy ra

| HTTP Status | Điều kiện | Response `message` |
|-------------|-----------|-------------------|
| 404 | `id` không tồn tại hoặc đã bị xóa | `"Không tìm thấy Bài viết với id: {id}"` |

---

## 8. Like bài viết

```
POST /users/posts/{id}/like
```

### Mục đích
Thích một bài viết. Mỗi user chỉ có thể like một bài viết **một lần duy nhất**.

### Path Params

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `id` | Integer | ID bài viết muốn like |

### Request Body

```json
{
  "userId": 1
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `userId` | Integer | ✅ | ID người dùng thực hiện like |

### Response — `201 Created`

```json
{
  "success": true,
  "message": "Đã thích bài viết!",
  "code": 201,
  "data": null
}
```

### Lỗi có thể xảy ra

| HTTP Status | Điều kiện | Response `message` |
|-------------|-----------|-------------------|
| 400 | `userId` là null | `"ID người dùng không được để trống"` |
| 400 | User đã like bài viết này rồi | `"Like đã tồn tại"` |
| 404 | Bài viết không tồn tại hoặc đã bị xóa | `"Không tìm thấy Bài viết với id: {id}"` |
| 404 | User không tồn tại | `"Không tìm thấy Người dùng với id: {userId}"` |

---

## 9. Unlike bài viết

```
DELETE /users/posts/{id}/like
```

### Mục đích
Bỏ thích một bài viết.

### Path Params

| Param | Kiểu | Mô tả |
|-------|------|-------|
| `id` | Integer | ID bài viết muốn unlike |

### Request Body

```json
{
  "userId": 1
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `userId` | Integer | ✅ | ID người dùng thực hiện unlike |

### Response — `200 OK`

```json
{
  "success": true,
  "message": "Đã bỏ thích bài viết!",
  "code": 200,
  "data": null
}
```

### Lỗi có thể xảy ra

| HTTP Status | Điều kiện | Response `message` |
|-------------|-----------|-------------------|
| 400 | `userId` là null | `"ID người dùng không được để trống"` |
| 404 | User chưa like bài viết này | `"Không tìm thấy Like với id: {id}"` |

---

## Tổng hợp Data Models

### PostDTO (Trả về khi tạo bài viết)

```typescript
interface PostDTO {
  id: number;
  authorName: string;
  isGroupPosted: boolean;
  createdAt: string;    // ISO-8601: "2026-05-26T10:00:00Z"
  content: string;
  likeCount: number;
}
```

### PostSummaryDTO (Trả về trong danh sách)

```typescript
interface PostSummaryDTO {
  id: number;
  authorName: string;
  isGroupPosted: boolean;
  createdAt: string;
  commentCount: number;
  content: string;
  likeCount: number;
}
```

### PostDetailDTO (Trả về khi xem/cập nhật chi tiết)

```typescript
interface PostDetailDTO {
  id: number;
  authorName: string;
  isGroupPosted: boolean;
  createdAt: string;
  content: string;
  likeCount: number;
}
```

### CursorPageResponse (Infinite Scroll)

```typescript
interface CursorPageResponse<T> {
  data: T[];
  nextCursor: string | null;
  hasMore: boolean;
}
```

### PageResponse (Standard Pagination)

```typescript
interface PageResponse<T> {
  data: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}
```

### ApiResponse Wrapper (Bọc mọi response)

```typescript
interface ApiResponse<T> {
  success: boolean;
  message: string;
  code: number;
  data: T;
}
```

---

## Xử lý lỗi phía Frontend

### Kiểm tra chung

```typescript
const response = await axios.get("/users/posts");
if (response.data.success) {
  // Xử lý dữ liệu: response.data.data
} else {
  // Hiển thị lỗi: response.data.message
}
```

### Bảng HTTP Status cần xử lý

| Status | Ý nghĩa | Cách xử lý |
|--------|---------|-------------|
| 200 | Thành công | Render dữ liệu |
| 201 | Tạo thành công | Render dữ liệu + thông báo thành công |
| 400 | Validation lỗi | Hiển thị lỗi theo field trong `data` |
| 401 | Chưa đăng nhập | Redirect về trang login |
| 404 | Không tìm thấy | Hiển thị thông báo "Không tìm thấy" |
| 409 | Dữ liệu trùng lặp (Like) | Hiển thị thông báo đã like rồi |
| 500 | Lỗi server | Hiển thị thông báo lỗi chung |

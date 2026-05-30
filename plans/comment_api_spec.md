# 📋 Yêu cầu API — Chức năng Comment trong Post

> Tài liệu này mô tả chi tiết các API endpoint cần phía **Backend** implement để hỗ trợ chức năng bình luận (comment) trên bài viết. Thiết kế dựa trên các pattern đã có trong hệ thống (Post CRUD, Like/Unlike, Cursor Pagination).

---

## 1. Tổng quan

| Hạng mục | Chi tiết |
|---|---|
| **Base path** | `/users/posts/{postId}/comments` |
| **Entity** | `Comment` |
| **Quan hệ** | `Comment` → `Post` (many-to-one), `Comment` → `User` (many-to-one), `Comment` → `Comment` (self-referencing, reply) |
| **Phân quyền** | Tất cả endpoint yêu cầu `Bearer Token` (JWT) |
| **Soft delete** | Áp dụng pattern `isActive` giống Post |
| **Reply** | Hỗ trợ **1 cấp** nested (reply vào comment gốc, KHÔNG reply vào reply) |

---

## 2. Entity — Comment

### 2.1. Database Schema

```sql
CREATE TABLE comments (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id     BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    parent_id   BIGINT NULL,            -- NULL = comment gốc, NOT NULL = reply
    content     TEXT NOT NULL,
    like_count  INT DEFAULT 0,
    reply_count INT DEFAULT 0,          -- chỉ tăng/giảm ở comment gốc
    is_active   BOOLEAN DEFAULT TRUE,   -- soft delete
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES posts(id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comments(id),
    
    INDEX idx_comment_post_created (post_id, created_at DESC),
    INDEX idx_comment_parent (parent_id, created_at ASC)
);
```

### 2.2. Comment Like Table

```sql
CREATE TABLE comment_likes (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    comment_id  BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
    CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_comment_user (comment_id, user_id)
);
```

---

## 3. DTO Definitions

### 3.1. Response DTO — `CommentDTO`

> Tương tự `PostSummaryDTO` — dùng cho cả list view và single view.

```java
public class CommentDTO {
    private Long id;
    private AuthorDTO author;       // { id, name, avatar, nickName } — reuse AuthorDTO đã có
    private Long postId;
    private Long parentId;          // null nếu là comment gốc
    private String content;
    private int likeCount;
    private int replyCount;         // số reply (chỉ > 0 ở comment gốc)
    private boolean liked;          // user hiện tại đã like comment này chưa
    private String createdAt;       // ISO 8601 format
}
```

### 3.2. Request DTO — `CommentCreateRequest`

```java
public class CommentCreateRequest {
    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 2000)
    private String content;

    private Long parentId;          // optional, null = comment gốc
}
```

### 3.3. Request DTO — `CommentUpdateRequest`

```java
public class CommentUpdateRequest {
    @NotBlank
    @Size(max = 2000)
    private String content;
}
```

### 3.4. Like Request DTO

```java
public class CommentLikeRequest {
    @NotNull
    private Long userId;
}
```

---

## 4. API Endpoints

### 4.1. Lấy danh sách comment của bài viết

```
GET /users/posts/{postId}/comments
```

| Param | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `postId` | Path | ✅ | — | ID bài viết |
| `userId` | Query | ✅ | — | ID user hiện tại (để xác định `liked`) |
| `cursor` | Query | ❌ | `null` | Cursor cho pagination (ID comment cuối cùng) |
| `size` | Query | ❌ | `10` | Số comment mỗi trang |

**Response:** `ApiResultGeneric<CursorPageResponse<CommentDTO>>`

```json
{
  "code": 200,
  "success": true,
  "data": {
    "data": [
      {
        "id": 1,
        "author": {
          "id": 5,
          "name": "Nguyen Van A",
          "avatar": "https://...",
          "nickName": null
        },
        "postId": 42,
        "parentId": null,
        "content": "Bài viết hay quá! 🔥",
        "likeCount": 5,
        "replyCount": 3,
        "liked": false,
        "createdAt": "2026-05-29T10:30:00"
      }
    ],
    "nextCursor": "eyJpZCI6MX0=",
    "hasMore": true
  }
}
```

> [!IMPORTANT]
> - Chỉ trả về **comment gốc** (`parent_id IS NULL`), KHÔNG kèm replies.
> - Sắp xếp theo `created_at DESC` (comment mới nhất lên trên).
> - Chỉ lấy comment có `is_active = true`.
> - Trường `liked` = `true` nếu tồn tại record trong `comment_likes` với `user_id` = userId param.

---

### 4.2. Lấy danh sách reply của một comment

```
GET /users/posts/{postId}/comments/{commentId}/replies
```

| Param | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `postId` | Path | ✅ | — | ID bài viết |
| `commentId` | Path | ✅ | — | ID comment gốc |
| `userId` | Query | ✅ | — | ID user hiện tại |
| `cursor` | Query | ❌ | `null` | Cursor |
| `size` | Query | ❌ | `5` | Số reply mỗi trang |

**Response:** `ApiResultGeneric<CursorPageResponse<CommentDTO>>`

> [!NOTE]
> - Chỉ trả về comment có `parent_id = commentId`.
> - Sắp xếp theo `created_at ASC` (reply cũ nhất lên trước, giống chat).
> - Reply có `replyCount = 0` và `parentId = commentId`.

---

### 4.3. Tạo comment mới

```
POST /users/posts/{postId}/comments
```

**Request Body:** `CommentCreateRequest`

```json
{
  "userId": 5,
  "content": "Comment hay quá!",
  "parentId": null
}
```

**Response:** `ApiResultGeneric<CommentDTO>`

```json
{
  "code": 200,
  "success": true,
  "data": {
    "id": 99,
    "author": { "id": 5, "name": "Nguyen Van A", "avatar": "...", "nickName": null },
    "postId": 42,
    "parentId": null,
    "content": "Comment hay quá!",
    "likeCount": 0,
    "replyCount": 0,
    "liked": false,
    "createdAt": "2026-05-29T15:00:00"
  }
}
```

> [!IMPORTANT]
> **Side effects khi tạo comment:**
> 1. Tăng `comment_count` trên bảng `posts` (+1).
> 2. Nếu `parentId != null` → tăng `reply_count` trên comment gốc (+1).
> 3. Nếu `parentId` trỏ tới một comment đã có `parent_id != null` (reply vào reply) → trả **400 Bad Request** với message: `"Không thể trả lời bình luận phản hồi"`.
> 4. Validate `parentId` phải thuộc cùng `postId`, nếu không → **400 Bad Request**.

---

### 4.4. Cập nhật comment

```
PUT /users/posts/{postId}/comments/{commentId}
```

**Request Body:** `CommentUpdateRequest`

```json
{
  "content": "Nội dung đã chỉnh sửa"
}
```

**Response:** `ApiResultGeneric<CommentDTO>`

> [!NOTE]
> - Chỉ cho phép **chính chủ** (user_id của comment = user trong JWT) sửa. Nếu không → **403 Forbidden**.
> - Cập nhật `updated_at`.

---

### 4.5. Xoá comment (soft delete)

```
DELETE /users/posts/{postId}/comments/{commentId}
```

**Response:** `ApiResult`

```json
{
  "code": 200,
  "success": true,
  "message": "Xoá bình luận thành công"
}
```

> [!IMPORTANT]
> **Side effects khi xoá comment:**
> 1. Set `is_active = false` (soft delete).
> 2. Giảm `comment_count` trên bảng `posts` (-1).
> 3. Nếu xoá comment gốc → soft delete **tất cả replies** của nó, giảm `comment_count` thêm `reply_count` lượt.
> 4. Nếu xoá reply → giảm `reply_count` trên comment gốc (-1).
> 5. Chỉ cho phép **chính chủ** hoặc **chủ bài viết** xoá. Nếu không → **403 Forbidden**.

---

### 4.6. Like comment

```
POST /users/posts/{postId}/comments/{commentId}/like
```

**Request Body:** `CommentLikeRequest`

```json
{
  "userId": 5
}
```

**Response:** `ApiResult`

```json
{
  "code": 200,
  "success": true
}
```

> [!NOTE]
> - Nếu đã like rồi → trả **409 Conflict** hoặc **200** idempotent (tuỳ team quyết định).
> - Tăng `like_count` trên bảng `comments` (+1).
> - Insert record vào `comment_likes`.

---

### 4.7. Unlike comment

```
DELETE /users/posts/{postId}/comments/{commentId}/like
```

**Request Body:** `CommentLikeRequest`

```json
{
  "userId": 5
}
```

**Response:** `ApiResult`

```json
{
  "code": 200,
  "success": true
}
```

> [!NOTE]
> - Nếu chưa like → trả **404 Not Found** hoặc **200** idempotent.
> - Giảm `like_count` trên bảng `comments` (-1).
> - Xoá record khỏi `comment_likes`.

---

## 5. Error Responses

Tất cả lỗi trả về format `ApiResult`:

```json
{
  "code": 400,
  "success": false,
  "message": "Nội dung bình luận không được để trống"
}
```

| HTTP Status | Trường hợp |
|---|---|
| **400** | Validation fail (content rỗng, parentId không hợp lệ, reply vào reply) |
| **401** | Token hết hạn hoặc không có |
| **403** | Không có quyền (sửa/xoá comment của người khác) |
| **404** | Post hoặc Comment không tồn tại (hoặc đã bị soft delete) |
| **409** | Like trùng (nếu không chọn idempotent) |

---

## 6. Tóm tắt Endpoints

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/users/posts/{postId}/comments` | Lấy danh sách comment (cursor pagination) |
| `GET` | `/users/posts/{postId}/comments/{commentId}/replies` | Lấy danh sách reply |
| `POST` | `/users/posts/{postId}/comments` | Tạo comment/reply mới |
| `PUT` | `/users/posts/{postId}/comments/{commentId}` | Sửa comment |
| `DELETE` | `/users/posts/{postId}/comments/{commentId}` | Xoá comment (soft delete) |
| `POST` | `/users/posts/{postId}/comments/{commentId}/like` | Like comment |
| `DELETE` | `/users/posts/{postId}/comments/{commentId}/like` | Unlike comment |

**Tổng cộng: 7 endpoints**

---

## 7. Lưu ý triển khai

> [!WARNING]
> ### Consistency với Post API hiện tại
> - Response wrapper: luôn dùng `ApiResultGeneric<T>` / `ApiResult` (giống Post API).
> - Cursor pagination: dùng `CursorPageResponse<T>` format giống `users/posts/suggested`.
> - AuthorDTO: reuse DTO đã có (`{ id, name, avatar, nickName }`).
> - Like/Unlike pattern: giống `users/posts/{postId}/like` đã implement.

> [!TIP]
> ### Performance
> - Index `(post_id, created_at DESC)` cho query list comments.
> - Index `(parent_id, created_at ASC)` cho query replies.
> - `like_count` và `reply_count` lưu denormalized trên bảng `comments` (tránh COUNT query).
> - `comment_count` trên bảng `posts` cũng denormalized (đã có sẵn).

> [!NOTE]
> ### Liên quan đến FE
> - FE sẽ gọi `userId` qua query param (giống pattern hiện tại ở `users/posts/suggested?userId=...`).
> - FE cần trường `liked` để hiển thị trạng thái like ban đầu mà không cần gọi thêm API.
> - FE sẽ optimistic update `likeCount` — nếu API fail sẽ rollback.

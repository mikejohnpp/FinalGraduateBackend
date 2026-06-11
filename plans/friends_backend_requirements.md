# 📋 Backend Requirements — Friends Feature

> Tài liệu yêu cầu cho BE team implement tính năng **Bạn bè (Friends)**.
> FE sẽ consume các API này theo đúng contract bên dưới.

---

## 1. Tổng quan

Tính năng Friends bao gồm:
- **Lời mời kết bạn**: Gửi / Chấp nhận / Từ chối
- **Gợi ý bạn bè**: Hiển thị danh sách người dùng có thể quen biết
- **Danh sách bạn bè**: Xem tất cả bạn bè đã kết nối
- **Huỷ kết bạn**: Xoá quan hệ bạn bè
- **Badge count**: Đếm số lời mời chưa xử lý

---

## 2. Database Schema

### Bảng `user_friends`

| Column | Type | Constraint | Mô tả |
|---|---|---|---|
| `user_id` | `BIGINT` | `FK → users.id`, `NOT NULL` | Người gửi lời mời |
| `friend_id` | `BIGINT` | `FK → users.id`, `NOT NULL` | Người nhận lời mời |
| `status` | `VARCHAR / ENUM` | `NOT NULL` | `PENDING`, `ACCEPTED`, `DECLINED` |
| `created_at` | `TIMESTAMP` | `NOT NULL`, `DEFAULT NOW()` | Thời điểm gửi lời mời |
| `updated_at` | `TIMESTAMP` | `NULL` | Thời điểm accept/decline |

**Primary Key**: `(user_id, friend_id)`

### Business Rules

| Hành động | Xử lý trong DB |
|---|---|
| A gửi lời mời cho B | `INSERT (user_id=A, friend_id=B, status=PENDING)` |
| B chấp nhận | `UPDATE status=ACCEPTED` cho row `(A, B)` **+** `INSERT (user_id=B, friend_id=A, status=ACCEPTED)` |
| B từ chối | `UPDATE status=DECLINED` cho row `(A, B)` hoặc `DELETE` row |
| A huỷ kết bạn B | `DELETE` cả 2 row `(A, B)` và `(B, A)` |

> [!NOTE]
> Quan hệ bạn bè là **2 chiều** — khi ACCEPTED phải có 2 rows ngược nhau.

---

## 3. Response Format

Tất cả API phải trả về theo format chuẩn đã có trong dự án:

```java
// Có data
ApiResultGeneric<T> {
    T data;
    boolean success;
    int code;
}

// Không có data
ApiResult {
    boolean success;
    int code;
    String message; // optional
}
```

### Pagination — Cursor-based

Dùng `CursorPageResponse<T>` (giống Post/Comment đang dùng):

```java
CursorPageResponse<T> {
    List<T> data;
    String nextCursor;  // ISO timestamp của item cuối, null nếu hết
    boolean hasMore;
}
```

---

## 4. DTOs

### 4.1 `FriendRequestDTO`

Dùng cho: danh sách lời mời kết bạn đã nhận.

```java
FriendRequestDTO {
    Long requestId;       // = user_friends.user_id (ID của người gửi, dùng làm identifier)
    AuthorDTO sender;     // Thông tin người gửi
    int mutualFriendCount;
    String createdAt;     // ISO 8601
}
```

### 4.2 `FriendSuggestionDTO`

Dùng cho: danh sách gợi ý bạn bè.

```java
FriendSuggestionDTO {
    AuthorDTO user;
    int mutualFriendCount;
}
```

### 4.3 `FriendshipDTO`

Dùng cho: danh sách bạn bè đã kết nối.

```java
FriendshipDTO {
    AuthorDTO user;
    String friendSince;       // ISO 8601
    int mutualFriendCount;
}
```

### 4.4 `AuthorDTO` (đã có sẵn)

```java
AuthorDTO {
    Long id;
    String name;
    String avatar;     // URL, nullable
    String nickName;   // nullable
}
```

> Tái sử dụng `AuthorDTO` đã có trong Post/Comment feature, **không tạo DTO mới** cho user info.

---

## 5. API Endpoints

### 5.1 Lời mời kết bạn đã nhận

```
GET /users/friends/requests?userId={userId}&cursor={cursor}&size={size}
```

| Param | Type | Required | Default | Mô tả |
|---|---|---|---|---|
| `userId` | `Long` | ✅ | — | ID user hiện tại |
| `cursor` | `String` | ❌ | `null` | ISO timestamp, lấy từ `nextCursor` |
| `size` | `int` | ❌ | `10` | Số lượng mỗi trang |

**Logic**:
- Query các row có `friend_id = userId` AND `status = PENDING`
- Sort theo `created_at DESC` (mới nhất trước)
- Cursor dựa trên `created_at`

**Response**: `ApiResultGeneric<CursorPageResponse<FriendRequestDTO>>`

```json
{
  "data": {
    "data": [
      {
        "requestId": 42,
        "sender": {
          "id": 42,
          "name": "Lam Phong Luckystar",
          "avatar": "https://example.com/avatar.jpg",
          "nickName": null
        },
        "mutualFriendCount": 4,
        "createdAt": "2026-05-29T10:00:00Z"
      }
    ],
    "nextCursor": "2026-05-29T09:00:00Z",
    "hasMore": true
  },
  "success": true,
  "code": 200
}
```

---

### 5.2 Gửi lời mời kết bạn

```
POST /users/friends/requests
Content-Type: application/json
```

**Request Body**:

```json
{
  "userId": 1,
  "targetUserId": 42
}
```

**Logic**:
1. Kiểm tra `userId != targetUserId`
2. Kiểm tra chưa có row `(userId, targetUserId)` với status `PENDING` hoặc `ACCEPTED`
3. Kiểm tra chưa có row ngược `(targetUserId, userId)` với status `PENDING` (nếu có → auto accept luôn, hoặc trả lỗi tuỳ business)
4. `INSERT (user_id=userId, friend_id=targetUserId, status=PENDING)`

**Response thành công**: `ApiResult { success: true, code: 201 }`

**Response lỗi**:

| Case | Code | Message |
|---|---|---|
| Đã gửi lời mời rồi | `409` | `"Lời mời kết bạn đã tồn tại"` |
| Đã là bạn bè | `409` | `"Hai người đã là bạn bè"` |
| User không tồn tại | `404` | `"Người dùng không tồn tại"` |

---

### 5.3 Chấp nhận lời mời kết bạn

```
PUT /users/friends/requests/{requestId}/accept?userId={userId}
```

| Param | Type | Mô tả |
|---|---|---|
| `requestId` | `Long` (path) | ID của người gửi lời mời (`user_id` trong bảng) |
| `userId` | `Long` (query) | ID user hiện tại (người nhận, phải = `friend_id` trong row) |

**Logic**:
1. Tìm row `(user_id=requestId, friend_id=userId, status=PENDING)`
2. `UPDATE status=ACCEPTED, updated_at=NOW()`
3. `INSERT (user_id=userId, friend_id=requestId, status=ACCEPTED)` — tạo row ngược

**Response thành công**: `ApiResult { success: true, code: 200 }`

**Response lỗi**:

| Case | Code | Message |
|---|---|---|
| Không tìm thấy lời mời | `404` | `"Lời mời kết bạn không tồn tại"` |
| Đã xử lý rồi | `409` | `"Lời mời đã được xử lý"` |

---

### 5.4 Từ chối lời mời kết bạn

```
PUT /users/friends/requests/{requestId}/decline?userId={userId}
```

| Param | Type | Mô tả |
|---|---|---|
| `requestId` | `Long` (path) | ID của người gửi lời mời |
| `userId` | `Long` (query) | ID user hiện tại |

**Logic**:
1. Tìm row `(user_id=requestId, friend_id=userId, status=PENDING)`
2. `DELETE` row (hoặc `UPDATE status=DECLINED`)

**Response thành công**: `ApiResult { success: true, code: 200 }`

---

### 5.5 Gợi ý bạn bè

```
GET /users/friends/suggestions?userId={userId}&cursor={cursor}&size={size}
```

| Param | Type | Required | Default |
|---|---|---|---|
| `userId` | `Long` | ✅ | — |
| `cursor` | `String` | ❌ | `null` |
| `size` | `int` | ❌ | `20` |

**Logic**:
- Gợi ý user dựa trên **số bạn chung** (mutual friends)
- **Loại trừ**: chính mình, đã là bạn bè (ACCEPTED), đã gửi/nhận lời mời (PENDING)
- Sort theo `mutualFriendCount DESC`
- Cursor có thể dựa trên `(mutualFriendCount, userId)` composite

**Query mẫu (tham khảo)**:

```sql
SELECT u.id, u.name, u.avatar, u.nick_name,
       COUNT(mf.friend_id) AS mutual_friend_count
FROM users u
-- Tìm bạn chung: bạn của tôi mà cũng là bạn của user u
LEFT JOIN user_friends mf
    ON mf.friend_id = u.id
    AND mf.status = 'ACCEPTED'
    AND mf.user_id IN (
        SELECT friend_id FROM user_friends
        WHERE user_id = :userId AND status = 'ACCEPTED'
    )
WHERE u.id != :userId
  AND u.id NOT IN (
      SELECT friend_id FROM user_friends
      WHERE user_id = :userId AND status IN ('ACCEPTED', 'PENDING')
  )
  AND u.id NOT IN (
      SELECT user_id FROM user_friends
      WHERE friend_id = :userId AND status = 'PENDING'
  )
GROUP BY u.id
ORDER BY mutual_friend_count DESC, u.id ASC
LIMIT :size + 1
```

**Response**: `ApiResultGeneric<CursorPageResponse<FriendSuggestionDTO>>`

```json
{
  "data": {
    "data": [
      {
        "user": {
          "id": 55,
          "name": "Nguyen Tuan Kiet",
          "avatar": "https://example.com/avatar.jpg",
          "nickName": "kiet_dev"
        },
        "mutualFriendCount": 12
      }
    ],
    "nextCursor": "12_55",
    "hasMore": true
  },
  "success": true,
  "code": 200
}
```

---

### 5.6 Danh sách bạn bè

```
GET /users/friends?userId={userId}&cursor={cursor}&size={size}
```

| Param | Type | Required | Default |
|---|---|---|---|
| `userId` | `Long` | ✅ | — |
| `cursor` | `String` | ❌ | `null` |
| `size` | `int` | ❌ | `20` |

**Logic**:
- Query các row có `user_id = userId` AND `status = ACCEPTED`
- JOIN với `users` table để lấy thông tin friend
- Tính `mutualFriendCount` cho mỗi friend
- Sort theo `created_at DESC` (bạn mới nhất trước) hoặc `name ASC` (tuỳ chọn)
- `friendSince` = `created_at` của row ACCEPTED

**Response**: `ApiResultGeneric<CursorPageResponse<FriendshipDTO>>`

```json
{
  "data": {
    "data": [
      {
        "user": {
          "id": 10,
          "name": "Thao Nguyen",
          "avatar": "https://example.com/avatar.jpg",
          "nickName": null
        },
        "friendSince": "2026-01-15T08:00:00Z",
        "mutualFriendCount": 3
      }
    ],
    "nextCursor": "2026-01-15T08:00:00Z",
    "hasMore": false
  },
  "success": true,
  "code": 200
}
```

---

### 5.7 Huỷ kết bạn

```
DELETE /users/friends/{friendUserId}?userId={userId}
```

| Param | Type | Mô tả |
|---|---|---|
| `friendUserId` | `Long` (path) | ID người bạn cần huỷ |
| `userId` | `Long` (query) | ID user hiện tại |

**Logic**:
1. `DELETE` row `(user_id=userId, friend_id=friendUserId)` — xoá chiều đi
2. `DELETE` row `(user_id=friendUserId, friend_id=userId)` — xoá chiều về
3. Cả 2 phải nằm trong 1 transaction

**Response thành công**: `ApiResult { success: true, code: 200 }`

---

### 5.8 Bỏ gợi ý bạn bè (Dismiss)

```
DELETE /users/friends/suggestions/{targetUserId}?userId={userId}
```

**Logic**:
- Lưu vào bảng phụ `dismissed_suggestions(user_id, target_user_id)` để không gợi ý lại
- Hoặc nếu đơn giản hơn: FE tự xoá khỏi list local, không cần persist

> [!NOTE]
> Endpoint này là **optional**. Nếu BE không muốn lưu dismissed state, FE sẽ chỉ xoá khỏi UI session hiện tại. Khi reload sẽ hiện lại.

**Response thành công**: `ApiResult { success: true, code: 200 }`

---

### 5.9 Đếm lời mời chưa xử lý

```
GET /users/friends/requests/count?userId={userId}
```

**Logic**:
- `SELECT COUNT(*) FROM user_friends WHERE friend_id = userId AND status = 'PENDING'`

**Response**: `ApiResultGeneric<Integer>`

```json
{
  "data": 5,
  "success": true,
  "code": 200
}
```

---

## 6. Tổng hợp Endpoints

| # | Method | Path | Mô tả | Priority |
|---|---|---|---|---|
| 1 | `GET` | `/users/friends/requests` | Danh sách lời mời đã nhận | 🔴 Cao |
| 2 | `POST` | `/users/friends/requests` | Gửi lời mời kết bạn | 🔴 Cao |
| 3 | `PUT` | `/users/friends/requests/{id}/accept` | Chấp nhận lời mời | 🔴 Cao |
| 4 | `PUT` | `/users/friends/requests/{id}/decline` | Từ chối lời mời | 🔴 Cao |
| 5 | `GET` | `/users/friends/suggestions` | Gợi ý bạn bè | 🟡 Trung bình |
| 6 | `GET` | `/users/friends` | Danh sách bạn bè | 🔴 Cao |
| 7 | `DELETE` | `/users/friends/{friendUserId}` | Huỷ kết bạn | 🟡 Trung bình |
| 8 | `DELETE` | `/users/friends/suggestions/{id}` | Bỏ gợi ý | 🟢 Thấp (optional) |
| 9 | `GET` | `/users/friends/requests/count` | Đếm lời mời | 🟡 Trung bình |

---

## 7. Lưu ý khi implement

### Performance
- Query **mutual friend count** có thể nặng nếu user nhiều → nên dùng batch query hoặc cache
- Endpoint **suggestions** (5.5) là nặng nhất → cân nhắc cache kết quả, TTL ~5 phút
- Index cần thiết: `(user_id, status)`, `(friend_id, status)`, `(user_id, friend_id)` unique

### Validation chung
- Mọi endpoint cần verify `userId` là user đang đăng nhập (thông qua JWT/SecurityContext)
- `targetUserId` / `friendUserId` phải tồn tại trong bảng `users`
- Không cho phép tự kết bạn chính mình

### Transaction
- **Accept** (5.3): UPDATE + INSERT phải trong cùng 1 transaction
- **Unfriend** (5.7): 2 DELETE phải trong cùng 1 transaction

### Error Codes thống nhất

| HTTP Code | Ý nghĩa |
|---|---|
| `200` | Thành công |
| `201` | Tạo mới thành công |
| `400` | Request body không hợp lệ |
| `401` | Chưa đăng nhập |
| `403` | Không có quyền |
| `404` | Không tìm thấy resource |
| `409` | Conflict (đã tồn tại, đã xử lý) |

---

## 8. Thứ tự implement đề xuất

```
Phase 1 (Core):
  ├── Entity + Repository (UserFriend đã có sẵn)
  ├── DTO classes (FriendRequestDTO, FriendSuggestionDTO, FriendshipDTO)
  ├── Mapper classes
  └── Service layer

Phase 2 (API):
  ├── GET  /users/friends/requests        (5.1)
  ├── POST /users/friends/requests        (5.2)
  ├── PUT  /users/friends/requests/{id}/accept   (5.3)
  ├── PUT  /users/friends/requests/{id}/decline  (5.4)
  └── GET  /users/friends                 (5.6)

Phase 3 (Extended):
  ├── GET  /users/friends/suggestions     (5.5)
  ├── DELETE /users/friends/{id}          (5.7)
  ├── GET  /users/friends/requests/count  (5.9)
  └── DELETE /users/friends/suggestions/{id} (5.8) — optional
```

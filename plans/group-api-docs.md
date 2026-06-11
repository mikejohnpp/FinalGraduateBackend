# Tài liệu API Nhóm (Group APIs)

Tất cả các API dưới đây đều được định tuyến qua Gateway và yêu cầu xác thực Bearer Token trong Header.

**Base URL:** `http://localhost:8080/users/groups`

---

## 1. Cấu trúc Response chung (ApiResponse)
Tất cả kết quả trả về đều tuân thủ cấu trúc:
```json
{
  "success": boolean,
  "message": "Thông báo thành công hoặc lỗi",
  "data": T,
  "code": "Mã HTTP (200, 201, 400, 404...)"
}
```

---

## 2. Chi tiết cấu trúc Dữ liệu (DTOs)

### 2.1 GroupDTO
Sử dụng cho các màn hình danh sách nhóm và chi tiết nhóm.
*   `id` (Integer): ID của nhóm.
*   `name` (String): Tên nhóm.
*   `coverPhoto` (String): URL ảnh bìa.
*   `avatar` (String): URL ảnh đại diện nhóm.
*   `privacy` (String): `public` hoặc `private`.
*   `memberCount` (Long): Tổng số thành viên.
*   `isJoined` (Boolean): User hiện tại đã tham gia chưa.
*   `role` (String): Vai trò của user (`ADMIN`, `MODERATOR`, `MEMBER`).
*   `mutualFriendCount` (Long): Số bạn bè chung đã tham gia nhóm.

### 2.2 PostSummaryDTO (với Group info)
Sử dụng cho bảng tin (Feed).
*   `authorRole` (String): Vai trò của người đăng bài trong nhóm đó.
*   `group` (Object):
    *   `id`, `name`, `avatar`.
*   *(Các trường khác như content, createdAt, likeCount... giữ nguyên như Post cũ)*

### 2.3 GroupMemberDTO
*   `userId` (Integer)
*   `name` (String): Tên hiển thị (Nickname hoặc Username).
*   `avatar` (String)
*   `role` (String): Vai trò trong nhóm.

---

## 3. Danh sách Endpoints

### 3.1 Tạo nhóm mới
*   **Endpoint:** `POST /users/groups`
*   **Query Params:** `userId` (Integer) - ID người tạo.
*   **Body (JSON):**
    ```json
    {
      "name": "Tên nhóm",
      "privacy": "public" | "private",
      "invitees": [1, 2, 3] // Danh sách ID người dùng được mời (tùy chọn)
    }
    ```
*   **Output (data):** `GroupDTO` (Thông tin nhóm vừa tạo và role='ADMIN').
*   **Lỗi thường gặp:** `404` (User không tồn tại), `400` (Tên nhóm trống).

### 3.2 Lấy chi tiết một nhóm
*   **Endpoint:** `GET /users/groups/{id}`
*   **Query Params:** `userId` (Integer) - Để check trạng thái `isJoined` và `role` của user hiện tại.
*   **Output (data):** `GroupDTO`.
*   **Lỗi thường gặp:** `404` (Nhóm không tồn tại hoặc bị xóa).

### 3.3 Danh sách nhóm đã tham gia
*   **Endpoint:** `GET /users/groups/joined`
*   **Query Params:** `userId` (Integer).
*   **Output (data):** `List<GroupDTO>`.

### 3.4 Khám phá nhóm (Gợi ý)
*   **Endpoint:** `GET /users/groups/suggested`
*   **Query Params:** `userId` (Integer).
*   **Output (data):** `List<GroupDTO>` (Danh sách các nhóm user chưa tham gia).

### 3.5 Tham gia nhóm
*   **Endpoint:** `POST /users/groups/{id}/join`
*   **Query Params:** `userId` (Integer).
*   **Output:** `ApiResponse<Void>` (200 OK).

### 3.6 Rời khỏi nhóm
*   **Endpoint:** `POST /users/groups/{id}/leave`
*   **Query Params:** `userId` (Integer).
*   **Output:** `ApiResponse<Void>` (200 OK).

### 3.7 Lấy danh sách thành viên nhóm
*   **Endpoint:** `GET /users/groups/{id}/members`
*   **Output (data):** `List<GroupMemberDTO>` (userId, name, avatar, role).

### 3.8 Bảng tin Nhóm tổng hợp (Group Feed)
*   **Endpoint:** `GET /users/groups/posts/feed`
*   **Query Params:** 
    *   `userId` (Integer)
    *   `cursor` (String, optional) - ISO Timestamp cho infinite scroll.
    *   `size` (Integer, default=10)
*   **Output (data):** `CursorPageResponse<PostSummaryDTO>` (Chứa bài viết từ tất cả nhóm đã join).

### 3.9 Lấy danh sách bài viết của một nhóm cụ thể
*   **Endpoint:** `GET /users/groups/{id}/posts`
*   **Query Params:** 
    *   `userId` (Integer)
    *   `cursor` (String, optional) - ISO Timestamp cho infinite scroll.
    *   `size` (Integer, default=10)
*   **Output (data):** `CursorPageResponse<PostSummaryDTO>`.
*   **Logic bổ sung:** Nếu nhóm là nhóm kín (private), user truyền lên bắt buộc phải là thành viên, nếu không sẽ trả về 403 Forbidden.

### 3.10 Tạo bài viết trong nhóm (Cập nhật từ Post API)
*   **Endpoint:** `POST /users/posts`
*   **Body (JSON):**
    ```json
    {
      "userId": 1,
      "content": "Nội dung bài viết",
      "isGroupPosted": true,
      "groupId": 123
    }
    ```
*   **Logic bổ sung:** Backend sẽ trả về `400 Validation Failed` nếu user đăng bài không phải là thành viên của nhóm.

---

## 4. Mã lỗi (Error Codes)
| Code | Ý nghĩa |
| :--- | :--- |
| `404` | Resource không tồn tại (Nhóm hoặc User). |
| `400` | Lỗi dữ liệu hoặc Logic (VD: Đăng bài khi chưa join nhóm). |
| `401` | Token không hợp lệ hoặc hết hạn. |
| `409` | Dữ liệu trùng lặp (VD: Đã join nhóm trước đó). |

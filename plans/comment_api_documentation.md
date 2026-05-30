# 📋 Tài Liệu API — Chức Năng Comment

Tài liệu này cung cấp đặc tả chi tiết về cách gọi và xử lý các REST API thuộc tính năng Bình luận (Comment).

**Base URL Context**: Các API endpoint dưới đây được gọi qua API Gateway và sẽ proxy đến User Service.
**Tiền tố (Prefix)**: `/users/posts/{postId}/comments`
*(Lưu ý: Qua Gateway, domain sẽ là `http://localhost:8080/api/users/posts...` nhưng thực chất map tới `/posts` của User Service)*

Tất cả Response đều được bọc trong class `ApiResponse<T>`:
```json
{
  "code": 200,
  "success": true,
  "message": "...",
  "data": { ... }
}
```

---

## 1. Lấy danh sách bình luận gốc của bài viết (Paging)

- **Mục đích**: Hiển thị danh sách các bình luận cha (parent_id = null) của một bài viết. Cấu trúc cuộn vô hạn (Infinite Scroll). Sắp xếp theo **mới nhất đến cũ nhất** (DESC).
- **Method**: `GET`
- **Endpoint**: `/users/posts/{postId}/comments`
- **Path Parameters**:
  - `postId` (Integer): ID của bài viết.
- **Query Parameters**:
  - `userId` (Integer, **Bắt buộc**): ID của user đang xem bài viết (Dùng để check xem user này đã like các bình luận trả về hay chưa).
  - `cursor` (String, Tuỳ chọn): Timestamp chuẩn ISO-8601 của bình luận cuối cùng ở page trước (VD: `2026-05-29T10:30:00Z`). Nếu lấy trang đầu tiên thì bỏ trống.
  - `size` (Integer, Tuỳ chọn, Mặc định = 10): Số lượng bình luận muốn lấy.
- **Output**: `ApiResponse<CursorPageResponse<CommentDTO>>`
- **Các trường hợp lỗi**:
  - **400 Bad Request**: Bị thiếu query param `userId`.

---

## 2. Lấy danh sách phản hồi (Replies) của một bình luận

- **Mục đích**: Hiển thị các phản hồi con thuộc về một bình luận gốc cụ thể. Sắp xếp theo **cũ nhất đến mới nhất** (ASC - Giống chat).
- **Method**: `GET`
- **Endpoint**: `/users/posts/{postId}/comments/{commentId}/replies`
- **Path Parameters**:
  - `postId` (Integer): ID của bài viết.
  - `commentId` (Integer): ID của bình luận gốc.
- **Query Parameters**: Tương tự như API 1 (`userId` bắt buộc, `cursor` và `size` tuỳ chọn nhưng mặc định size = 5).
- **Output**: `ApiResponse<CursorPageResponse<CommentDTO>>`

---

## 3. Thêm mới một bình luận / Phản hồi

- **Mục đích**: Tạo mới một bình luận gốc hoặc một phản hồi cho một bình luận khác.
- **Method**: `POST`
- **Endpoint**: `/users/posts/{postId}/comments`
- **Path Parameters**:
  - `postId` (Integer): ID của bài viết.
- **Request Body**: (JSON)
  ```json
  {
    "userId": 1,
    "content": "Nội dung bình luận quá hay ho!",
    "parentId": 2 
  }
  ```
  *(Truyền `parentId` nếu muốn tạo reply. Bỏ đi hoặc truyền `null` nếu là bình luận gốc)*
- **Output**: `ApiResponse<CommentDTO>` (Trả về bình luận vừa được tạo, kèm thông tin tác giả).
- **Các trường hợp lỗi**:
  - **400 Bad Request (Validation)**: `content` bị bỏ trống hoặc dài quá 2000 ký tự. `userId` bị bỏ trống.
  - **400 Bad Request (Business logic)**: Cố tình truyền `parentId` thuộc về một bài viết khác, hoặc `parentId` là một phản hồi (Hệ thống không cho phép reply lồng nhau quá 1 cấp).
  - **404 Not Found**: Bài viết hoặc Người dùng không tồn tại.

---

## 4. Chỉnh sửa bình luận

- **Mục đích**: Cập nhật lại nội dung của bình luận do chính người đó viết.
- **Method**: `PUT`
- **Endpoint**: `/users/posts/{postId}/comments/{commentId}`
- **Path Parameters**:
  - `postId` (Integer)
  - `commentId` (Integer): ID của bình luận cần sửa.
- **Query Parameters**:
  - `userId` (Integer, **Bắt buộc**): ID của user đang gửi yêu cầu sửa (để check quyền).
- **Request Body**: (JSON)
  ```json
  {
    "content": "Nội dung đã được thay đổi!"
  }
  ```
- **Output**: `ApiResponse<CommentDTO>`
- **Các trường hợp lỗi**:
  - **400 Bad Request**: Nội dung rỗng.
  - **403 Forbidden**: Người gửi yêu cầu sửa (`userId`) không phải là người tạo ra bình luận này.
  - **404 Not Found**: Bình luận không tồn tại (hoặc đã bị xoá).

---

## 5. Xoá bình luận

- **Mục đích**: Xoá mềm (Soft delete) bình luận. Giảm biến đếm. Nếu xoá bình luận gốc, hệ thống sẽ tự động xoá toàn bộ các phản hồi (replies) của nó.
- **Method**: `DELETE`
- **Endpoint**: `/users/posts/{postId}/comments/{commentId}`
- **Path Parameters**:
  - `postId` (Integer)
  - `commentId` (Integer): ID của bình luận cần xoá.
- **Query Parameters**:
  - `userId` (Integer, **Bắt buộc**): ID của người đang yêu cầu xoá (để phân quyền).
- **Output**: `ApiResponse<Void>`
- **Các trường hợp lỗi**:
  - **403 Forbidden**: Người xoá không phải là tác giả của bình luận, đồng thời cũng **không phải là chủ của bài viết**. (Chủ bài viết có quyền xoá comment của người khác trong bài mình).
  - **404 Not Found**: Bình luận hoặc bài viết không tồn tại.

---

## 6. Thích (Like) bình luận

- **Mục đích**: Gắn cờ thích (Like) cho bình luận.
- **Method**: `POST`
- **Endpoint**: `/users/posts/{postId}/comments/{commentId}/like`
- **Path Parameters**:
  - `postId` (Integer)
  - `commentId` (Integer): ID của bình luận.
- **Request Body**: (JSON)
  ```json
  {
    "userId": 1
  }
  ```
- **Output**: `ApiResponse<Void>`
- **Các trường hợp lỗi**:
  - **404 Not Found**: Bình luận hoặc Người dùng không tồn tại.
  - **409 Conflict**: Trạng thái "Đã tồn tại" - Xảy ra khi user này đã bấm like bình luận này từ trước rồi mà vẫn cố gọi lại API tạo Like.

---

## 7. Bỏ thích (Unlike) bình luận

- **Mục đích**: Huỷ cờ thích (Like) bình luận.
- **Method**: `DELETE`
- **Endpoint**: `/users/posts/{postId}/comments/{commentId}/like`
- **Path Parameters**:
  - `postId` (Integer)
  - `commentId` (Integer): ID của bình luận.
- **Request Body**: (JSON)
  ```json
  {
    "userId": 1
  }
  ```
- **Output**: `ApiResponse<Void>`
- **Các trường hợp lỗi**:
  - **404 Not Found**: User này chưa từng like bình luận (hoặc bình luận bị xoá), không tìm thấy record Like để unlike.

---

## Đối tượng Data DTO (Output Data Schema)

### CommentDTO
Dữ liệu chuẩn trả về khi get danh sách hoặc thêm/sửa một bình luận:

```json
{
  "id": 101,
  "author": {
    "id": 5,
    "name": "Nguyễn Văn A",
    "avatar": "https://example.com/avatar.jpg",
    "nickName": null
  },
  "postId": 42,
  "parentId": null,               // Nếu null thì là comment gốc, nếu có ID thì là reply
  "content": "Nội dung bình luận...",
  "likeCount": 15,                // Tổng lượt thích
  "replyCount": 3,                // Tổng số replies (Chỉ comment gốc mới > 0)
  "liked": true,                  // Trạng thái: User đang request có like comment này chưa
  "createdAt": "2026-05-29T10:30:00Z"
}
```

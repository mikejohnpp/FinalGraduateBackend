# Tài Liệu Tích Hợp API Group Admin (Dành Cho Frontend)

Tài liệu này tổng hợp toàn bộ 8 API Quản lý Nhóm (Group Admin) vừa được Backend triển khai, bao gồm input, output, các mã lỗi và những lưu ý quan trọng để team Frontend (FE) tích hợp.

---

## Tổng Quan Cấu Trúc Trả Về

Tất cả các API đều trả về format chuẩn của hệ thống (`ApiResponse<T>`):

```json
{
  "code": 200,
  "success": true,
  "message": "Thành công",
  "data": { ... } // Tùy thuộc vào từng API
}
```

**Các mã lỗi (HTTP Status & Lỗi hệ thống) chung:**
- `401 Unauthorized`: Lỗi Token không hợp lệ hoặc đã hết hạn.
- `403 Forbidden`: User gọi API **không phải là ADMIN** của nhóm.
- `404 Not Found`: Không tìm thấy nhóm (`groupId` không tồn tại hoặc nhóm đã bị xóa/ẩn).

> [!IMPORTANT]
> - Mọi API dưới đây đều yêu cầu truyền header `Authorization: Bearer <jwt_token>`.
> - Base Path prefix thông qua API Gateway là: `http://localhost:8080/users`
> - Do đó, đường dẫn đầy đủ của các API bên dưới sẽ bắt đầu bằng: `http://localhost:8080/users/groups/{groupId}/admin/...`

---

## Chi Tiết Các API

### 1. Lấy Thông Tin Nhóm (Admin View)

API trả về thông tin chi tiết của nhóm từ góc nhìn quản trị.

- **Method:** `GET`
- **Path:** `/groups/{groupId}/admin/info`
- **Query Params:**
  - `userId` (Integer): ID của user đang đăng nhập.
- **Response Data (`GroupAdminInfoDTO`):**
  ```json
  {
    "id": 1,
    "name": "Tên Nhóm",
    "avatarUrl": "https://...",
    "coverUrl": "https://...",
    "privacy": "PRIVATE",
    "memberCount": 150, // Chỉ đếm những thành viên có status = 'APPROVED'
    "description": "Mô tả nhóm...",
    "createdAt": "2024-01-15T08:00:00Z",
    "role": "ADMIN"
  }
  ```

### 2. Lấy Thống Kê Nhóm (Dashboard)

API trả về các chỉ số thống kê hoạt động của nhóm trong 7 ngày qua.

- **Method:** `GET`
- **Path:** `/groups/{groupId}/admin/stats`
- **Query Params:**
  - `userId` (Integer): ID của user đang đăng nhập.
- **Response Data (`GroupStatsDTO`):**
  ```json
  {
    "pendingReviews": 5, // Tổng memberRequests + pendingPosts
    "reportedContent": 0, // Hiện tại Backend đang fix cứng là 0
    "pendingPosts": 3,
    "memberRequests": 2,
    "groupStatusViolations": 0, // Hiện tại Backend đang fix cứng là 0
    "moderationNotifications": 0, // Hiện tại Backend đang fix cứng là 0
    "weeklyPosts": 15,
    "weeklyPostsChange": 12.5, // % thay đổi so với 7 ngày trước đó
    "weeklyComments": 45,
    "weeklyCommentsChange": -5.0,
    "weeklyReactions": 120,
    "weeklyReactionsChange": 10.2,
    "activeMembers": 0, // Hiện tại chưa có bảng tracking activity log
    "activeMembersChange": 0.0,
    "weeklyActivity": [ // Mảng 7 ngày để vẽ biểu đồ
      { "label": "CN", "value": 2 },
      { "label": "T2", "value": 5 }
      // ...
    ]
  }
  ```
  > [!NOTE]
  > Theo yêu cầu, Backend tạm thời bỏ qua các logic xử lý report, violation, activeMembers do chưa có cấu trúc bảng tương ứng. Dữ liệu trả về sẽ luôn là `0` hoặc `0.0`.

### 3. Danh Sách Yêu Cầu Tham Gia Nhóm

Lấy danh sách các user đang gửi yêu cầu xin vào nhóm (trạng thái `PENDING`).

- **Method:** `GET`
- **Path:** `/groups/{groupId}/admin/member-requests`
- **Query Params:**
  - `userId` (Integer)
  - `search` (String, Optional): Tìm kiếm theo tên user.
  - `gender` (String, Optional): Lọc theo giới tính (`MALE`, `FEMALE`, `OTHER`, `ALL`).
  - `sort` (String, Optional): Sắp xếp (`newest` hoặc `oldest`). Mặc định: `newest`.
  - `page` (Integer, Optional): Mặc định `0`.
  - `size` (Integer, Optional): Mặc định `20`.
- **Response Data (`PageResponse<MemberRequestDTO>`):**
  ```json
  {
    "data": [
      {
        "id": 101, // Dùng để approve/reject (đây là userId)
        "userId": 101,
        "username": "Nguyen Van A",
        "avatarUrl": "https://...",
        "requestedAt": "2024-06-14T10:00:00Z",
        "gender": "MALE",
        "joinedPlatformAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
  ```

### 4. Phê Duyệt Yêu Cầu Tham Gia

Chấp nhận hàng loạt các yêu cầu xin vào nhóm.

- **Method:** `POST`
- **Path:** `/groups/{groupId}/admin/member-requests/approve`
- **Query Params:**
  - `userId` (Integer)
- **Body Request (`MemberRequestActionRequest`):**
  ```json
  {
    "requestIds": [101, 102] // Danh sách các ID của user cần duyệt
  }
  ```
- **Response:**
  - Data là `null`.
  - `message`: "Đã phê duyệt X thành viên".

### 5. Từ Chối Yêu Cầu Tham Gia

Từ chối hàng loạt các yêu cầu xin vào nhóm.

- **Method:** `POST`
- **Path:** `/groups/{groupId}/admin/member-requests/reject`
- **Query Params:**
  - `userId` (Integer)
- **Body Request (`MemberRequestActionRequest`):**
  ```json
  {
    "requestIds": [103] // Danh sách các ID của user bị từ chối
  }
  ```
- **Response:**
  - Data là `null`.
  - `message`: "Đã từ chối Y yêu cầu".

### 6. Danh Sách Bài Viết Chờ Duyệt

Lấy danh sách các bài viết đang chờ phê duyệt.

- **Method:** `GET`
- **Path:** `/groups/{groupId}/admin/pending-posts`
- **Query Params:**
  - `userId` (Integer)
  - `page` (Integer, Optional): Mặc định `0`.
  - `size` (Integer, Optional): Mặc định `10`.
- **Response Data (`PageResponse<PendingPostDTO>`):**
  ```json
  {
    "data": [
      {
        "id": 500, // ID của bài viết
        "authorId": 101,
        "authorName": "Nguyen Van A",
        "authorAvatarUrl": "https://...",
        "content": "Nội dung bài viết",
        "createdAt": "2024-06-15T09:00:00Z",
        "status": "PENDING"
      }
    ],
    // Pagination fields...
  }
  ```
  > [!NOTE]
  > Theo thống nhất, field `imageUrls` đã được gỡ bỏ khỏi DTO do chưa có bảng `post_images`.

### 7. Phê Duyệt Bài Viết

Phê duyệt 1 bài viết để nó xuất hiện trên News Feed của nhóm.

- **Method:** `POST`
- **Path:** `/groups/{groupId}/admin/pending-posts/{postId}/approve`
- **Query Params:**
  - `userId` (Integer)
- **Body Request:** Không có.
- **Response:** 
  - Data là `null`.
  - `message`: "Đã phê duyệt bài viết".

### 8. Từ Chối Bài Viết

Từ chối bài viết, đánh dấu là `REJECTED`.

- **Method:** `POST`
- **Path:** `/groups/{groupId}/admin/pending-posts/{postId}/reject`
- **Query Params:**
  - `userId` (Integer)
- **Body Request:** Không có.
- **Response:** 
  - Data là `null`.
  - `message`: "Đã từ chối bài viết".

---

## Các Lưu Ý (Take Care) Quan Trọng Dành Cho Frontend

> [!WARNING]
> **1. Thay Đổi Trong Logic Xin Vào Nhóm (`/groups/{groupId}/join`)**
> - **Trước đây:** API chỉ trả về `HTTP 200` với data `null`. User bấm Join là sẽ vào nhóm ngay (status auto là APPROVED).
> - **Hiện tại:** API trả về một object chỉ định rõ status:
>   ```json
>   {
>     "code": 200,
>     "success": true,
>     "message": "Thao tác thành công!",
>     "data": {
>       "status": "PENDING" // hoặc "APPROVED"
>     }
>   }
>   ```
>   - Nếu nhóm là `PUBLIC`: status sẽ là `APPROVED` và join vào ngay.
>   - Nếu nhóm là `PRIVATE`: status sẽ là `PENDING`. Dựa vào response này, FE cần cập nhật hiển thị trạng thái nút là *"Đang chờ duyệt"*.
>
> Ngoài ra, trong model **`GroupDTO`** (hiển thị thông tin nhóm), Backend đã bổ sung thêm 1 thuộc tính: `isPending: boolean`. Nếu `isPending = true` có nghĩa là user hiện tại đã gửi yêu cầu và đang chờ duyệt. FE có thể đưa field này vào `IGroup` interface.

> [!TIP]
> **2. Member Count & Feed Bài Viết Đã Bị Thay Đổi Cách Tính**
> - Backend đã thay đổi logic đếm `memberCount`: Giờ đây chỉ những user có `status = 'APPROVED'` mới được đếm.
> - Cả API `/groups/{groupId}/posts` (Feed của 1 nhóm) lẫn API bảng tin chung đều đã bổ sung điều kiện chỉ hiển thị những bài viết có `status = 'APPROVED'`. Những bài `PENDING` sẽ không xuất hiện nữa.

> [!CAUTION]
> **3. Xử lý Lỗi / Optimistic UI**
> - FE đang sử dụng **Optimistic UI** (Hiển thị thay đổi ngay trước khi API response).
> - Trong API Approve/Reject (Bài viết hoặc Member), Backend có thể trả về lỗi 400 (Bad Request) nếu bài viết không thuộc nhóm đó hoặc bài viết không nằm ở trạng thái `PENDING`. FE cần có cơ chế rollback giao diện và hiển thị Toast nếu API gọi về báo lỗi.

> [!NOTE]
> **4. Về Tính Năng Thống Kê (Dashboard)**
> - Những tính năng liên quan đến việc Tố cáo (Reported Content), Cảnh báo nhóm (Violations) và User Đang Hoạt Động (Active Members) hiện thời chưa được Backend tính toán (do thiếu thiết kế Schema từ giai đoạn đầu). 
> - Những giá trị này trả về mặc định `0`, FE cứ bind dữ liệu bình thường, sau này BE update logic tính toán thì biểu đồ sẽ tự nhảy số.

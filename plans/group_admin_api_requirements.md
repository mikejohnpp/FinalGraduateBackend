# Yêu cầu API Backend — Tính năng Quản lý Nhóm (Group Admin)

> Tài liệu này mô tả chi tiết các API endpoint mà Backend cần triển khai để phục vụ tính năng **Quản lý Nhóm** trên Frontend.
> Frontend đã hoàn thành giao diện với mock data và sẵn sàng tích hợp khi API được cung cấp.

---

## Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Quy ước chung](#2-quy-ước-chung)
3. [API 1 — Lấy thông tin nhóm (Admin view)](#api-1--lấy-thông-tin-nhóm-admin-view)
4. [API 2 — Lấy thống kê nhóm (Dashboard)](#api-2--lấy-thống-kê-nhóm-dashboard)
5. [API 3 — Danh sách yêu cầu tham gia nhóm](#api-3--danh-sách-yêu-cầu-tham-gia-nhóm)
6. [API 4 — Phê duyệt yêu cầu tham gia](#api-4--phê-duyệt-yêu-cầu-tham-gia)
7. [API 5 — Từ chối yêu cầu tham gia](#api-5--từ-chối-yêu-cầu-tham-gia)
8. [API 6 — Danh sách bài viết chờ duyệt](#api-6--danh-sách-bài-viết-chờ-duyệt)
9. [API 7 — Phê duyệt bài viết](#api-7--phê-duyệt-bài-viết)
10. [API 8 — Từ chối bài viết](#api-8--từ-chối-bài-viết)
11. [Phụ lục: DTO Schemas](#phụ-lục-dto-schemas)

---

## 1. Tổng quan kiến trúc

Tính năng Quản lý Nhóm cung cấp 4 màn hình chính cho Admin/Owner của nhóm:

| Màn hình | Chức năng | API cần dùng |
|---|---|---|
| **Trang chủ cộng đồng** | Hiển thị thông tin nhóm, feed bài viết | API 1 (+ API POST hiện có) |
| **Tổng quan (Dashboard)** | Thống kê hoạt động nhóm 7 ngày | API 1, API 2 |
| **Yêu cầu làm thành viên** | Xem, tìm kiếm, phê duyệt/từ chối | API 3, API 4, API 5 |
| **Bài viết đang chờ** | Xem, phê duyệt/từ chối bài viết | API 6, API 7, API 8 |

---

## 2. Quy ước chung

### Base URL
```
{VITE_SERVER_API} = http://localhost:8080
```

### Đề xuất path prefix
```
users/groups/{groupId}/admin/...
```

### Response format chuẩn
Tất cả các response phải tuân theo cấu trúc `ApiResultGeneric<T>`:

```json
{
  "code": 200,
  "success": true,
  "message": "Thành công",
  "data": { ... }
}
```

Trường hợp lỗi:
```json
{
  "code": 403,
  "success": false,
  "message": "Bạn không có quyền quản trị nhóm này",
  "data": null
}
```

### Authentication
- Tất cả API đều yêu cầu JWT access token qua header `Authorization: Bearer <token>`.
- BE cần kiểm tra **user gọi API phải là ADMIN của nhóm** (trừ khi ghi chú khác).

### Phân quyền (Roles)
Chỉ có 2 role trong nhóm:
- **`ADMIN`** — Quản trị viên (có quyền phê duyệt/từ chối)
- **`MEMBER`** — Thành viên thường

> [!IMPORTANT]
> Không có role MODERATOR. Mọi thao tác quản trị chỉ dành cho ADMIN.

---

## API 1 — Lấy thông tin nhóm (Admin view)

Trả về thông tin chi tiết nhóm dưới góc nhìn quản trị.

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `GET` |
| **Path** | `users/groups/{groupId}/admin/info` |
| **Phân quyền** | ADMIN của nhóm |
| **Query Params** | Không có |

### Response Body — `ApiResultGeneric<GroupAdminInfoDTO>`

```json
{
  "code": 200,
  "success": true,
  "data": {
    "id": 1,
    "name": "Nhóm test",
    "avatarUrl": "https://...",
    "coverUrl": "https://...",
    "privacy": "PRIVATE",
    "memberCount": 150,
    "description": "Đây là nhóm thử nghiệm...",
    "createdAt": "2024-01-15T08:00:00.000Z",
    "role": "ADMIN"
  }
}
```

### Bảng field chi tiết

| Field | Type | Nullable | Mô tả |
|---|---|---|---|
| `id` | `number` | ❌ | ID nhóm |
| `name` | `string` | ❌ | Tên nhóm |
| `avatarUrl` | `string` | ✅ | URL ảnh đại diện nhóm |
| `coverUrl` | `string` | ✅ | URL ảnh bìa nhóm |
| `privacy` | `enum` | ❌ | `"PUBLIC"` hoặc `"PRIVATE"` |
| `memberCount` | `number` | ❌ | Tổng số thành viên |
| `description` | `string` | ✅ | Mô tả nhóm |
| `createdAt` | `string (ISO 8601)` | ❌ | Ngày tạo nhóm |
| `role` | `enum` | ❌ | Role của user đang request: `"ADMIN"` hoặc `"MEMBER"` |

### Error cases

| HTTP Code | Mô tả |
|---|---|
| `403` | User không phải ADMIN của nhóm |
| `404` | Không tìm thấy nhóm |

---

## API 2 — Lấy thống kê nhóm (Dashboard)

Trả về các chỉ số thống kê hoạt động nhóm **trong 7 ngày qua**, bao gồm biểu đồ hoạt động hàng ngày.

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `GET` |
| **Path** | `users/groups/{groupId}/admin/stats` |
| **Phân quyền** | ADMIN của nhóm |
| **Query Params** | Không có |

### Response Body — `ApiResultGeneric<GroupStatsDTO>`

```json
{
  "code": 200,
  "success": true,
  "data": {
    "pendingReviews": 5,
    "reportedContent": 2,
    "pendingPosts": 3,
    "memberRequests": 4,
    "groupStatusViolations": 0,
    "moderationNotifications": 1,
    "weeklyPosts": 8,
    "weeklyPostsChange": 12.5,
    "weeklyComments": 24,
    "weeklyCommentsChange": -5.3,
    "weeklyReactions": 47,
    "weeklyReactionsChange": 0,
    "activeMembers": 45,
    "activeMembersChange": 8.2,
    "weeklyActivity": [
      { "label": "T2", "value": 5 },
      { "label": "T3", "value": 12 },
      { "label": "T4", "value": 8 },
      { "label": "T5", "value": 15 },
      { "label": "T6", "value": 20 },
      { "label": "T7", "value": 10 },
      { "label": "CN", "value": 3 }
    ]
  }
}
```

### Bảng field chi tiết

| Field | Type | Mô tả |
|---|---|---|
| `pendingReviews` | `number` | Tổng số mục cần xem xét (= reportedContent + pendingPosts + memberRequests) |
| `reportedContent` | `number` | Số nội dung bị báo cáo |
| `pendingPosts` | `number` | Số bài viết đang chờ duyệt |
| `memberRequests` | `number` | Số yêu cầu tham gia nhóm đang chờ |
| `groupStatusViolations` | `number` | Số trường hợp vi phạm trạng thái nhóm |
| `moderationNotifications` | `number` | Số thông báo kiểm duyệt |
| `weeklyPosts` | `number` | Tổng bài viết trong 7 ngày qua |
| `weeklyPostsChange` | `number` | % thay đổi so với 7 ngày trước đó (VD: `12.5` = tăng 12.5%, `-5.3` = giảm 5.3%) |
| `weeklyComments` | `number` | Tổng bình luận trong 7 ngày qua |
| `weeklyCommentsChange` | `number` | % thay đổi bình luận |
| `weeklyReactions` | `number` | Tổng lượt cảm xúc trong 7 ngày qua |
| `weeklyReactionsChange` | `number` | % thay đổi cảm xúc |
| `activeMembers` | `number` | Số thành viên hoạt động trong 7 ngày qua |
| `activeMembersChange` | `number` | % thay đổi thành viên hoạt động |
| `weeklyActivity` | `Array<{label, value}>` | Mảng 7 phần tử, mỗi phần tử chứa `label` (tên ngày viết tắt) và `value` (tổng hoạt động ngày đó). Dùng để vẽ biểu đồ cột (BarChart). |

> [!TIP]
> **Cách tính `weeklyPostsChange`**: `((weeklyPosts_current - weeklyPosts_previous) / weeklyPosts_previous) * 100`. Nếu tuần trước = 0, trả về `0`.
>
> **Cách tính `activeMembers`**: Đếm số thành viên có ít nhất 1 hành động (xem, đăng bài, bình luận, thả cảm xúc) trong nhóm trong 7 ngày qua.

---

## API 3 — Danh sách yêu cầu tham gia nhóm

Trả về danh sách người dùng đang chờ được phê duyệt vào nhóm.

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `GET` |
| **Path** | `users/groups/{groupId}/admin/member-requests` |
| **Phân quyền** | ADMIN của nhóm |

### Query Params

| Param | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `search` | `string` | ❌ | Tìm kiếm theo tên người dùng (so khớp mờ - LIKE) |
| `gender` | `string` | ❌ | Lọc theo giới tính: `MALE`, `FEMALE`, `OTHER`. Bỏ qua hoặc truyền `ALL` để không lọc. |
| `sort` | `string` | ❌ | Sắp xếp: `newest` (mặc định) hoặc `oldest` — theo `requestedAt` |
| `page` | `number` | ❌ | Trang hiện tại (mặc định: `0`) |
| `size` | `number` | ❌ | Số item/trang (mặc định: `20`) |

### Response Body — `ApiResultGeneric<Page<MemberRequestDTO>>`

```json
{
  "code": 200,
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "userId": 101,
        "username": "Nguyễn Văn An",
        "avatarUrl": "https://...",
        "requestedAt": "2025-06-13T10:30:00.000Z",
        "gender": "MALE",
        "joinedPlatformAt": "2019-03-22T00:00:00.000Z"
      }
    ],
    "totalElements": 4,
    "totalPages": 1,
    "number": 0,
    "size": 20
  }
}
```

### Bảng field `MemberRequestDTO`

| Field | Type | Nullable | Mô tả |
|---|---|---|---|
| `id` | `number` | ❌ | ID của bản ghi yêu cầu (dùng để approve/reject) |
| `userId` | `number` | ❌ | ID user trên hệ thống |
| `username` | `string` | ❌ | Tên hiển thị của user |
| `avatarUrl` | `string` | ✅ | URL avatar. FE sẽ hiện chữ cái đầu nếu null. |
| `requestedAt` | `string (ISO 8601)` | ❌ | Thời điểm gửi yêu cầu tham gia |
| `gender` | `enum` | ✅ | `"MALE"`, `"FEMALE"`, `"OTHER"` hoặc `null` |
| `joinedPlatformAt` | `string (ISO 8601)` | ✅ | Ngày user tham gia nền tảng. FE hiển thị "Đã tham gia nền tảng từ Tháng X, YYYY". |

---

## API 4 — Phê duyệt yêu cầu tham gia

Chấp nhận 1 hoặc nhiều yêu cầu tham gia nhóm. User được duyệt sẽ trở thành `MEMBER` của nhóm.

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `POST` |
| **Path** | `users/groups/{groupId}/admin/member-requests/approve` |
| **Phân quyền** | ADMIN của nhóm |

### Request Body

```json
{
  "requestIds": [1, 2, 3]
}
```

| Field | Type | Mô tả |
|---|---|---|
| `requestIds` | `number[]` | Mảng ID các yêu cầu cần phê duyệt. Hỗ trợ cả đơn lẻ `[1]` và hàng loạt `[1,2,3]`. |

### Response Body — `ApiResult`

```json
{
  "code": 200,
  "success": true,
  "message": "Đã phê duyệt 1 thành viên"
}
```

### Business Logic

- Thay đổi trạng thái yêu cầu → `APPROVED`
- Thêm user vào danh sách thành viên nhóm với role `MEMBER`
- Cập nhật `memberCount` của nhóm (+1 cho mỗi yêu cầu được duyệt)
- (Tùy chọn) Gửi thông báo cho user rằng yêu cầu đã được chấp nhận

### Error cases

| HTTP Code | Mô tả |
|---|---|
| `400` | `requestIds` trống hoặc chứa ID không hợp lệ |
| `403` | User không phải ADMIN |
| `404` | Nhóm không tồn tại hoặc yêu cầu không tồn tại |

---

## API 5 — Từ chối yêu cầu tham gia

Từ chối 1 hoặc nhiều yêu cầu tham gia nhóm.

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `POST` |
| **Path** | `users/groups/{groupId}/admin/member-requests/reject` |
| **Phân quyền** | ADMIN của nhóm |

### Request Body

```json
{
  "requestIds": [1]
}
```

### Response Body — `ApiResult`

```json
{
  "code": 200,
  "success": true,
  "message": "Đã từ chối 1 yêu cầu"
}
```

### Business Logic

- Thay đổi trạng thái yêu cầu → `REJECTED`
- **Không** thêm user vào nhóm
- (Tùy chọn) Gửi thông báo cho user rằng yêu cầu bị từ chối
- User có thể gửi lại yêu cầu sau này

---

## API 6 — Danh sách bài viết chờ duyệt

Trả về danh sách bài viết có trạng thái `PENDING` trong nhóm.

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `GET` |
| **Path** | `users/groups/{groupId}/admin/pending-posts` |
| **Phân quyền** | ADMIN của nhóm |

### Query Params

| Param | Type | Bắt buộc | Mô tả |
|---|---|---|---|
| `page` | `number` | ❌ | Trang hiện tại (mặc định: `0`) |
| `size` | `number` | ❌ | Số item/trang (mặc định: `10`) |

### Response Body — `ApiResultGeneric<Page<PendingPostDTO>>`

```json
{
  "code": 200,
  "success": true,
  "data": {
    "content": [
      {
        "id": 201,
        "authorId": 101,
        "authorName": "Nguyễn Văn An",
        "authorAvatarUrl": "https://...",
        "content": "Chào cả nhóm! Mình vừa tìm được...",
        "imageUrls": ["https://picsum.photos/seed/post201/800/450"],
        "createdAt": "2025-06-15T06:20:00.000Z",
        "status": "PENDING"
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "number": 0,
    "size": 10
  }
}
```

### Bảng field `PendingPostDTO`

| Field | Type | Nullable | Mô tả |
|---|---|---|---|
| `id` | `number` | ❌ | ID bài viết |
| `authorId` | `number` | ❌ | ID tác giả |
| `authorName` | `string` | ❌ | Tên hiển thị tác giả |
| `authorAvatarUrl` | `string` | ✅ | URL avatar tác giả |
| `content` | `string` | ❌ | Nội dung bài viết |
| `imageUrls` | `string[]` | ✅ | Mảng URL ảnh đính kèm. Trả `[]` hoặc `null` nếu không có ảnh. |
| `createdAt` | `string (ISO 8601)` | ❌ | Thời điểm tạo bài viết |
| `status` | `enum` | ❌ | Trạng thái: `"PENDING"`, `"APPROVED"`, `"REJECTED"` |

---

## API 7 — Phê duyệt bài viết

Phê duyệt bài viết để đăng lên feed nhóm.

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `POST` |
| **Path** | `users/groups/{groupId}/admin/pending-posts/{postId}/approve` |
| **Phân quyền** | ADMIN của nhóm |

### Request Body
Không cần body.

### Response Body — `ApiResult`

```json
{
  "code": 200,
  "success": true,
  "message": "Đã phê duyệt bài viết"
}
```

### Business Logic

- Cập nhật `status` bài viết: `PENDING` → `APPROVED`
- Bài viết sẽ xuất hiện trên feed nhóm
- (Tùy chọn) Gửi thông báo cho tác giả rằng bài viết đã được duyệt

---

## API 8 — Từ chối bài viết

Từ chối bài viết, không cho đăng lên feed nhóm.

| Thuộc tính | Giá trị |
|---|---|
| **Method** | `POST` |
| **Path** | `users/groups/{groupId}/admin/pending-posts/{postId}/reject` |
| **Phân quyền** | ADMIN của nhóm |

### Request Body
Không cần body.

### Response Body — `ApiResult`

```json
{
  "code": 200,
  "success": true,
  "message": "Đã từ chối bài viết"
}
```

### Business Logic

- Cập nhật `status` bài viết: `PENDING` → `REJECTED`
- Bài viết **không** xuất hiện trên feed
- (Tùy chọn) Gửi thông báo cho tác giả rằng bài viết bị từ chối

---

## Phụ lục: DTO Schemas

### Tóm tắt tất cả DTO

```
GroupAdminInfoDTO       → API 1
GroupStatsDTO           → API 2
MemberRequestDTO        → API 3
PendingPostDTO          → API 6
```

### Bảng đối chiếu FE Interface ↔ BE DTO

| FE Interface | BE DTO | File FE |
|---|---|---|
| `IGroupAdmin` | `GroupAdminInfoDTO` | `src/types/interfaces/group/IGroupAdmin.ts` |
| `IGroupStats` | `GroupStatsDTO` | `src/types/interfaces/group/IGroupStats.ts` |
| `IGroupAdminMember` | `MemberRequestDTO` | `src/types/interfaces/group/IGroupAdminMember.ts` |
| `IGroupAdminPost` | `PendingPostDTO` | `src/types/interfaces/group/IGroupAdminPost.ts` |

### Tóm tắt tất cả Endpoints

| # | Method | Path | Mô tả |
|---|---|---|---|
| 1 | `GET` | `users/groups/{groupId}/admin/info` | Thông tin nhóm (admin view) |
| 2 | `GET` | `users/groups/{groupId}/admin/stats` | Thống kê nhóm 7 ngày |
| 3 | `GET` | `users/groups/{groupId}/admin/member-requests` | Danh sách yêu cầu tham gia |
| 4 | `POST` | `users/groups/{groupId}/admin/member-requests/approve` | Phê duyệt yêu cầu |
| 5 | `POST` | `users/groups/{groupId}/admin/member-requests/reject` | Từ chối yêu cầu |
| 6 | `GET` | `users/groups/{groupId}/admin/pending-posts` | Danh sách bài viết chờ duyệt |
| 7 | `POST` | `users/groups/{groupId}/admin/pending-posts/{postId}/approve` | Phê duyệt bài viết |
| 8 | `POST` | `users/groups/{groupId}/admin/pending-posts/{postId}/reject` | Từ chối bài viết |

> [!NOTE]
> FE hiện đang sử dụng mock data với Optimistic UI (cập nhật giao diện ngay lập tức khi thao tác). Khi tích hợp API thực, FE sẽ:
> 1. Gọi API → nếu thành công → cập nhật UI
> 2. Gọi API → nếu thất bại → rollback UI và hiển thị toast lỗi

> [!IMPORTANT]
> Tất cả 8 API đều yêu cầu kiểm tra phân quyền: **chỉ user có role `ADMIN` trong nhóm mới được truy cập**. Nếu không phải ADMIN, trả `403 Forbidden`.

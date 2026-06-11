# Yêu Cầu Backend API — Tính Năng Profile (Trang Cá Nhân)

Dựa trên cấu trúc UI/UX tại `src/views/profile/`, type definitions, và code thực tế đã có trong project.

---

## Review Notes — Những vấn đề cần lưu ý

> [!IMPORTANT]
> **Backend đã có sẵn endpoint `GET /users/{userId}/profile`** — `userService.ts` (line 61-68) đã implement method `getProfile(userId)` trả về `UserProfileDTO`. Tuy nhiên, `UserProfileDTO` hiện tại **rất thiếu trường** so với những gì UI cần. Cần mở rộng DTO này thay vì tạo mới.

> [!WARNING]
> **Hai hệ thống type đang chồng chéo:**
> - `UserProfileDTO` (`src/types/interfaces/user/UserProfileDTO.ts`) — đã được BE trả về, đang dùng trong Redux store (`userSlice.profile`). Có các trường: `id`, `userName`, `nickName`, `avatar`, `email`, `phoneNumber`, `dateOfBirth`, `role`, `isActive`.
> - `UserProfile` (`src/types/Profile.ts`) — mock type dùng riêng cho UI profile page. Có các trường: `id`, `name`, `avatar`, `coverPhoto`, `friendCount`, `bio`, `location`, `education`, `workplace`, `hometown`, `birthday`, `relationship`, `gender`, `pronouns`, `language`, `isOwner`.
>
> Khi BE implement, cần **hợp nhất 2 type này** hoặc mở rộng `UserProfileDTO` để chứa đủ thông tin cho trang Profile.

> [!NOTE]
> **`isOwner` không cần BE trả về** — FE tự tính bằng cách so sánh `profileDTO.id` với `userSlice.userId` trong Redux store. Đây là cách tiếp cận đúng.

---

## 1. Data Transfer Objects (DTOs)

### 1.1 `UserProfileDTO` (Mở rộng — Response)

Mở rộng DTO hiện tại tại BE. Dưới đây là **tất cả các trường** mà FE đang cần, đánh dấu trường nào đã có (✅) và trường nào cần thêm (🆕):

```java
public class UserProfileDTO {
    // ✅ Đã có
    private Long id;
    private String userName;           // FE map thành "name"
    private String nickName;
    private String avatar;
    private String email;
    private Long phoneNumber;
    private String dateOfBirth;        // FE dùng cho "birthday"
    private String role;
    private boolean isActive;

    // 🆕 Cần thêm — dùng trong ProfileCover
    private String coverPhoto;         // URL ảnh bìa
    private Integer friendCount;       // Số lượng bạn bè (denormalized count)

    // 🆕 Cần thêm — dùng trong ProfileAbout + ProfileEditPanel
    private String bio;                // Tiểu sử ngắn (max 101 ký tự)
    private String location;           // Nơi sống hiện tại
    private String education;          // Trường học
    private String workplace;          // Nơi làm việc
    private String hometown;           // Quê quán
    private String relationship;       // Tình trạng mối quan hệ (Độc thân, Hẹn hò, ...)
    private String gender;             // Giới tính
    private String pronouns;           // Danh xưng
    private String language;           // Ngôn ngữ
}
```

### 1.2 `ProfileUpdateDTO` (Request Body — Cập nhật thông tin)

Dùng khi user lưu thay đổi từ `ProfileEditPanel`. Chỉ chứa các trường **user được phép tự sửa**:

```java
public class ProfileUpdateDTO {
    private String bio;                // max 101 ký tự
    private String location;
    private String hometown;
    private String birthday;           // format: yyyy-MM-dd hoặc ISO string
    private String relationship;
    private String gender;
    private String pronouns;
    private String language;
    // Lưu ý: education và workplace hiện không có trong EditPanel
    // nhưng có hiển thị trong ProfileAbout → cân nhắc thêm vào
}
```

> [!TIP]
> `education` và `workplace` hiện **chỉ hiển thị** trong `ProfileAbout` nhưng **không có** trong form chỉnh sửa (`ProfileEditPanel`). BE nên hỗ trợ sẵn 2 trường này trong `ProfileUpdateDTO` để FE dễ bổ sung form sau.

---

## 2. API Endpoints

Tất cả response phải bọc trong `ApiResultGeneric<T>`.

### 2.1. Lấy thông tin Profile

| | |
|---|---|
| **Endpoint** | `GET /users/{userId}/profile` |
| **Status** | ⚠️ **Đã có nhưng cần mở rộng response** |
| **Mô tả** | Trả về toàn bộ thông tin profile bao gồm `friendCount`, `bio`, `coverPhoto`, etc. |
| **FE đang gọi tại** | `userService.getProfile(userId)` — line 61 |
| **Response** | `ApiResultGeneric<UserProfileDTO>` (bản mở rộng) |

### 2.2. Cập nhật thông tin cá nhân

| | |
|---|---|
| **Endpoint** | `PUT /users/profile` |
| **Status** | 🆕 Cần tạo mới |
| **Auth** | Lấy userId từ JWT token, không cần truyền trong URL |
| **Request Body** | `ProfileUpdateDTO` (JSON) |
| **Response** | `ApiResultGeneric<UserProfileDTO>` (trả về profile đầy đủ sau update) |
| **Validation** | `bio` ≤ 101 ký tự |

### 2.3. Upload Ảnh Bìa (Cover Photo)

| | |
|---|---|
| **Endpoint** | `POST /users/profile/cover` |
| **Status** | 🆕 Cần tạo mới |
| **Content-Type** | `multipart/form-data` |
| **Payload** | `file` (MultipartFile) |
| **Response** | `ApiResultGeneric<String>` — URL ảnh bìa mới |
| **FE sẽ dùng** | `http.postWithFile()` |
| **Lưu ý** | FE hiện đọc file bằng `FileReader.readAsDataURL()` → chỉ lưu local. Cần chuyển sang upload thực qua API. |

### 2.4. Upload Ảnh Đại Diện (Avatar)

| | |
|---|---|
| **Endpoint** | `POST /users/profile/avatar` |
| **Status** | 🆕 Cần tạo mới |
| **Content-Type** | `multipart/form-data` |
| **Payload** | `file` (MultipartFile) |
| **Response** | `ApiResultGeneric<String>` — URL avatar mới |
| **Lưu ý** | UI chưa có nút đổi avatar nhưng nên implement sẵn |

---

## 3. API Tích Hợp Liên Quan (Các tab con trong Profile)

### 3.1. Bài viết của User — Tab "Bài viết"

| | |
|---|---|
| **Endpoint** | `GET /users/posts?userId={userId}&cursor={cursor}&limit={limit}` |
| **Status** | ⚠️ Cần kiểm tra — `POST.BASE` (`users/posts`) đã có nhưng cần hỗ trợ filter theo `userId` |
| **Mô tả** | Lấy bài viết chỉ của 1 user (khác với suggested feed lấy chung). Infinite scroll. |
| **Response** | `ApiResultGeneric<CursorPageResponse<PostSummaryDTO>>` |
| **UI component** | `ProfilePostFeed` — nhận `IPost[]`, render `PostCard` |

> [!IMPORTANT]
> Hiện tại `Profile.tsx` đang dùng `mockPosts` rồi map thủ công sang `IPost[]` (line 24-36). Khi BE sẵn sàng, FE sẽ tạo hook `useUserPosts(userId)` gọi endpoint này.

### 3.2. Ảnh của User — Tab "Ảnh" & Album Modal

| | |
|---|---|
| **Endpoint** | `GET /users/{userId}/photos?cursor={cursor}&limit={limit}` |
| **Status** | 🆕 Cần tạo mới |
| **Mô tả** | Trả về danh sách ảnh user đã đăng (trích xuất từ các bài viết có ảnh). |
| **Response** | `ApiResultGeneric<CursorPageResponse<PhotoDTO>>` |
| **Cần định nghĩa** | `PhotoDTO { id, url, createdAt, postId? }` |
| **Dùng tại** | `ProfilePhotos` (grid 3 cột, 9 ảnh preview) + `CoverPhotoAlbumModal` (chọn ảnh làm cover) |

### 3.3. Danh sách Bạn bè — Tab "Bạn bè"

| | |
|---|---|
| **Endpoint** | `GET /users/friends?userId={userId}&page={page}&size={size}` |
| **Status** | ⚠️ Kiểm tra — `FRIEND.BASE` (`users/friends`) đã có, cần hỗ trợ query theo `userId` khác (xem profile người khác) |
| **Response** | `ApiResultGeneric<PageResponse<FriendDTO>>` hoặc tái sử dụng `IAuthor[]` |
| **UI component** | `ProfileFriends` — hiển thị grid 3x2 `FriendMiniCard` (chỉ cần `id`, `name`, `avatar`) |

> [!NOTE]
> `FriendMiniCard` chỉ cần `Pick<UserProfile, 'id' | 'name' | 'avatar'>` — rất nhẹ, có thể tái sử dụng `IAuthor` thay vì tạo DTO mới.

---

## 4. Tổng Hợp Công Việc BE

| # | Endpoint | Method | Priority | Status |
|---|---|---|---|---|
| 1 | `/users/{userId}/profile` | GET | 🔴 Cao | ⚠️ Mở rộng response |
| 2 | `/users/profile` | PUT | 🔴 Cao | 🆕 Mới |
| 3 | `/users/profile/cover` | POST | 🟡 Trung bình | 🆕 Mới |
| 4 | `/users/profile/avatar` | POST | 🟡 Trung bình | 🆕 Mới |
| 5 | `/users/posts?userId=...` | GET | 🔴 Cao | ⚠️ Thêm filter |
| 6 | `/users/{userId}/photos` | GET | 🟢 Thấp | 🆕 Mới |
| 7 | `/users/friends?userId=...` | GET | 🟡 Trung bình | ⚠️ Thêm filter |

### Database Migration cần thiết

```sql
-- Thêm các cột vào bảng users (nếu chưa có)
ALTER TABLE users ADD COLUMN cover_photo VARCHAR(500);
ALTER TABLE users ADD COLUMN bio VARCHAR(101);
ALTER TABLE users ADD COLUMN location VARCHAR(100);
ALTER TABLE users ADD COLUMN education VARCHAR(200);
ALTER TABLE users ADD COLUMN workplace VARCHAR(200);
ALTER TABLE users ADD COLUMN hometown VARCHAR(100);
ALTER TABLE users ADD COLUMN relationship VARCHAR(50);
ALTER TABLE users ADD COLUMN gender VARCHAR(20);
ALTER TABLE users ADD COLUMN pronouns VARCHAR(50);
ALTER TABLE users ADD COLUMN language VARCHAR(50);
```

> [!CAUTION]
> `friendCount` **không nên** là cột trong bảng `users`. Nên query đếm từ bảng `friends` (hoặc dùng denormalized counter nếu đã có pattern này trong project). Kiểm tra xem bảng `friends` đã có mechanism đếm chưa.

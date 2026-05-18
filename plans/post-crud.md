# Plan: Post CRUD — user-service

## Phân tích Entity

### `Post`
| Trường | Kiểu | Ghi chú |
|--------|------|---------|
| `id` | `Integer` | PK, auto |
| `user` | `User` | FK `user_id`, NOT NULL — chủ bài viết |
| `isGroupPosted` | `Boolean` | default `false` — bài trong group hay không |
| `group` | `Group` | FK `group_id`, nullable — group nếu có |
| `createdAt` | `Instant` | default `CURRENT_TIMESTAMP` |
| `comments` | `Set<Comment>` | OneToMany — không trả về thẳng |
| `postDetails` | `Set<PostDetail>` | OneToMany — nội dung bài viết |

### `PostDetail`
| Trường | Kiểu | Ghi chú |
|--------|------|---------|
| `id` | `Integer` | PK |
| `post` | `Post` | FK — NOT NULL |
| `content` | `String` (Lob) | Nội dung đoạn text/media URL |

> **Nhận xét:** Một `Post` có nhiều `PostDetail` — thiết kế theo dạng "sections" (mỗi đoạn là 1 PostDetail). CRUD cần tạo Post + list PostDetail cùng lúc.

---

## Các API sẽ implement

### 1. Tạo bài viết — `POST /posts`
- **Request**: `PostCreateRequest` — nhận `userId`, `isGroupPosted`, `groupId?`, list `contents` (String)
- **Logic**: Tạo `Post`, tạo các `PostDetail` cho từng content, lưu cùng lúc (cascade hoặc tay)
- **Response**: `PostDTO`

### 2. Lấy danh sách bài viết — `GET /posts`
- Lấy tất cả bài viết (sau có thể thêm phân trang)
- **Response**: `List<PostSummaryDTO>`

### 3. Lấy chi tiết bài viết — `GET /posts/{id}`
- Lấy 1 bài kèm toàn bộ PostDetail
- **Response**: `PostDetailDTO`

### 4. Cập nhật bài viết — `PUT /posts/{id}`
- **Request**: `PostUpdateRequest` — cập nhật list contents (xóa cũ, thêm mới)
- **Response**: `PostDetailDTO`

### 5. Xóa bài viết — `DELETE /posts/{id}`
- Xóa Post (cascade xóa PostDetail)
- **Response**: message

---

## Cấu trúc file sẽ tạo

### Trong `common` module (shared)
```
common/src/main/java/org/social/common/
├── repositories/
│   ├── PostRepository.java        [NEW]
│   └── PostDetailRepository.java  [NEW]
```

### Trong `user-service` module
```
user-service/src/main/java/org/social/userservice/
├── dto/
│   └── post/
│       ├── views/
│       │   ├── PostDTO.java            [NEW] — response tạo mới, gồm id + createdAt + authorName
│       │   ├── PostSummaryDTO.java     [NEW] — response danh sách, gọn nhẹ
│       │   └── PostDetailDTO.java      [NEW] — response chi tiết, kèm list contents
│       ├── requests/
│       │   ├── PostCreateRequest.java  [NEW]
│       │   └── PostUpdateRequest.java  [NEW]
│       └── mappers/
│           └── PostMapper.java         [NEW]
├── services/
│   ├── PostService.java            [NEW]
│   └── impl/
│       └── PostServiceImpl.java    [NEW]
└── controllers/
    └── PostController.java         [NEW]
```

---

## DTO Design (theo chuẩn Java Records)

```java
// PostDTO — sau khi tạo/update
record PostDTO(Integer id, String authorName, Boolean isGroupPosted, Instant createdAt) {}

// PostSummaryDTO — danh sách
record PostSummaryDTO(Integer id, String authorName, Boolean isGroupPosted, Instant createdAt, int commentCount) {}

// PostDetailDTO — chi tiết
record PostDetailDTO(Integer id, String authorName, Boolean isGroupPosted, Instant createdAt, List<String> contents) {}

// PostCreateRequest
record PostCreateRequest(Integer userId, Boolean isGroupPosted, Integer groupId, List<String> contents) {}

// PostUpdateRequest
record PostUpdateRequest(List<String> contents) {}
```

---

## Lưu ý quan trọng

1. **Circular reference**: `Post → User → Set<Post> → ...` → Luôn dùng DTO, không trả về `Post` entity thẳng.
2. **PostDetail cascade**: Khi xóa/update Post, phải xóa hết PostDetail cũ trước khi tạo mới.
3. **`CommentRepository` hiện bị sai**: đang extend `JpaRepository<Message, Integer>` thay vì `JpaRepository<Comment, Integer>` — không liên quan đến task này nhưng cần ghi nhớ.
4. **Repository nằm trong `common`**: Cả `PostRepository` và `PostDetailRepository` phải tạo ở `common` vì `user-service` scan từ `org.social.common.repositories`.
5. **Mọi endpoint đặt tại `/posts`**: api-gateway sẽ proxy từ `/users/**` → `user-service`, nên các route trong `PostController` phải nằm dưới `/posts` (không phải `/users/posts`).

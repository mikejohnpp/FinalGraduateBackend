# VieFace JMeter Performance Benchmarking

Bộ công cụ kiểm thử tải (Load Testing & Stress Testing) tự động cho toàn bộ hệ thống dịch vụ backend của mạng xã hội **VieFace** tại đường dẫn thực tế `https://api.vieface.io.vn`.

## 1. Yêu cầu Hệ thống

- **Java Runtime Environment (JRE/JDK)**: Phục vụ chạy JMeter (Java 17 hoặc Java 21).
- **Apache JMeter 5.5+**: Cài đặt via bash:
  ```bash
  sudo apt-get update && sudo apt-get install jmeter -y
  ```

## 2. Cấu hình Dữ liệu Test

Dữ liệu đầu vào cho quá trình kiểm thử được lưu trữ tại `bench/data/`:
- **`users.csv`**: Danh sách tài khoản đã kích hoạt (Mặc định: `tahoangphuc1901@gmail.com` / `phucta1901`).
- **`post-contents.csv`**: Dữ liệu tự động phát sinh nội dung đăng bài viết test.
- **`comment-contents.csv`**: Dữ liệu bình luận cho các bài viết.

## 3. Cách Sử dụng

Di chuyển tới mục `bench` trong project:
```bash
cd dev/FinalGraduateBackend/bench
```

### Chạy bằng CLI script (Khuyên dùng)

1. **Smoke Test** (Kiểm tra chức năng toàn bộ endpoints - 1 User, 1 Loop):
   ```bash
   ./run-test.sh smoke
   ```
2. **Light Load** (10 Người dùng đồng thời):
   ```bash
   ./run-test.sh light
   ```
3. **Normal Load** (25 Người dùng đồng thời):
   ```bash
   ./run-test.sh normal
   ```
4. **Heavy Load** (50 Người dùng đồng thời):
   ```bash
   ./run-test.sh heavy
   ```
5. **Stress Test Step-Up** (Kiểm tra chịu tải gia tăng từ 25 -> 50 -> 75 -> 100 -> 150 Người dùng):
   ```bash
   ./run-stress.sh
   ```
6. **Custom Parameters**:
   ```bash
   ./run-test.sh custom <threads> <rampup_seconds> <loops>
   # Ví dụ chạy 30 user trong rampup 15 giây, thực hiện 10 lần lặp:
   ./run-test.sh custom 30 15 10
   ```

## 4. Xem Báo cáo Kết quả

Sau mỗi lần thực nghiệm tải, một tập tin HTML báo cáo chi tiết sẽ được phát sinh bên trong danh mục `results/`:
- Mở thư mục `results/run_<profile>_<timestamp>/html_report/` và nhấp nháp đúp trỏ duyệt tệp `index.html` trong web browser của bạn.
- Báo cáo bao gồm: Throughput (TPS - Transactions Per Second), Phân bố thời gian phản hồi (p50, p90, p95, p99), Error Rate % và Biểu đồ thời gian thao tác mạng.

## 5. Danh sách Các module & Endpoint Được Test Trong Workload

1. **Authentication Flow (`setUp` Thread Group)**:
   - `POST /auth/login` (Trích xuất chuỗi Access Token JWT và ID tài khoản vào toàn cục).
   - `GET /auth/validate-token` (Kiểm chứng hợp lệ của chuỗi bảo mật).
2. **User & Search Operations**:
   - `GET /users/{userId}/profile`
   - `GET /users/search?q=a`
3. **Posts & Comments Pipeline**:
   - `GET /users/posts/suggested` (News Feed)
   - `POST /users/posts` (Tạo bài viết mới kèm trích ID)
   - `POST /users/posts/{id}/like` (Thả tim bài viết)
   - `POST /users/posts/{postId}/comments` (Gửi bình luận vào bài viết mới tạo)
   - `GET /users/posts/{postId}/comments` (Trích xuất và load danh sách bình luận)
4. **Social & Group Feed**:
   - `GET /users/friends/suggestions`
   - `GET /users/groups/joined`
   - `GET /users/groups/suggested`
5. **Messaging, Presence & Notifications**:
   - `GET /chat/conversations/user/{userId}`
   - `GET /chat/conversations/online`
   - `GET /notifications/unread-count?userId={userId}`
6. **System Admin & Sentinel Analytics**:
   - `GET /users/admin/reports/overview`
   - `GET /users/admin/sentiment/overview`

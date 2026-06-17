# Kế hoạch

- [ ] API gateway
  - [x] Thiết lập routing đến các service
  - [ ] Chuyển đổi các thiết lập xác thực jwt sang auth service
- [ ] User service
  - [ ] Post
    - [x] Đăng bài dưới dạng text
    - [x] Demo API post lấy bài viết liên tục
    - [x] Có thể biết mỗi post đó có đang like bởi user hiện tại không
    - [x] Group post
    - [ ] Setiment cho mỗi post
  - [ ] Friends
    - [x] Kết bạn với những thành viên có trong hệ thống
    - [ ] Tìm kiếm
  - [x] Comment
    - [x] Bình luận và trả lời bình luận
    - [x] Like, unlike bình luận
    - [ ] sentiemnt cho comment
  - [ ] Group
    - [x] Tạo nhóm, mời hoặc tham gia nhóm có sẵn
    - [ ] Tìm kiếm
    - [ ] chủ nhóm quản lý thành viên
    - [ ] Quyền chủ nhóm
    - [ ] Đánh giá bài post đó là tích cực hay tiêu cực
  - [ ] User
    - [x] Lấy và cập nhật profile
    - [ ] Cập nhật ảnh đại diện
- [ ] Chat service
  - [x] Chat 1-1, nhóm
  - [ ] video call 1-1
- [ ] Notification service
- [ ] Auth service
- [ ] AI service
  - [x] Tải model tự động
  - [x] Hỗ trợ multi model
  - [ ] Chuyển đổi model ở runtime
  - [ ] Cung cấp API Admin control service
  - [x] Thiết lập kafka
  - [x] Đóng gói docker
  - [ ] Thiết lập CI, CD, môi trường trên k8s
- [ ] Preprocessor service
  - [x] Pipeline tiền xử lý với text
  - [x] Cài đặt stanza coreNLP cho lemma text
  - [x] Thiết lập kafka
  - [x] Đóng gói docker
  - [ ] Thiết lập CI, CD, môi trường trên k8s
- [ ] SRL service
  - [x] Nghiên cứu cài đặt AllenNLP
  - [x] Thiết lập API để preprocessor service call
  - [x] Đóng gói docker
  - [ ] Thiết lập CI, CD, môi trường trên k8s
- [ ] Kafka
  - [x] Thiết lập cơ bản môi trường chạy và kết nối trong kafka
  - [ ] Testing

# Chưa được lên kế hoạch

- Nghiên cứu huấn luyện BiLSTM + Attention, NTN, Bert
- Nghiên cứu và triển khai hệ thống hỗ trợ phát hiện và sàng lọc nội dung tiêu cực trên mạng xã hội dựa trên mô hình Implicit Sentiment Analysis đã xây dựng.
- Phát hiện các bình luận có sắc thái tiêu cực ngầm - những nội dung không sử dụng từ ngữ tiêu cực trực tiếp nhưng thể hiện thái độ tiêu cực thông qua sự kiện hoặc ngữ cảnh.
- Hỗ trợ cảnh báo sớm các nội dung có nguy cơ gây tranh cãi hoặc xúc phạm, giúp quản trị viên kịp thời xử lý.
- Giúp duy trì môi trường mạng xã hội lành mạnh thông qua việc tự động hóa quy trình kiểm duyệt nội dung.
- Nghiên cứu và triển khai hệ thống phân tích và đánh giá thái độ người dùng đối với các chủ đề cụ thể trên mạng xã hội. Mô hình AI thực hiện phân tích hàng loạt bài viết và bình luận để xác định thái độ của người dùng như là ủng hộ, bức xúc hay lo ngại về một vấn đề nào đó.
- Phân tích được hành vi của người dùng mạng xã hội (đề cương)
- Admin
- Áp dụng Redis

# Bonus

# ConnectHub Backend

Backend của **ConnectHub** — một nền tảng mạng xã hội thu nhỏ, tập trung vào trải nghiệm backend thực tế với Java Spring Boot, JWT Security, phân quyền, tối ưu feed và tích hợp realtime chat service.

## Mục tiêu dự án

Dự án được xây dựng để mô phỏng một hệ thống mạng xã hội có các chức năng cốt lõi:
- Quản lý người dùng và xác thực đăng nhập
- Đăng bài viết, thả like, bình luận
- Kết nối người dùng
- Tối ưu tải dữ liệu cho feed
- Tích hợp với service chat realtime

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL
- Redis
- Docker
- Gradle

## Chức năng chính

- Đăng ký, đăng nhập, refresh token
- Xác thực bằng JWT
- Phân quyền với Spring Security
- CRUD bài viết
- Like / unlike bài viết
- Bình luận bài viết
- Kết nối người dùng
- Feed bài viết với Cursor Pagination
- Soft Delete để bảo toàn dữ liệu
- Rate Limiting bảo vệ API
- Tích hợp chat service riêng

## Điểm nhấn kỹ thuật

### 1. Authentication & Authorization
- Sử dụng JWT với mô hình Access Token / Refresh Token
- Ký token bằng RSA Keys để tăng tính bảo mật
- Phân quyền rõ ràng qua Spring Security

### 2. Tối ưu dữ liệu lớn
- Áp dụng **Cursor Pagination** cho feed thay vì offset truyền thống
- Giảm chi phí query khi dữ liệu tăng
- Phù hợp với luồng scroll bài viết liên tục

### 3. Bảo toàn dữ liệu
- Dùng **Soft Delete** để không xóa cứng dữ liệu
- Hỗ trợ audit, rollback logic và quản trị hệ thống tốt hơn

### 4. Tối ưu hiệu năng backend
- Ứng dụng **Virtual Threads** trong Java 21+ để xử lý đồng thời tốt hơn
- Áp dụng **Rate Limiting** để chống spam và bảo vệ tài nguyên server

## Kiến trúc tổng quan

Các nhóm module chính:
- Auth: xác thực, refresh token, bảo mật
- User: thông tin người dùng, kết nối
- Post: bài viết, feed, like
- Comment: bình luận
- Common: exception, response, config, security

## Liên kết liên quan

- Frontend: [connecthub-web](https://github.com/ln678090/connecthub-web)
- Chat service: [ChatRealTime](https://github.com/ln678090/ChatRealTime)

## Cách chạy local

### Yêu cầu
- JDK 21
- PostgreSQL
- Redis
- Docker (nếu chạy bằng container)

### Chạy bằng Gradle
```bash
git clone https://github.com/ln678090/connecthub-be.git
cd connecthub-be
./gradlew bootRun
```

### Chạy bằng Docker
```bash
docker build -t connecthub-be .
docker run -p 8080:8080 connecthub-be
```

## API tiêu biểu

- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `GET /api/posts/feed`
- `POST /api/posts`
- `POST /api/posts/{id}/like`
- `POST /api/comments`
- `GET /api/users/{id}`

## Vai trò của tôi

- Thiết kế và phát triển backend bằng Java Spring Boot
- Xây dựng xác thực JWT và phân quyền
- Thiết kế database và tối ưu query
- Tích hợp chat realtime service
- Tối ưu hiệu năng và bảo mật API

## Hướng phát triển tiếp theo

- Thêm test tự động cho service và controller
- Hoàn thiện CI/CD
- Bổ sung tài liệu API bằng Swagger/OpenAPI
- Monitoring và logging tốt hơn

## Tác giả

**Nguyễn Phúc Lâm**  
Backend Developer Intern (Java)  
Email: ln678090@gmail.com
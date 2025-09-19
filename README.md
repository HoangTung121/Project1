# Project1
# Fliply - Ứng Dụng Đọc Sách Điện Tử Trên Android

Fliply là ứng dụng đọc sách điện tử trên Android, tập trung vào sách tiếng Anh, hỗ trợ học ngoại ngữ và cá nhân hóa trải nghiệm đọc.

## Mục Lục
- [Tổng Quan](#tổng-quan)
- [Tính Năng](#tính-năng)
- [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
- [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
- [Cài Đặt](#cài-đặt)
- [Hướng Dẫn Sử Dụng](#hướng-dẫn-sử-dụng)
- [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
- [Đóng Góp](#đóng-góp)
- [Thành Viên Nhóm](#thành-viên-nhóm)
- [Giấy Phép](#giấy-phép)

## Tổng Quan
Fliply là ứng dụng đọc sách điện tử trên Android, giúp người dùng truy cập kho sách tiếng Anh phong phú, hỗ trợ học ngoại ngữ và cung cấp các tính năng như đánh dấu trang, tìm kiếm thông minh và đồng bộ tiến độ đọc. Dự án được phát triển bởi Nhóm 8 tại Trường Đại học Giao thông Vận tải, dưới sự hướng dẫn của TS. Nguyễn Trọng Phúc, trong tháng 9 năm 2024.

## Tính Năng

### Dành Cho Người Dùng
- Đăng ký/đăng nhập bằng email/mật khẩu, hỗ trợ khôi phục mật khẩu.
- Quản lý thư viện cá nhân: Thêm, xóa, xem sách yêu thích.
- Đọc sách định dạng PDF và TXT, hỗ trợ đánh dấu trang.
- Tìm kiếm sách theo tiêu đề, tác giả, thể loại hoặc từ khóa.
- Cá nhân hóa: Xem lịch sử đọc, gửi đánh giá ứng dụng.

### Dành Cho Quản Trị Viên
- Quản lý sách và thể loại: Thêm, chỉnh sửa, xóa.
- Xem và quản lý đánh giá từ người dùng.
- Tìm kiếm nâng cao theo tiêu đề, tác giả, thể loại.

### Tính Năng Phi Chức Năng
- **Hiệu Năng**: Tải sách trong 3 giây, hỗ trợ 500 người dùng đồng thời.
- **Bảo Mật**: Mã hóa mật khẩu (SHA-256/bcrypt), Firebase Authentication, JWT cho API.
- **Tương Thích**: Hỗ trợ Android 10 trở lên, tương thích với nhiều kích thước màn hình.

## Công Nghệ Sử Dụng
- **Frontend**: Android Studio (Java, XML), Figma (thiết kế UI/UX).
- **Backend**: Node.js (Express.js), Firebase (Realtime Database, Authentication).
- **Server**: Render (triển khai đám mây, CI/CD).
- **Quản Lý Mã Nguồn**: GitHub (Git, GitHub Actions).
- **Kiểm Thử**: Postman (API), Android Studio Emulator, Fiddler, Firebase Performance Monitoring.
- **Quản Lý Dự Án**: ClickUp, Discord, Messenger.
- **Tài Liệu & Sơ Đồ**: Microsoft Word, Draw.io.

## Yêu Cầu Hệ Thống
- **Hệ Điều Hành**: Android 10 trở lên.
- **Môi Trường Phát Triển**:
  - Android Studio (phiên bản mới nhất).
  - Node.js (cho backend).
  - Tài khoản Firebase (cho Authentication và Firestore).
- **Phần Cứng**: Tối thiểu 4GB RAM cho phát triển, thiết bị Android hoặc giả lập.
- **Internet**: Cần kết nối để xác thực, gọi API và triển khai.


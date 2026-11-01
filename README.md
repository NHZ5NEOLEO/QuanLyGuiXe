# Hệ Thống Quản Lý Gửi Xe (Parking Management System)

Dự án phần mềm hoàn chỉnh phục vụ tự động hóa quản lý bãi đỗ xe ô tô, xe máy và xe đạp thông minh: nhận diện biển số xe (OCR), quét mã QR vé xe, thanh toán không dùng tiền mặt (VietQR), sơ đồ ô đỗ trực quan và báo cáo doanh thu ca trực.

---

## 🚀 Công Nghệ Sử Dụng

- **Backend**: Java 21 / 25, Spring Boot, Spring Data JPA, Hibernate ORM, Spring Security
- **Cơ sở dữ liệu**: Microsoft SQL Server (Hỗ trợ chuẩn Unicode `NVARCHAR` & `NVARCHAR(MAX)` lưu trữ ảnh phương tiện)
- **Frontend**: Thymeleaf, Bootstrap 5.3, FontAwesome 6, Chart.js, SweetAlert2, HTML5-QRCode Scanner
- **Tích hợp**: VietQR thanh toán tự động, Tesseract OCR nhận diện biển số

---

## 📌 Các Tính Năng Chính

1. **Làn Xe Vào (Check-in)**:
   - Tự động nhận diện biển số hoặc quét QR thẻ gửi xe.
   - Hỗ trợ đầy đủ xe đạp / xe điện không biển số (sinh mã định danh `XD-...`).
   - Lưu trữ ảnh phương tiện làn vào chuẩn xác.
   - In vé QR nhiệt POS 80mm tức thì.
2. **Làn Xe Ra (Check-out) & Thu Phí**:
   - Quét mã QR hoặc tìm biển số xe để đối soát tự động.
   - Tính toán chi phí gửi xe chính xác theo bảng giá Ngày / Đêm / Vé tháng.
   - Tích hợp thanh toán tiền mặt & sinh mã chuyển khoản VietQR tự động.
3. **Quản Lý Bãi Đỗ & Vé Tháng**:
   - Sơ đồ bãi đỗ xe theo phân khu và vị trí ô đỗ trực quan.
   - Đăng ký, gia hạn và quản lý thuê bao vé tháng.
4. **An Ninh & Quản Trị**:
   - Cảnh báo xe tồn bãi quá 24h và kiểm soát danh sách đen (Blacklist).
   - Quản lý ca trực, giao ca và đối soát doanh thu.
   - Thống kê biểu đồ doanh thu theo ngày/tháng và xuất báo cáo.

---

## 🛠 Hướng Dẫn Cài Đặt & Chạy Dự Án

### 1. Cài đặt Cơ sở dữ liệu
- Mở **SQL Server Management Studio (SSMS)**.
- Mở file `csdl_quan_ly_gui_xe.sql` và nhấn **Execute** để khởi tạo database `QuanLyGuiXe` cùng toàn bộ bảng và dữ liệu mẫu.

### 2. Cấu hình ứng dụng
Chỉnh sửa thông tin kết nối CSDL trong file `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyGuiXe;encrypt=true;trustServerCertificate=true;
spring.datasource.username=sa
spring.datasource.password=YOUR_PASSWORD
server.port=80
```

### 3. Khởi chạy ứng dụng
Mở terminal tại thư mục gốc của dự án:
```bash
# Biên dịch dự án
.\mvnw.cmd compile

# Khởi chạy máy chủ
.\mvnw.cmd spring-boot:run
```

### 4. Truy cập hệ thống
- **URL**: `http://localhost/` hoặc `http://baixe.localhost/`
- **Tài khoản quản trị viên (Admin)**: `admin` / `admin123`
- **Tài khoản nhân viên trực cổng (Bảo vệ)**: `baove` / `123456`

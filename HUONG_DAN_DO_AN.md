# BÁO CÁO VÀ HƯỚNG DẪN ĐỒ ÁN CHUYÊN NGÀNH
## ĐỀ TÀI: XÂY DỰNG HỆ THỐNG PHẦN MỀM QUẢN LÝ GỬI XE THÔNG MINH (SMART PARKING MANAGEMENT SYSTEM)

---

### 1. TỔNG QUAN ĐỀ TÀI
- **Tên đề tài**: Xây dựng phần mềm quản lý gửi xe thông minh
- **Mục tiêu**: Tự động hóa quy trình kiểm soát bãi đỗ xe ô tô và xe máy, ứng dụng Trí tuệ Nhân tạo (AI OCR), thanh toán không dùng tiền mặt (VietQR), giám sát an ninh và quản lý ca trực chuyên nghiệp.
- **Công nghệ nền tảng**:
  - **Backend**: Java 17, Spring Boot 4 / Spring Framework 7, Spring Data JPA, Hibernate ORM
  - **Cơ sở dữ liệu**: Microsoft SQL Server (Database: `QuanLyGuiXe`, hỗ trợ đầy đủ Unicode Tiếng Việt chuẩn `NVARCHAR`)
  - **Frontend / Giao diện**: Thymeleaf Template Engine, Bootstrap 5.3 (Dark / Light Theme), FontAwesome 6, Chart.js 4, SweetAlert2, HTML5-QRCode Scanner, Flatpickr Datepicker (`dd/MM/yyyy`)
  - **Trí tuệ Nhân tạo AI**: Tesseract.js OCR nhận diện biển số xe tự động từ hình ảnh camera hoặc tải tệp
  - **Tích hợp thanh toán số**: Chuẩn VietQR NAPAS 24/7 sinh mã QR chuyển khoản tự động
  - **In hóa đơn**: Chuẩn hóa đơn nhiệt POS 80mm điện tử

---

### 2. KIẾN TRÚC HỆ THỐNG (3-TIER ARCHITECTURE)
Hệ thống được thiết kế theo mô hình phân tầng tiêu chuẩn công nghiệp:
```
com.example.demo
├── config/              # Khởi tạo dữ liệu mẫu (DataInitializer)
├── controller/          # Điều hướng Web và REST API:
│   ├── DashboardController.java       # Tổng quan thống kê và biểu đồ
│   ├── GateController.java            # Giao diện cổng Làn vào / Làn ra
│   ├── ApiGateController.java         # REST API check-in, check-out, OCR, check-plate
│   ├── AuthController.java            # Đăng nhập, đăng xuất, phân quyền
│   ├── ShiftController.java           # Quản lý ca trực và biên bản bàn giao ca
│   ├── SecurityAlertController.java   # Cảnh báo xe tồn bãi > 24h và Blacklist
│   ├── ParkingLotMapController.java   # Sơ đồ bãi đỗ trực quan
│   ├── MonthlyTicketController.java   # Quản lý thuê bao vé tháng
│   ├── HistoryController.java         # Lịch sử ra vào và xuất báo cáo CSV
│   └── PriceSettingController.java    # Cài đặt biểu phí gửi xe
├── model/               # Các thực thể CSDL (Entity):
│   ├── LoaiXe, KhuVuc, ViTriDo, KhachHang, VeThang, VeXe
│   ├── NguoiDung, CaTruc, DanhSachDen
├── repository/          # Tầng dữ liệu Spring Data JPA Repository
├── service/             # Tầng nghiệp vụ (Service Layer):
│   ├── ParkingService, MonthlyTicketService, ParkingLotService
│   ├── ReportService, VietQrService, AuthService, ShiftService, BlacklistService
└── dto/                 # Data Transfer Object trao đổi Client-Server
```

---

### 3. CƠ SỞ DỮ LIỆU VÀ CÁC BẢNG (ERD)
1. **`LoaiXe`**: Danh mục phương tiện (Xe máy, Ô tô 4-7 chỗ, Xe đạp/điện, Xe tải nhỏ) cùng đơn giá ngày, đêm và vé tháng.
2. **`KhuVuc`**: Phân chia các phân khu chuyên biệt (Khu A - Ô tô, Khu B - Xe máy, Khu C - Xe điện).
3. **`ViTriDo`**: Quản lý từng vị trí ô đỗ cụ thể (`A-01` đến `A-10`, `B-01` đến `B-20`, `C-01` đến `C-10`), theo dõi trạng thái `TRONG`, `DA_DO`, thời gian đỗ và biển số chiếm chỗ.
4. **`KhachHang`**: Thông tin cư dân đăng ký vé tháng (Họ tên, SĐT, CCCD, Email, Căn hộ).
5. **`VeThang`**: Thẻ gửi xe tháng (Mã thẻ RFID/QR, Biển số xe, Loại xe, Ngày bắt đầu, Ngày hết hạn, Trạng thái).
6. **`VeXe`**: Chi tiết từng lượt xe vào/ra (Mã QR vé, Biển số, Loại xe, Vị trí đỗ, Thời gian vào, Thời gian ra, Chi phí, Hình thức thanh toán, Ảnh chụp xe).
7. **`NguoiDung`**: Quản lý người dùng và phân quyền (ADMIN vs NHAN_VIEN).
8. **`CaTruc`**: Quản lý ca làm việc (Ca Sáng, Ca Chiều, Ca Đêm), doanh thu ca, tiền mặt bàn giao, ghi chú ca trực.
9. **`DanhSachDen`**: Quản lý biển số xe trộm cắp, vi phạm, đối tượng an ninh cần chặn tại cổng.

---

### 4. TỔNG HỢP CÁC TÍNH NĂNG VƯỢT TRỘI (NÂNG CẤP ĐIỂM 10)

| STT | Tính năng | Mô tả chi tiết |
|---|---|---|
| 1 | **AI OCR Nhận Diện Biển Số** | Ứng dụng mô hình AI Tesseract.js quét trực tiếp từ webcam hoặc ảnh chụp, trích xuất chính xác biển số xe theo định dạng Việt Nam. |
| 2 | **Cảnh Báo Danh Sách Đen (Blacklist)** | Kiểm tra thời gian thực biển số khi xe vừa đến cổng; cảnh báo chuông và popup đỏ khẩn cấp nếu là xe trộm cắp (`59X3-99999`). |
| 3 | **Cảnh Báo Xe Tồn Bãi > 24H** | Tự động phát hiện phương tiện lưu lại bãi quá 24 tiếng, tính toán số giờ lưu bãi và cảnh báo nhân viên an ninh kiểm tra. |
| 4 | **Thanh Toán Không Tiền Mặt VietQR** | Tự động sinh mã VietQR chuẩn NAPAS 24/7 theo đúng số tiền đỗ xe, hỗ trợ thanh toán từ mọi app ngân hàng. |
| 5 | **In Hóa Đơn 80mm (POS Thermal)** | Tự động sinh biên lai thu tiền định dạng POS 80mm chuẩn quốc tế có mã tra cứu và chi tiết giờ gửi. |
| 6 | **Quản Lý Ca Trực và Bàn Giao Ca** | Mở ca, chốt ca, lập biên bản bàn giao doanh thu tiền mặt, đếm lượt xe vào/ra trong ca. |
| 7 | **Hệ Thống Đăng Nhập và Phân Quyền** | Phân quyền bảo mật tài khoản Quản trị viên (Admin) và Nhân viên trực cổng (Bảo vệ). |
| 8 | **Chế Độ Giao Diện Dark / Light Mode** | Chuyển đổi giao diện Sáng / Tối toàn hệ thống với 1 click, lưu cấu hình vào trình duyệt. |
| 9 | **Sơ Đồ Bãi Đỗ Xe (Slot Map)** | Trực quan hóa toàn bộ ô đỗ với mã màu Realtime (Xanh = Trống, Đỏ = Đang đỗ, Vàng = Đã đặt trước trực tuyến, Xám = Bảo trì). |
| 10 | **Bộ Lọc Flatpickr Chuẩn `dd/MM/yyyy`** | Chọn ngày theo định dạng Việt Nam, xuất báo cáo doanh thu ra file Excel CSV UTF-8. |
| 11 | **Cổng Dịch Vụ Công Khai (Eco Portal / Landing Page)** | Giao diện công khai chuẩn quốc tế dành cho khách hàng và cư dân (`/`, `/home`), mang phong cách Bãi xe Sinh thái (Emerald Green, trạm sạc điện mặt trời EV, tra cứu xe và đăng ký vé tháng online). |
| 12 | **Đặt Chỗ Đỗ Xe Trước Trực Tuyến (Pre-Booking)** | Khách hàng có thể chọn ô đỗ trống trực tiếp trên web, cọc 20.000đ qua VietQR; hệ thống tự động khóa ô đỗ 60 phút và cập nhật trạng thái `DAT_TRUOC` trên Sơ đồ bãi xe. |
| 13 | **Cổng Đánh Giá và Góp Ý Chất Lượng (Customer Reviews)** | Cho phép khách hàng gửi đánh giá 1-5 sao theo từng danh mục dịch vụ (An ninh, Tốc độ, Trạm sạc EV, Vệ sinh) và hiển thị trực tiếp lên hệ thống. |
| 14 | **Phân Tích Lưu Lượng 24 Giờ và Dự Báo Giờ Cao Điểm** | Biểu đồ đường Area Line Chart trên Dashboard thể hiện lưu lượng xe vào từng khung giờ trong ngày, giúp điều phối nhân sự trực ca kịp thời. |
| 15 | **Hệ Thống Cảnh Báo An Ninh Telegram Bot và Hóa Đơn Email** | Tự động bắn thông báo khẩn cấp tới Telegram Ban Quản Lý khi phát hiện xe Blacklist trộm cắp và gửi Email xác nhận vé điện tử cho khách hàng. |
| 16 | **Mô Phỏng Trạm Barie Tự Động và Bảng LED Ma Trận Cổng (IoT Gate)** | Bảng LED ma trận thời gian thực báo số chỗ trống từng khu; Cần Barie thông minh có hoạt họa nâng/hạ tự động khi xe vào/ra bãi, tích hợp đèn giao thông Đỏ/Xanh, âm thanh điện tử và chế độ Mở khẩn cấp. |
| 17 | **Biên Bản Báo Cáo Tài Chính Chuẩn A4 và Cơ Cấu Thu Theo Loại Xe** | Báo cáo chi tiết cơ cấu doanh thu theo từng loại phương tiện (Ô tô, Xe máy, Xe điện); mẫu biên bản bàn giao ca trực chuẩn hành chính A4 có Quốc hiệu, Tiêu ngữ và chữ ký 3 bên sẵn sàng xuất PDF / In ấn. |
| 18 | **Kết Nối Trực Tuyến Toàn Cầu (Cloud Tunnel) và Quét Mã Trên Điện Thoại** | Kèm file khởi động 1-click `start-online-demo.bat` đưa hệ thống lên mạng Internet bảo mật HTTPS; nút **Quét Mã ĐT** hỗ trợ hội đồng chấm thi mở trực tiếp giao diện từ điện thoại thật. |

---

### 5. THÔNG TIN ĐĂNG NHẬP VÀ TÀI KHOẢN MẪU

| Tài khoản | Mật khẩu | Họ tên | Vai trò (Role) | Chức năng chính |
|---|---|---|---|---|
| `admin` | `admin123` | Quản Trị Viên | **ADMIN** | Toàn quyền quản trị, cài đặt bảng giá, xem doanh thu, bàn giao ca |
| `baove` | `123456` | Nhân Viên Trực Cổng | **NHAN_VIEN** | Trực cổng làn vào/ra, quét biển số OCR, thu tiền VietQR |

---

### 6. KỊCH BẢN DEMO THUYẾT TRÌNH BẢO VỆ ĐỒ ÁN (ĐIỂM 10)

- **Bước 1 (Kết nối Trực tuyến và Trình diễn trên Điện thoại Di động)**:
  - Nhấp đúp chuột file `start-online-demo.bat` để đưa hệ thống lên mạng thật (HTTPS).
  - Bấm nút **"📱 Quét Mã ĐT"** trên thanh menu -> Mời Thầy Cô giám khảo dùng điện thoại quét mã QR để mở trang chủ Landing Page ngay trên điện thoại thật!
- **Bước 2 (Giới thiệu Cổng Dịch Vụ Công Khai - Eco Smart Parking Portal)**:
  - Khách hàng trên điện thoại có thể tra cứu xe (`29A-12345`), đặt chỗ đỗ xe trước (`#prebook`) với tiền cọc VietQR 20.000đ và gửi đánh giá 5 sao.
- **Bước 3 (Đăng nhập và Chế độ Dark Mode Quản Trị)**:
  - Truy cập `http://baixe.localhost/login` (hoặc `http://localhost/login`) -> Đăng nhập bằng `admin` / `admin123`.
  - Bấm vào icon **Mặt trăng / Mặt trời** trên thanh Topbar để chuyển đổi giao diện Dark Mode bảo vệ mắt.
- **Bước 4 (Trình diễn Dashboard, 4 Biểu Đồ và Giờ Cao Điểm)**:
  - 5 thẻ KPI Realtime (Xe trong bãi, Chỗ trống, Vào/Ra, Doanh thu ngày, Xe tồn bãi > 24h).
  - 4 Biểu đồ phân tích: Doanh thu 7 ngày, Cơ cấu loại xe, Kênh thanh toán, và **Lưu lượng 24h và Giờ cao điểm**.
- **Bước 5 (Demo Trực Cổng: Bảng LED Matrix, AI OCR và Mô Phỏng Barie Thông Minh)**:
  - Menu **Cổng kiểm soát Vào/Ra** (`/gate`):
  - Giới thiệu **Bảng LED Ma Trận điện tử** hiển thị chữ chạy thời gian thực đếm số chỗ trống từng khu (Khu A, Khu B, trạm sạc EV).
  - Quét AI OCR hoặc click nhanh biển số mẫu -> Bấm **Xác nhận cho xe vào bãi**.
  - **Cần Barie thông minh tự động nâng lên**, đèn giao thông chuyển sang Xanh, phát âm thanh chuông điện tử xác nhận!
- **Bước 6 (Demo Cảnh báo An ninh: Blacklist xe trộm cắp và Telegram Alert)**:
  - Click biển số mẫu `59X3-99999` -> Hệ thống báo động đỏ, tự động bắn tin nhắn cảnh báo đến Telegram Ban Quản Lý.
- **Bước 7 (Demo Sơ đồ bãi đỗ Realtime và Ô Xe Đã Đặt Trước)**:
  - Menu **Sơ đồ bãi đỗ (Slot Map)**: Trực quan hóa trạng thái từng ô (Xanh = Trống, Đỏ = Đang đỗ, Vàng = Khách đã đặt trước trực tuyến).
- **Bước 8 (Demo Làn ra: Thanh toán VietQR, Mở Barie và In Bill 80mm)**:
  - Nhập biển số -> Hệ thống tính cước -> Khách thanh toán VietQR -> Bấm **Xác nhận và Mở cổng Barie** -> Cần Barie nâng lên, sinh hóa đơn POS 80mm.
- **Bước 9 (Demo Báo Cáo Tài Chính và Bàn Giao Ca Chuẩn A4)**:
  - Menu **Bàn giao ca trực** (`/shift`): Xem cơ cấu doanh thu theo từng loại phương tiện, bấm **Xuất Biên Bản PDF / In A4** để trình diễn mẫu biên bản hành chính có Quốc hiệu, Tiêu ngữ và chữ ký 3 bên.
- **Bước 10 (Demo Lịch sử và Bộ lọc ngày `dd/MM/yyyy`)**:
  - Menu **Lịch sử ra vào** (`/history`): Lọc theo ngày tháng năm chuẩn Việt Nam và xuất file Excel CSV UTF-8.

---

### 7. HƯỚNG DẪN KHỞI ĐỘNG HỆ THỐNG
1. **Khởi tạo CSDL SQL Server**: 
   - Mở file kịch bản SQL thuần Việt: `csdl_quan_ly_gui_xe.sql` trong SQL Server Management Studio (SSMS) và bấm **Execute (F5)** để tự động tạo toàn bộ bảng, khóa ngoại và nạp dữ liệu mẫu ban đầu.
   - Hoặc hệ thống Spring Boot cũng tự động đồng bộ cấu trúc qua Hibernate (`ddl-auto=update`).
2. **Khởi chạy ứng dụng**:
   ```powershell
   cd "d:\ECLIPSE WORK\Spring\demo"
   .\mvnw.cmd spring-boot:run
   ```
3. **Mở trình duyệt**:
   - Cổng thông tin khách hàng: `http://baixe.localhost/` (hoặc `http://localhost/`)
   - Khách quét mã gửi xe cổng vào: `http://baixe.localhost/khach-gui-xe`
   - Khách quét mã nhận xe cổng ra: `http://baixe.localhost/khach-nhan-xe`
   - Đăng nhập quản trị: `http://baixe.localhost/login` (admin / admin123)

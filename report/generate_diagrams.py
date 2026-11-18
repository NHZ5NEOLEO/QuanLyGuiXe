import os
from playwright.sync_api import sync_playwright

output_dir = os.path.abspath(r'report\assets\diagrams')
os.makedirs(output_dir, exist_ok=True)

diagrams = {
    "architecture": {
        "title": "Sơ đồ 2.1. Kiến trúc tổng thể hệ thống phần mềm quản lý gửi xe",
        "code": """graph TD
    subgraph Presentation["Tầng Giao Diện (Presentation Layer)"]
        UI_Web["Trình duyệt Web (HTML5, Bootstrap 5, Thymeleaf, JavaScript)"]
        UI_Admin["Giao diện Quản trị / Dashboard (Chart.js, Dark/Light Mode)"]
        UI_Gate["Giao diện Trực cổng Vào/Ra (Webcam Video, QR Scanner)"]
        UI_Portal["Cổng dịch vụ công khai (Tra cứu xe, Đăng ký vé tháng, Đánh giá)"]
    end

    subgraph Business["Tầng Xử Lý Nghiệp Vụ (Business Logic Layer - Spring Boot 4 / Java 17)"]
        Ctrl_Gate["GateController & ApiGateController"]
        Ctrl_Auth["AuthController & Spring Security"]
        Ctrl_Report["DashboardController & ReportService"]
        Ctrl_Shift["ShiftController (Quản lý ca trực)"]
        Svc_Parking["ParkingService (Điều phối Vào/Ra)"]
        Svc_Fee["FeeCalculationService (Tính phí lũy tiến Ngày/Đêm)"]
        Svc_QR["QrCodeService & VietQrService (Sinh mã QR & VietQR)"]
        Svc_Blacklist["BlacklistService (Cảnh báo an ninh)"]
        Svc_Monthly["MonthlyTicketService (Thuê bao vé tháng)"]
    end

    subgraph DataAccess["Tầng Truy Xuất Dữ Liệu (Data Access Layer)"]
        JPA["Spring Data JPA & Hibernate ORM"]
        Repo["VeXeRepository, NguoiDungRepository, LoaiXeRepository..."]
    end

    subgraph DatabaseLayer["Tầng Cơ Sở Dữ Liệu (Database Layer)"]
        SQLServer[("Microsoft SQL Server 2022<br/>Database: QuanLyGuiXe<br/>(11 bảng dữ liệu chuẩn Unicode NVARCHAR)")]
    end

    subgraph Hardware["Thiết Bị & Dịch Vụ Tích Hợp (Software-in-the-Loop)"]
        Cam["Camera nhận diện biển số (Webcam / Tesseract OCR)"]
        Barie["Cần Barie tự động (Mô phỏng động cơ & Đèn tín hiệu)"]
        NAPAS["Cổng thanh toán VietQR (Chuẩn NAPAS 24/7)"]
    end

    UI_Web --> Ctrl_Gate
    UI_Admin --> Ctrl_Report
    UI_Gate --> Ctrl_Gate
    UI_Portal --> Ctrl_Gate

    Ctrl_Gate --> Svc_Parking
    Ctrl_Gate --> Svc_Fee
    Ctrl_Gate --> Svc_QR
    Ctrl_Gate --> Svc_Blacklist
    Ctrl_Auth --> Svc_Monthly

    Svc_Parking --> JPA
    Svc_Fee --> JPA
    Svc_Monthly --> JPA
    JPA --> SQLServer

    Cam -.->|Truyền ảnh & nhận diện| UI_Gate
    Svc_QR -.->|Sinh mã VietQR| NAPAS
    UI_Gate -.->|Tín hiệu điều khiển mở| Barie
"""
    },

    "use-case": {
        "title": "Sơ đồ 2.2. Sơ đồ Use Case tổng quát của hệ thống",
        "code": """flowchart LR
    subgraph Tác Nhân
        Customer(("Khách hàng / Cư dân"))
        Guard(("Nhân viên Bảo vệ"))
        Admin(("Quản trị viên"))
    end

    subgraph Hệ Thống Quản Lý Gửi Xe
        UC_Login["Đăng nhập hệ thống"]
        UC_Entry["Tiếp nhận xe vào & Xác nhận Camera"]
        UC_Exit["Xử lý xe ra & Đối chiếu ảnh"]
        UC_QR_Ticket["Cấp & Tra cứu mã QR vé xe"]
        UC_Fee["Tính toán chi phí gửi xe"]
        UC_Pay["Thanh toán tiền mặt / Quét VietQR"]
        UC_Shift["Bàn giao ca trực & Đối soát tiền"]
        UC_Monthly["Quản lý & Đăng ký vé tháng"]
        UC_Security["Cảnh báo xe quá 24h & Blacklist"]
        UC_Dashboard["Xem Dashboard & Thống kê doanh thu"]
        UC_Price["Cài đặt biểu phí gửi xe"]
        UC_User["Quản lý tài khoản & phân quyền"]
        UC_Portal["Tra cứu xe công khai & Đánh giá dịch vụ"]
    end

    Customer --> UC_Portal
    Customer --> UC_Pay
    Customer --> UC_Monthly

    Guard --> UC_Login
    Guard --> UC_Entry
    Guard --> UC_Exit
    Guard --> UC_QR_Ticket
    Guard --> UC_Fee
    Guard --> UC_Shift
    Guard --> UC_Security

    Admin --> UC_Login
    Admin --> UC_Dashboard
    Admin --> UC_Price
    Admin --> UC_User
    Admin --> UC_Security
    Admin --> UC_Monthly
    Admin --> UC_Shift
"""
    },

    "activity-entry": {
        "title": "Sơ đồ 2.3. Sơ đồ hoạt động quy trình xe vào bãi",
        "code": """flowchart TD
    Start([Phương tiện tiến vào làn vào]) --> Step1[Camera chụp ảnh và nhận diện biển số xe]
    Step1 --> Step2[Hệ thống hiển thị hình ảnh và biển số lên màn hình trực cổng]
    Step2 --> Step3[Bảo vệ kiểm tra và đối chiếu biển số thực tế]
    Step3 --> Decision1{Biển số có cần chỉnh sửa?}
    Decision1 -- Có --> FixPlate[Bảo vệ chỉnh sửa lại biển số chính xác]
    Decision1 -- Không --> ConfirmPlate[Bảo vệ nhấn nút Xác nhận cho xe vào]
    FixPlate --> ConfirmPlate

    ConfirmPlate --> Step4[Backend kiểm tra dữ liệu đầu vào]
    Step4 --> CheckBlacklist{Biển số thuộc Blacklist?}
    CheckBlacklist -- Có --> AlertBlacklist[Báo động đỏ: Từ chối xe vào bãi] --> Stop([Dừng quy trình])
    
    CheckBlacklist -- Không --> CheckParked{Xe đang có trong bãi?}
    CheckParked -- Có --> AlertParked[Báo lỗi: Xe chưa hoàn tất lượt ra trước đó] --> Stop

    CheckParked -- Không --> CheckCapacity{Phân khu còn chỗ trống?}
    CheckCapacity -- Không --> AlertFull[Báo lỗi: Phân khu bãi đỗ đã đầy] --> Stop

    CheckCapacity -- Có --> CreateTicket[Tạo bản ghi VeXe trạng thái 'Đang gửi']
    CreateTicket --> GenQR[Sinh mã QR vé xe duy nhất bằng ZXing]
    GenQR --> OpenBarrier[Gửi tín hiệu điều khiển Barie tự động nâng cần]
    OpenBarrier --> CarIn[Phương tiện đi qua cổng vào bãi đỗ]
    CarIn --> CloseBarrier[Cần Barie tự động hạ xuống hoàn tất]
    CloseBarrier --> EndSuccess([Kết thúc lượt xe vào])
"""
    },

    "sequence-entry": {
        "title": "Sơ đồ 2.4. Sơ đồ tuần tự quy trình xe vào bãi",
        "code": """sequenceDiagram
    autonumber
    actor Driver as Lái xe
    actor Guard as Nhân viên Bảo vệ
    participant Camera as Camera / OCR
    participant WebUI as Giao diện Làn vào (Web UI)
    participant Backend as Backend (GateController / ParkingService)
    participant DB as SQL Server
    participant Barrier as Cần Barie tự động

    Driver->>Camera: Di chuyển phương tiện vào cổng
    Camera->>WebUI: Chụp ảnh phương tiện & trích xuất biển số
    WebUI->>Guard: Hiển thị ảnh chụp & biển số nhận diện
    Guard->>WebUI: Kiểm tra đối chiếu & bấm "Xác nhận xe vào"
    WebUI->>Backend: Gửi yêu cầu check-in (Biển số, Loại xe, Ảnh vào)
    activate Backend
    Backend->>DB: Kiểm tra Blacklist & Trùng xe trong bãi
    DB-->>Backend: Dữ liệu hợp lệ (Không vi phạm)
    Backend->>DB: Kiểm tra sức chứa phân khu đỗ xe
    DB-->>Backend: Còn chỗ trống hợp lệ
    Backend->>DB: INSERT bản ghi VeXe (Trạng thái: Đang gửi)
    Backend->>Backend: Sinh mã định danh QR duy nhất
    Backend-->>WebUI: Trả về kết quả thành công + Mã QR vé
    deactivate Backend
    WebUI->>Barrier: Kích hoạt tín hiệu mở cần Barie tự động
    activate Barrier
    Barrier-->>Driver: Nâng cần Barie, đèn chuyển Xanh
    Driver->>Barrier: Xe di chuyển qua cổng vào bãi
    Barrier-->>Barrier: Hạ cần Barie đóng cổng an toàn
    deactivate Barrier
    WebUI->>Driver: Cấp mã QR vé xe điện tử / In vé
"""
    },

    "activity-exit": {
        "title": "Sơ đồ 2.5. Sơ đồ hoạt động quy trình xe ra bãi",
        "code": """flowchart TD
    Start([Phương tiện tiến vào làn ra]) --> Scan[Khách xuất trình mã QR hoặc đọc biển số xe]
    Scan --> Lookup[Nhân viên quét mã QR hoặc nhập biển số tra cứu]
    Lookup --> CheckTicket{Tìm thấy vé 'Đang gửi'?}
    CheckTicket -- Không --> NotFound[Báo lỗi: Không tìm thấy lượt gửi xe] --> Stop([Dừng xử lý])

    CheckTicket -- Có --> CamExit[Camera chụp ảnh phương tiện lúc ra bãi]
    CamExit --> Compare[Nhân viên đối chiếu ảnh lúc vào và ảnh lúc ra]
    Compare --> Match{Biển số và ảnh khớp?}
    Match -- Không --> Suspicious[Cảnh báo an ninh: Nghi vấn trộm cắp / lệch biển] --> Stop

    Match -- Có --> Calc[Hệ thống tự động tính thời gian và cước phí gửi xe]
    Calc --> CheckMonthly{Xe có vé tháng còn hạn?}
    CheckMonthly -- Có --> FreePass[Miễn phí lượt gửi: Phí = 0 VNĐ] --> FinishOut
    
    CheckMonthly -- Không --> ChoosePay{Hình thức thanh toán}
    ChoosePay -- Tiền mặt --> Cash[Nhân viên thu tiền mặt] --> FinishOut
    ChoosePay -- VietQR --> QRGen[Tự động sinh mã VietQR NAPAS động theo đúng số tiền]
    QRGen --> PayQR[Khách quét mã thanh toán qua ứng dụng ngân hàng]
    PayQR --> ConfirmBank[Nhân viên xác nhận tiền đã vào tài khoản] --> FinishOut

    FinishOut[Cập nhật trạng thái vé: 'Đã ra' và lưu doanh thu]
    FinishOut --> OpenBarie[Barie tự động mở cần cho xe xuất bãi]
    OpenBarie --> PrintBill[In hóa đơn nhiệt POS 80mm cho khách]
    PrintBill --> EndSuccess([Hoàn tất lượt xe ra])
"""
    },

    "sequence-exit": {
        "title": "Sơ đồ 2.6. Sơ đồ tuần tự quy trình xe ra bãi",
        "code": """sequenceDiagram
    autonumber
    actor Customer as Khách hàng
    actor Guard as Nhân viên Bảo vệ
    participant WebUI as Giao diện Làn ra (Web UI)
    participant Backend as Backend (ParkingService / FeeService)
    participant DB as SQL Server
    participant VietQR as Hệ thống VietQR NAPAS
    participant Barrier as Cần Barie tự động

    Customer->>Guard: Xuất trình mã QR vé xe / đọc biển số
    Guard->>WebUI: Quét QR hoặc nhập biển số tra cứu
    WebUI->>Backend: GET /api/gate/check-out/preview?query=...
    activate Backend
    Backend->>DB: Truy vấn vé xe trạng thái 'Đang gửi'
    DB-->>Backend: Thông tin vé (Thời gian vào, Loại xe, Ảnh vào)
    Backend->>Backend: Tính thời lượng & Cước phí gửi xe (Lũy tiến Ngày/Đêm)
    Backend-->>WebUI: Trả về thông tin chi phí & Ảnh vào để đối chiếu
    deactivate Backend
    WebUI->>Guard: Hiển thị số tiền & ảnh vào/ra để so sánh
    alt Khách có vé tháng hợp lệ
        WebUI->>WebUI: Tự động áp dụng cước 0 VNĐ (Miễn phí)
    else Khách thanh toán VietQR
        WebUI->>VietQR: Yêu cầu sinh mã QR thanh toán động
        VietQR-->>WebUI: Trả về hình ảnh mã VietQR chuẩn NAPAS
        Customer->>VietQR: Quét mã chuyển khoản qua App ngân hàng
        Guard->>WebUI: Bấm xác nhận đã nhận chuyển khoản
    else Khách thanh toán Tiền mặt
        Customer->>Guard: Giao tiền mặt tại quầy
        Guard->>WebUI: Bấm xác nhận đã thu tiền mặt
    end
    WebUI->>Backend: POST /api/gate/check-out/confirm
    activate Backend
    Backend->>DB: UPDATE VeXe (Trạng thái: Đã ra, Lưu chi phí & giờ ra)
    Backend-->>WebUI: Xác nhận xe ra thành công
    deactivate Backend
    WebUI->>Barrier: Kích hoạt tín hiệu mở cần Barie tự động
    activate Barrier
    Barrier-->>Customer: Mở cần Barie cho xe xuất bãi
    deactivate Barrier
    WebUI->>Customer: In biên lai thu tiền POS 80mm
"""
    },

    "erd": {
        "title": "Sơ đồ 2.7. Sơ đồ thực thể liên kết (ERD) hoàn chỉnh",
        "code": """erDiagram
    NguoiDung ||--o{ CaTruc : "thuc hien"
    NguoiDung ||--o{ KhachHang : "lien ket portal"
    
    KhachHang ||--o{ PhuongTien : "so huu"
    KhachHang ||--o{ VeThang : "dang ky"
    
    LoaiXe ||--o{ KhuVuc : "quy hoach"
    LoaiXe ||--o{ PhuongTien : "phan loai"
    LoaiXe ||--o{ VeThang : "ap dung bieu phi"
    LoaiXe ||--o{ VeXe : "tinh phi luot"
    
    KhuVuc ||--o{ VeXe : "do tai khu"
    VeThang ||--o{ VeXe : "mien phi ve thang"
    DatChoTruoc ||--o| VeXe : "khau tru coc"

    NguoiDung {
        bigint id PK
        nvarchar ten_dang_nhap UK
        varchar mat_khau
        nvarchar ho_ten
        varchar vai_tro
        varchar so_dien_thoai
        nvarchar email
        bit trang_thai
    }

    KhachHang {
        bigint id PK
        nvarchar ho_ten
        varchar so_dien_thoai UK
        varchar cccd
        varchar email
        nvarchar dia_chi
    }

    LoaiXe {
        bigint id PK
        nvarchar ten_loai UK
        decimal gia_ban_ngay
        decimal gia_ban_dem
        decimal gia_thang
        varchar icon_class
        nvarchar mo_ta
    }

    KhuVuc {
        bigint id PK
        varchar ma_khu UK
        nvarchar ten_khu
        bigint loai_xe_id FK
        int suc_chua
        nvarchar vi_tri
    }

    PhuongTien {
        bigint id PK
        varchar ma_phuong_tien UK
        varchar bien_so_xe UK
        bigint loai_xe_id FK
        nvarchar chu_xe
        varchar so_dien_thoai
        nvarchar mo_ta
        datetime2 ngay_tao
    }

    VeThang {
        bigint id PK
        varchar ma_the UK
        bigint khach_hang_id FK
        bigint loai_xe_id FK
        varchar bien_so_xe
        date ngay_bat_dau
        date ngay_het_han
        decimal gia_tien
        varchar trang_thai
    }

    VeXe {
        bigint id PK
        varchar ma_qr UK
        varchar bien_so_xe
        varchar bien_so_clean
        bigint loai_xe_id FK
        bigint khu_vuc_id FK
        bigint ve_thang_id FK
        bigint dat_cho_truoc_id FK
        datetime2 thoi_gian_vao
        datetime2 thoi_gian_ra
        decimal chi_phi
        decimal so_tien_thuc_thu
        varchar hinh_thuc_thanh_toan
        varchar trang_thai_thanh_toan
        nvarchar trang_thai
        nvarchar anh_vao_url
        nvarchar anh_ra_url
        nvarchar ghi_chu
    }

    CaTruc {
        bigint id PK
        varchar ma_ca
        nvarchar ten_ca
        bigint nguoi_dung_id FK
        datetime2 thoi_gian_bat_dau
        datetime2 thoi_gian_ket_thuc
        int tong_luot_vao
        int tong_luot_ra
        decimal tien_mat_thu_duoc
        decimal chuyen_khoan_thu_duoc
        decimal tong_doanh_thu
        varchar trang_thai
        nvarchar ghi_chu_ban_giao
    }

    DanhSachDen {
        bigint id PK
        varchar bien_so_xe UK
        nvarchar ly_do
        nvarchar muc_do
        nvarchar nguoi_them
        datetime2 ngay_them
    }

    DatChoTruoc {
        bigint id PK
        varchar ma_dat_cho UK
        nvarchar ho_ten
        varchar so_dien_thoai
        varchar bien_so_xe
        varchar ma_khu_vuc
        datetime2 thoi_gian_dat
        datetime2 thoi_gian_het_han
        decimal tien_coc
        bit da_thanh_toan_coc
        varchar trang_thai
    }

    DanhGia {
        bigint id PK
        nvarchar ho_ten
        varchar so_dien_thoai
        int so_sao
        nvarchar noi_dung
        varchar loai_dich_vu
        datetime2 thoi_gian
    }
"""
    }
}

# Save source files
for name, data in diagrams.items():
    source_path = os.path.join(output_dir, f"{name}.mmd")
    with open(source_path, "w", encoding="utf-8") as f:
        f.write(data["code"])
    print(f"Saved source: {source_path}")

print("Rendering diagrams with Playwright...")
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, channel="msedge")
    context = browser.new_context(viewport={"width": 1920, "height": 1080}, device_scale_factor=2)

    for name, data in diagrams.items():
        page = context.new_page()
        html_content = f"""<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
    <script>
        mermaid.initialize({{
            startOnLoad: true,
            theme: 'default',
            flowchart: {{ useMaxWidth: false, htmlLabels: true }},
            sequence: {{ useMaxWidth: false }}
        }});
    </script>
    <style>
        body {{
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #ffffff;
            margin: 40px;
            display: flex;
            flex-direction: column;
            align-items: center;
        }}
        h2 {{
            color: #1e293b;
            font-size: 24px;
            margin-bottom: 25px;
            text-align: center;
        }}
        .mermaid {{
            background: #ffffff;
            padding: 20px;
            border-radius: 8px;
        }}
    </style>
</head>
<body>
    <h2>{data['title']}</h2>
    <div class="mermaid">
{data['code']}
    </div>
</body>
</html>"""
        page.set_content(html_content)
        page.wait_for_timeout(3500)
        
        # Take screenshot of the container
        png_path = os.path.join(output_dir, f"{name}.png")
        page.screenshot(path=png_path, full_page=True)
        print(f"Rendered PNG: {png_path}")
        page.close()

    browser.close()

print("ALL DIAGRAMS GENERATED AND RENDERED SUCCESSFULLY!")


-- =========================================================================================
-- ĐỒ ÁN CHUYÊN NGÀNH: XÂY DỰNG PHẦN MỀM QUẢN LÝ GỬI XE
-- NGÀNH: TIN HỌC TÀI NGUYÊN VÀ MÔI TRƯỜNG - TRƯỜNG ĐẠI HỌC TÀI NGUYÊN VÀ MÔI TRƯỜNG TP.HCM
-- TẬP TIN KỊCH BẢN CƠ SỞ DỮ LIỆU: csdl_quan_ly_gui_xe.sql
-- HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU: MICROSOFT SQL SERVER (HỖ TRỢ UNICODE TIẾNG VIỆT NVARCHAR)
-- =========================================================================================

-- 1. TẠO CƠ SỞ DỮ LIỆU (NẾU CHƯA CÓ)
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = N'QuanLyGuiXe')
BEGIN
    CREATE DATABASE QuanLyGuiXe COLLATE Vietnamese_CI_AS;
END
GO

USE QuanLyGuiXe;
GO

-- =========================================================================================
-- 2. XÓA BẢNG CŨ NẾU ĐÃ TỒN TẠI (THEO THỨ TỰ RÀNG BUỘC KHÓA NGOẠI ĐỂ TRÁNH LỖI)
-- =========================================================================================
IF OBJECT_ID(N'dbo.VeXe', N'U') IS NOT NULL DROP TABLE dbo.VeXe;
IF OBJECT_ID(N'dbo.DatChoTruoc', N'U') IS NOT NULL DROP TABLE dbo.DatChoTruoc;
IF OBJECT_ID(N'dbo.VeThang', N'U') IS NOT NULL DROP TABLE dbo.VeThang;
IF OBJECT_ID(N'dbo.PhuongTien', N'U') IS NOT NULL DROP TABLE dbo.PhuongTien;
IF OBJECT_ID(N'dbo.KhuVuc', N'U') IS NOT NULL DROP TABLE dbo.KhuVuc;
IF OBJECT_ID(N'dbo.LoaiXe', N'U') IS NOT NULL DROP TABLE dbo.LoaiXe;
IF OBJECT_ID(N'dbo.KhachHang', N'U') IS NOT NULL DROP TABLE dbo.KhachHang;
IF OBJECT_ID(N'dbo.CaTruc', N'U') IS NOT NULL DROP TABLE dbo.CaTruc;
IF OBJECT_ID(N'dbo.DanhSachDen', N'U') IS NOT NULL DROP TABLE dbo.DanhSachDen;
IF OBJECT_ID(N'dbo.DanhGia', N'U') IS NOT NULL DROP TABLE dbo.DanhGia;
IF OBJECT_ID(N'dbo.NguoiDung', N'U') IS NOT NULL DROP TABLE dbo.NguoiDung;
GO

-- =========================================================================================
-- 3. TẠO CÁC BẢNG DỮ LIỆU THUẦN TIẾNG VIỆT (CHUẨN HỆ THỐNG QUẢN LÝ GỬI XE)
-- =========================================================================================

-- Bảng 1: Người dùng hệ thống (Quản trị viên và Nhân viên trực ca)
CREATE TABLE NguoiDung (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(100) NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    vai_tro VARCHAR(20) NOT NULL,               -- 'ADMIN' hoặc 'NHAN_VIEN'
    so_dien_thoai VARCHAR(20),
    trang_thai BIT DEFAULT 1                    -- 1: Hoạt động, 0: Đã khóa
);
GO

-- Bảng 2: Danh mục loại phương tiện và Biểu phí gửi xe
CREATE TABLE LoaiXe (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_loai NVARCHAR(100) NOT NULL UNIQUE,     -- Ví dụ: Xe máy, Ô tô (4-7 chỗ), Xe đạp / Xe điện
    gia_ban_ngay FLOAT NOT NULL,                -- Giá gửi ban ngày (VNĐ)
    gia_ban_dem FLOAT NOT NULL,                 -- Giá gửi qua đêm (VNĐ)
    gia_thang FLOAT NOT NULL,                   -- Giá đăng ký vé tháng (VNĐ)
    icon_class VARCHAR(50),                     -- Biểu tượng hiển thị (fa-motorcycle, fa-car, fa-bicycle)
    mo_ta NVARCHAR(255)
);
GO

-- Bảng 3: Phân khu bãi đỗ xe theo nhóm phương tiện
CREATE TABLE KhuVuc (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_khu VARCHAR(20) NOT NULL UNIQUE,         -- Ví dụ: 'A', 'B', 'C'
    ten_khu NVARCHAR(100) NOT NULL,             -- 'Khu A - Đỗ ô tô', 'Khu B - Đỗ xe máy'
    loai_xe_id BIGINT NULL,                     -- Phân loại loại xe ưu tiên cho khu vực
    suc_chua INT NOT NULL DEFAULT 10,           -- Tổng sức chứa thiết kế của khu vực
    vi_tri NVARCHAR(255),                       -- Tầng hầm B1, Tầng 1 sảnh chính...
    CONSTRAINT FK_KhuVuc_LoaiXe FOREIGN KEY (loai_xe_id) REFERENCES LoaiXe(id)
);
GO

-- Bảng 4: Khách hàng đăng ký thuê bao / vé tháng
CREATE TABLE KhachHang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ho_ten NVARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(20) NOT NULL,
    so_cccd VARCHAR(20),
    email VARCHAR(100),
    dia_chi NVARCHAR(255)                       -- Số căn hộ, phòng ban
);
GO

-- Bảng 5: Thẻ vé tháng
CREATE TABLE VeThang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_the VARCHAR(50) NOT NULL UNIQUE,         -- Mã thẻ RFID hoặc mã định danh vé tháng
    khach_hang_id BIGINT NOT NULL,
    bien_so_xe VARCHAR(20) NOT NULL,
    loai_xe_id BIGINT NOT NULL,
    ngay_bat_dau DATE NOT NULL,
    ngay_het_han DATE NOT NULL,
    gia_tien FLOAT NOT NULL,
    trang_thai VARCHAR(20) DEFAULT 'HOAT_DONG', -- 'HOAT_DONG', 'HET_HAN', 'KHOA'
    CONSTRAINT FK_VeThang_KhachHang FOREIGN KEY (khach_hang_id) REFERENCES KhachHang(id),
    CONSTRAINT FK_VeThang_LoaiXe FOREIGN KEY (loai_xe_id) REFERENCES LoaiXe(id)
);
GO

-- Bảng 6: Danh mục phương tiện quản lý trong hệ thống
CREATE TABLE PhuongTien (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_phuong_tien VARCHAR(50) NOT NULL UNIQUE,
    bien_so_xe VARCHAR(20) NOT NULL UNIQUE,
    loai_xe_id BIGINT NULL,
    chu_xe NVARCHAR(100) NULL,
    so_dien_thoai VARCHAR(20) NULL,
    mo_ta NVARCHAR(255) NULL,
    ngay_tao DATETIME2 DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_PhuongTien_LoaiXe FOREIGN KEY (loai_xe_id) REFERENCES LoaiXe(id)
);
GO

-- Bảng 7: Vé xe lượt vào - ra bãi (Cốt lõi quản lý gửi xe)
CREATE TABLE VeXe (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_qr VARCHAR(100) NOT NULL UNIQUE,         -- Mã QR định danh lượt gửi
    bien_so_xe VARCHAR(20) NOT NULL,
    loai_xe_id BIGINT NULL,
    ve_thang_id BIGINT NULL,                    -- Khóa ngoại nếu xe gửi theo diện vé tháng
    thoi_gian_vao DATETIME2 NOT NULL,
    thoi_gian_ra DATETIME2 NULL,
    chi_phi FLOAT NULL,                         -- Tiền phí thu được
    hinh_thuc_thanh_toan VARCHAR(50) DEFAULT 'TIEN_MAT', -- 'TIEN_MAT', 'CHUYEN_KHOAN', 'VE_THANG'
    trang_thai NVARCHAR(50) DEFAULT N'Đang gửi',         -- N'Đang gửi', N'Đã ra'
    anh_vao_url NVARCHAR(MAX) NULL,
    anh_ra_url NVARCHAR(MAX) NULL,
    ghi_chu NVARCHAR(MAX) NULL,
    CONSTRAINT FK_VeXe_LoaiXe FOREIGN KEY (loai_xe_id) REFERENCES LoaiXe(id),
    CONSTRAINT FK_VeXe_VeThang FOREIGN KEY (ve_thang_id) REFERENCES VeThang(id)
);
GO

-- Bảng 8: Quản lý ca trực và biên bản bàn giao ca
CREATE TABLE CaTruc (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_ca VARCHAR(50) NOT NULL,                 -- 'CA-SANG', 'CA-CHIEU', 'CA-DEM'
    ten_ca NVARCHAR(100) NOT NULL,              -- N'Ca Sáng', N'Ca Chiều', N'Ca Đêm'
    nguoi_dung_id BIGINT NULL,
    thoi_gian_bat_dau DATETIME2 NOT NULL,
    thoi_gian_ket_thuc DATETIME2 NULL,
    tong_luot_vao INT DEFAULT 0,
    tong_luot_ra INT DEFAULT 0,
    tien_mat_thu_duoc FLOAT DEFAULT 0,
    chuyen_khoan_thu_duoc FLOAT DEFAULT 0,
    tong_doanh_thu FLOAT DEFAULT 0,
    trang_thai VARCHAR(50) DEFAULT 'DANG_TRUC', -- 'DANG_TRUC', 'DA_BAN_GIAO'
    ghi_chu_ban_giao NVARCHAR(500) NULL,
    CONSTRAINT FK_CaTruc_NguoiDung FOREIGN KEY (nguoi_dung_id) REFERENCES NguoiDung(id)
);
GO

-- Bảng 9: Danh sách đen phương tiện cần cảnh báo an ninh (Blacklist)
CREATE TABLE DanhSachDen (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    bien_so_xe VARCHAR(20) NOT NULL UNIQUE,
    ly_do NVARCHAR(255) NOT NULL,
    muc_do NVARCHAR(50) DEFAULT 'NGUY_HIEM',    -- 'CANH_BAO', 'NGUY_HIEM'
    nguoi_them NVARCHAR(100) NULL,
    ngay_them DATETIME2 DEFAULT CURRENT_TIMESTAMP
);
GO

-- Bảng 10: Đặt giữ chỗ gửi xe trực tuyến qua Cổng dịch vụ
CREATE TABLE DatChoTruoc (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_dat_cho VARCHAR(30) NOT NULL UNIQUE,     -- 'BK-261010-001'
    ho_ten NVARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(20) NOT NULL,
    bien_so_xe VARCHAR(20) NOT NULL,
    ma_khu_vuc VARCHAR(50) NULL,                -- Mã phân khu hoặc loại xe giữ chỗ
    thoi_gian_dat DATETIME2 NOT NULL,
    thoi_gian_het_han DATETIME2 NOT NULL,
    tien_coc FLOAT DEFAULT 20000,
    trang_thai VARCHAR(20) DEFAULT 'CHO_DEN'    -- 'CHO_DEN', 'DA_VAO', 'HUY', 'HET_HAN'
);
GO

-- Bảng 11: Đánh giá và góp ý chất lượng dịch vụ từ khách hàng
CREATE TABLE DanhGia (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ho_ten NVARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(20) NULL,
    so_sao INT NOT NULL CHECK (so_sao BETWEEN 1 AND 5),
    noi_dung NVARCHAR(500) NULL,
    loai_dich_vu VARCHAR(50) DEFAULT 'CHUNG',   -- 'AN_NINH', 'VE_SINH', 'GIA_CA', 'TOC_DO', 'CHUNG'
    thoi_gian DATETIME2 DEFAULT CURRENT_TIMESTAMP
);
GO

-- =========================================================================================
-- 4. TẠO CÁC CHỈ MỤC (INDEX) TỐI ƯU TỐC ĐỘ TRUY VẤN
-- =========================================================================================
CREATE INDEX IX_VeXe_BienSo ON VeXe(bien_so_xe);
CREATE INDEX IX_VeXe_MaQR ON VeXe(ma_qr);
CREATE INDEX IX_VeXe_TrangThai ON VeXe(trang_thai);
CREATE INDEX IX_VeThang_BienSo ON VeThang(bien_so_xe);
CREATE INDEX IX_DanhSachDen_BienSo ON DanhSachDen(bien_so_xe);
GO

-- =========================================================================================
-- 5. CHÈN DỮ LIỆU MẪU BAN ĐẦU THUẦN TIẾNG VIỆT
-- =========================================================================================

-- Chèn dữ liệu Người dùng hệ thống
INSERT INTO NguoiDung (ten_dang_nhap, mat_khau, ho_ten, vai_tro, so_dien_thoai, trang_thai) VALUES
('admin', 'admin123', N'Quản Trị Viên', 'ADMIN', '0909000111', 1),
('baove', '123456', N'Nhân Viên Trực Cổng', 'NHAN_VIEN', '0909000222', 1);
GO

-- Chèn dữ liệu Danh mục loại phương tiện & Đơn giá
INSERT INTO LoaiXe (ten_loai, gia_ban_ngay, gia_ban_dem, gia_thang, icon_class, mo_ta) VALUES
(N'Xe máy', 4000, 6000, 100000, 'fa-motorcycle', N'Xe số, xe tay ga phổ thông'),
(N'Ô tô (4-7 chỗ)', 20000, 35000, 1200000, 'fa-car', N'Xe ô tô con dưới 9 chỗ'),
(N'Xe đạp / Xe điện', 2000, 3000, 50000, 'fa-bicycle', N'Xe đạp thể thao, xe đạp điện, xe máy điện'),
(N'Xe tải nhỏ / Bán tải', 30000, 50000, 1800000, 'fa-truck', N'Xe bán tải, tải nhỏ dưới 1.5 tấn');
GO

-- Chèn dữ liệu Phân khu bãi đỗ
INSERT INTO KhuVuc (ma_khu, ten_khu, loai_xe_id, suc_chua, vi_tri) VALUES
('A', N'Khu A - Đỗ ô tô', 2, 10, N'Tầng 1 sảnh chính'),
('B', N'Khu B - Đỗ xe máy', 1, 20, N'Tầng hầm B1'),
('C', N'Khu C - Xe điện và Xe đạp', 3, 10, N'Sân phụ có mái che');
GO

-- Chèn dữ liệu Phương tiện quản lý mẫu (Vehicles)
INSERT INTO PhuongTien (ma_phuong_tien, bien_so_xe, loai_xe_id, chu_xe, so_dien_thoai, mo_ta) VALUES
('PT-29B188888', '29B1-88888', 1, N'Nguyễn Văn An', '0912345678', N'Xe máy Honda SH - Cư dân Tòa A'),
('PT-30E99999', '30E-99999', 2, N'Trần Thị Bích', '0988776655', N'Toyota Camry - Cư dân Tòa B'),
('PT-59A112345', '59A1-12345', 1, N'Lê Hoàng Long', '0903112233', N'Honda Air Blade - Cư dân Tòa C'),
('PT-30G67890', '30G-67890', 2, N'Phạm Quốc Huy', '0934567890', N'Mazda 3 - Khách vãng lai'),
('PT-51H11122', '51H-11122', 2, N'Đỗ Minh Tuấn', '0945678901', N'Hyundai Tucson - Khách vãng lai'),
('PT-29T154321', '29T154321', 1, N'Vũ Thùy Linh', '0956789012', N'Yamaha Grande - Khách vãng lai'),
('PT-43B199881', '43B1-99881', 1, N'Hoàng Văn Nam', '0967890123', N'Honda Wave Alpha');
GO

-- Chèn dữ liệu Khách hàng đăng ký vé tháng
INSERT INTO KhachHang (ho_ten, so_dien_thoai, so_cccd, email, dia_chi) VALUES
(N'Nguyễn Văn An', '0912345678', '001200001234', 'an.nv@gmail.com', N'Căn hộ 1205 Tòa A'),
(N'Trần Thị Bích', '0988776655', '001200005678', 'bich.tt@gmail.com', N'Căn hộ 802 Tòa B'),
(N'Lê Hoàng Long', '0903112233', '001200009999', 'long.lh@gmail.com', N'Phòng Kế toán P.301');
GO

-- Chèn dữ liệu Thẻ vé tháng
INSERT INTO VeThang (ma_the, khach_hang_id, bien_so_xe, loai_xe_id, ngay_bat_dau, ngay_het_han, gia_tien, trang_thai) VALUES
('VT-88888888', 1, '29B1-88888', 1, DATEADD(month, -1, CAST(GETDATE() AS DATE)), DATEADD(month, 5, CAST(GETDATE() AS DATE)), 600000, 'HOAT_DONG'),
('VT-99999999', 2, '30E-99999', 2, DATEADD(day, -15, CAST(GETDATE() AS DATE)), DATEADD(month, 2, CAST(GETDATE() AS DATE)), 2400000, 'HOAT_DONG'),
('VT-12345678', 3, '59A1-12345', 1, DATEADD(month, -2, CAST(GETDATE() AS DATE)), DATEADD(month, 1, CAST(GETDATE() AS DATE)), 300000, 'HOAT_DONG');
GO

-- Chèn dữ liệu Lượt gửi xe mẫu (VeXe)
-- Xe 1: Xe vé tháng đang gửi
INSERT INTO VeXe (ma_qr, bien_so_xe, loai_xe_id, ve_thang_id, thoi_gian_vao, thoi_gian_ra, chi_phi, hinh_thuc_thanh_toan, trang_thai) VALUES
('QR-DEMO-001', '29B1-88888', 1, 1, DATEADD(hour, -3, GETDATE()), NULL, NULL, 'VE_THANG', N'Đang gửi');

-- Xe 2: Xe ô tô vãng lai đang gửi
INSERT INTO VeXe (ma_qr, bien_so_xe, loai_xe_id, ve_thang_id, thoi_gian_vao, thoi_gian_ra, chi_phi, hinh_thuc_thanh_toan, trang_thai) VALUES
('QR-DEMO-002', '30G-67890', 2, NULL, DATEADD(minute, -90, GETDATE()), NULL, NULL, 'TIEN_MAT', N'Đang gửi');

-- Xe 3: Ô tô đã ra bãi, thanh toán chuyển khoản VietQR
INSERT INTO VeXe (ma_qr, bien_so_xe, loai_xe_id, ve_thang_id, thoi_gian_vao, thoi_gian_ra, chi_phi, hinh_thuc_thanh_toan, trang_thai) VALUES
('QR-DEMO-003', '51H-11122', 2, NULL, DATEADD(hour, -5, GETDATE()), DATEADD(hour, -2, GETDATE()), 20000, 'CHUYEN_KHOAN', N'Đã ra');

-- Xe 4: Xe máy đã ra bãi, thanh toán tiền mặt
INSERT INTO VeXe (ma_qr, bien_so_xe, loai_xe_id, ve_thang_id, thoi_gian_vao, thoi_gian_ra, chi_phi, hinh_thuc_thanh_toan, trang_thai) VALUES
('QR-DEMO-004', '29T1-54321', 1, NULL, DATEADD(hour, -4, GETDATE()), DATEADD(hour, -1, GETDATE()), 4000, 'TIEN_MAT', N'Đã ra');

-- Xe 5: Xe tồn bãi quá 24h để kiểm thử tính năng Cảnh báo an ninh
INSERT INTO VeXe (ma_qr, bien_so_xe, loai_xe_id, ve_thang_id, thoi_gian_vao, thoi_gian_ra, chi_phi, hinh_thuc_thanh_toan, trang_thai) VALUES
('QR-DEMO-005', '43B1-99881', 1, NULL, DATEADD(hour, -32, GETDATE()), NULL, NULL, 'TIEN_MAT', N'Đang gửi');
GO

-- Chèn dữ liệu Danh sách đen mẫu (Blacklist)
INSERT INTO DanhSachDen (bien_so_xe, ly_do, muc_do, nguoi_them) VALUES
('59X3-99999', N'Xe báo mất trộm trong khu đô thị ngày 08/10, nghi phạm có vũ khí', 'NGUY_HIEM', N'Công an khu vực');
GO

-- Chèn dữ liệu Đánh giá từ khách hàng
INSERT INTO DanhGia (ho_ten, so_dien_thoai, so_sao, noi_dung, loai_dich_vu) VALUES
(N'Nguyễn Văn An', '0912345678', 5, N'Quét mã QR và nhận diện biển số cực nhanh, không lo ùn tắc giờ cao điểm.', 'TOC_DO'),
(N'Trần Thị Bích', '0988776655', 5, N'Có camera và hệ thống cảnh báo xe tồn bãi nên gửi xe ở đây rất yên tâm.', 'AN_NINH'),
(N'Phạm Minh Tuấn', '0903112233', 5, N'Khu C có trạm sạc cho xe máy điện rất tiện lợi và an toàn.', 'CHUNG');
GO

-- =========================================================================================
-- 6. CÁC CÂU LỆNH TRUY VẤN MẪU PHỤC VỤ BÁO CÁO VÀ NGHIỆP VỤ
-- =========================================================================================

-- Truy vấn 1: Thống kê số lượng phương tiện đang gửi và sức chứa theo từng phân khu
SELECT 
    kv.ten_khu AS [Tên Khu Vực],
    kv.suc_chua AS [Tổng Sức Chứa],
    COUNT(vx.id) AS [Số Xe Đang Gửi],
    (kv.suc_chua - COUNT(vx.id)) AS [Số Chỗ Trống]
FROM KhuVuc kv
LEFT JOIN LoaiXe lx ON kv.loai_xe_id = lx.id
LEFT JOIN VeXe vx ON vx.loai_xe_id = lx.id AND vx.trang_thai = N'Đang gửi'
GROUP BY kv.ten_khu, kv.suc_chua;

-- Truy vấn 2: Thống kê các phương tiện đang lưu bãi quá 24 tiếng (Cảnh báo an ninh)
SELECT 
    vx.ma_qr AS [Mã Vé],
    vx.bien_so_xe AS [Biển Số Xe],
    lx.ten_loai AS [Loại Xe],
    vx.thoi_gian_vao AS [Thời Gian Vào],
    DATEDIFF(HOUR, vx.thoi_gian_vao, GETDATE()) AS [Số Giờ Tồn Bãi]
FROM VeXe vx
LEFT JOIN LoaiXe lx ON vx.loai_xe_id = lx.id
WHERE vx.trang_thai = N'Đang gửi'
  AND DATEDIFF(HOUR, vx.thoi_gian_vao, GETDATE()) >= 24;

-- Truy vấn 3: Doanh thu theo từng loại phương tiện
SELECT 
    lx.ten_loai AS [Loại Phương Tiện],
    COUNT(vx.id) AS [Số Lượt Ra],
    ISNULL(SUM(vx.chi_phi), 0) AS [Tổng Doanh Thu (VNĐ)]
FROM LoaiXe lx
LEFT JOIN VeXe vx ON lx.id = vx.loai_xe_id AND vx.trang_thai = N'Đã ra'
GROUP BY lx.ten_loai;
GO

PRINT N'=============================================================================';
PRINT N'ĐÃ KHỞI TẠO THÀNH CÔNG TOÀN BỘ CƠ SỞ DỮ LIỆU QUẢN LÝ GỬI XE THUẦN TIẾNG VIỆT!';
PRINT N'=============================================================================';
GO

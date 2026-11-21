-- =========================================================================================
-- ĐỒ ÁN CHUYÊN NGÀNH: XÂY DỰNG PHẦN MỀM QUẢN LÝ GỬI XE
-- TẬP TIN KỊCH BẢN CƠ SỞ DỮ LIỆU ĐẦY ĐỦ (13 BẢNG & 23 KHÓA NGOẠI CHUẨN)
-- ĐƯỜNG DẪN: database/quan_ly_gui_xe.sql
-- HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU: MICROSOFT SQL SERVER 2019 / 2022
-- HỖ TRỢ UNICODE TIẾNG VIỆT NVARCHAR TOÀN DIỆN
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
-- 2. XÓA BẢNG CŨ THEO THỨ TỰ RÀNG BUỘC KHÓA NGOẠI ĐỂ TRÁNH XUNG ĐỘT
-- =========================================================================================
IF OBJECT_ID(N'dbo.ve_xe', N'U') IS NOT NULL DROP TABLE dbo.ve_xe;
IF OBJECT_ID(N'dbo.password_reset_otp', N'U') IS NOT NULL DROP TABLE dbo.password_reset_otp;
IF OBJECT_ID(N'dbo.danh_gia', N'U') IS NOT NULL DROP TABLE dbo.danh_gia;
IF OBJECT_ID(N'dbo.dat_cho_truoc', N'U') IS NOT NULL DROP TABLE dbo.dat_cho_truoc;
IF OBJECT_ID(N'dbo.danh_sach_den', N'U') IS NOT NULL DROP TABLE dbo.danh_sach_den;
IF OBJECT_ID(N'dbo.ca_truc', N'U') IS NOT NULL DROP TABLE dbo.ca_truc;
IF OBJECT_ID(N'dbo.ve_thang', N'U') IS NOT NULL DROP TABLE dbo.ve_thang;
IF OBJECT_ID(N'dbo.phuong_tien', N'U') IS NOT NULL DROP TABLE dbo.phuong_tien;
IF OBJECT_ID(N'dbo.vi_tri_do', N'U') IS NOT NULL DROP TABLE dbo.vi_tri_do;

-- Phá vỡ quan hệ 2 chiều giữa loai_xe và khu_vuc trước khi drop
IF OBJECT_ID(N'dbo.khu_vuc', N'U') IS NOT NULL
BEGIN
    DECLARE @dropFkSql NVARCHAR(MAX) = N'';
    SELECT @dropFkSql += N'ALTER TABLE dbo.khu_vuc DROP CONSTRAINT ' + name + N'; '
    FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.khu_vuc');
    IF @dropFkSql <> N'' EXEC sp_executesql @dropFkSql;
END

IF OBJECT_ID(N'dbo.loai_xe', N'U') IS NOT NULL
BEGIN
    DECLARE @dropFkSql2 NVARCHAR(MAX) = N'';
    SELECT @dropFkSql2 += N'ALTER TABLE dbo.loai_xe DROP CONSTRAINT ' + name + N'; '
    FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID(N'dbo.loai_xe');
    IF @dropFkSql2 <> N'' EXEC sp_executesql @dropFkSql2;
END

IF OBJECT_ID(N'dbo.khu_vuc', N'U') IS NOT NULL DROP TABLE dbo.khu_vuc;
IF OBJECT_ID(N'dbo.loai_xe', N'U') IS NOT NULL DROP TABLE dbo.loai_xe;
IF OBJECT_ID(N'dbo.khach_hang', N'U') IS NOT NULL DROP TABLE dbo.khach_hang;
IF OBJECT_ID(N'dbo.nguoi_dung', N'U') IS NOT NULL DROP TABLE dbo.nguoi_dung;
GO

-- =========================================================================================
-- 3. TẠO CẤU TRÚC 13 BẢNG THỰC THỂ CƠ SỞ DỮ LIỆU
-- =========================================================================================

-- Bảng 1: Người dùng & Phân quyền hệ thống
CREATE TABLE dbo.nguoi_dung (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_dang_nhap VARCHAR(50) NOT NULL UNIQUE,
    mat_khau VARCHAR(100) NOT NULL,
    ho_ten NVARCHAR(100) NOT NULL,
    vai_tro VARCHAR(20) NOT NULL,                 -- ADMIN, NHAN_VIEN, KHACH_HANG
    so_dien_thoai VARCHAR(20) NULL,
    email NVARCHAR(150) NULL,
    trang_thai BIT DEFAULT 1                      -- 1: Hoạt động, 0: Khóa
);
GO

-- Bảng 2: Hồ sơ khách hàng / Cư dân
CREATE TABLE dbo.khach_hang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ho_ten NVARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(15) NOT NULL UNIQUE,
    cccd VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    dia_chi NVARCHAR(255) NULL,
    nguoi_dung_id BIGINT NULL                     -- FK liên kết tài khoản portal
);
GO

-- Bảng 3: Phân loại phương tiện & Biểu phí gửi xe
CREATE TABLE dbo.loai_xe (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ten_loai NVARCHAR(100) NOT NULL UNIQUE,       -- Xe máy, Ô tô (4-7 chỗ), Xe đạp / Xe điện
    gia_ban_ngay FLOAT NOT NULL,                  -- Phí gửi ngày
    gia_ban_dem FLOAT NOT NULL,                   -- Phí gửi đêm
    gia_thang FLOAT NOT NULL,                     -- Phí thuê bao tháng
    icon_class VARCHAR(50) NULL,                  -- fa-motorcycle, fa-car, fa-bicycle
    mo_ta NVARCHAR(255) NULL,
    khu_vuc_id BIGINT NULL                        -- FK phân khu chỉ định mặc định
);
GO

-- Bảng 4: Phân khu bãi đỗ xe
CREATE TABLE dbo.khu_vuc (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_khu VARCHAR(10) NOT NULL UNIQUE,           -- 2B, 4B, XD
    ten_khu NVARCHAR(100) NOT NULL,               -- Khu Đỗ Xe 2 Bánh, Khu Đỗ Xe 4 Bánh
    loai_xe_id BIGINT NULL,                       -- FK loại xe ưu tiên
    suc_chua INT NOT NULL DEFAULT 20,             -- Tổng ô thiết kế
    vi_tri NVARCHAR(255) NULL                     -- Tầng hầm B1, Sân trước
);
GO

-- Bảng 5: Vị trí ô đỗ xe thông minh & Bản đồ bãi xe
CREATE TABLE dbo.vi_tri_do (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_vi_tri VARCHAR(30) NOT NULL UNIQUE,        -- 2B-01, 4B-01, XD-01
    khu_vuc_id BIGINT NULL,                       -- FK khu vực chứa ô đỗ
    trang_thai VARCHAR(30) DEFAULT 'TRONG',       -- TRONG, DA_DO, BAO_TRI
    bien_so_xe_dang_do VARCHAR(30) NULL,
    thoi_gian_vao_do DATETIME2 NULL,
    version BIGINT NOT NULL DEFAULT 0             -- Optimistic Locking
);
GO

-- Bảng 6: Danh mục phương tiện quản lý
CREATE TABLE dbo.phuong_tien (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_phuong_tien VARCHAR(50) NULL UNIQUE,
    bien_so_xe VARCHAR(20) NOT NULL UNIQUE,
    loai_xe_id BIGINT NOT NULL,                   -- FK loại xe
    chu_xe NVARCHAR(100) NULL,
    so_dien_thoai VARCHAR(20) NULL,
    mo_ta NVARCHAR(255) NULL,
    ngay_tao DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    khach_hang_id BIGINT NULL                     -- FK khách hàng chủ xe
);
GO

-- Bảng 7: Thuê bao vé tháng định kỳ
CREATE TABLE dbo.ve_thang (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_the VARCHAR(50) NOT NULL UNIQUE,           -- Mã thẻ từ RFID / QR tháng
    khach_hang_id BIGINT NOT NULL,                -- FK khách hàng
    bien_so_xe VARCHAR(20) NOT NULL,
    loai_xe_id BIGINT NOT NULL,                   -- FK loại xe
    ngay_bat_dau DATE NOT NULL,
    ngay_het_han DATE NOT NULL,
    gia_tien FLOAT NULL,
    trang_thai VARCHAR(20) DEFAULT 'HOAT_DONG'    -- HOAT_DONG, HET_HAN, KHOA
);
GO

-- Bảng 8: Quản lý ca trực & Bàn giao doanh thu
CREATE TABLE dbo.ca_truc (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_ca VARCHAR(50) NOT NULL,                   -- CA-SANG, CA-CHIEU, CA-DEM
    ten_ca NVARCHAR(100) NOT NULL,                -- Ca Sáng, Ca Chiều, Ca Đêm
    nguoi_dung_id BIGINT NULL,                    -- FK nhân viên trực
    thoi_gian_bat_dau DATETIME2 NOT NULL,
    thoi_gian_ket_thuc DATETIME2 NULL,
    tong_luot_vao INT DEFAULT 0,
    tong_luot_ra INT DEFAULT 0,
    tien_mat_thu_duoc FLOAT DEFAULT 0,
    chuyen_khoan_thu_duoc FLOAT DEFAULT 0,
    tong_doanh_thu FLOAT DEFAULT 0,
    trang_thai VARCHAR(50) DEFAULT 'DANG_TRUC',   -- DANG_TRUC, DA_BAN_GIAO
    ghi_chu_ban_giao NVARCHAR(500) NULL
);
GO

-- Bảng 9: Danh sách đen cảnh báo an ninh (Blacklist)
CREATE TABLE dbo.danh_sach_den (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    bien_so_xe VARCHAR(20) NOT NULL UNIQUE,
    ly_do NVARCHAR(255) NOT NULL,                 -- Xe báo mất trộm, vi phạm nội quy
    muc_do NVARCHAR(50) DEFAULT N'NGUY_HIEM',     -- CANH_BAO, NGUY_HIEM
    ngay_them DATETIME2 DEFAULT CURRENT_TIMESTAMP,
    nguoi_them NVARCHAR(100) NULL,
    phuong_tien_id BIGINT NULL,                   -- FK phương tiện nếu đã có hồ sơ
    nguoi_them_id BIGINT NULL                     -- FK nhân viên/bảo vệ báo cáo
);
GO

-- Bảng 10: Đặt giữ chỗ gửi xe trực tuyến qua Portal
CREATE TABLE dbo.dat_cho_truoc (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ma_dat_cho VARCHAR(30) NOT NULL UNIQUE,       -- BK-261010-001
    ho_ten NVARCHAR(100) NOT NULL,                -- Hỗ trợ đầy đủ tiếng Việt NVARCHAR
    so_dien_thoai VARCHAR(20) NOT NULL,
    bien_so_xe VARCHAR(20) NOT NULL,
    ma_khu_vuc VARCHAR(50) NULL,
    thoi_gian_dat DATETIME2 NOT NULL,
    thoi_gian_het_han DATETIME2 NOT NULL,
    tien_coc FLOAT DEFAULT 20000,
    da_thanh_toan_coc BIT DEFAULT 0,
    ma_giao_dich_coc VARCHAR(100) NULL,
    thoi_gian_checkin DATETIME2 NULL,
    trang_thai VARCHAR(20) DEFAULT 'CHO_DEN',     -- CHO_DEN, DA_VAO, HUY, HET_HAN
    khu_vuc_id BIGINT NULL,                       -- FK khu vực đặt trước
    phuong_tien_id BIGINT NULL                    -- FK phương tiện đặt trước
);
GO

-- Bảng 11: Đánh giá chất lượng dịch vụ từ khách hàng
CREATE TABLE dbo.danh_gia (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ho_ten NVARCHAR(100) NOT NULL,                -- Hỗ trợ đầy đủ tiếng Việt NVARCHAR
    so_dien_thoai VARCHAR(20) NULL,
    so_sao INT NOT NULL CHECK (so_sao BETWEEN 1 AND 5),
    noi_dung NVARCHAR(500) NULL,
    loai_dich_vu VARCHAR(50) DEFAULT 'CHUNG',     -- AN_NINH, VE_SINH, GIA_CA, TOC_DO, CHUNG
    thoi_gian DATETIME2 DEFAULT CURRENT_TIMESTAMP,
    khach_hang_id BIGINT NULL,                    -- FK khách hàng đánh giá
    nguoi_dung_id BIGINT NULL                     -- FK tài khoản người dùng đánh giá
);
GO

-- Bảng 12: Mã OTP khôi phục mật khẩu qua Email
CREATE TABLE dbo.password_reset_otp (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(120) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expire_at DATETIME2 NOT NULL,
    used BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT CURRENT_TIMESTAMP,
    nguoi_dung_id BIGINT NULL                     -- FK tài khoản yêu cầu OTP
);
GO

-- Bảng 13: Lượt gửi xe (Thực thể nghiệp vụ trung tâm)
CREATE TABLE dbo.ve_xe (
    id INT IDENTITY(1,1) PRIMARY KEY,
    maqr VARCHAR(255) NOT NULL UNIQUE,            -- Mã QR định danh lượt gửi
    bien_so_xe VARCHAR(20) NOT NULL,              -- Biển số nhận diện
    bien_so_clean VARCHAR(20) NULL,               -- Biển số chuẩn hóa tra cứu nhanh
    loai_xe_id BIGINT NULL,                       -- FK loại xe
    ve_thang_id BIGINT NULL,                      -- FK vé tháng nếu có
    vi_tri_do_id BIGINT NULL,                     -- FK ô đỗ xe cụ thể
    khu_vuc_id BIGINT NULL,                       -- FK khu vực đỗ xe
    dat_cho_truoc_id BIGINT NULL UNIQUE,          -- FK booking đặt chỗ trước
    thoi_gian_vao DATETIME2 NOT NULL,
    thoi_gian_ra DATETIME2 NULL,
    chi_phi FLOAT NULL,                           -- Tiền thanh toán cuối
    so_tien_phai_thu FLOAT NULL,                  -- Phí tính theo biểu giá
    tien_coc_khau_tru FLOAT DEFAULT 0,            -- Tiền cọc booking đã trừ
    so_tien_thuc_thu FLOAT NULL,                  -- Tiền thực nhận
    hinh_thuc_thanh_toan VARCHAR(50) DEFAULT 'TIEN_MAT', -- TIEN_MAT, CHUYEN_KHOAN, VE_THANG
    trang_thai_thanh_toan VARCHAR(50) DEFAULT 'CHUA_THANH_TOAN',
    ma_giao_dich VARCHAR(100) NULL,
    thoi_gian_thanh_toan DATETIME2 NULL,
    nguoi_xac_nhan VARCHAR(100) NULL,             -- Username nhân viên xe ra
    trang_thai NVARCHAR(50) DEFAULT N'Đang gửi',  -- N'Đang gửi', N'Đã ra'
    anh_vao_url NVARCHAR(MAX) NULL,               -- Ảnh camera làn vào
    anh_ra_url NVARCHAR(MAX) NULL,                -- Ảnh camera làn ra
    ghi_chu NVARCHAR(MAX) NULL,
    da_kiem_tra BIT DEFAULT 0,                    -- Kiểm tra xe quá 24h
    thoi_gian_kiem_tra DATETIME2 NULL,
    nguoi_kiem_tra VARCHAR(100) NULL,
    ghi_chu_kiem_tra NVARCHAR(MAX) NULL,
    phuong_tien_id BIGINT NULL,                   -- FK hồ sơ phương tiện
    ca_truc_id BIGINT NULL                        -- FK ca trực tiếp nhận
);
GO

-- =========================================================================================
-- 4. THIẾT LẬP TOÀN BỘ 23 KHÓA NGOẠI (FOREIGN KEYS)
-- =========================================================================================

-- 1. ca_truc -> nguoi_dung
ALTER TABLE dbo.ca_truc ADD CONSTRAINT FKqui05bwdvo7xpt60dfhwwyuvq 
FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.nguoi_dung(id);

-- 2 & 3. danh_gia -> khach_hang, nguoi_dung
ALTER TABLE dbo.danh_gia ADD CONSTRAINT FK_DanhGia_KhachHang 
FOREIGN KEY (khach_hang_id) REFERENCES dbo.khach_hang(id) ON DELETE SET NULL;

ALTER TABLE dbo.danh_gia ADD CONSTRAINT FK_DanhGia_NguoiDung 
FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.nguoi_dung(id) ON DELETE SET NULL;

-- 4 & 5. danh_sach_den -> nguoi_dung, phuong_tien
ALTER TABLE dbo.danh_sach_den ADD CONSTRAINT FK_DanhSachDen_NguoiDung 
FOREIGN KEY (nguoi_them_id) REFERENCES dbo.nguoi_dung(id) ON DELETE SET NULL;

ALTER TABLE dbo.danh_sach_den ADD CONSTRAINT FK_DanhSachDen_PhuongTien 
FOREIGN KEY (phuong_tien_id) REFERENCES dbo.phuong_tien(id) ON DELETE SET NULL;

-- 6 & 7. dat_cho_truoc -> khu_vuc, phuong_tien
ALTER TABLE dbo.dat_cho_truoc ADD CONSTRAINT FK_DatChoTruoc_KhuVuc 
FOREIGN KEY (khu_vuc_id) REFERENCES dbo.khu_vuc(id) ON DELETE SET NULL;

ALTER TABLE dbo.dat_cho_truoc ADD CONSTRAINT FK_DatChoTruoc_PhuongTien 
FOREIGN KEY (phuong_tien_id) REFERENCES dbo.phuong_tien(id) ON DELETE SET NULL;

-- 8. khach_hang -> nguoi_dung
ALTER TABLE dbo.khach_hang ADD CONSTRAINT FK_KhachHang_NguoiDung 
FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.nguoi_dung(id) ON DELETE SET NULL;

-- 9. khu_vuc -> loai_xe
ALTER TABLE dbo.khu_vuc ADD CONSTRAINT FKks9w3l9syfn3t8t3q7n7nk2je 
FOREIGN KEY (loai_xe_id) REFERENCES dbo.loai_xe(id);

-- 10. loai_xe -> khu_vuc
ALTER TABLE dbo.loai_xe ADD CONSTRAINT FKaf5r5t5wtw6cyq95spus5pp 
FOREIGN KEY (khu_vuc_id) REFERENCES dbo.khu_vuc(id);

-- 11. password_reset_otp -> nguoi_dung
ALTER TABLE dbo.password_reset_otp ADD CONSTRAINT FK_PasswordResetOtp_NguoiDung 
FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.nguoi_dung(id) ON DELETE CASCADE;

-- 12 & 13. phuong_tien -> khach_hang, loai_xe
ALTER TABLE dbo.phuong_tien ADD CONSTRAINT FK_PhuongTien_KhachHang 
FOREIGN KEY (khach_hang_id) REFERENCES dbo.khach_hang(id) ON DELETE SET NULL;

ALTER TABLE dbo.phuong_tien ADD CONSTRAINT FK7uaa0it2sgxy6bxqvngar7rnk 
FOREIGN KEY (loai_xe_id) REFERENCES dbo.loai_xe(id);

-- 14 & 15. ve_thang -> khach_hang, loai_xe
ALTER TABLE dbo.ve_thang ADD CONSTRAINT FKtbcl3d63j5gw4k20762snrksj 
FOREIGN KEY (khach_hang_id) REFERENCES dbo.khach_hang(id);

ALTER TABLE dbo.ve_thang ADD CONSTRAINT FKohqlf8ewwcr35me4qqqa2fsw 
FOREIGN KEY (loai_xe_id) REFERENCES dbo.loai_xe(id);

-- 16-22. ve_xe -> ca_truc, dat_cho_truoc, khu_vuc, loai_xe, phuong_tien, ve_thang, vi_tri_do
ALTER TABLE dbo.ve_xe ADD CONSTRAINT FK_VeXe_CaTruc 
FOREIGN KEY (ca_truc_id) REFERENCES dbo.ca_truc(id) ON DELETE SET NULL;

ALTER TABLE dbo.ve_xe ADD CONSTRAINT FKmcamxtsi58tj4m045i9x3mfrs 
FOREIGN KEY (dat_cho_truoc_id) REFERENCES dbo.dat_cho_truoc(id);

ALTER TABLE dbo.ve_xe ADD CONSTRAINT FKqygxlaajsarl8m7o0p3q8f9m1 
FOREIGN KEY (khu_vuc_id) REFERENCES dbo.khu_vuc(id);

ALTER TABLE dbo.ve_xe ADD CONSTRAINT FK26qnknljwg0t1ow4ai1brjilg 
FOREIGN KEY (loai_xe_id) REFERENCES dbo.loai_xe(id);

ALTER TABLE dbo.ve_xe ADD CONSTRAINT FK_VeXe_PhuongTien 
FOREIGN KEY (phuong_tien_id) REFERENCES dbo.phuong_tien(id) ON DELETE SET NULL;

ALTER TABLE dbo.ve_xe ADD CONSTRAINT FKih4ihedda3xwnyvqmlvwmsxro 
FOREIGN KEY (ve_thang_id) REFERENCES dbo.ve_thang(id);

ALTER TABLE dbo.ve_xe ADD CONSTRAINT FKbyfctnwaj04vu9e4si2g35r1h 
FOREIGN KEY (vi_tri_do_id) REFERENCES dbo.vi_tri_do(id);

-- 23. vi_tri_do -> khu_vuc
ALTER TABLE dbo.vi_tri_do ADD CONSTRAINT FKqm27pc1pxulhd4fcuh6joahah 
FOREIGN KEY (khu_vuc_id) REFERENCES dbo.khu_vuc(id);
GO

-- =========================================================================================
-- 5. TẠO CÁC CHỈ MỤC TỐI ƯU HIỆU NĂNG TRUY VẤN (INDEXES)
-- =========================================================================================
CREATE INDEX idx_vexe_bienso_trangthai ON dbo.ve_xe(bien_so_xe, trang_thai);
CREATE INDEX idx_vexe_biensoclean_trangthai ON dbo.ve_xe(bien_so_clean, trang_thai);
CREATE INDEX idx_vexe_maqr ON dbo.ve_xe(maqr);
CREATE INDEX IX_VeXe_PhuongTien ON dbo.ve_xe(phuong_tien_id);
CREATE INDEX IX_VeXe_CaTruc ON dbo.ve_xe(ca_truc_id);
CREATE INDEX idx_datcho_bienso_trangthai ON dbo.dat_cho_truoc(bien_so_xe, trang_thai);
CREATE INDEX IX_PhuongTien_KhachHang ON dbo.phuong_tien(khach_hang_id);
CREATE INDEX IX_KhachHang_NguoiDung ON dbo.khach_hang(nguoi_dung_id);
CREATE INDEX IX_DanhSachDen_PhuongTien ON dbo.danh_sach_den(phuong_tien_id);
CREATE INDEX IX_DanhSachDen_NguoiThem ON dbo.danh_sach_den(nguoi_them_id);
GO

-- =========================================================================================
-- 6. NẠP DỮ LIỆU KHỞI TẠO MẪU ĐẦY ĐỦ (SEED DATA)
-- =========================================================================================

-- 6.1. Người dùng hệ thống (Mật khẩu admin123 / 123456 mã hóa BCrypt hoặc plain text tương thích)
INSERT INTO dbo.nguoi_dung (ten_dang_nhap, mat_khau, ho_ten, vai_tro, so_dien_thoai, email, trang_thai) VALUES
('admin', '$2a$10$7Z2v6J5G8y8l2K4K4K4K4eN8z7K4K4K4K4K4K4K4K4K4K4K4K4K4K', N'Quản Trị Viên', 'ADMIN', '0909000111', N'admin@ecoparking.vn', 1),
('baove', '$2a$10$7Z2v6J5G8y8l2K4K4K4K4eN8z7K4K4K4K4K4K4K4K4K4K4K4K4K4K', N'Nhân Viên Trực Cổng', 'NHAN_VIEN', '0909000222', N'baove@ecoparking.vn', 1),
('nguyenvanan', '$2a$10$7Z2v6J5G8y8l2K4K4K4K4eN8z7K4K4K4K4K4K4K4K4K4K4K4K4K4K', N'Nguyễn Văn An', 'KHACH_HANG', '0912345678', N'an.nv@gmail.com', 1),
('tranthibich', '$2a$10$7Z2v6J5G8y8l2K4K4K4K4eN8z7K4K4K4K4K4K4K4K4K4K4K4K4K4K', N'Trần Thị Bích', 'KHACH_HANG', '0988776655', N'bich.tt@gmail.com', 1);

-- 6.2. Danh mục loại xe & Đơn giá
INSERT INTO dbo.loai_xe (ten_loai, gia_ban_ngay, gia_ban_dem, gia_thang, icon_class, mo_ta, khu_vuc_id) VALUES
(N'Xe máy', 4000, 6000, 100000, 'fa-motorcycle', N'Xe số, xe tay ga phổ thông', NULL),
(N'Ô tô (4-7 chỗ)', 20000, 35000, 1200000, 'fa-car', N'Xe ô tô con dưới 9 chỗ', NULL),
(N'Xe đạp / Xe điện', 3000, 3000, 50000, 'fa-bicycle', N'Xe đạp thể thao, xe đạp điện, xe máy điện', NULL),
(N'Xe tải nhỏ / Bán tải', 30000, 50000, 1800000, 'fa-truck', N'Xe bán tải, tải nhỏ dưới 1.5 tấn', NULL);

-- 6.3. Phân khu bãi đỗ xe
INSERT INTO dbo.khu_vuc (ma_khu, ten_khu, loai_xe_id, suc_chua, vi_tri) VALUES
('2B', N'Khu Đỗ Xe 2 Bánh', 1, 100, N'Tầng hầm B1 trung tâm'),
('4B', N'Khu Đỗ Xe 4 Bánh', 2, 20, N'Sân đỗ chính ngoài trời'),
('XD', N'Khu Dành Cho Xe Đạp', 3, 30, N'Lán xe có mái che');

-- Cập nhật khu_vuc_id ngược lại cho loai_xe
UPDATE dbo.loai_xe SET khu_vuc_id = 1 WHERE id = 1;
UPDATE dbo.loai_xe SET khu_vuc_id = 2 WHERE id = 2;
UPDATE dbo.loai_xe SET khu_vuc_id = 3 WHERE id = 3;
UPDATE dbo.loai_xe SET khu_vuc_id = 2 WHERE id = 4;

-- 6.4. Vị trí ô đỗ xe thông minh (Khởi tạo tự động 100 ô 2B, 20 ô 4B, 30 ô XD)
DECLARE @i INT = 1;
WHILE @i <= 100
BEGIN
    INSERT INTO dbo.vi_tri_do (ma_vi_tri, khu_vuc_id, trang_thai, version) 
    VALUES ('2B-' + RIGHT('00' + CAST(@i AS VARCHAR(3)), 2), 1, 'TRONG', 0);
    SET @i = @i + 1;
END

SET @i = 1;
WHILE @i <= 20
BEGIN
    INSERT INTO dbo.vi_tri_do (ma_vi_tri, khu_vuc_id, trang_thai, version) 
    VALUES ('4B-' + RIGHT('00' + CAST(@i AS VARCHAR(3)), 2), 2, 'TRONG', 0);
    SET @i = @i + 1;
END

SET @i = 1;
WHILE @i <= 30
BEGIN
    INSERT INTO dbo.vi_tri_do (ma_vi_tri, khu_vuc_id, trang_thai, version) 
    VALUES ('XD-' + RIGHT('00' + CAST(@i AS VARCHAR(3)), 2), 3, 'TRONG', 0);
    SET @i = @i + 1;
END
GO

-- 6.5. Khách hàng
INSERT INTO dbo.khach_hang (ho_ten, so_dien_thoai, cccd, email, dia_chi, nguoi_dung_id) VALUES
(N'Nguyễn Văn An', '0912345678', '001200001234', 'an.nv@gmail.com', N'Căn hộ 1205 Tòa A', 3),
(N'Trần Thị Bích', '0988776655', '001200005678', 'bich.tt@gmail.com', N'Căn hộ 802 Tòa B', 4),
(N'Lê Hoàng Long', '0903112233', '001200009999', 'long.lh@gmail.com', N'Phòng Kế toán P.301', NULL),
(N'Trần Quốc Bảo', '0988776600', '001200008888', 'bao.tq@gmail.com', N'Căn hộ 1502 Tòa Landmark', NULL);

-- 6.6. Phương tiện quản lý
INSERT INTO dbo.phuong_tien (ma_phuong_tien, bien_so_xe, loai_xe_id, chu_xe, so_dien_thoai, mo_ta, khach_hang_id) VALUES
('PT-29B188888', '29B1-88888', 1, N'Nguyễn Văn An', '0912345678', N'Honda SH 150i - Cư dân Tòa A', 1),
('PT-30E99999', '30E-99999', 2, N'Trần Thị Bích', '0988776655', N'Toyota Camry - Cư dân Tòa B', 2),
('PT-59A112345', '59A1-12345', 1, N'Lê Hoàng Long', '0903112233', N'Honda Air Blade - Cư dân Tòa C', 3),
('PT-30G67890', '30G-67890', 2, N'Phạm Quốc Huy', '0934567890', N'Mazda 3 - Khách vãng lai', NULL),
('PT-51H11122', '51H-11122', 2, N'Đỗ Minh Tuấn', '0945678901', N'Hyundai Tucson - Khách vãng lai', NULL),
('PT-29T154321', '29T1-54321', 1, N'Vũ Thùy Linh', '0956789012', N'Yamaha Grande - Khách vãng lai', NULL),
('PT-43B199881', '43B1-99881', 1, N'Hoàng Văn Nam', '0967890123', N'Honda Wave Alpha', NULL);

-- 6.7. Vé tháng
INSERT INTO dbo.ve_thang (ma_the, khach_hang_id, bien_so_xe, loai_xe_id, ngay_bat_dau, ngay_het_han, gia_tien, trang_thai) VALUES
('VT-88888888', 1, '29B1-88888', 1, DATEADD(month, -1, CAST(GETDATE() AS DATE)), DATEADD(month, 5, CAST(GETDATE() AS DATE)), 600000, 'HOAT_DONG'),
('VT-99999999', 2, '30E-99999', 2, DATEADD(day, -15, CAST(GETDATE() AS DATE)), DATEADD(month, 2, CAST(GETDATE() AS DATE)), 2400000, 'HOAT_DONG'),
('VT-12345678', 3, '59A1-12345', 1, DATEADD(month, -2, CAST(GETDATE() AS DATE)), DATEADD(month, 1, CAST(GETDATE() AS DATE)), 300000, 'HOAT_DONG');

-- 6.8. Ca trực ban đầu
INSERT INTO dbo.ca_truc (ma_ca, ten_ca, nguoi_dung_id, thoi_gian_bat_dau, thoi_gian_ket_thuc, tong_luot_vao, tong_luot_ra, tien_mat_thu_duoc, chuyen_khoan_thu_duoc, tong_doanh_thu, trang_thai, ghi_chu_ban_giao) VALUES
('CA-SANG', N'Ca Sáng (06:00 - 14:00)', 2, DATEADD(hour, -4, GETDATE()), NULL, 2, 2, 4000, 20000, 24000, 'DANG_TRUC', N'Ca trực ổn định, các làn vào/ra vận hành bình thường.');

-- 6.9. Danh sách đen (Blacklist)
INSERT INTO dbo.danh_sach_den (bien_so_xe, ly_do, muc_do, nguoi_them, nguoi_them_id) VALUES
('59X3-99999', N'Xe báo mất trộm trong khu đô thị ngày 08/10, nghi phạm có vũ khí', N'NGUY_HIEM', N'Quản Trị Viên', 1);

-- 6.10. Đặt chỗ trước (Booking)
INSERT INTO dbo.dat_cho_truoc (ma_dat_cho, ho_ten, so_dien_thoai, bien_so_xe, ma_khu_vuc, thoi_gian_dat, thoi_gian_het_han, tien_coc, da_thanh_toan_coc, ma_giao_dich_coc, trang_thai, khu_vuc_id, phuong_tien_id) VALUES
('BK-261010-001', N'Hoàng Minh', '0911223344', '51A-77788', '4B', DATEADD(minute, -30, GETDATE()), DATEADD(minute, 90, GETDATE()), 20000, 1, 'MOMOBK77788', 'CHO_DEN', 2, NULL),
('BK-261010-002', N'Trần Quốc Bảo (VIP)', '0988776655', '30E-666.88', '4B', DATEADD(minute, -15, GETDATE()), DATEADD(minute, 105, GETDATE()), 20000, 1, 'MOMOBK66688', 'CHO_DEN', 2, NULL);

-- 6.11. Đánh giá chất lượng
INSERT INTO dbo.danh_gia (ho_ten, so_dien_thoai, so_sao, noi_dung, loai_dich_vu, khach_hang_id, nguoi_dung_id) VALUES
(N'Nguyễn Văn An', '0912345678', 5, N'Hệ thống nhận diện biển số OCR và mở barie cực nhanh, không lo ùn tắc giờ cao điểm.', 'TOC_DO', 1, 3),
(N'Khách gửi xe', '0988776655', 5, N'Có camera và hệ thống cảnh báo xe tồn bãi nên gửi xe ở đây rất yên tâm.', 'AN_NINH', 2, 4),
(N'Lê Hoàng Long', '0903112233', 5, N'Khu C có mái che rộng rãi, trạm sạc cho xe điện rất tiện lợi.', 'CHUNG', 3, NULL);

-- 6.12. Vé xe mẫu
INSERT INTO dbo.ve_xe (maqr, bien_so_xe, bien_so_clean, loai_xe_id, ve_thang_id, vi_tri_do_id, khu_vuc_id, thoi_gian_vao, thoi_gian_ra, chi_phi, so_tien_phai_thu, so_tien_thuc_thu, hinh_thuc_thanh_toan, trang_thai_thanh_toan, trang_thai, phuong_tien_id, ca_truc_id) VALUES
('QR-DEMO-001', '29B1-88888', '29B188888', 1, 1, 1, 1, DATEADD(hour, -3, GETDATE()), NULL, NULL, NULL, NULL, 'VE_THANG', 'MIEN_PHI', N'Đang gửi', 1, 1),
('QR-DEMO-002', '30G-67890', '30G67890', 2, NULL, 101, 2, DATEADD(minute, -90, GETDATE()), NULL, NULL, NULL, NULL, 'TIEN_MAT', 'CHUA_THANH_TOAN', N'Đang gửi', 4, 1),
('QR-DEMO-003', '51H-11122', '51H11122', 2, NULL, 102, 2, DATEADD(hour, -5, GETDATE()), DATEADD(hour, -2, GETDATE()), 20000, 20000, 20000, 'CHUYEN_KHOAN', 'DA_THANH_TOAN', N'Đã ra', 5, 1),
('QR-DEMO-004', '29T1-54321', '29T154321', 1, NULL, 2, 1, DATEADD(hour, -4, GETDATE()), DATEADD(hour, -1, GETDATE()), 4000, 4000, 4000, 'TIEN_MAT', 'DA_THANH_TOAN', N'Đã ra', 6, 1);
GO

-- Bảng 14: Lịch sử sửa đổi biểu phí gửi xe (Audit Log)
IF OBJECT_ID(N'dbo.lich_su_thay_doi_gia', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.lich_su_thay_doi_gia (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        loai_xe_id BIGINT NULL,
        ten_loai NVARCHAR(100) NOT NULL,
        gia_ban_ngay_cu FLOAT NULL,
        gia_ban_ngay_moi FLOAT NOT NULL,
        gia_ban_dem_cu FLOAT NULL,
        gia_ban_dem_moi FLOAT NOT NULL,
        gia_thang_cu FLOAT NULL,
        gia_thang_moi FLOAT NOT NULL,
        nguoi_thuc_hien NVARCHAR(100) NOT NULL,
        thoi_gian_thay_doi DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        hanh_dong NVARCHAR(50) NOT NULL DEFAULT N'CẬP NHẬT',
        ghi_chu NVARCHAR(500) NULL
    );
END
GO

PRINT N'=============================================================================';
PRINT N'ĐÃ KHỞI TẠO THÀNH CÔNG TOÀN BỘ CƠ SỞ DỮ LIỆU QuanLyGuiXe VỚI 14 BẢNG!';
PRINT N'=============================================================================';
GO

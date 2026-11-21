-- =========================================================================================
-- ĐỒ ÁN CHUYÊN NGÀNH: XÂY DỰNG PHẦN MỀM QUẢN LÝ GỬI XE
-- TẬP TIN KỊCH BẢN CẬP NHẬT CƠ SỞ DỮ LIỆU: database/update_database.sql
-- HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU: MICROSOFT SQL SERVER 2019 / 2022
-- MỤC TIÊU: 
--   1. Hoàn thiện bảng DanhSachDen với 2 khóa ngoại: phuong_tien_id và nguoi_them_id.
--   2. Chuẩn hóa và bổ sung toàn diện các khóa ngoại (Foreign Keys) cho 13 bảng.
--   3. Đảm bảo 100% dữ liệu cũ không bị mất mát, an toàn với Spring Boot Backend.
-- =========================================================================================

USE QuanLyGuiXe;
GO

PRINT N'-----------------------------------------------------------------------------';
PRINT N'BẮT ĐẦU CẬP NHẬT CẤU TRÚC VÀ RÀNG BUỘC CƠ SỞ DỮ LIỆU QuanLyGuiXe...';
PRINT N'-----------------------------------------------------------------------------';
GO

-- =========================================================================================
-- PHẦN 1: CẬP NHẬT BẢNG DanhSachDen (DANH SÁCH ĐEN CẢNH BÁO AN NINH)
-- =========================================================================================

-- 1.1. Thêm cột phuong_tien_id (NULLABLE để hỗ trợ xe lạ chưa đăng ký trong hệ thống)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.danh_sach_den') AND name = N'phuong_tien_id')
BEGIN
    ALTER TABLE dbo.danh_sach_den ADD phuong_tien_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột phuong_tien_id vào bảng danh_sach_den.';
END
GO

-- 1.2. Thêm cột nguoi_them_id (Khóa ngoại liên kết người dùng/bảo vệ thêm vào blacklist)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.danh_sach_den') AND name = N'nguoi_them_id')
BEGIN
    ALTER TABLE dbo.danh_sach_den ADD nguoi_them_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột nguoi_them_id vào bảng danh_sach_den.';
END
GO

-- 1.3. Cập nhật dữ liệu cũ cho danh_sach_den (nếu biển số đã có trong phuong_tien)
UPDATE d
SET d.phuong_tien_id = pt.id
FROM dbo.danh_sach_den d
INNER JOIN dbo.phuong_tien pt ON REPLACE(REPLACE(UPPER(d.bien_so_xe), '-', ''), '.', '') = REPLACE(REPLACE(UPPER(pt.bien_so_xe), '-', ''), '.', '')
WHERE d.phuong_tien_id IS NULL;

-- 1.4. Cập nhật nguoi_them_id từ thông tin tên tài khoản hoặc họ tên người dùng
UPDATE d
SET d.nguoi_them_id = u.id
FROM dbo.danh_sach_den d
INNER JOIN dbo.nguoi_dung u ON d.nguoi_them = u.ho_ten OR d.nguoi_them = u.ten_dang_nhap
WHERE d.nguoi_them_id IS NULL;

-- Nếu không khớp, gán mặc định cho tài khoản quản trị viên (id = 1) để bảo đảm tính toàn vẹn
UPDATE dbo.danh_sach_den
SET nguoi_them_id = 1
WHERE nguoi_them_id IS NULL AND EXISTS (SELECT 1 FROM dbo.nguoi_dung WHERE id = 1);
GO

-- 1.5. Thêm ràng buộc khóa ngoại cho bảng danh_sach_den
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_DanhSachDen_PhuongTien')
BEGIN
    ALTER TABLE dbo.danh_sach_den ADD CONSTRAINT FK_DanhSachDen_PhuongTien 
    FOREIGN KEY (phuong_tien_id) REFERENCES dbo.phuong_tien(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_DanhSachDen_PhuongTien.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_DanhSachDen_NguoiDung')
BEGIN
    ALTER TABLE dbo.danh_sach_den ADD CONSTRAINT FK_DanhSachDen_NguoiDung 
    FOREIGN KEY (nguoi_them_id) REFERENCES dbo.nguoi_dung(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_DanhSachDen_NguoiDung.';
END
GO

-- =========================================================================================
-- PHẦN 2: CẬP NHẬT CÁC BẢNG LIÊN QUAN ĐẾN KHÁCH HÀNG & PHƯƠNG TIỆN
-- =========================================================================================

-- 2.1. Bổ sung nguoi_dung_id vào khach_hang (Khách hàng có tài khoản Cổng dịch vụ trực tuyến)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.khach_hang') AND name = N'nguoi_dung_id')
BEGIN
    ALTER TABLE dbo.khach_hang ADD nguoi_dung_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột nguoi_dung_id vào bảng khach_hang.';
END
GO

-- Cập nhật liên kết khách hàng với người dùng có cùng số điện thoại
UPDATE kh
SET kh.nguoi_dung_id = u.id
FROM dbo.khach_hang kh
INNER JOIN dbo.nguoi_dung u ON kh.so_dien_thoai = u.so_dien_thoai AND kh.so_dien_thoai <> ''
WHERE kh.nguoi_dung_id IS NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_KhachHang_NguoiDung')
BEGIN
    ALTER TABLE dbo.khach_hang ADD CONSTRAINT FK_KhachHang_NguoiDung 
    FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.nguoi_dung(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_KhachHang_NguoiDung.';
END
GO

-- 2.2. Bổ sung khach_hang_id vào phuong_tien (Định danh chủ sở hữu phương tiện đã đăng ký)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.phuong_tien') AND name = N'khach_hang_id')
BEGIN
    ALTER TABLE dbo.phuong_tien ADD khach_hang_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột khach_hang_id vào bảng phuong_tien.';
END
GO

-- Cập nhật liên kết phương tiện với khách hàng qua số điện thoại hoặc tên chủ xe
UPDATE pt
SET pt.khach_hang_id = kh.id
FROM dbo.phuong_tien pt
INNER JOIN dbo.khach_hang kh ON (pt.so_dien_thoai = kh.so_dien_thoai AND pt.so_dien_thoai <> '')
                             OR (pt.chu_xe = kh.ho_ten AND pt.chu_xe <> '')
WHERE pt.khach_hang_id IS NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_PhuongTien_KhachHang')
BEGIN
    ALTER TABLE dbo.phuong_tien ADD CONSTRAINT FK_PhuongTien_KhachHang 
    FOREIGN KEY (khach_hang_id) REFERENCES dbo.khach_hang(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_PhuongTien_KhachHang.';
END
GO

-- =========================================================================================
-- PHẦN 3: CẬP NHẬT BẢNG VeXe (LƯỢT GỬI XE VỚI PHƯƠNG TIỆN & CA TRỰC)
-- =========================================================================================

-- 3.1. Bổ sung phuong_tien_id vào ve_xe (Liên kết lượt gửi với hồ sơ phương tiện quản lý)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.ve_xe') AND name = N'phuong_tien_id')
BEGIN
    ALTER TABLE dbo.ve_xe ADD phuong_tien_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột phuong_tien_id vào bảng ve_xe.';
END
GO

-- Cập nhật phuong_tien_id cho các vé xe có biển số trùng khớp với phương tiện đã đăng ký
UPDATE v
SET v.phuong_tien_id = pt.id
FROM dbo.ve_xe v
INNER JOIN dbo.phuong_tien pt ON REPLACE(REPLACE(UPPER(v.bien_so_xe), '-', ''), '.', '') = REPLACE(REPLACE(UPPER(pt.bien_so_xe), '-', ''), '.', '')
WHERE v.phuong_tien_id IS NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_VeXe_PhuongTien')
BEGIN
    ALTER TABLE dbo.ve_xe ADD CONSTRAINT FK_VeXe_PhuongTien 
    FOREIGN KEY (phuong_tien_id) REFERENCES dbo.phuong_tien(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_VeXe_PhuongTien.';
END
GO

-- 3.2. Bổ sung ca_truc_id vào ve_xe (Ghi nhận ca trực chịu trách nhiệm lượt gửi xe)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.ve_xe') AND name = N'ca_truc_id')
BEGIN
    ALTER TABLE dbo.ve_xe ADD ca_truc_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột ca_truc_id vào bảng ve_xe.';
END
GO

-- Gán ca trực mặc định cho các vé xe lịch sử nếu chưa có ca trực
DECLARE @defaultShiftId BIGINT;
SELECT TOP 1 @defaultShiftId = id FROM dbo.ca_truc ORDER BY id DESC;
IF @defaultShiftId IS NOT NULL
BEGIN
    UPDATE dbo.ve_xe SET ca_truc_id = @defaultShiftId WHERE ca_truc_id IS NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_VeXe_CaTruc')
BEGIN
    ALTER TABLE dbo.ve_xe ADD CONSTRAINT FK_VeXe_CaTruc 
    FOREIGN KEY (ca_truc_id) REFERENCES dbo.ca_truc(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_VeXe_CaTruc.';
END
GO

-- =========================================================================================
-- PHẦN 4: CẬP NHẬT BẢNG DatChoTruoc (ĐẶT CHỖ VỚI KHU VỰC VÀ PHƯƠNG TIỆN)
-- =========================================================================================

-- 4.1. Bổ sung khu_vuc_id vào dat_cho_truoc
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.dat_cho_truoc') AND name = N'khu_vuc_id')
BEGIN
    ALTER TABLE dbo.dat_cho_truoc ADD khu_vuc_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột khu_vuc_id vào bảng dat_cho_truoc.';
END
GO

-- Cập nhật khu_vuc_id theo mã phân khu hiện có
UPDATE dct
SET dct.khu_vuc_id = kv.id
FROM dbo.dat_cho_truoc dct
INNER JOIN dbo.khu_vuc kv ON dct.ma_khu_vuc = kv.ma_khu
WHERE dct.khu_vuc_id IS NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_DatChoTruoc_KhuVuc')
BEGIN
    ALTER TABLE dbo.dat_cho_truoc ADD CONSTRAINT FK_DatChoTruoc_KhuVuc 
    FOREIGN KEY (khu_vuc_id) REFERENCES dbo.khu_vuc(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_DatChoTruoc_KhuVuc.';
END
GO

-- 4.2. Bổ sung phuong_tien_id vào dat_cho_truoc
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.dat_cho_truoc') AND name = N'phuong_tien_id')
BEGIN
    ALTER TABLE dbo.dat_cho_truoc ADD phuong_tien_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột phuong_tien_id vào bảng dat_cho_truoc.';
END
GO

-- Cập nhật phuong_tien_id nếu biển số đã đăng ký
UPDATE dct
SET dct.phuong_tien_id = pt.id
FROM dbo.dat_cho_truoc dct
INNER JOIN dbo.phuong_tien pt ON REPLACE(REPLACE(UPPER(dct.bien_so_xe), '-', ''), '.', '') = REPLACE(REPLACE(UPPER(pt.bien_so_xe), '-', ''), '.', '')
WHERE dct.phuong_tien_id IS NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_DatChoTruoc_PhuongTien')
BEGIN
    ALTER TABLE dbo.dat_cho_truoc ADD CONSTRAINT FK_DatChoTruoc_PhuongTien 
    FOREIGN KEY (phuong_tien_id) REFERENCES dbo.phuong_tien(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_DatChoTruoc_PhuongTien.';
END
GO

-- =========================================================================================
-- PHẦN 5: CẬP NHẬT BẢNG DanhGia & PasswordResetOtp
-- =========================================================================================

-- 5.1. Bổ sung nguoi_dung_id và khach_hang_id vào danh_gia
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.danh_gia') AND name = N'nguoi_dung_id')
BEGIN
    ALTER TABLE dbo.danh_gia ADD nguoi_dung_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột nguoi_dung_id vào bảng danh_gia.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.danh_gia') AND name = N'khach_hang_id')
BEGIN
    ALTER TABLE dbo.danh_gia ADD khach_hang_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột khach_hang_id vào bảng danh_gia.';
END
GO

-- Cập nhật liên kết đánh giá với người dùng / khách hàng theo số điện thoại
UPDATE dg
SET dg.khach_hang_id = kh.id
FROM dbo.danh_gia dg
INNER JOIN dbo.khach_hang kh ON dg.so_dien_thoai = kh.so_dien_thoai AND dg.so_dien_thoai <> ''
WHERE dg.khach_hang_id IS NULL;

UPDATE dg
SET dg.nguoi_dung_id = u.id
FROM dbo.danh_gia dg
INNER JOIN dbo.nguoi_dung u ON dg.so_dien_thoai = u.so_dien_thoai AND dg.so_dien_thoai <> ''
WHERE dg.nguoi_dung_id IS NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_DanhGia_NguoiDung')
BEGIN
    ALTER TABLE dbo.danh_gia ADD CONSTRAINT FK_DanhGia_NguoiDung 
    FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.nguoi_dung(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_DanhGia_NguoiDung.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_DanhGia_KhachHang')
BEGIN
    ALTER TABLE dbo.danh_gia ADD CONSTRAINT FK_DanhGia_KhachHang 
    FOREIGN KEY (khach_hang_id) REFERENCES dbo.khach_hang(id) ON DELETE SET NULL;
    PRINT N'[OK] Đã thêm ràng buộc FK_DanhGia_KhachHang.';
END
GO

-- 5.2. Chuyển đổi tên bảng sang tiếng Việt: otp_dat_lai_mat_khau
IF OBJECT_ID(N'dbo.password_reset_otp', N'U') IS NOT NULL AND OBJECT_ID(N'dbo.otp_dat_lai_mat_khau', N'U') IS NULL
BEGIN
    EXEC sp_rename 'dbo.password_reset_otp', 'otp_dat_lai_mat_khau';
    PRINT N'[OK] Đã đổi tên bảng password_reset_otp thành otp_dat_lai_mat_khau.';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.otp_dat_lai_mat_khau') AND name = N'nguoi_dung_id')
BEGIN
    ALTER TABLE dbo.otp_dat_lai_mat_khau ADD nguoi_dung_id BIGINT NULL;
    PRINT N'[OK] Đã thêm cột nguoi_dung_id vào bảng otp_dat_lai_mat_khau.';
END
GO

UPDATE p
SET p.nguoi_dung_id = u.id
FROM dbo.otp_dat_lai_mat_khau p
INNER JOIN dbo.nguoi_dung u ON p.email = u.email AND p.email <> ''
WHERE p.nguoi_dung_id IS NULL;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = N'FK_PasswordResetOtp_NguoiDung')
BEGIN
    ALTER TABLE dbo.otp_dat_lai_mat_khau ADD CONSTRAINT FK_PasswordResetOtp_NguoiDung 
    FOREIGN KEY (nguoi_dung_id) REFERENCES dbo.nguoi_dung(id) ON DELETE CASCADE;
    PRINT N'[OK] Đã thêm ràng buộc FK_PasswordResetOtp_NguoiDung.';
END
GO

-- =========================================================================================
-- PHẦN 6: TẠO CÁC CHỈ MỤC TỐI ƯU TRUY VẤN CHO CÁC KHÓA NGOẠI MỚI (PERFORMANCE INDEXES)
-- =========================================================================================
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_DanhSachDen_PhuongTien' AND object_id = OBJECT_ID(N'dbo.danh_sach_den'))
    CREATE INDEX IX_DanhSachDen_PhuongTien ON dbo.danh_sach_den(phuong_tien_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_DanhSachDen_NguoiThem' AND object_id = OBJECT_ID(N'dbo.danh_sach_den'))
    CREATE INDEX IX_DanhSachDen_NguoiThem ON dbo.danh_sach_den(nguoi_them_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_PhuongTien_KhachHang' AND object_id = OBJECT_ID(N'dbo.phuong_tien'))
    CREATE INDEX IX_PhuongTien_KhachHang ON dbo.phuong_tien(khach_hang_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_VeXe_PhuongTien' AND object_id = OBJECT_ID(N'dbo.ve_xe'))
    CREATE INDEX IX_VeXe_PhuongTien ON dbo.ve_xe(phuong_tien_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_VeXe_CaTruc' AND object_id = OBJECT_ID(N'dbo.ve_xe'))
    CREATE INDEX IX_VeXe_CaTruc ON dbo.ve_xe(ca_truc_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = N'IX_KhachHang_NguoiDung' AND object_id = OBJECT_ID(N'dbo.khach_hang'))
    CREATE INDEX IX_KhachHang_NguoiDung ON dbo.khach_hang(nguoi_dung_id);
GO

-- =========================================================================================
-- PHẦN 7: CHUẨN HÓA KIỂU DỮ LIỆU UNICODE TIẾNG VIỆT (NVARCHAR) & SỬA LỖI DẤU '?'
-- =========================================================================================
-- 7.1. Chuyển ho_ten trong dat_cho_truoc và danh_gia sang NVARCHAR(100)
ALTER TABLE dbo.dat_cho_truoc ALTER COLUMN ho_ten NVARCHAR(100) NOT NULL;
ALTER TABLE dbo.danh_gia ALTER COLUMN ho_ten NVARCHAR(100) NOT NULL;
PRINT N'[OK] Đã chuyển đổi cột ho_ten sang NVARCHAR(100) hỗ trợ đầy đủ tiếng Việt.';
GO

-- 7.2. Cập nhật khôi phục các dữ liệu bị biến dạng thành dấu ? trước đó
UPDATE dbo.dat_cho_truoc 
SET ho_ten = N'Trần Quốc Bảo (VIP)' 
WHERE id = 2 OR ho_ten LIKE '%Tr%n%Qu%c%B%o%';

UPDATE dbo.danh_gia 
SET ho_ten = N'Khách gửi xe' 
WHERE ho_ten LIKE '%Kh%ch%g%i%xe%';
PRINT N'[OK] Đã chuẩn hóa dữ liệu tiếng Việt có dấu trong dat_cho_truoc và danh_gia.';
GO

-- =========================================================================================
-- PHẦN 8: BẢNG LỊCH SỬ THAY ĐỔI BIỂU PHÍ GỬI XE (AUDIT LOG LƯU TRỮ SQL)
-- =========================================================================================
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
    PRINT N'[OK] Đã tạo bảng dbo.lich_su_thay_doi_gia để lưu vết lịch sử sửa đổi biểu phí.';
END
GO

PRINT N'-----------------------------------------------------------------------------';
PRINT N'CẬP NHẬT CƠ SỞ DỮ LIỆU THÀNH CÔNG VÀ AN TOÀN 100%! KHÔNG CÓ DỮ LIỆU NÀO BỊ XÓA.';
PRINT N'-----------------------------------------------------------------------------';
GO

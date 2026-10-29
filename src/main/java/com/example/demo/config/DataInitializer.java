package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final LoaiXeRepository loaiXeRepository;
    private final KhuVucRepository khuVucRepository;
    private final KhachHangRepository khachHangRepository;
    private final VeThangRepository veThangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final VeXeRepository veXeRepository;
    private final DanhSachDenRepository danhSachDenRepository;
    private final PhuongTienRepository phuongTienRepository;

    public DataInitializer(
            LoaiXeRepository loaiXeRepository,
            KhuVucRepository khuVucRepository,
            KhachHangRepository khachHangRepository,
            VeThangRepository veThangRepository,
            NguoiDungRepository nguoiDungRepository,
            VeXeRepository veXeRepository,
            DanhSachDenRepository danhSachDenRepository,
            PhuongTienRepository phuongTienRepository
    ) {
        this.loaiXeRepository = loaiXeRepository;
        this.khuVucRepository = khuVucRepository;
        this.khachHangRepository = khachHangRepository;
        this.veThangRepository = veThangRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.veXeRepository = veXeRepository;
        this.danhSachDenRepository = danhSachDenRepository;
        this.phuongTienRepository = phuongTienRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Khởi tạo Loại xe
        List<LoaiXe> allLoaiXe = loaiXeRepository.findAll();
        if (allLoaiXe.isEmpty()) {
            LoaiXe xeMay = new LoaiXe("Xe máy", 4000.0, 6000.0, 100000.0, "fa-motorcycle", "Xe số, xe tay ga");
            LoaiXe oTo = new LoaiXe("Ô tô (4-7 chỗ)", 20000.0, 35000.0, 1200000.0, "fa-car", "Xe ô tô con dưới 9 chỗ");
            LoaiXe xeDapDien = new LoaiXe("Xe đạp / Xe điện", 2000.0, 3000.0, 50000.0, "fa-bicycle", "Xe đạp điện, xe máy điện");
            LoaiXe xeTaiNho = new LoaiXe("Xe tải nhỏ / Bán tải", 30000.0, 50000.0, 1800000.0, "fa-truck", "Xe bán tải, tải nhỏ dưới 1.5 tấn");

            allLoaiXe = loaiXeRepository.saveAll(List.of(xeMay, oTo, xeDapDien, xeTaiNho));
        }

        LoaiXe xeMay = allLoaiXe.get(0);
        LoaiXe oTo = allLoaiXe.size() > 1 ? allLoaiXe.get(1) : xeMay;
        LoaiXe xeDap = allLoaiXe.size() > 2 ? allLoaiXe.get(2) : xeMay;

        // 2. Khởi tạo Khu vực đỗ xe
        if (khuVucRepository.count() == 0) {
            KhuVuc khuA = new KhuVuc("A", "Khu A - Đỗ ô tô", oTo, 10, "Tầng 1 sảnh chính");
            KhuVuc khuB = new KhuVuc("B", "Khu B - Đỗ xe máy", xeMay, 20, "Tầng hầm B1");
            KhuVuc khuC = new KhuVuc("C", "Khu C - Xe điện và Xe đạp", xeDap, 10, "Sân phụ có mái che");

            khuVucRepository.saveAll(List.of(khuA, khuB, khuC));
        }

        // 3. Khởi tạo Khách hàng và Vé tháng
        if (veThangRepository.count() == 0) {
            List<KhachHang> savedKh = khachHangRepository.findAll();
            if (savedKh.isEmpty()) {
                KhachHang kh1 = new KhachHang("Nguyễn Văn An", "0912345678", "001200001234", "an.nv@gmail.com", "Căn hộ 1205 Tòa A");
                KhachHang kh2 = new KhachHang("Trần Thị Bích", "0988776655", "001200005678", "bich.tt@gmail.com", "Căn hộ 802 Tòa B");
                KhachHang kh3 = new KhachHang("Lê Hoàng Long", "0903112233", "001200009999", "long.lh@gmail.com", "Phòng Kế toán P.301");
                savedKh = khachHangRepository.saveAll(List.of(kh1, kh2, kh3));
            }

            VeThang vt1 = new VeThang("VT-88888888", savedKh.get(0), "29B1-88888", xeMay, LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(5), 600000.0, "HOAT_DONG");
            VeThang vt2 = new VeThang("VT-99999999", savedKh.get(1), "30E-99999", oTo, LocalDate.now().minusDays(15), LocalDate.now().plusMonths(2), 2400000.0, "HOAT_DONG");
            VeThang vt3 = new VeThang("VT-12345678", savedKh.get(2), "59A1-12345", xeMay, LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(1), 300000.0, "HOAT_DONG");
            veThangRepository.saveAll(List.of(vt1, vt2, vt3));
        }

        // 4. Khởi tạo Tài khoản
        if (nguoiDungRepository.count() == 0) {
            NguoiDung admin = new NguoiDung("admin", "admin123", "Quản Trị Viên", "ADMIN", "0909000111");
            NguoiDung baove = new NguoiDung("baove", "123456", "Nhân Viên Trực Cổng", "NHAN_VIEN", "0909000222");
            nguoiDungRepository.saveAll(List.of(admin, baove));
        }

        // 5. Khởi tạo một số dữ liệu vé xe mẫu nếu chưa có dữ liệu xe
        if (veXeRepository.count() == 0) {
            // Xe vé tháng đang gửi
            VeThang vt1 = veThangRepository.findFirstByBienSoXe("29B1-88888").orElse(null);
            VeXe vx1 = new VeXe("QR-DEMO-001", "29B1-88888", xeMay, LocalDateTime.now().minusHours(3), "Đang gửi");
            vx1.setVeThang(vt1);
            vx1.setHinhThucThanhToan("VE_THANG");

            // Xe khách vãng lai đang gửi
            VeXe vx2 = new VeXe("QR-DEMO-002", "30G-67890", oTo, LocalDateTime.now().minusHours(1).minusMinutes(30), "Đang gửi");

            // Vài xe đã ra có doanh thu
            VeXe vx3 = new VeXe("QR-DEMO-003", "51H-11122", oTo, LocalDateTime.now().minusHours(5), "Đã ra");
            vx3.setThoiGianRa(LocalDateTime.now().minusHours(2));
            vx3.setChiPhi(20000.0);
            vx3.setHinhThucThanhToan("CHUYEN_KHOAN");

            VeXe vx4 = new VeXe("QR-DEMO-004", "29T1-54321", xeMay, LocalDateTime.now().minusHours(4), "Đã ra");
            vx4.setThoiGianRa(LocalDateTime.now().minusHours(1));
            vx4.setChiPhi(4000.0);
            vx4.setHinhThucThanhToan("TIEN_MAT");

            // Xe đỗ quá 24h để demo tính năng Cảnh báo an ninh xe tồn bãi
            VeXe vx5 = new VeXe("QR-DEMO-005", "43B1-99881", xeMay, LocalDateTime.now().minusHours(32), "Đang gửi");

            veXeRepository.saveAll(List.of(vx1, vx2, vx3, vx4, vx5));
        }

        // 6. Khởi tạo Danh sách đen mẫu
        if (danhSachDenRepository.count() == 0) {
            DanhSachDen bl = new DanhSachDen("59X3-99999", "Xe báo mất trộm trong khu đô thị ngày 08/10", "NGUY_HIEM", "Công an khu vực");
            danhSachDenRepository.save(bl);
        }

        // 7. Khởi tạo Danh mục phương tiện quản lý (PhuongTien)
        if (phuongTienRepository.count() == 0) {
            PhuongTien pt1 = new PhuongTien("29B1-88888", xeMay, "Nguyễn Văn An", "0912345678", "Xe máy Honda SH - Cư dân Tòa A");
            PhuongTien pt2 = new PhuongTien("30E-99999", oTo, "Trần Thị Bích", "0988776655", "Toyota Camry - Cư dân Tòa B");
            PhuongTien pt3 = new PhuongTien("59A1-12345", xeMay, "Lê Hoàng Long", "0903112233", "Honda Air Blade - Cư dân Tòa C");
            PhuongTien pt4 = new PhuongTien("30G-67890", oTo, "Phạm Quốc Huy", "0934567890", "Mazda 3 - Khách vãng lai");
            PhuongTien pt5 = new PhuongTien("51H-11122", oTo, "Đỗ Minh Tuấn", "0945678901", "Hyundai Tucson - Khách vãng lai");
            PhuongTien pt6 = new PhuongTien("29T1-54321", xeMay, "Vũ Thùy Linh", "0956789012", "Yamaha Grande - Khách vãng lai");
            PhuongTien pt7 = new PhuongTien("43B1-99881", xeMay, "Hoàng Văn Nam", "0967890123", "Honda Wave Alpha");
            phuongTienRepository.saveAll(List.of(pt1, pt2, pt3, pt4, pt5, pt6, pt7));
        }
    }
}

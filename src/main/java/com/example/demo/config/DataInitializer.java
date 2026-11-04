package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.util.BienSoXeHelper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile({"dev", "demo", "default"})
public class DataInitializer implements CommandLineRunner {

    private final LoaiXeRepository loaiXeRepository;
    private final KhuVucRepository khuVucRepository;
    private final KhachHangRepository khachHangRepository;
    private final VeThangRepository veThangRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final VeXeRepository veXeRepository;
    private final DanhSachDenRepository danhSachDenRepository;
    private final PhuongTienRepository phuongTienRepository;
    private final ViTriDoRepository viTriDoRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            LoaiXeRepository loaiXeRepository,
            KhuVucRepository khuVucRepository,
            KhachHangRepository khachHangRepository,
            VeThangRepository veThangRepository,
            NguoiDungRepository nguoiDungRepository,
            VeXeRepository veXeRepository,
            DanhSachDenRepository danhSachDenRepository,
            PhuongTienRepository phuongTienRepository,
            ViTriDoRepository viTriDoRepository,
            DanhGiaRepository danhGiaRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.loaiXeRepository = loaiXeRepository;
        this.khuVucRepository = khuVucRepository;
        this.khachHangRepository = khachHangRepository;
        this.veThangRepository = veThangRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.veXeRepository = veXeRepository;
        this.danhSachDenRepository = danhSachDenRepository;
        this.phuongTienRepository = phuongTienRepository;
        this.viTriDoRepository = viTriDoRepository;
        this.danhGiaRepository = danhGiaRepository;
        this.passwordEncoder = passwordEncoder;
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
        // 2. Khởi tạo 3 Phân khu bãi đỗ xe chuẩn theo quy hoạch vật lý
        KhuVuc kv2B = khuVucRepository.findByMaKhu("2B").orElseGet(() -> khuVucRepository.save(new KhuVuc("2B", "Khu Đỗ Xe 2 Bánh", null, 100, "Tầng hầm B1 & B2")));
        KhuVuc kv4B = khuVucRepository.findByMaKhu("4B").orElseGet(() -> khuVucRepository.save(new KhuVuc("4B", "Khu Đỗ Xe 4 Bánh", null, 20, "Tầng 1 sảnh ngoài trời")));
        KhuVuc kvXD = khuVucRepository.findByMaKhu("XD").orElseGet(() -> khuVucRepository.save(new KhuVuc("XD", "Khu Dành Cho Xe Đạp / Xe Điện", null, 30, "Sân phụ có mái che")));

        // Loại bỏ phân khu giả định 24H nếu có trong CSDL (vì 24H là cảnh báo theo thời gian, không phải phân khu vật lý)
        khuVucRepository.findByMaKhu("24H").ifPresent(khuVucRepository::delete);

        // Đảm bảo sức chứa chuẩn
        if (kv2B.getSucChua() == null || kv2B.getSucChua() != 100) { kv2B.setSucChua(100); khuVucRepository.save(kv2B); }
        if (kv4B.getSucChua() == null || kv4B.getSucChua() != 20) { kv4B.setSucChua(20); khuVucRepository.save(kv4B); }
        if (kvXD.getSucChua() == null || kvXD.getSucChua() != 30) { kvXD.setSucChua(30); khuVucRepository.save(kvXD); }

        // Gán cấu hình phân khu rõ ràng trong CSDL cho từng Loại xe:
        // - Xe máy -> 2B (Khu xe 2 bánh)
        // - Xe đạp / Xe điện -> XD (Khu xe đạp / xe điện)
        // - Ô tô (4-7 chỗ) -> 4B (Khu ô tô)
        // - Xe tải nhỏ / Bán tải -> 4B (Khu ô tô)
        for (LoaiXe lx : loaiXeRepository.findAll()) {
            String lower = lx.getTenLoai().toLowerCase();
            if (lower.contains("máy") || lower.contains("motor")) {
                lx.setKhuVuc(kv2B);
            } else if (lower.contains("đạp") || lower.contains("điện")) {
                lx.setKhuVuc(kvXD);
            } else if (lower.contains("ô tô") || lower.contains("tải")) {
                lx.setKhuVuc(kv4B);
            }
            loaiXeRepository.save(lx);
        }

        // Khởi tạo các vị trí ô đỗ cụ thể trong DB nếu chưa có
        if (viTriDoRepository.count() == 0 && kv2B != null && kv4B != null && kvXD != null) {
            List<ViTriDo> initialSlots = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                initialSlots.add(new ViTriDo(String.format("2B-%02d", i), kv2B, "TRONG"));
            }
            for (int i = 1; i <= 20; i++) {
                initialSlots.add(new ViTriDo(String.format("4B-%02d", i), kv4B, "TRONG"));
            }
            for (int i = 1; i <= 30; i++) {
                initialSlots.add(new ViTriDo(String.format("XD-%02d", i), kvXD, "TRONG"));
            }
            viTriDoRepository.saveAll(initialSlots);
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

        // 4. Khởi tạo Tài khoản với mật khẩu mã hóa BCrypt
        if (nguoiDungRepository.count() == 0) {
            NguoiDung admin = new NguoiDung("admin", passwordEncoder.encode("admin123"), "Quản Trị Viên", "ADMIN", "0909000111");
            NguoiDung baove = new NguoiDung("baove", passwordEncoder.encode("123456"), "Nhân Viên Trực Cổng", "NHAN_VIEN", "0909000222");
            NguoiDung khach = new NguoiDung("khachhang", passwordEncoder.encode("123456"), "Nguyễn Văn An", "KHACH_HANG", "0912345678");
            nguoiDungRepository.saveAll(List.of(admin, baove, khach));
        } else {
            // Nâng cấp mật khẩu cũ sang BCrypt nếu phát hiện dạng plain text
            List<NguoiDung> users = nguoiDungRepository.findAll();
            for (NguoiDung u : users) {
                if (u.getMatKhau() != null && !u.getMatKhau().startsWith("$2a$") && !u.getMatKhau().startsWith("$2b$")) {
                    u.setMatKhau(passwordEncoder.encode(u.getMatKhau()));
                    nguoiDungRepository.save(u);
                }
            }
        }

        // 5. Chuẩn hóa biển số sạch (bienSoClean) cho các vé xe hiện có
        List<VeXe> existingTickets = veXeRepository.findAll();
        for (VeXe vx : existingTickets) {
            boolean updated = false;
            if (vx.getBienSoClean() == null && vx.getBienSoXe() != null) {
                vx.setBienSoClean(BienSoXeHelper.clean(vx.getBienSoXe()));
                updated = true;
            }
            if (updated) {
                veXeRepository.save(vx);
            }
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

        // 8. Khởi tạo Đánh giá mẫu từ khách hàng
        if (danhGiaRepository.count() == 0) {
            DanhGia dg1 = new DanhGia("Nguyễn Văn An", "0912345678", 5, "Bãi xe hiện đại, nhận diện biển số rất nhanh và bảo vệ thân thiện.", "TOC_DO");
            DanhGia dg2 = new DanhGia("Trần Thị Bích", "0988776655", 5, "Hầm gửi xe sạch sẽ, an ninh tốt, camera đối soát biển số rõ nét.", "AN_NINH");
            DanhGia dg3 = new DanhGia("Lê Hoàng Long", "0903112233", 4, "Giá vé hợp lý, thủ tục thanh toán chuyển khoản QR nhanh gọn.", "GIA_CA");
            DanhGia dg4 = new DanhGia("Phạm Minh Đức", "0977112233", 3, "Giờ cao điểm chiều làn ra hơi đông, nên mở thêm luồng ra linh hoạt.", "TOC_DO");
            danhGiaRepository.saveAll(List.of(dg1, dg2, dg3, dg4));
        }
    }
}

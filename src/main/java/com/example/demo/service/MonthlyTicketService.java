package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.model.KhachHang;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.NguoiDung;
import com.example.demo.model.VeThang;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VeThangRepository;
import com.example.demo.util.BienSoXeHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("veThangService")
public class MonthlyTicketService implements VeThangService {

    private final VeThangRepository veThangRepository;
    private final KhachHangRepository khachHangRepository;
    private final LoaiXeRepository loaiXeRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;

    public MonthlyTicketService(VeThangRepository veThangRepository,
                                KhachHangRepository khachHangRepository,
                                LoaiXeRepository loaiXeRepository,
                                NguoiDungRepository nguoiDungRepository,
                                PasswordEncoder passwordEncoder) {
        this.veThangRepository = veThangRepository;
        this.khachHangRepository = khachHangRepository;
        this.loaiXeRepository = loaiXeRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<VeThang> layTatCaVeThang() {
        return veThangRepository.findAll();
    }

    public List<VeThang> getAllVeThang() {
        return layTatCaVeThang();
    }

    @Override
    public List<KhachHang> layTatCaKhachHang() {
        return khachHangRepository.findAll();
    }

    public List<KhachHang> getAllKhachHang() {
        return layTatCaKhachHang();
    }

    @Override
    public List<VeThang> timKiemVeThang(String tuKhoa) {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return veThangRepository.findAll();
        }
        return veThangRepository.searchByKeyword(tuKhoa.trim());
    }

    public List<VeThang> searchVeThang(String keyword) {
        return timKiemVeThang(keyword);
    }

    @Override
    public Optional<VeThang> layVeThangTheoId(Long id) {
        return veThangRepository.findById(id);
    }

    public Optional<VeThang> getVeThangById(Long id) {
        return layVeThangTheoId(id);
    }

    @Override
    public Optional<VeThang> timTheoMaThe(String maThe) {
        if (maThe == null || maThe.isBlank()) return Optional.empty();
        return veThangRepository.findByMaThe(maThe.trim());
    }

    @Override
    public Optional<VeThang> timVeThangConHan(String bienSoXe) {
        return timVeThangConHan(bienSoXe, null, LocalDate.now());
    }

    @Override
    public Optional<VeThang> timVeThangConHan(String bienSoXe, Long loaiXeId) {
        return timVeThangConHan(bienSoXe, loaiXeId, LocalDate.now());
    }

    @Override
    public Optional<VeThang> timVeThangConHan(String bienSoXe, Long loaiXeId, LocalDate targetDate) {
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) return Optional.empty();
        LocalDate checkDate = targetDate != null ? targetDate : LocalDate.now();
        List<VeThang> list = veThangRepository.findByTrangThai("HOAT_DONG");
        for (VeThang vt : list) {
            if (com.example.demo.util.BienSoXeHelper.isSamePlate(vt.getBienSoXe(), bienSoXe) && vt.isConHan(checkDate)) {
                if (loaiXeId == null || (vt.getLoaiXe() != null && vt.getLoaiXe().getId() != null && vt.getLoaiXe().getId().equals(loaiXeId))) {
                    return Optional.of(vt);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<VeThang> findActiveMonthlyPass(String bienSoXe) {
        return timVeThangConHan(bienSoXe, null, LocalDate.now());
    }

    public Optional<VeThang> findActiveMonthlyPass(String bienSoXe, Long loaiXeId) {
        return timVeThangConHan(bienSoXe, loaiXeId, LocalDate.now());
    }

    public Optional<VeThang> findActiveMonthlyPass(String bienSoXe, Long loaiXeId, LocalDate targetDate) {
        return timVeThangConHan(bienSoXe, loaiXeId, targetDate);
    }

    @Override
    @Transactional
    public VeThang dangKyVeThang(String hoTen, String sdt, String cccd, String email, String diaChi,
                                  String bienSoXe, Long loaiXeId, int soThang) {
        if (soThang <= 0 || soThang > 24) {
            throw new BadRequestException("Số tháng đăng ký vé tháng phải từ 1 đến 24 tháng!");
        }

        if (loaiXeId == null) {
            throw new BadRequestException("Vui lòng chọn loại phương tiện!");
        }

        LoaiXe loaiXe = loaiXeRepository.findById(loaiXeId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy loại xe ID: " + loaiXeId));

        if (loaiXe.getGiaThang() == null || loaiXe.getGiaThang() <= 0) {
            throw new BadRequestException("Loại xe [" + loaiXe.getTenLoai() + "] chưa được cấu hình giá vé tháng hợp lệ!");
        }

        BienSoXeHelper.ValidationResult vResult = 
                BienSoXeHelper.validate(bienSoXe, loaiXe.getTenLoai());
        if (!vResult.valid()) {
            throw new BadRequestException("Biển số xe không hợp lệ theo quy chuẩn Việt Nam: " + vResult.message());
        }
        String displayPlate = vResult.displayValue();

        // Chặn tạo nhiều vé tháng hoạt động trùng biển số
        LocalDate today = LocalDate.now();
        List<VeThang> activePasses = veThangRepository.findByTrangThai("HOAT_DONG");
        boolean existsActive = activePasses.stream().anyMatch(vt ->
                BienSoXeHelper.isSamePlate(vt.getBienSoXe(), displayPlate) &&
                vt.getNgayHetHan() != null && !today.isAfter(vt.getNgayHetHan())
        );
        if (existsActive) {
            throw new BadRequestException("Phương tiện biển số [" + displayPlate + "] hiện đang có vé tháng đang hoạt động và còn hạn sử dụng!");
        }

        // Tìm hoặc tạo mới khách hàng
        KhachHang khachHang = khachHangRepository.findBySoDienThoai(sdt)
                .orElseGet(() -> {
                    KhachHang newKh = new KhachHang(hoTen, sdt, cccd, email, diaChi);
                    return khachHangRepository.save(newKh);
                });

        // Cập nhật thông tin nếu cần
        khachHang.setHoTen(hoTen);
        if (cccd != null && !cccd.isBlank()) khachHang.setCccd(cccd);
        if (email != null && !email.isBlank()) khachHang.setEmail(email);
        if (diaChi != null && !diaChi.isBlank()) khachHang.setDiaChi(diaChi);
        khachHangRepository.save(khachHang);

        // Đảm bảo khách hàng luôn có tài khoản đăng nhập (tenDangNhap = sdt) với mật khẩu mã hóa BCrypt
        if (nguoiDungRepository.findByTenDangNhap(sdt).isEmpty()) {
            NguoiDung nd = new NguoiDung(sdt, passwordEncoder.encode("123456"), hoTen, "KHACH_HANG", sdt);
            nguoiDungRepository.save(nd);
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(soThang);
        // Chính sách: Giá vé = giá tháng của loại xe x số tháng. Không áp dụng giảm giá.
        double totalPrice = loaiXe.getGiaThang() * soThang;

        String maThe = "VT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        VeThang veThang = new VeThang(
                maThe,
                khachHang,
                displayPlate,
                loaiXe,
                startDate,
                endDate,
                totalPrice,
                "HOAT_DONG"
        );

        return veThangRepository.save(veThang);
    }

    @Transactional
    public VeThang registerMonthlyPass(String hoTen, String sdt, String cccd, String email, String diaChi,
                                       String bienSoXe, Long loaiXeId, int months) {
        return dangKyVeThang(hoTen, sdt, cccd, email, diaChi, bienSoXe, loaiXeId, months);
    }

    /**
     * Nghiệp vụ: Đăng ký vé tháng trực tuyến.
     * Vé online chưa xác nhận thanh toán ở trạng thái CHO_THANH_TOAN và không được dùng để miễn phí check-in.
     * Ngày bắt đầu và hết hạn chỉ được tính từ thời điểm nhân viên xác nhận đã nhận tiền.
     */
    @Transactional
    public VeThang dangKyVeThangOnline(String hoTen, String sdt, String cccd, String email, String diaChi,
                                       String bienSoXe, Long loaiXeId, int soThang) {
        VeThang vt = dangKyVeThang(hoTen, sdt, cccd, email, diaChi, bienSoXe, loaiXeId, soThang);
        vt.setTrangThai("CHO_THANH_TOAN");
        return veThangRepository.save(vt);
    }

    /**
     * Xác nhận đã nhận tiền vé tháng (Dành cho tài khoản có quyền: Admin / Nhân viên quản lý).
     * Chuyển vé sang HOAT_DONG; ngày bắt đầu và hết hạn được tính từ thời điểm xác nhận này.
     */
    @Transactional
    public VeThang xacNhanThanhToanVeThang(Long veThangId, String nguoiXacNhan) {
        VeThang vt = veThangRepository.findById(veThangId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy vé tháng ID: " + veThangId));

        if ("HOAT_DONG".equalsIgnoreCase(vt.getTrangThai()) && vt.isConHan()) {
            throw new BadRequestException("Vé tháng [" + vt.getMaThe() + "] đã ở trạng thái HOẠT ĐỘNG!");
        }

        LoaiXe lx = vt.getLoaiXe();
        int soThang = 1;
        if (lx != null && lx.getGiaThang() != null && lx.getGiaThang() > 0 && vt.getGiaTien() != null) {
            soThang = (int) Math.max(1, Math.round(vt.getGiaTien() / lx.getGiaThang()));
        }

        LocalDate activationDate = LocalDate.now();
        vt.setNgayBatDau(activationDate);
        vt.setNgayHetHan(activationDate.plusMonths(soThang));
        vt.setTrangThai("HOAT_DONG");
        return veThangRepository.save(vt);
    }

    @Override
    @Transactional
    public VeThang giaHanVeThang(Long veThangId, int soThang) {
        if (soThang <= 0 || soThang > 24) {
            throw new BadRequestException("Số tháng gia hạn phải từ 1 đến 24 tháng!");
        }

        VeThang veThang = veThangRepository.findById(veThangId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy vé tháng ID: " + veThangId));

        LoaiXe loaiXe = veThang.getLoaiXe();
        if (loaiXe == null || loaiXe.getGiaThang() == null || loaiXe.getGiaThang() <= 0) {
            throw new BadRequestException("Loại xe của vé tháng chưa được cấu hình giá vé tháng hợp lệ!");
        }

        LocalDate currentEnd = veThang.getNgayHetHan();
        LocalDate today = LocalDate.now();
        // Chính sách: Gia hạn trước khi hết hạn thì nối thời gian mới từ ngày hết hạn hiện tại.
        // Gia hạn sau khi hết hạn thì tính từ ngày gia hạn.
        boolean isExpired = (currentEnd == null || today.isAfter(currentEnd));
        LocalDate newStart = (isExpired || currentEnd == null) ? today : currentEnd;
        LocalDate newEnd = newStart.plusMonths(soThang);

        if (isExpired) {
            veThang.setNgayBatDau(newStart);
        }
        veThang.setNgayHetHan(newEnd);
        veThang.setTrangThai("HOAT_DONG");
        double renewalPrice = loaiXe.getGiaThang() * soThang;
        veThang.setGiaTien(veThang.getGiaTien() != null ? veThang.getGiaTien() + renewalPrice : renewalPrice);

        return veThangRepository.save(veThang);
    }

    @Transactional
    public VeThang renewMonthlyPass(Long veThangId, int months) {
        return giaHanVeThang(veThangId, months);
    }

    @Override
    @Transactional
    public void doiTrangThai(Long veThangId) {
        VeThang veThang = veThangRepository.findById(veThangId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé tháng ID: " + veThangId));
        if ("HOAT_DONG".equals(veThang.getTrangThai())) {
            veThang.setTrangThai("KHOA");
        } else {
            veThang.setTrangThai("HOAT_DONG");
        }
        veThangRepository.save(veThang);
    }

    @Transactional
    public void toggleStatus(Long veThangId) {
        doiTrangThai(veThangId);
    }

    @Override
    public long demVeThangHoatDong() {
        return veThangRepository.countByTrangThai("HOAT_DONG");
    }

    public long countActivePasses() {
        return demVeThangHoatDong();
    }
}

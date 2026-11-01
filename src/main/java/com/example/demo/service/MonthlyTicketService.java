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
        return timVeThangConHan(bienSoXe, null);
    }

    @Override
    public Optional<VeThang> timVeThangConHan(String bienSoXe, Long loaiXeId) {
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) return Optional.empty();
        List<VeThang> list = veThangRepository.findByTrangThai("HOAT_DONG");
        for (VeThang vt : list) {
            if (com.example.demo.util.BienSoXeHelper.isSamePlate(vt.getBienSoXe(), bienSoXe) && vt.isConHan()) {
                if (loaiXeId == null || (vt.getLoaiXe() != null && vt.getLoaiXe().getId() != null && vt.getLoaiXe().getId().equals(loaiXeId))) {
                    return Optional.of(vt);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<VeThang> findActiveMonthlyPass(String bienSoXe) {
        return timVeThangConHan(bienSoXe, null);
    }

    public Optional<VeThang> findActiveMonthlyPass(String bienSoXe, Long loaiXeId) {
        return timVeThangConHan(bienSoXe, loaiXeId);
    }

    @Override
    @Transactional
    public VeThang dangKyVeThang(String hoTen, String sdt, String cccd, String email, String diaChi,
                                  String bienSoXe, Long loaiXeId, int soThang) {
        if (soThang <= 0 || soThang > 24) {
            throw new BadRequestException("Số tháng đăng ký vé tháng phải từ 1 đến 24 tháng!");
        }

        LoaiXe loaiXe = loaiXeRepository.findById(loaiXeId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy loại xe ID: " + loaiXeId));

        BienSoXeHelper.ValidationResult vResult = 
                BienSoXeHelper.validate(bienSoXe, loaiXe.getTenLoai());
        if (!vResult.valid()) {
            throw new BadRequestException("Biển số xe không hợp lệ theo quy chuẩn Việt Nam: " + vResult.message());
        }
        String displayPlate = vResult.displayValue();
        String cleanPlate = BienSoXeHelper.clean(displayPlate);

        // Chặn tạo nhiều vé tháng hoạt động trùng biển số
        long activeCount = veThangRepository.countActiveByBienSoXe(cleanPlate, LocalDate.now());
        if (activeCount > 0) {
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
        khachHang.setCccd(cccd);
        khachHang.setEmail(email);
        khachHang.setDiaChi(diaChi);
        khachHangRepository.save(khachHang);

        // Đảm bảo khách hàng luôn có tài khoản đăng nhập (tenDangNhap = sdt) với mật khẩu mã hóa BCrypt
        if (nguoiDungRepository.findByTenDangNhap(sdt).isEmpty()) {
            NguoiDung nd = new NguoiDung(sdt, passwordEncoder.encode("123456"), hoTen, "KHACH_HANG", sdt);
            nguoiDungRepository.save(nd);
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(soThang);
        double totalPrice = (loaiXe.getGiaThang() != null ? loaiXe.getGiaThang() : 100000.0) * soThang;

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

    @Override
    @Transactional
    public VeThang giaHanVeThang(Long veThangId, int soThang) {
        if (soThang <= 0 || soThang > 24) {
            throw new BadRequestException("Số tháng gia hạn phải từ 1 đến 24 tháng!");
        }

        VeThang veThang = veThangRepository.findById(veThangId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy vé tháng ID: " + veThangId));

        LocalDate currentEnd = veThang.getNgayHetHan();
        LocalDate newStart = (currentEnd != null && currentEnd.isAfter(LocalDate.now())) ? currentEnd : LocalDate.now();
        LocalDate newEnd = newStart.plusMonths(soThang);

        veThang.setNgayHetHan(newEnd);
        veThang.setTrangThai("HOAT_DONG");
        if (veThang.getLoaiXe() != null && veThang.getLoaiXe().getGiaThang() != null) {
            veThang.setGiaTien(veThang.getGiaTien() + (veThang.getLoaiXe().getGiaThang() * soThang));
        }

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

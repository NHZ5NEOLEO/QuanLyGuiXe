package com.example.demo.service;

import com.example.demo.model.KhachHang;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeThang;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeThangRepository;
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

    public MonthlyTicketService(VeThangRepository veThangRepository,
                                KhachHangRepository khachHangRepository,
                                LoaiXeRepository loaiXeRepository) {
        this.veThangRepository = veThangRepository;
        this.khachHangRepository = khachHangRepository;
        this.loaiXeRepository = loaiXeRepository;
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
    public Optional<VeThang> timVeThangConHan(String bienSoXe) {
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) return Optional.empty();
        List<VeThang> list = veThangRepository.findByTrangThai("HOAT_DONG");
        for (VeThang vt : list) {
            if (com.example.demo.util.BienSoXeHelper.isSamePlate(vt.getBienSoXe(), bienSoXe) && vt.isConHan()) {
                return Optional.of(vt);
            }
        }
        return Optional.empty();
    }

    public Optional<VeThang> findActiveMonthlyPass(String bienSoXe) {
        return timVeThangConHan(bienSoXe);
    }

    @Override
    @Transactional
    public VeThang dangKyVeThang(String hoTen, String sdt, String cccd, String email, String diaChi,
                                  String bienSoXe, Long loaiXeId, int soThang) {
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

        LoaiXe loaiXe = loaiXeRepository.findById(loaiXeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại xe ID: " + loaiXeId));

        com.example.demo.util.BienSoXeHelper.ValidationResult vResult = 
                com.example.demo.util.BienSoXeHelper.validate(bienSoXe, loaiXe.getTenLoai());
        String displayPlate = vResult.valid() ? vResult.displayValue() : bienSoXe.trim().toUpperCase();

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
        VeThang veThang = veThangRepository.findById(veThangId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé tháng ID: " + veThangId));

        LocalDate currentEnd = veThang.getNgayHetHan();
        LocalDate newStart = currentEnd.isAfter(LocalDate.now()) ? currentEnd : LocalDate.now();
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

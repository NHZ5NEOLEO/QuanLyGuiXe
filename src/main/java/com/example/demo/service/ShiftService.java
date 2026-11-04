package com.example.demo.service;

import com.example.demo.model.CaTruc;
import com.example.demo.model.NguoiDung;
import com.example.demo.model.VeXe;
import com.example.demo.repository.CaTrucRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VeXeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("caTrucService")
public class ShiftService implements CaTrucService {

    private final CaTrucRepository caTrucRepository;
    private final VeXeRepository veXeRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public ShiftService(CaTrucRepository caTrucRepository,
                        VeXeRepository veXeRepository,
                        NguoiDungRepository nguoiDungRepository) {
        this.caTrucRepository = caTrucRepository;
        this.veXeRepository = veXeRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    @Transactional
    public CaTruc layCaTrucHienTai() {
        Optional<CaTruc> currentOpt = caTrucRepository.findFirstByTrangThaiOrderByThoiGianBatDauDesc("DANG_TRUC");
        if (currentOpt.isPresent()) {
            CaTruc current = currentOpt.get();
            capNhatThongKeCaTruc(current);
            return current;
        }
        // Nghiệp vụ chuẩn: Không tự động tạo ca hoặc gán người trực khi chỉ mở trang
        return null;
    }

    /**
     * Bắt đầu ca trực mới: Gắn trực tiếp với nhân viên đang đăng nhập.
     * Ngăn chặn tạo nhiều ca DANG_TRUC cùng lúc.
     */
    @Transactional
    public CaTruc batDauCaTruc(String tenCa, String username) {
        Optional<CaTruc> currentOpt = caTrucRepository.findFirstByTrangThaiOrderByThoiGianBatDauDesc("DANG_TRUC");
        if (currentOpt.isPresent()) {
            throw new IllegalArgumentException("Hiện đã có ca trực [" + currentOpt.get().getTenCa() + "] đang hoạt động! Vui lòng chốt ca cũ trước.");
        }

        NguoiDung nguoiTruc = null;
        if (username != null && !username.isBlank()) {
            nguoiTruc = nguoiDungRepository.findByTenDangNhap(username).orElse(null);
        }
        if (nguoiTruc == null) {
            nguoiTruc = nguoiDungRepository.findAll().stream()
                    .filter(u -> !"KHACH_HANG".equalsIgnoreCase(u.getVaiTro()))
                    .findFirst().orElse(null);
        }

        String maCa = "CA-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd-HHmm"));
        String name = (tenCa != null && !tenCa.isBlank()) ? tenCa.trim() : "Ca Trực Hiện Tại";
        CaTruc newShift = new CaTruc(maCa, name, nguoiTruc, LocalDateTime.now());
        newShift.setTrangThai("DANG_TRUC");
        newShift = caTrucRepository.save(newShift);
        capNhatThongKeCaTruc(newShift);
        return newShift;
    }

    public CaTruc getCurrentShift() {
        return layCaTrucHienTai();
    }

    @Override
    public void capNhatThongKeCaTruc(CaTruc shift) {
        LocalDateTime start = shift.getThoiGianBatDau();
        LocalDateTime end = shift.getThoiGianKetThuc() != null ? shift.getThoiGianKetThuc() : LocalDateTime.now();

        long luotVao = veXeRepository.countByThoiGianVaoBetween(start, end);
        long luotRa = veXeRepository.countByThoiGianRaBetween(start, end);

        List<VeXe> exitedVehicles = veXeRepository.searchHistory(null, "Đã ra", null, start, end);
        double tienMat = 0.0;
        double chuyenKhoan = 0.0;

        for (VeXe v : exitedVehicles) {
            if (v.getThoiGianRa() != null && v.getThoiGianRa().isAfter(start) && v.getThoiGianRa().isBefore(end.plusSeconds(1))) {
                double tien = v.getChiPhi() != null ? v.getChiPhi() : 0.0;
                if ("CHUYEN_KHOAN".equalsIgnoreCase(v.getHinhThucThanhToan())) {
                    chuyenKhoan += tien;
                } else if ("TIEN_MAT".equalsIgnoreCase(v.getHinhThucThanhToan())) {
                    tienMat += tien;
                }
            }
        }

        shift.setTongLuotVao((int) luotVao);
        shift.setTongLuotRa((int) luotRa);
        shift.setTienMatThuDuoc(tienMat);
        shift.setChuyenKhoanThuDuoc(chuyenKhoan);
        shift.setTongDoanhThu(tienMat + chuyenKhoan);
        caTrucRepository.save(shift);
    }

    public void updateShiftStats(CaTruc shift) {
        capNhatThongKeCaTruc(shift);
    }

    @Override
    @Transactional
    public CaTruc banGiaoCaTruc(Long shiftId, String ghiChu) {
        return banGiaoCaTruc(shiftId, ghiChu, null);
    }

    @Transactional
    public CaTruc banGiaoCaTruc(Long shiftId, String ghiChu, Long nguoiNhanCaId) {
        CaTruc shift = caTrucRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca trực ID: " + shiftId));

        shift.setThoiGianKetThuc(LocalDateTime.now());
        shift.setTrangThai("DA_BAN_GIAO");
        shift.setGhiChuBanGiao(ghiChu);
        capNhatThongKeCaTruc(shift);

        // Nghiệp vụ: Chốt ca cũ; nếu có người nhận ca được chọn thì tạo ca mới gán đúng người nhận ca đó.
        // Tuyệt đối không tự gán lại người bàn giao làm người trực ca kế tiếp.
        if (nguoiNhanCaId != null) {
            NguoiDung nguoiNhan = nguoiDungRepository.findById(nguoiNhanCaId).orElse(null);
            if (nguoiNhan != null) {
                String maCaMoi = "CA-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd-HHmm"));
                CaTruc nextShift = new CaTruc(maCaMoi, "Ca Trực Kế Tiếp", nguoiNhan, LocalDateTime.now());
                nextShift.setTrangThai("DANG_TRUC");
                caTrucRepository.save(nextShift);
            }
        }

        return shift;
    }

    public CaTruc handoverShift(Long shiftId, String ghiChu) {
        return banGiaoCaTruc(shiftId, ghiChu, null);
    }

    public CaTruc handoverShift(Long shiftId, String ghiChu, Long nguoiNhanCaId) {
        return banGiaoCaTruc(shiftId, ghiChu, nguoiNhanCaId);
    }

    @Override
    public List<CaTruc> layCaTrucGanDay() {
        return caTrucRepository.findTop10ByOrderByThoiGianBatDauDesc();
    }

    public List<CaTruc> getRecentShifts() {
        return layCaTrucGanDay();
    }
}

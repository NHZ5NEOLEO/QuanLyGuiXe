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

        // Nếu chưa có ca trực nào mở, tự động khởi tạo Ca trực hiện tại
        NguoiDung defaultUser = nguoiDungRepository.findAll().stream().findFirst().orElse(null);
        CaTruc newShift = new CaTruc("CA-HIEN-TAI", "Ca Trực Hiện Tại", defaultUser, LocalDateTime.now().minusHours(2));
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
        CaTruc shift = caTrucRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca trực ID: " + shiftId));

        shift.setThoiGianKetThuc(LocalDateTime.now());
        shift.setTrangThai("DA_BAN_GIAO");
        shift.setGhiChuBanGiao(ghiChu);
        capNhatThongKeCaTruc(shift);

        // Mở ca mới ngay sau đó
        NguoiDung defaultUser = shift.getNguoiTruc();
        CaTruc nextShift = new CaTruc("CA-TIEP-THEO", "Ca Trực Kế Tiếp", defaultUser, LocalDateTime.now());
        caTrucRepository.save(nextShift);

        return shift;
    }

    public CaTruc handoverShift(Long shiftId, String ghiChu) {
        return banGiaoCaTruc(shiftId, ghiChu);
    }

    @Override
    public List<CaTruc> layCaTrucGanDay() {
        return caTrucRepository.findTop10ByOrderByThoiGianBatDauDesc();
    }

    public List<CaTruc> getRecentShifts() {
        return layCaTrucGanDay();
    }
}

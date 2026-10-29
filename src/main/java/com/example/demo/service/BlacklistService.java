package com.example.demo.service;

import com.example.demo.model.DanhSachDen;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DanhSachDenRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.util.BienSoXeHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("danhSachDenService")
public class BlacklistService implements DanhSachDenService {

    private final DanhSachDenRepository danhSachDenRepository;
    private final VeXeRepository veXeRepository;

    public BlacklistService(DanhSachDenRepository danhSachDenRepository, VeXeRepository veXeRepository) {
        this.danhSachDenRepository = danhSachDenRepository;
        this.veXeRepository = veXeRepository;
    }

    @Override
    public List<DanhSachDen> layTatCaDanhSachDen() {
        return danhSachDenRepository.findAll();
    }

    public List<DanhSachDen> getAllBlacklist() {
        return layTatCaDanhSachDen();
    }

    @Override
    public Optional<DanhSachDen> kiemTraDanhSachDen(String bienSoXe) {
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) {
            return Optional.empty();
        }
        List<DanhSachDen> all = danhSachDenRepository.findAll();
        for (DanhSachDen item : all) {
            if (BienSoXeHelper.isSamePlate(item.getBienSoXe(), bienSoXe)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public Optional<DanhSachDen> checkBlacklist(String bienSoXe) {
        return kiemTraDanhSachDen(bienSoXe);
    }

    @Override
    @Transactional
    public DanhSachDen themVaoDanhSachDen(String bienSoXe, String lyDo, String mucDo, String nguoiThem) {
        String displayPlate = BienSoXeHelper.toDisplayFormat(bienSoXe);
        Optional<DanhSachDen> existing = kiemTraDanhSachDen(bienSoXe);
        if (existing.isPresent()) {
            DanhSachDen item = existing.get();
            item.setBienSoXe(displayPlate);
            item.setLyDo(lyDo);
            item.setMucDo(mucDo);
            return danhSachDenRepository.save(item);
        }
        DanhSachDen item = new DanhSachDen(displayPlate, lyDo, mucDo, nguoiThem);
        return danhSachDenRepository.save(item);
    }

    @Transactional
    public DanhSachDen addToBlacklist(String bienSoXe, String lyDo, String mucDo, String nguoiThem) {
        return themVaoDanhSachDen(bienSoXe, lyDo, mucDo, nguoiThem);
    }

    @Override
    @Transactional
    public void xoaKhoiDanhSachDen(Long id) {
        danhSachDenRepository.deleteById(id);
    }

    @Transactional
    public void removeFromBlacklist(Long id) {
        xoaKhoiDanhSachDen(id);
    }

    @Override
    public List<VeXe> layDanhSachXeQuaGio(int gioiHanGio) {
        LocalDateTime now = LocalDateTime.now();
        List<VeXe> activeVehicles = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        return activeVehicles.stream()
                .filter(v -> Duration.between(v.getThoiGianVao(), now).toHours() >= gioiHanGio)
                .collect(Collectors.toList());
    }

    public List<VeXe> getOverdueVehicles(int hoursLimit) {
        return layDanhSachXeQuaGio(hoursLimit);
    }
}

package com.example.demo.service;

import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.model.ViTriDo;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("baiDoXeService")
public class ParkingLotService implements BaiDoXeService {

    private final KhuVucRepository khuVucRepository;
    private final LoaiXeRepository loaiXeRepository;
    private final VeXeRepository veXeRepository;

    public ParkingLotService(KhuVucRepository khuVucRepository,
                             LoaiXeRepository loaiXeRepository,
                             VeXeRepository veXeRepository) {
        this.khuVucRepository = khuVucRepository;
        this.loaiXeRepository = loaiXeRepository;
        this.veXeRepository = veXeRepository;
    }

    @Override
    public List<KhuVuc> layTatCaKhuVuc() {
        return khuVucRepository.findAll();
    }

    @Override
    public List<ViTriDo> layTatCaViTriDo() {
        List<KhuVuc> khuVucs = khuVucRepository.findAll();
        List<VeXe> activeVehicles = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        List<ViTriDo> result = new ArrayList<>();

        for (KhuVuc kv : khuVucs) {
            int capacity = kv.getSucChua() != null && kv.getSucChua() > 0 ? kv.getSucChua() : 10;
            String maKhu = kv.getMaKhu() != null ? kv.getMaKhu() : "K";
            
            // Lọc các xe đang gửi thuộc loại xe của khu này
            List<VeXe> matchedVehicles = activeVehicles.stream()
                    .filter(v -> v.getLoaiXe() != null && kv.getLoaiXe() != null && v.getLoaiXe().getId().equals(kv.getLoaiXe().getId()))
                    .collect(Collectors.toList());

            for (int i = 1; i <= capacity; i++) {
                String maViTri = String.format("%s-%02d", maKhu, i);
                ViTriDo slot = new ViTriDo(maViTri, kv, "TRONG");
                slot.setId((long) (kv.getId() * 1000 + i));

                if (i <= matchedVehicles.size()) {
                    VeXe v = matchedVehicles.get(i - 1);
                    slot.setTrangThai("DA_DO");
                    slot.setBienSoXeDangDo(v.getBienSoXe());
                    slot.setThoiGianVaoDo(v.getThoiGianVao());
                }
                result.add(slot);
            }
        }
        return result;
    }

    @Override
    public List<ViTriDo> layViTriDoTheoKhuVuc(Long khuVucId) {
        return layTatCaViTriDo().stream()
                .filter(s -> s.getKhuVuc() != null && s.getKhuVuc().getId().equals(khuVucId))
                .collect(Collectors.toList());
    }

    @Override
    public List<LoaiXe> layTatCaLoaiXe() {
        return loaiXeRepository.findAll();
    }

    @Override
    public List<ViTriDo> layViTriTrong() {
        return layTatCaViTriDo().stream()
                .filter(s -> "TRONG".equals(s.getTrangThai()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ViTriDo> timViTriTrongChoXe(LoaiXe loaiXe) {
        return layViTriTrong().stream().findFirst();
    }

    public Optional<ViTriDo> findAvailableSlotForVehicle(LoaiXe loaiXe) {
        return timViTriTrongChoXe(loaiXe);
    }

    @Override
    public void chiemViTri(ViTriDo slot, String bienSoXe) {
        // Trong hệ thống quản lý gửi xe thực tế, quản lý chỗ đỗ theo vé xe
    }

    @Override
    public void giaiPhongViTri(ViTriDo slot) {
        // Trong hệ thống quản lý gửi xe thực tế, chỗ trống cập nhật tự động khi xe xuất bến
    }

    @Override
    public void giaiPhongViTriTheoBienSo(String bienSoXe) {
        // Tự động đồng bộ theo vé xe
    }

    @Override
    public long demTongViTri() {
        return khuVucRepository.findAll().stream()
                .mapToLong(k -> k.getSucChua() != null ? k.getSucChua() : 0)
                .sum();
    }

    @Override
    public long demViTriTrong() {
        long tong = demTongViTri();
        long dangDo = demViTriDangDo();
        return Math.max(0, tong - dangDo);
    }

    @Override
    public long demViTriDangDo() {
        return veXeRepository.countByTrangThai("Đang gửi");
    }

    @Override
    public long demViTriDaDat() {
        return 0L;
    }
}

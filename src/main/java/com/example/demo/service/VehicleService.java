package com.example.demo.service;

import com.example.demo.dto.PhuongTienDto;
import com.example.demo.dto.VehicleDto;
import com.example.demo.model.PhuongTien;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Lớp tương thích ngược kế thừa từ {@link PhuongTienService}
 */
public interface VehicleService extends PhuongTienService {

    default List<VehicleDto> getAllVehicles() {
        return layTatCaPhuongTien().stream()
                .map(p -> (p instanceof VehicleDto) ? (VehicleDto) p : new VehicleDto(p.getId(), p.getMaPhuongTien(), p.getBienSoXe(), p.getLoaiXeId(), p.getTenLoaiXe(), p.getChuXe(), p.getSoDienThoai(), p.getMoTa(), p.getNgayTao()))
                .collect(Collectors.toList());
    }

    default VehicleDto getVehicleById(Long id) {
        PhuongTienDto p = layPhuongTienTheoId(id);
        if (p instanceof VehicleDto) return (VehicleDto) p;
        return new VehicleDto(p.getId(), p.getMaPhuongTien(), p.getBienSoXe(), p.getLoaiXeId(), p.getTenLoaiXe(), p.getChuXe(), p.getSoDienThoai(), p.getMoTa(), p.getNgayTao());
    }

    default Optional<VehicleDto> findByBienSoXe(String bienSoXe) {
        return timTheoBienSoXe(bienSoXe).map(p -> (p instanceof VehicleDto) ? (VehicleDto) p : new VehicleDto(p.getId(), p.getMaPhuongTien(), p.getBienSoXe(), p.getLoaiXeId(), p.getTenLoaiXe(), p.getChuXe(), p.getSoDienThoai(), p.getMoTa(), p.getNgayTao()));
    }

    default List<VehicleDto> searchVehicles(String keyword) {
        return timKiemPhuongTien(keyword).stream()
                .map(p -> (p instanceof VehicleDto) ? (VehicleDto) p : new VehicleDto(p.getId(), p.getMaPhuongTien(), p.getBienSoXe(), p.getLoaiXeId(), p.getTenLoaiXe(), p.getChuXe(), p.getSoDienThoai(), p.getMoTa(), p.getNgayTao()))
                .collect(Collectors.toList());
    }

    default VehicleDto createVehicle(VehicleDto dto) {
        PhuongTienDto p = taoPhuongTien(dto);
        if (p instanceof VehicleDto) return (VehicleDto) p;
        return new VehicleDto(p.getId(), p.getMaPhuongTien(), p.getBienSoXe(), p.getLoaiXeId(), p.getTenLoaiXe(), p.getChuXe(), p.getSoDienThoai(), p.getMoTa(), p.getNgayTao());
    }

    default VehicleDto updateVehicle(Long id, VehicleDto dto) {
        PhuongTienDto p = capNhatPhuongTien(id, dto);
        if (p instanceof VehicleDto) return (VehicleDto) p;
        return new VehicleDto(p.getId(), p.getMaPhuongTien(), p.getBienSoXe(), p.getLoaiXeId(), p.getTenLoaiXe(), p.getChuXe(), p.getSoDienThoai(), p.getMoTa(), p.getNgayTao());
    }

    default void deleteVehicle(Long id) {
        xoaPhuongTien(id);
    }

    default PhuongTien findOrCreateByBienSo(String bienSoXe, Long loaiXeId, String chuXe, String soDienThoai) {
        return timHoacTaoTheoBienSo(bienSoXe, loaiXeId, chuXe, soDienThoai);
    }
}

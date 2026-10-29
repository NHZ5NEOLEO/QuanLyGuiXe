package com.example.demo.service;

import com.example.demo.model.KhachHang;
import com.example.demo.model.VeThang;

import java.util.List;
import java.util.Optional;

/**
 * Service quản lý vé tháng (thuê bao dài hạn) cho khách hàng
 */
public interface VeThangService {

    List<VeThang> layTatCaVeThang();

    List<KhachHang> layTatCaKhachHang();

    List<VeThang> timKiemVeThang(String tuKhoa);

    Optional<VeThang> layVeThangTheoId(Long id);

    Optional<VeThang> timVeThangConHan(String bienSoXe);

    VeThang dangKyVeThang(String hoTen, String sdt, String cccd, String email, String diaChi,
                          String bienSoXe, Long loaiXeId, int soThang);

    VeThang giaHanVeThang(Long veThangId, int soThang);

    void doiTrangThai(Long veThangId);

    long demVeThangHoatDong();
}

package com.example.demo.service;

import com.example.demo.dto.PhuongTienDto;
import com.example.demo.model.PhuongTien;

import java.util.List;
import java.util.Optional;

/**
 * Service quản lý thông tin phương tiện giao thông ra vào bãi
 */
public interface PhuongTienService {

    List<PhuongTienDto> layTatCaPhuongTien();

    PhuongTienDto layPhuongTienTheoId(Long id);

    Optional<PhuongTienDto> timTheoBienSoXe(String bienSoXe);

    List<PhuongTienDto> timKiemPhuongTien(String tuKhoa);

    PhuongTienDto taoPhuongTien(PhuongTienDto dto);

    PhuongTienDto capNhatPhuongTien(Long id, PhuongTienDto dto);

    void xoaPhuongTien(Long id);

    PhuongTien timHoacTaoTheoBienSo(String bienSoXe, Long loaiXeId, String chuXe, String soDienThoai);
}

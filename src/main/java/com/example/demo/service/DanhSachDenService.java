package com.example.demo.service;

import com.example.demo.model.DanhSachDen;
import com.example.demo.model.VeXe;

import java.util.List;
import java.util.Optional;

/**
 * Service quản lý danh sách đen các phương tiện vi phạm hoặc cảnh báo
 */
public interface DanhSachDenService {

    List<DanhSachDen> layTatCaDanhSachDen();

    Optional<DanhSachDen> kiemTraDanhSachDen(String bienSoXe);

    DanhSachDen themVaoDanhSachDen(String bienSoXe, String lyDo, String mucDo, String nguoiThem);

    void xoaKhoiDanhSachDen(Long id);

    List<VeXe> layDanhSachXeQuaGio(int gioiHanGio);
}

package com.example.demo.service;

import com.example.demo.model.DatChoTruoc;

import java.util.List;
import java.util.Optional;

/**
 * Service quản lý nghiệp vụ đặt chỗ đỗ xe trước và thanh toán cọc giữ chỗ
 */
public interface DatChoTruocService {

    List<DatChoTruoc> layDanhSachDatChoHieuLuc();

    List<DatChoTruoc> layTatCaDatCho();

    DatChoTruoc taoDatCho(String hoTen, String sdt, String bienSoXe, String maViTri);

    DatChoTruoc taoDatCho(String hoTen, String sdt, String bienSoXe, String maKhuVuc, Double tienCoc, Integer soPhutGiu);

    String taoQrDatCoc(DatChoTruoc datChoTruoc);

    Optional<DatChoTruoc> timTheoMaDatCho(String maDatCho);

    List<DatChoTruoc> timTheoBienSo(String bienSoXe);

    List<DatChoTruoc> timKiem(String keyword);

    boolean huyDatCho(Long id);

    boolean xacNhanThanhToanCoc(Long id, String maGiaoDich);

    boolean xoaDatCho(Long id);

    void quetVaCapNhatHetHan();
}

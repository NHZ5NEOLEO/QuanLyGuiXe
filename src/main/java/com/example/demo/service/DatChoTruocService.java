package com.example.demo.service;

import com.example.demo.model.DatChoTruoc;

import java.util.List;

/**
 * Service quản lý đặt chỗ trước và thanh toán cọc giữ chỗ
 */
public interface DatChoTruocService {

    List<DatChoTruoc> layDanhSachDatChoHieuLuc();

    DatChoTruoc taoDatCho(String hoTen, String sdt, String bienSoXe, String maViTri);

    String taoQrDatCoc(DatChoTruoc datChoTruoc);
}

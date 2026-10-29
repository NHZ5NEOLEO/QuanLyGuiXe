package com.example.demo.dto;

/**
 * Lớp tương thích ngược kế thừa từ {@link YeuCauVaoBai}
 */
public class CheckInRequest extends YeuCauVaoBai {
    public CheckInRequest() {
        super();
    }

    public CheckInRequest(String bienSoXe, String maQR, Long loaiXeId, String maViTri, String anhVaoData, String ghiChu) {
        super(bienSoXe, maQR, loaiXeId, maViTri, anhVaoData, ghiChu);
    }
}

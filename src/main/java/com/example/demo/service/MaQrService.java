package com.example.demo.service;

/**
 * Service xử lý tạo và kiểm tra tính hợp lệ của mã QR code
 */
public interface MaQrService {

    byte[] taoAnhQrPngBytes(String noiDung, int chieuRong, int chieuCao);

    String taoAnhQrBase64(String noiDung, int chieuRong, int chieuCao);

    String taoQrCongVao(String diaChiGoc);

    String taoQrCongRa(String diaChiGoc);

    boolean xacThucQrVeXe(String maQr);
}

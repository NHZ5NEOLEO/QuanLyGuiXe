package com.example.demo.service;

import com.example.demo.exception.InvalidQrCodeException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Service quản lý mã QR sử dụng thư viện ZXing.
 * Hỗ trợ tạo ảnh PNG byte[], chuỗi Base64 Data URI, và xác minh mã QR vé xe.
 */
@Service("maQrService")
public class QrCodeService implements MaQrService {

    @Override
    public byte[] taoAnhQrPngBytes(String noiDung, int chieuRong, int chieuCao) {
        if (noiDung == null || noiDung.trim().isEmpty()) {
            throw new InvalidQrCodeException("Nội dung tạo mã QR không được để trống!");
        }
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(noiDung, BarcodeFormat.QR_CODE, chieuRong, chieuCao);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new InvalidQrCodeException("Không thể tạo mã QR: " + e.getMessage());
        }
    }

    public byte[] generateQrPngBytes(String content, int width, int height) {
        return taoAnhQrPngBytes(content, width, height);
    }

    @Override
    public String taoAnhQrBase64(String noiDung, int chieuRong, int chieuCao) {
        byte[] imageBytes = taoAnhQrPngBytes(noiDung, chieuRong, chieuCao);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    public String generateQrBase64(String content, int width, int height) {
        return taoAnhQrBase64(content, width, height);
    }

    @Override
    public String taoQrCongVao(String diaChiGoc) {
        String url = diaChiGoc + "/khach-gui-xe";
        return taoAnhQrBase64(url, 300, 300);
    }

    public String generateEntryGateQr(String baseUrl) {
        return taoQrCongVao(baseUrl);
    }

    @Override
    public String taoQrCongRa(String diaChiGoc) {
        String url = diaChiGoc + "/khach-nhan-xe";
        return taoAnhQrBase64(url, 300, 300);
    }

    public String generateExitGateQr(String baseUrl) {
        return taoQrCongRa(baseUrl);
    }

    @Override
    public boolean xacThucQrVeXe(String maQr) {
        if (maQr == null || maQr.trim().isEmpty()) {
            return false;
        }
        String clean = maQr.trim();
        return clean.length() >= 5 && clean.length() <= 100;
    }

    public boolean verifyTicketQr(String qrToken) {
        return xacThucQrVeXe(qrToken);
    }
}

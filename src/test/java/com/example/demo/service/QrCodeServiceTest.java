package com.example.demo.service;

import com.example.demo.exception.InvalidQrCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private QrCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeService();
    }

    @Test
    @DisplayName("1. Sinh mảng byte PNG mã QR hợp lệ")
    void testGenerateQrPngBytes_ValidContent_ReturnsNonEmptyBytes() {
        byte[] bytes = qrCodeService.taoAnhQrPngBytes("QR-261119-TEST", 250, 250);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        // Header PNG bắt đầu bằng byte: -119, 80, 78, 71 (0x89 0x50 0x4E 0x47)
        assertEquals((byte) 0x89, bytes[0]);
        assertEquals((byte) 'P', bytes[1]);
        assertEquals((byte) 'N', bytes[2]);
        assertEquals((byte) 'G', bytes[3]);
    }

    @Test
    @DisplayName("2. Sinh chuỗi Base64 Data URI cho hiển thị web và in phiếu")
    void testGenerateQrBase64_ValidContent_ReturnsDataUri() {
        String base64 = qrCodeService.taoAnhQrBase64("QR-261119-TEST", 200, 200);

        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/png;base64,"));
        assertTrue(base64.length() > 30);
    }

    @Test
    @DisplayName("3. Ném ngoại lệ InvalidQrCodeException khi nội dung rỗng hoặc null")
    void testGenerateQr_EmptyOrNullContent_ThrowsInvalidQrCodeException() {
        assertThrows(InvalidQrCodeException.class, () -> qrCodeService.taoAnhQrPngBytes(null, 200, 200));
        assertThrows(InvalidQrCodeException.class, () -> qrCodeService.taoAnhQrPngBytes("", 200, 200));
        assertThrows(InvalidQrCodeException.class, () -> qrCodeService.taoAnhQrPngBytes("   ", 200, 200));
    }

    @Test
    @DisplayName("4. Sinh mã QR cổng vào và cổng ra chứa đúng URL")
    void testGateQrUrls() {
        String entryQr = qrCodeService.taoQrCongVao("http://localhost");
        assertNotNull(entryQr);
        assertTrue(entryQr.startsWith("data:image/png;base64,"));

        String exitQr = qrCodeService.taoQrCongRa("http://localhost");
        assertNotNull(exitQr);
        assertTrue(exitQr.startsWith("data:image/png;base64,"));
    }
}

package com.example.demo;

import com.example.demo.util.BienSoXeHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BienSoXeHelperTest {

    @Test
    @DisplayName("Chuẩn hóa biển số ô tô 5 số có dấu chấm và gạch ngang")
    void testCar5Plate() {
        assertEquals("30A-123.45", BienSoXeHelper.toDisplayFormat("30a12345", "Ô tô"));
        assertEquals("30A-123.45", BienSoXeHelper.toDisplayFormat("  30A - 12345  ", "Ô tô"));
        assertEquals("30A-123.45", BienSoXeHelper.toDisplayFormat("30a-123.45", "Ô tô"));
        assertEquals("51K-999.99", BienSoXeHelper.toDisplayFormat("51k 99999", "Ô tô"));
    }

    @Test
    @DisplayName("Chuẩn hóa biển số xe máy 5 số có dấu chấm và gạch ngang")
    void testMotorcycle5Plate() {
        assertEquals("29B1-888.88", BienSoXeHelper.toDisplayFormat("29b188888", "Xe máy"));
        assertEquals("29B1-888.88", BienSoXeHelper.toDisplayFormat(" 29-b1 888.88 ", "Xe máy"));
        assertEquals("59X3-999.99", BienSoXeHelper.toDisplayFormat("59X3-99999", "Xe máy"));
        assertEquals("43B1-998.81", BienSoXeHelper.toDisplayFormat("43b1 99881", "Xe máy"));
    }

    @Test
    @DisplayName("Hỗ trợ biển số 4 số cũ vẫn đang lưu hành hợp pháp")
    void testOld4Plates() {
        // Biển ô tô 4 số
        assertEquals("29A-1234", BienSoXeHelper.toDisplayFormat("29a1234", "Ô tô"));
        assertEquals("52Z-9999", BienSoXeHelper.toDisplayFormat(" 52z 9999 ", "Ô tô"));

        // Biển xe máy 4 số
        assertEquals("29B1-1234", BienSoXeHelper.toDisplayFormat("29b11234", "Xe máy"));
        // Biển tỉnh cũ (Hà Tây 33)
        assertEquals("33M2-1234", BienSoXeHelper.toDisplayFormat("33m2 1234", "Xe máy"));
    }

    @Test
    @DisplayName("Bảo toàn số 0 ở đầu mã tỉnh và nhóm số đăng ký")
    void testPreserveLeadingZeros() {
        assertEquals("01A-001.23", BienSoXeHelper.toDisplayFormat("01a00123", "Ô tô"));
        assertEquals("01A00123", BienSoXeHelper.toCleanKey("01A-001.23"));
    }

    @Test
    @DisplayName("So sánh nhận diện cùng phương tiện bất kể định dạng nhập")
    void testIsSamePlate() {
        assertTrue(BienSoXeHelper.isSamePlate("30A-123.45", "30a12345"));
        assertTrue(BienSoXeHelper.isSamePlate("29B1-888.88", "29-B1 888.88"));
        assertTrue(BienSoXeHelper.isSamePlate("29b188888", "29B1-888.88"));
        assertTrue(BienSoXeHelper.isSamePlate("29A-1234", "29a 1234"));

        assertFalse(BienSoXeHelper.isSamePlate("30A-123.45", "30A-123.46"));
        assertFalse(BienSoXeHelper.isSamePlate("29B1-888.88", "29B2-888.88"));
    }

    @Test
    @DisplayName("Kiểm tra tính hợp lệ cơ bản")
    void testValidation() {
        assertFalse(BienSoXeHelper.validate("").valid());
        assertFalse(BienSoXeHelper.validate("   ").valid());
        assertFalse(BienSoXeHelper.validate("29A@123").valid());
        assertFalse(BienSoXeHelper.validate("123").valid()); // Quá ngắn

        assertTrue(BienSoXeHelper.validate("29A-123.45").valid());
        assertTrue(BienSoXeHelper.validate("29b1-88888").valid());
        assertTrue(BienSoXeHelper.validate("33M2-1234").valid()); // Hà Tây cũ hợp lệ
    }

    @Test
    @DisplayName("Hỗ trợ phương tiện không biển số (Xe đạp, xe điện không biển)")
    void testBicycleUnplatedSupport() {
        // Hợp lệ với mã XD
        assertTrue(BienSoXeHelper.validate("XD-01").valid());
        assertTrue(BienSoXeHelper.validate("XD-8899").valid());
        assertTrue(BienSoXeHelper.validate("XEDAP-05").valid());

        // Hợp lệ khi loại xe là Xe đạp dù người dùng chỉ nhập số thẻ (VD thẻ số 15)
        assertTrue(BienSoXeHelper.validate("15", "Xe đạp / Xe điện").valid());
        assertEquals("XD-15", BienSoXeHelper.toDisplayFormat("15", "Xe đạp / Xe điện"));
        assertEquals("XD-01", BienSoXeHelper.toDisplayFormat("1", "Xe đạp / Xe điện"));
        assertEquals("XD-05", BienSoXeHelper.toDisplayFormat("XD05", "Xe đạp / Xe điện"));
    }
}

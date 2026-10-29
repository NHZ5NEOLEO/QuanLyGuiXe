package com.example.demo;

import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.exception.InvalidTicketStateException;
import com.example.demo.model.LoaiXe;
import com.example.demo.service.impl.FeeCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FeeCalculationServiceTest {

    private FeeCalculationServiceImpl feeCalculationService;
    private LoaiXe xeMay;
    private LoaiXe oTo;

    @BeforeEach
    void setUp() {
        feeCalculationService = new FeeCalculationServiceImpl();
        xeMay = new LoaiXe("Xe máy", 4000.0, 6000.0, 100000.0, "fa-motorcycle", "Xe máy");
        oTo = new LoaiXe("Ô tô", 20000.0, 35000.0, 1200000.0, "fa-car", "Ô tô con");
    }

    @Test
    @DisplayName("Tính phí gửi xe máy ban ngày (08:00 - 11:30)")
    void testCalculate_Daytime_Motorbike() {
        LocalDateTime vao = LocalDateTime.of(2026, 10, 12, 8, 0);
        LocalDateTime ra = LocalDateTime.of(2026, 10, 12, 11, 30);

        FeeCalculationResult result = feeCalculationService.calculate(xeMay, vao, ra, false, false);

        assertNotNull(result);
        assertEquals(4000.0, result.getSoTienThanhToan());
        assertFalse(result.isLaVeThang());
        assertTrue(result.getQuyTacApDung().contains("ban ngày"));
    }

    @Test
    @DisplayName("Tính phí gửi ô tô qua đêm (21:00 - 06:30 hôm sau)")
    void testCalculate_Nighttime_Car() {
        LocalDateTime vao = LocalDateTime.of(2026, 10, 12, 21, 0);
        LocalDateTime ra = LocalDateTime.of(2026, 10, 13, 6, 30);

        FeeCalculationResult result = feeCalculationService.calculate(oTo, vao, ra, false, false);

        assertNotNull(result);
        assertEquals(35000.0, result.getSoTienThanhToan());
        assertTrue(result.getQuyTacApDung().contains("ban đêm"));
    }

    @Test
    @DisplayName("Tính phí lưu bãi nhiều ngày (3 ngày 2 giờ)")
    void testCalculate_MultiDay() {
        LocalDateTime vao = LocalDateTime.of(2026, 10, 10, 8, 0);
        LocalDateTime ra = LocalDateTime.of(2026, 10, 13, 10, 0); // 3 ngày 2 giờ

        FeeCalculationResult result = feeCalculationService.calculate(xeMay, vao, ra, false, false);

        // 3 ngày x (4000 + 6000) + 4000 = 34000
        assertEquals(34000.0, result.getSoTienThanhToan());
        assertTrue(result.getQuyTacApDung().contains("nhiều ngày"));
    }

    @Test
    @DisplayName("Thuê bao vé tháng được miễn phí vé lượt (0 VNĐ)")
    void testCalculate_MonthlyTicket_Free() {
        LocalDateTime vao = LocalDateTime.of(2026, 10, 12, 8, 0);
        LocalDateTime ra = LocalDateTime.of(2026, 10, 12, 17, 0);

        FeeCalculationResult result = feeCalculationService.calculate(oTo, vao, ra, true, false);

        assertEquals(0.0, result.getSoTienThanhToan());
        assertTrue(result.isLaVeThang());
        assertTrue(result.getQuyTacApDung().contains("vé tháng"));
    }

    @Test
    @DisplayName("Khấu trừ tiền cọc đặt chỗ trước 20.000 VNĐ")
    void testCalculate_WithPreBookingDeposit() {
        LocalDateTime vao = LocalDateTime.of(2026, 10, 12, 22, 0);
        LocalDateTime ra = LocalDateTime.of(2026, 10, 13, 7, 0); // Ô tô qua đêm 35.000đ

        FeeCalculationResult result = feeCalculationService.calculate(oTo, vao, ra, false, true);

        // 35000 - 20000 = 15000
        assertEquals(35000.0, result.getTongTienGoc());
        assertEquals(20000.0, result.getTienKhauTru());
        assertEquals(15000.0, result.getSoTienThanhToan());
    }

    @Test
    @DisplayName("Báo lỗi khi thời gian ra nhỏ hơn thời gian vào")
    void testCalculate_CheckOutBeforeCheckIn_ThrowsException() {
        LocalDateTime vao = LocalDateTime.of(2026, 10, 12, 12, 0);
        LocalDateTime ra = LocalDateTime.of(2026, 10, 12, 10, 0);

        assertThrows(InvalidTicketStateException.class, () -> {
            feeCalculationService.calculate(xeMay, vao, ra, false, false);
        });
    }
}

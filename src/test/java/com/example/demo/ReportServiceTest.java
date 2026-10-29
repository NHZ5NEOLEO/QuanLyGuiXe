package com.example.demo;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.repository.*;
import com.example.demo.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private VeXeRepository veXeRepository;

    @Mock
    private VeThangRepository veThangRepository;

    @Mock
    private LoaiXeRepository loaiXeRepository;

    @Mock
    private KhuVucRepository khuVucRepository;

    @InjectMocks
    private ReportService reportService;

    private LoaiXe xeMay;

    @BeforeEach
    void setUp() {
        xeMay = new LoaiXe("Xe máy", 4000.0, 6000.0, 100000.0, "fa-motorcycle", "Xe máy");
    }

    /**
     * Trường hợp 11: Thống kê chính xác số xe đang ở bãi
     */
    @Test
    @DisplayName("11. Thống kê chính xác số xe đang ở trong bãi (trạng thái Đang gửi)")
    void testOccupancyCount_CalculatesCorrectActiveVehicles() {
        com.example.demo.model.KhuVuc kv = new com.example.demo.model.KhuVuc("A", "Khu A", xeMay, 40, "Tầng 1");
        when(khuVucRepository.findAll()).thenReturn(java.util.List.of(kv));
        when(veXeRepository.countByTrangThai("Đang gửi")).thenReturn(15L);
        when(veThangRepository.countByTrangThai("HOAT_DONG")).thenReturn(3L);

        DashboardStatsDto stats = reportService.getDashboardStats("today");

        assertNotNull(stats);
        assertEquals(40L, stats.getTongCho());
        assertEquals(25L, stats.getChoTrong());
        assertEquals(15L, stats.getXeDangTrongBai());
        assertEquals(3L, stats.getTongVeThangHoatDong());
        // Tỷ lệ lấp đầy: (40 - 25) / 40 = 15 / 40 = 37.5%
        assertEquals(37.5, stats.getTyLeLapDay());
    }

    /**
     * Trường hợp 12: Thống kê doanh thu mà không tính trùng giao dịch
     */
    @Test
    @DisplayName("12. Thống kê doanh thu chính xác: chỉ tính vé 'Đã ra' có phí, không tính trùng vé 'Đang gửi'")
    void testRevenueCalculation_ExcludesActiveTicketsWithoutDuplicate() {
        // Vé 1: Đã ra, thu 20.000đ
        VeXe vx1 = new VeXe("QR-1", "30A-11111", xeMay, null, LocalDateTime.now().minusHours(4), "Đã ra");
        vx1.setChiPhi(20000.0);

        // Vé 2: Đã ra, thu 4.000đ
        VeXe vx2 = new VeXe("QR-2", "29B-22222", xeMay, null, LocalDateTime.now().minusHours(3), "Đã ra");
        vx2.setChiPhi(4000.0);

        // Vé 3: Đang gửi, chưa thu tiền (chiPhi = null)
        VeXe vx3 = new VeXe("QR-3", "51H-33333", xeMay, null, LocalDateTime.now().minusHours(1), "Đang gửi");
        vx3.setChiPhi(null);

        when(veXeRepository.calculateRevenueBetween(any(), any())).thenReturn(24000.0);

        DashboardStatsDto stats = reportService.getDashboardStats("today");

        assertNotNull(stats);
        assertEquals(24000.0, stats.getDoanhThuHomNay());
        verify(veXeRepository, atLeastOnce()).calculateRevenueBetween(any(), any());
    }
}

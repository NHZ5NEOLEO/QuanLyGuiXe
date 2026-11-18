package com.example.demo.service;

import com.example.demo.model.CaTruc;
import com.example.demo.model.NguoiDung;
import com.example.demo.model.VeXe;
import com.example.demo.repository.CaTrucRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VeXeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private CaTrucRepository caTrucRepository;

    @Mock
    private VeXeRepository veXeRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @InjectMocks
    private ShiftService shiftService;

    private NguoiDung nhanVien;

    @BeforeEach
    void setUp() {
        nhanVien = new NguoiDung("baove_a", "123456", "Nguyễn Văn Bảo Vệ", "NHAN_VIEN", "0901234567");
        nhanVien.setId(10L);
    }

    @Test
    @DisplayName("1. Lấy ca trực hiện tại khi chưa có ca trực nào mở -> Trả về null an toàn")
    void testLayCaTrucHienTai_NoActiveShift_ReturnsNull() {
        when(caTrucRepository.findFirstByTrangThaiOrderByThoiGianBatDauDesc("DANG_TRUC"))
                .thenReturn(Optional.empty());

        CaTruc result = shiftService.layCaTrucHienTai();
        assertNull(result);
    }

    @Test
    @DisplayName("2. Bắt đầu ca trực mới thành công gắn với nhân viên đăng nhập")
    void testBatDauCaTruc_Success() {
        when(caTrucRepository.findFirstByTrangThaiOrderByThoiGianBatDauDesc("DANG_TRUC"))
                .thenReturn(Optional.empty());
        when(nguoiDungRepository.findByTenDangNhap("baove_a")).thenReturn(Optional.of(nhanVien));
        when(caTrucRepository.save(any(CaTruc.class))).thenAnswer(inv -> inv.getArgument(0));

        CaTruc created = shiftService.batDauCaTruc("Ca Sáng", "baove_a");

        assertNotNull(created);
        assertEquals("Ca Sáng", created.getTenCa());
        assertEquals("DANG_TRUC", created.getTrangThai());
        assertEquals(nhanVien, created.getNguoiTruc());
        assertNotNull(created.getThoiGianBatDau());
        verify(caTrucRepository, atLeastOnce()).save(any(CaTruc.class));
    }

    @Test
    @DisplayName("3. Từ chối mở ca mới khi đang có ca trực hoạt động (ngăn mở trùng)")
    void testBatDauCaTruc_ActiveShiftAlreadyExists_ThrowsException() {
        CaTruc activeShift = new CaTruc("CA-01", "Ca Sáng", nhanVien, LocalDateTime.now().minusHours(2));
        activeShift.setTrangThai("DANG_TRUC");

        when(caTrucRepository.findFirstByTrangThaiOrderByThoiGianBatDauDesc("DANG_TRUC"))
                .thenReturn(Optional.of(activeShift));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> shiftService.batDauCaTruc("Ca Chiều", "baove_a"));
        assertTrue(ex.getMessage().contains("đang hoạt động"));
        verify(caTrucRepository, never()).save(any(CaTruc.class));
    }

    @Test
    @DisplayName("4. Cập nhật thống kê ca trực: tính đúng lượt vào/ra, tiền mặt và chuyển khoản")
    void testCapNhatThongKeCaTruc_CalculatesCorrectStats() {
        LocalDateTime start = LocalDateTime.now().minusHours(4);
        CaTruc shift = new CaTruc("CA-02", "Ca Chiều", nhanVien, start);
        shift.setTrangThai("DANG_TRUC");

        VeXe v1 = new VeXe();
        v1.setHinhThucThanhToan("TIEN_MAT");
        v1.setChiPhi(20000.0);
        v1.setThoiGianRa(start.plusHours(1));

        VeXe v2 = new VeXe();
        v2.setHinhThucThanhToan("CHUYEN_KHOAN");
        v2.setChiPhi(35000.0);
        v2.setThoiGianRa(start.plusHours(2));

        when(veXeRepository.countByThoiGianVaoBetween(any(), any())).thenReturn(5L);
        when(veXeRepository.countByThoiGianRaBetween(any(), any())).thenReturn(2L);
        when(veXeRepository.searchHistory(isNull(), eq("Đã ra"), isNull(), any(), any()))
                .thenReturn(List.of(v1, v2));
        when(caTrucRepository.save(any(CaTruc.class))).thenAnswer(inv -> inv.getArgument(0));

        shiftService.capNhatThongKeCaTruc(shift);

        assertEquals(5, shift.getTongLuotVao());
        assertEquals(2, shift.getTongLuotRa());
        assertEquals(20000.0, shift.getTienMatThuDuoc());
        assertEquals(35000.0, shift.getChuyenKhoanThuDuoc());
        assertEquals(55000.0, shift.getTongDoanhThu());
    }

    @Test
    @DisplayName("5. Bàn giao ca trực (TC-12): Đổi trạng thái DA_BAN_GIAO, chốt thời gian và ghi chú")
    void testBanGiaoCaTruc_Success() {
        LocalDateTime start = LocalDateTime.now().minusHours(8);
        CaTruc shift = new CaTruc("CA-03", "Ca Đêm", nhanVien, start);
        shift.setId(100L);
        shift.setTrangThai("DANG_TRUC");

        when(caTrucRepository.findById(100L)).thenReturn(Optional.of(shift));
        when(caTrucRepository.save(any(CaTruc.class))).thenAnswer(inv -> inv.getArgument(0));

        CaTruc handedOver = shiftService.banGiaoCaTruc(100L, "Bàn giao ca đầy đủ, tiền mặt 200k khớp");

        assertEquals("DA_BAN_GIAO", handedOver.getTrangThai());
        assertNotNull(handedOver.getThoiGianKetThuc());
        assertEquals("Bàn giao ca đầy đủ, tiền mặt 200k khớp", handedOver.getGhiChuBanGiao());
    }
}

package com.example.demo;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.exception.InvalidTicketStateException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.VehicleAlreadyParkedException;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private VeXeRepository veXeRepository;

    @Mock
    private LoaiXeRepository loaiXeRepository;

    @Mock
    private ParkingLotService parkingLotService;

    @Mock
    private MonthlyTicketService monthlyTicketService;

    @Mock
    private VietQrService vietQrService;

    @Mock
    private DatChoTruocRepository datChoTruocRepository;

    @Mock
    private FeeCalculationService feeCalculationService;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private ParkingService parkingService;

    private LoaiXe loaiXeMay;
    private LoaiXe loaiXeOTo;

    @BeforeEach
    void setUp() {
        loaiXeMay = new LoaiXe("Xe máy", 4000.0, 6000.0, 100000.0, "fa-motorcycle", "Xe máy");
        loaiXeMay.setId(1L);

        loaiXeOTo = new LoaiXe("Ô tô (4-7 chỗ)", 20000.0, 35000.0, 1200000.0, "fa-car", "Ô tô con");
        loaiXeOTo.setId(2L);
    }

    /**
     * Trường hợp 1: Gửi xe với biển số hợp lệ -> Thành công
     */
    @Test
    @DisplayName("1. Gửi xe với biển số hợp lệ -> Tạo vé thành công")
    void testCheckIn_ValidLicensePlate_Success() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("29B1-123.45");
        request.setLoaiXeId(1L);

        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc("29B1-123.45", "Đang gửi"))
                .thenReturn(Optional.empty());
        when(veXeRepository.findByMaQR(anyString())).thenReturn(Optional.empty());
        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(monthlyTicketService.findActiveMonthlyPass("29B1-123.45")).thenReturn(Optional.empty());
        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc("29B1-123.45", "CHO_DEN"))
                .thenReturn(Optional.empty());
        lenient().when(parkingLotService.findAvailableSlotForVehicle(loaiXeMay)).thenReturn(Optional.empty());

        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> {
            VeXe v = invocation.getArgument(0);
            v.setId(101);
            return v;
        });

        VeXe result = parkingService.checkIn(request);

        assertNotNull(result);
        assertEquals("29B1-123.45", result.getBienSoXe());
        assertEquals("Đang gửi", result.getTrangThai());
        assertNotNull(result.getMaQR());
        assertTrue(result.getMaQR().startsWith("QR-"));
        verify(veXeRepository, times(1)).save(any(VeXe.class));
    }

    /**
     * Trường hợp 2: Không cho tạo lượt gửi trùng trái quy tắc
     */
    @Test
    @DisplayName("2. Không cho tạo lượt gửi trùng khi xe đang trong bãi")
    void testCheckIn_DuplicateActiveVehicle_ThrowsVehicleAlreadyParkedException() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("30A-999.99");

        VeXe xeDangTrongBai = new VeXe("QR-OLD-001", "30A-999.99", loaiXeOTo, null, LocalDateTime.now().minusHours(2), "Đang gửi");
        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc("30A-999.99", "Đang gửi"))
                .thenReturn(Optional.of(xeDangTrongBai));

        assertThrows(VehicleAlreadyParkedException.class, () -> {
            parkingService.checkIn(request);
        });

        verify(veXeRepository, never()).save(any(VeXe.class));
    }

    /**
     * Trường hợp 3: Ghi nhận đúng thời gian vào từ máy chủ
     */
    @Test
    @DisplayName("3. Ghi nhận đúng thời gian vào theo đồng hồ máy chủ")
    void testCheckIn_RecordsServerTimestamp() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("51H-555.55");
        request.setLoaiXeId(2L);

        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc("51H-555.55", "Đang gửi"))
                .thenReturn(Optional.empty());
        when(loaiXeRepository.findById(2L)).thenReturn(Optional.of(loaiXeOTo));
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime beforeTime = LocalDateTime.now().minusSeconds(1);
        VeXe result = parkingService.checkIn(request);
        LocalDateTime afterTime = LocalDateTime.now().plusSeconds(1);

        assertNotNull(result.getThoiGianVao());
        assertTrue(result.getThoiGianVao().isAfter(beforeTime) || result.getThoiGianVao().isEqual(beforeTime));
        assertTrue(result.getThoiGianVao().isBefore(afterTime) || result.getThoiGianVao().isEqual(afterTime));
    }

    /**
     * Trường hợp 4: Sinh mã vé duy nhất và mã QR tương ứng
     */
    @Test
    @DisplayName("4. Sinh mã vé duy nhất và mã QR hợp lệ")
    void testCheckIn_GeneratesUniqueTicketCodeAndQrToken() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("43B1-777.77");

        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc("43B1-777.77", "Đang gửi"))
                .thenReturn(Optional.empty());
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeXe result = parkingService.checkIn(request);

        assertNotNull(result.getMaQR());
        assertFalse(result.getMaQR().trim().isEmpty());
        assertEquals(result.getMaQR(), result.getTicketCode());
    }

    /**
     * Trường hợp 5: Tra cứu vé hợp lệ qua mã QR
     */
    @Test
    @DisplayName("5. Tra cứu vé hợp lệ qua mã QR")
    void testPreviewCheckOut_ValidQrCode_ReturnsTicketInfo() {
        String maQR = "QR-261012-TEST";
        VeXe veXe = new VeXe(maQR, "29B1-88888", loaiXeMay, null, LocalDateTime.now().minusHours(2), "Đang gửi");
        veXe.setId(10);

        when(veXeRepository.findByMaQR(maQR)).thenReturn(Optional.of(veXe));

        FeeCalculationResult feeResult = new FeeCalculationResult();
        feeResult.setSoTienThanhToan(4000.0);
        feeResult.setThoiLuongText("2 giờ 0 phút");
        when(feeCalculationService.calculate(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(feeResult);

        CheckOutPreviewResponse preview = parkingService.previewCheckOut(maQR);

        assertNotNull(preview);
        assertEquals("29B1-88888", preview.getBienSoXe());
        assertEquals(4000.0, preview.getSoTien());
        assertEquals("2 giờ 0 phút", preview.getThoiLuong());
    }

    /**
     * Trường hợp 6: Không tìm thấy vé khi dùng mã sai
     */
    @Test
    @DisplayName("6. Báo lỗi không tìm thấy vé khi dùng mã sai")
    void testPreviewCheckOut_InvalidCode_ThrowsResourceNotFoundException() {
        when(veXeRepository.findByMaQR("QR-KHONG-TON-TAI")).thenReturn(Optional.empty());
        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc("QR-KHONG-TON-TAI", "Đang gửi"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            parkingService.previewCheckOut("QR-KHONG-TON-TAI");
        });
    }

    /**
     * Trường hợp 7: Tính phí với thời gian gửi hợp lệ
     */
    @Test
    @DisplayName("7. Tính phí chính xác theo quy tắc giá cấu hình")
    void testFeeCalculation_ValidDuration_CalculatesCorrectFee() {
        LocalDateTime vao = LocalDateTime.of(2026, 10, 12, 8, 0);
        LocalDateTime ra = LocalDateTime.of(2026, 10, 12, 10, 30);

        FeeCalculationResult result = new FeeCalculationResult();
        result.setSoTienThanhToan(4000.0);
        result.setTongTienGoc(4000.0);
        when(feeCalculationService.calculate(eq(loaiXeMay), eq(vao), eq(ra), eq(false), eq(false)))
                .thenReturn(result);

        double fee = parkingService.calculateFee(loaiXeMay, vao, ra, 150);
        assertEquals(4000.0, fee);
    }

    /**
     * Trường hợp 8: Nhận xe và hoàn tất vé thành công
     */
    @Test
    @DisplayName("8. Nhận xe và hoàn tất vé thành công (Check-out)")
    void testConfirmCheckOut_Success_CompletesTicket() {
        VeXe veXe = new VeXe("QR-VALID-123", "29B1-12345", loaiXeMay, null, LocalDateTime.now().minusHours(1), "Đang gửi");
        veXe.setId(50);

        when(veXeRepository.findById(50)).thenReturn(Optional.of(veXe));

        FeeCalculationResult feeResult = new FeeCalculationResult();
        feeResult.setSoTienThanhToan(4000.0);
        when(feeCalculationService.calculate(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(feeResult);
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeXe completed = parkingService.confirmCheckOut(50, "TIEN_MAT", null, "Đã thu tiền đủ");

        assertNotNull(completed);
        assertEquals("Đã ra", completed.getTrangThai());
        assertNotNull(completed.getThoiGianRa());
        assertEquals(4000.0, completed.getChiPhi());
        assertEquals("TIEN_MAT", completed.getHinhThucThanhToan());
        verify(veXeRepository, times(1)).save(completed);
    }

    /**
     * Trường hợp 9: Không cho nhận lại vé đã hoàn tất
     */
    @Test
    @DisplayName("9. Chặn Check-out lặp lại đối với vé đã hoàn tất")
    void testConfirmCheckOut_AlreadyCompleted_ThrowsInvalidTicketStateException() {
        VeXe veXe = new VeXe("QR-COMPLETED", "29B1-12345", loaiXeMay, null, LocalDateTime.now().minusHours(3), "Đã ra");
        veXe.setId(60);
        veXe.setThoiGianRa(LocalDateTime.now().minusHours(1));

        when(veXeRepository.findById(60)).thenReturn(Optional.of(veXe));

        assertThrows(InvalidTicketStateException.class, () -> {
            parkingService.confirmCheckOut(60, "TIEN_MAT", null, null);
        });

        verify(veXeRepository, never()).save(any(VeXe.class));
    }

    /**
     * Trường hợp 10: Ngăn thời gian ra nhỏ hơn thời gian vào
     */
    @Test
    @DisplayName("10. Ngăn chặn thời gian ra nhỏ hơn thời gian vào")
    void testConfirmCheckOut_CheckOutBeforeCheckIn_ThrowsInvalidTicketStateException() {
        VeXe veXe = new VeXe("QR-FUTURE", "29B1-12345", loaiXeMay, null, LocalDateTime.now().plusHours(2), "Đang gửi");
        veXe.setId(70);

        when(veXeRepository.findById(70)).thenReturn(Optional.of(veXe));

        assertThrows(InvalidTicketStateException.class, () -> {
            parkingService.confirmCheckOut(70, "TIEN_MAT", null, null);
        });
    }
}

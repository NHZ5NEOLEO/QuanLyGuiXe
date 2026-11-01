package com.example.demo;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.InvalidTicketStateException;
import com.example.demo.exception.VehicleAlreadyParkedException;
import com.example.demo.model.DanhSachDen;
import com.example.demo.model.DatChoTruoc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeThang;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.BlacklistService;
import com.example.demo.service.FeeCalculationService;
import com.example.demo.service.MonthlyTicketService;
import com.example.demo.service.ParkingService;
import com.example.demo.service.QrCodeService;
import com.example.demo.service.VietQrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    private KhuVucRepository khuVucRepository;

    @Mock
    private BlacklistService blacklistService;

    @InjectMocks
    private ParkingService parkingService;

    private LoaiXe loaiXeMay;
    private LoaiXe loaiXeOTo;

    @BeforeEach
    void setUp() {
        loaiXeMay = new LoaiXe("Xe máy", 4000.0, 6000.0, 100000.0, "fa-motorcycle", "Xe máy");
        loaiXeMay.setId(1L);

        loaiXeOTo = new LoaiXe("Ô tô", 20000.0, 35000.0, 1200000.0, "fa-car", "Ô tô con");
        loaiXeOTo.setId(2L);
    }

    @Test
    @DisplayName("1. Check-in hợp lệ: Tạo vé thành công và ghi nhận thời gian máy chủ")
    void testCheckIn_ValidPlate_SuccessAndRecordsServerTime() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("29B1-123.45");
        request.setLoaiXeId(1L);

        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);
        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(anyString(), eq("Đang gửi")))
                .thenReturn(Optional.empty());
        when(veXeRepository.findByMaQR(anyString())).thenReturn(Optional.empty());
        when(monthlyTicketService.findActiveMonthlyPass(anyString(), anyLong())).thenReturn(Optional.empty());
        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(anyString(), eq("CHO_DEN")))
                .thenReturn(Optional.empty());
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime beforeTime = LocalDateTime.now().minusSeconds(1);
        VeXe result = parkingService.checkIn(request);
        LocalDateTime afterTime = LocalDateTime.now().plusSeconds(1);

        assertNotNull(result);
        assertEquals("29B1-123.45", result.getBienSoXe());
        assertEquals("Đang gửi", result.getTrangThai());
        assertNotNull(result.getMaQR());
        assertTrue(result.getMaQR().startsWith("QR-"));
        assertNotNull(result.getThoiGianVao());
        assertTrue(result.getThoiGianVao().isAfter(beforeTime) || result.getThoiGianVao().isEqual(beforeTime));
        assertTrue(result.getThoiGianVao().isBefore(afterTime) || result.getThoiGianVao().isEqual(afterTime));
        verify(veXeRepository, times(1)).save(any(VeXe.class));
    }

    @Test
    @DisplayName("2. Check-in thất bại: Biển số sai định dạng hoặc rỗng")
    void testCheckIn_InvalidPlate_ThrowsBadRequestException() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("BIENSO-KHONG-DUNG");
        request.setLoaiXeId(1L);

        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));

        assertThrows(BadRequestException.class, () -> parkingService.checkIn(request));
        verify(veXeRepository, never()).save(any(VeXe.class));
    }

    @Test
    @DisplayName("3. Check-in thất bại: Loại xe không tồn tại hoặc bỏ trống")
    void testCheckIn_MissingOrInvalidVehicleType_ThrowsBadRequestException() {
        CheckInRequest request1 = new CheckInRequest();
        request1.setBienSoXe("29B1-123.45");
        request1.setLoaiXeId(null);

        assertThrows(BadRequestException.class, () -> parkingService.checkIn(request1));

        CheckInRequest request2 = new CheckInRequest();
        request2.setBienSoXe("29B1-123.45");
        request2.setLoaiXeId(999L);
        when(loaiXeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> parkingService.checkIn(request2));
    }

    @Test
    @DisplayName("4. Check-in thất bại: Xe đã có lượt đang gửi trong bãi")
    void testCheckIn_AlreadyParked_ThrowsVehicleAlreadyParkedException() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("30A-999.99");
        request.setLoaiXeId(2L);

        when(loaiXeRepository.findById(2L)).thenReturn(Optional.of(loaiXeOTo));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai("30A99999", "Đang gửi")).thenReturn(true);

        assertThrows(VehicleAlreadyParkedException.class, () -> parkingService.checkIn(request));
        verify(veXeRepository, never()).save(any(VeXe.class));
    }

    @Test
    @DisplayName("5. Check-in thất bại: Xe trong Danh sách đen (Blacklist) bị từ chối")
    void testCheckIn_BlacklistedVehicle_RejectedAndThrowsBadRequestException() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("29A-888.88");
        request.setLoaiXeId(2L);

        when(loaiXeRepository.findById(2L)).thenReturn(Optional.of(loaiXeOTo));
        DanhSachDen blacklisted = new DanhSachDen("29A-888.88", "Nghi vấn trộm cắp", "CAO", "admin");
        when(blacklistService.checkBlacklist("29A-888.88")).thenReturn(Optional.of(blacklisted));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> parkingService.checkIn(request));
        assertTrue(ex.getMessage().contains("Danh sách đen"));
        verify(veXeRepository, never()).save(any(VeXe.class));
    }

    @Test
    @DisplayName("6. Check-in với vé tháng hợp lệ: Tự động gắn vé tháng và miễn phí")
    void testCheckIn_ValidMonthlyPass_AttachesTicketAndFree() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("29B1-555.55");
        request.setLoaiXeId(1L);

        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);

        VeThang veThang = new VeThang();
        veThang.setId(10L);
        veThang.setBienSoXe("29B1-555.55");
        veThang.setLoaiXe(loaiXeMay);
        veThang.setTrangThai("HOAT_DONG");
        veThang.setNgayHetHan(LocalDate.now().plusMonths(1));

        when(monthlyTicketService.findActiveMonthlyPass(eq("29B1-555.55"), eq(1L)))
                .thenReturn(Optional.of(veThang));
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeXe result = parkingService.checkIn(request);

        assertNotNull(result.getVeThang());
        assertEquals("VE_THANG", result.getHinhThucThanhToan());
        assertEquals("MIEN_PHI", result.getTrangThaiThanhToan());
    }

    @Test
    @DisplayName("7. Check-in với đặt chỗ trước (Pre-booking): Booking hợp lệ không tự khấu trừ cọc")
    void testCheckIn_WithValidBooking_LinksBookingAndNoAutoDepositDeduction() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("30A-111.22");
        request.setLoaiXeId(2L);

        when(loaiXeRepository.findById(2L)).thenReturn(Optional.of(loaiXeOTo));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);

        DatChoTruoc booking = new DatChoTruoc();
        booking.setId(20L);
        booking.setMaDatCho("BK-001");
        booking.setBienSoXe("30A-111.22");
        booking.setTrangThai("CHO_DEN");
        booking.setTienCoc(20000.0);
        booking.setThoiGianHetHan(LocalDateTime.now().plusHours(2));

        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc("30A-111.22", "CHO_DEN"))
                .thenReturn(Optional.of(booking));
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeXe result = parkingService.checkIn(request);

        assertNotNull(result.getDatChoTruoc());
        assertEquals(0.0, result.getTienCocKhauTru()); // Không tự ý khấu trừ tiền cọc khi chưa có giao dịch cọc
        assertEquals("DA_VAO", booking.getTrangThai()); // Booking được cập nhật trạng thái sau khi lưu vé
        verify(datChoTruocRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("8. Check-in với đặt chỗ trước đã hết hạn: Đổi sang HET_HAN và không gắn vào vé")
    void testCheckIn_WithExpiredBooking_MarksHetHanAndDoesNotLink() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("30A-333.44");
        request.setLoaiXeId(2L);

        when(loaiXeRepository.findById(2L)).thenReturn(Optional.of(loaiXeOTo));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);

        DatChoTruoc expiredBooking = new DatChoTruoc();
        expiredBooking.setId(21L);
        expiredBooking.setTrangThai("CHO_DEN");
        expiredBooking.setThoiGianHetHan(LocalDateTime.now().minusMinutes(30));

        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc("30A-333.44", "CHO_DEN"))
                .thenReturn(Optional.of(expiredBooking));
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeXe result = parkingService.checkIn(request);

        assertNull(result.getDatChoTruoc());
        assertEquals("HET_HAN", expiredBooking.getTrangThai());
        verify(datChoTruocRepository, times(1)).save(expiredBooking);
    }

    @Test
    @DisplayName("9. Preview Check-out: Báo lỗi khi vé đã ở trạng thái Đã ra")
    void testPreviewCheckOut_AlreadyExited_ThrowsInvalidTicketStateException() {
        String code = "QR-EXITED-123";
        VeXe veXe = new VeXe(code, "30A-123.45", loaiXeOTo, LocalDateTime.now().minusHours(4), "Đã ra");
        veXe.setThoiGianRa(LocalDateTime.now().minusHours(1));

        when(veXeRepository.findByMaQR(code)).thenReturn(Optional.of(veXe));

        assertThrows(InvalidTicketStateException.class, () -> parkingService.previewCheckOut(code));
    }

    @Test
    @DisplayName("10. Confirm Check-out: Biển số thực tế không khớp vé -> Báo lỗi kiểm tra thủ công")
    void testConfirmCheckOut_PlateMismatch_ThrowsBadRequestException() {
        VeXe veXe = new VeXe("QR-MISMATCH", "30A-123.45", loaiXeOTo, LocalDateTime.now().minusHours(1), "Đang gửi");
        veXe.setId(100);

        when(veXeRepository.findById(100)).thenReturn(Optional.of(veXe));

        // Biển số thực tế quan sát được là 51H-999.99 khác với vé 30A-123.45
        BadRequestException ex = assertThrows(BadRequestException.class, () -> {
            parkingService.confirmCheckOut(100, "TIEN_MAT", null, null, "51H-999.99", "nhanvien");
        });

        assertTrue(ex.getMessage().contains("KHÔNG KHỚP"));
        verify(veXeRepository, never()).save(any(VeXe.class));
    }

    @Test
    @DisplayName("11. Confirm Check-out Tiền mặt: Hoàn tất vé và ghi nhận người xác nhận")
    void testConfirmCheckOut_Cash_CompletesTicketAndRecordsStaff() {
        VeXe veXe = new VeXe("QR-CASH", "29B1-123.45", loaiXeMay, LocalDateTime.now().minusHours(2), "Đang gửi");
        veXe.setId(101);

        when(veXeRepository.findById(101)).thenReturn(Optional.of(veXe));

        FeeCalculationResult feeResult = new FeeCalculationResult();
        feeResult.setTongTienGoc(4000.0);
        feeResult.setSoTienThanhToan(4000.0);
        when(feeCalculationService.calculate(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(feeResult);
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeXe completed = parkingService.confirmCheckOut(101, "TIEN_MAT", null, "Thu tiền tại cổng", "29B1-123.45", "bao_ve_tuan");

        assertNotNull(completed);
        assertEquals("Đã ra", completed.getTrangThai());
        assertEquals("DA_THANH_TOAN", completed.getTrangThaiThanhToan());
        assertEquals(4000.0, completed.getSoTienThucThu());
        assertEquals("bao_ve_tuan", completed.getNguoiXacNhan());
    }

    @Test
    @DisplayName("12. Confirm Check-out Chuyển khoản: Trạng thái CHO_THANH_TOAN, không đóng vé cho đến khi nhân viên xác nhận")
    void testConfirmCheckOut_BankTransfer_KeepsDangGuiUntilStaffConfirmed() {
        VeXe veXe = new VeXe("QR-TRANSFER", "30A-555.66", loaiXeOTo, LocalDateTime.now().minusHours(1), "Đang gửi");
        veXe.setId(102);

        when(veXeRepository.findById(102)).thenReturn(Optional.of(veXe));

        FeeCalculationResult feeResult = new FeeCalculationResult();
        feeResult.setTongTienGoc(20000.0);
        feeResult.setSoTienThanhToan(20000.0);
        when(feeCalculationService.calculate(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(feeResult);
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Bước 1: Chọn chuyển khoản -> Vé vẫn là "Đang gửi" và chờ thanh toán
        VeXe pending = parkingService.confirmCheckOut(102, "CHUYEN_KHOAN", null, null, null, null);
        assertEquals("Đang gửi", pending.getTrangThai());
        assertEquals("CHO_THANH_TOAN", pending.getTrangThaiThanhToan());

        // Bước 2: Nhân viên xác nhận đã nhận được tiền -> Hoàn tất xuất bãi
        VeXe finalized = parkingService.xacNhanThanhToanChuyenKhoan(102, "BANK-TXN-123", "ketoan_mai");
        assertEquals("Đã ra", finalized.getTrangThai());
        assertEquals("DA_THANH_TOAN", finalized.getTrangThaiThanhToan());
        assertEquals("ketoan_mai", finalized.getNguoiXacNhan());
        assertEquals("BANK-TXN-123", finalized.getMaGiaoDich());
    }

    @Test
    @DisplayName("13. Preview và Confirm áp dụng cùng một kết quả tính phí từ FeeCalculationService")
    void testPreviewAndConfirm_UseIdenticalPricingImplementation() {
        String qr = "QR-PRICING-SAME";
        VeXe veXe = new VeXe(qr, "29B1-999.88", loaiXeMay, LocalDateTime.now().minusHours(3), "Đang gửi");
        veXe.setId(103);

        when(veXeRepository.findByMaQR(qr)).thenReturn(Optional.of(veXe));
        when(veXeRepository.findById(103)).thenReturn(Optional.of(veXe));

        FeeCalculationResult feeResult = new FeeCalculationResult();
        feeResult.setTongTienGoc(6000.0);
        feeResult.setSoTienThanhToan(6000.0);
        feeResult.setThoiLuongText("3 giờ 0 phút");
        feeResult.setQuyTacApDung("Biểu phí ban ngày");
        when(feeCalculationService.calculate(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(feeResult);
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CheckOutPreviewResponse preview = parkingService.previewCheckOut(qr);
        VeXe confirmed = parkingService.confirmCheckOut(103, "TIEN_MAT", null, null);

        assertEquals(preview.getSoTien(), confirmed.getChiPhi());
        verify(feeCalculationService, times(2)).calculate(any(), any(), any(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("14. Chặn xử lý Check-out hai lần cho cùng một vé")
    void testConfirmCheckOut_DoubleCheckOut_ThrowsInvalidTicketStateException() {
        VeXe veXe = new VeXe("QR-DOUBLE", "29B1-123.45", loaiXeMay, LocalDateTime.now().minusHours(1), "Đang gửi");
        veXe.setId(104);

        when(veXeRepository.findById(104)).thenReturn(Optional.of(veXe));

        FeeCalculationResult feeResult = new FeeCalculationResult();
        feeResult.setSoTienThanhToan(4000.0);
        when(feeCalculationService.calculate(any(), any(), any(), anyBoolean(), anyBoolean())).thenReturn(feeResult);
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> {
            VeXe v = invocation.getArgument(0);
            return v;
        });

        // Lần 1: Thành công
        VeXe firstExit = parkingService.confirmCheckOut(104, "TIEN_MAT", null, null);
        assertEquals("Đã ra", firstExit.getTrangThai());

        // Lần 2: Báo lỗi vì trạng thái không còn là "Đang gửi"
        assertThrows(InvalidTicketStateException.class, () -> {
            parkingService.confirmCheckOut(104, "TIEN_MAT", null, null);
        });
    }
}

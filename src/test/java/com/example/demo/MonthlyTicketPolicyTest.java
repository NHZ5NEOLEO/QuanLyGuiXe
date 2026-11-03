package com.example.demo;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.KhachHang;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.NguoiDung;
import com.example.demo.model.VeThang;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VeThangRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonthlyTicketPolicyTest {

    @Mock
    private VeXeRepository veXeRepository;

    @Mock
    private LoaiXeRepository loaiXeRepository;

    @Mock
    private VeThangRepository veThangRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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

    private MonthlyTicketService monthlyTicketService;
    private ParkingService parkingService;

    private LoaiXe loaiXeMay;
    private LoaiXe loaiXeOTo;
    private KhachHang khachHang;

    @BeforeEach
    void setUp() {
        monthlyTicketService = new MonthlyTicketService(
                veThangRepository,
                khachHangRepository,
                loaiXeRepository,
                nguoiDungRepository,
                passwordEncoder
        );

        parkingService = new ParkingService(
                veXeRepository,
                loaiXeRepository,
                monthlyTicketService,
                vietQrService,
                datChoTruocRepository,
                feeCalculationService,
                qrCodeService,
                khuVucRepository,
                blacklistService
        );

        KhuVuc kv2B = new KhuVuc("2B", "Khu Đỗ Xe 2 Bánh", null, 100, "Tầng hầm B1");
        kv2B.setId(1L);

        KhuVuc kv4B = new KhuVuc("4B", "Khu Đỗ Xe 4 Bánh", null, 20, "Tầng 1 ngoài trời");
        kv4B.setId(2L);

        loaiXeMay = new LoaiXe("Xe máy", 4000.0, 6000.0, 100000.0, "fa-motorcycle", "Xe máy");
        loaiXeMay.setId(1L);
        loaiXeMay.setKhuVuc(kv2B);

        loaiXeOTo = new LoaiXe("Ô tô", 20000.0, 35000.0, 1200000.0, "fa-car", "Ô tô");
        loaiXeOTo.setId(2L);
        loaiXeOTo.setKhuVuc(kv4B);

        khachHang = new KhachHang("Nguyễn Văn A", "0912345678", "001200001234", "a@example.com", "Hà Nội");
        khachHang.setId(1L);
    }

    @Test
    @DisplayName("Chính sách 2: Vé tháng còn hạn lúc check-in -> Lượt gửi được miễn phí")
    void testPolicy2_ActivePassAtCheckIn_EntitlementGrantedAndFree() {
        LocalDate today = LocalDate.now();
        VeThang vt = new VeThang("VT-1234", khachHang, "29B1-123.45", loaiXeMay, today.minusDays(5), today.plusDays(25), 100000.0, "HOAT_DONG");
        vt.setId(10L);

        CheckInRequest req = new CheckInRequest();
        req.setBienSoXe("29B1-123.45");
        req.setLoaiXeId(1L);

        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);
        when(veXeRepository.findByMaQR(anyString())).thenReturn(Optional.empty());
        when(veThangRepository.findByTrangThai("HOAT_DONG")).thenReturn(List.of(vt));
        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(anyString(), eq("CHO_DEN")))
                .thenReturn(Optional.empty());
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(inv -> inv.getArgument(0));

        VeXe checkInResult = parkingService.checkIn(req);

        assertNotNull(checkInResult);
        assertNotNull(checkInResult.getVeThang(), "VeXe phải lưu tham chiếu bền vững tới vé tháng lúc check-in");
        assertEquals("VT-1234", checkInResult.getVeThang().getMaThe());
        assertEquals("VE_THANG", checkInResult.getHinhThucThanhToan());
        assertEquals("MIEN_PHI", checkInResult.getTrangThaiThanhToan());
        assertEquals(0.0, checkInResult.getSoTienPhaiThu());
        assertEquals(0.0, checkInResult.getChiPhi());
    }

    @Test
    @DisplayName("Chính sách 3: Vé tháng hết hạn lúc check-in -> Tính phí vé thường")
    void testPolicy3_ExpiredPassAtCheckIn_TreatedAsRegularTicket() {
        LocalDate today = LocalDate.now();
        // Vé đã hết hạn hôm qua
        VeThang expiredPass = new VeThang("VT-OLD", khachHang, "29B1-123.45", loaiXeMay, today.minusMonths(2), today.minusDays(1), 100000.0, "HOAT_DONG");
        expiredPass.setId(11L);

        CheckInRequest req = new CheckInRequest();
        req.setBienSoXe("29B1-123.45");
        req.setLoaiXeId(1L);

        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);
        when(veXeRepository.findByMaQR(anyString())).thenReturn(Optional.empty());
        // findByTrangThai trả về vé nhưng vé đã hết hạn
        when(veThangRepository.findByTrangThai("HOAT_DONG")).thenReturn(List.of(expiredPass));
        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(anyString(), eq("CHO_DEN")))
                .thenReturn(Optional.empty());
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(inv -> inv.getArgument(0));

        VeXe checkInResult = parkingService.checkIn(req);

        assertNotNull(checkInResult);
        assertNull(checkInResult.getVeThang(), "Vé hết hạn lúc check-in không được gán quyền lợi vé tháng");
        assertEquals("TIEN_MAT", checkInResult.getHinhThucThanhToan());
        assertEquals("CHUA_THANH_TOAN", checkInResult.getTrangThaiThanhToan());
    }

    @Test
    @DisplayName("Chính sách 3: Vé tháng bị khóa (KHOA) lúc check-in -> Tính phí vé thường")
    void testPolicy3_LockedPassAtCheckIn_TreatedAsRegularTicket() {
        LocalDate today = LocalDate.now();
        // Vé còn hạn nhưng bị KHOA
        VeThang lockedPass = new VeThang("VT-LOCKED", khachHang, "29B1-123.45", loaiXeMay, today.minusDays(5), today.plusDays(25), 100000.0, "KHOA");
        lockedPass.setId(12L);

        CheckInRequest req = new CheckInRequest();
        req.setBienSoXe("29B1-123.45");
        req.setLoaiXeId(1L);

        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);
        when(veXeRepository.findByMaQR(anyString())).thenReturn(Optional.empty());
        when(veThangRepository.findByTrangThai("HOAT_DONG")).thenReturn(Collections.emptyList());
        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(anyString(), eq("CHO_DEN")))
                .thenReturn(Optional.empty());
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(inv -> inv.getArgument(0));

        VeXe checkInResult = parkingService.checkIn(req);

        assertNotNull(checkInResult);
        assertNull(checkInResult.getVeThang(), "Vé bị khóa lúc check-in không được áp dụng");
        assertEquals("TIEN_MAT", checkInResult.getHinhThucThanhToan());
        assertEquals("CHUA_THANH_TOAN", checkInResult.getTrangThaiThanhToan());
    }

    private FeeCalculationResult createFeeResult(double amount, String quyTac) {
        FeeCalculationResult r = new FeeCalculationResult();
        r.setTongTienGoc(amount);
        r.setSoTienThanhToan(amount);
        r.setPhuPhiLuuKho(0.0);
        r.setThoiLuongText("1 giờ");
        r.setQuyTacApDung(quyTac);
        return r;
    }

    @Test
    @DisplayName("Chính sách 2: Vé tháng hết hạn trong lúc xe còn trong bãi -> Lượt gửi vẫn miễn phí")
    void testPolicy2_PassExpiresWhileVehicleParked_StillFreeAtCheckOut() {
        LocalDate today = LocalDate.now();
        // Vé tháng hết hạn hôm nay hoặc hôm qua
        VeThang vt = new VeThang("VT-1234", khachHang, "29B1-123.45", loaiXeMay, today.minusDays(30), today.minusDays(1), 100000.0, "HOAT_DONG");
        vt.setId(10L);

        // VeXe đã được gán quyền vé tháng lúc vào bãi 3 ngày trước (khi vé còn hạn)
        VeXe veXe = new VeXe();
        veXe.setId(101);
        veXe.setMaQR("QR-MTH-01");
        veXe.setBienSoXe("29B1-123.45");
        veXe.setLoaiXe(loaiXeMay);
        veXe.setVeThang(vt);
        veXe.setHinhThucThanhToan("VE_THANG");
        veXe.setTrangThaiThanhToan("MIEN_PHI");
        veXe.setThoiGianVao(LocalDateTime.now().minusDays(3));
        veXe.setTrangThai("Đang gửi");

        when(veXeRepository.findByMaQR("QR-MTH-01")).thenReturn(Optional.of(veXe));
        when(veXeRepository.findById(101)).thenReturn(Optional.of(veXe));
        when(feeCalculationService.calculate(eq(loaiXeMay), any(LocalDateTime.class), any(LocalDateTime.class), eq(true), eq(false)))
                .thenReturn(createFeeResult(0.0, "Miễn phí (Vé tháng hợp lệ lúc check-in)"));
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Preview checkout
        CheckOutPreviewResponse preview = parkingService.previewCheckOut("QR-MTH-01");
        assertTrue(preview.isLaVeThang(), "Preview phải nhận diện là lượt vé tháng");
        assertEquals(0.0, preview.getSoTien(), "Chi phí phải là 0đ");

        // 2. Confirm checkout
        VeXe checkedOut = parkingService.confirmCheckOut(101, "VE_THANG", null, "Checkout test", null, "admin");
        assertEquals("Đã ra", checkedOut.getTrangThai());
        assertEquals(0.0, checkedOut.getChiPhi());
        assertEquals("MIEN_PHI", checkedOut.getTrangThaiThanhToan());
        assertEquals("VE_THANG", checkedOut.getHinhThucThanhToan());
    }

    @Test
    @DisplayName("Chính sách 4: Xe vào bãi không có vé tháng, mua vé tháng trước checkout -> Không miễn phí hồi tố")
    void testPolicy4_NoRetroactiveMonthlyPassAtCheckOut() {
        // VeXe vào bãi là vé lượt (veThang == null, hinhThucThanhToan == TIEN_MAT)
        VeXe veXe = new VeXe();
        veXe.setId(102);
        veXe.setMaQR("QR-REGULAR-02");
        veXe.setBienSoXe("29B1-999.99");
        veXe.setLoaiXe(loaiXeMay);
        veXe.setVeThang(null);
        veXe.setHinhThucThanhToan("TIEN_MAT");
        veXe.setTrangThaiThanhToan("CHUA_THANH_TOAN");
        veXe.setThoiGianVao(LocalDateTime.now().minusHours(4));
        veXe.setTrangThai("Đang gửi");

        when(veXeRepository.findByMaQR("QR-REGULAR-02")).thenReturn(Optional.of(veXe));
        when(veXeRepository.findById(102)).thenReturn(Optional.of(veXe));
        // Tại checkout, fee calculation nhận laVeThang = FALSE
        when(feeCalculationService.calculate(eq(loaiXeMay), any(LocalDateTime.class), any(LocalDateTime.class), eq(false), eq(false)))
                .thenReturn(createFeeResult(4000.0, "Biểu phí vé lượt"));
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(inv -> inv.getArgument(0));

        // Preview checkout
        CheckOutPreviewResponse preview = parkingService.previewCheckOut("QR-REGULAR-02");
        assertFalse(preview.isLaVeThang(), "Không được miễn phí hồi tố cho lượt gửi trước đó chưa có vé tháng");
        assertEquals(4000.0, preview.getSoTien());

        // Confirm checkout
        VeXe checkedOut = parkingService.confirmCheckOut(102, "TIEN_MAT", null, "Checkout test", null, "staff");
        assertEquals(4000.0, checkedOut.getChiPhi());
        assertEquals("TIEN_MAT", checkedOut.getHinhThucThanhToan());
        assertEquals("DA_THANH_TOAN", checkedOut.getTrangThaiThanhToan());
    }

    @Test
    @DisplayName("Chính sách: Preview và Confirm checkout nhất quán cùng 1 quy tắc tính phí")
    void testPreviewAndConfirmCheckOut_Consistency() {
        VeXe veXe = new VeXe();
        veXe.setId(103);
        veXe.setMaQR("QR-PREVIEW-CONSISTENT");
        veXe.setBienSoXe("30A-111.22");
        veXe.setLoaiXe(loaiXeOTo);
        veXe.setVeThang(null);
        veXe.setHinhThucThanhToan("TIEN_MAT");
        veXe.setTrangThaiThanhToan("CHUA_THANH_TOAN");
        veXe.setThoiGianVao(LocalDateTime.now().minusHours(2));
        veXe.setTrangThai("Đang gửi");

        when(veXeRepository.findByMaQR("QR-PREVIEW-CONSISTENT")).thenReturn(Optional.of(veXe));
        when(veXeRepository.findById(103)).thenReturn(Optional.of(veXe));
        when(feeCalculationService.calculate(eq(loaiXeOTo), any(LocalDateTime.class), any(LocalDateTime.class), eq(false), eq(false)))
                .thenReturn(createFeeResult(20000.0, "Vé lượt ban ngày"));
        when(veXeRepository.save(any(VeXe.class))).thenAnswer(inv -> inv.getArgument(0));

        CheckOutPreviewResponse preview = parkingService.previewCheckOut("QR-PREVIEW-CONSISTENT");
        assertEquals(20000.0, preview.getSoTien());

        VeXe confirmed = parkingService.confirmCheckOut(103, "TIEN_MAT", null, null, null, "admin");
        assertEquals(preview.getSoTien(), confirmed.getChiPhi(), "Số tiền xem trước và số tiền xác nhận lưu vào DB phải nhất quán");
    }

    @Test
    @DisplayName("Validation: Từ chối số tháng <= 0 hoặc > 24, biển số sai quy chuẩn, loại xe không tồn tại")
    void testRegistration_Validation_Failures() {
        // 1. Số tháng = 0
        assertThrows(BadRequestException.class, () ->
                monthlyTicketService.dangKyVeThang("A", "0912345678", "001", "a@b.c", "HN", "29B1-123.45", 1L, 0));

        // 2. Số tháng âm
        assertThrows(BadRequestException.class, () ->
                monthlyTicketService.dangKyVeThang("A", "0912345678", "001", "a@b.c", "HN", "29B1-123.45", 1L, -3));

        // 3. Số tháng > 24
        assertThrows(BadRequestException.class, () ->
                monthlyTicketService.dangKyVeThang("A", "0912345678", "001", "a@b.c", "HN", "29B1-123.45", 1L, 25));

        // 4. Loại xe không tồn tại
        when(loaiXeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () ->
                monthlyTicketService.dangKyVeThang("A", "0912345678", "001", "a@b.c", "HN", "29B1-123.45", 999L, 1));

        // 5. Biển số xe sai quy chuẩn
        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        assertThrows(BadRequestException.class, () ->
                monthlyTicketService.dangKyVeThang("A", "0912345678", "001", "a@b.c", "HN", "BIENSO_SAI_QUY_CHUAN", 1L, 1));
    }

    @Test
    @DisplayName("Chính sách 1 & Validation: Ngăn chặn tạo nhiều vé đang hoạt động cho cùng biển số")
    void testRegistration_PreventsDuplicateActivePassForSamePlate() {
        LocalDate today = LocalDate.now();
        VeThang existingActive = new VeThang("VT-EXISTING", khachHang, "29B1-123.45", loaiXeMay, today.minusDays(5), today.plusDays(25), 100000.0, "HOAT_DONG");

        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(veThangRepository.findByTrangThai("HOAT_DONG")).thenReturn(List.of(existingActive));

        // Đăng ký cho biển số cùng mã clean nhưng viết dạng khác: "29B112345"
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                monthlyTicketService.dangKyVeThang("Trần B", "0987654321", "002", "b@b.c", "HN", "29B1-123.45", 1L, 1));

        assertTrue(ex.getMessage().contains("đang có vé tháng đang hoạt động"), "Phải báo lỗi trùng biển số xe đang hoạt động");
    }

    @Test
    @DisplayName("Chính sách 6: Giá vé = giá tháng của loại xe x số tháng (Không giảm giá)")
    void testPolicy6_PricingCalculation() {
        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(veThangRepository.findByTrangThai("HOAT_DONG")).thenReturn(Collections.emptyList());
        when(khachHangRepository.findBySoDienThoai("0912345678")).thenReturn(Optional.of(khachHang));
        when(nguoiDungRepository.findByTenDangNhap("0912345678")).thenReturn(Optional.of(new NguoiDung()));
        when(veThangRepository.save(any(VeThang.class))).thenAnswer(inv -> inv.getArgument(0));

        VeThang vt3Months = monthlyTicketService.dangKyVeThang("A", "0912345678", "001", "a@b.c", "HN", "29B1-123.45", 1L, 3);
        assertEquals(300000.0, vt3Months.getGiaTien(), "3 tháng x 100,000 = 300,000đ (Không áp dụng chiết khấu/giảm giá)");
    }

    @Test
    @DisplayName("Chính sách 7: Gia hạn trước khi hết hạn -> Nối thời gian mới từ ngày hết hạn hiện tại")
    void testPolicy7_RenewalBeforeExpiry_ExtendsFromCurrentExpiry() {
        LocalDate currentExpiry = LocalDate.now().plusDays(10);
        VeThang vt = new VeThang("VT-RENEW", khachHang, "29B1-123.45", loaiXeMay, LocalDate.now().minusDays(20), currentExpiry, 100000.0, "HOAT_DONG");
        vt.setId(20L);

        when(veThangRepository.findById(20L)).thenReturn(Optional.of(vt));
        when(veThangRepository.save(any(VeThang.class))).thenAnswer(inv -> inv.getArgument(0));

        VeThang renewed = monthlyTicketService.giaHanVeThang(20L, 2);

        LocalDate expectedNewExpiry = currentExpiry.plusMonths(2);
        assertEquals(expectedNewExpiry, renewed.getNgayHetHan(), "Gia hạn trước hạn phải cộng tiếp 2 tháng từ ngày hết hạn cũ");
        assertEquals("HOAT_DONG", renewed.getTrangThai());
    }

    @Test
    @DisplayName("Chính sách 7: Gia hạn sau khi hết hạn -> Tính từ ngày gia hạn (hôm nay)")
    void testPolicy7_RenewalAfterExpiry_ExtendsFromRenewalDate() {
        LocalDate currentExpiry = LocalDate.now().minusDays(10); // Đã hết hạn 10 ngày trước
        VeThang vt = new VeThang("VT-RENEW-EXPIRED", khachHang, "29B1-123.45", loaiXeMay, LocalDate.now().minusMonths(2), currentExpiry, 100000.0, "HOAT_DONG");
        vt.setId(21L);

        when(veThangRepository.findById(21L)).thenReturn(Optional.of(vt));
        when(veThangRepository.save(any(VeThang.class))).thenAnswer(inv -> inv.getArgument(0));

        VeThang renewed = monthlyTicketService.giaHanVeThang(21L, 1);

        LocalDate today = LocalDate.now();
        assertEquals(today, renewed.getNgayBatDau(), "Gia hạn sau hạn thì ngày bắt đầu mới là ngày hôm nay");
        assertEquals(today.plusMonths(1), renewed.getNgayHetHan(), "Ngày hết hạn mới tính từ ngày hôm nay + 1 tháng");
        assertEquals("HOAT_DONG", renewed.getTrangThai());
    }
}

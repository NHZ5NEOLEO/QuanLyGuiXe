package com.example.demo;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.ParkingOverviewDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeThangRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.BlacklistService;
import com.example.demo.service.FeeCalculationService;
import com.example.demo.service.MonthlyTicketService;
import com.example.demo.service.ParkingLotService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingLotPolicyTest {

    @Mock
    private VeXeRepository veXeRepository;

    @Mock
    private LoaiXeRepository loaiXeRepository;

    @Mock
    private KhuVucRepository khuVucRepository;

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
    private BlacklistService blacklistService;

    @Mock
    private VeThangRepository veThangRepository;

    @InjectMocks
    private ParkingService parkingService;

    private ParkingLotService parkingLotService;

    private KhuVuc khu2B;
    private KhuVuc khu4B;
    private KhuVuc khuXD;
    private LoaiXe loaiXeMay;
    private LoaiXe loaiXeOTo;
    private LoaiXe loaiXeTai;
    private LoaiXe loaiXeChuaGanKhu;

    @BeforeEach
    void setUp() {
        parkingLotService = new ParkingLotService(khuVucRepository, loaiXeRepository, veXeRepository, veThangRepository);

        khu2B = new KhuVuc("2B", "Khu Đỗ Xe 2 Bánh", null, 100, "Tầng hầm B1");
        khu2B.setId(1L);

        khu4B = new KhuVuc("4B", "Khu Đỗ Xe 4 Bánh", null, 20, "Tầng 1 sảnh");
        khu4B.setId(2L);

        khuXD = new KhuVuc("XD", "Khu Xe Đạp", null, 30, "Sân phụ");
        khuXD.setId(3L);

        loaiXeMay = new LoaiXe("Xe máy", 4000.0, 6000.0, 100000.0, "fa-motorcycle", "Xe máy", khu2B);
        loaiXeMay.setId(1L);

        loaiXeOTo = new LoaiXe("Ô tô", 20000.0, 35000.0, 1200000.0, "fa-car", "Ô tô con", khu4B);
        loaiXeOTo.setId(2L);

        loaiXeTai = new LoaiXe("Xe tải nhỏ", 30000.0, 50000.0, 1800000.0, "fa-truck", "Xe bán tải", khu4B);
        loaiXeTai.setId(3L);

        loaiXeChuaGanKhu = new LoaiXe("Xe đặc chủng", 50000.0, 80000.0, 2000000.0, "fa-shuttle-van", "Chưa gán", null);
        loaiXeChuaGanKhu.setId(4L);
    }

    @Test
    @DisplayName("1. Check-in thành công khi khu vực còn chỗ: gán đúng KhuVuc, không gán ô đỗ giả lập")
    void testCheckIn_ValidZoneWithAvailableCapacity_SucceedsWithoutFakeSlot() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("29M1-999.99");
        request.setLoaiXeId(1L);

        when(loaiXeRepository.findById(1L)).thenReturn(Optional.of(loaiXeMay));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);
        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(anyString(), eq("Đang gửi")))
                .thenReturn(Optional.empty());
        when(veXeRepository.findByMaQR(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.countDangGuiByKhuVucId(1L)).thenReturn(50L); // 50/100 chỗ
        when(monthlyTicketService.findActiveMonthlyPass(anyString(), anyLong())).thenReturn(Optional.empty());
        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(anyString(), eq("CHO_DEN")))
                .thenReturn(Optional.empty());

        when(veXeRepository.save(any(VeXe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VeXe ticket = parkingService.checkIn(request);

        assertNotNull(ticket);
        assertEquals("29M1-999.99", ticket.getBienSoXe());
        assertNotNull(ticket.getKhuVuc(), "Vé xe phải được gán phân khu đỗ xe");
        assertEquals("2B", ticket.getKhuVuc().getMaKhu());
        assertNull(ticket.getViTriDo(), "Không được gán vi_tri_do_id hoặc ô đỗ giả lập");
        assertEquals("Khu Đỗ Xe 2 Bánh", ticket.getMaViTri());
    }

    @Test
    @DisplayName("2. Check-in bị từ chối khi loại xe chưa được cấu hình phân khu trong DB")
    void testCheckIn_VehicleTypeWithoutConfiguredZone_RejectsWithBadRequest() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("80A-000.01");
        request.setLoaiXeId(4L);

        when(loaiXeRepository.findById(4L)).thenReturn(Optional.of(loaiXeChuaGanKhu));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);
        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(anyString(), eq("Đang gửi")))
                .thenReturn(Optional.empty());
        when(veXeRepository.findByMaQR(anyString())).thenReturn(Optional.empty());
        when(khuVucRepository.findAll()).thenReturn(List.of(khu2B, khu4B, khuXD));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> parkingService.checkIn(request));
        assertTrue(ex.getMessage().contains("chưa được cấu hình phân khu"));
        verify(veXeRepository, never()).save(any(VeXe.class));
    }

    @Test
    @DisplayName("3. Check-in bị từ chối khi phân khu đã hết sức chứa: Báo rõ tên khu vực và sức chứa")
    void testCheckIn_ZoneAtCapacity_RejectsWithZoneNameAndCapacity() {
        CheckInRequest request = new CheckInRequest();
        request.setBienSoXe("30F-123.45");
        request.setLoaiXeId(2L); // Ô tô -> Khu 4B (sức chứa 20)

        when(loaiXeRepository.findById(2L)).thenReturn(Optional.of(loaiXeOTo));
        when(blacklistService.checkBlacklist(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.existsByBienSoCleanAndTrangThai(anyString(), eq("Đang gửi"))).thenReturn(false);
        when(veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(anyString(), eq("Đang gửi")))
                .thenReturn(Optional.empty());
        when(veXeRepository.findByMaQR(anyString())).thenReturn(Optional.empty());
        when(veXeRepository.countDangGuiByKhuVucId(2L)).thenReturn(20L); // 20/20 chỗ -> ĐẦY

        BadRequestException ex = assertThrows(BadRequestException.class, () -> parkingService.checkIn(request));
        assertTrue(ex.getMessage().contains("Khu Đỗ Xe 4 Bánh"));
        assertTrue(ex.getMessage().contains("20 chỗ"));
        assertTrue(ex.getMessage().contains("hết chỗ"));
        verify(veXeRepository, never()).save(any(VeXe.class));
    }

    @Test
    @DisplayName("4. Nhiều loại xe chia sẻ cùng một phân khu (Ô tô và Xe tải trong Khu 4B)")
    void testMultipleVehicleTypesInSameZone_ShareCapacityCorrectly() {
        assertEquals("4B", loaiXeOTo.getKhuVuc().getMaKhu());
        assertEquals("4B", loaiXeTai.getKhuVuc().getMaKhu());
        assertEquals(loaiXeOTo.getKhuVuc().getId(), loaiXeTai.getKhuVuc().getId());
    }

    @Test
    @DisplayName("5. Xe quá 24h là danh sách cảnh báo theo thời gian, KHÔNG tạo phân khu vật lý hay cộng sức chứa")
    void testOverdueVehicles_AreAlertOnly_NotPhysicalZoneNorCapacity() {
        when(khuVucRepository.findAll()).thenReturn(List.of(khu2B, khu4B, khuXD));

        VeXe v1 = new VeXe();
        v1.setId(101);
        v1.setBienSoXe("30A-999.99");
        v1.setLoaiXe(loaiXeOTo);
        v1.setKhuVuc(khu4B);
        v1.setThoiGianVao(LocalDateTime.now().minusHours(30)); // Đỗ 30 tiếng -> quá 24h
        v1.setTrangThai("Đang gửi");

        when(veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi")).thenReturn(List.of(v1));
        when(veThangRepository.findFirstByBienSoXe(anyString())).thenReturn(Optional.empty());

        ParkingOverviewDto overview = parkingLotService.layTongQuanBaiDoXe();

        // Tổng sức chứa = 100 (2B) + 20 (4B) + 30 (XD) = 150
        assertEquals(150, overview.getTotalCapacity(), "Sức chứa chỉ tính từ các khu vật lý, không cộng khu 24H giả lập");
        assertEquals(1, overview.getOverdueCount(), "Có 1 xe quá 24h trong danh sách cảnh báo");
        assertEquals(1, overview.getOverdueVehicles().size());
        assertEquals("30A-999.99", overview.getOverdueVehicles().get(0).getBienSoXe());

        // Danh sách phân khu chỉ gồm 3 phân khu vật lý (không có khu 24H)
        assertEquals(3, overview.getZones().size());
        assertTrue(overview.getZones().stream().noneMatch(z -> "24H".equalsIgnoreCase(z.getMaKhu())));
    }

    @Test
    @DisplayName("6. Hệ thống không tự tạo danh sách ô đỗ giả lập tuần tự (layTatCaViTriDo trả về rỗng)")
    void testParkingLotService_LayTatCaViTriDo_DoesNotGenerateFakeSequentialSlots() {
        List<?> slots = parkingLotService.layTatCaViTriDo();
        assertTrue(slots.isEmpty(), "Không được sinh danh sách ô đỗ giả lập K-01, K-02");
    }
}


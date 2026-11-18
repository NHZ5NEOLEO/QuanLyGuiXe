package com.example.demo.controller;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.model.DanhSachDen;
import com.example.demo.model.KhachHang;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeThang;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.BlacklistService;
import com.example.demo.service.MonthlyTicketService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.ParkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApiGateControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ParkingService parkingService;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private MonthlyTicketService monthlyTicketService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DatChoTruocRepository datChoTruocRepository;

    @Mock
    private VeXeRepository veXeRepository;

    @InjectMocks
    private ApiGateController apiGateController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(apiGateController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("1. GET /api/gate/check-plate: Nhận diện biển số xe trong danh sách đen và gửi cảnh báo")
    void testCheckPlate_Blacklisted() throws Exception {
        DanhSachDen black = new DanhSachDen("30A-999.99", "Xe bị truy nã", "NGUY_HIEM", "admin");
        when(blacklistService.checkBlacklist("30A-999.99")).thenReturn(Optional.of(black));
        when(monthlyTicketService.findActiveMonthlyPass("30A-999.99")).thenReturn(Optional.empty());
        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc("30A-999.99", "CHO_DEN"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/gate/check-plate").param("plate", "30A-999.99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBlacklisted").value(true))
                .andExpect(jsonPath("$.blacklistReason").value("Xe bị truy nã"))
                .andExpect(jsonPath("$.blacklistLevel").value("NGUY_HIEM"))
                .andExpect(jsonPath("$.isMonthlyPass").value(false));

        verify(notificationService, times(1)).sendTelegramSecurityAlert(anyString(), anyString());
    }

    @Test
    @DisplayName("2. GET /api/gate/check-plate: Nhận diện xe có thuê bao tháng hợp lệ")
    void testCheckPlate_MonthlyPass() throws Exception {
        LoaiXe loaiXe = new LoaiXe("Ô tô", 20000.0, 35000.0, 1200000.0, "fa-car", "Ô tô");
        loaiXe.setId(2L);
        KhachHang kh = new KhachHang("Nguyễn Văn A", "0912345678", "001", "a@b.c", "Hà Nội");
        VeThang vt = new VeThang("VT-001", kh, "30A-123.45", loaiXe, LocalDate.now().minusDays(5), LocalDate.now().plusDays(25), 1200000.0, "HOAT_DONG");

        when(blacklistService.checkBlacklist("30A-123.45")).thenReturn(Optional.empty());
        when(monthlyTicketService.findActiveMonthlyPass("30A-123.45")).thenReturn(Optional.of(vt));
        when(datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc("30A-123.45", "CHO_DEN"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/gate/check-plate").param("plate", "30A-123.45"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBlacklisted").value(false))
                .andExpect(jsonPath("$.isMonthlyPass").value(true))
                .andExpect(jsonPath("$.customerName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.vehicleTypeId").value(2));
    }

    @Test
    @DisplayName("3. POST /api/gate/check-in: Tiếp nhận xe vào thành công")
    void testCheckIn_Success() throws Exception {
        CheckInRequest req = new CheckInRequest();
        req.setBienSoXe("29B1-123.45");
        req.setLoaiXeId(1L);

        KhuVuc kv = new KhuVuc("2B", "Khu Đỗ Xe 2 Bánh", null, 100, "B1");
        VeXe ticket = new VeXe();
        ticket.setId(201);
        ticket.setMaQR("QR-201");
        ticket.setBienSoXe("29B1-123.45");
        ticket.setThoiGianVao(LocalDateTime.now());
        ticket.setKhuVuc(kv);

        when(parkingService.checkIn(any(CheckInRequest.class))).thenReturn(ticket);

        mockMvc.perform(post("/api/gate/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.ticketId").value(201))
                .andExpect(jsonPath("$.maQR").value("QR-201"))
                .andExpect(jsonPath("$.bienSoXe").value("29B1-123.45"));
    }

    @Test
    @DisplayName("4. GET /api/gate/check-out/preview: Xem trước chi phí xe ra")
    void testPreviewCheckOut_Success() throws Exception {
        CheckOutPreviewResponse preview = new CheckOutPreviewResponse();
        preview.setVeXeId(301);
        preview.setBienSoXe("30A-888.88");
        preview.setSoTien(20000.0);
        preview.setThoiLuong("1 giờ 15 phút");

        when(parkingService.previewCheckOut("QR-301")).thenReturn(preview);

        mockMvc.perform(get("/api/gate/check-out/preview").param("query", "QR-301"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veXeId").value(301))
                .andExpect(jsonPath("$.soTien").value(20000.0));
    }

    @Test
    @DisplayName("5. POST /api/gate/check-out/confirm: Xác nhận hoàn tất lượt gửi bằng tiền mặt")
    void testConfirmCheckOut_Cash_Success() throws Exception {
        VeXe veXe = new VeXe();
        veXe.setId(401);
        veXe.setBienSoXe("51H-123.45");
        veXe.setChiPhi(4000.0);
        veXe.setTrangThai("Đã ra");
        veXe.setTrangThaiThanhToan("DA_THANH_TOAN");
        veXe.setHinhThucThanhToan("TIEN_MAT");

        when(parkingService.confirmCheckOut(eq(401), eq("TIEN_MAT"), isNull(), isNull(), isNull(), anyString()))
                .thenReturn(veXe);

        Map<String, Object> payload = Map.of(
                "veXeId", 401,
                "hinhThucThanhToan", "TIEN_MAT"
        );

        mockMvc.perform(post("/api/gate/check-out/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.ticketId").value(401))
                .andExpect(jsonPath("$.trangThai").value("Đã ra"))
                .andExpect(jsonPath("$.trangThaiThanhToan").value("DA_THANH_TOAN"));
    }

    @Test
    @DisplayName("6. GET /api/gate/check-qr-paid: Polling trạng thái thanh toán vé xe")
    void testCheckQrPaid_FoundAndPaid() throws Exception {
        VeXe veXe = new VeXe();
        veXe.setId(501);
        veXe.setBienSoXe("29A-111.22");
        veXe.setTrangThaiThanhToan("DA_THANH_TOAN");
        veXe.setTrangThai("Đã ra");
        veXe.setChiPhi(20000.0);

        when(veXeRepository.findById(501)).thenReturn(Optional.of(veXe));

        mockMvc.perform(get("/api/gate/check-qr-paid").param("veXeId", "501"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.pending").value(false))
                .andExpect(jsonPath("$.chiPhi").value(20000.0));
    }

    @Test
    @DisplayName("7. POST /api/gate/confirm-bank-transfer: Nhân viên xác nhận đã nhận chuyển khoản VietQR")
    void testConfirmBankTransfer_Success() throws Exception {
        VeXe veXe = new VeXe();
        veXe.setId(601);
        veXe.setBienSoXe("30A-666.77");
        veXe.setChiPhi(35000.0);
        veXe.setThoiGianRa(LocalDateTime.now());
        veXe.setTrangThai("Đã ra");
        veXe.setTrangThaiThanhToan("DA_THANH_TOAN");

        when(parkingService.xacNhanThanhToanChuyenKhoan(eq(601), anyString(), anyString()))
                .thenReturn(veXe);

        Map<String, Object> payload = Map.of(
                "veXeId", 601,
                "maGiaoDich", "BANK-TXN-999"
        );

        mockMvc.perform(post("/api/gate/confirm-bank-transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.paid").value(true))
                .andExpect(jsonPath("$.ticketId").value(601));
    }
}

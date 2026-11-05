package com.example.demo;

import com.example.demo.controller.PortalController;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PortalVehicleLookupPrivacyTest {

    private MockMvc mockMvc;

    @Mock
    private LoaiXeRepository loaiXeRepository;
    @Mock
    private VeXeRepository veXeRepository;
    @Mock
    private VeThangRepository veThangRepository;
    @Mock
    private KhuVucRepository khuVucRepository;
    @Mock
    private KhachHangRepository khachHangRepository;
    @Mock
    private NguoiDungRepository nguoiDungRepository;
    @Mock
    private com.example.demo.service.MonthlyTicketService monthlyTicketService;
    @Mock
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private PortalController portalController;

    private LoaiXe xeMay;
    private VeXe activeTicket;
    private KhachHang khachHang;
    private NguoiDung userKhachHang;
    private VeThang veThangKhach;

    @BeforeEach
    void setUp() {
        portalController = new PortalController(
                loaiXeRepository,
                veXeRepository,
                veThangRepository,
                khuVucRepository,
                khachHangRepository,
                nguoiDungRepository,
                monthlyTicketService,
                jdbcTemplate
        );

        mockMvc = MockMvcBuilders.standaloneSetup(portalController).build();

        xeMay = new LoaiXe("Xe máy", 4000.0, 6000.0, 100000.0, "fa-motorcycle", "Xe số, xe ga");

        activeTicket = new VeXe();
        activeTicket.setId(101);
        activeTicket.setMaQR("QR-261018172300-19C4");
        activeTicket.setBienSoXe("33M2-5678");
        activeTicket.setTrangThai("Đang gửi");
        activeTicket.setThoiGianVao(LocalDateTime.now().minusHours(2));
        activeTicket.setLoaiXe(xeMay);

        khachHang = new KhachHang("Nguyễn Văn An", "0912345678", "001200001234", "an.nv@gmail.com", "Hà Nội");
        khachHang.setId(1L);

        userKhachHang = new NguoiDung("khachhang", "pass", "Nguyễn Văn An", "KHACH_HANG", "0912345678");
        userKhachHang.setId(10L);

        veThangKhach = new VeThang("VT-88888888", khachHang, "29B1-88888", xeMay, LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(5), 600000.0, "HOAT_DONG");
        veThangKhach.setId(501L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Khách chưa đăng nhập tra cứu biển số -> Mã vé điện tử bị che bảo mật")
    void testLookupUnauthenticated_MasksTicketCode() throws Exception {
        when(veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi"))
                .thenReturn(List.of(activeTicket));

        mockMvc.perform(get("/portal/api/lookup").param("plate", "33M2-5678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.dangGui").value(true))
                .andExpect(jsonPath("$.canViewFullTicket").value(false))
                .andExpect(jsonPath("$.isUserRegisteredPlate").value(false))
                .andExpect(jsonPath("$.maQR").value("QR-••••••••••••"));
    }

    @Test
    @DisplayName("Khách hàng đăng nhập tra cứu đúng biển số vé tháng của mình -> Hiện mã vé và thông báo xác thực")
    void testLookupCustomer_OwnMonthlyPass_ShowsFullTicketCode() throws Exception {
        // Giả lập đăng nhập tài khoản khachhang
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("khachhang", "pass", List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
        );

        when(nguoiDungRepository.findByTenDangNhap("khachhang")).thenReturn(Optional.of(userKhachHang));
        when(khachHangRepository.findBySoDienThoai("0912345678")).thenReturn(Optional.of(khachHang));
        when(veThangRepository.findByKhachHangIdOrderByNgayBatDauDesc(1L)).thenReturn(List.of(veThangKhach));

        // Vé xe đang gửi của chính biển số vé tháng 29B1-88888
        VeXe ownActiveTicket = new VeXe();
        ownActiveTicket.setId(102);
        ownActiveTicket.setMaQR("QR-29B188888-ABCD");
        ownActiveTicket.setBienSoXe("29B1-88888");
        ownActiveTicket.setTrangThai("Đang gửi");
        ownActiveTicket.setThoiGianVao(LocalDateTime.now().minusHours(1));
        ownActiveTicket.setLoaiXe(xeMay);

        when(veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi")).thenReturn(List.of(ownActiveTicket));
        when(veThangRepository.findByTrangThai("HOAT_DONG")).thenReturn(List.of(veThangKhach));

        mockMvc.perform(get("/portal/api/lookup").param("plate", "29B1-88888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.canViewFullTicket").value(true))
                .andExpect(jsonPath("$.isUserRegisteredPlate").value(true))
                .andExpect(jsonPath("$.maQR").value("QR-29B188888-ABCD"))
                .andExpect(jsonPath("$.userPlateNotice").value("Đây là biển số xe mà bạn đã đăng ký vé tháng"));
    }

    @Test
    @DisplayName("Khách hàng đăng nhập nhưng tra cứu xe người khác -> Mã vé vẫn bị che bảo mật")
    void testLookupCustomer_OtherVehicle_MasksTicketCode() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("khachhang", "pass", List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
        );

        when(nguoiDungRepository.findByTenDangNhap("khachhang")).thenReturn(Optional.of(userKhachHang));
        when(khachHangRepository.findBySoDienThoai("0912345678")).thenReturn(Optional.of(khachHang));
        when(veThangRepository.findByKhachHangIdOrderByNgayBatDauDesc(1L)).thenReturn(List.of(veThangKhach));

        when(veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi")).thenReturn(List.of(activeTicket));

        mockMvc.perform(get("/portal/api/lookup").param("plate", "33M2-5678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.canViewFullTicket").value(false))
                .andExpect(jsonPath("$.isUserRegisteredPlate").value(false))
                .andExpect(jsonPath("$.maQR").value("QR-••••••••••••"));
    }

    @Test
    @DisplayName("Nhân viên bảo vệ đăng nhập tra cứu -> Có quyền xem mã vé đầy đủ")
    void testLookupStaff_ShowsFullTicketCode() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("baove", "123456", List.of(new SimpleGrantedAuthority("ROLE_NHAN_VIEN")))
        );

        when(veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi")).thenReturn(List.of(activeTicket));

        mockMvc.perform(get("/portal/api/lookup").param("plate", "33M2-5678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.canViewFullTicket").value(true))
                .andExpect(jsonPath("$.maQR").value("QR-261018172300-19C4"));
    }
}

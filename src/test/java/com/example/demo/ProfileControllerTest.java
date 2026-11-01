package com.example.demo;

import com.example.demo.controller.ProfileController;
import com.example.demo.model.KhachHang;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.NguoiDung;
import com.example.demo.model.VeThang;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VeThangRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private VeThangRepository veThangRepository;

    private PasswordEncoder passwordEncoder;
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        profileController = new ProfileController(
                nguoiDungRepository,
                khachHangRepository,
                veThangRepository,
                passwordEncoder
        );

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /tai-khoan khi chưa đăng nhập -> Chuyển hướng sang /login")
    void testProfilePageUnauthenticated() throws Exception {
        mockMvc.perform(get("/tai-khoan"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /tai-khoan khi đã đăng nhập -> Hiển thị view portal/ho-so cùng thông tin và vé tháng")
    void testProfilePageAuthenticated() throws Exception {
        NguoiDung user = new NguoiDung("testuser", passwordEncoder.encode("123456"), "Nguyen Van Test", "KHACH_HANG", "0912345678");
        user.setId(10L);

        KhachHang customer = new KhachHang("Nguyen Van Test", "0912345678", "001201000123", "test@domain.com", "Hà Nội");
        customer.setId(20L);

        LoaiXe loaiXe = new LoaiXe();
        loaiXe.setTenLoai("Xe máy");

        VeThang ticket = new VeThang("VT-TEST01", customer, "29A-12345", loaiXe, LocalDate.now(), LocalDate.now().plusMonths(1), 100000.0, "HOAT_DONG");

        when(nguoiDungRepository.findByTenDangNhap("testuser")).thenReturn(Optional.of(user));
        when(khachHangRepository.findBySoDienThoai("0912345678")).thenReturn(Optional.of(customer));
        when(veThangRepository.findByKhachHangIdOrderByNgayBatDauDesc(20L)).thenReturn(List.of(ticket));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
        );

        mockMvc.perform(get("/tai-khoan"))
                .andExpect(status().isOk())
                .andExpect(view().name("portal/ho-so"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attributeExists("tickets"))
                .andExpect(model().attribute("activeTicketsCount", 1L))
                .andExpect(model().attribute("totalTicketsCount", 1));
    }

    @Test
    @DisplayName("POST /tai-khoan/cap-nhat thành công -> Cập nhật NguoiDung, KhachHang và Session")
    void testUpdateProfileSuccess() throws Exception {
        NguoiDung user = new NguoiDung("testuser", passwordEncoder.encode("123456"), "Old Name", "KHACH_HANG", "0912345678");
        user.setId(10L);

        KhachHang customer = new KhachHang("Old Name", "0912345678", "001201000123", "old@domain.com", "Hà Nội");
        customer.setId(20L);

        when(nguoiDungRepository.findByTenDangNhap("testuser")).thenReturn(Optional.of(user));
        when(khachHangRepository.findBySoDienThoai("0912345678")).thenReturn(Optional.of(customer));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
        );

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/tai-khoan/cap-nhat")
                        .session(session)
                        .param("hoTen", "Nguyen Van Moi")
                        .param("soDienThoai", "0987654321")
                        .param("email", "new@domain.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tai-khoan#tab-info"))
                .andExpect(flash().attribute("successMessage", "Cập nhật thông tin cá nhân thành công!"));

        assertEquals("Nguyen Van Moi", user.getHoTen());
        assertEquals("0987654321", user.getSoDienThoai());
        verify(nguoiDungRepository, times(1)).save(user);
        verify(khachHangRepository, times(1)).save(customer);
        assertEquals("Nguyen Van Moi", session.getAttribute("userName"));
    }

    @Test
    @DisplayName("POST /tai-khoan/cap-nhat lỗi khi số điện thoại mới đã bị người khác sử dụng")
    void testUpdateProfileDuplicatePhone() throws Exception {
        NguoiDung currentUser = new NguoiDung("testuser", passwordEncoder.encode("123456"), "Nguyen Test", "KHACH_HANG", "0912345678");
        currentUser.setId(10L);

        NguoiDung anotherUser = new NguoiDung("otheruser", "pwd", "Other Person", "KHACH_HANG", "0988888888");
        anotherUser.setId(99L);

        when(nguoiDungRepository.findByTenDangNhap("testuser")).thenReturn(Optional.of(currentUser));
        when(nguoiDungRepository.findBySoDienThoai("0988888888")).thenReturn(Optional.of(anotherUser));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
        );

        mockMvc.perform(post("/tai-khoan/cap-nhat")
                        .param("hoTen", "Nguyen Test")
                        .param("soDienThoai", "0988888888"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tai-khoan#tab-info"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(nguoiDungRepository, never()).save(currentUser);
    }

    @Test
    @DisplayName("POST /tai-khoan/doi-mat-khau thành công khi mật khẩu hiện tại đúng")
    void testChangePasswordSuccess() throws Exception {
        String rawOldPassword = "oldPassword123";
        NguoiDung user = new NguoiDung("testuser", passwordEncoder.encode(rawOldPassword), "Nguyen Test", "KHACH_HANG", "0912345678");

        when(nguoiDungRepository.findByTenDangNhap("testuser")).thenReturn(Optional.of(user));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
        );

        mockMvc.perform(post("/tai-khoan/doi-mat-khau")
                        .param("matKhauHienTai", rawOldPassword)
                        .param("matKhauMoi", "newSecret999")
                        .param("xacNhanMatKhau", "newSecret999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tai-khoan#tab-password"))
                .andExpect(flash().attribute("passwordSuccess", "Đổi mật khẩu thành công! Mật khẩu mới đã được kích hoạt."));

        assertTrue(passwordEncoder.matches("newSecret999", user.getMatKhau()));
        verify(nguoiDungRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("POST /tai-khoan/doi-mat-khau thất bại khi sai mật khẩu hiện tại")
    void testChangePasswordWrongCurrent() throws Exception {
        NguoiDung user = new NguoiDung("testuser", passwordEncoder.encode("correctPassword"), "Nguyen Test", "KHACH_HANG", "0912345678");

        when(nguoiDungRepository.findByTenDangNhap("testuser")).thenReturn(Optional.of(user));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
        );

        mockMvc.perform(post("/tai-khoan/doi-mat-khau")
                        .param("matKhauHienTai", "wrongPassword")
                        .param("matKhauMoi", "newSecret999")
                        .param("xacNhanMatKhau", "newSecret999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tai-khoan#tab-password"))
                .andExpect(flash().attribute("passwordError", "Mật khẩu hiện tại không chính xác!"));

        verify(nguoiDungRepository, never()).save(user);
    }

    @Test
    @DisplayName("POST /tai-khoan/doi-mat-khau thất bại khi xác nhận mật khẩu không khớp")
    void testChangePasswordMismatchConfirm() throws Exception {
        String rawOldPassword = "oldPassword123";
        NguoiDung user = new NguoiDung("testuser", passwordEncoder.encode(rawOldPassword), "Nguyen Test", "KHACH_HANG", "0912345678");

        when(nguoiDungRepository.findByTenDangNhap("testuser")).thenReturn(Optional.of(user));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of(new SimpleGrantedAuthority("ROLE_KHACH_HANG")))
        );

        mockMvc.perform(post("/tai-khoan/doi-mat-khau")
                        .param("matKhauHienTai", rawOldPassword)
                        .param("matKhauMoi", "newSecret999")
                        .param("xacNhanMatKhau", "mismatch123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tai-khoan#tab-password"))
                .andExpect(flash().attribute("passwordError", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!"));

        verify(nguoiDungRepository, never()).save(user);
    }
}

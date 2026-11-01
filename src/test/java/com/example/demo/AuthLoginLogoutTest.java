package com.example.demo;

import com.example.demo.controller.AuthController;
import com.example.demo.controller.AuthRestController;
import com.example.demo.model.NguoiDung;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthLoginLogoutTest {

    private MockMvc authMvc;
    private MockMvc restMvc;
    private ObjectMapper objectMapper;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    private PasswordEncoder passwordEncoder;

    private AuthController authController;
    private AuthRestController authRestController;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        objectMapper = new ObjectMapper();

        authController = new AuthController(nguoiDungRepository, khachHangRepository, passwordEncoder);
        authRestController = new AuthRestController(nguoiDungRepository, khachHangRepository, passwordEncoder);

        org.springframework.web.servlet.view.InternalResourceViewResolver viewResolver = new org.springframework.web.servlet.view.InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        authMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
        restMvc = MockMvcBuilders.standaloneSetup(authRestController).build();

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("1. Truy cập trang đăng nhập khi chưa đăng nhập trả về view dang-nhap")
    void testLoginPage_Unauthenticated_ReturnsDangNhapView() throws Exception {
        authMvc.perform(get("/dang-nhap"))
                .andExpect(status().isOk())
                .andExpect(view().name("dang-nhap"));

        authMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("dang-nhap"));
    }

    @Test
    @DisplayName("2. Trang đăng nhập hiển thị thông báo lỗi khi có param error")
    void testLoginPage_WithErrorParam_SetsErrorMessage() throws Exception {
        authMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("dang-nhap"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("3. Trang đăng nhập hiển thị thông báo khi có param logout")
    void testLoginPage_WithLogoutParam_SetsSuccessMessage() throws Exception {
        authMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("dang-nhap"))
                .andExpect(model().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("4. Nếu đã đăng nhập với vai trò ADMIN, truy cập /login được tự động chuyển hướng tới /tong-quan")
    void testLoginPage_AlreadyAuthenticatedAdmin_RedirectsToDashboard() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        authMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tong-quan"));
    }

    @Test
    @DisplayName("5. Nếu đã đăng nhập với vai trò NHAN_VIEN, truy cập /login được tự động chuyển hướng tới /gui-xe")
    void testLoginPage_AlreadyAuthenticatedStaff_RedirectsToGuiXe() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "baove", null, List.of(new SimpleGrantedAuthority("ROLE_NHAN_VIEN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        authMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gui-xe"));
    }

    @Test
    @DisplayName("6. Xử lý đăng nhập POST /dang-nhap thành công thiết lập session và chuyển hướng đúng vai trò")
    void testHandleLoginPost_ValidCredentials_Success() throws Exception {
        String rawPass = "123456";
        String encoded = passwordEncoder.encode(rawPass);
        NguoiDung staff = new NguoiDung("baove", encoded, "Nguyễn Bảo Vệ", "NHAN_VIEN", "0909000222");

        when(nguoiDungRepository.findByTenDangNhap("baove")).thenReturn(Optional.of(staff));

        MockHttpSession session = new MockHttpSession();

        authMvc.perform(post("/dang-nhap")
                        .param("username", "baove")
                        .param("password", rawPass)
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gui-xe"));

        assertEquals("Nguyễn Bảo Vệ", session.getAttribute("userName"));
        assertEquals("NHAN_VIEN", session.getAttribute("userRole"));
    }

    @Test
    @DisplayName("7. Xử lý đăng nhập POST /dang-nhap sai mật khẩu chuyển hướng về /dang-nhap?error=true")
    void testHandleLoginPost_WrongPassword_RedirectsWithError() throws Exception {
        String encoded = passwordEncoder.encode("correctPass");
        NguoiDung user = new NguoiDung("baove", encoded, "Nguyễn Bảo Vệ", "NHAN_VIEN", "0909000222");

        when(nguoiDungRepository.findByTenDangNhap("baove")).thenReturn(Optional.of(user));

        authMvc.perform(post("/dang-nhap")
                        .param("username", "baove")
                        .param("password", "wrongPass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dang-nhap?error=true"));
    }

    @Test
    @DisplayName("8. Đăng xuất GET và POST (/logout và /dang-xuat) hủy session và chuyển hướng về /login?logout=true")
    void testLogout_BothEndpointsAndMethods_InvalidatesSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userName", "Admin");

        authMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));

        assertTrue(session.isInvalid());

        MockHttpSession session2 = new MockHttpSession();
        authMvc.perform(get("/dang-xuat").session(session2))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));

        assertTrue(session2.isInvalid());
    }

    @Test
    @DisplayName("9. API POST /api/auth/login xác thực thành công và trả về thông tin người dùng")
    void testApiLogin_Success() throws Exception {
        String rawPass = "admin123";
        String encoded = passwordEncoder.encode(rawPass);
        NguoiDung admin = new NguoiDung("admin", encoded, "Quản Trị Viên", "ADMIN", "0909000111");

        when(nguoiDungRepository.findByTenDangNhap("admin")).thenReturn(Optional.of(admin));

        Map<String, String> creds = Map.of("username", "admin", "password", rawPass);

        restMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.fullName").value("Quản Trị Viên"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.redirectUrl").value("/tong-quan"));
    }

    @Test
    @DisplayName("10. API /api/auth/logout hủy session và trả về kết quả thành công")
    void testApiLogout_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userName", "TestUser");

        restMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đăng xuất thành công!"));

        assertTrue(session.isInvalid());
    }
}

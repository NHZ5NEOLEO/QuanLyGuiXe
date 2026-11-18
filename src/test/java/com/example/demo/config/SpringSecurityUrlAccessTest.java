package com.example.demo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SpringSecurityUrlAccessTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("1. Khách vãng lai (Chưa đăng nhập) được phép truy cập các trang công khai và API tra cứu")
    void testPublicEndpoints_AllowUnauthenticated() throws Exception {
        mockMvc.perform(get("/portal")).andExpect(status().isOk());
        mockMvc.perform(get("/api/gate/check-plate").param("plate", "29B1-12345"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. Khách vãng lai (Chưa đăng nhập) bị chặn khi truy cập trang nội bộ (chuyển hướng /login)")
    void testProtectedEndpoints_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/tong-quan")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/gui-xe")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
        mockMvc.perform(get("/ve-xe")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "khachhang_test", roles = "KHACH_HANG")
    @DisplayName("3. Vai trò KHÁCH HÀNG (KHACH_HANG) bị chặn khi cố truy cập trang vận hành/quản trị (accessDeniedHandler điều hướng an toàn về /dang-ky-ve-thang)")
    void testKhachHang_CannotAccessOperationalAndAdminPages() throws Exception {
        // Chặn truy cập trang tổng quan Admin
        mockMvc.perform(get("/tong-quan"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dang-ky-ve-thang"));

        // Chặn truy cập trang quản lý nhân viên
        mockMvc.perform(get("/nhan-vien"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dang-ky-ve-thang"));

        // Chặn truy cập trang trực cổng
        mockMvc.perform(get("/gui-xe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dang-ky-ve-thang"));

        // Chặn truy cập trang quản lý vé xe
        mockMvc.perform(get("/ve-xe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dang-ky-ve-thang"));
    }

    @Test
    @WithMockUser(username = "baove_test", roles = "NHAN_VIEN")
    @DisplayName("4. Vai trò NHÂN VIÊN (NHAN_VIEN) bị chặn khi cố truy cập trang cấu hình Admin hoặc xóa vé (accessDeniedHandler điều hướng về /gui-xe?denied=true)")
    void testNhanVien_CannotAccessAdminOnlyPagesOrDeleteTickets() throws Exception {
        // Chặn trang quản lý người dùng / nhân viên
        mockMvc.perform(get("/nhan-vien"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gui-xe?denied=true"));

        mockMvc.perform(get("/staff"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gui-xe?denied=true"));

        // Chặn trang cấu hình bảng giá
        mockMvc.perform(get("/price-settings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gui-xe?denied=true"));

        // Chặn trang cài đặt hệ thống
        mockMvc.perform(get("/settings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gui-xe?denied=true"));

        // Chặn trang báo cáo doanh thu
        mockMvc.perform(get("/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gui-xe?denied=true"));

        // Chặn xóa vé xe
        mockMvc.perform(get("/ve-xe/1/xoa"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gui-xe?denied=true"));
    }

    @Test
    @WithMockUser(username = "baove_test", roles = "NHAN_VIEN")
    @DisplayName("5. Vai trò NHÂN VIÊN (NHAN_VIEN) được phép truy cập các chức năng vận hành trực cổng")
    void testNhanVien_CanAccessOperationalPages() throws Exception {
        mockMvc.perform(get("/gui-xe")).andExpect(status().isOk());
        mockMvc.perform(get("/ca-truc")).andExpect(status().isOk());
        mockMvc.perform(get("/canh-bao-an-ninh")).andExpect(status().isOk());
        mockMvc.perform(get("/ve-xe")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin_test", roles = "ADMIN")
    @DisplayName("6. Vai trò QUẢN TRỊ VIÊN (ADMIN) có toàn quyền truy cập các trang quản trị")
    void testAdmin_CanAccessAdminPages() throws Exception {
        mockMvc.perform(get("/tong-quan")).andExpect(status().isOk());
        mockMvc.perform(get("/nhan-vien")).andExpect(status().isOk());
        mockMvc.perform(get("/price-settings")).andExpect(status().isOk());
        mockMvc.perform(get("/settings")).andExpect(status().isOk());
    }
}

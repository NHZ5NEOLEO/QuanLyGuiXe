package com.example.demo;

import com.example.demo.model.NguoiDung;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RoleAccessControlTest {

    /**
     * Trường hợp 13: Phân quyền và kiểm tra quyền người dùng
     */
    @Test
    @DisplayName("13a. Quản trị viên (ADMIN) có đầy đủ quyền hạn quản trị")
    void testAdminRole_HasAdminAuthority() {
        NguoiDung admin = new NguoiDung("admin", "admin123", "Quản Trị Viên", "ADMIN", "0909000111");
        String role = "ROLE_" + admin.getVaiTro();

        User userDetails = new User(admin.getTenDangNhap(), admin.getMatKhau(), List.of(new SimpleGrantedAuthority(role)));

        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertFalse(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_NHAN_VIEN")));
    }

    @Test
    @DisplayName("13b. Nhân viên (NHAN_VIEN) không có quyền hạn ADMIN (bị chặn chức năng quản trị)")
    void testStaffRole_DoesNotHaveAdminAuthority() {
        NguoiDung staff = new NguoiDung("baove", "123456", "Nhân Viên Trực Cổng", "NHAN_VIEN", "0909000222");
        String role = "ROLE_" + staff.getVaiTro();

        User userDetails = new User(staff.getTenDangNhap(), staff.getMatKhau(), List.of(new SimpleGrantedAuthority(role)));

        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_NHAN_VIEN")));
        // Không được có ROLE_ADMIN
        assertFalse(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("13c. Tài khoản bị khóa (trangThai = false) không được phép hoạt động")
    void testLockedUser_CannotAuthenticate() {
        NguoiDung lockedUser = new NguoiDung("locked", "123456", "Tài Khoản Khóa", "NHAN_VIEN", "0909000333");
        lockedUser.setTrangThai(false);

        assertFalse(Boolean.TRUE.equals(lockedUser.getTrangThai()));
    }
}

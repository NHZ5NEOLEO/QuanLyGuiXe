package com.example.demo.controller;

import com.example.demo.exception.BadRequestException;
import com.example.demo.model.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final NguoiDungRepository nguoiDungRepository;

    public AuthRestController(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    /**
     * API Đăng nhập và tạo session cho người dùng
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Tên đăng nhập và mật khẩu không được để trống!");
        }

        NguoiDung user = nguoiDungRepository.findByTenDangNhap(username.trim())
                .orElseThrow(() -> new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác!"));

        if (!password.equals(user.getMatKhau())) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        if (!Boolean.TRUE.equals(user.getTrangThai())) {
            throw new BadRequestException("Tài khoản của bạn hiện đang bị khóa!");
        }

        // Tạo Spring Security Authentication trong Session
        String role = "ROLE_" + user.getVaiTro();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getTenDangNhap(),
                null,
                List.of(new SimpleGrantedAuthority(role))
        );

        SecurityContext sc = SecurityContextHolder.createEmptyContext();
        sc.setAuthentication(auth);
        SecurityContextHolder.setContext(sc);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
        session.setAttribute("userName", user.getHoTen());
        session.setAttribute("userRole", user.getVaiTro());

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đăng nhập thành công!");
        resp.put("userId", user.getId());
        resp.put("username", user.getTenDangNhap());
        resp.put("fullName", user.getHoTen());
        resp.put("role", user.getVaiTro());

        return ResponseEntity.ok(resp);
    }

    /**
     * API Đăng xuất và hủy session
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đăng xuất thành công!");
        return ResponseEntity.ok(resp);
    }

    /**
     * API Lấy thông tin tài khoản đang đăng nhập
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            Map<String, Object> anonymous = new HashMap<>();
            anonymous.put("authenticated", false);
            return ResponseEntity.ok(anonymous);
        }

        String username = auth.getName();
        NguoiDung user = nguoiDungRepository.findByTenDangNhap(username).orElse(null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("authenticated", true);
        resp.put("username", username);
        if (user != null) {
            resp.put("userId", user.getId());
            resp.put("fullName", user.getHoTen());
            resp.put("role", user.getVaiTro());
            resp.put("phone", user.getSoDienThoai());
        }
        return ResponseEntity.ok(resp);
    }
}

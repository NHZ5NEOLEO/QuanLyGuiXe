package com.example.demo.controller;

import com.example.demo.exception.BadRequestException;
import com.example.demo.model.KhachHang;
import com.example.demo.model.NguoiDung;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthRestController(NguoiDungRepository nguoiDungRepository, 
                              KhachHangRepository khachHangRepository,
                              PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * API Đăng ký tài khoản khách hàng / cư dân mới
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> data, 
                                      HttpServletRequest request, 
                                      HttpServletResponse response) {
        String username = data.getOrDefault("username", data.get("tenDangNhap"));
        String password = data.getOrDefault("password", data.get("matKhau"));
        String fullName = data.getOrDefault("fullName", data.get("hoTen"));
        String phone = data.getOrDefault("phone", data.get("soDienThoai"));
        String email = data.get("email");
        String cccd = data.get("cccd");
        String address = data.getOrDefault("address", data.get("diaChi"));

        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Tên đăng nhập không được để trống!");
        }
        username = username.trim().toLowerCase();
        if (username.length() < 3) {
            throw new BadRequestException("Tên đăng nhập phải có ít nhất 3 ký tự!");
        }
        if (nguoiDungRepository.findByTenDangNhap(username).isPresent()) {
            throw new BadRequestException("Tên đăng nhập [" + username + "] đã tồn tại, vui lòng chọn tên khác!");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Mật khẩu không được để trống!");
        }
        if (password.length() < 6) {
            throw new BadRequestException("Mật khẩu phải có ít nhất 6 ký tự!");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new BadRequestException("Họ và tên không được để trống!");
        }
        fullName = fullName.trim();

        if (phone == null || phone.trim().isEmpty()) {
            throw new BadRequestException("Số điện thoại không được để trống!");
        }
        phone = phone.trim();
        if (!phone.matches("^[0-9]{9,11}$")) {
            throw new BadRequestException("Số điện thoại không hợp lệ (cần 9-11 chữ số)!");
        }

        // 1. Mã hóa mật khẩu bằng BCrypt
        String encodedPassword = passwordEncoder.encode(password);

        // 2. Tạo tài khoản người dùng NguoiDung với vai trò KHACH_HANG
        NguoiDung user = new NguoiDung(username, encodedPassword, fullName, "KHACH_HANG", phone);
        nguoiDungRepository.save(user);

        // 3. Tạo hoặc liên kết hồ sơ khách hàng KhachHang
        KhachHang kh = khachHangRepository.findBySoDienThoai(phone).orElseGet(KhachHang::new);
        kh.setHoTen(fullName);
        kh.setSoDienThoai(phone);
        if (email != null && !email.isBlank()) kh.setEmail(email.trim());
        if (cccd != null && !cccd.isBlank()) kh.setCccd(cccd.trim());
        if (address != null && !address.isBlank()) kh.setDiaChi(address.trim());
        khachHangRepository.save(kh);

        // 4. Tự động xác thực đăng nhập trong Session
        String role = "ROLE_KHACH_HANG";
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user.getTenDangNhap(),
                null,
                List.of(new SimpleGrantedAuthority(role))
        );

        SecurityContext sc = SecurityContextHolder.createEmptyContext();
        sc.setAuthentication(auth);
        SecurityContextHolder.setContext(sc);
        securityContextRepository.saveContext(sc, request, response);

        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getHoTen());
        session.setAttribute("userRole", user.getVaiTro());
        session.setAttribute("userLogin", user.getTenDangNhap());

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đăng ký tài khoản thành công!");
        resp.put("userId", user.getId());
        resp.put("username", user.getTenDangNhap());
        resp.put("fullName", user.getHoTen());
        resp.put("phone", user.getSoDienThoai());
        resp.put("role", user.getVaiTro());
        resp.put("redirectUrl", "/dang-ky-ve-thang");

        return ResponseEntity.ok(resp);
    }

    /**
     * API Đăng nhập và tạo session cho người dùng
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, 
                                   HttpServletRequest request, 
                                   HttpServletResponse response) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BadRequestException("Tên đăng nhập và mật khẩu không được để trống!");
        }

        NguoiDung user = nguoiDungRepository.findByTenDangNhap(username.trim().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác!"));

        // So khớp mật khẩu qua PasswordEncoder (hỗ trợ cả BCrypt và plain text tương thích ngược)
        if (!passwordEncoder.matches(password, user.getMatKhau())) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        if (!Boolean.TRUE.equals(user.getTrangThai())) {
            throw new BadRequestException("Tài khoản của bạn hiện đang bị khóa! Vui lòng liên hệ ban quản lý.");
        }

        // Tự động nâng cấp mã hóa mật khẩu sang BCrypt nếu cần
        if (passwordEncoder.upgradeEncoding(user.getMatKhau())) {
            user.setMatKhau(passwordEncoder.encode(password));
            nguoiDungRepository.save(user);
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
        securityContextRepository.saveContext(sc, request, response);

        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getHoTen());
        session.setAttribute("userRole", user.getVaiTro());
        session.setAttribute("userLogin", user.getTenDangNhap());

        String redirectUrl = "/tong-quan";
        if ("NHAN_VIEN".equalsIgnoreCase(user.getVaiTro())) {
            redirectUrl = "/gui-xe";
        } else if ("KHACH_HANG".equalsIgnoreCase(user.getVaiTro()) || "CU_DAN".equalsIgnoreCase(user.getVaiTro())) {
            redirectUrl = "/dang-ky-ve-thang";
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đăng nhập thành công!");
        resp.put("userId", user.getId());
        resp.put("username", user.getTenDangNhap());
        resp.put("fullName", user.getHoTen());
        resp.put("role", user.getVaiTro());
        resp.put("redirectUrl", redirectUrl);

        return ResponseEntity.ok(resp);
    }

    /**
     * API Đăng xuất và hủy session hoàn toàn
     */
    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        // Xóa Cookie JSESSIONID và remember-me
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);

        Cookie rmCookie = new Cookie("remember-me", null);
        rmCookie.setPath("/");
        rmCookie.setMaxAge(0);
        rmCookie.setHttpOnly(true);
        response.addCookie(rmCookie);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đăng xuất thành công!");
        return ResponseEntity.ok(resp);
    }

    /**
     * API Lấy thông tin tài khoản đang đăng nhập
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = null;
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
        } else {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userLogin") != null) {
                username = (String) session.getAttribute("userLogin");
            }
        }

        if (username == null) {
            Map<String, Object> anonymous = new HashMap<>();
            anonymous.put("authenticated", false);
            return ResponseEntity.ok(anonymous);
        }

        NguoiDung user = nguoiDungRepository.findByTenDangNhap(username).orElse(null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("authenticated", true);
        resp.put("username", username);
        if (user != null) {
            resp.put("userId", user.getId());
            resp.put("fullName", user.getHoTen());
            resp.put("role", user.getVaiTro());
            resp.put("phone", user.getSoDienThoai());
            resp.put("isAdmin", "ADMIN".equalsIgnoreCase(user.getVaiTro()));
            resp.put("isStaff", "NHAN_VIEN".equalsIgnoreCase(user.getVaiTro()));
            resp.put("isCustomer", "KHACH_HANG".equalsIgnoreCase(user.getVaiTro()) || "CU_DAN".equalsIgnoreCase(user.getVaiTro()));
        }
        return ResponseEntity.ok(resp);
    }
}

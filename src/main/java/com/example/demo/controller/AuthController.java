package com.example.demo.controller;

import com.example.demo.model.KhachHang;
import com.example.demo.model.NguoiDung;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller xử lý trang đăng nhập, đăng xuất và đăng ký tài khoản khách hàng.
 */
@Controller
public class AuthController {

    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(NguoiDungRepository nguoiDungRepository, 
                          KhachHangRepository khachHangRepository,
                          PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping({"/dang-nhap", "/login"})
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            @RequestParam(value = "denied", required = false) String denied,
            HttpServletRequest request,
            Model model
    ) {
        // Đảm bảo session được khởi tạo trước khi template render tránh lỗi commit response
        request.getSession(true);

        // Nếu đã đăng nhập thành công từ trước và không phải đang thực hiện logout, tự động chuyển về trang phân hệ phù hợp
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (logout == null && auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            boolean isStaff = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_NHAN_VIEN"));
            boolean isCustomer = auth.getAuthorities().stream().anyMatch(a -> 
                    a.getAuthority().equals("ROLE_KHACH_HANG") || a.getAuthority().equals("ROLE_CU_DAN"));
            if (isStaff) {
                return "redirect:/gui-xe";
            } else if (isCustomer) {
                return "redirect:/dang-ky-ve-thang";
            } else {
                return "redirect:/tong-quan";
            }
        }

        if (error != null) {
            if ("locked".equalsIgnoreCase(error)) {
                model.addAttribute("errorMessage", "Tài khoản của bạn hiện đang bị khóa! Vui lòng liên hệ ban quản lý.");
            } else {
                model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            }
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất khỏi hệ thống thành công!");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        }
        if (denied != null) {
            model.addAttribute("errorMessage", "Bạn không có quyền truy cập trang yêu cầu. Vui lòng đăng nhập tài khoản có thẩm quyền!");
        }
        return "dang-nhap";
    }

    /**
     * Hỗ trợ xử lý form đăng nhập gửi trực tiếp tới endpoint /dang-nhap (bên cạnh /login của Spring Security)
     */
    @PostMapping("/dang-nhap")
    public String handleLoginPost(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/dang-nhap";
        }

        var userOpt = nguoiDungRepository.findByTenDangNhap(username.trim().toLowerCase());
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getMatKhau())) {
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/dang-nhap";
        }

        NguoiDung user = userOpt.get();
        if (!Boolean.TRUE.equals(user.getTrangThai())) {
            redirectAttributes.addAttribute("error", "locked");
            return "redirect:/dang-nhap";
        }

        // Tự động nâng cấp mật khẩu sang BCrypt nếu mật khẩu cũ đang lưu dạng plain text
        if (passwordEncoder.upgradeEncoding(user.getMatKhau())) {
            user.setMatKhau(passwordEncoder.encode(password));
            nguoiDungRepository.save(user);
        }

        // Thiết lập Security Context trong Session
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
        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getHoTen());
        session.setAttribute("userRole", user.getVaiTro());
        session.setAttribute("userLogin", user.getTenDangNhap());

        // Kiểm tra SavedRequest để quay lại trang người dùng đang xem trước đó
        RequestCache requestCache = new HttpSessionRequestCache();
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null && savedRequest.getRedirectUrl() != null) {
            String redirectUrl = savedRequest.getRedirectUrl();
            if (!redirectUrl.contains("/login") && !redirectUrl.contains("/dang-nhap")) {
                return "redirect:" + redirectUrl;
            }
        }

        if ("NHAN_VIEN".equalsIgnoreCase(user.getVaiTro())) {
            return "redirect:/gui-xe";
        } else if ("KHACH_HANG".equalsIgnoreCase(user.getVaiTro()) || "CU_DAN".equalsIgnoreCase(user.getVaiTro())) {
            return "redirect:/dang-ky-ve-thang";
        } else {
            return "redirect:/tong-quan";
        }
    }

    @GetMapping({"/dang-ky", "/register"})
    public String registerPage(Model model) {
        model.addAttribute("pageTitle", "Đăng ký tài khoản");
        return "dang-ky";
    }

    @PostMapping({"/dang-ky", "/register"})
    public String handleRegisterForm(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("fullName") String fullName,
            @RequestParam("phone") String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "cccd", required = false) String cccd,
            @RequestParam(value = "address", required = false) String address,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!");
            return "dang-ky";
        }
        username = username.trim().toLowerCase();
        if (username.length() < 3) {
            model.addAttribute("errorMessage", "Tên đăng nhập phải có ít nhất 3 ký tự!");
            return "dang-ky";
        }
        if (nguoiDungRepository.findByTenDangNhap(username).isPresent()) {
            model.addAttribute("errorMessage", "Tên đăng nhập [" + username + "] đã tồn tại! Vui lòng chọn tên khác.");
            return "dang-ky";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "dang-ky";
        }
        if (password.length() < 6) {
            model.addAttribute("errorMessage", "Mật khẩu phải có độ dài từ 6 ký tự trở lên!");
            return "dang-ky";
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập họ và tên của bạn!");
            return "dang-ky";
        }
        if (phone == null || !phone.trim().matches("^[0-9]{9,11}$")) {
            model.addAttribute("errorMessage", "Số điện thoại không hợp lệ (cần 9-11 chữ số)!");
            return "dang-ky";
        }

        // Tạo NguoiDung vai trò KHACH_HANG với mật khẩu BCrypt
        String encodedPassword = passwordEncoder.encode(password);
        NguoiDung user = new NguoiDung(username, encodedPassword, fullName.trim(), "KHACH_HANG", phone.trim());
        nguoiDungRepository.save(user);

        // Tạo hoặc cập nhật KhachHang
        KhachHang kh = khachHangRepository.findBySoDienThoai(phone.trim()).orElseGet(KhachHang::new);
        kh.setHoTen(fullName.trim());
        kh.setSoDienThoai(phone.trim());
        if (email != null && !email.isBlank()) kh.setEmail(email.trim());
        if (cccd != null && !cccd.isBlank()) kh.setCccd(cccd.trim());
        if (address != null && !address.isBlank()) kh.setDiaChi(address.trim());
        khachHangRepository.save(kh);

        // Đăng nhập tự động vào Security Context
        String role = "ROLE_KHACH_HANG";
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
        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getHoTen());
        session.setAttribute("userRole", user.getVaiTro());
        session.setAttribute("userLogin", user.getTenDangNhap());

        redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Bạn có thể tiến hành đăng ký vé tháng ngay.");
        return "redirect:/dang-ky-ve-thang";
    }

    @RequestMapping(value = {"/logout", "/dang-xuat"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        // Xóa triệt để Cookie JSESSIONID và remember-me
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

        return "redirect:/login?logout=true";
    }
}

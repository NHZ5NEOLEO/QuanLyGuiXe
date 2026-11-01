package com.example.demo.controller;

import com.example.demo.model.KhachHang;
import com.example.demo.model.NguoiDung;
import com.example.demo.model.VeThang;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.VeThangRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Controller quản lý Hồ sơ cá nhân (Profile) dành cho khách hàng & người dùng hệ thống.
 * Cho phép xem thông tin tài khoản, cập nhật thông tin cá nhân (họ tên, SĐT, CCCD, email, địa chỉ),
 * đổi mật khẩu an toàn (BCrypt) và quản lý danh sách vé tháng đã đăng ký.
 */
@Controller
public class ProfileController {

    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;
    private final VeThangRepository veThangRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0|\\+84)[0-9]{8,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public ProfileController(
            NguoiDungRepository nguoiDungRepository,
            KhachHangRepository khachHangRepository,
            VeThangRepository veThangRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
        this.veThangRepository = veThangRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Giao diện Hồ sơ cá nhân của khách hàng / người dùng hiện tại
     */
    @GetMapping({"/tai-khoan", "/ho-so", "/portal/ho-so", "/portal/profile"})
    public String profilePage(Model model, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByTenDangNhap(username);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        NguoiDung user = userOpt.get();
        model.addAttribute("user", user);

        // Đồng bộ session với dữ liệu mới nhất từ CSDL
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("userName", user.getHoTen());
            session.setAttribute("currentUser", user);
        }

        // Tìm hồ sơ Khách hàng tương ứng theo SĐT của tài khoản
        KhachHang customer = null;
        if (user.getSoDienThoai() != null && !user.getSoDienThoai().isBlank()) {
            customer = khachHangRepository.findBySoDienThoai(user.getSoDienThoai().trim()).orElse(null);
        }

        if (customer == null) {
            customer = new KhachHang();
            customer.setHoTen(user.getHoTen());
            customer.setSoDienThoai(user.getSoDienThoai());
        }
        model.addAttribute("customer", customer);

        // Lấy danh sách vé tháng của khách hàng
        List<VeThang> tickets = new ArrayList<>();
        if (customer.getId() != null) {
            tickets = veThangRepository.findByKhachHangIdOrderByNgayBatDauDesc(customer.getId());
        } else if (user.getSoDienThoai() != null && !user.getSoDienThoai().isBlank()) {
            tickets = veThangRepository.findByKhachHangSoDienThoaiOrderByNgayBatDauDesc(user.getSoDienThoai().trim());
        }
        model.addAttribute("tickets", tickets);

        long activeCount = tickets.stream().filter(t -> t != null && t.isConHan()).count();
        model.addAttribute("activeTicketsCount", activeCount);
        model.addAttribute("totalTicketsCount", tickets.size());

        model.addAttribute("pageTitle", "Hồ sơ cá nhân & Tài khoản");
        model.addAttribute("activeNav", "tai-khoan");

        return "portal/ho-so";
    }

    /**
     * Cập nhật thông tin cá nhân (Họ tên, SĐT, Email, CCCD, Địa chỉ)
     */
    @PostMapping({"/tai-khoan/cap-nhat", "/portal/ho-so/cap-nhat"})
    public String updateProfile(
            @RequestParam(value = "hoTen", required = false) String hoTen,
            @RequestParam(value = "soDienThoai", required = false) String soDienThoai,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "cccd", required = false) String cccd,
            @RequestParam(value = "diaChi", required = false) String diaChi,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByTenDangNhap(username);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        NguoiDung user = userOpt.get();

        String trimmedHoTen = hoTen != null ? hoTen.trim() : "";
        String trimmedSdt = soDienThoai != null ? soDienThoai.trim() : "";
        String trimmedEmail = email != null ? email.trim() : "";
        String trimmedCccd = cccd != null ? cccd.trim() : "";
        String trimmedDiaChi = diaChi != null ? diaChi.trim() : "";

        if (trimmedHoTen.length() < 2 || trimmedHoTen.length() > 100) {
            redirectAttributes.addFlashAttribute("errorMessage", "Họ và tên phải có độ dài từ 2 đến 100 ký tự!");
            return "redirect:/tai-khoan#tab-info";
        }

        if (!PHONE_PATTERN.matcher(trimmedSdt).matches()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại không đúng định dạng (9-11 chữ số, VD: 0912345678)!");
            return "redirect:/tai-khoan#tab-info";
        }

        if (!trimmedEmail.isEmpty() && !EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ email không đúng định dạng (VD: example@domain.com)!");
            return "redirect:/tai-khoan#tab-info";
        }

        // Kiểm tra xem số điện thoại mới có bị trùng với tài khoản người dùng khác hay không
        String oldPhone = user.getSoDienThoai() != null ? user.getSoDienThoai().trim() : "";
        if (!trimmedSdt.equalsIgnoreCase(oldPhone)) {
            Optional<NguoiDung> existingUser = nguoiDungRepository.findBySoDienThoai(trimmedSdt);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại này đã được sử dụng bởi một tài khoản khác trong hệ thống!");
                return "redirect:/tai-khoan#tab-info";
            }
        }

        // Cập nhật thông tin NguoiDung
        user.setHoTen(trimmedHoTen);
        user.setSoDienThoai(trimmedSdt);
        nguoiDungRepository.save(user);

        // Cập nhật hoặc tạo mới bản ghi KhachHang
        KhachHang customer = null;
        if (!oldPhone.isEmpty()) {
            customer = khachHangRepository.findBySoDienThoai(oldPhone).orElse(null);
        }
        if (customer == null) {
            customer = khachHangRepository.findBySoDienThoai(trimmedSdt).orElse(null);
        }

        if (customer == null) {
            customer = new KhachHang(trimmedHoTen, trimmedSdt, trimmedCccd, trimmedEmail, trimmedDiaChi);
        } else {
            customer.setHoTen(trimmedHoTen);
            customer.setSoDienThoai(trimmedSdt);
            customer.setCccd(trimmedCccd);
            customer.setEmail(trimmedEmail);
            customer.setDiaChi(trimmedDiaChi);
        }
        khachHangRepository.save(customer);

        // Cập nhật session để thanh tiêu đề/header hiển thị tên mới ngay lập tức
        if (session != null) {
            session.setAttribute("userName", user.getHoTen());
            session.setAttribute("currentUser", user);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        return "redirect:/tai-khoan#tab-info";
    }

    /**
     * Đổi mật khẩu tài khoản
     */
    @PostMapping({"/tai-khoan/doi-mat-khau", "/portal/ho-so/doi-mat-khau"})
    public String changePassword(
            @RequestParam(value = "matKhauHienTai", required = false) String matKhauHienTai,
            @RequestParam(value = "matKhauMoi", required = false) String matKhauMoi,
            @RequestParam(value = "xacNhanMatKhau", required = false) String xacNhanMatKhau,
            RedirectAttributes redirectAttributes
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }

        String username = auth.getName();
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByTenDangNhap(username);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }

        NguoiDung user = userOpt.get();

        if (matKhauHienTai == null || matKhauHienTai.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("passwordError", "Vui lòng nhập mật khẩu hiện tại!");
            return "redirect:/tai-khoan#tab-password";
        }

        if (!passwordEncoder.matches(matKhauHienTai, user.getMatKhau())) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu hiện tại không chính xác!");
            return "redirect:/tai-khoan#tab-password";
        }

        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới phải có tối thiểu 6 ký tự!");
            return "redirect:/tai-khoan#tab-password";
        }

        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!");
            return "redirect:/tai-khoan#tab-password";
        }

        if (passwordEncoder.matches(matKhauMoi, user.getMatKhau())) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            return "redirect:/tai-khoan#tab-password";
        }

        // Mã hóa mật khẩu mới bằng BCrypt và lưu
        user.setMatKhau(passwordEncoder.encode(matKhauMoi));
        nguoiDungRepository.save(user);

        redirectAttributes.addFlashAttribute("passwordSuccess", "Đổi mật khẩu thành công! Mật khẩu mới đã được kích hoạt.");
        return "redirect:/tai-khoan#tab-password";
    }
}


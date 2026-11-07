package com.example.demo.controller;

import com.example.demo.model.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/nhan-vien", "/staff", "/nguoi-dung", "/users", "/quan-ly-nguoi-dung", "/quan-ly-nhan-vien"})
public class StaffController {

    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;

    public StaffController(NguoiDungRepository nguoiDungRepository, PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String staffList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        List<NguoiDung> users = nguoiDungRepository.findAll();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> (u.getHoTen() != null && u.getHoTen().toLowerCase().contains(kw)) ||
                                 (u.getTenDangNhap() != null && u.getTenDangNhap().toLowerCase().contains(kw)) ||
                                 (u.getEmail() != null && u.getEmail().toLowerCase().contains(kw)) ||
                                 (u.getSoDienThoai() != null && u.getSoDienThoai().contains(kw)))
                    .collect(Collectors.toList());
        }

        if (role != null && !role.trim().isEmpty() && !"ALL".equalsIgnoreCase(role)) {
            users = users.stream()
                    .filter(u -> role.equalsIgnoreCase(u.getVaiTro()))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            boolean act = "ACTIVE".equalsIgnoreCase(status);
            users = users.stream()
                    .filter(u -> Boolean.valueOf(act).equals(u.getTrangThai()))
                    .collect(Collectors.toList());
        }

        long activeCount = nguoiDungRepository.findAll().stream().filter(u -> Boolean.TRUE.equals(u.getTrangThai())).count();
        long adminCount = nguoiDungRepository.findAll().stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getVaiTro())).count();
        long staffCount = nguoiDungRepository.findAll().stream().filter(u -> "NHAN_VIEN".equalsIgnoreCase(u.getVaiTro())).count();
        long customerCount = nguoiDungRepository.findAll().stream().filter(u -> "KHACH_HANG".equalsIgnoreCase(u.getVaiTro())).count();

        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("totalCount", users.size());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("staffCount", staffCount);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("pageTitle", "Quản lý người dùng");
        model.addAttribute("activePage", "staff");

        List<NguoiDung> paged = com.example.demo.util.PaginationHelper.paginate(users, page, size, model);
        model.addAttribute("users", paged);

        return "nhan-vien";
    }

    @PostMapping("/add")
    public String addStaff(
            @RequestParam("hoTen") String hoTen,
            @RequestParam("tenDangNhap") String tenDangNhap,
            @RequestParam("matKhau") String matKhau,
            @RequestParam(value = "vaiTro", defaultValue = "NHAN_VIEN") String vaiTro,
            @RequestParam(value = "soDienThoai", required = false) String soDienThoai,
            @RequestParam(value = "email", required = false) String email,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (tenDangNhap == null || tenDangNhap.trim().isEmpty() || matKhau == null || matKhau.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập và mật khẩu không được để trống!");
                return "redirect:/nhan-vien";
            }

            String cleanUsername = normalizeUsername(tenDangNhap);
            if (cleanUsername.isEmpty()) cleanUsername = tenDangNhap.trim().toLowerCase();

            if (nguoiDungRepository.findByTenDangNhap(cleanUsername).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập '" + cleanUsername + "' đã tồn tại!");
                return "redirect:/nhan-vien";
            }

            NguoiDung user = new NguoiDung(
                    cleanUsername,
                    passwordEncoder.encode(matKhau.trim()),
                    hoTen.trim(),
                    vaiTro.trim().toUpperCase(),
                    soDienThoai != null ? soDienThoai.trim() : ""
            );
            if (email != null && !email.isBlank()) {
                user.setEmail(email.trim());
            }
            nguoiDungRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm người dùng [" + hoTen + "] thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/nhan-vien";
    }

    @PostMapping("/edit")
    public String editStaff(
            @RequestParam("id") Long id,
            @RequestParam("hoTen") String hoTen,
            @RequestParam("vaiTro") String vaiTro,
            @RequestParam(value = "tenDangNhap", required = false) String tenDangNhap,
            @RequestParam(value = "soDienThoai", required = false) String soDienThoai,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "matKhauMoi", required = false) String matKhauMoi,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NguoiDung user = nguoiDungRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));

            user.setHoTen(hoTen.trim());
            user.setVaiTro(vaiTro.trim().toUpperCase());
            if (tenDangNhap != null && !tenDangNhap.trim().isEmpty()) {
                String cleanUsername = normalizeUsername(tenDangNhap);
                if (cleanUsername.isEmpty()) cleanUsername = tenDangNhap.trim().toLowerCase();
                if (!cleanUsername.equalsIgnoreCase(user.getTenDangNhap())) {
                    if (nguoiDungRepository.findByTenDangNhap(cleanUsername).isPresent()) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập '" + cleanUsername + "' đã được sử dụng!");
                        return "redirect:/nhan-vien";
                    }
                    user.setTenDangNhap(cleanUsername);
                }
            }
            if (soDienThoai != null) user.setSoDienThoai(soDienThoai.trim());
            if (email != null) user.setEmail(email.trim());
            if (matKhauMoi != null && !matKhauMoi.trim().isEmpty()) {
                user.setMatKhau(passwordEncoder.encode(matKhauMoi.trim()));
            }

            nguoiDungRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/nhan-vien";
    }

    private String normalizeUsername(String input) {
        if (input == null) return "";
        String trimmed = input.trim();
        String nfd = java.text.Normalizer.normalize(trimmed.toLowerCase(), java.text.Normalizer.Form.NFD);
        String noAccents = nfd.replaceAll("\\p{M}", "").replace("đ", "d").replace("Đ", "d");
        return noAccents.replaceAll("[^a-z0-9_.-]", "");
    }

    @PostMapping("/toggle")
    public String toggleStatus(
            @RequestParam("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NguoiDung user = nguoiDungRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên ID: " + id));

            boolean newState = !Boolean.TRUE.equals(user.getTrangThai());
            user.setTrangThai(newState);
            nguoiDungRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã " + (newState ? "kích hoạt" : "tạm khóa") + " tài khoản [" + user.getTenDangNhap() + "]!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/nhan-vien";
    }
}

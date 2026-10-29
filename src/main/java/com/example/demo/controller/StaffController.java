package com.example.demo.controller;

import com.example.demo.model.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/nhan-vien", "/staff"})
public class StaffController {

    private final NguoiDungRepository nguoiDungRepository;

    public StaffController(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @GetMapping
    public String staffList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            Model model
    ) {
        List<NguoiDung> users = nguoiDungRepository.findAll();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            users = users.stream()
                    .filter(u -> (u.getHoTen() != null && u.getHoTen().toLowerCase().contains(kw)) ||
                                 (u.getTenDangNhap() != null && u.getTenDangNhap().toLowerCase().contains(kw)) ||
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

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("totalCount", users.size());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("staffCount", staffCount);
        model.addAttribute("pageTitle", "Quản lý nhân viên");
        model.addAttribute("activePage", "staff");

        return "nhan-vien";
    }

    @PostMapping("/add")
    public String addStaff(
            @RequestParam("hoTen") String hoTen,
            @RequestParam("tenDangNhap") String tenDangNhap,
            @RequestParam("matKhau") String matKhau,
            @RequestParam(value = "vaiTro", defaultValue = "NHAN_VIEN") String vaiTro,
            @RequestParam(value = "soDienThoai", required = false) String soDienThoai,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (tenDangNhap == null || tenDangNhap.trim().isEmpty() || matKhau == null || matKhau.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập và mật khẩu không được để trống!");
                return "redirect:/nhan-vien";
            }

            if (nguoiDungRepository.findByTenDangNhap(tenDangNhap.trim()).isPresent()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập '" + tenDangNhap.trim() + "' đã tồn tại!");
                return "redirect:/nhan-vien";
            }

            NguoiDung user = new NguoiDung(
                    tenDangNhap.trim(),
                    matKhau.trim(),
                    hoTen.trim(),
                    vaiTro.trim().toUpperCase(),
                    soDienThoai != null ? soDienThoai.trim() : ""
            );
            nguoiDungRepository.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Thêm nhân viên [" + hoTen + "] thành công!");
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
            @RequestParam(value = "soDienThoai", required = false) String soDienThoai,
            @RequestParam(value = "matKhauMoi", required = false) String matKhauMoi,
            RedirectAttributes redirectAttributes
    ) {
        try {
            NguoiDung user = nguoiDungRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên ID: " + id));

            user.setHoTen(hoTen.trim());
            user.setVaiTro(vaiTro.trim().toUpperCase());
            if (soDienThoai != null) user.setSoDienThoai(soDienThoai.trim());
            if (matKhauMoi != null && !matKhauMoi.trim().isEmpty()) {
                user.setMatKhau(matKhauMoi.trim());
            }

            nguoiDungRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin nhân viên thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/nhan-vien";
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

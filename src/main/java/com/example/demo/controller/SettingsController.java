package com.example.demo.controller;

import com.example.demo.model.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.ParkingLotService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/cai-dat", "/settings"})
public class SettingsController {

    private final ParkingLotService parkingLotService;
    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingsController(ParkingLotService parkingLotService, 
                              NguoiDungRepository nguoiDungRepository,
                              PasswordEncoder passwordEncoder) {
        this.parkingLotService = parkingLotService;
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String settingsPage(HttpSession session, Model model) {
        model.addAttribute("totalSlots", parkingLotService.getTotalSlots());
        model.addAttribute("khuVucs", parkingLotService.getAllKhuVuc());
        model.addAttribute("pageTitle", "Cài đặt hệ thống");
        model.addAttribute("activePage", "settings");

        return "cai-dat";
    }

    @PostMapping("/save-general")
    public String saveGeneral(
            @RequestParam(value = "parkingName", defaultValue = "BÃI XE TRUNG TÂM PARKING") String parkingName,
            @RequestParam(value = "hotline", defaultValue = "1900 6868") String hotline,
            @RequestParam(value = "address", defaultValue = "Trung tâm Quản lý Bãi xe TP") String address,
            RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("successMessage", "Đã lưu thông tin cấu hình bãi xe thành công!");
        return "redirect:/cai-dat";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : null;
            if (username == null || "anonymousUser".equals(username)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
                return "redirect:/login";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp!");
                return "redirect:/cai-dat";
            }

            NguoiDung userInDb = nguoiDungRepository.findByTenDangNhap(username).orElse(null);
            if (userInDb == null || !passwordEncoder.matches(currentPassword.trim(), userInDb.getMatKhau())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
                return "redirect:/cai-dat";
            }

            userInDb.setMatKhau(passwordEncoder.encode(newPassword.trim()));
            nguoiDungRepository.save(userInDb);
            session.setAttribute("currentUser", userInDb);

            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/cai-dat";
    }
}

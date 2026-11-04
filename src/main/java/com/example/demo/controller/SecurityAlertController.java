package com.example.demo.controller;

import com.example.demo.model.DanhSachDen;
import com.example.demo.model.VeXe;
import com.example.demo.service.BlacklistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping({"/canh-bao-an-ninh", "/security-alerts"})
public class SecurityAlertController {

    private final BlacklistService blacklistService;

    public SecurityAlertController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @GetMapping
    public String securityAlerts(
            @RequestParam(value = "tab", defaultValue = "overdue") String tab,
            Model model
    ) {
        List<VeXe> overdueVehicles = blacklistService.getOverdueVehicles(24); // Xe gửi > 24h
        List<DanhSachDen> blacklist = blacklistService.getAllBlacklist();

        model.addAttribute("overdueVehicles", overdueVehicles);
        model.addAttribute("blacklist", blacklist);
        model.addAttribute("activeTab", tab);
        model.addAttribute("activePage", "security-alerts");
        model.addAttribute("pageTitle", "Cảnh báo an ninh");

        return "canh-bao-an-ninh";
    }

    @PostMapping("/blacklist/add")
    public String addToBlacklist(
            @RequestParam("bienSoXe") String bienSoXe,
            @RequestParam("lyDo") String lyDo,
            @RequestParam(value = "mucDo", defaultValue = "NGUY_HIEM") String mucDo,
            @RequestParam(value = "nguoiThem", defaultValue = "Bảo vệ") String nguoiThem,
            RedirectAttributes redirectAttributes
    ) {
        try {
            blacklistService.addToBlacklist(bienSoXe, lyDo, mucDo, nguoiThem);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm biển số [" + bienSoXe.toUpperCase() + "] vào Danh sách đen cảnh báo an ninh!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/canh-bao-an-ninh?tab=blacklist";
    }

    @PostMapping("/blacklist/delete")
    public String deleteFromBlacklist(
            @RequestParam("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            blacklistService.removeFromBlacklist(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa xe khỏi danh sách đen an ninh!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/canh-bao-an-ninh?tab=blacklist";
    }

    @PostMapping("/overdue/verify")
    public String verifyOverdueVehicle(
            @RequestParam("veXeId") Integer veXeId,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
            Principal principal,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String staff = resolveStaffName(principal, session);
            VeXe vx = blacklistService.ghiNhanKiemTraXeQuaGio(veXeId, ghiChu, staff);
            redirectAttributes.addFlashAttribute("successMessage", "Đã ghi nhận kiểm tra phương tiện [" + vx.getBienSoXe() + "] thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/canh-bao-an-ninh?tab=overdue";
    }

    @PostMapping("/overdue/verify-api")
    @ResponseBody
    public ResponseEntity<?> verifyOverdueApi(
            @RequestBody Map<String, Object> payload,
            Principal principal,
            HttpSession session
    ) {
        try {
            if (payload == null || !payload.containsKey("veXeId")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu mã lượt gửi xe"));
            }
            Integer veXeId = payload.get("veXeId") instanceof Number
                    ? ((Number) payload.get("veXeId")).intValue()
                    : Integer.parseInt(payload.get("veXeId").toString());
            String ghiChu = payload.get("ghiChu") != null ? payload.get("ghiChu").toString() : null;
            String staff = resolveStaffName(principal, session);
            VeXe vx = blacklistService.ghiNhanKiemTraXeQuaGio(veXeId, ghiChu, staff);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã ghi nhận kiểm tra phương tiện [" + vx.getBienSoXe() + "] thành công.",
                    "daKiemTra", true,
                    "nguoiKiemTra", vx.getNguoiKiemTra() != null ? vx.getNguoiKiemTra() : "Bảo vệ",
                    "ghiChuKiemTra", vx.getGhiChuKiemTra() != null ? vx.getGhiChuKiemTra() : "",
                    "thoiGianKiemTra", vx.getThoiGianKiemTra() != null ? vx.getThoiGianKiemTra().toString() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    private String resolveStaffName(Principal principal, HttpSession session) {
        if (session != null && session.getAttribute("userName") != null) {
            String name = (String) session.getAttribute("userName");
            if (!name.isBlank()) return name;
        }
        if (session != null && session.getAttribute("userLogin") != null) {
            String login = (String) session.getAttribute("userLogin");
            if (!login.isBlank()) return login;
        }
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            return principal.getName();
        }
        return "Bảo vệ";
    }
}

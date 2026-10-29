package com.example.demo.controller;

import com.example.demo.model.DanhSachDen;
import com.example.demo.model.VeXe;
import com.example.demo.service.BlacklistService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
}

package com.example.demo.controller;

import com.example.demo.model.CaTruc;
import com.example.demo.service.ReportService;
import com.example.demo.service.ShiftService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping({"/ca-truc", "/shift"})
public class ShiftController {

    private final ShiftService shiftService;
    private final ReportService reportService;

    public ShiftController(ShiftService shiftService, ReportService reportService) {
        this.shiftService = shiftService;
        this.reportService = reportService;
    }

    @GetMapping
    public String shiftManagement(Model model) {
        CaTruc currentShift = shiftService.getCurrentShift();
        List<CaTruc> pastShifts = shiftService.getRecentShifts();

        model.addAttribute("currentShift", currentShift);
        model.addAttribute("pastShifts", pastShifts);
        model.addAttribute("revenueByVehicleType", reportService.getRevenueByVehicleType());
        model.addAttribute("activePage", "shift");
        model.addAttribute("pageTitle", "Bàn giao ca trực");

        return "ca-truc";
    }

    @PostMapping("/handover")
    public String handoverShift(
            @RequestParam("shiftId") Long shiftId,
            @RequestParam(value = "ghiChuBanGiao", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        try {
            CaTruc handed = shiftService.handoverShift(shiftId, ghiChu);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã chốt và bàn giao ca trực thành công! Tổng doanh thu ca: " +
                            String.format("%,.0f", handed.getTongDoanhThu()) + " VNĐ");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/ca-truc";
    }
}

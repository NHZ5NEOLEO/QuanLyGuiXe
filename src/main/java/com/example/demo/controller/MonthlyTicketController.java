package com.example.demo.controller;

import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeThang;
import com.example.demo.service.MonthlyTicketService;
import com.example.demo.service.ParkingLotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping({"/ve-thang", "/monthly-tickets"})
public class MonthlyTicketController {

    private final MonthlyTicketService monthlyTicketService;
    private final ParkingLotService parkingLotService;

    public MonthlyTicketController(MonthlyTicketService monthlyTicketService, ParkingLotService parkingLotService) {
        this.monthlyTicketService = monthlyTicketService;
        this.parkingLotService = parkingLotService;
    }

    @GetMapping
    public String listMonthlyTickets(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        List<VeThang> tickets = monthlyTicketService.searchVeThang(keyword);

        // Lọc theo trạng thái vé tháng
        java.time.LocalDate today = java.time.LocalDate.now();
        if ("HOAT_DONG".equalsIgnoreCase(status)) {
            tickets = tickets.stream()
                    .filter(t -> "HOAT_DONG".equalsIgnoreCase(t.getTrangThai()) && t.getNgayHetHan() != null && !today.isAfter(t.getNgayHetHan()))
                    .toList();
        } else if ("CHO_THANH_TOAN".equalsIgnoreCase(status)) {
            tickets = tickets.stream()
                    .filter(t -> "CHO_THANH_TOAN".equalsIgnoreCase(t.getTrangThai()))
                    .toList();
        } else if ("EXPIRED".equalsIgnoreCase(status)) {
            tickets = tickets.stream()
                    .filter(t -> !"CHO_THANH_TOAN".equalsIgnoreCase(t.getTrangThai()) && (t.getNgayHetHan() == null || today.isAfter(t.getNgayHetHan())))
                    .toList();
        } else if ("KHOA".equalsIgnoreCase(status)) {
            tickets = tickets.stream()
                    .filter(t -> "KHOA".equalsIgnoreCase(t.getTrangThai()))
                    .toList();
        }

        List<LoaiXe> loaiXes = parkingLotService.getAllLoaiXe();

        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("totalActive", monthlyTicketService.countActivePasses());
        model.addAttribute("countTotal", monthlyTicketService.demTongVeThang());
        model.addAttribute("countActive", monthlyTicketService.countActivePasses());
        model.addAttribute("countPending", monthlyTicketService.demVeThangChoThanhToan());
        model.addAttribute("countExpired", monthlyTicketService.demVeThangHetHan());
        model.addAttribute("countLocked", monthlyTicketService.demVeThangKhoa());
        model.addAttribute("activePage", "monthly-tickets");
        model.addAttribute("pageTitle", "Quản lý vé tháng");

        List<VeThang> paged = com.example.demo.util.PaginationHelper.paginate(tickets, page, size, model);
        model.addAttribute("tickets", paged);

        return "ve-thang";
    }

    @PostMapping("/add")
    public String addMonthlyTicket(
            @RequestParam("hoTen") String hoTen,
            @RequestParam("soDienThoai") String soDienThoai,
            @RequestParam(value = "cccd", required = false) String cccd,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "diaChi", required = false) String diaChi,
            @RequestParam("bienSoXe") String bienSoXe,
            @RequestParam("loaiXeId") Long loaiXeId,
            @RequestParam(value = "months", defaultValue = "1") int months,
            RedirectAttributes redirectAttributes
    ) {
        try {
            VeThang vt = monthlyTicketService.registerMonthlyPass(hoTen, soDienThoai, cccd, email, diaChi, bienSoXe, loaiXeId, months);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký vé tháng thành công! Mã thẻ: " + vt.getMaThe() + " | Biển số: " + vt.getBienSoXe());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/ve-thang";
    }

    @PostMapping("/renew")
    public String renewMonthlyTicket(
            @RequestParam("veThangId") Long veThangId,
            @RequestParam(value = "months", defaultValue = "1") int months,
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String role = (String) session.getAttribute("userRole");
        if ("NHAN_VIEN".equalsIgnoreCase(role)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Nhân viên trực cổng không có quyền gia hạn vé tháng. Thao tác này chỉ dành cho Ban quản lý / Admin!");
            return "redirect:/ve-thang";
        }
        try {
            VeThang vt = monthlyTicketService.renewMonthlyPass(veThangId, months);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Gia hạn vé tháng thành công cho biển số: " + vt.getBienSoXe() + " đến ngày " + vt.getNgayHetHan());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/ve-thang";
    }

    @PostMapping("/toggle")
    public String toggleStatus(
            @RequestParam("veThangId") Long veThangId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            monthlyTicketService.toggleStatus(veThangId);
            redirectAttributes.addFlashAttribute("successMessage", "Thay đổi trạng thái thẻ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/ve-thang";
    }

    @PostMapping({"/confirm-payment", "/xac-nhan-thanh-toan"})
    public String confirmPayment(
            @RequestParam("veThangId") Long veThangId,
            java.security.Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String staff = principal != null ? principal.getName() : "admin";
            VeThang vt = monthlyTicketService.xacNhanThanhToanVeThang(veThangId, staff);
            double giaTien = (vt.getGiaTien() != null) ? vt.getGiaTien() : 0.0;
            redirectAttributes.addFlashAttribute("successMessage",
                    "Xe [" + vt.getBienSoXe() + "] đã thanh toán thành công " + String.format("%,.0f", giaTien) + " VNĐ! Mã thẻ vé tháng: " + vt.getMaThe() + " đã được kích hoạt thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/ve-thang";
    }

    @PostMapping({"/delete", "/xoa"})
    public String deleteMonthlyTicket(
            @RequestParam("veThangId") Long veThangId,
            jakarta.servlet.http.HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String role = (String) session.getAttribute("userRole");
        if (role != null && "NHAN_VIEN".equalsIgnoreCase(role)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nhân viên trực cổng không có quyền xóa thẻ vé tháng! Thao tác này chỉ dành cho Quản trị viên (ADMIN).");
            return "redirect:/ve-thang";
        }
        try {
            monthlyTicketService.xoaVeThang(veThangId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa thành công thẻ vé tháng khỏi hệ thống!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa vé tháng: " + e.getMessage());
        }
        return "redirect:/ve-thang";
    }
}

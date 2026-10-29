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
            Model model
    ) {
        List<VeThang> tickets = monthlyTicketService.searchVeThang(keyword);
        List<LoaiXe> loaiXes = parkingLotService.getAllLoaiXe();

        model.addAttribute("tickets", tickets);
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalActive", monthlyTicketService.countActivePasses());
        model.addAttribute("activePage", "monthly-tickets");
        model.addAttribute("pageTitle", "Quản lý vé tháng");

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
            RedirectAttributes redirectAttributes
    ) {
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
}

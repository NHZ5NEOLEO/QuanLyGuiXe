package com.example.demo.controller;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.dto.KhuVucStatusDto;
import com.example.demo.dto.TrafficActivityDto;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.VeXe;
import com.example.demo.service.ParkingLotService;
import com.example.demo.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ReportService reportService;
    private final ParkingLotService parkingLotService;

    public DashboardController(ReportService reportService, ParkingLotService parkingLotService) {
        this.reportService = reportService;
        this.parkingLotService = parkingLotService;
    }

    @GetMapping({"/tong-quan", "/dashboard", "/index"})
    public String dashboard(
            @RequestParam(value = "period", defaultValue = "today") String period,
            HttpSession session,
            Authentication authentication,
            Model model
    ) {
        if (session != null && "NHAN_VIEN".equalsIgnoreCase((String) session.getAttribute("userRole"))) {
            return "redirect:/gui-xe";
        }
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_NHAN_VIEN".equalsIgnoreCase(a.getAuthority()))) {
            return "redirect:/gui-xe";
        }

        DashboardStatsDto stats = reportService.getDashboardStats(period);
        List<VeXe> recentActivities = reportService.getRecentActivities(10);
        TrafficActivityDto trafficActivity = reportService.getTrafficActivityLast7Days();
        List<KhuVucStatusDto> khuVucStatuses = reportService.getKhuVucStatuses();
        List<VeXe> overdueVehicles = reportService.getOverdueVehicles(5);
        Map<String, Double> revenue7Days = reportService.getRevenueLast7Days();
        Map<String, Long> vehicleDistribution = reportService.getVehicleTypeDistribution();
        List<KhuVuc> khuVucs = parkingLotService.getAllKhuVuc();

        Map<String, Double> paymentDistribution = reportService.getPaymentMethodDistribution();
        long overdueCount = reportService.getOverdueCount();
        Map<String, Long> hourlyTraffic = reportService.getHourlyTrafficToday();

        model.addAttribute("stats", stats);
        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("trafficActivity", trafficActivity);
        model.addAttribute("khuVucStatuses", khuVucStatuses);
        model.addAttribute("overdueVehicles", overdueVehicles);
        model.addAttribute("revenue7Days", revenue7Days);
        model.addAttribute("vehicleDistribution", vehicleDistribution);
        model.addAttribute("paymentDistribution", paymentDistribution);
        model.addAttribute("hourlyTraffic", hourlyTraffic);
        model.addAttribute("revenueByVehicleType", reportService.getRevenueByVehicleType());
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("khuVucs", khuVucs);
        model.addAttribute("loaiXes", parkingLotService.getAllLoaiXe());
        model.addAttribute("selectedPeriod", period);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Tổng quan bãi xe");

        return "tong-quan/index";
    }
}

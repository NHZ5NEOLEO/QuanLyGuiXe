package com.example.demo.controller;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class ReportController {

    private final ReportService reportService;
    private final VeXeRepository veXeRepository;

    public ReportController(ReportService reportService, VeXeRepository veXeRepository) {
        this.reportService = reportService;
        this.veXeRepository = veXeRepository;
    }

    @GetMapping({"/thong-ke", "/reports"})
    public String reportsPage(
            @RequestParam(value = "period", defaultValue = "7days") String period,
            Model model
    ) {
        DashboardStatsDto stats = reportService.getDashboardStats();

        LocalDate today = LocalDate.now();
        LocalDate startDate;

        if ("today".equalsIgnoreCase(period)) {
            startDate = today;
        } else if ("30days".equalsIgnoreCase(period)) {
            startDate = today.minusDays(29);
        } else if ("thisMonth".equalsIgnoreCase(period)) {
            startDate = today.withDayOfMonth(1);
        } else {
            // default 7days
            startDate = today.minusDays(6);
        }

        // Thống kê doanh thu theo từng ngày trong kỳ
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();
        Map<String, Long> dailyIn = new LinkedHashMap<>();
        Map<String, Long> dailyOut = new LinkedHashMap<>();

        DateTimeFormatter dFmt = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate cur = startDate;
        double periodTotalRevenue = 0.0;
        long periodTotalIn = 0;
        long periodTotalOut = 0;

        while (!cur.isAfter(today)) {
            LocalDateTime s = LocalDateTime.of(cur, LocalTime.MIN);
            LocalDateTime e = LocalDateTime.of(cur, LocalTime.MAX);

            Double rev = veXeRepository.calculateRevenueBetween(s, e);
            double rVal = rev != null ? rev : 0.0;
            dailyRevenue.put(cur.format(dFmt), rVal);
            periodTotalRevenue += rVal;

            long inCount = veXeRepository.countByThoiGianVaoBetween(s, e);
            dailyIn.put(cur.format(dFmt), inCount);
            periodTotalIn += inCount;

            long outCount = veXeRepository.countByThoiGianRaBetween(s, e);
            dailyOut.put(cur.format(dFmt), outCount);
            periodTotalOut += outCount;

            cur = cur.plusDays(1);
        }

        // Báo cáo chi tiết theo ngày
        List<Map<String, Object>> dailyReports = new ArrayList<>();
        LocalDate tableCur = today;
        while (!tableCur.isBefore(startDate)) {
            LocalDateTime s = LocalDateTime.of(tableCur, LocalTime.MIN);
            LocalDateTime e = LocalDateTime.of(tableCur, LocalTime.MAX);

            Map<String, Object> row = new HashMap<>();
            row.put("date", tableCur.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            row.put("inCount", veXeRepository.countByThoiGianVaoBetween(s, e));
            row.put("outCount", veXeRepository.countByThoiGianRaBetween(s, e));
            Double rev = veXeRepository.calculateRevenueBetween(s, e);
            row.put("revenue", rev != null ? rev : 0.0);
            dailyReports.add(row);

            tableCur = tableCur.minusDays(1);
        }

        model.addAttribute("stats", stats);
        model.addAttribute("period", period);
        model.addAttribute("dailyRevenue", dailyRevenue);
        model.addAttribute("dailyIn", dailyIn);
        model.addAttribute("dailyOut", dailyOut);
        model.addAttribute("vehicleDistribution", reportService.getVehicleTypeDistribution());
        model.addAttribute("dailyReports", dailyReports);
        model.addAttribute("periodTotalRevenue", periodTotalRevenue);
        model.addAttribute("periodTotalIn", periodTotalIn);
        model.addAttribute("periodTotalOut", periodTotalOut);
        model.addAttribute("periodTotalTransactions", periodTotalIn + periodTotalOut);
        model.addAttribute("pageTitle", "Thống kê & Báo cáo");
        model.addAttribute("activePage", "reports");

        return "thong-ke/index";
    }
}

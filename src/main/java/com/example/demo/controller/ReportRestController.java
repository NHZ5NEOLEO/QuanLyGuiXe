package com.example.demo.controller;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportRestController {

    private final ReportService reportService;
    private final VeXeRepository veXeRepository;
    private final KhuVucRepository khuVucRepository;
    private final LoaiXeRepository loaiXeRepository;

    public ReportRestController(
            ReportService reportService,
            VeXeRepository veXeRepository,
            KhuVucRepository khuVucRepository,
            LoaiXeRepository loaiXeRepository
    ) {
        this.reportService = reportService;
        this.veXeRepository = veXeRepository;
        this.khuVucRepository = khuVucRepository;
        this.loaiXeRepository = loaiXeRepository;
    }

    /**
     * API Thống kê tổng quan hoạt động bãi xe
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardStatsDto> getSummary(@RequestParam(value = "period", defaultValue = "today") String period) {
        DashboardStatsDto stats = reportService.getDashboardStats(period);
        return ResponseEntity.ok(stats);
    }

    /**
     * API Thống kê doanh thu theo khoảng thời gian và phân loại phương tiện
     */
    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(6);

        LocalDateTime startDt = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endDt = LocalDateTime.of(end, LocalTime.MAX);

        Double totalRevenue = veXeRepository.calculateRevenueBetween(startDt, endDt);

        // Doanh thu theo từng ngày
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate cur = start;
        while (!cur.isAfter(end)) {
            LocalDateTime dayStart = LocalDateTime.of(cur, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(cur, LocalTime.MAX);
            Double dayRev = veXeRepository.calculateRevenueBetween(dayStart, dayEnd);
            dailyRevenue.put(cur.format(fmt), dayRev != null ? dayRev : 0.0);
            cur = cur.plusDays(1);
        }

        // Doanh thu theo loại xe
        List<LoaiXe> loaiXes = loaiXeRepository.findAll();
        List<Map<String, Object>> revenueByVehicleType = new ArrayList<>();
        for (LoaiXe lx : loaiXes) {
            List<VeXe> outList = veXeRepository.searchHistory(null, "Đã ra", lx.getId(), startDt, endDt);
            double typeTotal = outList.stream().mapToDouble(v -> v.getChiPhi() != null ? v.getChiPhi() : 0.0).sum();
            Map<String, Object> item = new HashMap<>();
            item.put("vehicleTypeId", lx.getId());
            item.put("vehicleTypeName", lx.getTenLoai());
            item.put("ticketCount", outList.size());
            item.put("totalRevenue", typeTotal);
            revenueByVehicleType.add(item);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("startDate", start.toString());
        resp.put("endDate", end.toString());
        resp.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        resp.put("dailyRevenue", dailyRevenue);
        resp.put("revenueByVehicleType", revenueByVehicleType);

        return ResponseEntity.ok(resp);
    }

    /**
     * API Thống kê số lượng xe vào, ra và hiện diện trong bãi
     */
    @GetMapping("/vehicles")
    public ResponseEntity<?> getVehicleTraffic(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        LocalDate start = (startDate != null) ? startDate : end.minusDays(6);

        LocalDateTime startDt = LocalDateTime.of(start, LocalTime.MIN);
        LocalDateTime endDt = LocalDateTime.of(end, LocalTime.MAX);

        long vehiclesCurrentlyInLot = veXeRepository.countByTrangThai("Đang gửi");
        long totalSlots = khuVucRepository.findAll().stream().mapToLong(k -> k.getSucChua() != null ? k.getSucChua() : 0).sum();
        long emptySlots = Math.max(0, totalSlots - vehiclesCurrentlyInLot);

        long totalCheckIn = veXeRepository.countByThoiGianVaoBetween(startDt, endDt);
        long totalCheckOut = veXeRepository.countByThoiGianRaBetween(startDt, endDt);

        Map<String, Object> resp = new HashMap<>();
        resp.put("vehiclesCurrentlyInLot", vehiclesCurrentlyInLot);
        resp.put("totalSlots", totalSlots);
        resp.put("emptySlots", emptySlots);
        resp.put("periodTotalCheckIn", totalCheckIn);
        resp.put("periodTotalCheckOut", totalCheckOut);
        resp.put("startDate", start.toString());
        resp.put("endDate", end.toString());

        return ResponseEntity.ok(resp);
    }
}

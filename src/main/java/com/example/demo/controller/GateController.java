package com.example.demo.controller;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeThang;
import com.example.demo.model.VeXe;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.MonthlyTicketService;
import com.example.demo.service.ParkingLotService;
import com.example.demo.service.ParkingService;
import com.example.demo.util.BienSoXeHelper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.DatChoTruoc;
import com.example.demo.repository.DatChoTruocRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.service.BlacklistService;

@Controller
public class GateController {

    private final ParkingService parkingService;
    private final ParkingLotService parkingLotService;
    private final VeXeRepository veXeRepository;
    private final LoaiXeRepository loaiXeRepository;
    private final MonthlyTicketService monthlyTicketService;
    private final DatChoTruocRepository datChoTruocRepository;
    private final BlacklistService blacklistService;

    public GateController(
            ParkingService parkingService, 
            ParkingLotService parkingLotService, 
            VeXeRepository veXeRepository,
            LoaiXeRepository loaiXeRepository,
            MonthlyTicketService monthlyTicketService,
            DatChoTruocRepository datChoTruocRepository,
            BlacklistService blacklistService
    ) {
        this.parkingService = parkingService;
        this.parkingLotService = parkingLotService;
        this.veXeRepository = veXeRepository;
        this.loaiXeRepository = loaiXeRepository;
        this.monthlyTicketService = monthlyTicketService;
        this.datChoTruocRepository = datChoTruocRepository;
        this.blacklistService = blacklistService;
    }

    /**
     * Trang Xe Vào (Làn vào) - URL /gui-xe hoặc /xe-vao (hỗ trợ /parking/check-in)
     */
    @GetMapping({"/gui-xe", "/parking/check-in", "/xe-vao"})
    public String checkInPage(
            @RequestParam(value = "plate", required = false) String plate,
            @RequestParam(value = "bienSoXe", required = false) String bienSoXe,
            Model model
    ) {
        List<LoaiXe> loaiXes = parkingLotService.getAllLoaiXe();
        List<VeXe> recentCheckIns = veXeRepository.findTop10ByOrderByThoiGianVaoDesc();
        long activeCount = veXeRepository.countActiveVehicles();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todayCheckInsCount = veXeRepository.countByThoiGianVaoBetween(todayStart, todayEnd);
        long todayMotorbikesCount = veXeRepository.countTodayMotorbikes(todayStart, todayEnd);
        long todayCarsCount = veXeRepository.countTodayCars(todayStart, todayEnd);

        String preFilledPlate = plate != null ? plate : bienSoXe;
        model.addAttribute("inputBienSo", preFilledPlate);
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("todayCheckInsCount", todayCheckInsCount);
        model.addAttribute("todayMotorbikesCount", todayMotorbikesCount);
        model.addAttribute("todayCarsCount", todayCarsCount);
        model.addAttribute("recentCheckIns", recentCheckIns);
        model.addAttribute("activePage", "check-in");
        model.addAttribute("pageTitle", "Xe vào – Tiếp nhận phương tiện");

        return "gui-xe/gui-xe";
    }

    /**
     * API kiểm tra biển số trực tiếp: Xác thực định dạng, tự nhận diện loại xe, kiểm tra vé tháng & xe đang trong bãi
     */
    @GetMapping({"/api/xe-vao/check-plate", "/api/check-plate"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkPlate(@RequestParam(value = "plate", required = false) String plate) {
        Map<String, Object> res = new HashMap<>();
        if (plate == null || plate.trim().isEmpty()) {
            res.put("valid", false);
            return ResponseEntity.ok(res);
        }

        String raw = plate.trim();
        var vResult = BienSoXeHelper.validate(raw);
        res.put("valid", vResult.valid());
        res.put("message", vResult.message());
        res.put("displayValue", vResult.displayValue());
        res.put("cleanKey", vResult.cleanKey());

        if (!vResult.valid()) {
            return ResponseEntity.ok(res);
        }

        // 0. Kiểm tra Danh sách đen (Blacklist Security Alert)
        var blackOpt = blacklistService.checkBlacklist(vResult.displayValue());
        if (blackOpt.isEmpty() && !vResult.cleanKey().equalsIgnoreCase(vResult.displayValue())) {
            blackOpt = blacklistService.checkBlacklist(vResult.cleanKey());
        }
        if (blackOpt.isPresent()) {
            var bl = blackOpt.get();
            res.put("isBlacklisted", true);
            res.put("blacklistReason", bl.getLyDo());
            res.put("blacklistLevel", bl.getMucDo() != null ? bl.getMucDo() : "CAO");
        } else {
            res.put("isBlacklisted", false);
        }

        // 1. Tự động suy luận loại xe
        String inferredType = BienSoXeHelper.inferVehicleType(vResult.cleanKey());
        res.put("inferredType", inferredType); // "XE_MAY" hoặc "O_TO"

        List<LoaiXe> allLoaiXe = loaiXeRepository.findAll();
        Long suggestedLoaiXeId = null;
        String suggestedLoaiXeTen = null;
        for (LoaiXe lx : allLoaiXe) {
            String nameLower = lx.getTenLoai().toLowerCase();
            if ("XE_MAY".equals(inferredType) && (nameLower.contains("máy") || nameLower.contains("moto"))) {
                suggestedLoaiXeId = lx.getId();
                suggestedLoaiXeTen = lx.getTenLoai();
                break;
            } else if ("O_TO".equals(inferredType) && (nameLower.contains("tô") || nameLower.contains("car") || nameLower.contains("4-7"))) {
                suggestedLoaiXeId = lx.getId();
                suggestedLoaiXeTen = lx.getTenLoai();
                break;
            }
        }
        res.put("suggestedLoaiXeId", suggestedLoaiXeId);
        res.put("suggestedLoaiXeTen", suggestedLoaiXeTen);

        // 2. Kiểm tra xem xe này có đang ở trong bãi không
        List<VeXe> activeMatches = veXeRepository.findActiveByPlateList(vResult.displayValue(), vResult.cleanKey());
        if (!activeMatches.isEmpty()) {
            VeXe v = activeMatches.get(0);
            res.put("alreadyInLot", true);
            res.put("ticketId", v.getId());
            res.put("ticketCode", v.getMaQR());
            res.put("thoiGianVao", v.getThoiGianVao() != null ? v.getThoiGianVao().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")) : "");
        } else {
            res.put("alreadyInLot", false);
        }

        // 3. Kiểm tra xem xe có Vé Tháng hợp lệ còn hạn không
        Optional<VeThang> veThangOpt = monthlyTicketService.findActiveMonthlyPass(vResult.displayValue());
        if (veThangOpt.isEmpty() && !vResult.cleanKey().equalsIgnoreCase(vResult.displayValue())) {
            veThangOpt = monthlyTicketService.findActiveMonthlyPass(vResult.cleanKey());
        }
        if (veThangOpt.isPresent()) {
            VeThang vt = veThangOpt.get();
            res.put("hasMonthlyPass", true);
            res.put("monthlyPassCode", vt.getMaThe());
            res.put("customerName", vt.getKhachHang() != null ? vt.getKhachHang().getHoTen() : "Khách vé tháng");
            res.put("expiryDate", vt.getNgayHetHan() != null ? vt.getNgayHetHan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        } else {
            res.put("hasMonthlyPass", false);
        }

        // 4. Kiểm tra xem xe có Đơn Đặt Chỗ Trước hợp lệ (CHO_DEN) không
        Optional<DatChoTruoc> bookingOpt = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(vResult.displayValue(), "CHO_DEN");
        if (bookingOpt.isEmpty() && !vResult.cleanKey().equalsIgnoreCase(vResult.displayValue())) {
            bookingOpt = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(vResult.cleanKey(), "CHO_DEN");
        }
        if (bookingOpt.isPresent()) {
            DatChoTruoc bk = bookingOpt.get();
            res.put("hasPreBooking", true);
            res.put("bookingCode", bk.getMaDatCho());
            res.put("bookingCustomerName", bk.getHoTen());
            res.put("bookingPhone", bk.getSoDienThoai());
            res.put("bookingDeposit", bk.getTienCoc() != null ? bk.getTienCoc() : 20000.0);
            res.put("bookingDepositFormatted", String.format("%,.0f đ", bk.getTienCoc() != null ? bk.getTienCoc() : 20000.0));
            res.put("bookingExpiry", bk.getThoiGianHetHan() != null ? bk.getThoiGianHetHan().format(DateTimeFormatter.ofPattern("HH:mm dd/MM")) : "");
        } else {
            res.put("hasPreBooking", false);
        }

        // 5. Tự động nhận diện Tỉnh / Thành phố & Biển số đặc biệt (VIP, Tứ quý, Ngũ quý, Lộc phát...)
        String provinceName = BienSoXeHelper.getProvinceName(vResult.cleanKey());
        String specialDesc = BienSoXeHelper.getSpecialPlateDescription(vResult.cleanKey());
        res.put("provinceName", provinceName);
        res.put("specialDesc", specialDesc);

        // 6. Tra cứu lịch sử xe từng gửi vào bãi (Khách quen / ghi chú lần trước)
        List<VeXe> pastVisits = veXeRepository.findByBienSoXeOrderByThoiGianVaoDesc(vResult.displayValue());
        if (pastVisits.isEmpty() && !vResult.cleanKey().equalsIgnoreCase(vResult.displayValue())) {
            pastVisits = veXeRepository.findByBienSoXeOrderByThoiGianVaoDesc(vResult.cleanKey());
        }
        int visitCount = pastVisits.size();
        String pastNote = null;
        boolean hasDebt = false;
        double debtAmount = 0.0;
        String debtDetails = "";

        for (VeXe pv : pastVisits) {
            if (pv.getGhiChu() != null && !pv.getGhiChu().trim().isEmpty() && pastNote == null) {
                pastNote = pv.getGhiChu().trim();
            }
            if (!hasDebt && ("Chưa thanh toán".equalsIgnoreCase(pv.getTrangThai()) || 
                             "Nợ phí".equalsIgnoreCase(pv.getTrangThai()) || 
                             "Đang nợ".equalsIgnoreCase(pv.getTrangThai()))) {
                hasDebt = true;
                debtAmount = (pv.getChiPhi() != null ? pv.getChiPhi() : 0.0);
                debtDetails = "Lượt gửi ngày " + (pv.getThoiGianVao() != null ? pv.getThoiGianVao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "") +
                              " chưa thanh toán (" + String.format("%,.0f đ", debtAmount) + ", Mã vé: " + pv.getMaQR() + ")";
            }
        }
        res.put("hasDebt", hasDebt);
        res.put("debtAmount", debtAmount);
        res.put("debtAmountFormatted", String.format("%,.0f đ", debtAmount));
        res.put("debtDetails", debtDetails);
        res.put("visitCount", visitCount);
        res.put("pastNote", pastNote);

        // 7. Tạo gợi ý ghi chú thông minh (Smart Auto Note)
        StringBuilder autoNote = new StringBuilder();
        if (veThangOpt.isPresent()) {
            autoNote.append("[Vé tháng VIP] Khách: ").append(veThangOpt.get().getKhachHang() != null ? veThangOpt.get().getKhachHang().getHoTen() : "Khách vé tháng");
        } else if (bookingOpt.isPresent()) {
            autoNote.append("[Đặt chỗ] ").append(bookingOpt.get().getHoTen());
        }

        if (provinceName != null) {
            if (autoNote.length() > 0) autoNote.append(" • ");
            autoNote.append("Biển ").append(provinceName);
        }
        if (specialDesc != null) {
            if (autoNote.length() > 0) autoNote.append(" • ");
            autoNote.append(specialDesc);
        }
        if (visitCount > 1) {
            if (autoNote.length() > 0) autoNote.append(" • ");
            autoNote.append("Khách quen (lần ").append(visitCount + 1).append(")");
        }
        if (pastNote != null && !pastNote.isEmpty()) {
            res.put("pastNoteReminder", pastNote);
        }

        res.put("autoNote", autoNote.toString());

        return ResponseEntity.ok(res);
    }

    /**
     * Xử lý xe vào từ Form tại trang /gui-xe hoặc /xe-vao
     */
    @PostMapping({"/gui-xe", "/parking/check-in", "/xe-vao"})
    public String handleCheckIn(@ModelAttribute CheckInRequest request, RedirectAttributes redirectAttributes) {
        try {
            VeXe veXe = parkingService.checkIn(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Xe vào bãi thành công! Biển số: " + veXe.getBienSoXe() + " | Mã vé: " + veXe.getMaQR());
            redirectAttributes.addFlashAttribute("newTicket", veXe);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("inputBienSo", request.getBienSoXe());
            redirectAttributes.addFlashAttribute("inputLoaiXeId", request.getLoaiXeId());
        }
        return "redirect:/gui-xe";
    }

    /**
     * Trang Xe Ra (Làn ra & Thanh toán) - URL /xe-ra (hỗ trợ /nhan-xe và /parking/check-out)
     */
    @GetMapping({"/xe-ra", "/nhan-xe", "/parking/check-out"})
    public String checkOutPage(
            @RequestParam(value = "code", required = false) String code,
            Model model
    ) {
        if (code != null && !code.trim().isEmpty()) {
            try {
                CheckOutPreviewResponse preview = parkingService.previewCheckOut(code.trim());
                if (preview != null && preview.getBienSoXe() != null) {
                    var blackOpt = blacklistService.checkBlacklist(preview.getBienSoXe());
                    if (blackOpt.isPresent()) {
                        model.addAttribute("isBlacklisted", true);
                        model.addAttribute("blacklistReason", blackOpt.get().getLyDo());
                        model.addAttribute("blacklistLevel", blackOpt.get().getMucDo() != null ? blackOpt.get().getMucDo() : "CAO");
                    }
                }
                model.addAttribute("preview", preview);
                model.addAttribute("searchedCode", code.trim());
            } catch (Exception e) {
                model.addAttribute("errorMessage", e.getMessage());
                model.addAttribute("searchedCode", code.trim());
            }
        }

        List<VeXe> activeVehicles = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        long activeCount = veXeRepository.countActiveVehicles();

        // Thống kê ca trực Làn Ra hôm nay
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        long todayCheckOutsCount = veXeRepository.countByThoiGianRaBetween(todayStart, todayEnd);
        long todayMotorbikesOutCount = veXeRepository.countTodayMotorbikesOut(todayStart, todayEnd);
        long todayCarsOutCount = veXeRepository.countTodayCarsOut(todayStart, todayEnd);
        Double todayRevenue = veXeRepository.calculateRevenueBetween(todayStart, todayEnd);

        model.addAttribute("activeVehicles", activeVehicles);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("todayCheckOutsCount", todayCheckOutsCount);
        model.addAttribute("todayMotorbikesOutCount", todayMotorbikesOutCount);
        model.addAttribute("todayCarsOutCount", todayCarsOutCount);
        model.addAttribute("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);
        model.addAttribute("activePage", "check-out");
        model.addAttribute("pageTitle", "Xe ra – Thanh toán & Xuất bãi");

        return "nhan-xe/nhan-xe";
    }

    /**
     * Xử lý xác nhận Xe Ra & Thanh toán từ Form tại trang /xe-ra hoặc /nhan-xe
     */
    @PostMapping({"/xe-ra/xac-nhan", "/nhan-xe/xac-nhan", "/parking/check-out/confirm"})
    public String handleCheckOut(
            @RequestParam(value = "veXeId", required = false) Integer veXeId,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "hinhThucThanhToan", defaultValue = "TIEN_MAT") String hinhThucThanhToan,
            @RequestParam(value = "anhRaData", required = false) String anhRaData,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (veXeId == null && code != null && !code.trim().isEmpty()) {
                CheckOutPreviewResponse preview = parkingService.previewCheckOut(code.trim());
                veXeId = preview.getVeXeId();
            }
            if (veXeId == null) {
                throw new IllegalArgumentException("Vui lòng tra cứu hoặc chọn vé xe hợp lệ trước khi xác nhận!");
            }
            VeXe veXe = parkingService.confirmCheckOut(veXeId, hinhThucThanhToan, anhRaData, ghiChu);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thanh toán và cho xe ra thành công! Biển số: " + veXe.getBienSoXe() +
                            " | Phí: " + String.format("%,.0f", veXe.getChiPhi()) + " VNĐ | Cổng Barie đã mở.");
            redirectAttributes.addFlashAttribute("completedTicket", veXe);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/xe-ra";
    }

    // --- Legacy / Alias routes redirect to dedicated pages ---
    @GetMapping("/gate")
    public String gateAlias(@RequestParam(value = "tab", defaultValue = "checkin") String tab) {
        return "checkout".equalsIgnoreCase(tab) ? "redirect:/parking/check-out" : "redirect:/parking/check-in";
    }

    @GetMapping("/check-in")
    public String checkInAlias() {
        return "redirect:/parking/check-in";
    }

    @GetMapping("/check-out")
    public String checkOutAlias() {
        return "redirect:/parking/check-out";
    }

    @PostMapping("/checkin")
    public String legacyCheckInForm(@ModelAttribute CheckInRequest request, RedirectAttributes redirectAttributes) {
        return handleCheckIn(request, redirectAttributes);
    }

    @PostMapping("/checkout/confirm")
    public String legacyCheckOutConfirm(
            @RequestParam("veXeId") Integer veXeId,
            @RequestParam(value = "hinhThucThanhToan", defaultValue = "TIEN_MAT") String hinhThucThanhToan,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        return handleCheckOut(veXeId, null, hinhThucThanhToan, null, ghiChu, redirectAttributes);
    }
}

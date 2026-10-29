package com.example.demo.controller;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.service.BlacklistService;
import com.example.demo.service.MonthlyTicketService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.ParkingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gate")
public class ApiGateController {

    private final ParkingService parkingService;
    private final BlacklistService blacklistService;
    private final MonthlyTicketService monthlyTicketService;
    private final NotificationService notificationService;
    private final DatChoTruocRepository datChoTruocRepository;
    private final com.example.demo.repository.VeXeRepository veXeRepository;

    public ApiGateController(
            ParkingService parkingService,
            BlacklistService blacklistService,
            MonthlyTicketService monthlyTicketService,
            NotificationService notificationService,
            DatChoTruocRepository datChoTruocRepository,
            com.example.demo.repository.VeXeRepository veXeRepository
    ) {
        this.parkingService = parkingService;
        this.blacklistService = blacklistService;
        this.monthlyTicketService = monthlyTicketService;
        this.notificationService = notificationService;
        this.datChoTruocRepository = datChoTruocRepository;
        this.veXeRepository = veXeRepository;
    }

    /**
     * API kiểm tra nhanh biển số xe (Blacklist, Vé tháng và Đặt chỗ trước)
     */
    @GetMapping("/check-plate")
    public ResponseEntity<?> checkPlate(@RequestParam("plate") String plate) {
        Map<String, Object> resp = new HashMap<>();
        String cleanPlate = plate != null ? plate.trim().toUpperCase() : "";

        var blackOpt = blacklistService.checkBlacklist(cleanPlate);
        if (blackOpt.isPresent()) {
            resp.put("isBlacklisted", true);
            resp.put("blacklistReason", blackOpt.get().getLyDo());
            resp.put("blacklistLevel", blackOpt.get().getMucDo());

            // Tự động kích hoạt thông báo Telegram khẩn cấp cho Đội bảo vệ
            notificationService.sendTelegramSecurityAlert(
                "PHÁT HIỆN PHƯƠNG TIỆN DANH SÁCH ĐEN TẠI CỔNG",
                "🚗 *Biển số:* " + cleanPlate + "\n⚠️ *Lý do:* " + blackOpt.get().getLyDo() + "\n🔴 *Mức độ nguy hiểm:* " + blackOpt.get().getMucDo()
            );
        } else {
            resp.put("isBlacklisted", false);
        }

        var vtOpt = monthlyTicketService.findActiveMonthlyPass(cleanPlate);
        if (vtOpt.isPresent()) {
            resp.put("isMonthlyPass", true);
            resp.put("customerName", vtOpt.get().getKhachHang().getHoTen());
            resp.put("vehicleTypeId", vtOpt.get().getLoaiXe() != null ? vtOpt.get().getLoaiXe().getId() : null);
            resp.put("expireDate", vtOpt.get().getNgayHetHan().toString());
        } else {
            resp.put("isMonthlyPass", false);
        }

        // Kiểm tra xem xe có đặt chỗ trước trực tuyến không
        var bookOpt = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(cleanPlate, "CHO_DEN");
        if (bookOpt.isPresent()) {
            var bk = bookOpt.get();
            resp.put("isPreBooked", true);
            resp.put("preBookedSlot", bk.getMaViTri());
            resp.put("preBookedCode", bk.getMaDatCho());
            resp.put("preBookedCustomer", bk.getHoTen());
            resp.put("preBookedDeposit", bk.getTienCoc());
        } else {
            resp.put("isPreBooked", false);
        }

        return ResponseEntity.ok(resp);
    }

    /**
     * API Check-in bằng Ajax
     */
    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(@RequestBody CheckInRequest request) {
        try {
            VeXe veXe = parkingService.checkIn(request);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Xe vào bãi thành công!");
            resp.put("ticketId", veXe.getId());
            resp.put("maQR", veXe.getMaQR());
            resp.put("bienSoXe", veXe.getBienSoXe());
            resp.put("loaiXe", veXe.getLoaiXe() != null ? veXe.getLoaiXe().getTenLoai() : "");
            resp.put("maViTri", veXe.getMaViTri());
            resp.put("thoiGianVao", veXe.getThoiGianVao().toString());
            resp.put("laVeThang", veXe.getVeThang() != null);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * API Quét xem trước chi phí Check-out
     */
    @GetMapping("/check-out/preview")
    public ResponseEntity<?> previewCheckOut(@RequestParam("query") String query) {
        try {
            CheckOutPreviewResponse preview = parkingService.previewCheckOut(query);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * API Xác nhận Check-out hoàn tất
     */
    @PostMapping("/check-out/confirm")
    public ResponseEntity<?> confirmCheckOut(@RequestBody Map<String, Object> payload) {
        try {
            Integer veXeId = Integer.valueOf(payload.get("veXeId").toString());
            String hinhThuc = payload.getOrDefault("hinhThucThanhToan", "TIEN_MAT").toString();
            String anhRaData = payload.get("anhRaData") != null ? payload.get("anhRaData").toString() : null;
            String ghiChu = payload.get("ghiChu") != null ? payload.get("ghiChu").toString() : null;

            VeXe veXe = parkingService.confirmCheckOut(veXeId, hinhThuc, anhRaData, ghiChu);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Thanh toán và Check-out thành công! Cổng mở cho xe ra.");
            resp.put("ticketId", veXe.getId());
            resp.put("maQR", veXe.getMaQR());
            resp.put("bienSoXe", veXe.getBienSoXe());
            resp.put("loaiXe", veXe.getLoaiXe() != null ? veXe.getLoaiXe().getTenLoai() : "Phương tiện");
            resp.put("maViTri", veXe.getMaViTri());
            resp.put("thoiGianVao", veXe.getThoiGianVao() != null ? veXe.getThoiGianVao().toString() : "");
            resp.put("thoiGianRa", veXe.getThoiGianRa() != null ? veXe.getThoiGianRa().toString() : "");
            resp.put("chiPhi", veXe.getChiPhi());
            resp.put("hinhThucThanhToan", veXe.getHinhThucThanhToan());
            resp.put("laVeThang", veXe.getVeThang() != null);
            resp.put("khachHang", (veXe.getVeThang() != null && veXe.getVeThang().getKhachHang() != null) ? veXe.getVeThang().getKhachHang().getHoTen() : "Khách vãng lai");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * API kiểm tra xem vé xe đã được thanh toán hoặc hoàn thành chưa (dành cho polling tự động)
     */
    @GetMapping("/check-qr-paid")
    public ResponseEntity<?> checkQrPaid(
            @RequestParam(value = "veXeId", required = false) Integer veXeId,
            @RequestParam(value = "code", required = false) String code
    ) {
        Map<String, Object> resp = new HashMap<>();
        VeXe veXe = null;
        if (veXeId != null) {
            veXe = veXeRepository.findById(veXeId).orElse(null);
        } else if (code != null && !code.trim().isEmpty()) {
            veXe = veXeRepository.findByMaQR(code.trim()).orElse(null);
            if (veXe == null) {
                var list = veXeRepository.findByBienSoXeOrderByThoiGianVaoDesc(code.trim());
                if (!list.isEmpty()) veXe = list.get(0);
            }
        }

        if (veXe == null) {
            resp.put("found", false);
            resp.put("paid", false);
            return ResponseEntity.ok(resp);
        }

        boolean isPaid = "Đã ra".equalsIgnoreCase(veXe.getTrangThai()) ||
                         "Đã thanh toán".equalsIgnoreCase(veXe.getTrangThai()) ||
                         veXe.getThoiGianRa() != null;

        resp.put("found", true);
        resp.put("ticketId", veXe.getId());
        resp.put("plate", veXe.getBienSoXe());
        resp.put("paid", isPaid);
        resp.put("trangThai", veXe.getTrangThai());
        resp.put("chiPhi", veXe.getChiPhi());
        resp.put("hinhThucThanhToan", veXe.getHinhThucThanhToan());
        return ResponseEntity.ok(resp);
    }

    /**
     * API Mô phỏng / Webhook ghi nhận khách quét mã VietQR và chuyển khoản thành công
     * -> Tự động chốt thanh toán và kích hoạt tín hiệu mở Barie
     */
    @PostMapping("/simulate-vietqr-payment")
    public ResponseEntity<?> simulateVietQrPayment(@RequestBody Map<String, Object> payload) {
        try {
            Integer veXeId = null;
            if (payload.get("veXeId") != null) {
                veXeId = Integer.valueOf(payload.get("veXeId").toString());
            } else if (payload.get("code") != null) {
                String code = payload.get("code").toString();
                CheckOutPreviewResponse prev = parkingService.previewCheckOut(code);
                veXeId = prev.getVeXeId();
            }

            if (veXeId == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy vé xe!"));
            }

            String anhRaData = payload.get("anhRaData") != null ? payload.get("anhRaData").toString() : null;
            VeXe veXe = parkingService.confirmCheckOut(veXeId, "CHUYEN_KHOAN", anhRaData, "Thanh toán tự động qua VietQR");

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("paid", true);
            resp.put("message", "Đã nhận chuyển khoản VietQR thành công! Cổng Barie tự động nâng mở.");
            resp.put("ticketId", veXe.getId());
            resp.put("bienSoXe", veXe.getBienSoXe());
            resp.put("chiPhi", veXe.getChiPhi());
            resp.put("thoiGianRa", veXe.getThoiGianRa() != null ? veXe.getThoiGianRa().toString() : "");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}

package com.example.demo.controller;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.service.ParkingLotService;
import com.example.demo.service.ParkingService;
import com.example.demo.service.QrCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller phục vụ khách hàng quét mã QR tại cổng vào/ra.
 * Các trang này KHÔNG yêu cầu đăng nhập.
 * Khách hàng chỉ nhập thông tin, nhân viên phải xác nhận phía bên kia.
 */
@Controller
public class CustomerController {

    private final ParkingService parkingService;
    private final ParkingLotService parkingLotService;
    private final QrCodeService qrCodeService;

    public CustomerController(
            ParkingService parkingService,
            ParkingLotService parkingLotService,
            QrCodeService qrCodeService
    ) {
        this.parkingService = parkingService;
        this.parkingLotService = parkingLotService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Trang xe vào cho khách hàng (quét QR cổng vào).
     * Khách hàng nhập biển số và chọn loại xe.
     * Sau khi submit, hệ thống tạo lượt gửi chờ nhân viên xác nhận.
     */
    @GetMapping({"/khach-gui-xe", "/customer-checkin", "/khach-xe-vao"})
    public String customerCheckIn(Model model) {
        List<LoaiXe> loaiXes = parkingLotService.getAllLoaiXe();
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("pageTitle", "Xe vào bãi");
        return "khach-gui-xe";
    }

    /**
     * Trang xe ra cho khách hàng (quét QR cổng ra).
     * Khách hàng nhập mã vé hoặc biển số xe để tra cứu.
     */
    @GetMapping({"/khach-nhan-xe", "/customer-checkout", "/khach-xe-ra"})
    public String customerCheckOut(Model model) {
        model.addAttribute("pageTitle", "Xe ra bãi");
        return "khach-nhan-xe";
    }

    /**
     * API khách hàng gửi yêu cầu xe vào (không tự động xác nhận).
     */
    @PostMapping("/api/gate/qr-checkin")
    @ResponseBody
    public ResponseEntity<?> qrCheckIn(@RequestBody CheckInRequest request) {
        try {
            if (request.getBienSoXe() == null || request.getBienSoXe().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập biển số xe!"));
            }

            // Xác thực cấu trúc biển số xe Việt Nam bằng BienSoXeHelper
            var vResult = com.example.demo.util.BienSoXeHelper.validate(request.getBienSoXe());
            if (!vResult.valid()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", vResult.message()));
            }

            VeXe veXe = parkingService.checkIn(request);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Đã ghi nhận yêu cầu xe vào. Vui lòng chờ nhân viên xác nhận tại cổng.");
            resp.put("maVe", veXe.getMaQR());
            resp.put("bienSoXe", veXe.getBienSoXe());
            resp.put("loaiXe", veXe.getLoaiXe() != null ? veXe.getLoaiXe().getTenLoai() : "");
            resp.put("thoiGianVao", veXe.getThoiGianVao().toString());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * API khách hàng tra cứu lượt gửi để xe ra (không tự động xác nhận checkout).
     * Chỉ hiển thị thông tin, nhân viên mới có quyền xác nhận.
     */
    @GetMapping("/api/gate/qr-checkout-preview")
    @ResponseBody
    public ResponseEntity<?> qrCheckOutPreview(@RequestParam("query") String query) {
        try {
            CheckOutPreviewResponse preview = parkingService.previewCheckOut(query);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("veXeId", preview.getVeXeId());
            resp.put("maVe", preview.getMaQR());
            resp.put("bienSoXe", preview.getBienSoXe());
            resp.put("loaiXe", preview.getTenLoaiXe());
            resp.put("thoiGianVao", preview.getThoiGianVao() != null ? preview.getThoiGianVao().toString() : "");
            resp.put("thoiLuong", preview.getThoiLuong());
            resp.put("soTien", preview.getSoTien());
            resp.put("laVeThang", preview.isLaVeThang());
            resp.put("message", "Vui lòng đưa thông tin này cho nhân viên tại cổng ra để xác nhận thanh toán.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Trang hiển thị mã QR cổng vào (dành cho admin in QR dán tại cổng).
     */
    @GetMapping({"/qr-cong-vao", "/qr-entry"})
    public String qrEntryPage(HttpServletRequest request, Model model) {
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
        String qrImage = qrCodeService.generateEntryGateQr(baseUrl);
        model.addAttribute("qrImage", qrImage);
        model.addAttribute("gateType", "CỔNG VÀO");
        model.addAttribute("targetUrl", baseUrl + "/khach-gui-xe");
        model.addAttribute("pageTitle", "QR Cổng Vào");
        return "qr-gate";
    }

    /**
     * Trang hiển thị mã QR cổng ra (dành cho admin in QR dán tại cổng).
     */
    @GetMapping({"/qr-cong-ra", "/qr-exit"})
    public String qrExitPage(HttpServletRequest request, Model model) {
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + (request.getServerPort() != 80 && request.getServerPort() != 443
                ? ":" + request.getServerPort() : "");
        String qrImage = qrCodeService.generateExitGateQr(baseUrl);
        model.addAttribute("qrImage", qrImage);
        model.addAttribute("gateType", "CỔNG RA");
        model.addAttribute("targetUrl", baseUrl + "/khach-nhan-xe");
        model.addAttribute("pageTitle", "QR Cổng Ra");
        return "qr-gate";
    }
}

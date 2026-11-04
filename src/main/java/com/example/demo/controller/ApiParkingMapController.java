package com.example.demo.controller;

import com.example.demo.dto.ParkingOverviewDto;
import com.example.demo.model.VeXe;
import com.example.demo.service.BlacklistService;
import com.example.demo.service.ParkingLotService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/parking-map")
public class ApiParkingMapController {

    private final ParkingLotService parkingLotService;
    private final BlacklistService blacklistService;

    public ApiParkingMapController(ParkingLotService parkingLotService, BlacklistService blacklistService) {
        this.parkingLotService = parkingLotService;
        this.blacklistService = blacklistService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ParkingOverviewDto> getOverview() {
        ParkingOverviewDto overview = parkingLotService.layTongQuanBaiDoXe();
        return ResponseEntity.ok(overview);
    }

    @PostMapping("/verify-overdue")
    public ResponseEntity<?> verifyOverdue(
            @RequestBody Map<String, Object> payload,
            Principal principal,
            HttpSession session
    ) {
        try {
            if (payload == null || !payload.containsKey("veXeId")) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu mã lượt gửi"));
            }
            Integer veXeId = payload.get("veXeId") instanceof Number
                    ? ((Number) payload.get("veXeId")).intValue()
                    : Integer.parseInt(payload.get("veXeId").toString());
            String ghiChu = payload.get("ghiChu") != null ? payload.get("ghiChu").toString() : null;

            String staff = "Bảo vệ";
            if (session != null && session.getAttribute("userName") != null) {
                staff = (String) session.getAttribute("userName");
            } else if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
                staff = principal.getName();
            }

            VeXe vx = blacklistService.ghiNhanKiemTraXeQuaGio(veXeId, ghiChu, staff);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã ghi nhận kiểm tra phương tiện [" + vx.getBienSoXe() + "] thành công.",
                    "daKiemTra", true,
                    "nguoiKiemTra", vx.getNguoiKiemTra() != null ? vx.getNguoiKiemTra() : "Bảo vệ",
                    "ghiChuKiemTra", vx.getGhiChuKiemTra() != null ? vx.getGhiChuKiemTra() : "",
                    "thoiGianKiemTra", vx.getThoiGianKiemTra() != null ? vx.getThoiGianKiemTra().toString() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }
}


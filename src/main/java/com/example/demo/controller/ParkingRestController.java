package com.example.demo.controller;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.dto.CheckOutRequest;
import com.example.demo.dto.TicketResponseDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.VeXe;
import com.example.demo.service.ParkingService;
import com.example.demo.service.QrCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/parking")
public class ParkingRestController {

    private final ParkingService parkingService;
    private final QrCodeService qrCodeService;

    public ParkingRestController(ParkingService parkingService, QrCodeService qrCodeService) {
        this.parkingService = parkingService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * API Check-in tạo lượt gửi xe mới (Thời gian ghi nhận từ máy chủ)
     */
    @PostMapping("/check-in")
    public ResponseEntity<TicketResponseDto> checkIn(@RequestBody CheckInRequest request) {
        VeXe veXe = parkingService.checkIn(request);
        TicketResponseDto dto = parkingService.mapToTicketResponseDto(veXe);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * API Xem trước thông tin và tính toán phí gửi xe trước khi Check-out
     */
    @PostMapping("/preview-checkout")
    public ResponseEntity<CheckOutPreviewResponse> previewCheckOut(@RequestBody Map<String, String> payload) {
        String identifier = payload.get("identifier");
        if (identifier == null || identifier.trim().isEmpty()) {
            identifier = payload.get("query");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng cung cấp mã vé hoặc biển số xe (identifier)!");
        }
        CheckOutPreviewResponse preview = parkingService.previewCheckOut(identifier.trim());
        return ResponseEntity.ok(preview);
    }

    /**
     * API Xác nhận nhận xe (Check-out) và hoàn tất thanh toán
     */
    @PostMapping("/check-out")
    public ResponseEntity<TicketResponseDto> checkOut(@RequestBody CheckOutRequest request, java.security.Principal principal) {
        Integer veXeId = request.getVeXeId();
        if (veXeId == null && request.getTicketCode() != null) {
            VeXe existing = parkingService.getTicketByCode(request.getTicketCode());
            veXeId = existing.getId();
        }
        if (veXeId == null) {
            throw new BadRequestException("Vui lòng cung cấp veXeId hoặc ticketCode để thực hiện Check-out!");
        }

        String staff = principal != null ? principal.getName() : "api_staff";
        VeXe completed = parkingService.confirmCheckOut(
                veXeId,
                request.getHinhThucThanhToan(),
                request.getAnhRaData(),
                request.getGhiChu(),
                null,
                staff
        );
        TicketResponseDto dto = parkingService.mapToTicketResponseDto(completed);
        return ResponseEntity.ok(dto);
    }

    /**
     * API Tạo và tải ảnh mã QR dạng PNG trực tiếp từ máy chủ
     */
    @GetMapping(value = "/qr/{ticketCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getTicketQrImage(@PathVariable("ticketCode") String ticketCode) {
        byte[] imageBytes = qrCodeService.generateQrPngBytes(ticketCode, 300, 300);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-" + ticketCode + ".png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }
}

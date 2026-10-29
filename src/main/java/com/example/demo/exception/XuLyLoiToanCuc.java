package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(annotations = org.springframework.web.bind.annotation.RestController.class)
public class XuLyLoiToanCuc {

    private static final Logger log = LoggerFactory.getLogger(XuLyLoiToanCuc.class);

    @ExceptionHandler({KhongTimThayDuLieuException.class, ResourceNotFoundException.class})
    public ResponseEntity<PhanHoiLoi> handleKhongTimThay(Exception ex, HttpServletRequest request) {
        log.warn("Không tìm thấy dữ liệu: {} - Đường dẫn: {}", ex.getMessage(), request.getRequestURI());
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler({XeDangTrongBaiException.class, VehicleAlreadyParkedException.class})
    public ResponseEntity<PhanHoiLoi> handleXeDangTrongBai(Exception ex, HttpServletRequest request) {
        log.warn("Xe đang gửi trong bãi: {} - Đường dẫn: {}", ex.getMessage(), request.getRequestURI());
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.CONFLICT.value(),
                "XE_DANG_TRONG_BAI",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler({TrangThaiVeKhongHopLeException.class, InvalidTicketStateException.class})
    public ResponseEntity<PhanHoiLoi> handleTrangThaiVeKhongHopLe(Exception ex, HttpServletRequest request) {
        log.warn("Trạng thái vé không hợp lệ: {} - Đường dẫn: {}", ex.getMessage(), request.getRequestURI());
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.BAD_REQUEST.value(),
                "TRANG_THAI_VE_KHONG_HOP_LE",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({MaQrKhongHopLeException.class, InvalidQrCodeException.class})
    public ResponseEntity<PhanHoiLoi> handleMaQrKhongHopLe(Exception ex, HttpServletRequest request) {
        log.warn("Mã QR không hợp lệ: {} - Đường dẫn: {}", ex.getMessage(), request.getRequestURI());
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.BAD_REQUEST.value(),
                "MA_QR_KHONG_HOP_LE",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({YeuCauKhongHopLeException.class, BadRequestException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<PhanHoiLoi> handleYeuCauKhongHopLe(Exception ex, HttpServletRequest request) {
        log.warn("Yêu cầu không hợp lệ: {} - Đường dẫn: {}", ex.getMessage(), request.getRequestURI());
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.BAD_REQUEST.value(),
                "YEU_CAU_KHONG_HOP_LE",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({NghiepVuException.class, BusinessException.class})
    public ResponseEntity<PhanHoiLoi> handleNghiepVu(Exception ex, HttpServletRequest request) {
        log.warn("Vi phạm quy tắc nghiệp vụ: {} - Đường dẫn: {}", ex.getMessage(), request.getRequestURI());
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.BAD_REQUEST.value(),
                "VI_PHAM_NGHIEP_VU",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PhanHoiLoi> handleXacThuc(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Dữ liệu đầu vào không hợp lệ",
                request.getRequestURI()
        );
        error.setChiTietLoi(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<PhanHoiLoi> handleTuChoiTruyCap(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Từ chối quyền truy cập: {}", request.getRequestURI());
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.FORBIDDEN.value(),
                "TU_CHOI_TRUY_CAP",
                "Bạn không có quyền truy cập chức năng này!",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<PhanHoiLoi> handleLoiHeThong(Exception ex, HttpServletRequest request) {
        log.error("Lỗi hệ thống máy chủ: ", ex);
        PhanHoiLoi error = new PhanHoiLoi(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "LOI_HE_THONG",
                "Đã xảy ra lỗi trên hệ thống máy chủ. Vui lòng thử lại sau!",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

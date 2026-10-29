package com.example.demo.service.impl;

import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.InvalidTicketStateException;
import com.example.demo.model.LoaiXe;
import com.example.demo.service.FeeCalculationService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service("tinhPhiService")
public class FeeCalculationServiceImpl implements FeeCalculationService {

    @Override
    public FeeCalculationResult calculate(LoaiXe loaiXe, LocalDateTime thoiGianVao, LocalDateTime thoiGianRa, boolean laVeThang, boolean daCocTruoc) {
        if (thoiGianVao == null || thoiGianRa == null) {
            throw new BadRequestException("Thời gian vào và thời gian ra không được để trống!");
        }

        if (thoiGianRa.isBefore(thoiGianVao)) {
            throw new InvalidTicketStateException("Thời gian ra không được nhỏ hơn thời gian vào!");
        }

        Duration duration = Duration.between(thoiGianVao, thoiGianRa);
        long totalMinutes = Math.max(duration.toMinutes(), 1);

        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" ngày ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append(" giờ ");
        }
        sb.append(minutes).append(" phút");
        String thoiLuongStr = sb.toString().trim();

        FeeCalculationResult result = new FeeCalculationResult();
        result.setThoiGianVao(thoiGianVao);
        result.setThoiGianRa(thoiGianRa);
        result.setTongPhut(totalMinutes);
        result.setThoiLuongText(thoiLuongStr);
        result.setLaVeThang(laVeThang);

        // Trường hợp vé tháng còn hạn
        if (laVeThang) {
            result.setGiaBanNgay(0.0);
            result.setGiaBanDem(0.0);
            result.setTongTienGoc(0.0);
            result.setTienKhauTru(0.0);
            result.setSoTienThanhToan(0.0);
            result.setQuyTacApDung("Thuê bao vé tháng (Miễn phí lượt gửi)");
            return result;
        }

        // Lấy đơn giá từ cấu hình Loại xe
        double giaNgay = (loaiXe != null && loaiXe.getGiaBanNgay() != null) ? loaiXe.getGiaBanNgay() : 5000.0;
        double giaDem = (loaiXe != null && loaiXe.getGiaBanDem() != null) ? loaiXe.getGiaBanDem() : 10000.0;

        result.setGiaBanNgay(giaNgay);
        result.setGiaBanDem(giaDem);

        double baseFee;
        String quyTac;

        if (totalMinutes <= 24 * 60) {
            int gioVao = thoiGianVao.getHour();
            int gioRa = thoiGianRa.getHour();
            boolean quaDem = !thoiGianVao.toLocalDate().isEqual(thoiGianRa.toLocalDate())
                    || (gioVao >= 22 || gioVao < 6)
                    || (gioRa >= 22 || gioRa < 6)
                    || (totalMinutes > 12 * 60);

            if (quaDem) {
                baseFee = giaDem;
                quyTac = "Biểu phí ban đêm / trên 12 giờ (" + String.format("%,.0f", giaDem) + " VNĐ)";
            } else {
                baseFee = giaNgay;
                quyTac = "Biểu phí ban ngày 06:00 - 21:59 (" + String.format("%,.0f", giaNgay) + " VNĐ)";
            }
        } else {
            long soNgay = totalMinutes / (24 * 60);
            long phutDu = totalMinutes % (24 * 60);
            baseFee = soNgay * (giaNgay + giaDem);

            if (phutDu > 12 * 60) {
                baseFee += (giaNgay + giaDem);
                quyTac = String.format("Lưu bãi nhiều ngày (%d ngày + phát sinh qua đêm)", soNgay);
            } else if (phutDu > 0) {
                baseFee += giaNgay;
                quyTac = String.format("Lưu bãi nhiều ngày (%d ngày + phát sinh ban ngày)", soNgay);
            } else {
                quyTac = String.format("Lưu bãi trọn vẹn %d ngày", soNgay);
            }
        }

        double tienKhauTru = 0.0;
        if (daCocTruoc) {
            tienKhauTru = 20000.0;
        }

        double soTienThanhToan = Math.max(0.0, baseFee - tienKhauTru);

        result.setTongTienGoc(baseFee);
        result.setTienKhauTru(tienKhauTru);
        result.setSoTienThanhToan(soTienThanhToan);
        result.setQuyTacApDung(quyTac);

        return result;
    }
}

package com.example.demo.service;

import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.dto.KetQuaTinhPhi;
import com.example.demo.model.LoaiXe;

import java.time.LocalDateTime;

public interface TinhPhiService {

    KetQuaTinhPhi tinhPhi(LoaiXe loaiXe, LocalDateTime thoiGianVao, LocalDateTime thoiGianRa, boolean laVeThang, boolean daCocTruoc);

    default FeeCalculationResult calculate(LoaiXe loaiXe, LocalDateTime thoiGianVao, LocalDateTime thoiGianRa, boolean laVeThang, boolean daCocTruoc) {
        KetQuaTinhPhi res = tinhPhi(loaiXe, thoiGianVao, thoiGianRa, laVeThang, daCocTruoc);
        if (res instanceof FeeCalculationResult) {
            return (FeeCalculationResult) res;
        }
        FeeCalculationResult dto = new FeeCalculationResult();
        dto.setThoiGianVao(res.getThoiGianVao());
        dto.setThoiGianRa(res.getThoiGianRa());
        dto.setTongPhut(res.getTongPhut());
        dto.setThoiLuongText(res.getThoiLuongText());
        dto.setGiaBanNgay(res.getGiaBanNgay());
        dto.setGiaBanDem(res.getGiaBanDem());
        dto.setQuyTacApDung(res.getQuyTacApDung());
        dto.setTongTienGoc(res.getTongTienGoc());
        dto.setTienKhauTru(res.getTienKhauTru());
        dto.setSoTienThanhToan(res.getSoTienThanhToan());
        dto.setLaVeThang(res.isLaVeThang());
        return dto;
    }
}

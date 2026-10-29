package com.example.demo.service;

import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.dto.KetQuaTinhPhi;
import com.example.demo.model.LoaiXe;

import java.time.LocalDateTime;

/**
 * Lớp tương thích ngược kế thừa từ {@link TinhPhiService}
 */
public interface FeeCalculationService extends TinhPhiService {

    FeeCalculationResult calculate(LoaiXe loaiXe, LocalDateTime thoiGianVao, LocalDateTime thoiGianRa, boolean laVeThang, boolean daCocTruoc);

    @Override
    default KetQuaTinhPhi tinhPhi(LoaiXe loaiXe, LocalDateTime thoiGianVao, LocalDateTime thoiGianRa, boolean laVeThang, boolean daCocTruoc) {
        return calculate(loaiXe, thoiGianVao, thoiGianRa, laVeThang, daCocTruoc);
    }
}

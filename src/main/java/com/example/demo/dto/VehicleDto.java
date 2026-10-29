package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * Lớp tương thích ngược kế thừa từ {@link PhuongTienDto}
 */
public class VehicleDto extends PhuongTienDto {
    public VehicleDto() {
        super();
    }

    public VehicleDto(Long id, String maPhuongTien, String bienSoXe, Long loaiXeId, String tenLoaiXe, String chuXe, String soDienThoai, String moTa, LocalDateTime ngayTao) {
        super(id, maPhuongTien, bienSoXe, loaiXeId, tenLoaiXe, chuXe, soDienThoai, moTa, ngayTao);
    }
}

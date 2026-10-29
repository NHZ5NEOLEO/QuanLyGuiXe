package com.example.demo.dto;

/**
 * Lớp tương thích ngược kế thừa từ {@link TrangThaiKhuVucDto}
 */
public class KhuVucStatusDto extends TrangThaiKhuVucDto {
    public KhuVucStatusDto() {
        super();
    }

    public KhuVucStatusDto(Long id, String maKhu, String tenKhu, String viTri, String tenLoaiXe, long tongCho, long choTrong) {
        super(id, maKhu, tenKhu, viTri, tenLoaiXe, tongCho, choTrong);
    }

    public KhuVucStatusDto(Long id, String maKhu, String tenKhu, String viTri, String tenLoaiXe, long tongCho, long choTrong, long soLuongDatTruoc) {
        super(id, maKhu, tenKhu, viTri, tenLoaiXe, tongCho, choTrong, soLuongDatTruoc);
    }
}

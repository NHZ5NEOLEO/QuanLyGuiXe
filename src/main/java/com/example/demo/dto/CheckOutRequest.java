package com.example.demo.dto;

/**
 * Lớp tương thích ngược kế thừa từ {@link YeuCauXuatBai}
 */
public class CheckOutRequest extends YeuCauXuatBai {
    public CheckOutRequest() {
        super();
    }

    public CheckOutRequest(Integer veXeId, String ticketCode, String hinhThucThanhToan, String anhRaData, String ghiChu) {
        super(veXeId, ticketCode, hinhThucThanhToan, anhRaData, ghiChu);
    }
}

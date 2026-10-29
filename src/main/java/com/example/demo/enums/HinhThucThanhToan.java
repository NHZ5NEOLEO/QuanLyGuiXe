package com.example.demo.enums;

public enum HinhThucThanhToan {
    TIEN_MAT("Tiền mặt"),
    CHUYEN_KHOAN("Chuyển khoản VietQR"),
    VE_THANG("Vé tháng");

    private final String moTa;

    HinhThucThanhToan(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}

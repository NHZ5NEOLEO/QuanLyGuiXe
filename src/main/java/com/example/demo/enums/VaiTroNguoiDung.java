package com.example.demo.enums;

public enum VaiTroNguoiDung {
    ADMIN("Quản trị viên"),
    NHAN_VIEN("Nhân viên trực cổng"),
    KHACH_HANG("Khách hàng");

    private final String moTa;

    VaiTroNguoiDung(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}

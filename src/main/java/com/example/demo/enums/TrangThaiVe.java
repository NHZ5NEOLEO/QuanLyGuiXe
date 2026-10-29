package com.example.demo.enums;

public enum TrangThaiVe {
    DANG_GUI("Đang gửi"),
    DA_RA("Đã ra");

    private final String moTa;

    TrangThaiVe(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}

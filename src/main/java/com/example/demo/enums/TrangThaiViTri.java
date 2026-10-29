package com.example.demo.enums;

public enum TrangThaiViTri {
    TRONG("TRONG"),
    DA_DO("DA_DO"),
    DAT_TRUOC("DAT_TRUOC"),
    BAO_TRI("BAO_TRI");

    private final String maTrangThai;

    TrangThaiViTri(String maTrangThai) {
        this.maTrangThai = maTrangThai;
    }

    public String getMaTrangThai() {
        return maTrangThai;
    }
}

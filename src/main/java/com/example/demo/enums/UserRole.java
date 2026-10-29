package com.example.demo.enums;

public enum UserRole {
    ADMIN("Quản trị viên"),
    NHAN_VIEN("Nhân viên trực cổng"),
    KHACH_HANG("Khách hàng");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

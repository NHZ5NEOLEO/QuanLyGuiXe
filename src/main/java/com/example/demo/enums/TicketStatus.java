package com.example.demo.enums;

public enum TicketStatus {
    DANG_GUI("Đang gửi"),
    DA_RA("Đã ra");

    private final String description;

    TicketStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

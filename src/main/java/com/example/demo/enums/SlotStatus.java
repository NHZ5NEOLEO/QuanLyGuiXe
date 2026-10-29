package com.example.demo.enums;

public enum SlotStatus {
    TRONG("TRONG"),
    DA_DO("DA_DO"),
    DAT_TRUOC("DAT_TRUOC"),
    BAO_TRI("BAO_TRI");

    private final String code;

    SlotStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

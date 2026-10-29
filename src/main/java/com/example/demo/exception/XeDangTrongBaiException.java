package com.example.demo.exception;

public class XeDangTrongBaiException extends RuntimeException {
    public XeDangTrongBaiException(String thongBao) {
        super(thongBao);
    }
}

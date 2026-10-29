package com.example.demo.exception;

public class KhongTimThayDuLieuException extends RuntimeException {
    public KhongTimThayDuLieuException(String thongBao) {
        super(thongBao);
    }
}

package com.example.demo.exception;

public class TrangThaiVeKhongHopLeException extends RuntimeException {
    public TrangThaiVeKhongHopLeException(String thongBao) {
        super(thongBao);
    }
}

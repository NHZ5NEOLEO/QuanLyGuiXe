package com.example.demo.exception;

public class InvalidQrCodeException extends RuntimeException {
    public InvalidQrCodeException(String message) {
        super(message);
    }
}

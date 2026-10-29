package com.example.demo.exception;

/**
 * Lớp tương thích ngược kế thừa từ {@link PhanHoiLoi}
 */
public class ErrorResponse extends PhanHoiLoi {
    public ErrorResponse() {
        super();
    }

    public ErrorResponse(int status, String error, String message) {
        super(status, error, message);
    }

    public ErrorResponse(int status, String error, String message, String path) {
        super(status, error, message, path);
    }
}

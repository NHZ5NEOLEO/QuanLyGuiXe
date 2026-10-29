package com.example.demo.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhanHoiLoi {

    private int maTrangThai;
    private String loaiLoi;
    private String thongBao;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime thoiGian;

    private String duongDan;
    private Map<String, String> chiTietLoi;

    public PhanHoiLoi() {
        this.thoiGian = LocalDateTime.now();
    }

    public PhanHoiLoi(int maTrangThai, String loaiLoi, String thongBao) {
        this.maTrangThai = maTrangThai;
        this.loaiLoi = loaiLoi;
        this.thongBao = thongBao;
        this.thoiGian = LocalDateTime.now();
    }

    public PhanHoiLoi(int maTrangThai, String loaiLoi, String thongBao, String duongDan) {
        this.maTrangThai = maTrangThai;
        this.loaiLoi = loaiLoi;
        this.thongBao = thongBao;
        this.duongDan = duongDan;
        this.thoiGian = LocalDateTime.now();
    }

    public int getMaTrangThai() {
        return maTrangThai;
    }

    public int getStatus() {
        return maTrangThai;
    }

    public void setMaTrangThai(int maTrangThai) {
        this.maTrangThai = maTrangThai;
    }

    public String getLoaiLoi() {
        return loaiLoi;
    }

    public String getError() {
        return loaiLoi;
    }

    public void setLoaiLoi(String loaiLoi) {
        this.loaiLoi = loaiLoi;
    }

    public String getThongBao() {
        return thongBao;
    }

    public String getMessage() {
        return thongBao;
    }

    public void setThongBao(String thongBao) {
        this.thongBao = thongBao;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public LocalDateTime getTimestamp() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public String getDuongDan() {
        return duongDan;
    }

    public String getPath() {
        return duongDan;
    }

    public void setDuongDan(String duongDan) {
        this.duongDan = duongDan;
    }

    public Map<String, String> getChiTietLoi() {
        return chiTietLoi;
    }

    public Map<String, String> getValidationErrors() {
        return chiTietLoi;
    }

    public void setChiTietLoi(Map<String, String> chiTietLoi) {
        this.chiTietLoi = chiTietLoi;
    }
}

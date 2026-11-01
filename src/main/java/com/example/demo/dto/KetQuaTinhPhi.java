package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class KetQuaTinhPhi {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime thoiGianVao;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime thoiGianRa;

    private long tongPhut;
    private String thoiLuongText;
    private Double giaBanNgay;
    private Double giaBanDem;
    private String quyTacApDung;
    private Double tongTienGoc;
    private Double tienKhauTru;
    private Double soTienThanhToan;
    private boolean laVeThang;
    private Double phuPhiLuuKho = 0.0; // Phụ phí lưu kho khi xe gửi quá 24h
    private long soNgayVuot24h = 0;    // Số ngày vượt mốc 24h

    public KetQuaTinhPhi() {
    }

    public LocalDateTime getThoiGianVao() {
        return thoiGianVao;
    }

    public void setThoiGianVao(LocalDateTime thoiGianVao) {
        this.thoiGianVao = thoiGianVao;
    }

    public LocalDateTime getThoiGianRa() {
        return thoiGianRa;
    }

    public void setThoiGianRa(LocalDateTime thoiGianRa) {
        this.thoiGianRa = thoiGianRa;
    }

    public long getTongPhut() {
        return tongPhut;
    }

    public void setTongPhut(long tongPhut) {
        this.tongPhut = tongPhut;
    }

    public String getThoiLuongText() {
        return thoiLuongText;
    }

    public void setThoiLuongText(String thoiLuongText) {
        this.thoiLuongText = thoiLuongText;
    }

    public Double getGiaBanNgay() {
        return giaBanNgay;
    }

    public void setGiaBanNgay(Double giaBanNgay) {
        this.giaBanNgay = giaBanNgay;
    }

    public Double getGiaBanDem() {
        return giaBanDem;
    }

    public void setGiaBanDem(Double giaBanDem) {
        this.giaBanDem = giaBanDem;
    }

    public String getQuyTacApDung() {
        return quyTacApDung;
    }

    public void setQuyTacApDung(String quyTacApDung) {
        this.quyTacApDung = quyTacApDung;
    }

    public Double getTongTienGoc() {
        return tongTienGoc;
    }

    public void setTongTienGoc(Double tongTienGoc) {
        this.tongTienGoc = tongTienGoc;
    }

    public Double getTienKhauTru() {
        return tienKhauTru;
    }

    public void setTienKhauTru(Double tienKhauTru) {
        this.tienKhauTru = tienKhauTru;
    }

    public Double getSoTienThanhToan() {
        return soTienThanhToan;
    }

    public void setSoTienThanhToan(Double soTienThanhToan) {
        this.soTienThanhToan = soTienThanhToan;
    }

    public boolean isLaVeThang() {
        return laVeThang;
    }

    public void setLaVeThang(boolean laVeThang) {
        this.laVeThang = laVeThang;
    }

    public Double getPhuPhiLuuKho() {
        return phuPhiLuuKho;
    }

    public void setPhuPhiLuuKho(Double phuPhiLuuKho) {
        this.phuPhiLuuKho = phuPhiLuuKho;
    }

    public long getSoNgayVuot24h() {
        return soNgayVuot24h;
    }

    public void setSoNgayVuot24h(long soNgayVuot24h) {
        this.soNgayVuot24h = soNgayVuot24h;
    }
}

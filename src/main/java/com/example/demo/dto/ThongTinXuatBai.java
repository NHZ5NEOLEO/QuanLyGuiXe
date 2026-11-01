package com.example.demo.dto;

import java.time.LocalDateTime;

public class ThongTinXuatBai {
    private Integer veXeId;
    private String maQR;
    private String bienSoXe;
    private String tenLoaiXe;
    private String maViTri;
    private LocalDateTime thoiGianVao;
    private LocalDateTime thoiGianRa;
    private String thoiLuong;
    private boolean laVeThang;
    private String tenKhachHang;
    private Double soTien;
    private Double phuPhiLuuKho = 0.0;
    private String quyTacApDung;
    private String vietQrUrl;
    private String anhVaoUrl;

    public ThongTinXuatBai() {
    }

    public Integer getVeXeId() {
        return veXeId;
    }

    public void setVeXeId(Integer veXeId) {
        this.veXeId = veXeId;
    }

    public String getMaQR() {
        return maQR;
    }

    public void setMaQR(String maQR) {
        this.maQR = maQR;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public String getTenLoaiXe() {
        return tenLoaiXe;
    }

    public void setTenLoaiXe(String tenLoaiXe) {
        this.tenLoaiXe = tenLoaiXe;
    }

    public String getMaViTri() {
        return maViTri;
    }

    public void setMaViTri(String maViTri) {
        this.maViTri = maViTri;
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

    public String getThoiLuong() {
        return thoiLuong;
    }

    public void setThoiLuong(String thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public boolean isLaVeThang() {
        return laVeThang;
    }

    public void setLaVeThang(boolean laVeThang) {
        this.laVeThang = laVeThang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public Double getSoTien() {
        return soTien;
    }

    public void setSoTien(Double soTien) {
        this.soTien = soTien;
    }

    public String getVietQrUrl() {
        return vietQrUrl;
    }

    public void setVietQrUrl(String vietQrUrl) {
        this.vietQrUrl = vietQrUrl;
    }

    public String getAnhVaoUrl() {
        return anhVaoUrl;
    }

    public void setAnhVaoUrl(String anhVaoUrl) {
        this.anhVaoUrl = anhVaoUrl;
    }

    public Double getPhuPhiLuuKho() {
        return phuPhiLuuKho;
    }

    public void setPhuPhiLuuKho(Double phuPhiLuuKho) {
        this.phuPhiLuuKho = phuPhiLuuKho;
    }

    public String getQuyTacApDung() {
        return quyTacApDung;
    }

    public void setQuyTacApDung(String quyTacApDung) {
        this.quyTacApDung = quyTacApDung;
    }
}

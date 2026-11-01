package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * DTO đại diện cho một phương tiện đang gửi trong bãi đỗ xe
 */
public class PhuongTienDangGuiDto {

    private Integer id;
    private String maQR;
    private String bienSoXe;
    private Long loaiXeId;
    private String tenLoaiXe;
    private String iconLoaiXe;
    private String maPhanKhu;     // 2B, 4B, XD
    private String tenPhanKhu;    // Khu xe 2 bánh, Khu xe 4 bánh, Khu xe đạp
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime thoiGianVao;
    private String thoiGianVaoFormatted;
    private long tongPhut;
    private String thoiLuongText;
    private boolean qua24h;
    private String anhVaoUrl;
    private boolean laVeThang;
    private String trangThai;
    private String ghiChu;

    public PhuongTienDangGuiDto() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Long getLoaiXeId() {
        return loaiXeId;
    }

    public void setLoaiXeId(Long loaiXeId) {
        this.loaiXeId = loaiXeId;
    }

    public String getTenLoaiXe() {
        return tenLoaiXe;
    }

    public void setTenLoaiXe(String tenLoaiXe) {
        this.tenLoaiXe = tenLoaiXe;
    }

    public String getIconLoaiXe() {
        return iconLoaiXe;
    }

    public void setIconLoaiXe(String iconLoaiXe) {
        this.iconLoaiXe = iconLoaiXe;
    }

    public String getMaPhanKhu() {
        return maPhanKhu;
    }

    public void setMaPhanKhu(String maPhanKhu) {
        this.maPhanKhu = maPhanKhu;
    }

    public String getTenPhanKhu() {
        return tenPhanKhu;
    }

    public void setTenPhanKhu(String tenPhanKhu) {
        this.tenPhanKhu = tenPhanKhu;
    }

    public LocalDateTime getThoiGianVao() {
        return thoiGianVao;
    }

    public void setThoiGianVao(LocalDateTime thoiGianVao) {
        this.thoiGianVao = thoiGianVao;
    }

    public String getThoiGianVaoFormatted() {
        return thoiGianVaoFormatted;
    }

    public void setThoiGianVaoFormatted(String thoiGianVaoFormatted) {
        this.thoiGianVaoFormatted = thoiGianVaoFormatted;
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

    public boolean isQua24h() {
        return qua24h;
    }

    public void setQua24h(boolean qua24h) {
        this.qua24h = qua24h;
    }

    public String getAnhVaoUrl() {
        return anhVaoUrl;
    }

    public void setAnhVaoUrl(String anhVaoUrl) {
        this.anhVaoUrl = anhVaoUrl;
    }

    public boolean isLaVeThang() {
        return laVeThang;
    }

    public void setLaVeThang(boolean laVeThang) {
        this.laVeThang = laVeThang;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}


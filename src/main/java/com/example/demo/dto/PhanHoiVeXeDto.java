package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class PhanHoiVeXeDto {

    private Integer id;
    private String maQR;
    private String qrCodeBase64;
    private String bienSoXe;
    private Long loaiXeId;
    private String tenLoaiXe;
    private String maViTri;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime thoiGianVao;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime thoiGianRa;

    private Double chiPhi;
    private String hinhThucThanhToan;
    private String trangThai;
    private boolean laVeThang;
    private String tenKhachHang;
    private String ghiChu;
    private String anhVaoUrl;
    private String anhRaUrl;

    public PhanHoiVeXeDto() {
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

    public String getTicketCode() {
        return maQR;
    }

    public void setTicketCode(String ticketCode) {
        this.maQR = ticketCode;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
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

    public Double getChiPhi() {
        return chiPhi;
    }

    public void setChiPhi(Double chiPhi) {
        this.chiPhi = chiPhi;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
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

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getAnhVaoUrl() {
        return anhVaoUrl;
    }

    public void setAnhVaoUrl(String anhVaoUrl) {
        this.anhVaoUrl = anhVaoUrl;
    }

    public String getAnhRaUrl() {
        return anhRaUrl;
    }

    public void setAnhRaUrl(String anhRaUrl) {
        this.anhRaUrl = anhRaUrl;
    }
}

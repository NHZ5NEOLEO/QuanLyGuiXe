package com.example.demo.dto;

public class YeuCauXuatBai {
    private Integer veXeId;
    private String ticketCode;
    private String hinhThucThanhToan = "TIEN_MAT";
    private String anhRaData;
    private String ghiChu;

    public YeuCauXuatBai() {
    }

    public YeuCauXuatBai(Integer veXeId, String ticketCode, String hinhThucThanhToan, String anhRaData, String ghiChu) {
        this.veXeId = veXeId;
        this.ticketCode = ticketCode;
        this.hinhThucThanhToan = hinhThucThanhToan != null ? hinhThucThanhToan : "TIEN_MAT";
        this.anhRaData = anhRaData;
        this.ghiChu = ghiChu;
    }

    public Integer getVeXeId() {
        return veXeId;
    }

    public void setVeXeId(Integer veXeId) {
        this.veXeId = veXeId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public String getMaVe() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public void setMaVe(String maVe) {
        this.ticketCode = maVe;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public String getAnhRaData() {
        return anhRaData;
    }

    public void setAnhRaData(String anhRaData) {
        this.anhRaData = anhRaData;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

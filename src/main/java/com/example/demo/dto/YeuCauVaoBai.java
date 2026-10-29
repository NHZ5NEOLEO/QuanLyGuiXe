package com.example.demo.dto;

public class YeuCauVaoBai {
    private String bienSoXe;
    private String maQR;
    private Long loaiXeId;
    private String maViTri;
    private String anhVaoData;
    private String ghiChu;

    public YeuCauVaoBai() {
    }

    public YeuCauVaoBai(String bienSoXe, String maQR, Long loaiXeId, String maViTri, String anhVaoData, String ghiChu) {
        this.bienSoXe = bienSoXe;
        this.maQR = maQR;
        this.loaiXeId = loaiXeId;
        this.maViTri = maViTri;
        this.anhVaoData = anhVaoData;
        this.ghiChu = ghiChu;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public String getMaQR() {
        return maQR;
    }

    public void setMaQR(String maQR) {
        this.maQR = maQR;
    }

    public Long getLoaiXeId() {
        return loaiXeId;
    }

    public void setLoaiXeId(Long loaiXeId) {
        this.loaiXeId = loaiXeId;
    }

    public String getMaViTri() {
        return maViTri;
    }

    public void setMaViTri(String maViTri) {
        this.maViTri = maViTri;
    }

    public String getAnhVaoData() {
        return anhVaoData;
    }

    public void setAnhVaoData(String anhVaoData) {
        this.anhVaoData = anhVaoData;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

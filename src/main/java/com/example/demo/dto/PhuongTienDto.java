package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class PhuongTienDto {

    private Long id;
    private String maPhuongTien;

    @NotBlank(message = "Biển số xe không được để trống")
    private String bienSoXe;

    @NotNull(message = "Loại xe không được để trống")
    private Long loaiXeId;

    private String tenLoaiXe;
    private String chuXe;
    private String soDienThoai;
    private String moTa;
    private LocalDateTime ngayTao;

    public PhuongTienDto() {
    }

    public PhuongTienDto(Long id, String maPhuongTien, String bienSoXe, Long loaiXeId, String tenLoaiXe, String chuXe, String soDienThoai, String moTa, LocalDateTime ngayTao) {
        this.id = id;
        this.maPhuongTien = maPhuongTien;
        this.bienSoXe = bienSoXe;
        this.loaiXeId = loaiXeId;
        this.tenLoaiXe = tenLoaiXe;
        this.chuXe = chuXe;
        this.soDienThoai = soDienThoai;
        this.moTa = moTa;
        this.ngayTao = ngayTao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaPhuongTien() {
        return maPhuongTien;
    }

    public void setMaPhuongTien(String maPhuongTien) {
        this.maPhuongTien = maPhuongTien;
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

    public String getChuXe() {
        return chuXe;
    }

    public void setChuXe(String chuXe) {
        this.chuXe = chuXe;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}

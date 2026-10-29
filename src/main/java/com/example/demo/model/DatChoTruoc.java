package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DatChoTruoc")
public class DatChoTruoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String maDatCho; // BK-261010-001

    @Column(nullable = false, length = 100)
    private String hoTen;

    @Column(nullable = false, length = 20)
    private String soDienThoai;

    @Column(nullable = false, length = 20)
    private String bienSoXe;

    @Column(name = "ma_khu_vuc", length = 50)
    private String maKhuVuc = "Khu vực chung";

    @Column(nullable = false)
    private LocalDateTime thoiGianDat;

    @Column(nullable = false)
    private LocalDateTime thoiGianHetHan; // Sau 60 phút không đến sẽ tự hủy

    private Double tienCoc = 20000.0;

    @Column(length = 20)
    private String trangThai = "CHO_DEN"; // CHO_DEN, DA_VAO, HUY, HET_HAN

    public DatChoTruoc() {
    }

    public DatChoTruoc(String maDatCho, String hoTen, String soDienThoai, String bienSoXe, LocalDateTime thoiGianDat, LocalDateTime thoiGianHetHan, Double tienCoc, String trangThai) {
        this.maDatCho = maDatCho;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.bienSoXe = bienSoXe;
        this.thoiGianDat = thoiGianDat;
        this.thoiGianHetHan = thoiGianHetHan;
        this.tienCoc = tienCoc;
        this.trangThai = trangThai;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaDatCho() {
        return maDatCho;
    }

    public void setMaDatCho(String maDatCho) {
        this.maDatCho = maDatCho;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public String getMaKhuVuc() {
        return maKhuVuc;
    }

    public void setMaKhuVuc(String maKhuVuc) {
        this.maKhuVuc = maKhuVuc;
    }

    public String getMaViTri() {
        return maKhuVuc != null ? maKhuVuc : "Khu vực chung";
    }

    public LocalDateTime getThoiGianDat() {
        return thoiGianDat;
    }

    public void setThoiGianDat(LocalDateTime thoiGianDat) {
        this.thoiGianDat = thoiGianDat;
    }

    public LocalDateTime getThoiGianHetHan() {
        return thoiGianHetHan;
    }

    public void setThoiGianHetHan(LocalDateTime thoiGianHetHan) {
        this.thoiGianHetHan = thoiGianHetHan;
    }

    public Double getTienCoc() {
        return tienCoc;
    }

    public void setTienCoc(Double tienCoc) {
        this.tienCoc = tienCoc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}

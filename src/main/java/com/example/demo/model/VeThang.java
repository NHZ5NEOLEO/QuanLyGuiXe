package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "VeThang")
public class VeThang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String maThe; // Mã thẻ từ RFID hoặc QR code vé tháng

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "khach_hang_id", nullable = false)
    private KhachHang khachHang;

    @Column(nullable = false, length = 20)
    private String bienSoXe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loai_xe_id", nullable = false)
    private LoaiXe loaiXe;

    @Column(nullable = false)
    private LocalDate ngayBatDau;

    @Column(nullable = false)
    private LocalDate ngayHetHan;

    private Double giaTien;

    @Column(length = 20)
    private String trangThai = "HOAT_DONG"; // HOAT_DONG, HET_HAN, KHOA

    public VeThang() {
    }

    public VeThang(String maThe, KhachHang khachHang, String bienSoXe, LoaiXe loaiXe, LocalDate ngayBatDau, LocalDate ngayHetHan, Double giaTien, String trangThai) {
        this.maThe = maThe;
        this.khachHang = khachHang;
        this.bienSoXe = bienSoXe;
        this.loaiXe = loaiXe;
        this.ngayBatDau = ngayBatDau;
        this.ngayHetHan = ngayHetHan;
        this.giaTien = giaTien;
        this.trangThai = trangThai;
    }

    public boolean isConHan() {
        if (!"HOAT_DONG".equalsIgnoreCase(this.trangThai)) {
            return false;
        }
        LocalDate now = LocalDate.now();
        return (ngayBatDau != null && !now.isBefore(ngayBatDau)) &&
               (ngayHetHan != null && !now.isAfter(ngayHetHan));
    }

    public long getSoNgayConLai() {
        if (ngayHetHan == null) return 0;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), ngayHetHan);
        return Math.max(days, 0);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaThe() {
        return maThe;
    }

    public void setMaThe(String maThe) {
        this.maThe = maThe;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public LoaiXe getLoaiXe() {
        return loaiXe;
    }

    public void setLoaiXe(LoaiXe loaiXe) {
        this.loaiXe = loaiXe;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(LocalDate ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }

    public Double getGiaTien() {
        return giaTien;
    }

    public void setGiaTien(Double giaTien) {
        this.giaTien = giaTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}

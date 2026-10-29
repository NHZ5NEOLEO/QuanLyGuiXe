package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhuongTien")
public class PhuongTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phuong_tien", length = 50, unique = true)
    private String maPhuongTien;

    @Column(name = "bien_so_xe", nullable = false, unique = true, length = 20)
    private String bienSoXe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loai_xe_id", nullable = false)
    private LoaiXe loaiXe;

    @Column(name = "chu_xe", length = 100, columnDefinition = "NVARCHAR(100)")
    private String chuXe;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "mo_ta", length = 255, columnDefinition = "NVARCHAR(255)")
    private String moTa;

    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao = LocalDateTime.now();

    public PhuongTien() {
    }

    public PhuongTien(String bienSoXe, LoaiXe loaiXe, String chuXe, String soDienThoai, String moTa) {
        this.bienSoXe = bienSoXe != null ? bienSoXe.trim().toUpperCase() : null;
        this.loaiXe = loaiXe;
        this.chuXe = chuXe;
        this.soDienThoai = soDienThoai;
        this.moTa = moTa;
        this.maPhuongTien = "PT-" + (bienSoXe != null ? bienSoXe.replaceAll("[^a-zA-Z0-9]", "") : System.currentTimeMillis());
        this.ngayTao = LocalDateTime.now();
    }

    public PhuongTien(String maPhuongTien, String bienSoXe, LoaiXe loaiXe, String chuXe, String soDienThoai, String moTa) {
        this.maPhuongTien = maPhuongTien;
        this.bienSoXe = bienSoXe != null ? bienSoXe.trim().toUpperCase() : null;
        this.loaiXe = loaiXe;
        this.chuXe = chuXe;
        this.soDienThoai = soDienThoai;
        this.moTa = moTa;
        this.ngayTao = LocalDateTime.now();
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
        this.bienSoXe = bienSoXe != null ? bienSoXe.trim().toUpperCase() : null;
    }

    public LoaiXe getLoaiXe() {
        return loaiXe;
    }

    public void setLoaiXe(LoaiXe loaiXe) {
        this.loaiXe = loaiXe;
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

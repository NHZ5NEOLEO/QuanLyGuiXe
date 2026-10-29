package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "LoaiXe")
public class LoaiXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true, columnDefinition = "NVARCHAR(100)")
    private String tenLoai; // Ví dụ: Xe máy, Ô tô 4-7 chỗ, Xe đạp/điện, Xe tải nhỏ

    @Column(nullable = false)
    private Double giaBanNgay; // Giá vé lượt ban ngày (VND)

    @Column(nullable = false)
    private Double giaBanDem; // Giá vé lượt ban đêm (VND)

    @Column(nullable = false)
    private Double giaThang; // Giá vé tháng (VND)

    @Column(length = 50)
    private String iconClass; // FontAwesome icon class (ví dụ: fa-motorcycle, fa-car, fa-bicycle)

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String moTa;

    public LoaiXe() {
    }

    public LoaiXe(String tenLoai, Double giaBanNgay, Double giaBanDem, Double giaThang, String iconClass, String moTa) {
        this.tenLoai = tenLoai;
        this.giaBanNgay = giaBanNgay;
        this.giaBanDem = giaBanDem;
        this.giaThang = giaThang;
        this.iconClass = iconClass;
        this.moTa = moTa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public Double getGiaBanNgay() {
        return giaBanNgay;
    }

    public Double getGiaNgay() {
        return giaBanNgay;
    }

    public void setGiaBanNgay(Double giaBanNgay) {
        this.giaBanNgay = giaBanNgay;
    }

    public Double getGiaBanDem() {
        return giaBanDem;
    }

    public Double getGiaDem() {
        return giaBanDem;
    }

    public void setGiaBanDem(Double giaBanDem) {
        this.giaBanDem = giaBanDem;
    }

    public Double getGiaThang() {
        return giaThang;
    }

    public void setGiaThang(Double giaThang) {
        this.giaThang = giaThang;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}

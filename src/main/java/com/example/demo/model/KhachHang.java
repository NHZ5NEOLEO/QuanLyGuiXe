package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "KhachHang")
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String hoTen;

    @Column(nullable = false, length = 15, unique = true)
    private String soDienThoai;

    @Column(length = 20)
    private String cccd;

    @Column(length = 100)
    private String email;

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String diaChi;

    public KhachHang() {
    }

    public KhachHang(String hoTen, String soDienThoai, String cccd, String email, String diaChi) {
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.cccd = cccd;
        this.email = email;
        this.diaChi = diaChi;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}

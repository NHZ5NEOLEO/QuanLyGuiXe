package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "NguoiDung")
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String tenDangNhap;

    @Column(nullable = false, length = 100)
    private String matKhau;

    @Column(nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String hoTen;

    @Column(nullable = false, length = 20)
    private String vaiTro; // ADMIN, NHAN_VIEN

    @Column(length = 20)
    private String soDienThoai;

    private Boolean trangThai = true;

    public NguoiDung() {
    }

    public NguoiDung(String tenDangNhap, String matKhau, String hoTen, String vaiTro, String soDienThoai) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
        this.soDienThoai = soDienThoai;
        this.trangThai = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public Boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(Boolean trangThai) {
        this.trangThai = trangThai;
    }
}

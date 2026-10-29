package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "KhuVuc")
public class KhuVuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    private String maKhu; // A, B, C

    @Column(nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String tenKhu; // Khu A - Đỗ ô tô, Khu B - Đỗ xe máy

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loai_xe_id")
    private LoaiXe loaiXe;

    @Column(nullable = false)
    private Integer sucChua = 20;

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String viTri; // Tầng 1, Hầm B1, Ngoài trời

    public KhuVuc() {
    }

    public KhuVuc(String maKhu, String tenKhu, LoaiXe loaiXe, Integer sucChua, String viTri) {
        this.maKhu = maKhu;
        this.tenKhu = tenKhu;
        this.loaiXe = loaiXe;
        this.sucChua = sucChua;
        this.viTri = viTri;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaKhu() {
        return maKhu;
    }

    public void setMaKhu(String maKhu) {
        this.maKhu = maKhu;
    }

    public String getTenKhu() {
        return tenKhu;
    }

    public void setTenKhu(String tenKhu) {
        this.tenKhu = tenKhu;
    }

    public LoaiXe getLoaiXe() {
        return loaiXe;
    }

    public void setLoaiXe(LoaiXe loaiXe) {
        this.loaiXe = loaiXe;
    }

    public Integer getSucChua() {
        return sucChua;
    }

    public void setSucChua(Integer sucChua) {
        this.sucChua = sucChua;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    @Transient
    public String getLoaiXeKhuVuc() {
        return loaiXe != null ? loaiXe.getTenLoai() : "Phương tiện";
    }
}

package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DanhSachDen")
public class DanhSachDen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String bienSoXe;

    @Column(nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String lyDo; // Ví dụ: Xe báo mất cắp, Xe gây tai nạn bỏ chạy, Khách vi phạm nội quy

    @Column(length = 50, columnDefinition = "NVARCHAR(50)")
    private String mucDo = "NGUY_HIEM"; // CANH_BAO, NGUY_HIEM

    private LocalDateTime ngayThem = LocalDateTime.now();

    @Column(length = 100, columnDefinition = "NVARCHAR(100)")
    private String nguoiThem;

    public DanhSachDen() {
    }

    public DanhSachDen(String bienSoXe, String lyDo, String mucDo, String nguoiThem) {
        this.bienSoXe = bienSoXe;
        this.lyDo = lyDo;
        this.mucDo = mucDo;
        this.nguoiThem = nguoiThem;
        this.ngayThem = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public String getLyDo() {
        return lyDo;
    }

    public void setLyDo(String lyDo) {
        this.lyDo = lyDo;
    }

    public String getMucDo() {
        return mucDo;
    }

    public void setMucDo(String mucDo) {
        this.mucDo = mucDo;
    }

    public LocalDateTime getNgayThem() {
        return ngayThem;
    }

    public void setNgayThem(LocalDateTime ngayThem) {
        this.ngayThem = ngayThem;
    }

    public String getNguoiThem() {
        return nguoiThem;
    }

    public void setNguoiThem(String nguoiThem) {
        this.nguoiThem = nguoiThem;
    }
}

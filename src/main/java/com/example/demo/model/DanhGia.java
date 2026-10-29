package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DanhGia")
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String hoTen;

    @Column(length = 20)
    private String soDienThoai;

    @Column(nullable = false)
    private Integer soSao; // 1 đến 5 sao

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String noiDung;

    @Column(length = 50)
    private String loaiDichVu = "CHUNG"; // AN_NINH, VE_SINH, GIA_CA, TOC_DO

    private LocalDateTime thoiGian = LocalDateTime.now();

    public DanhGia() {
    }

    public DanhGia(String hoTen, String soDienThoai, Integer soSao, String noiDung, String loaiDichVu) {
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.soSao = soSao;
        this.noiDung = noiDung;
        this.loaiDichVu = loaiDichVu;
        this.thoiGian = LocalDateTime.now();
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

    public Integer getSoSao() {
        return soSao;
    }

    public void setSoSao(Integer soSao) {
        this.soSao = soSao;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getLoaiDichVu() {
        return loaiDichVu;
    }

    public void setLoaiDichVu(String loaiDichVu) {
        this.loaiDichVu = loaiDichVu;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
}

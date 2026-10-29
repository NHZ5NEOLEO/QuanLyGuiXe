package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "CaTruc")
public class CaTruc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String maCa; // CA-SANG, CA-CHIEU, CA-DEM

    @Column(nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String tenCa; // Ca Sáng (06:00 - 14:00), Ca Chiều (14:00 - 22:00), Ca Đêm (22:00 - 06:00)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nguoi_dung_id")
    private NguoiDung nguoiTruc;

    @Column(nullable = false)
    private LocalDateTime thoiGianBatDau = LocalDateTime.now();

    private LocalDateTime thoiGianKetThuc;

    private Integer tongLuotVao = 0;

    private Integer tongLuotRa = 0;

    private Double tienMatThuDuoc = 0.0;

    private Double chuyenKhoanThuDuoc = 0.0;

    private Double tongDoanhThu = 0.0;

    @Column(length = 50)
    private String trangThai = "DANG_TRUC"; // DANG_TRUC, DA_BAN_GIAO

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String ghiChuBanGiao;

    public CaTruc() {
    }

    public CaTruc(String maCa, String tenCa, NguoiDung nguoiTruc, LocalDateTime thoiGianBatDau) {
        this.maCa = maCa;
        this.tenCa = tenCa;
        this.nguoiTruc = nguoiTruc;
        this.thoiGianBatDau = thoiGianBatDau;
        this.trangThai = "DANG_TRUC";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaCa() {
        return maCa;
    }

    public void setMaCa(String maCa) {
        this.maCa = maCa;
    }

    public String getTenCa() {
        return tenCa;
    }

    public void setTenCa(String tenCa) {
        this.tenCa = tenCa;
    }

    public NguoiDung getNguoiTruc() {
        return nguoiTruc;
    }

    public void setNguoiTruc(NguoiDung nguoiTruc) {
        this.nguoiTruc = nguoiTruc;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public Integer getTongLuotVao() {
        return tongLuotVao;
    }

    public void setTongLuotVao(Integer tongLuotVao) {
        this.tongLuotVao = tongLuotVao;
    }

    public Integer getTongLuotRa() {
        return tongLuotRa;
    }

    public void setTongLuotRa(Integer tongLuotRa) {
        this.tongLuotRa = tongLuotRa;
    }

    public Double getTienMatThuDuoc() {
        return tienMatThuDuoc;
    }

    public void setTienMatThuDuoc(Double tienMatThuDuoc) {
        this.tienMatThuDuoc = tienMatThuDuoc;
    }

    public Double getChuyenKhoanThuDuoc() {
        return chuyenKhoanThuDuoc;
    }

    public void setChuyenKhoanThuDuoc(Double chuyenKhoanThuDuoc) {
        this.chuyenKhoanThuDuoc = chuyenKhoanThuDuoc;
    }

    public Double getTongDoanhThu() {
        return tongDoanhThu;
    }

    public void setTongDoanhThu(Double tongDoanhThu) {
        this.tongDoanhThu = tongDoanhThu;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getGhiChuBanGiao() {
        return ghiChuBanGiao;
    }

    public void setGhiChuBanGiao(String ghiChuBanGiao) {
        this.ghiChuBanGiao = ghiChuBanGiao;
    }
}

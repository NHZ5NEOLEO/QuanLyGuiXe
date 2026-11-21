package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_thay_doi_gia")
public class LichSuThayDoiGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loai_xe_id")
    private Long loaiXeId;

    @Column(name = "ten_loai", nullable = false, length = 100)
    private String tenLoai;

    @Column(name = "gia_ban_ngay_cu")
    private Double giaBanNgayCu;

    @Column(name = "gia_ban_ngay_moi", nullable = false)
    private Double giaBanNgayMoi;

    @Column(name = "gia_ban_dem_cu")
    private Double giaBanDemCu;

    @Column(name = "gia_ban_dem_moi", nullable = false)
    private Double giaBanDemMoi;

    @Column(name = "gia_thang_cu")
    private Double giaThangCu;

    @Column(name = "gia_thang_moi", nullable = false)
    private Double giaThangMoi;

    @Column(name = "nguoi_thuc_hien", nullable = false, length = 100)
    private String nguoiThucHien;

    @Column(name = "thoi_gian_thay_doi", nullable = false)
    private LocalDateTime thoiGianThayDoi;

    @Column(name = "hanh_dong", nullable = false, length = 50)
    private String hanhDong;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    public LichSuThayDoiGia() {
        this.thoiGianThayDoi = LocalDateTime.now();
        this.hanhDong = "CẬP NHẬT";
    }

    public LichSuThayDoiGia(Long loaiXeId, String tenLoai, Double giaBanNgayCu, Double giaBanNgayMoi,
                            Double giaBanDemCu, Double giaBanDemMoi, Double giaThangCu, Double giaThangMoi,
                            String nguoiThucHien, String hanhDong, String ghiChu) {
        this.loaiXeId = loaiXeId;
        this.tenLoai = tenLoai;
        this.giaBanNgayCu = giaBanNgayCu;
        this.giaBanNgayMoi = giaBanNgayMoi;
        this.giaBanDemCu = giaBanDemCu;
        this.giaBanDemMoi = giaBanDemMoi;
        this.giaThangCu = giaThangCu;
        this.giaThangMoi = giaThangMoi;
        this.nguoiThucHien = nguoiThucHien;
        this.thoiGianThayDoi = LocalDateTime.now();
        this.hanhDong = hanhDong != null ? hanhDong : "CẬP NHẬT";
        this.ghiChu = ghiChu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoaiXeId() {
        return loaiXeId;
    }

    public void setLoaiXeId(Long loaiXeId) {
        this.loaiXeId = loaiXeId;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public Double getGiaBanNgayCu() {
        return giaBanNgayCu;
    }

    public void setGiaBanNgayCu(Double giaBanNgayCu) {
        this.giaBanNgayCu = giaBanNgayCu;
    }

    public Double getGiaBanNgayMoi() {
        return giaBanNgayMoi;
    }

    public void setGiaBanNgayMoi(Double giaBanNgayMoi) {
        this.giaBanNgayMoi = giaBanNgayMoi;
    }

    public Double getGiaBanDemCu() {
        return giaBanDemCu;
    }

    public void setGiaBanDemCu(Double giaBanDemCu) {
        this.giaBanDemCu = giaBanDemCu;
    }

    public Double getGiaBanDemMoi() {
        return giaBanDemMoi;
    }

    public void setGiaBanDemMoi(Double giaBanDemMoi) {
        this.giaBanDemMoi = giaBanDemMoi;
    }

    public Double getGiaThangCu() {
        return giaThangCu;
    }

    public void setGiaThangCu(Double giaThangCu) {
        this.giaThangCu = giaThangCu;
    }

    public Double getGiaThangMoi() {
        return giaThangMoi;
    }

    public void setGiaThangMoi(Double giaThangMoi) {
        this.giaThangMoi = giaThangMoi;
    }

    public String getNguoiThucHien() {
        return nguoiThucHien;
    }

    public void setNguoiThucHien(String nguoiThucHien) {
        this.nguoiThucHien = nguoiThucHien;
    }

    public LocalDateTime getThoiGianThayDoi() {
        return thoiGianThayDoi;
    }

    public void setThoiGianThayDoi(LocalDateTime thoiGianThayDoi) {
        this.thoiGianThayDoi = thoiGianThayDoi;
    }

    public String getHanhDong() {
        return hanhDong;
    }

    public void setHanhDong(String hanhDong) {
        this.hanhDong = hanhDong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "VeXe")
public class VeXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maqr", length = 100)
    private String maQR; // Mã QR duy nhất của vé / Mã vé

    @Column(name = "bien_so_xe", nullable = false, length = 20)
    private String bienSoXe; // Biển số xe

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loai_xe_id")
    private LoaiXe loaiXe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ve_thang_id")
    private VeThang veThang; // Null nếu là vé lượt

    @Column(name = "thoi_gian_vao", nullable = false)
    private LocalDateTime thoiGianVao = LocalDateTime.now();

    @Column(name = "thoi_gian_ra")
    private LocalDateTime thoiGianRa;

    @Column(name = "chi_phi")
    private Double chiPhi;

    @Column(name = "hinh_thuc_thanh_toan", length = 50)
    private String hinhThucThanhToan = "TIEN_MAT"; // TIEN_MAT, CHUYEN_KHOAN, VE_THANG

    @Column(name = "trang_thai", length = 50, columnDefinition = "NVARCHAR(50)")
    private String trangThai = "Đang gửi"; // "Đang gửi", "Đã ra"

    @Column(name = "anh_vao_url", columnDefinition = "NVARCHAR(MAX)")
    private String anhVaoUrl;

    @Column(name = "anh_ra_url", columnDefinition = "NVARCHAR(MAX)")
    private String anhRaUrl;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;

    public String getTicketCode() {
        return maQR;
    }

    public String getMaVe() {
        return maQR;
    }

    public VeXe() {
    }

    public VeXe(String maQR, String bienSoXe, LoaiXe loaiXe, LocalDateTime thoiGianVao, String trangThai) {
        this.maQR = maQR;
        this.bienSoXe = bienSoXe;
        this.loaiXe = loaiXe;
        this.thoiGianVao = thoiGianVao;
        this.trangThai = trangThai;
    }

    public VeXe(String maQR, String bienSoXe, LoaiXe loaiXe, ViTriDo viTriDo, LocalDateTime thoiGianVao, String trangThai) {
        this.maQR = maQR;
        this.bienSoXe = bienSoXe;
        this.loaiXe = loaiXe;
        this.thoiGianVao = thoiGianVao;
        this.trangThai = trangThai;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMaQR() {
        return maQR;
    }

    public void setMaQR(String maQR) {
        this.maQR = maQR;
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

    public String getMaViTri() {
        return "Khu vực chung";
    }

    public VeThang getVeThang() {
        return veThang;
    }

    public void setVeThang(VeThang veThang) {
        this.veThang = veThang;
    }

    public LocalDateTime getThoiGianVao() {
        return thoiGianVao;
    }

    public void setThoiGianVao(LocalDateTime thoiGianVao) {
        this.thoiGianVao = thoiGianVao;
    }

    public LocalDateTime getThoiGianRa() {
        return thoiGianRa;
    }

    public void setThoiGianRa(LocalDateTime thoiGianRa) {
        this.thoiGianRa = thoiGianRa;
    }

    public Double getChiPhi() {
        return chiPhi;
    }

    public void setChiPhi(Double chiPhi) {
        this.chiPhi = chiPhi;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getAnhVaoUrl() {
        return anhVaoUrl;
    }

    public void setAnhVaoUrl(String anhVaoUrl) {
        this.anhVaoUrl = anhVaoUrl;
    }

    public String getAnhRaUrl() {
        return anhRaUrl;
    }

    public void setAnhRaUrl(String anhRaUrl) {
        this.anhRaUrl = anhRaUrl;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

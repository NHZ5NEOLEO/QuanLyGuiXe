package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "VeXe", indexes = {
    @Index(name = "idx_vexe_bienso_trangthai", columnList = "bien_so_xe, trang_thai"),
    @Index(name = "idx_vexe_biensoclean_trangthai", columnList = "bien_so_clean, trang_thai"),
    @Index(name = "idx_vexe_maqr", columnList = "maqr")
})
public class VeXe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "maqr")
    private String maQR; // Mã QR duy nhất của vé / Mã vé

    @Column(name = "bien_so_xe", nullable = false, length = 20)
    private String bienSoXe; // Biển số xe hiển thị chuẩn (VD: 30A-123.45)

    @Column(name = "bien_so_clean", length = 20)
    private String bienSoClean; // Biển số đã chuẩn hóa chỉ gồm chữ và số để so sánh chính xác (VD: 30A12345)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "loai_xe_id")
    private LoaiXe loaiXe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ve_thang_id")
    private VeThang veThang; // Null nếu là vé lượt

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vi_tri_do_id")
    private ViTriDo viTriDo; // Vị trí ô đỗ cụ thể được cấp

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dat_cho_truoc_id")
    private DatChoTruoc datChoTruoc; // Liên kết đơn đặt chỗ trước và tiền cọc nếu có

    @Column(name = "thoi_gian_vao", nullable = false)
    private LocalDateTime thoiGianVao = LocalDateTime.now();

    @Column(name = "thoi_gian_ra")
    private LocalDateTime thoiGianRa;

    @Column(name = "chi_phi")
    private Double chiPhi; // Số tiền thực tế phải nộp sau khi trừ tiền cọc

    @Column(name = "so_tien_phai_thu")
    private Double soTienPhaiThu; // Tổng tiền tính theo biểu phí gốc

    @Column(name = "tien_coc_khau_tru")
    private Double tienCocKhauTru = 0.0; // Tiền cọc đặt chỗ được khấu trừ từ booking đã xác nhận

    @Column(name = "so_tien_thuc_thu")
    private Double soTienThucThu; // Số tiền thực tế đã thu được từ khách

    @Column(name = "hinh_thuc_thanh_toan", length = 50)
    private String hinhThucThanhToan = "TIEN_MAT"; // TIEN_MAT, CHUYEN_KHOAN, VE_THANG

    @Column(name = "trang_thai_thanh_toan", length = 50)
    private String trangThaiThanhToan = "CHUA_THANH_TOAN"; // CHUA_THANH_TOAN, CHO_THANH_TOAN, DA_THANH_TOAN, MIEN_PHI

    @Column(name = "ma_giao_dich", length = 100)
    private String maGiaoDich; // Mã giao dịch thanh toán ngân hàng / VietQR / biên lai

    @Column(name = "thoi_gian_thanh_toan")
    private LocalDateTime thoiGianThanhToan; // Thời điểm hoàn tất xác nhận thanh toán

    @Column(name = "nguoi_xac_nhan", length = 100)
    private String nguoiXacNhan; // Username nhân viên/admin xác nhận thu tiền

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
        this.viTriDo = viTriDo;
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

    public String getBienSoClean() {
        return bienSoClean;
    }

    public void setBienSoClean(String bienSoClean) {
        this.bienSoClean = bienSoClean;
    }

    public LoaiXe getLoaiXe() {
        return loaiXe;
    }

    public void setLoaiXe(LoaiXe loaiXe) {
        this.loaiXe = loaiXe;
    }

    public ViTriDo getViTriDo() {
        return viTriDo;
    }

    public void setViTriDo(ViTriDo viTriDo) {
        this.viTriDo = viTriDo;
    }

    public String getMaViTri() {
        if (viTriDo != null && viTriDo.getMaViTri() != null) {
            return viTriDo.getMaViTri();
        }
        return "Khu vực chung";
    }

    public DatChoTruoc getDatChoTruoc() {
        return datChoTruoc;
    }

    public void setDatChoTruoc(DatChoTruoc datChoTruoc) {
        this.datChoTruoc = datChoTruoc;
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

    public Double getSoTienPhaiThu() {
        return soTienPhaiThu;
    }

    public void setSoTienPhaiThu(Double soTienPhaiThu) {
        this.soTienPhaiThu = soTienPhaiThu;
    }

    public Double getTienCocKhauTru() {
        return tienCocKhauTru != null ? tienCocKhauTru : 0.0;
    }

    public void setTienCocKhauTru(Double tienCocKhauTru) {
        this.tienCocKhauTru = tienCocKhauTru;
    }

    public Double getSoTienThucThu() {
        return soTienThucThu;
    }

    public void setSoTienThucThu(Double soTienThucThu) {
        this.soTienThucThu = soTienThucThu;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public LocalDateTime getThoiGianThanhToan() {
        return thoiGianThanhToan;
    }

    public void setThoiGianThanhToan(LocalDateTime thoiGianThanhToan) {
        this.thoiGianThanhToan = thoiGianThanhToan;
    }

    public String getNguoiXacNhan() {
        return nguoiXacNhan;
    }

    public void setNguoiXacNhan(String nguoiXacNhan) {
        this.nguoiXacNhan = nguoiXacNhan;
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

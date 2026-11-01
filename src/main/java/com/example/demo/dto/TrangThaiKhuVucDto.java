package com.example.demo.dto;

import com.example.demo.model.VeXe;
import java.util.ArrayList;
import java.util.List;

public class TrangThaiKhuVucDto {
    private Long id;
    private String maKhu;
    private String tenKhu;
    private String viTri;
    private String tenLoaiXe;
    private String iconClass = "fa-square-parking";
    private String badgeColor = "primary";
    private long tongCho;
    private long choTrong;
    private long daDo;
    private double tyLeLapDay;
    private String mucCanhBao = "BINH_THUONG"; // BINH_THUONG (<80%), DONG_DUC (80-94%), SAP_DAY (95-99%), QUA_TAI (>=100%)
    private String thongDiepCanhBao = "Thông thoáng";
    private long soLuongDatTruoc = 0; // Đơn đặt chỗ trước đang chờ đến
    private long soLuongBaoLuu = 0;   // Chỗ bảo lưu cho xe đặt trước / vé tháng
    private List<VeXe> danhSachXe = new ArrayList<>();

    public TrangThaiKhuVucDto() {
    }

    public TrangThaiKhuVucDto(Long id, String maKhu, String tenKhu, String viTri, String tenLoaiXe, long tongCho, long choTrong) {
        this(id, maKhu, tenKhu, viTri, tenLoaiXe, tongCho, choTrong, 0);
    }

    public TrangThaiKhuVucDto(Long id, String maKhu, String tenKhu, String viTri, String tenLoaiXe, long tongCho, long choTrong, long soLuongDatTruoc) {
        this.id = id;
        this.maKhu = maKhu;
        this.tenKhu = tenKhu;
        this.viTri = viTri;
        this.tenLoaiXe = tenLoaiXe;
        this.tongCho = tongCho;
        this.choTrong = choTrong;
        this.daDo = Math.max(0, tongCho - choTrong);
        this.soLuongDatTruoc = soLuongDatTruoc;
        this.soLuongBaoLuu = Math.round(tongCho * 0.1); // Bảo lưu 10% cho khách đặt trước & vé tháng
        this.tyLeLapDay = tongCho > 0 ? Math.round(((double) this.daDo / tongCho) * 1000.0) / 10.0 : 0.0;

        // Phân loại mức cảnh báo
        if (this.tyLeLapDay >= 100.0) {
            this.mucCanhBao = "QUA_TAI";
            this.thongDiepCanhBao = "Quá tải (Dùng làn phụ)";
        } else if (this.tyLeLapDay >= 95.0) {
            this.mucCanhBao = "SAP_DAY";
            this.thongDiepCanhBao = "Sắp đầy";
        } else if (this.tyLeLapDay >= 80.0) {
            this.mucCanhBao = "DONG_DUC";
            this.thongDiepCanhBao = "Đông đúc";
        } else {
            this.mucCanhBao = "BINH_THUONG";
            this.thongDiepCanhBao = "Thông thoáng";
        }
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

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public String getTenLoaiXe() {
        return tenLoaiXe;
    }

    public void setTenLoaiXe(String tenLoaiXe) {
        this.tenLoaiXe = tenLoaiXe;
    }

    public String getLoaiXe() {
        return tenLoaiXe;
    }

    public void setLoaiXe(String loaiXe) {
        this.tenLoaiXe = loaiXe;
    }

    public long getTongCho() {
        return tongCho;
    }

    public void setTongCho(long tongCho) {
        this.tongCho = tongCho;
    }

    public long getChoTrong() {
        return choTrong;
    }

    public void setChoTrong(long choTrong) {
        this.choTrong = choTrong;
    }

    public long getDaDo() {
        return daDo;
    }

    public void setDaDo(long daDo) {
        this.daDo = daDo;
    }

    public double getTyLeLapDay() {
        return tyLeLapDay;
    }

    public void setTyLeLapDay(double tyLeLapDay) {
        this.tyLeLapDay = tyLeLapDay;
    }

    public String getMucCanhBao() {
        return mucCanhBao;
    }

    public void setMucCanhBao(String mucCanhBao) {
        this.mucCanhBao = mucCanhBao;
    }

    public String getThongDiepCanhBao() {
        return thongDiepCanhBao;
    }

    public void setThongDiepCanhBao(String thongDiepCanhBao) {
        this.thongDiepCanhBao = thongDiepCanhBao;
    }

    public long getSoLuongDatTruoc() {
        return soLuongDatTruoc;
    }

    public void setSoLuongDatTruoc(long soLuongDatTruoc) {
        this.soLuongDatTruoc = soLuongDatTruoc;
    }

    public long getSoLuongBaoLuu() {
        return soLuongBaoLuu;
    }

    public void setSoLuongBaoLuu(long soLuongBaoLuu) {
        this.soLuongBaoLuu = soLuongBaoLuu;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
    }

    public String getBadgeColor() {
        return badgeColor;
    }

    public void setBadgeColor(String badgeColor) {
        this.badgeColor = badgeColor;
    }

    public List<VeXe> getDanhSachXe() {
        return danhSachXe;
    }

    public void setDanhSachXe(List<VeXe> danhSachXe) {
        this.danhSachXe = danhSachXe;
    }
}

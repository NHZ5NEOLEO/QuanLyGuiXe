package com.example.demo.dto;

public class ThongKeTongQuanDto {
    private long xeDangTrongBai;
    private long choTrong;
    private long tongCho;
    private long luotVaoHomNay;
    private long luotRaHomNay;
    private double doanhThuHomNay;
    private double doanhThuThangNay;
    private long tongVeThangHoatDong;
    private double tyLeLapDay;
    private String selectedPeriod = "today";
    private String periodTitle = "Hôm nay";
    private long periodLuotVao;
    private long periodLuotRa;
    private double periodDoanhThu;

    public ThongKeTongQuanDto() {
    }

    public long getXeDangTrongBai() {
        return xeDangTrongBai;
    }

    public void setXeDangTrongBai(long xeDangTrongBai) {
        this.xeDangTrongBai = xeDangTrongBai;
    }

    public long getChoTrong() {
        return choTrong;
    }

    public void setChoTrong(long choTrong) {
        this.choTrong = choTrong;
    }

    public long getTongCho() {
        return tongCho;
    }

    public void setTongCho(long tongCho) {
        this.tongCho = tongCho;
    }

    public long getLuotVaoHomNay() {
        return luotVaoHomNay;
    }

    public void setLuotVaoHomNay(long luotVaoHomNay) {
        this.luotVaoHomNay = luotVaoHomNay;
    }

    public long getLuotRaHomNay() {
        return luotRaHomNay;
    }

    public void setLuotRaHomNay(long luotRaHomNay) {
        this.luotRaHomNay = luotRaHomNay;
    }

    public double getDoanhThuHomNay() {
        return doanhThuHomNay;
    }

    public void setDoanhThuHomNay(double doanhThuHomNay) {
        this.doanhThuHomNay = doanhThuHomNay;
    }

    public double getDoanhThuThangNay() {
        return doanhThuThangNay;
    }

    public void setDoanhThuThangNay(double doanhThuThangNay) {
        this.doanhThuThangNay = doanhThuThangNay;
    }

    public long getTongVeThangHoatDong() {
        return tongVeThangHoatDong;
    }

    public void setTongVeThangHoatDong(long tongVeThangHoatDong) {
        this.tongVeThangHoatDong = tongVeThangHoatDong;
    }

    public double getTyLeLapDay() {
        return tyLeLapDay;
    }

    public void setTyLeLapDay(double tyLeLapDay) {
        this.tyLeLapDay = tyLeLapDay;
    }

    public String getSelectedPeriod() {
        return selectedPeriod;
    }

    public void setSelectedPeriod(String selectedPeriod) {
        this.selectedPeriod = selectedPeriod;
    }

    public String getPeriodTitle() {
        return periodTitle;
    }

    public void setPeriodTitle(String periodTitle) {
        this.periodTitle = periodTitle;
    }

    public long getPeriodLuotVao() {
        return periodLuotVao;
    }

    public void setPeriodLuotVao(long periodLuotVao) {
        this.periodLuotVao = periodLuotVao;
    }

    public long getPeriodLuotRa() {
        return periodLuotRa;
    }

    public void setPeriodLuotRa(long periodLuotRa) {
        this.periodLuotRa = periodLuotRa;
    }

    public double getPeriodDoanhThu() {
        return periodDoanhThu;
    }

    public void setPeriodDoanhThu(double periodDoanhThu) {
        this.periodDoanhThu = periodDoanhThu;
    }
}

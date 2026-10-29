package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

public class LuuLuongXeDto {
    private List<String> labels = new ArrayList<>();
    private List<Long> inCounts = new ArrayList<>();
    private List<Long> outCounts = new ArrayList<>();
    private List<Double> revenues = new ArrayList<>();
    private long totalIn;
    private long totalOut;
    private double totalRevenue;

    public LuuLuongXeDto() {
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<String> getNhanThoiGian() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setNhanThoiGian(List<String> nhanThoiGian) {
        this.labels = nhanThoiGian;
    }

    public List<Long> getInCounts() {
        return inCounts;
    }

    public List<Long> getLuotVao() {
        return inCounts;
    }

    public void setInCounts(List<Long> inCounts) {
        this.inCounts = inCounts;
    }

    public void setLuotVao(List<Long> luotVao) {
        this.inCounts = luotVao;
    }

    public List<Long> getOutCounts() {
        return outCounts;
    }

    public List<Long> getLuotRa() {
        return outCounts;
    }

    public void setOutCounts(List<Long> outCounts) {
        this.outCounts = outCounts;
    }

    public void setLuotRa(List<Long> luotRa) {
        this.outCounts = luotRa;
    }

    public List<Double> getRevenues() {
        return revenues;
    }

    public List<Double> getDoanhThu() {
        return revenues;
    }

    public void setRevenues(List<Double> revenues) {
        this.revenues = revenues;
    }

    public void setDoanhThu(List<Double> doanhThu) {
        this.revenues = doanhThu;
    }

    public long getTotalIn() {
        return totalIn;
    }

    public long getTongVao() {
        return totalIn;
    }

    public void setTotalIn(long totalIn) {
        this.totalIn = totalIn;
    }

    public void setTongVao(long tongVao) {
        this.totalIn = tongVao;
    }

    public long getTotalOut() {
        return totalOut;
    }

    public long getTongRa() {
        return totalOut;
    }

    public void setTotalOut(long totalOut) {
        this.totalOut = totalOut;
    }

    public void setTongRa(long tongRa) {
        this.totalOut = tongRa;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public double getTongDoanhThu() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public void setTongDoanhThu(double tongDoanhThu) {
        this.totalRevenue = tongDoanhThu;
    }
}

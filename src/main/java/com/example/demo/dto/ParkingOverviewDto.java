package com.example.demo.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO đóng gói toàn bộ dữ liệu thống kê tổng thể của Bãi đỗ xe
 */
public class ParkingOverviewDto {

    private long totalCapacity = 0;
    private long occupied = 0;
    private long available = 0;
    private double occupancyRate = 0.0;
    private long overdueCount = 0;

    private List<TrangThaiKhuVucDto> zones = new ArrayList<>();
    private List<PhuongTienDangGuiDto> vehicles = new ArrayList<>();
    private List<PhuongTienDangGuiDto> overdueVehicles = new ArrayList<>();
    private Map<String, Object> deviceStatus = new HashMap<>();

    public ParkingOverviewDto() {
    }

    public long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public long getOccupied() {
        return occupied;
    }

    public void setOccupied(long occupied) {
        this.occupied = occupied;
    }

    public long getAvailable() {
        return available;
    }

    public void setAvailable(long available) {
        this.available = available;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public List<TrangThaiKhuVucDto> getZones() {
        return zones;
    }

    public void setZones(List<TrangThaiKhuVucDto> zones) {
        this.zones = zones;
    }

    public List<PhuongTienDangGuiDto> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<PhuongTienDangGuiDto> vehicles) {
        this.vehicles = vehicles;
    }

    public List<PhuongTienDangGuiDto> getOverdueVehicles() {
        return overdueVehicles;
    }

    public void setOverdueVehicles(List<PhuongTienDangGuiDto> overdueVehicles) {
        this.overdueVehicles = overdueVehicles;
    }

    public Map<String, Object> getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(Map<String, Object> deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
}


package com.example.demo.service;

import com.example.demo.dto.DashboardStatsDto;
import com.example.demo.dto.KhuVucStatusDto;
import com.example.demo.dto.TrafficActivityDto;
import com.example.demo.dto.TrangThaiKhuVucDto;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeThangRepository;
import com.example.demo.repository.VeXeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("baoCaoService")
public class ReportService implements BaoCaoService {

    private final VeXeRepository veXeRepository;
    private final VeThangRepository veThangRepository;
    private final LoaiXeRepository loaiXeRepository;
    private final KhuVucRepository khuVucRepository;

    public ReportService(VeXeRepository veXeRepository,
                         VeThangRepository veThangRepository,
                         LoaiXeRepository loaiXeRepository,
                         KhuVucRepository khuVucRepository) {
        this.veXeRepository = veXeRepository;
        this.veThangRepository = veThangRepository;
        this.loaiXeRepository = loaiXeRepository;
        this.khuVucRepository = khuVucRepository;
    }

    @Override
    public DashboardStatsDto layThongKeTongQuan() {
        return layThongKeTongQuan("today");
    }

    public DashboardStatsDto getDashboardStats() {
        return layThongKeTongQuan();
    }

    @Override
    public DashboardStatsDto layThongKeTongQuan(String chuKy) {
        if (chuKy == null || chuKy.trim().isEmpty()) {
            chuKy = "today";
        }
        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setSelectedPeriod(chuKy);

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime startOf7Days = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);

        long tongCho = khuVucRepository.findAll().stream().mapToLong(k -> k.getSucChua() != null ? k.getSucChua() : 0).sum();
        if (tongCho <= 0) {
            tongCho = 100;
        }
        long xeDangGui = veXeRepository.countByTrangThai("Đang gửi");
        if (xeDangGui <= 0) {
            xeDangGui = veXeRepository.countActiveVehicles();
        }
        long choTrong = Math.max(0, tongCho - xeDangGui);

        dto.setTongCho(tongCho);
        dto.setChoTrong(choTrong);
        dto.setXeDangTrongBai(xeDangGui);

        long luotVaoHomNay = veXeRepository.countByThoiGianVaoBetween(startOfToday, endOfToday);
        long luotRaHomNay = veXeRepository.countByThoiGianRaBetween(startOfToday, endOfToday);
        dto.setLuotVaoHomNay(luotVaoHomNay);
        dto.setLuotRaHomNay(luotRaHomNay);

        Double revToday = veXeRepository.calculateRevenueBetween(startOfToday, endOfToday);
        dto.setDoanhThuHomNay(revToday != null ? revToday : 0.0);

        Double revMonth = veXeRepository.calculateRevenueBetween(startOfMonth, endOfToday);
        dto.setDoanhThuThangNay(revMonth != null ? revMonth : 0.0);

        dto.setTongVeThangHoatDong(veThangRepository.countByTrangThai("HOAT_DONG"));

        double rate = tongCho > 0 ? ((double) (tongCho - choTrong) / tongCho) * 100 : 0;
        dto.setTyLeLapDay(Math.round(rate * 10.0) / 10.0);

        if ("week".equalsIgnoreCase(chuKy)) {
            dto.setPeriodTitle("7 ngày qua");
            dto.setPeriodLuotVao(veXeRepository.countByThoiGianVaoBetween(startOf7Days, endOfToday));
            dto.setPeriodLuotRa(veXeRepository.countByThoiGianRaBetween(startOf7Days, endOfToday));
            Double rev7 = veXeRepository.calculateRevenueBetween(startOf7Days, endOfToday);
            dto.setPeriodDoanhThu(rev7 != null ? rev7 : 0.0);
        } else if ("month".equalsIgnoreCase(chuKy)) {
            dto.setPeriodTitle("Tháng này");
            dto.setPeriodLuotVao(veXeRepository.countByThoiGianVaoBetween(startOfMonth, endOfToday));
            dto.setPeriodLuotRa(veXeRepository.countByThoiGianRaBetween(startOfMonth, endOfToday));
            Double revM = veXeRepository.calculateRevenueBetween(startOfMonth, endOfToday);
            dto.setPeriodDoanhThu(revM != null ? revM : 0.0);
        } else {
            dto.setPeriodTitle("Hôm nay");
            dto.setPeriodLuotVao(luotVaoHomNay);
            dto.setPeriodLuotRa(luotRaHomNay);
            dto.setPeriodDoanhThu(revToday != null ? revToday : 0.0);
        }

        return dto;
    }

    public DashboardStatsDto getDashboardStats(String period) {
        return layThongKeTongQuan(period);
    }

    @Override
    public List<VeXe> layHoatDongGanDay(int gioiHan) {
        return veXeRepository.findTop10ByOrderByThoiGianVaoDesc();
    }

    public List<VeXe> getRecentActivities(int limit) {
        return layHoatDongGanDay(limit);
    }

    @Override
    public Map<String, Double> layDoanhThu7NgayGanNhat() {
        Map<String, Double> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            Double rev = veXeRepository.calculateRevenueBetween(start, end);
            result.put(date.format(formatter), rev != null ? rev : 0.0);
        }

        return result;
    }

    public Map<String, Double> getRevenueLast7Days() {
        return layDoanhThu7NgayGanNhat();
    }

    @Override
    public Map<String, Long> layCoCauLoaiXe() {
        Map<String, Long> result = new LinkedHashMap<>();
        List<LoaiXe> loaiXes = loaiXeRepository.findAll();
        List<VeXe> activeList = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");

        for (LoaiXe lx : loaiXes) {
            long count = activeList.stream()
                    .filter(v -> v.getLoaiXe() != null && v.getLoaiXe().getId().equals(lx.getId()))
                    .count();
            result.put(lx.getTenLoai(), count);
        }

        return result;
    }

    public Map<String, Long> getVehicleTypeDistribution() {
        return layCoCauLoaiXe();
    }

    @Override
    public Map<String, Double> layCoCauThanhToan() {
        Map<String, Double> result = new LinkedHashMap<>();
        List<VeXe> exitedList = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đã ra");

        double tienMat = 0.0;
        double vietQr = 0.0;

        for (VeXe v : exitedList) {
            double c = v.getChiPhi() != null ? v.getChiPhi() : 0.0;
            if ("CHUYEN_KHOAN".equalsIgnoreCase(v.getHinhThucThanhToan())) {
                vietQr += c;
            } else if ("TIEN_MAT".equalsIgnoreCase(v.getHinhThucThanhToan())) {
                tienMat += c;
            }
        }

        result.put("Tiền mặt", tienMat);
        result.put("VietQR Chuyển khoản", vietQr);
        return result;
    }

    public Map<String, Double> getPaymentMethodDistribution() {
        return layCoCauThanhToan();
    }

    @Override
    public long demXeQuaHan() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<VeXe> active = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        return active.stream().filter(v -> v.getThoiGianVao().isBefore(threshold)).count();
    }

    public long getOverdueCount() {
        return demXeQuaHan();
    }

    @Override
    public Map<String, Long> layLuuLuongTheoGio() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) {
            result.put(String.format("%02d:00", h), 0L);
        }

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<VeXe> todayList = veXeRepository.findByThoiGianVaoBetween(startOfToday, endOfToday);

        for (VeXe v : todayList) {
            if (v.getThoiGianVao() != null) {
                int hour = v.getThoiGianVao().getHour();
                String key = String.format("%02d:00", hour);
                result.put(key, result.getOrDefault(key, 0L) + 1);
            }
        }

        return result;
    }

    public Map<String, Long> getHourlyTrafficToday() {
        return layLuuLuongTheoGio();
    }

    @Override
    public Map<String, Double> layDoanhThuTheoLoaiXe() {
        Map<String, Double> result = new LinkedHashMap<>();
        List<VeXe> exitedList = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đã ra");

        for (VeXe v : exitedList) {
            String loai = (v.getLoaiXe() != null) ? v.getLoaiXe().getTenLoai() : "Khác";
            double chiPhi = v.getChiPhi() != null ? v.getChiPhi() : 0.0;
            result.put(loai, result.getOrDefault(loai, 0.0) + chiPhi);
        }

        return result;
    }

    public Map<String, Double> getRevenueByVehicleType() {
        return layDoanhThuTheoLoaiXe();
    }

    @Override
    public TrafficActivityDto layHoatDongGiaoThong7Ngay() {
        TrafficActivityDto dto = new TrafficActivityDto();
        List<String> labels = new ArrayList<>();
        List<Long> inCounts = new ArrayList<>();
        List<Long> outCounts = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        long totalIn = 0;
        long totalOut = 0;
        double totalRev = 0.0;

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            long inCount = veXeRepository.countByThoiGianVaoBetween(start, end);
            long outCount = veXeRepository.countByThoiGianRaBetween(start, end);
            Double rev = veXeRepository.calculateRevenueBetween(start, end);
            double dayRev = rev != null ? rev : 0.0;

            labels.add(date.format(formatter));
            inCounts.add(inCount);
            outCounts.add(outCount);
            revenues.add(dayRev);

            totalIn += inCount;
            totalOut += outCount;
            totalRev += dayRev;
        }

        dto.setLabels(labels);
        dto.setInCounts(inCounts);
        dto.setOutCounts(outCounts);
        dto.setRevenues(revenues);
        dto.setTotalIn(totalIn);
        dto.setTotalOut(totalOut);
        dto.setTotalRevenue(totalRev);

        return dto;
    }

    public TrafficActivityDto getTrafficActivityLast7Days() {
        return layHoatDongGiaoThong7Ngay();
    }

    @Override
    public List<TrangThaiKhuVucDto> layTrangThaiKhuVuc() {
        List<KhuVuc> khuVucs = khuVucRepository.findAll();
        List<VeXe> activeVehicles = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        List<TrangThaiKhuVucDto> list = new ArrayList<>();

        for (KhuVuc kv : khuVucs) {
            long totalSlots = (kv.getSucChua() != null && kv.getSucChua() > 0) ? kv.getSucChua() : 100;

            String maKhu = kv.getMaKhu() != null ? kv.getMaKhu().toUpperCase() : "";
            String tenKhu = kv.getTenKhu() != null ? kv.getTenKhu().toLowerCase() : "";
            String loaiXeTen = (kv.getLoaiXe() != null) ? kv.getLoaiXe().getTenLoai() : "Đa năng";

            // Đếm số lượng xe thực tế đang gửi phù hợp với phân khu này
            long daDo = 0;
            for (VeXe v : activeVehicles) {
                String vType = (v.getLoaiXe() != null) ? v.getLoaiXe().getTenLoai().toLowerCase() : "";
                if ("A".equals(maKhu) || tenKhu.contains("ô tô") || tenKhu.contains("o to")) {
                    if (vType.contains("tô") || vType.contains("car") || vType.contains("4-7") || vType.contains("xe con")) {
                        daDo++;
                    }
                } else if ("B".equals(maKhu) || tenKhu.contains("máy") || tenKhu.contains("may")) {
                    if (vType.contains("máy") || vType.contains("moto") || vType.contains("tay ga") || vType.contains("số")) {
                        daDo++;
                    }
                } else {
                    if (!vType.contains("tô") && !vType.contains("máy")) {
                        daDo++;
                    }
                }
            }

            long bookingCount = 0;

            long emptySlots = Math.max(0, totalSlots - daDo);
            list.add(new KhuVucStatusDto(kv.getId(), kv.getMaKhu(), kv.getTenKhu(), kv.getViTri(), loaiXeTen, totalSlots, emptySlots, bookingCount));
        }

        return list;
    }

    public List<KhuVucStatusDto> getKhuVucStatuses() {
        List<TrangThaiKhuVucDto> statuses = layTrangThaiKhuVuc();
        List<KhuVucStatusDto> result = new ArrayList<>();
        for (TrangThaiKhuVucDto s : statuses) {
            if (s instanceof KhuVucStatusDto) {
                result.add((KhuVucStatusDto) s);
            } else {
                result.add(new KhuVucStatusDto(s.getId(), s.getMaKhu(), s.getTenKhu(), s.getViTri(), s.getLoaiXe(), s.getTongCho(), s.getChoTrong(), s.getSoLuongDatTruoc()));
            }
        }
        return result;
    }

    @Override
    public List<VeXe> layDanhSachXeQuaHan(int gioiHan) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<VeXe> active = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        return active.stream()
                .filter(v -> v.getThoiGianVao() != null && v.getThoiGianVao().isBefore(threshold))
                .limit(gioiHan)
                .toList();
    }

    public List<VeXe> getOverdueVehicles(int limit) {
        return layDanhSachXeQuaHan(limit);
    }
}

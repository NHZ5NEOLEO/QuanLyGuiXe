package com.example.demo.service;

import com.example.demo.dto.ParkingOverviewDto;
import com.example.demo.dto.PhuongTienDangGuiDto;
import com.example.demo.dto.TrangThaiKhuVucDto;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.model.ViTriDo;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeThangRepository;
import com.example.demo.repository.VeXeRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service("baiDoXeService")
public class ParkingLotService implements BaiDoXeService {

    private final KhuVucRepository khuVucRepository;
    private final LoaiXeRepository loaiXeRepository;
    private final VeXeRepository veXeRepository;
    private final VeThangRepository veThangRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    public ParkingLotService(KhuVucRepository khuVucRepository,
                             LoaiXeRepository loaiXeRepository,
                             VeXeRepository veXeRepository,
                             VeThangRepository veThangRepository) {
        this.khuVucRepository = khuVucRepository;
        this.loaiXeRepository = loaiXeRepository;
        this.veXeRepository = veXeRepository;
        this.veThangRepository = veThangRepository;
    }

    @Override
    public ParkingOverviewDto layTongQuanBaiDoXe() {
        ParkingOverviewDto overview = new ParkingOverviewDto();
        List<KhuVuc> khuVucs = khuVucRepository.findAll();
        List<VeXe> activeVehicles = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        LocalDateTime now = LocalDateTime.now();

        // 1. Phân loại và chuyển đổi toàn bộ xe đang gửi sang PhuongTienDangGuiDto
        List<PhuongTienDangGuiDto> vehicleDtos = new ArrayList<>();
        List<PhuongTienDangGuiDto> overdueDtos = new ArrayList<>();

        for (VeXe vx : activeVehicles) {
            PhuongTienDangGuiDto dto = new PhuongTienDangGuiDto();
            dto.setId(vx.getId());
            dto.setMaQR(vx.getMaQR());
            dto.setBienSoXe(vx.getBienSoXe());
            dto.setThoiGianVao(vx.getThoiGianVao());
            dto.setAnhVaoUrl(vx.getAnhVaoUrl());
            boolean isMonthly = vx.getVeThang() != null
                    || "VE_THANG".equalsIgnoreCase(vx.getHinhThucThanhToan())
                    || (vx.getGhiChu() != null && vx.getGhiChu().toLowerCase().contains("tháng"))
                    || (vx.getBienSoXe() != null && veThangRepository.findFirstByBienSoXe(vx.getBienSoXe()).isPresent());
            dto.setLaVeThang(isMonthly);
            dto.setTrangThai(vx.getTrangThai() != null ? vx.getTrangThai() : "Đang gửi");
            dto.setGhiChu(vx.getGhiChu());

            // Định dạng ngày giờ và tính thời lượng đã đỗ
            if (vx.getThoiGianVao() != null) {
                dto.setThoiGianVaoFormatted(vx.getThoiGianVao().format(DATE_TIME_FORMATTER));
                Duration dur = Duration.between(vx.getThoiGianVao(), now);
                long totalMins = Math.max(1, dur.toMinutes());
                dto.setTongPhut(totalMins);

                long days = totalMins / (24 * 60);
                long hours = (totalMins % (24 * 60)) / 60;
                long mins = totalMins % 60;

                StringBuilder sb = new StringBuilder();
                if (days > 0) sb.append(days).append(" ngày ");
                if (hours > 0 || days > 0) sb.append(hours).append(" giờ ");
                sb.append(mins).append(" phút");
                dto.setThoiLuongText(sb.toString().trim());

                // Điều kiện xe quá 24h: Thời gian hiện tại - Thời gian vào > 24 giờ.
                // Vé tháng là khách thuê bao gửi xe hợp lệ -> không tính cảnh báo quá 24h
                boolean isOver24h = dur.toHours() >= 24 && !isMonthly;
                dto.setQua24h(isOver24h);
            } else {
                dto.setThoiGianVaoFormatted("—");
                dto.setThoiLuongText("Vừa vào");
                dto.setQua24h(false);
            }

            // Phân loại phương tiện theo nhóm thực tế (Không tự ý đổi phân khu vật lý khi xe quá 24h)
            String typeName = "Chưa rõ";
            String icon = "fa-car";
            String maKhu = "2B";
            String tenKhu = "Khu xe 2 bánh";

            if (vx.getLoaiXe() != null) {
                dto.setLoaiXeId(vx.getLoaiXe().getId());
                typeName = vx.getLoaiXe().getTenLoai();
                if (vx.getLoaiXe().getIconClass() != null && !vx.getLoaiXe().getIconClass().isBlank()) {
                    icon = vx.getLoaiXe().getIconClass();
                }
                String lower = typeName.toLowerCase();
                if (lower.contains("máy") || lower.contains("motor")) {
                    maKhu = "2B";
                    tenKhu = "Khu xe 2 bánh";
                    icon = "fa-motorcycle";
                } else if (lower.contains("ô tô") || lower.contains("o to") || lower.contains("tải") || lower.contains("tai")) {
                    maKhu = "4B";
                    tenKhu = "Khu xe 4 bánh";
                    icon = "fa-car";
                } else if (lower.contains("đạp") || lower.contains("dap") || lower.contains("điện")) {
                    maKhu = "XD";
                    tenKhu = "Khu xe đạp";
                    icon = "fa-bicycle";
                }
            }
            dto.setTenLoaiXe(typeName);
            dto.setIconLoaiXe(icon);
            dto.setMaPhanKhu(maKhu);
            dto.setTenPhanKhu(tenKhu);

            vehicleDtos.add(dto);

            // Chỉ tính cảnh báo quá 24h đối với xe vãng lai/vé lượt (vé tháng thuê bao không cảnh báo)
            if (dto.isQua24h() && !dto.isLaVeThang()) {
                overdueDtos.add(dto);
            }
        }

        overview.setVehicles(vehicleDtos);
        overview.setOverdueVehicles(overdueDtos);
        overview.setOverdueCount(overdueDtos.size());
        overview.setOccupied(vehicleDtos.size());

        // 2. Tính sức chứa & Thống kê 4 phân khu (2B, 4B, XD, 24H)
        long totalCap = 0;
        List<TrangThaiKhuVucDto> zoneDtos = new ArrayList<>();

        for (KhuVuc kv : khuVucs) {
            String ma = kv.getMaKhu() != null ? kv.getMaKhu() : "";
            long cap = kv.getSucChua() != null && kv.getSucChua() > 0 ? kv.getSucChua() : 20;

            if (!"24H".equalsIgnoreCase(ma)) {
                totalCap += cap;
            }

            long daDo = 0;
            String iconKhu = "fa-square-parking";
            String badge = "primary";

            if ("2B".equalsIgnoreCase(ma)) {
                daDo = vehicleDtos.stream().filter(v -> "2B".equalsIgnoreCase(v.getMaPhanKhu())).count();
                iconKhu = "fa-motorcycle";
                badge = "primary";
            } else if ("4B".equalsIgnoreCase(ma)) {
                daDo = vehicleDtos.stream().filter(v -> "4B".equalsIgnoreCase(v.getMaPhanKhu())).count();
                iconKhu = "fa-car";
                badge = "success";
            } else if ("XD".equalsIgnoreCase(ma)) {
                daDo = vehicleDtos.stream().filter(v -> "XD".equalsIgnoreCase(v.getMaPhanKhu())).count();
                iconKhu = "fa-bicycle";
                badge = "info";
            } else if ("24H".equalsIgnoreCase(ma)) {
                daDo = overdueDtos.size();
                cap = Math.max(cap, daDo);
                iconKhu = "fa-clock-rotate-left";
                badge = "danger";
            }

            long choTrong = Math.max(0, cap - daDo);
            TrangThaiKhuVucDto zDto = new TrangThaiKhuVucDto(
                    kv.getId(),
                    kv.getMaKhu(),
                    kv.getTenKhu(),
                    kv.getViTri(),
                    kv.getLoaiXe() != null ? kv.getLoaiXe().getTenLoai() : "Tổng hợp",
                    cap,
                    choTrong
            );
            zDto.setIconClass(iconKhu);
            zDto.setBadgeColor(badge);
            zDto.setDaDo(daDo);
            zDto.setChoTrong(choTrong);
            zDto.setTyLeLapDay(cap > 0 ? Math.round(((double) daDo / cap) * 1000.0) / 10.0 : 0.0);

            if ("24H".equalsIgnoreCase(ma)) {
                if (daDo > 0) {
                    zDto.setMucCanhBao("QUA_TAI");
                    zDto.setThongDiepCanhBao("Phát hiện " + daDo + " xe quá 24h");
                } else {
                    zDto.setMucCanhBao("BINH_THUONG");
                    zDto.setThongDiepCanhBao("Không có xe tồn bãi");
                }
            }

            zoneDtos.add(zDto);
        }

        overview.setZones(zoneDtos);
        overview.setTotalCapacity(totalCap);
        long available = Math.max(0, totalCap - vehicleDtos.size());
        overview.setAvailable(available);

        double rate = totalCap > 0 ? Math.round(((double) vehicleDtos.size() / totalCap) * 1000.0) / 10.0 : 0.0;
        overview.setOccupancyRate(rate);

        // 3. Trạng thái thiết bị Camera AI & Barie (Đúng với giới hạn kỹ thuật của hệ thống hiện tại)
        Map<String, Object> dev = new HashMap<>();
        dev.put("cameraInStatus", "ONLINE");
        dev.put("cameraInName", "Camera AI Làn Vào");
        dev.put("cameraInDesc", "Nhận diện biển số tự động qua Web/AI Camera tại làn vào (/gui-xe)");

        dev.put("cameraOutStatus", "ONLINE");
        dev.put("cameraOutName", "Camera AI Làn Ra");
        dev.put("cameraOutDesc", "Đối soát biển số và quét mã QR vé tại làn ra (/xe-ra)");

        dev.put("barrierInStatus", "STANDBY");
        dev.put("barrierInName", "Cần Barie Làn Vào");
        dev.put("barrierInDesc", "Chế độ phần mềm: Lệnh mở gửi tự động khi bảo vệ xác nhận xe vào");
        dev.put("barrierInNote", "Hệ thống logic phần mềm; chưa kết nối cảm biến vật lý phản hồi 2 chiều");

        dev.put("barrierOutStatus", "STANDBY");
        dev.put("barrierOutName", "Cần Barie Làn Ra");
        dev.put("barrierOutDesc", "Chế độ phần mềm: Lệnh mở gửi tự động sau khi xác nhận thanh toán");
        dev.put("barrierOutNote", "Hệ thống logic phần mềm; chưa kết nối cảm biến vật lý phản hồi 2 chiều");

        overview.setDeviceStatus(dev);

        return overview;
    }

    @Override
    public List<TrangThaiKhuVucDto> layTrangThaiCacKhuVuc() {
        return layTongQuanBaiDoXe().getZones();
    }

    @Override
    public List<KhuVuc> layTatCaKhuVuc() {
        return khuVucRepository.findAll();
    }

    @Override
    public List<ViTriDo> layTatCaViTriDo() {
        List<KhuVuc> khuVucs = khuVucRepository.findAll();
        List<VeXe> activeVehicles = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        List<ViTriDo> result = new ArrayList<>();

        for (KhuVuc kv : khuVucs) {
            int capacity = kv.getSucChua() != null && kv.getSucChua() > 0 ? kv.getSucChua() : 10;
            String maKhu = kv.getMaKhu() != null ? kv.getMaKhu() : "K";
            
            // Lọc các xe đang gửi thuộc loại xe của khu này
            List<VeXe> matchedVehicles = activeVehicles.stream()
                    .filter(v -> v.getLoaiXe() != null && kv.getLoaiXe() != null && v.getLoaiXe().getId().equals(kv.getLoaiXe().getId()))
                    .collect(Collectors.toList());

            for (int i = 1; i <= capacity; i++) {
                String maViTri = String.format("%s-%02d", maKhu, i);
                ViTriDo slot = new ViTriDo(maViTri, kv, "TRONG");
                slot.setId((long) (kv.getId() * 1000 + i));

                if (i <= matchedVehicles.size()) {
                    VeXe v = matchedVehicles.get(i - 1);
                    slot.setTrangThai("DA_DO");
                    slot.setBienSoXeDangDo(v.getBienSoXe());
                    slot.setThoiGianVaoDo(v.getThoiGianVao());
                }
                result.add(slot);
            }
        }
        return result;
    }

    @Override
    public List<ViTriDo> layViTriDoTheoKhuVuc(Long khuVucId) {
        return layTatCaViTriDo().stream()
                .filter(s -> s.getKhuVuc() != null && s.getKhuVuc().getId().equals(khuVucId))
                .collect(Collectors.toList());
    }

    @Override
    public List<LoaiXe> layTatCaLoaiXe() {
        return loaiXeRepository.findAll();
    }

    @Override
    public List<ViTriDo> layViTriTrong() {
        return layTatCaViTriDo().stream()
                .filter(s -> "TRONG".equals(s.getTrangThai()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ViTriDo> timViTriTrongChoXe(LoaiXe loaiXe) {
        return layViTriTrong().stream().findFirst();
    }

    public Optional<ViTriDo> findAvailableSlotForVehicle(LoaiXe loaiXe) {
        return timViTriTrongChoXe(loaiXe);
    }

    @Override
    public void chiemViTri(ViTriDo slot, String bienSoXe) {
        // Trong hệ thống quản lý gửi xe thực tế, quản lý chỗ đỗ theo vé xe
    }

    @Override
    public void giaiPhongViTri(ViTriDo slot) {
        // Trong hệ thống quản lý gửi xe thực tế, chỗ trống cập nhật tự động khi xe xuất bến
    }

    @Override
    public void giaiPhongViTriTheoBienSo(String bienSoXe) {
        // Tự động đồng bộ theo vé xe
    }

    @Override
    public long demTongViTri() {
        return khuVucRepository.findAll().stream()
                .mapToLong(k -> k.getSucChua() != null ? k.getSucChua() : 0)
                .sum();
    }

    @Override
    public long demViTriTrong() {
        long tong = demTongViTri();
        long dangDo = demViTriDangDo();
        return Math.max(0, tong - dangDo);
    }

    @Override
    public long demViTriDangDo() {
        return veXeRepository.countByTrangThai("Đang gửi");
    }

    @Override
    public long demViTriDaDat() {
        return 0L;
    }
}

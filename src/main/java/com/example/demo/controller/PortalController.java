package com.example.demo.controller;

import com.example.demo.model.KhachHang;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeThang;
import com.example.demo.model.VeXe;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeThangRepository;
import com.example.demo.repository.VeXeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class PortalController {

    private final LoaiXeRepository loaiXeRepository;
    private final VeXeRepository veXeRepository;
    private final VeThangRepository veThangRepository;
    private final KhuVucRepository khuVucRepository;
    private final KhachHangRepository khachHangRepository;
    private final JdbcTemplate jdbcTemplate;

    public PortalController(
            LoaiXeRepository loaiXeRepository,
            VeXeRepository veXeRepository,
            VeThangRepository veThangRepository,
            KhuVucRepository khuVucRepository,
            KhachHangRepository khachHangRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.loaiXeRepository = loaiXeRepository;
        this.veXeRepository = veXeRepository;
        this.veThangRepository = veThangRepository;
        this.khuVucRepository = khuVucRepository;
        this.khachHangRepository = khachHangRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Trang chủ công khai cho khách hàng và cư dân (Public Portal / Landing Page)
     */
    @GetMapping({"/", "/home", "/portal", "/cong-thong-tin"})
    public String portalPage(Model model) {
        long tongCho = khuVucRepository.findAll().stream().mapToLong(k -> k.getSucChua() != null ? k.getSucChua() : 0).sum();
        if (tongCho <= 0) {
            tongCho = 40;
        }
        long xeDangTrongBai = veXeRepository.countByTrangThai("Đang gửi");
        long choTrong = Math.max(0, tongCho - xeDangTrongBai);
        double tyLeLapDay = tongCho > 0 ? ((double) (tongCho - choTrong) / tongCho) * 100.0 : 0.0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("tongCho", tongCho);
        stats.put("choTrong", choTrong);
        stats.put("xeDangTrongBai", xeDangTrongBai);
        stats.put("tyLeLapDay", Math.round(tyLeLapDay * 10.0) / 10.0);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        long luotVaoHomNay = veXeRepository.countByThoiGianVaoBetween(startOfDay, endOfDay);
        long luotRaHomNay = veXeRepository.countByThoiGianRaBetween(startOfDay, endOfDay);
        stats.put("luotVaoHomNay", luotVaoHomNay);
        stats.put("luotRaHomNay", luotRaHomNay);

        List<VeXe> activeTickets = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
        long countXeMay = 0;
        long countOTo = 0;
        long countXeDapDien = 0;
        for (VeXe vx : activeTickets) {
            if (vx.getLoaiXe() != null) {
                String tenLoai = vx.getLoaiXe().getTenLoai() != null ? vx.getLoaiXe().getTenLoai().toLowerCase() : "";
                if (tenLoai.contains("tô") || tenLoai.contains("car") || tenLoai.contains("oto")) {
                    countOTo++;
                } else if (tenLoai.contains("đạp") || tenLoai.contains("điện") || tenLoai.contains("dap")) {
                    countXeDapDien++;
                } else {
                    countXeMay++;
                }
            } else {
                countXeMay++;
            }
        }
        if (xeDangTrongBai > 0 && (countXeMay + countOTo + countXeDapDien == 0)) {
            countXeMay = Math.max(1, xeDangTrongBai - 2);
            countOTo = 2;
        }

        stats.put("countXeMay", countXeMay);
        stats.put("countOTo", countOTo);
        stats.put("countXeDapDien", countXeDapDien);

        // Giờ cao điểm: 07h00 - 08h30 & 16h30 - 18h30
        java.time.LocalTime now = java.time.LocalTime.now();
        int totalMinutes = now.getHour() * 60 + now.getMinute();
        boolean isRushHour = (totalMinutes >= 420 && totalMinutes <= 510) || (totalMinutes >= 990 && totalMinutes <= 1110);
        stats.put("isRushHour", isRushHour);

        List<LoaiXe> loaiXes = loaiXeRepository.findAll();
        List<KhuVuc> khuVucs = khuVucRepository.findAll();

        List<Map<String, Object>> recentReviews = getRecentReviewsFromDb();

        model.addAttribute("stats", stats);
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("khuVucs", khuVucs);
        model.addAttribute("availableSlots", choTrong);
        model.addAttribute("recentReviews", recentReviews);
        model.addAttribute("pageTitle", "Hệ thống Quản lý Bãi đỗ xe");
        model.addAttribute("activeNav", "home");
        return "portal";
    }

    /**
     * Trang web riêng: Tra cứu thông tin xe & vé gửi công khai
     */
    @GetMapping({"/tra-cuu", "/portal/tra-cuu", "/tra-cuu-xe"})
    public String traCuuPage(
            @RequestParam(value = "plate", required = false) String plate,
            Model model
    ) {
        model.addAttribute("pageTitle", "Tra cứu tình trạng xe & vé gửi");
        model.addAttribute("activeNav", "tra-cuu");
        model.addAttribute("searchedPlate", plate != null ? plate.trim() : "");
        return "portal/tra-cuu";
    }

    /**
     * Trang web riêng: Bảng giá dịch vụ gửi xe công khai
     */
    @GetMapping({"/bang-gia-dich-vu", "/portal/bang-gia", "/dich-vu/bang-gia"})
    public String bangGiaPage(Model model) {
        List<LoaiXe> loaiXes = loaiXeRepository.findAll();
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("pageTitle", "Bảng giá gửi xe chi tiết");
        model.addAttribute("activeNav", "bang-gia");
        return "portal/bang-gia";
    }

    /**
     * Trang web riêng: Đăng ký vé tháng trực tuyến
     */
    @GetMapping({"/dang-ky-ve-thang", "/ve-thang/dang-ky", "/portal/ve-thang"})
    public String dangKyVeThangPage(Model model) {
        List<LoaiXe> loaiXes = loaiXeRepository.findAll();
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("pageTitle", "Đăng ký vé tháng trực tuyến");
        model.addAttribute("activeNav", "ve-thang");
        return "portal/dang-ky-ve-thang";
    }

    /**
     * Trang web riêng: Đánh giá & Góp ý chất lượng dịch vụ
     */
    @GetMapping({"/danh-gia", "/portal/danh-gia"})
    public String danhGiaPage(Model model) {
        model.addAttribute("recentReviews", getRecentReviewsFromDb());
        model.addAttribute("pageTitle", "Đánh giá & Góp ý dịch vụ");
        model.addAttribute("activeNav", "danh-gia");
        return "portal/danh-gia";
    }

    /**
     * API Tra cứu thông tin xe và vé tháng trực tuyến dành cho khách hàng
     */
    @GetMapping("/portal/api/lookup")
    @ResponseBody
    public ResponseEntity<?> lookupVehicle(@RequestParam("plate") String plate) {
        if (plate == null || plate.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập biển số xe!"));
        }

        String rawPlate = plate.trim();
        String cleanKey = rawPlate.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleanKey.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Biển số xe không hợp lệ!"));
        }
        String displayPlate = formatPlateDisplay(cleanKey);
        Map<String, Object> resp = new HashMap<>();
        resp.put("bienSoXe", !displayPlate.isEmpty() ? displayPlate : rawPlate.toUpperCase());

        // 1. Kiểm tra xe đang gửi trong bãi (hỗ trợ mã vé QR hoặc biển số xe)
        Optional<VeXe> xeDangGuiOpt = Optional.empty();
        if (rawPlate.toUpperCase().startsWith("QR") || rawPlate.contains("-")) {
            xeDangGuiOpt = veXeRepository.findByMaQR(rawPlate);
            if (xeDangGuiOpt.isEmpty()) {
                xeDangGuiOpt = veXeRepository.findByMaQR(rawPlate.toUpperCase());
            }
        }
        if (xeDangGuiOpt.isEmpty()) {
            xeDangGuiOpt = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(displayPlate, "Đang gửi");
        }
        if (xeDangGuiOpt.isEmpty()) {
            xeDangGuiOpt = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(rawPlate.toUpperCase(), "Đang gửi");
        }
        if (xeDangGuiOpt.isEmpty()) {
            List<VeXe> activeList = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
            if (activeList != null) {
                for (VeXe vx : activeList) {
                    String existingKey = vx.getBienSoXe() != null ? vx.getBienSoXe().replaceAll("[^a-zA-Z0-9]", "").toUpperCase() : "";
                    String existingQrKey = vx.getMaQR() != null ? vx.getMaQR().replaceAll("[^a-zA-Z0-9]", "").toUpperCase() : "";
                    if (existingKey.equals(cleanKey) || (cleanKey.startsWith("QR") && existingQrKey.equals(cleanKey))) {
                        xeDangGuiOpt = Optional.of(vx);
                        break;
                    }
                }
            }
        }

        if (xeDangGuiOpt.isPresent()) {
            VeXe vx = xeDangGuiOpt.get();
            resp.put("dangGui", true);
            resp.put("bienSoXe", vx.getBienSoXe() != null ? vx.getBienSoXe() : displayPlate);
            resp.put("viTri", vx.getMaViTri());
            if (vx.getThoiGianVao() != null) {
                resp.put("thoiGianVao", vx.getThoiGianVao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                Duration dur = Duration.between(vx.getThoiGianVao(), LocalDateTime.now());
                long hours = dur.toHours();
                long mins = dur.toMinutesPart();
                resp.put("thoiLuong", hours + " giờ " + mins + " phút");
            } else {
                resp.put("thoiGianVao", "—");
                resp.put("thoiLuong", "Không xác định");
            }
            resp.put("maQR", vx.getMaQR() != null ? vx.getMaQR() : "—");
            resp.put("loaiXe", vx.getLoaiXe() != null ? vx.getLoaiXe().getTenLoai() : "Phương tiện");
        } else {
            resp.put("dangGui", false);
        }

        // 2. Kiểm tra vé tháng (hỗ trợ mã thẻ vé tháng hoặc biển số)
        Optional<VeThang> vtOpt = Optional.empty();
        if (rawPlate.toUpperCase().startsWith("VT-") || rawPlate.contains("-")) {
            vtOpt = veThangRepository.findByMaThe(rawPlate);
            if (vtOpt.isEmpty()) {
                vtOpt = veThangRepository.findByMaThe(rawPlate.toUpperCase());
            }
        }
        if (vtOpt.isEmpty()) {
            vtOpt = veThangRepository.findFirstByBienSoXeAndTrangThai(displayPlate, "HOAT_DONG");
        }
        if (vtOpt.isEmpty()) {
            vtOpt = veThangRepository.findFirstByBienSoXeAndTrangThai(rawPlate.toUpperCase(), "HOAT_DONG");
        }
        if (vtOpt.isEmpty()) {
            List<VeThang> activeTickets = veThangRepository.findByTrangThai("HOAT_DONG");
            if (activeTickets != null) {
                for (VeThang vt : activeTickets) {
                    String existingKey = vt.getBienSoXe() != null ? vt.getBienSoXe().replaceAll("[^a-zA-Z0-9]", "").toUpperCase() : "";
                    if (existingKey.equals(cleanKey) && vt.isConHan()) {
                        vtOpt = Optional.of(vt);
                        break;
                    }
                }
            }
        }

        if (vtOpt.isPresent()) {
            VeThang vt = vtOpt.get();
            resp.put("coVeThang", true);
            resp.put("maThe", vt.getMaThe());
            resp.put("chuXe", vt.getKhachHang() != null ? vt.getKhachHang().getHoTen() : "Cư dân");
            resp.put("ngayHetHan", vt.getNgayHetHan() != null ? vt.getNgayHetHan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");
            resp.put("conHan", vt.isConHan());
            resp.put("loaiXeVeThang", vt.getLoaiXe() != null ? vt.getLoaiXe().getTenLoai() : "");
        } else {
            resp.put("coVeThang", false);
        }

        if (!xeDangGuiOpt.isPresent() && !vtOpt.isPresent()) {
            resp.put("success", false);
            resp.put("message", "Không tìm thấy dữ liệu xe [" + (!displayPlate.isEmpty() ? displayPlate : rawPlate.toUpperCase()) + "] đang gửi hoặc đã đăng ký vé tháng.");
        } else {
            resp.put("success", true);
        }

        return ResponseEntity.ok(resp);
    }

    /**
     * API Đăng ký vé tháng trực tuyến công khai và Sinh mã VietQR chuyển khoản
     */
    @PostMapping("/portal/api/register-monthly")
    @ResponseBody
    public ResponseEntity<?> registerMonthlyOnline(@RequestBody Map<String, Object> req) {
        try {
            if (req.get("hoTen") == null || req.get("hoTen").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập họ và tên chủ xe!"));
            }
            if (req.get("sdt") == null || req.get("sdt").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập số điện thoại liên hệ!"));
            }
            if (req.get("bienSoXe") == null || req.get("bienSoXe").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập biển số xe đăng ký!"));
            }
            if (req.get("loaiXeId") == null || req.get("loaiXeId").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng chọn loại phương tiện!"));
            }

            String hoTen = req.get("hoTen").toString().trim();
            String sdt = req.get("sdt").toString().trim();
            String cccd = req.getOrDefault("cccd", "").toString().trim();
            String email = req.getOrDefault("email", "").toString().trim();
            String diaChi = req.getOrDefault("diaChi", "").toString().trim();
            String bienSoXe = req.get("bienSoXe").toString().trim().toUpperCase();
            Long loaiXeId = Long.valueOf(req.get("loaiXeId").toString());
            int months = Integer.parseInt(req.getOrDefault("months", "1").toString());
            if (months < 1) months = 1;

            // 1. Tìm hoặc tạo khách hàng
            KhachHang khachHang = khachHangRepository.findBySoDienThoai(sdt)
                    .orElseGet(() -> {
                        KhachHang newKh = new KhachHang(hoTen, sdt, cccd, email, diaChi);
                        return khachHangRepository.save(newKh);
                    });
            khachHang.setHoTen(hoTen);
            if (!cccd.isEmpty()) khachHang.setCccd(cccd);
            if (!email.isEmpty()) khachHang.setEmail(email);
            if (!diaChi.isEmpty()) khachHang.setDiaChi(diaChi);
            khachHangRepository.save(khachHang);

            LoaiXe loaiXe = loaiXeRepository.findById(loaiXeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại xe ID: " + loaiXeId));

            String cleanKey = bienSoXe.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
            String displayPlate = formatPlateDisplay(cleanKey);
            if (displayPlate.isEmpty()) {
                displayPlate = bienSoXe;
            }

            LocalDate startDate = LocalDate.now();
            if (req.containsKey("startDate") && req.get("startDate") != null && !req.get("startDate").toString().trim().isEmpty()) {
                try {
                    startDate = LocalDate.parse(req.get("startDate").toString().trim());
                } catch (Exception ignored) {}
            }
            LocalDate endDate = startDate.plusMonths(months);
            double basePrice = loaiXe.getGiaThang() != null ? loaiXe.getGiaThang() : 100000.0;
            double totalPrice = basePrice * months;

            String maThe = "VT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            VeThang veThang = new VeThang(
                    maThe,
                    khachHang,
                    displayPlate,
                    loaiXe,
                    startDate,
                    endDate,
                    totalPrice,
                    "HOAT_DONG"
            );
            veThangRepository.save(veThang);

            // Sinh mã QR VietQR đúng số tiền
            long intAmount = Math.round(totalPrice);
            String addInfo = URLEncoder.encode("VE THANG " + displayPlate + " " + maThe, StandardCharsets.UTF_8);
            String encodedName = URLEncoder.encode("NGUYEN HUE", StandardCharsets.UTF_8);
            String qrUrl = String.format(
                    "https://img.vietqr.io/image/971025-PSP2601617600000204-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    intAmount, addInfo, encodedName
            );

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Đăng ký vé tháng thành công! Vui lòng quét mã VietQR để hoàn tất thanh toán.");
            resp.put("maThe", maThe);
            resp.put("hoTen", hoTen);
            resp.put("bienSoXe", displayPlate);
            resp.put("loaiXe", loaiXe.getTenLoai());
            resp.put("ngayBatDau", startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            resp.put("ngayKetThuc", endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            resp.put("tongTien", totalPrice);
            resp.put("vietQrUrl", qrUrl);
            resp.put("bankId", "971025 (MoMo)");
            resp.put("accountNo", "PSP2601617600000204");
            resp.put("accountName", "NGUYỄN HUỆ");

            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi đăng ký: " + e.getMessage()));
        }
    }

    /**
     * API Gửi Đánh Giá và Góp Ý Dịch Vụ Khách Hàng (Rating 1-5 sao)
     */
    @PostMapping("/portal/api/feedback")
    @ResponseBody
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> req) {
        try {
            String noiDung = req.getOrDefault("noiDung", "").toString().trim();
            if (noiDung.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập nội dung đánh giá!"));
            }
            String hoTen = req.getOrDefault("hoTen", "Khách hàng").toString().trim();
            String sdt = req.getOrDefault("sdt", "").toString().trim();
            int soSao = Integer.parseInt(req.getOrDefault("soSao", "5").toString());
            soSao = Math.max(1, Math.min(5, soSao));
            String loaiDichVu = req.getOrDefault("loaiDichVu", "CHUNG").toString().trim();

            jdbcTemplate.update(
                    "INSERT INTO danh_gia (ho_ten, so_dien_thoai, so_sao, noi_dung, loai_dich_vu, thoi_gian) VALUES (?, ?, ?, ?, ?, ?)",
                    hoTen, sdt, soSao, noiDung, loaiDichVu, LocalDateTime.now()
            );

            return ResponseEntity.ok(Map.of("success", true, "message", "Cảm ơn bạn đã đóng góp ý kiến quý báu cho bãi đỗ xe!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi gửi đánh giá: " + e.getMessage()));
        }
    }

    /**
     * API Lấy danh sách đánh giá khách hàng
     */
    @GetMapping("/portal/api/feedbacks")
    @ResponseBody
    public ResponseEntity<?> getFeedbacks() {
        return ResponseEntity.ok(getRecentReviewsFromDb());
    }

    private List<Map<String, Object>> getRecentReviewsFromDb() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT TOP 10 id, ho_ten AS hoTen, so_dien_thoai AS soDienThoai, so_sao AS soSao, noi_dung AS noiDung, loai_dich_vu AS loaiDichVu, thoi_gian AS thoiGian FROM danh_gia ORDER BY thoi_gian DESC"
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    private String formatPlateDisplay(String cleanPlate) {
        if (cleanPlate == null) return "";
        // Ô tô 5 số: 30A12345 -> 30A-123.45
        Matcher m1 = Pattern.compile("^(\\d{2})([A-Z])(\\d{3})(\\d{2})$").matcher(cleanPlate);
        if (m1.matches()) {
            return m1.group(1) + m1.group(2) + "-" + m1.group(3) + "." + m1.group(4);
        }
        // Ô tô 2 chữ cái: 51LD12345 -> 51LD-123.45
        Matcher m2 = Pattern.compile("^(\\d{2})([A-Z]{2})(\\d{3})(\\d{2})$").matcher(cleanPlate);
        if (m2.matches()) {
            return m2.group(1) + m2.group(2) + "-" + m2.group(3) + "." + m2.group(4);
        }
        // Xe máy: 29B112345 -> 29-B1 123.45
        Matcher m3 = Pattern.compile("^(\\d{2})([A-Z]\\d)(\\d{3})(\\d{2})$").matcher(cleanPlate);
        if (m3.matches()) {
            return m3.group(1) + "-" + m3.group(2) + " " + m3.group(3) + "." + m3.group(4);
        }
        // Xe 4 số cũ: 29A1234 -> 29A-1234
        Matcher m4 = Pattern.compile("^(\\d{2})([A-Z]{1,2})(\\d{4})$").matcher(cleanPlate);
        if (m4.matches()) {
            return m4.group(1) + m4.group(2) + "-" + m4.group(3);
        }
        // Xe máy 4 số cũ: 29B11234 -> 29-B1 1234 hoặc 33M25678 -> 33-M2 5678
        Matcher m5 = Pattern.compile("^(\\d{2})([A-Z]\\d)(\\d{4})$").matcher(cleanPlate);
        if (m5.matches()) {
            return m5.group(1) + "-" + m5.group(2) + " " + m5.group(3);
        }
        return cleanPlate;
    }
}

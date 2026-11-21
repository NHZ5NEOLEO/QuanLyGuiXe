package com.example.demo.controller;

import com.example.demo.model.DatChoTruoc;
import com.example.demo.model.KhachHang;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.NguoiDung;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.service.DatChoTruocService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class DatChoTruocController {

    private final DatChoTruocService datChoTruocService;
    private final KhuVucRepository khuVucRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;

    @Autowired
    public DatChoTruocController(
            DatChoTruocService datChoTruocService,
            KhuVucRepository khuVucRepository,
            NguoiDungRepository nguoiDungRepository,
            KhachHangRepository khachHangRepository
    ) {
        this.datChoTruocService = datChoTruocService;
        this.khuVucRepository = khuVucRepository;
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
    }

    /**
     * Giao diện công khai: Đặt chỗ đỗ xe trước dành cho khách hàng & cư dân (Public Portal)
     */
    @GetMapping({"/dat-cho-truoc", "/portal/dat-cho-truoc", "/booking"})
    public String publicBookingPage(Model model, HttpServletRequest request) {
        List<KhuVuc> khuVucs = khuVucRepository.findAll();
        if (khuVucs.isEmpty()) {
            KhuVuc kv2b = new KhuVuc();
            kv2b.setMaKhu("2B");
            kv2b.setTenKhu("Khu Đỗ Xe 2 Bánh");
            kv2b.setSucChua(50);
            KhuVuc kv4b = new KhuVuc();
            kv4b.setMaKhu("4B");
            kv4b.setTenKhu("Khu Đỗ Xe 4 Bánh");
            kv4b.setSucChua(20);
            khuVucs = List.of(kv2b, kv4b);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
        NguoiDung currentUser = null;

        if (isLoggedIn) {
            String username = auth.getName();
            currentUser = nguoiDungRepository.findByTenDangNhap(username)
                    .or(() -> nguoiDungRepository.findBySoDienThoai(username))
                    .or(() -> nguoiDungRepository.findFirstByEmailIgnoreCase(username))
                    .orElse(null);
        } else {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("currentUser") != null) {
                currentUser = (NguoiDung) session.getAttribute("currentUser");
                isLoggedIn = true;
            }
        }

        model.addAttribute("khuVucs", khuVucs);
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("defaultDeposit", 20000.0);
        model.addAttribute("activeNav", "dat-cho");
        model.addAttribute("pageTitle", "Đặt Chỗ Đỗ Xe Trước — QUẢN LÝ GỬI XE");

        return "portal/dat-cho-truoc";
    }

    /**
     * API công khai: Tạo đơn đặt chỗ trước mới
     */
    @PostMapping({"/api/dat-cho-truoc/tao", "/portal/api/booking"})
    @ResponseBody
    public ResponseEntity<?> taoDatChoApi(@RequestBody Map<String, String> payload) {
        String hoTen = payload.get("hoTen");
        String soDienThoai = payload.get("soDienThoai");
        String bienSoXe = payload.get("bienSoXe");
        String maKhuVuc = payload.get("maKhuVuc");
        String soPhutGiuStr = payload.get("soPhutGiu");

        if (hoTen == null || hoTen.trim().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập họ và tên của bạn!"));
        }
        if (soDienThoai == null || soDienThoai.trim().isBlank() || !soDienThoai.trim().matches("^(0|\\+84)[0-9]{8,10}$")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại không hợp lệ (cần 9-11 chữ số)!"));
        }
        if (bienSoXe == null || bienSoXe.trim().isBlank() || bienSoXe.trim().length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập biển số xe chính xác!"));
        }

        int soPhutGiu = 60;
        if (soPhutGiuStr != null && !soPhutGiuStr.isBlank()) {
            try {
                soPhutGiu = Integer.parseInt(soPhutGiuStr);
            } catch (NumberFormatException ignored) {}
        }

        DatChoTruoc booking = datChoTruocService.taoDatCho(hoTen, soDienThoai, bienSoXe, maKhuVuc, 20000.0, soPhutGiu);
        String qrUrl = datChoTruocService.taoQrDatCoc(booking);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Đã khởi tạo đơn đặt giữ chỗ thành công!");
        resp.put("id", booking.getId());
        resp.put("maDatCho", booking.getMaDatCho());
        resp.put("hoTen", booking.getHoTen());
        resp.put("soDienThoai", booking.getSoDienThoai());
        resp.put("bienSoXe", booking.getBienSoXe());
        resp.put("maKhuVuc", booking.getMaKhuVuc());
        resp.put("tienCoc", booking.getTienCoc());
        resp.put("trangThai", booking.getTrangThai());
        resp.put("thoiGianDat", booking.getThoiGianDat() != null ? booking.getThoiGianDat().format(dtf) : "");
        resp.put("thoiGianHetHan", booking.getThoiGianHetHan() != null ? booking.getThoiGianHetHan().format(dtf) : "");
        resp.put("qrUrl", qrUrl);

        return ResponseEntity.ok(resp);
    }

    /**
     * API công khai: Tra cứu thông tin đơn đặt chỗ theo mã hoặc biển số
     */
    @GetMapping({"/api/dat-cho-truoc/tra-cuu", "/portal/api/booking/lookup"})
    @ResponseBody
    public ResponseEntity<?> traCuuDatChoApi(@RequestParam("q") String query) {
        if (query == null || query.trim().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng nhập mã đặt chỗ hoặc biển số xe để tra cứu!"));
        }

        String keyword = query.trim().toUpperCase();
        List<DatChoTruoc> list = datChoTruocService.timKiem(keyword);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        List<Map<String, Object>> result = new ArrayList<>();
        for (DatChoTruoc b : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", b.getId());
            item.put("maDatCho", b.getMaDatCho());
            item.put("hoTen", b.getHoTen());
            item.put("soDienThoai", b.getSoDienThoai());
            item.put("bienSoXe", b.getBienSoXe());
            item.put("maKhuVuc", b.getMaKhuVuc());
            item.put("tienCoc", b.getTienCoc());
            item.put("daThanhToanCoc", b.getDaThanhToanCoc());
            item.put("trangThai", b.getTrangThai());
            item.put("thoiGianDat", b.getThoiGianDat() != null ? b.getThoiGianDat().format(dtf) : "");
            item.put("thoiGianHetHan", b.getThoiGianHetHan() != null ? b.getThoiGianHetHan().format(dtf) : "");
            item.put("isExpired", b.getThoiGianHetHan() != null && b.getThoiGianHetHan().isBefore(LocalDateTime.now()));
            item.put("qrUrl", datChoTruocService.taoQrDatCoc(b));
            result.add(item);
        }

        return ResponseEntity.ok(Map.of("success", true, "data", result, "total", result.size()));
    }

    /**
     * API công khai: Khách hàng yêu cầu hủy đơn đặt chỗ trước
     */
    @PostMapping({"/api/dat-cho-truoc/huy", "/portal/api/booking/cancel"})
    @ResponseBody
    public ResponseEntity<?> huyDatChoApi(@RequestBody Map<String, Object> payload) {
        Long id = null;
        if (payload.get("id") != null) {
            try {
                id = Long.valueOf(payload.get("id").toString());
            } catch (Exception ignored) {}
        }
        if (id == null && payload.get("maDatCho") != null) {
            Optional<DatChoTruoc> opt = datChoTruocService.timTheoMaDatCho(payload.get("maDatCho").toString());
            if (opt.isPresent()) id = opt.get().getId();
        }

        if (id == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy thông tin đơn đặt chỗ!"));
        }

        boolean ok = datChoTruocService.huyDatCho(id);
        if (ok) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã hủy đơn đặt giữ chỗ thành công!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể hủy đơn đặt chỗ này (đơn đã check-in hoặc đã hết hạn)!"));
        }
    }

    /**
     * Giao diện Quản trị: Quản lý danh sách đặt chỗ đỗ xe trước (Dành cho ADMIN & NHÂN VIÊN)
     */
    @GetMapping({"/quan-ly-dat-cho", "/admin/dat-cho-truoc", "/dat-cho-truoc/quan-ly"})
    public String adminBookingManagementPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            Model model
    ) {
        List<DatChoTruoc> allBookings = datChoTruocService.layTatCaDatCho();

        long waitingCount = allBookings.stream().filter(b -> "CHO_DEN".equals(b.getTrangThai())).count();
        long checkedInCount = allBookings.stream().filter(b -> "DA_VAO".equals(b.getTrangThai())).count();
        long expiredCount = allBookings.stream().filter(b -> "HET_HAN".equals(b.getTrangThai()) || "HUY".equals(b.getTrangThai())).count();
        double totalDeposit = allBookings.stream().filter(b -> Boolean.TRUE.equals(b.getDaThanhToanCoc()))
                .mapToDouble(b -> b.getTienCoc() != null ? b.getTienCoc() : 0.0).sum();

        List<DatChoTruoc> filtered = allBookings;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            filtered = filtered.stream().filter(b -> status.equalsIgnoreCase(b.getTrangThai())).toList();
        }
        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.trim().toLowerCase();
            filtered = filtered.stream().filter(b ->
                    (b.getMaDatCho() != null && b.getMaDatCho().toLowerCase().contains(q)) ||
                    (b.getBienSoXe() != null && b.getBienSoXe().toLowerCase().contains(q)) ||
                    (b.getHoTen() != null && b.getHoTen().toLowerCase().contains(q)) ||
                    (b.getSoDienThoai() != null && b.getSoDienThoai().contains(q))
            ).toList();
        }

        model.addAttribute("bookings", filtered);
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("waitingCount", waitingCount);
        model.addAttribute("checkedInCount", checkedInCount);
        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("totalDeposit", totalDeposit);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("activePage", "dat-cho-truoc");
        model.addAttribute("pageTitle", "Quản lý Đặt chỗ đỗ xe trước");

        return "quan-ly-dat-cho";
    }

    /**
     * API Quản trị: Xác nhận đã nhận tiền đặt cọc VietQR
     */
    @PostMapping("/api/quan-ly-dat-cho/xac-nhan-coc/{id}")
    @ResponseBody
    public ResponseEntity<?> adminXacNhanCocApi(@PathVariable("id") Long id) {
        boolean ok = datChoTruocService.xacNhanThanhToanCoc(id, "XAC_NHAN_BOI_ADMIN_" + System.currentTimeMillis());
        if (ok) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xác nhận thanh toán cọc thành công!"));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy đơn đặt chỗ!"));
    }

    /**
     * API Quản trị: Hủy đơn đặt chỗ
     */
    @PostMapping("/api/quan-ly-dat-cho/huy/{id}")
    @ResponseBody
    public ResponseEntity<?> adminHuyBookingApi(@PathVariable("id") Long id) {
        boolean ok = datChoTruocService.huyDatCho(id);
        if (ok) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã hủy đơn đặt chỗ thành công!"));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể hủy đơn đặt chỗ này!"));
    }

    /**
     * API Quản trị: Xóa đơn đặt chỗ (Dành riêng cho ADMIN)
     */
    @PostMapping("/api/quan-ly-dat-cho/xoa/{id}")
    @ResponseBody
    public ResponseEntity<?> adminXoaBookingApi(@PathVariable("id") Long id) {
        boolean ok = datChoTruocService.xoaDatCho(id);
        if (ok) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa đơn đặt chỗ khỏi hệ thống!"));
        }
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể xóa đơn đặt chỗ!"));
    }
}

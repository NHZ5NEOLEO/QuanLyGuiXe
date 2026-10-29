package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.util.BienSoXeHelper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HistoryController {

    private final VeXeRepository veXeRepository;
    private final LoaiXeRepository loaiXeRepository;

    public HistoryController(VeXeRepository veXeRepository, LoaiXeRepository loaiXeRepository) {
        this.veXeRepository = veXeRepository;
        this.loaiXeRepository = loaiXeRepository;
    }

    private LocalDate parseDate(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        str = str.trim();
        try {
            if (str.contains("/")) {
                String[] parts = str.split("/");
                if (parts.length == 3) {
                    if (parts[0].length() == 4) {
                        return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy/M/d"));
                    }
                    return LocalDate.parse(str, DateTimeFormatter.ofPattern("d/M/yyyy"));
                }
            } else if (str.contains("-")) {
                String[] parts = str.split("-");
                if (parts.length == 3) {
                    if (parts[0].length() == 4) {
                        return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-M-d"));
                    } else {
                        return LocalDate.parse(str, DateTimeFormatter.ofPattern("d-M-yyyy"));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Trang danh sách vé xe - URL /ve-xe (hỗ trợ alias /tickets, /history)
     */
    @GetMapping({"/ve-xe", "/tickets", "/history"})
    public String ticketsList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "loaiXeId", required = false) Long loaiXeId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model
    ) {
        LocalDate parsedStart = parseDate(startDate);
        LocalDate parsedEnd = parseDate(endDate);

        LocalDateTime start = (parsedStart != null) ? LocalDateTime.of(parsedStart, LocalTime.MIN) : null;
        LocalDateTime end = (parsedEnd != null) ? LocalDateTime.of(parsedEnd, LocalTime.MAX) : null;

        List<VeXe> allCandidates = veXeRepository.searchHistory(null, trangThai, loaiXeId, start, end);
        List<VeXe> historyList;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            historyList = new java.util.ArrayList<>();
            for (VeXe v : allCandidates) {
                if (com.example.demo.util.BienSoXeHelper.matchesSearch(v.getBienSoXe(), kw) ||
                    (v.getMaQR() != null && v.getMaQR().toLowerCase().contains(kw.toLowerCase()))) {
                    historyList.add(v);
                }
            }
        } else {
            historyList = allCandidates;
        }
        List<LoaiXe> loaiXes = loaiXeRepository.findAll();

        double totalRevenue = 0.0;
        long activeCount = 0;
        long exitedCount = 0;
        for (VeXe v : historyList) {
            if (v != null) {
                if (v.getChiPhi() != null) {
                    totalRevenue += v.getChiPhi();
                }
                if ("Đang gửi".equalsIgnoreCase(v.getTrangThai())) {
                    activeCount++;
                } else if ("Đã ra".equalsIgnoreCase(v.getTrangThai())) {
                    exitedCount++;
                }
            }
        }

        model.addAttribute("historyList", historyList);
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("loaiXeId", loaiXeId);
        model.addAttribute("startDate", parsedStart != null ? parsedStart.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : (startDate != null ? startDate : ""));
        model.addAttribute("endDate", parsedEnd != null ? parsedEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : (endDate != null ? endDate : ""));
        model.addAttribute("totalCount", historyList.size());
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("exitedCount", exitedCount);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("activePage", "tickets");
        model.addAttribute("pageTitle", "Quản lý vé xe");

        return "ve-xe/danh-sach";
    }

    /**
     * Lấy thông tin chi tiết vé xe dạng JSON phục vụ cho Modal Sửa vé
     */
    @GetMapping({"/api/tickets/{id}", "/api/ve-xe/{id}"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiGetTicket(@PathVariable("id") Integer id) {
        VeXe t = veXeRepository.findById(id).orElse(null);
        if (t == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("maQR", t.getMaQR());
        map.put("bienSoXe", t.getBienSoXe());
        map.put("loaiXeId", t.getLoaiXe() != null ? t.getLoaiXe().getId() : null);
        map.put("loaiXeTen", t.getLoaiXe() != null ? t.getLoaiXe().getTenLoai() : "");
        map.put("trangThai", t.getTrangThai());
        map.put("thoiGianVao", t.getThoiGianVao() != null ? t.getThoiGianVao().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "");
        map.put("thoiGianRa", t.getThoiGianRa() != null ? t.getThoiGianRa().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")) : "");
        map.put("chiPhi", t.getChiPhi() != null ? t.getChiPhi() : 0);
        map.put("hinhThucThanhToan", t.getHinhThucThanhToan() != null ? t.getHinhThucThanhToan() : "TIEN_MAT");
        map.put("ghiChu", t.getGhiChu() != null ? t.getGhiChu() : "");
        return ResponseEntity.ok(map);
    }

    /**
     * Cập nhật thông tin vé xe - URL POST /ve-xe/{id}/sua hoặc /tickets/{id}/edit
     */
    @PostMapping({"/ve-xe/{id}/sua", "/tickets/{id}/edit", "/ve-xe/{id}/edit"})
    public String updateTicket(
            @PathVariable("id") Integer id,
            @RequestParam("bienSoXe") String bienSoXe,
            @RequestParam("loaiXeId") Long loaiXeId,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "thoiGianVao", required = false) String thoiGianVaoStr,
            @RequestParam(value = "thoiGianRa", required = false) String thoiGianRaStr,
            @RequestParam(value = "chiPhi", required = false) Double chiPhi,
            @RequestParam(value = "hinhThucThanhToan", defaultValue = "TIEN_MAT") String hinhThucThanhToan,
            @RequestParam(value = "ghiChu", required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        try {
            VeXe ticket = veXeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe: " + id));

            // Kiểm tra tính hợp lệ của biển số
            LoaiXe loaiXe = loaiXeRepository.findById(loaiXeId).orElse(null);
            var vResult = BienSoXeHelper.validate(bienSoXe, loaiXe != null ? loaiXe.getTenLoai() : null);
            if (!vResult.valid()) {
                throw new IllegalArgumentException(vResult.message());
            }

            ticket.setBienSoXe(vResult.displayValue());
            ticket.setLoaiXe(loaiXe);
            ticket.setTrangThai(trangThai);
            ticket.setHinhThucThanhToan(hinhThucThanhToan);
            ticket.setGhiChu(ghiChu);
            ticket.setChiPhi(chiPhi);

            if (thoiGianVaoStr != null && !thoiGianVaoStr.trim().isEmpty()) {
                ticket.setThoiGianVao(parseDateTimeFlexible(thoiGianVaoStr.trim()));
            }

            if ("Đã ra".equalsIgnoreCase(trangThai)) {
                if (thoiGianRaStr != null && !thoiGianRaStr.trim().isEmpty()) {
                    ticket.setThoiGianRa(parseDateTimeFlexible(thoiGianRaStr.trim()));
                } else if (ticket.getThoiGianRa() == null) {
                    ticket.setThoiGianRa(LocalDateTime.now());
                }
            } else {
                ticket.setThoiGianRa(null);
            }

            veXeRepository.save(ticket);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cập nhật thành công vé xe #" + ticket.getMaQR() + " (Biển số: " + ticket.getBienSoXe() + ")!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật vé xe: " + e.getMessage());
        }
        return "redirect:/ve-xe";
    }

    /**
     * Xóa vé xe khỏi hệ thống - URL POST /ve-xe/{id}/xoa hoặc /tickets/{id}/delete
     */
    @PostMapping({"/ve-xe/{id}/xoa", "/tickets/{id}/delete", "/ve-xe/{id}/delete"})
    public String deleteTicket(
            @PathVariable("id") Integer id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            VeXe ticket = veXeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe: " + id));

            String maVe = ticket.getMaQR();
            String bienSo = ticket.getBienSoXe();
            veXeRepository.delete(ticket);

            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã xóa vĩnh viễn vé xe #" + maVe + " (Biển số: " + bienSo + ") khỏi hệ thống!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa vé xe: " + e.getMessage());
        }
        return "redirect:/ve-xe";
    }

    /**
     * API xóa vé xe dạng JSON phục vụ AJAX SweetAlert2
     */
    @DeleteMapping({"/api/tickets/{id}", "/api/ve-xe/{id}"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiDeleteTicket(@PathVariable("id") Integer id) {
        try {
            VeXe ticket = veXeRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe: " + id));
            String maVe = ticket.getMaQR();
            veXeRepository.delete(ticket);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa thành công vé xe #" + maVe));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    private LocalDateTime parseDateTimeFlexible(String str) {
        if (str == null || str.trim().isEmpty()) return LocalDateTime.now();
        str = str.trim();
        try {
            if (str.contains("T")) {
                if (str.length() == 16) {
                    return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                }
                return LocalDateTime.parse(str);
            }
            if (str.contains("/")) {
                if (str.contains(":")) {
                    return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("d/M/yyyy HH:mm"));
                }
                LocalDate ld = parseDate(str);
                return ld != null ? ld.atStartOfDay() : LocalDateTime.now();
            }
            if (str.contains("-")) {
                if (str.contains(":")) {
                    if (str.length() == 16) {
                        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    }
                    return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                LocalDate ld = parseDate(str);
                return ld != null ? ld.atStartOfDay() : LocalDateTime.now();
            }
        } catch (Exception ignored) {
        }
        return LocalDateTime.now();
    }

    /**
     * Trang chi tiết vé xe riêng biệt - URL /ve-xe/{id} (hỗ trợ /tickets/{id})
     */
    @GetMapping({"/ve-xe/{id}", "/tickets/{id}"})
    public String ticketDetail(@PathVariable("id") Integer id, Model model) {
        VeXe ticket = veXeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với mã định danh: " + id));

        model.addAttribute("ticket", ticket);
        model.addAttribute("activePage", "tickets");
        model.addAttribute("pageTitle", "Chi tiết vé xe #" + ticket.getMaQR());

        return "ve-xe/chi-tiet";
    }

    /**
     * Xuất danh sách vé gửi xe ra tệp tin CSV
     */
    @GetMapping({"/ve-xe/xuat-csv", "/tickets/export-csv", "/history/export-csv"})
    public void exportCsv(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) String trangThai,
            @RequestParam(value = "loaiXeId", required = false) Long loaiXeId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            HttpServletResponse response
    ) throws IOException {
        LocalDate parsedStart = parseDate(startDate);
        LocalDate parsedEnd = parseDate(endDate);

        LocalDateTime start = (parsedStart != null) ? LocalDateTime.of(parsedStart, LocalTime.MIN) : null;
        LocalDateTime end = (parsedEnd != null) ? LocalDateTime.of(parsedEnd, LocalTime.MAX) : null;

        List<VeXe> allCandidates = veXeRepository.searchHistory(null, trangThai, loaiXeId, start, end);
        List<VeXe> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            list = new java.util.ArrayList<>();
            for (VeXe v : allCandidates) {
                if (com.example.demo.util.BienSoXeHelper.matchesSearch(v.getBienSoXe(), kw) ||
                    (v.getMaQR() != null && v.getMaQR().toLowerCase().contains(kw.toLowerCase()))) {
                    list.add(v);
                }
            }
        } else {
            list = allCandidates;
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"DanhSachVeXe_" + LocalDate.now() + ".csv\"");

        PrintWriter writer = response.getWriter();
        // UTF-8 BOM for Excel in Vietnamese
        writer.write('\ufeff');
        writer.println("Mã Vé,Biển Số Xe,Loại Xe,Vị Trí Đỗ,Thời Gian Vào,Thời Gian Ra,Trạng Thái,Chi Phí (VNĐ),Hình Thức Thanh Toán,Ghi Chú");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (VeXe v : list) {
            String vao = v.getThoiGianVao() != null ? v.getThoiGianVao().format(dtf) : "";
            String ra = v.getThoiGianRa() != null ? v.getThoiGianRa().format(dtf) : "";
            String loaiXe = v.getLoaiXe() != null ? v.getLoaiXe().getTenLoai() : "";
            String slot = v.getMaViTri();
            double tien = v.getChiPhi() != null ? v.getChiPhi() : 0.0;
            String hinhThuc = v.getHinhThucThanhToan() != null ? v.getHinhThucThanhToan() : "";
            String note = v.getGhiChu() != null ? v.getGhiChu().replace(",", " ") : "";

            writer.printf("%s,%s,%s,%s,%s,%s,%s,%.0f,%s,%s\n",
                    v.getMaQR(), v.getBienSoXe(), loaiXe, slot, vao, ra, v.getTrangThai(), tien, hinhThuc, note);
        }
        writer.flush();
    }
}

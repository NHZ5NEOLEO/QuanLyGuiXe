package com.example.demo.controller;

import com.example.demo.dto.VehicleDto;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping({"/phuong-tien", "/vehicles"})
public class VehicleWebController {

    private final VehicleService vehicleService;
    private final LoaiXeRepository loaiXeRepository;
    private final VeXeRepository veXeRepository;

    public VehicleWebController(VehicleService vehicleService, LoaiXeRepository loaiXeRepository, VeXeRepository veXeRepository) {
        this.vehicleService = vehicleService;
        this.loaiXeRepository = loaiXeRepository;
        this.veXeRepository = veXeRepository;
    }

    /**
     * Trang danh sách phương tiện - URL /vehicles
     */
    @GetMapping
    public String listVehicles(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        List<VehicleDto> vehicles;
        if (keyword != null && !keyword.trim().isEmpty()) {
            vehicles = vehicleService.searchVehicles(keyword.trim());
        } else {
            vehicles = vehicleService.getAllVehicles();
        }

        List<LoaiXe> loaiXes = loaiXeRepository.findAll();

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCount", vehicles.size());
        model.addAttribute("activePage", "vehicles");
        model.addAttribute("pageTitle", "Quản lý phương tiện");

        return "phuong-tien/danh-sach";
    }

    /**
     * Trang chi tiết phương tiện và lịch sử gửi xe - URL /vehicles/{id}
     */
    @GetMapping("/{id}")
    public String vehicleDetail(@PathVariable("id") Long id, Model model) {
        VehicleDto vehicle = vehicleService.getVehicleById(id);
        List<VeXe> parkingHistory = veXeRepository.findByBienSoXeOrderByThoiGianVaoDesc(vehicle.getBienSoXe());

        double totalSpent = 0.0;
        long activeCount = 0;
        for (VeXe v : parkingHistory) {
            if (v.getChiPhi() != null) {
                totalSpent += v.getChiPhi();
            }
            if ("Đang gửi".equals(v.getTrangThai())) {
                activeCount++;
            }
        }

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("parkingHistory", parkingHistory);
        model.addAttribute("totalVisits", parkingHistory.size());
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("isCurrentlyParked", activeCount > 0);
        model.addAttribute("activePage", "vehicles");
        model.addAttribute("pageTitle", "Hồ sơ phương tiện: " + vehicle.getBienSoXe());

        return "phuong-tien/chi-tiet";
    }

    @PostMapping("/create")
    public String createVehicle(@ModelAttribute VehicleDto dto, RedirectAttributes redirectAttributes) {
        try {
            vehicleService.createVehicle(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm phương tiện biển số [" + dto.getBienSoXe().toUpperCase() + "] thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/phuong-tien";
    }

    @PostMapping("/update")
    public String updateVehicle(
            @RequestParam("id") Long id,
            @ModelAttribute VehicleDto dto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            vehicleService.updateVehicle(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin phương tiện thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/phuong-tien";
    }

    @PostMapping("/delete")
    public String deleteVehicle(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            vehicleService.deleteVehicle(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa phương tiện thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/phuong-tien";
    }
}

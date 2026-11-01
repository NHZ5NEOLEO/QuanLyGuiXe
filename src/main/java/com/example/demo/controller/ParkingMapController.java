package com.example.demo.controller;

import com.example.demo.dto.ParkingOverviewDto;
import com.example.demo.dto.PhuongTienDangGuiDto;
import com.example.demo.dto.TrangThaiKhuVucDto;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.ViTriDo;
import com.example.demo.service.ParkingLotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ParkingMapController {

    private final ParkingLotService parkingLotService;

    public ParkingMapController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @GetMapping({"/so-do-bai-xe", "/quan-ly-do-xe", "/parking-map"})
    public String parkingMap(
            @RequestParam(value = "khuVucId", required = false) Long khuVucId,
            @RequestParam(value = "maKhu", required = false) String maKhu,
            Model model
    ) {
        ParkingOverviewDto overview = parkingLotService.layTongQuanBaiDoXe();
        List<TrangThaiKhuVucDto> allZones = overview.getZones();
        List<TrangThaiKhuVucDto> displayZones;

        if (maKhu != null && !maKhu.isBlank() && !"ALL".equalsIgnoreCase(maKhu)) {
            displayZones = allZones.stream()
                    .filter(z -> maKhu.equalsIgnoreCase(z.getMaKhu()))
                    .collect(Collectors.toList());
        } else if (khuVucId != null) {
            displayZones = allZones.stream()
                    .filter(z -> z.getId() != null && z.getId().equals(khuVucId))
                    .collect(Collectors.toList());
        } else {
            displayZones = allZones;
        }

        List<PhuongTienDangGuiDto> displayVehicles;
        if (maKhu != null && !maKhu.isBlank() && !"ALL".equalsIgnoreCase(maKhu)) {
            if ("24H".equalsIgnoreCase(maKhu)) {
                displayVehicles = overview.getOverdueVehicles();
            } else {
                displayVehicles = overview.getVehicles().stream()
                        .filter(v -> maKhu.equalsIgnoreCase(v.getMaPhanKhu()))
                        .collect(Collectors.toList());
            }
        } else {
            displayVehicles = overview.getVehicles();
        }

        List<KhuVuc> khuVucs = parkingLotService.getAllKhuVuc();
        List<ViTriDo> slots = parkingLotService.getAllViTriDo();

        model.addAttribute("overview", overview);
        model.addAttribute("khuVucs", khuVucs);
        model.addAttribute("allZones", allZones);
        model.addAttribute("displayZones", displayZones);
        model.addAttribute("vehicles", overview.getVehicles());
        model.addAttribute("displayVehicles", displayVehicles);
        model.addAttribute("overdueVehicles", overview.getOverdueVehicles());
        model.addAttribute("overdueCount", overview.getOverdueCount());
        model.addAttribute("deviceStatus", overview.getDeviceStatus());
        model.addAttribute("slots", slots);
        model.addAttribute("selectedKhuVucId", khuVucId);
        model.addAttribute("selectedMaKhu", maKhu != null ? maKhu : "ALL");

        model.addAttribute("totalSlots", overview.getTotalCapacity());
        model.addAttribute("availableSlots", overview.getAvailable());
        model.addAttribute("occupiedSlots", overview.getOccupied());
        model.addAttribute("occupancyRate", overview.getOccupancyRate());
        model.addAttribute("activePage", "quan-ly-do-xe");
        model.addAttribute("pageTitle", "Quản lý đỗ xe");

        return "so-do-bai-xe";
    }
}

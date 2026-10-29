package com.example.demo.controller;

import com.example.demo.model.KhuVuc;
import com.example.demo.model.ViTriDo;
import com.example.demo.service.ParkingLotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ParkingMapController {

    private final ParkingLotService parkingLotService;

    public ParkingMapController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @GetMapping({"/so-do-bai-xe", "/parking-map"})
    public String parkingMap(
            @RequestParam(value = "khuVucId", required = false) Long khuVucId,
            Model model
    ) {
        List<KhuVuc> khuVucs = parkingLotService.getAllKhuVuc();
        List<ViTriDo> slots;

        if (khuVucId != null) {
            slots = parkingLotService.getViTriDoByKhuVuc(khuVucId);
        } else {
            slots = parkingLotService.getAllViTriDo();
        }

        long total = parkingLotService.getTotalSlots();
        long available = parkingLotService.getAvailableSlotsCount();
        long occupied = parkingLotService.getOccupiedSlotsCount();
        long booked = parkingLotService.getBookedSlotsCount();
        double occupancyRate = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;

        model.addAttribute("khuVucs", khuVucs);
        model.addAttribute("slots", slots);
        model.addAttribute("selectedKhuVucId", khuVucId);
        model.addAttribute("totalSlots", total);
        model.addAttribute("availableSlots", available);
        model.addAttribute("occupiedSlots", occupied);
        model.addAttribute("bookedSlots", booked);
        model.addAttribute("occupancyRate", Math.round(occupancyRate * 10.0) / 10.0);
        model.addAttribute("activePage", "parking-map");
        model.addAttribute("pageTitle", "Sơ đồ bãi đỗ xe");

        return "so-do-bai-xe";
    }
}

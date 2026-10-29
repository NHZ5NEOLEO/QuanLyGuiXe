package com.example.demo.controller;

import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.util.BienSoXeHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class ActiveVehiclesController {

    private final VeXeRepository veXeRepository;
    private final LoaiXeRepository loaiXeRepository;

    public ActiveVehiclesController(VeXeRepository veXeRepository, LoaiXeRepository loaiXeRepository) {
        this.veXeRepository = veXeRepository;
        this.loaiXeRepository = loaiXeRepository;
    }

    @GetMapping({"/xe-trong-bai", "/active-vehicles"})
    public String activeVehicles(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "loaiXeId", required = false) Long loaiXeId,
            Model model
    ) {
        List<VeXe> allActive = veXeRepository.searchHistory(null, "Đang gửi", loaiXeId, null, null);
        List<VeXe> activeList;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            activeList = new ArrayList<>();
            for (VeXe vx : allActive) {
                if (BienSoXeHelper.matchesSearch(vx.getBienSoXe(), kw) ||
                    (vx.getMaQR() != null && vx.getMaQR().toLowerCase().contains(kw.toLowerCase()))) {
                    activeList.add(vx);
                }
            }
        } else {
            activeList = allActive;
        }
        List<LoaiXe> loaiXes = loaiXeRepository.findAll();

        // Tính thời gian đã đỗ cho từng xe
        Map<Integer, String> durations = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        long motorCount = 0;
        long carCount = 0;
        long bikeCount = 0;

        for (VeXe vx : activeList) {
            if (vx.getThoiGianVao() != null) {
                Duration dur = Duration.between(vx.getThoiGianVao(), now);
                long totalMins = Math.max(1, dur.toMinutes());
                long hours = totalMins / 60;
                long mins = totalMins % 60;
                String dStr = (hours > 0 ? hours + " giờ " : "") + mins + " phút";
                durations.put(vx.getId(), dStr);
            } else {
                durations.put(vx.getId(), "Vừa vào");
            }

            if (vx.getLoaiXe() != null) {
                String typeName = vx.getLoaiXe().getTenLoai().toLowerCase();
                if (typeName.contains("máy")) motorCount++;
                else if (typeName.contains("tô")) carCount++;
                else bikeCount++;
            }
        }

        model.addAttribute("activeList", activeList);
        model.addAttribute("durations", durations);
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("totalActive", activeList.size());
        model.addAttribute("motorCount", motorCount);
        model.addAttribute("carCount", carCount);
        model.addAttribute("bikeCount", bikeCount);
        model.addAttribute("keyword", keyword);
        model.addAttribute("loaiXeId", loaiXeId);
        model.addAttribute("pageTitle", "Danh sách xe đang gửi");
        model.addAttribute("activePage", "active-vehicles");

        return "xe-trong-bai";
    }
}

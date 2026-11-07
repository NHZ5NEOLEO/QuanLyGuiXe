package com.example.demo.controller;

import com.example.demo.model.DanhGia;
import com.example.demo.repository.DanhGiaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/thong-tin-danh-gia", "/danh-gia-khach-hang", "/admin/reviews", "/reviews", "/quan-ly-danh-gia", "/danh-gia-he-thong"})
public class ReviewController {

    private final DanhGiaRepository danhGiaRepository;

    public ReviewController(DanhGiaRepository danhGiaRepository) {
        this.danhGiaRepository = danhGiaRepository;
    }

    @GetMapping
    public String reviewList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "stars", required = false) Integer stars,
            @RequestParam(value = "service", required = false) String service,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        List<DanhGia> all = danhGiaRepository.findAll();
        for (DanhGia d : all) {
            if (d.getHoTen() != null && (d.getHoTen().contains("?") || d.getHoTen().toLowerCase().contains("g?i"))) {
                d.setHoTen("Khách gửi xe");
                try {
                    danhGiaRepository.save(d);
                } catch (Exception ignored) {}
            }
        }
        all.sort((a, b) -> {
            if (a.getThoiGian() == null) return 1;
            if (b.getThoiGian() == null) return -1;
            return b.getThoiGian().compareTo(a.getThoiGian());
        });

        List<DanhGia> filtered = all;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(d -> (d.getHoTen() != null && d.getHoTen().toLowerCase().contains(kw)) ||
                                 (d.getSoDienThoai() != null && d.getSoDienThoai().contains(kw)) ||
                                 (d.getNoiDung() != null && d.getNoiDung().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }

        if (stars != null && stars >= 1 && stars <= 5) {
            filtered = filtered.stream()
                    .filter(d -> d.getSoSao() != null && d.getSoSao().equals(stars))
                    .collect(Collectors.toList());
        }

        if (service != null && !service.trim().isEmpty() && !"ALL".equalsIgnoreCase(service)) {
            filtered = filtered.stream()
                    .filter(d -> service.equalsIgnoreCase(d.getLoaiDichVu()))
                    .collect(Collectors.toList());
        }

        int totalReviews = all.size();
        double avgRating = totalReviews > 0
                ? all.stream().mapToInt(d -> d.getSoSao() != null ? d.getSoSao() : 5).average().orElse(5.0)
                : 5.0;
        long positiveCount = all.stream().filter(d -> d.getSoSao() != null && d.getSoSao() >= 4).count();
        long negativeCount = all.stream().filter(d -> d.getSoSao() != null && d.getSoSao() <= 3).count();

        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));
        model.addAttribute("positiveCount", positiveCount);
        model.addAttribute("negativeCount", negativeCount);
        model.addAttribute("keyword", keyword);
        model.addAttribute("stars", stars);
        model.addAttribute("service", service);
        model.addAttribute("pageTitle", "Thông tin đánh giá & Góp ý");
        model.addAttribute("activePage", "reviews");

        List<DanhGia> paged = com.example.demo.util.PaginationHelper.paginate(filtered, page, size, model);
        model.addAttribute("reviews", paged);

        return "thong-tin-danh-gia";
    }

    @PostMapping("/delete")
    public String deleteReview(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            danhGiaRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa đánh giá thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa đánh giá: " + e.getMessage());
        }
        return "redirect:/thong-tin-danh-gia";
    }
}


package com.example.demo.controller;

import com.example.demo.model.LoaiXe;
import com.example.demo.repository.LoaiXeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping({"/bang-gia", "/price-settings"})
public class PriceSettingController {

    private final LoaiXeRepository loaiXeRepository;

    public PriceSettingController(LoaiXeRepository loaiXeRepository) {
        this.loaiXeRepository = loaiXeRepository;
    }

    @GetMapping
    public String priceSettings(Model model) {
        List<LoaiXe> loaiXes = loaiXeRepository.findAll();
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("activePage", "price-settings");
        model.addAttribute("pageTitle", "Bảng giá gửi xe");
        return "bang-gia";
    }

    @PostMapping("/update")
    public String updatePrice(
            @RequestParam("id") Long id,
            @RequestParam("giaBanNgay") Double giaBanNgay,
            @RequestParam("giaBanDem") Double giaBanDem,
            @RequestParam("giaThang") Double giaThang,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (giaBanNgay < 0 || giaBanDem < 0 || giaThang < 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mức phí gửi xe không được là số âm!");
                return "redirect:/bang-gia";
            }
            LoaiXe lx = loaiXeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại xe ID: " + id));
            lx.setGiaBanNgay(giaBanNgay);
            lx.setGiaBanDem(giaBanDem);
            lx.setGiaThang(giaThang);
            loaiXeRepository.save(lx);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật bảng giá thành công cho " + lx.getTenLoai());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/bang-gia";
    }

    @PostMapping("/create")
    public String createPrice(
            @RequestParam("tenLoai") String tenLoai,
            @RequestParam("giaBanNgay") Double giaBanNgay,
            @RequestParam("giaBanDem") Double giaBanDem,
            @RequestParam("giaThang") Double giaThang,
            @RequestParam(value = "iconClass", defaultValue = "fa-car") String iconClass,
            @RequestParam(value = "moTa", required = false) String moTa,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (giaBanNgay < 0 || giaBanDem < 0 || giaThang < 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mức phí gửi xe không được là số âm!");
                return "redirect:/bang-gia";
            }
            LoaiXe lx = new LoaiXe(tenLoai, giaBanNgay, giaBanDem, giaThang, iconClass, moTa);
            loaiXeRepository.save(lx);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới biểu phí cho loại xe '" + tenLoai + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo biểu phí: " + e.getMessage());
        }
        return "redirect:/bang-gia";
    }
}

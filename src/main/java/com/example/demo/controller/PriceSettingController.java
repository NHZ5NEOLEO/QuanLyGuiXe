package com.example.demo.controller;

import com.example.demo.model.LichSuThayDoiGia;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.NguoiDung;
import com.example.demo.repository.LichSuThayDoiGiaRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.NguoiDungRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping({"/bang-gia", "/price-settings", "/bang_gia"})
public class PriceSettingController {

    private final LoaiXeRepository loaiXeRepository;
    private final LichSuThayDoiGiaRepository lichSuThayDoiGiaRepository;
    private final NguoiDungRepository nguoiDungRepository;

    public PriceSettingController(LoaiXeRepository loaiXeRepository,
                                  LichSuThayDoiGiaRepository lichSuThayDoiGiaRepository,
                                  NguoiDungRepository nguoiDungRepository) {
        this.loaiXeRepository = loaiXeRepository;
        this.lichSuThayDoiGiaRepository = lichSuThayDoiGiaRepository;
        this.nguoiDungRepository = nguoiDungRepository;
    }

    private Double parseCurrency(String val) {
        if (val == null || val.trim().isEmpty()) return 0.0;
        String cleaned = val.replace(".", "").replace(",", "").replace("đ", "").replace("VND", "").trim();
        return Double.parseDouble(cleaned);
    }

    private String resolveCurrentUserName(HttpSession session) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                String username = auth.getName();
                NguoiDung user = nguoiDungRepository.findByTenDangNhap(username).orElse(null);
                if (user != null && user.getHoTen() != null && !user.getHoTen().trim().isEmpty()) {
                    return user.getHoTen() + " (" + username + ")";
                }
                return username;
            }
        } catch (Exception ignored) {
        }
        if (session != null) {
            Object sUser = session.getAttribute("currentUser");
            if (sUser instanceof NguoiDung u && u.getHoTen() != null) {
                return u.getHoTen();
            }
        }
        return "Quản Trị Viên";
    }

    @GetMapping
    public String priceSettings(Model model) {
        List<LoaiXe> loaiXes = loaiXeRepository.findAll();
        List<LichSuThayDoiGia> lichSuList = lichSuThayDoiGiaRepository.findAllByOrderByThoiGianThayDoiDesc();
        model.addAttribute("loaiXes", loaiXes);
        model.addAttribute("lichSuList", lichSuList);
        model.addAttribute("activePage", "price-settings");
        model.addAttribute("pageTitle", "Bảng giá gửi xe");
        return "bang-gia";
    }

    @PostMapping("/update")
    public String updatePrice(
            @RequestParam("id") Long id,
            @RequestParam("giaBanNgay") String giaBanNgayStr,
            @RequestParam("giaBanDem") String giaBanDemStr,
            @RequestParam("giaThang") String giaThangStr,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Double giaBanNgay = parseCurrency(giaBanNgayStr);
            Double giaBanDem = parseCurrency(giaBanDemStr);
            Double giaThang = parseCurrency(giaThangStr);

            if (giaBanNgay < 0 || giaBanDem < 0 || giaThang < 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mức phí gửi xe không được là số âm!");
                return "redirect:/bang-gia";
            }
            LoaiXe lx = loaiXeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại xe ID: " + id));

            Double oldNgay = lx.getGiaBanNgay();
            Double oldDem = lx.getGiaBanDem();
            Double oldThang = lx.getGiaThang();

            boolean changed = !Objects.equals(oldNgay, giaBanNgay)
                    || !Objects.equals(oldDem, giaBanDem)
                    || !Objects.equals(oldThang, giaThang);

            if (changed) {
                String currentUser = resolveCurrentUserName(session);

                StringBuilder sb = new StringBuilder();
                if (!Objects.equals(oldNgay, giaBanNgay)) {
                    sb.append(String.format("Ngày: %,.0f đ → %,.0f đ. ", oldNgay, giaBanNgay));
                }
                if (!Objects.equals(oldDem, giaBanDem)) {
                    sb.append(String.format("Đêm: %,.0f đ → %,.0f đ. ", oldDem, giaBanDem));
                }
                if (!Objects.equals(oldThang, giaThang)) {
                    sb.append(String.format("Tháng: %,.0f đ → %,.0f đ. ", oldThang, giaThang));
                }

                LichSuThayDoiGia log = new LichSuThayDoiGia();
                log.setLoaiXeId(lx.getId());
                log.setTenLoai(lx.getTenLoai());
                log.setGiaBanNgayCu(oldNgay);
                log.setGiaBanNgayMoi(giaBanNgay);
                log.setGiaBanDemCu(oldDem);
                log.setGiaBanDemMoi(giaBanDem);
                log.setGiaThangCu(oldThang);
                log.setGiaThangMoi(giaThang);
                log.setNguoiThucHien(currentUser);
                log.setThoiGianThayDoi(LocalDateTime.now());
                log.setHanhDong("CẬP NHẬT");
                log.setGhiChu(sb.toString().trim());

                // Lưu bản ghi lịch sử vào SQL Server
                lichSuThayDoiGiaRepository.save(log);

                // Cập nhật giá mới cho loại xe
                lx.setGiaBanNgay(giaBanNgay);
                lx.setGiaBanDem(giaBanDem);
                lx.setGiaThang(giaThang);
                loaiXeRepository.save(lx);

                redirectAttributes.addFlashAttribute("successMessage",
                        "Cập nhật bảng giá thành công và đã lưu lịch sử sửa đổi vào SQL cho " + lx.getTenLoai());
            } else {
                redirectAttributes.addFlashAttribute("infoMessage",
                        "Các mức giá của " + lx.getTenLoai() + " không thay đổi so với hiện tại.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/bang-gia";
    }

    @PostMapping("/create")
    public String createPrice(
            @RequestParam("tenLoai") String tenLoai,
            @RequestParam("giaBanNgay") String giaBanNgayStr,
            @RequestParam("giaBanDem") String giaBanDemStr,
            @RequestParam("giaThang") String giaThangStr,
            @RequestParam(value = "iconClass", defaultValue = "fa-car") String iconClass,
            @RequestParam(value = "moTa", required = false) String moTa,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Double giaBanNgay = parseCurrency(giaBanNgayStr);
            Double giaBanDem = parseCurrency(giaBanDemStr);
            Double giaThang = parseCurrency(giaThangStr);

            if (giaBanNgay < 0 || giaBanDem < 0 || giaThang < 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Mức phí gửi xe không được là số âm!");
                return "redirect:/bang-gia";
            }
            LoaiXe lx = new LoaiXe(tenLoai, giaBanNgay, giaBanDem, giaThang, iconClass, moTa);
            LoaiXe saved = loaiXeRepository.save(lx);

            String currentUser = resolveCurrentUserName(session);
            LichSuThayDoiGia log = new LichSuThayDoiGia();
            log.setLoaiXeId(saved.getId());
            log.setTenLoai(saved.getTenLoai());
            log.setGiaBanNgayCu(null);
            log.setGiaBanNgayMoi(giaBanNgay);
            log.setGiaBanDemCu(null);
            log.setGiaBanDemMoi(giaBanDem);
            log.setGiaThangCu(null);
            log.setGiaThangMoi(giaThang);
            log.setNguoiThucHien(currentUser);
            log.setThoiGianThayDoi(LocalDateTime.now());
            log.setHanhDong("THÊM MỚI");
            log.setGhiChu(String.format("Thêm mới loại xe: Ngày %,.0f đ | Đêm %,.0f đ | Tháng %,.0f đ", giaBanNgay, giaBanDem, giaThang));

            // Lưu bản ghi lịch sử vào SQL Server
            lichSuThayDoiGiaRepository.save(log);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm mới biểu phí cho loại xe '" + tenLoai + "' và lưu lịch sử SQL thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tạo biểu phí: " + e.getMessage());
        }
        return "redirect:/bang-gia";
    }
}

package com.example.demo.service;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.dto.TicketResponseDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.InvalidTicketStateException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.VehicleAlreadyParkedException;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeThang;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("xuLyGuiXeService")
public class ParkingService implements XuLyGuiXeService {

    private final VeXeRepository veXeRepository;
    private final LoaiXeRepository loaiXeRepository;
    private final MonthlyTicketService monthlyTicketService;
    private final VietQrService vietQrService;
    private final DatChoTruocRepository datChoTruocRepository;
    private final FeeCalculationService feeCalculationService;
    private final QrCodeService qrCodeService;

    public ParkingService(VeXeRepository veXeRepository,
                          LoaiXeRepository loaiXeRepository,
                          MonthlyTicketService monthlyTicketService,
                          VietQrService vietQrService,
                          DatChoTruocRepository datChoTruocRepository,
                          FeeCalculationService feeCalculationService,
                          QrCodeService qrCodeService) {
        this.veXeRepository = veXeRepository;
        this.loaiXeRepository = loaiXeRepository;
        this.monthlyTicketService = monthlyTicketService;
        this.vietQrService = vietQrService;
        this.datChoTruocRepository = datChoTruocRepository;
        this.feeCalculationService = feeCalculationService;
        this.qrCodeService = qrCodeService;
    }

    /**
     * Nghiệp vụ Check-in (Xe vào bãi)
     */
    @Transactional(rollbackFor = Exception.class)
    public VeXe checkIn(CheckInRequest request) {
        if (request == null || request.getBienSoXe() == null || request.getBienSoXe().trim().isEmpty()) {
            throw new BadRequestException("Biển số xe không được để trống!");
        }

        // Xác định loại xe trước để hỗ trợ chuẩn hóa mẫu biển số chính xác
        LoaiXe loaiXe = null;
        if (request.getLoaiXeId() != null) {
            loaiXe = loaiXeRepository.findById(request.getLoaiXeId()).orElse(null);
        }
        if (loaiXe == null) {
            List<LoaiXe> loaiXes = loaiXeRepository.findAll();
            if (!loaiXes.isEmpty()) {
                loaiXe = loaiXes.get(0);
            }
        }

        String rawPlate = request.getBienSoXe().trim();
        String loaiXeTen = (loaiXe != null) ? loaiXe.getTenLoai() : null;

        // Chuẩn hóa và kiểm tra cấu trúc biển số xe Việt Nam
        com.example.demo.util.BienSoXeHelper.ValidationResult vResult = 
                com.example.demo.util.BienSoXeHelper.validate(rawPlate, loaiXeTen);
        if (!vResult.valid()) {
            throw new BadRequestException(vResult.message());
        }

        String bienSoDisplay = vResult.displayValue();
        String cleanKey = vResult.cleanKey();

        // 1. Kiểm tra xem xe này có đang trong bãi chưa ra không (chống tạo lượt gửi trùng lặp)
        Optional<VeXe> xeDangGui = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(rawPlate.toUpperCase(), "Đang gửi");
        if (xeDangGui.isEmpty() && !bienSoDisplay.equalsIgnoreCase(rawPlate)) {
            xeDangGui = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(bienSoDisplay, "Đang gửi");
        }
        if (xeDangGui.isEmpty()) {
            List<VeXe> activeList = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
            if (activeList != null) {
                for (VeXe vx : activeList) {
                    if (com.example.demo.util.BienSoXeHelper.isSamePlate(vx.getBienSoXe(), cleanKey)) {
                        xeDangGui = Optional.of(vx);
                        break;
                    }
                }
            }
        }

        if (xeDangGui.isPresent()) {
            throw new VehicleAlreadyParkedException("Phương tiện biển số [" + bienSoDisplay + "] hiện đang có lượt gửi xe chưa hoàn tất (chưa Check-out)!");
        }

        // 2. Tạo mã vé / QR token duy nhất
        String maQR = (request.getMaQR() != null && !request.getMaQR().trim().isEmpty())
                ? request.getMaQR().trim()
                : "QR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        if (veXeRepository.findByMaQR(maQR).isPresent()) {
            maQR = maQR + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        }

        // 4. Kiểm tra xem xe có vé tháng hợp lệ không
        Optional<VeThang> veThangOpt = monthlyTicketService.findActiveMonthlyPass(bienSoDisplay);
        if (veThangOpt.isEmpty() && !cleanKey.equalsIgnoreCase(bienSoDisplay)) {
            veThangOpt = monthlyTicketService.findActiveMonthlyPass(cleanKey);
        }

        // 5. Kiểm tra xem xe có đặt chỗ trước (Pre-booking) không
        Optional<com.example.demo.model.DatChoTruoc> bookingOpt = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(bienSoDisplay, "CHO_DEN");
        if (bookingOpt.isEmpty()) {
            bookingOpt = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(rawPlate.toUpperCase(), "CHO_DEN");
        }

        String ghiChu = request.getGhiChu();
        if (bookingOpt.isPresent()) {
            var bk = bookingOpt.get();
            bk.setTrangThai("DA_VAO");
            datChoTruocRepository.save(bk);
            String note = "Đã cọc trực tuyến " + String.format("%,.0fđ", bk.getTienCoc() != null ? bk.getTienCoc() : 20000.0);
            ghiChu = (ghiChu != null && !ghiChu.isEmpty()) ? ghiChu + " | " + note : note;
        }

        // 6. Khởi tạo lượt gửi xe với thời gian máy chủ và biển số hiển thị chuẩn
        VeXe veXe = new VeXe();
        veXe.setBienSoXe(bienSoDisplay);
        veXe.setMaQR(maQR);
        veXe.setLoaiXe(loaiXe);
        veXe.setThoiGianVao(LocalDateTime.now()); // Server time
        veXe.setTrangThai("Đang gửi");
        veXe.setAnhVaoUrl(request.getAnhVaoData());
        veXe.setGhiChu(ghiChu);

        if (veThangOpt.isPresent()) {
            veXe.setVeThang(veThangOpt.get());
            veXe.setHinhThucThanhToan("VE_THANG");
        }

        return veXeRepository.save(veXe);
    }

    /**
     * Tính toán chi phí và xem trước thông tin Check-out
     */
    public CheckOutPreviewResponse previewCheckOut(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng nhập hoặc quét Mã QR / Biển số xe!");
        }

        String query = identifier.trim();
        Optional<VeXe> veXeOpt = veXeRepository.findByMaQR(query);
        if (veXeOpt.isEmpty()) {
            veXeOpt = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(query.toUpperCase(), "Đang gửi");
        }
        if (veXeOpt.isEmpty()) {
            String displayPlate = com.example.demo.util.BienSoXeHelper.toDisplayFormat(query);
            if (!displayPlate.equalsIgnoreCase(query)) {
                veXeOpt = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(displayPlate, "Đang gửi");
            }
        }
        if (veXeOpt.isEmpty()) {
            List<VeXe> activeList = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
            if (activeList != null) {
                for (VeXe vx : activeList) {
                    if (com.example.demo.util.BienSoXeHelper.isSamePlate(vx.getBienSoXe(), query)) {
                        veXeOpt = Optional.of(vx);
                        break;
                    }
                }
            }
        }

        if (veXeOpt.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin lượt gửi xe cho mã / biển số: " + query);
        }

        VeXe veXe = veXeOpt.get();
        if (!"Đang gửi".equals(veXe.getTrangThai())) {
            throw new InvalidTicketStateException("Vé xe này đã được Check-out trước đó vào lúc " +
                    (veXe.getThoiGianRa() != null ? veXe.getThoiGianRa().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
        }

        LocalDateTime thoiGianRa = LocalDateTime.now(); // Server time
        boolean laVeThang = veXe.getVeThang() != null || monthlyTicketService.findActiveMonthlyPass(veXe.getBienSoXe()).isPresent();
        boolean daCoc = veXe.getGhiChu() != null && veXe.getGhiChu().contains("Đã cọc");

        FeeCalculationResult feeResult = feeCalculationService.calculate(veXe.getLoaiXe(), veXe.getThoiGianVao(), thoiGianRa, laVeThang, daCoc);

        CheckOutPreviewResponse res = new CheckOutPreviewResponse();
        res.setVeXeId(veXe.getId());
        res.setMaQR(veXe.getMaQR());
        res.setBienSoXe(veXe.getBienSoXe());
        res.setTenLoaiXe(veXe.getLoaiXe() != null ? veXe.getLoaiXe().getTenLoai() : "Chưa xác định");
        res.setMaViTri("Khu vực chung");
        res.setThoiGianVao(veXe.getThoiGianVao());
        res.setThoiGianRa(thoiGianRa);
        res.setThoiLuong(feeResult.getThoiLuongText());
        res.setAnhVaoUrl(veXe.getAnhVaoUrl());
        res.setLaVeThang(laVeThang);

        if (laVeThang) {
            if (veXe.getVeThang() != null && veXe.getVeThang().getKhachHang() != null) {
                res.setTenKhachHang(veXe.getVeThang().getKhachHang().getHoTen());
            } else {
                res.setTenKhachHang("Khách vé tháng");
            }
        }

        double fee = feeResult.getSoTienThanhToan();
        res.setSoTien(fee);

        // Sinh link VietQR nếu có phí phải nộp
        if (fee > 0) {
            String vietQrUrl = vietQrService.generateQrUrl(fee, veXe.getBienSoXe(), veXe.getMaQR());
            res.setVietQrUrl(vietQrUrl);
        }

        return res;
    }

    /**
     * Xác nhận Check-out hoàn tất lượt gửi xe
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized VeXe confirmCheckOut(Integer veXeId, String hinhThucThanhToan, String anhRaData, String ghiChu) {
        if (veXeId == null) {
            throw new BadRequestException("ID vé xe không được để trống!");
        }

        VeXe veXe = veXeRepository.findById(veXeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với ID: " + veXeId));

        if (!"Đang gửi".equals(veXe.getTrangThai())) {
            throw new InvalidTicketStateException("Vé xe [" + veXe.getMaQR() + "] đã hoàn thành check-out trước đó!");
        }

        LocalDateTime thoiGianRa = LocalDateTime.now(); // Server time
        if (thoiGianRa.isBefore(veXe.getThoiGianVao())) {
            throw new InvalidTicketStateException("Thời gian ra không được nhỏ hơn thời gian vào!");
        }

        boolean laVeThang = veXe.getVeThang() != null || monthlyTicketService.findActiveMonthlyPass(veXe.getBienSoXe()).isPresent();
        boolean daCoc = veXe.getGhiChu() != null && veXe.getGhiChu().contains("Đã cọc");

        FeeCalculationResult feeResult = feeCalculationService.calculate(veXe.getLoaiXe(), veXe.getThoiGianVao(), thoiGianRa, laVeThang, daCoc);
        double fee = feeResult.getSoTienThanhToan();

        veXe.setThoiGianRa(thoiGianRa);
        veXe.setChiPhi(fee);
        veXe.setTrangThai("Đã ra");
        veXe.setHinhThucThanhToan(laVeThang ? "VE_THANG" : (hinhThucThanhToan != null ? hinhThucThanhToan : "TIEN_MAT"));
        if (anhRaData != null && !anhRaData.isEmpty()) {
            veXe.setAnhRaUrl(anhRaData);
        }
        if (ghiChu != null && !ghiChu.isEmpty()) {
            veXe.setGhiChu(veXe.getGhiChu() != null ? veXe.getGhiChu() + " | " + ghiChu : ghiChu);
        }

        return veXeRepository.save(veXe);
    }

    /**
     * Tra cứu vé xe theo ID
     */
    public VeXe getTicketById(Integer id) {
        return veXeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với ID: " + id));
    }

    /**
     * Tra cứu vé xe theo Mã vé / Mã QR
     */
    public VeXe getTicketByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Mã vé không được để trống!");
        }
        return veXeRepository.findByMaQR(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với mã: " + code));
    }

    /**
     * Chuyển đổi VeXe entity sang TicketResponseDto
     */
    public TicketResponseDto mapToTicketResponseDto(VeXe veXe) {
        if (veXe == null) return null;
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(veXe.getId());
        dto.setTicketCode(veXe.getMaQR());
        dto.setBienSoXe(veXe.getBienSoXe());
        if (veXe.getLoaiXe() != null) {
            dto.setLoaiXeId(veXe.getLoaiXe().getId());
            dto.setTenLoaiXe(veXe.getLoaiXe().getTenLoai());
        }
        dto.setMaViTri(veXe.getMaViTri());
        dto.setThoiGianVao(veXe.getThoiGianVao());
        dto.setThoiGianRa(veXe.getThoiGianRa());
        dto.setChiPhi(veXe.getChiPhi());
        dto.setHinhThucThanhToan(veXe.getHinhThucThanhToan());
        dto.setTrangThai(veXe.getTrangThai());
        dto.setLaVeThang(veXe.getVeThang() != null);
        if (veXe.getVeThang() != null && veXe.getVeThang().getKhachHang() != null) {
            dto.setTenKhachHang(veXe.getVeThang().getKhachHang().getHoTen());
        }
        dto.setGhiChu(veXe.getGhiChu());
        dto.setAnhVaoUrl(veXe.getAnhVaoUrl());
        dto.setAnhRaUrl(veXe.getAnhRaUrl());

        // Sinh mã QR Base64
        try {
            dto.setQrCodeBase64(qrCodeService.generateQrBase64(veXe.getMaQR(), 250, 250));
        } catch (Exception ignored) {
        }

        return dto;
    }

    /**
     * Công thức tính giá vé chuẩn (Ủy quyền cho FeeCalculationService)
     */
    public double calculateFee(LoaiXe loaiXe, LocalDateTime vao, LocalDateTime ra, long totalMinutes) {
        FeeCalculationResult result = feeCalculationService.calculate(loaiXe, vao, ra, false, false);
        return result.getSoTienThanhToan();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VeXe ghiNhanXeVao(com.example.demo.dto.YeuCauVaoBai request) {
        if (request instanceof CheckInRequest) {
            return checkIn((CheckInRequest) request);
        }
        CheckInRequest req = new CheckInRequest(request.getBienSoXe(), request.getMaQR(), request.getLoaiXeId(), request.getMaViTri(), request.getAnhVaoData(), request.getGhiChu());
        return checkIn(req);
    }

    @Override
    public com.example.demo.dto.ThongTinXuatBai xemTruocXuatBai(String identifier) {
        return previewCheckOut(identifier);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VeXe xacNhanXuatBai(Integer veXeId, String hinhThucThanhToan, String anhRaData, String ghiChu) {
        return confirmCheckOut(veXeId, hinhThucThanhToan, anhRaData, ghiChu);
    }

    @Override
    public VeXe timVeTheoId(Integer id) {
        return getTicketById(id);
    }

    @Override
    public VeXe timVeTheoMa(String code) {
        return getTicketByCode(code);
    }

    @Override
    public com.example.demo.dto.PhanHoiVeXeDto chuyenSangPhanHoiDto(VeXe veXe) {
        return mapToTicketResponseDto(veXe);
    }

    @Override
    public double tinhPhi(LoaiXe loaiXe, LocalDateTime vao, LocalDateTime ra, long tongPhut) {
        return calculateFee(loaiXe, vao, ra, tongPhut);
    }
}

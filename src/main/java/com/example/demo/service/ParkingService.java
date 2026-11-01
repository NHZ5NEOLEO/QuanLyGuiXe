package com.example.demo.service;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.dto.FeeCalculationResult;
import com.example.demo.dto.TicketResponseDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.InvalidTicketStateException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.VehicleAlreadyParkedException;
import com.example.demo.model.DanhSachDen;
import com.example.demo.model.DatChoTruoc;
import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeThang;
import com.example.demo.model.VeXe;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.repository.KhuVucRepository;
import com.example.demo.repository.LoaiXeRepository;
import com.example.demo.repository.VeXeRepository;
import com.example.demo.util.BienSoXeHelper;
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
    private final KhuVucRepository khuVucRepository;
    private final BlacklistService blacklistService;

    public ParkingService(VeXeRepository veXeRepository,
                          LoaiXeRepository loaiXeRepository,
                          MonthlyTicketService monthlyTicketService,
                          VietQrService vietQrService,
                          DatChoTruocRepository datChoTruocRepository,
                          FeeCalculationService feeCalculationService,
                          QrCodeService qrCodeService,
                          KhuVucRepository khuVucRepository,
                          BlacklistService blacklistService) {
        this.veXeRepository = veXeRepository;
        this.loaiXeRepository = loaiXeRepository;
        this.monthlyTicketService = monthlyTicketService;
        this.vietQrService = vietQrService;
        this.datChoTruocRepository = datChoTruocRepository;
        this.feeCalculationService = feeCalculationService;
        this.qrCodeService = qrCodeService;
        this.khuVucRepository = khuVucRepository;
        this.blacklistService = blacklistService;
    }

    /**
     * Nghiệp vụ Check-in (Xe vào bãi)
     */
    @Transactional(rollbackFor = Exception.class)
    public VeXe checkIn(CheckInRequest request) {
        if (request == null || request.getBienSoXe() == null || request.getBienSoXe().trim().isEmpty()) {
            throw new BadRequestException("Biển số xe không được để trống!");
        }

        // Kiểm tra loại xe bắt buộc, không tự động fallback về loại đầu tiên khi ID thiếu hoặc không hợp lệ
        if (request.getLoaiXeId() == null) {
            throw new BadRequestException("Vui lòng chọn loại phương tiện hợp lệ!");
        }
        LoaiXe loaiXe = loaiXeRepository.findById(request.getLoaiXeId())
                .orElseThrow(() -> new BadRequestException("Loại phương tiện không tồn tại (ID: " + request.getLoaiXeId() + ")!"));

        String rawPlate = request.getBienSoXe().trim();
        if (rawPlate.toUpperCase().startsWith("VT-")) {
            var cardOpt = monthlyTicketService.timTheoMaThe(rawPlate.toUpperCase());
            if (cardOpt.isPresent()) {
                rawPlate = cardOpt.get().getBienSoXe();
            }
        } else if (rawPlate.toUpperCase().startsWith("BK-")) {
            var bkOpt = datChoTruocRepository.findByMaDatCho(rawPlate.toUpperCase());
            if (bkOpt.isPresent()) {
                rawPlate = bkOpt.get().getBienSoXe();
            }
        }
        String loaiXeTen = loaiXe.getTenLoai();

        // Chuẩn hóa và kiểm tra cấu trúc biển số xe Việt Nam
        BienSoXeHelper.ValidationResult vResult = BienSoXeHelper.validate(rawPlate, loaiXeTen);
        if (!vResult.valid()) {
            throw new BadRequestException("Biển số xe không hợp lệ: " + vResult.message());
        }

        String bienSoDisplay = vResult.displayValue();
        String cleanKey = vResult.cleanKey();

        // Kiểm tra Danh sách đen (Blacklist) trong Backend trước khi tạo vé
        if (blacklistService != null) {
            Optional<DanhSachDen> blackOpt = blacklistService.checkBlacklist(bienSoDisplay);
            if (blackOpt.isEmpty() && !cleanKey.equalsIgnoreCase(bienSoDisplay)) {
                blackOpt = blacklistService.checkBlacklist(cleanKey);
            }
            if (blackOpt.isPresent()) {
                throw new BadRequestException("CẢNH BÁO AN NINH: Phương tiện biển số [" + bienSoDisplay + 
                        "] nằm trong Danh sách đen (Lý do: " + blackOpt.get().getLyDo() + "). Từ chối tạo vé vào bãi!");
            }
        }

        // Chống race condition: khóa theo biển số đã chuẩn hóa
        synchronized (("checkin-plate-" + cleanKey).intern()) {
            // 1. Kiểm tra xem xe này có đang trong bãi chưa ra không (chống tạo lượt gửi trùng lặp)
            if (veXeRepository.existsByBienSoCleanAndTrangThai(cleanKey, "Đang gửi")) {
                throw new VehicleAlreadyParkedException("Phương tiện biển số [" + bienSoDisplay + "] hiện đang có lượt gửi xe chưa hoàn tất (chưa xuất bãi)!");
            }

            Optional<VeXe> xeDangGui = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(rawPlate.toUpperCase(), "Đang gửi");
            if (xeDangGui.isEmpty() && !bienSoDisplay.equalsIgnoreCase(rawPlate)) {
                xeDangGui = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(bienSoDisplay, "Đang gửi");
            }
            if (xeDangGui.isPresent()) {
                throw new VehicleAlreadyParkedException("Phương tiện biển số [" + bienSoDisplay + "] hiện đang có lượt gửi xe chưa hoàn tất (chưa xuất bãi)!");
            }

            // 2. Tạo mã vé / QR token duy nhất
            String maQR = (request.getMaQR() != null && !request.getMaQR().trim().isEmpty())
                    ? request.getMaQR().trim()
                    : "QR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            if (veXeRepository.findByMaQR(maQR).isPresent()) {
                maQR = maQR + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            }

            // 3. Kiểm tra sức chứa phân khu (nếu có cấu hình)
            if (khuVucRepository != null) {
                String loai = loaiXe.getTenLoai().toLowerCase();
                KhuVuc khuVuc = null;
                if (loai.contains("máy") || loai.contains("motor")) {
                    khuVuc = khuVucRepository.findByMaKhu("2B").orElse(null);
                } else if (loai.contains("ô tô") || loai.contains("tải")) {
                    khuVuc = khuVucRepository.findByMaKhu("4B").orElse(null);
                } else {
                    khuVuc = khuVucRepository.findByMaKhu("XD").orElse(null);
                }
                if (khuVuc != null && khuVuc.getSucChua() != null && khuVuc.getSucChua() > 0) {
                    long curParked = veXeRepository.countByTrangThaiAndLoaiXeId("Đang gửi", loaiXe.getId());
                    if (curParked >= khuVuc.getSucChua()) {
                        throw new BadRequestException("Phân khu đỗ xe [" + khuVuc.getTenKhu() + "] đã hết chỗ trống (" + khuVuc.getSucChua() + " chỗ)!");
                    }
                }
            }

            // 4. Kiểm tra vé tháng hợp lệ theo quy tắc thống nhất (phải còn hạn, HOAT_DONG, và đúng loại xe)
            Optional<VeThang> veThangOpt = monthlyTicketService.findActiveMonthlyPass(bienSoDisplay, loaiXe.getId());
            if (veThangOpt.isEmpty() && !cleanKey.equalsIgnoreCase(bienSoDisplay)) {
                veThangOpt = monthlyTicketService.findActiveMonthlyPass(cleanKey, loaiXe.getId());
            }

            // 5. Kiểm tra xem xe có đặt chỗ trước (Pre-booking) không
            Optional<DatChoTruoc> bookingOpt = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(bienSoDisplay, "CHO_DEN");
            if (bookingOpt.isEmpty() && !cleanKey.equalsIgnoreCase(bienSoDisplay)) {
                bookingOpt = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(cleanKey, "CHO_DEN");
            }
            if (bookingOpt.isEmpty()) {
                bookingOpt = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(rawPlate.toUpperCase(), "CHO_DEN");
            }

            String ghiChu = request.getGhiChu();
            DatChoTruoc validBooking = null;
            if (bookingOpt.isPresent()) {
                DatChoTruoc bk = bookingOpt.get();
                if (bk.getThoiGianHetHan() != null && bk.getThoiGianHetHan().isBefore(LocalDateTime.now())) {
                    bk.setTrangThai("HET_HAN");
                    datChoTruocRepository.save(bk);
                } else {
                    // Booking hợp lệ: Giữ tham chiếu nhưng chỉ cập nhật trạng thái sau khi tạo lượt gửi thành công
                    validBooking = bk;
                    String note = "Đặt chỗ trước [" + bk.getMaDatCho() + "]. Chưa có mô hình giao dịch cọc";
                    ghiChu = (ghiChu != null && !ghiChu.isEmpty()) ? ghiChu + " | " + note : note;
                }
            }

            // 6. Khởi tạo lượt gửi xe với thời gian máy chủ và không giả lập vị trí ô đỗ cụ thể (để Khu vực chung)
            VeXe veXe = new VeXe();
            veXe.setBienSoXe(bienSoDisplay);
            veXe.setBienSoClean(cleanKey);
            veXe.setMaQR(maQR);
            veXe.setLoaiXe(loaiXe);
            veXe.setViTriDo(null); // Giữ khu vực chung, không giả lập ô đỗ cụ thể khi chưa có quản lý cảm biến thực tế
            veXe.setDatChoTruoc(validBooking);
            veXe.setTienCocKhauTru(0.0); // Không tự ý khấu trừ tiền cọc khi chưa có mô hình giao dịch cọc xác minh
            veXe.setThoiGianVao(LocalDateTime.now()); // Giờ server
            veXe.setTrangThai("Đang gửi");
            veXe.setTrangThaiThanhToan(veThangOpt.isPresent() ? "MIEN_PHI" : "CHUA_THANH_TOAN");
            veXe.setAnhVaoUrl(request.getAnhVaoData());
            veXe.setGhiChu(ghiChu);

            if (veThangOpt.isPresent()) {
                veXe.setVeThang(veThangOpt.get());
                veXe.setHinhThucThanhToan("VE_THANG");
            }

            VeXe savedTicket = veXeRepository.save(veXe);

            // Chỉ cập nhật trạng thái booking sau khi tạo lượt gửi thành công trong transaction
            if (validBooking != null) {
                validBooking.setTrangThai("DA_VAO");
                validBooking.setThoiGianCheckIn(LocalDateTime.now());
                datChoTruocRepository.save(validBooking);
            }

            return savedTicket;
        }
    }

    /**
     * Tính toán chi phí và xem trước thông tin Check-out (Chỉ cho phép lượt Đang gửi)
     */
    public CheckOutPreviewResponse previewCheckOut(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng nhập hoặc quét Mã QR / Biển số xe!");
        }

        String query = identifier.trim();
        if (query.toUpperCase().startsWith("VT-")) {
            var cardOpt = monthlyTicketService.timTheoMaThe(query.toUpperCase());
            if (cardOpt.isPresent()) {
                query = cardOpt.get().getBienSoXe();
            }
        }
        Optional<VeXe> veXeOpt = veXeRepository.findByMaQR(query);
        if (veXeOpt.isEmpty()) {
            veXeOpt = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(query.toUpperCase(), "Đang gửi");
        }
        if (veXeOpt.isEmpty()) {
            String displayPlate = BienSoXeHelper.toDisplayFormat(query);
            if (!displayPlate.equalsIgnoreCase(query)) {
                veXeOpt = veXeRepository.findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(displayPlate, "Đang gửi");
            }
        }
        if (veXeOpt.isEmpty()) {
            List<VeXe> activeList = veXeRepository.findByTrangThaiOrderByThoiGianVaoDesc("Đang gửi");
            if (activeList != null) {
                for (VeXe vx : activeList) {
                    if (BienSoXeHelper.isSamePlate(vx.getBienSoXe(), query)) {
                        veXeOpt = Optional.of(vx);
                        break;
                    }
                }
            }
        }

        if (veXeOpt.isEmpty()) {
            // Kiểm tra xem vé có trong DB nhưng đã checkout chưa để báo lỗi chính xác
            Optional<VeXe> anyTicket = veXeRepository.findByMaQR(query);
            if (anyTicket.isEmpty()) {
                List<VeXe> byPlate = veXeRepository.findByBienSoXeOrderByThoiGianVaoDesc(query);
                if (!byPlate.isEmpty()) anyTicket = Optional.of(byPlate.get(0));
            }
            if (anyTicket.isPresent()) {
                VeXe closed = anyTicket.get();
                throw new InvalidTicketStateException("Vé xe [" + closed.getMaQR() + "] đã được xử lý xe ra trước đó vào lúc " +
                        (closed.getThoiGianRa() != null ? closed.getThoiGianRa().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
            }
            throw new ResourceNotFoundException("Không tìm thấy thông tin lượt gửi xe cho mã / biển số: " + query);
        }

        VeXe veXe = veXeOpt.get();
        if (!"Đang gửi".equals(veXe.getTrangThai())) {
            throw new InvalidTicketStateException("Vé xe này đã được xử lý xe ra trước đó vào lúc " +
                    (veXe.getThoiGianRa() != null ? veXe.getThoiGianRa().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
        }

        LocalDateTime thoiGianRa = LocalDateTime.now(); // Server time
        boolean laVeThang = checkValidMonthlyPass(veXe);
        boolean daCoc = veXe.getDatChoTruoc() != null;

        // Áp dụng cùng 1 implementation tính phí
        FeeCalculationResult feeResult = feeCalculationService.calculate(veXe.getLoaiXe(), veXe.getThoiGianVao(), thoiGianRa, laVeThang, daCoc);

        CheckOutPreviewResponse res = new CheckOutPreviewResponse();
        res.setVeXeId(veXe.getId());
        res.setMaQR(veXe.getMaQR());
        res.setBienSoXe(veXe.getBienSoXe());
        res.setTenLoaiXe(veXe.getLoaiXe() != null ? veXe.getLoaiXe().getTenLoai() : "Chưa xác định");
        res.setMaViTri(veXe.getViTriDo() != null ? veXe.getViTriDo().getMaViTri() : "Khu vực chung");
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
        res.setPhuPhiLuuKho(feeResult.getPhuPhiLuuKho());
        res.setQuyTacApDung(feeResult.getQuyTacApDung());

        // Sinh link VietQR nếu có phí phải nộp
        if (fee > 0) {
            String vietQrUrl = vietQrService.generateQrUrl(fee, veXe.getBienSoXe(), veXe.getMaQR());
            res.setVietQrUrl(vietQrUrl);
        }

        return res;
    }

    /**
     * Xác nhận Check-out hoàn tất lượt gửi xe (Có kiểm tra đối chiếu biển số thực tế và người xác nhận)
     */
    @Transactional(rollbackFor = Exception.class)
    public VeXe confirmCheckOut(Integer veXeId, String hinhThucThanhToan, String anhRaData, String ghiChu, String bienSoThucTe, String nguoiXacNhan) {
        if (veXeId == null) {
            throw new BadRequestException("ID vé xe không được để trống!");
        }

        // Khóa chống double checkout cho cùng một vé
        synchronized (("checkout-ticket-" + veXeId).intern()) {
            VeXe veXe = veXeRepository.findById(veXeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với ID: " + veXeId));

            if (!"Đang gửi".equals(veXe.getTrangThai())) {
                throw new InvalidTicketStateException("Vé xe [" + veXe.getMaQR() + "] đã hoàn thành check-out trước đó hoặc không ở trạng thái đang gửi!");
            }

            // Kiểm tra đối chiếu biển số xe thực tế nếu có cung cấp
            if (bienSoThucTe != null && !bienSoThucTe.trim().isEmpty()) {
                if (!BienSoXeHelper.isSamePlate(bienSoThucTe.trim(), veXe.getBienSoXe())) {
                    throw new BadRequestException("Biển số xe thực tế [" + bienSoThucTe.trim() + 
                            "] KHÔNG KHỚP với thông tin trên vé xe [" + veXe.getBienSoXe() + "]! Yêu cầu nhân viên kiểm tra thủ công.");
                }
            }

            LocalDateTime thoiGianRa = LocalDateTime.now(); // Server time
            if (thoiGianRa.isBefore(veXe.getThoiGianVao())) {
                throw new InvalidTicketStateException("Thời gian ra không được nhỏ hơn thời gian vào!");
            }

            boolean laVeThang = checkValidMonthlyPass(veXe);
            boolean daCoc = veXe.getDatChoTruoc() != null;

            // Tính phí ở backend - KHÔNG nhận số tiền từ frontend
            FeeCalculationResult feeResult = feeCalculationService.calculate(veXe.getLoaiXe(), veXe.getThoiGianVao(), thoiGianRa, laVeThang, daCoc);
            double fee = feeResult.getSoTienThanhToan();

            veXe.setThoiGianRa(thoiGianRa);
            veXe.setSoTienPhaiThu(feeResult.getTongTienGoc());
            veXe.setChiPhi(fee);
            if (anhRaData != null && !anhRaData.isEmpty()) {
                veXe.setAnhRaUrl(anhRaData);
            }
            if (ghiChu != null && !ghiChu.isEmpty()) {
                veXe.setGhiChu(veXe.getGhiChu() != null ? veXe.getGhiChu() + " | " + ghiChu : ghiChu);
            }

            // Xử lý phương thức thanh toán
            if (laVeThang || fee == 0) {
                veXe.setHinhThucThanhToan(laVeThang ? "VE_THANG" : (hinhThucThanhToan != null ? hinhThucThanhToan : "TIEN_MAT"));
                veXe.setTrangThaiThanhToan("MIEN_PHI");
                veXe.setTrangThai("Đã ra");
                veXe.setSoTienThucThu(0.0);
                veXe.setThoiGianThanhToan(thoiGianRa);
                if (nguoiXacNhan != null && !nguoiXacNhan.isBlank()) {
                    veXe.setNguoiXacNhan(nguoiXacNhan);
                }
            } else if ("CHUYEN_KHOAN".equalsIgnoreCase(hinhThucThanhToan)) {
                // Chuyển khoản / VietQR: Chưa có xác nhận ngân hàng -> Giữ trạng thái CHO_THANH_TOAN và Đang gửi
                veXe.setHinhThucThanhToan("CHUYEN_KHOAN");
                veXe.setTrangThaiThanhToan("CHO_THANH_TOAN");
                veXe.setMaGiaoDich("QR-" + System.currentTimeMillis());
                // Không đóng vé (giữ trạng thái Đang gửi) cho đến khi nhân viên xác nhận đã thu tiền
            } else {
                // Tiền mặt: Nhân viên xác nhận đã thu tiền
                veXe.setHinhThucThanhToan("TIEN_MAT");
                veXe.setTrangThaiThanhToan("DA_THANH_TOAN");
                veXe.setTrangThai("Đã ra");
                veXe.setSoTienThucThu(fee);
                veXe.setThoiGianThanhToan(thoiGianRa);
                if (nguoiXacNhan != null && !nguoiXacNhan.isBlank()) {
                    veXe.setNguoiXacNhan(nguoiXacNhan);
                }
            }

            return veXeRepository.save(veXe);
        }
    }

    /**
     * Overload confirmCheckOut cho tương thích giao diện cũ
     */
    @Transactional(rollbackFor = Exception.class)
    public VeXe confirmCheckOut(Integer veXeId, String hinhThucThanhToan, String anhRaData, String ghiChu) {
        return confirmCheckOut(veXeId, hinhThucThanhToan, anhRaData, ghiChu, null, null);
    }

    /**
     * Nhân viên/Quản trị viên xác nhận đã nhận được tiền chuyển khoản ngân hàng
     */
    @Transactional(rollbackFor = Exception.class)
    public VeXe xacNhanThanhToanChuyenKhoan(Integer veXeId, String maGiaoDich, String nguoiXacNhan) {
        if (veXeId == null) {
            throw new BadRequestException("ID vé xe không được để trống!");
        }

        synchronized (("checkout-ticket-" + veXeId).intern()) {
            VeXe veXe = veXeRepository.findById(veXeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với ID: " + veXeId));

            if ("Đã ra".equals(veXe.getTrangThai())) {
                throw new InvalidTicketStateException("Vé xe [" + veXe.getMaQR() + "] đã hoàn tất xuất bãi trước đó!");
            }

            LocalDateTime now = LocalDateTime.now();
            veXe.setTrangThaiThanhToan("DA_THANH_TOAN");
            veXe.setTrangThai("Đã ra");
            veXe.setThoiGianRa(now);
            veXe.setThoiGianThanhToan(now);
            veXe.setSoTienThucThu(veXe.getChiPhi() != null ? veXe.getChiPhi() : 0.0);
            if (maGiaoDich != null && !maGiaoDich.isBlank()) {
                veXe.setMaGiaoDich(maGiaoDich);
            }
            if (nguoiXacNhan != null && !nguoiXacNhan.isBlank()) {
                veXe.setNguoiXacNhan(nguoiXacNhan);
            }

            return veXeRepository.save(veXe);
        }
    }

    /**
     * Kiểm tra vé tháng hợp lệ theo quy tắc thống nhất: Còn hạn, trạng thái HOAT_DONG, đúng loại xe
     */
    private boolean checkValidMonthlyPass(VeXe veXe) {
        if (veXe == null) return false;
        if (veXe.getVeThang() != null) {
            VeThang vt = veXe.getVeThang();
            if (vt.isConHan() && "HOAT_DONG".equalsIgnoreCase(vt.getTrangThai())) {
                if (veXe.getLoaiXe() == null || vt.getLoaiXe() == null || vt.getLoaiXe().getId().equals(veXe.getLoaiXe().getId())) {
                    return true;
                }
            }
        }
        Long loaiXeId = veXe.getLoaiXe() != null ? veXe.getLoaiXe().getId() : null;
        Optional<VeThang> passOpt = monthlyTicketService.findActiveMonthlyPass(veXe.getBienSoXe(), loaiXeId);
        if (passOpt.isPresent()) {
            veXe.setVeThang(passOpt.get());
            return true;
        }
        return false;
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
        return confirmCheckOut(veXeId, hinhThucThanhToan, anhRaData, ghiChu, null, null);
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

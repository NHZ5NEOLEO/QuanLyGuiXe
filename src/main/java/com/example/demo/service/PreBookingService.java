package com.example.demo.service;

import com.example.demo.model.DatChoTruoc;
import com.example.demo.model.KhuVuc;
import com.example.demo.repository.DatChoTruocRepository;
import com.example.demo.repository.KhuVucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("datChoTruocService")
public class PreBookingService implements DatChoTruocService {

    private final DatChoTruocRepository datChoTruocRepository;
    private final VietQrService vietQrService;
    private final KhuVucRepository khuVucRepository;

    @Autowired
    public PreBookingService(
            DatChoTruocRepository datChoTruocRepository,
            VietQrService vietQrService,
            @Autowired(required = false) KhuVucRepository khuVucRepository
    ) {
        this.datChoTruocRepository = datChoTruocRepository;
        this.vietQrService = vietQrService;
        this.khuVucRepository = khuVucRepository;
    }

    public PreBookingService(
            DatChoTruocRepository datChoTruocRepository,
            VietQrService vietQrService
    ) {
        this(datChoTruocRepository, vietQrService, null);
    }

    @Override
    public List<DatChoTruoc> layDanhSachDatChoHieuLuc() {
        quetVaCapNhatHetHan();
        return datChoTruocRepository.findByTrangThai("CHO_DEN");
    }

    public List<DatChoTruoc> getActiveBookings() {
        return layDanhSachDatChoHieuLuc();
    }

    @Override
    public List<DatChoTruoc> layTatCaDatCho() {
        quetVaCapNhatHetHan();
        return datChoTruocRepository.findAllByOrderByThoiGianDatDesc();
    }

    @Override
    @Transactional
    public DatChoTruoc taoDatCho(String hoTen, String sdt, String bienSoXe, String maViTri) {
        return taoDatCho(hoTen, sdt, bienSoXe, maViTri, 20000.0, 60);
    }

    @Override
    @Transactional
    public DatChoTruoc taoDatCho(String hoTen, String sdt, String bienSoXe, String maKhuVuc, Double tienCoc, Integer soPhutGiu) {
        String cleanPlate = bienSoXe != null ? bienSoXe.trim().toUpperCase() : "";
        String cleanHoTen = hoTen != null ? hoTen.trim() : "Khách hàng";
        String cleanPhone = sdt != null ? sdt.trim() : "";

        // Kiểm tra xem phương tiện đã có đơn đặt chỗ nào đang trong trạng thái CHO_DEN còn hạn không
        Optional<DatChoTruoc> existingBooking = datChoTruocRepository.findFirstByBienSoXeAndTrangThaiAndThoiGianHetHanAfterOrderByThoiGianDatDesc(
                cleanPlate, "CHO_DEN", LocalDateTime.now()
        );
        if (existingBooking.isPresent()) {
            return existingBooking.get();
        }

        String maDatCho = "BK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        int minutes = (soPhutGiu != null && soPhutGiu > 0) ? soPhutGiu : 60;
        LocalDateTime expiresAt = now.plusMinutes(minutes);
        double deposit = (tienCoc != null && tienCoc >= 0) ? tienCoc : 20000.0;

        DatChoTruoc booking = new DatChoTruoc(
                maDatCho,
                cleanHoTen,
                cleanPhone,
                cleanPlate,
                now,
                expiresAt,
                deposit,
                "CHO_DEN"
        );

        if (maKhuVuc != null && !maKhuVuc.isBlank()) {
            booking.setMaKhuVuc(maKhuVuc.trim());
            if (khuVucRepository != null) {
                Optional<KhuVuc> kvOpt = khuVucRepository.findByMaKhu(maKhuVuc.trim());
                kvOpt.ifPresent(booking::setKhuVuc);
            }
        }

        return datChoTruocRepository.save(booking);
    }

    @Transactional
    public DatChoTruoc createBooking(String hoTen, String sdt, String bienSoXe, String maViTri) {
        return taoDatCho(hoTen, sdt, bienSoXe, maViTri);
    }

    @Override
    public String taoQrDatCoc(DatChoTruoc booking) {
        if (booking == null) return "";
        return vietQrService.generateDepositQrUrl(
                booking.getTienCoc() != null ? booking.getTienCoc() : 20000.0,
                booking.getBienSoXe(),
                booking.getMaDatCho()
        );
    }

    public String generateDepositQr(DatChoTruoc booking) {
        return taoQrDatCoc(booking);
    }

    @Override
    public Optional<DatChoTruoc> timTheoMaDatCho(String maDatCho) {
        if (maDatCho == null || maDatCho.isBlank()) return Optional.empty();
        return datChoTruocRepository.findByMaDatCho(maDatCho.trim().toUpperCase());
    }

    @Override
    public List<DatChoTruoc> timTheoBienSo(String bienSoXe) {
        if (bienSoXe == null || bienSoXe.isBlank()) return List.of();
        return datChoTruocRepository.findByBienSoXeOrderByThoiGianDatDesc(bienSoXe.trim().toUpperCase());
    }

    @Override
    public List<DatChoTruoc> timKiem(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return layTatCaDatCho();
        }
        String q = keyword.trim();
        return datChoTruocRepository.findByBienSoXeContainingIgnoreCaseOrHoTenContainingIgnoreCaseOrSoDienThoaiContainingOrderByThoiGianDatDesc(
                q, q, q
        );
    }

    @Override
    @Transactional
    public boolean huyDatCho(Long id) {
        if (id == null) return false;
        Optional<DatChoTruoc> opt = datChoTruocRepository.findById(id);
        if (opt.isPresent()) {
            DatChoTruoc bk = opt.get();
            if ("CHO_DEN".equals(bk.getTrangThai())) {
                bk.setTrangThai("HUY");
                datChoTruocRepository.save(bk);
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean xacNhanThanhToanCoc(Long id, String maGiaoDich) {
        if (id == null) return false;
        Optional<DatChoTruoc> opt = datChoTruocRepository.findById(id);
        if (opt.isPresent()) {
            DatChoTruoc bk = opt.get();
            bk.setDaThanhToanCoc(true);
            if (maGiaoDich != null && !maGiaoDich.isBlank()) {
                bk.setMaGiaoDichCoc(maGiaoDich.trim());
            } else {
                bk.setMaGiaoDichCoc("MOMOBK-" + System.currentTimeMillis());
            }
            datChoTruocRepository.save(bk);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean xoaDatCho(Long id) {
        if (id == null) return false;
        if (datChoTruocRepository.existsById(id)) {
            datChoTruocRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void quetVaCapNhatHetHan() {
        LocalDateTime now = LocalDateTime.now();
        List<DatChoTruoc> expired = datChoTruocRepository.findByTrangThaiAndThoiGianHetHanBefore("CHO_DEN", now);
        for (DatChoTruoc bk : expired) {
            bk.setTrangThai("HET_HAN");
            datChoTruocRepository.save(bk);
        }
    }
}

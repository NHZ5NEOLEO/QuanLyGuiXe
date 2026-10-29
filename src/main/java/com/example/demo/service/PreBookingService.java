package com.example.demo.service;

import com.example.demo.model.DatChoTruoc;
import com.example.demo.repository.DatChoTruocRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service("datChoTruocService")
public class PreBookingService implements DatChoTruocService {

    private final DatChoTruocRepository datChoTruocRepository;
    private final VietQrService vietQrService;

    public PreBookingService(
            DatChoTruocRepository datChoTruocRepository,
            VietQrService vietQrService
    ) {
        this.datChoTruocRepository = datChoTruocRepository;
        this.vietQrService = vietQrService;
    }

    @Override
    public List<DatChoTruoc> layDanhSachDatChoHieuLuc() {
        return datChoTruocRepository.findByTrangThai("CHO_DEN");
    }

    public List<DatChoTruoc> getActiveBookings() {
        return layDanhSachDatChoHieuLuc();
    }

    @Override
    @Transactional
    public DatChoTruoc taoDatCho(String hoTen, String sdt, String bienSoXe, String maViTri) {
        String cleanPlate = bienSoXe.trim().toUpperCase();

        String maDatCho = "BK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(60); // Giữ chỗ 60 phút

        DatChoTruoc booking = new DatChoTruoc(
                maDatCho,
                hoTen.trim(),
                sdt.trim(),
                cleanPlate,
                now,
                expiresAt,
                20000.0, // Tiền đặt cọc giữ chỗ 20k
                "CHO_DEN"
        );
        if (maViTri != null && !maViTri.isEmpty()) {
            booking.setMaKhuVuc(maViTri);
        }

        return datChoTruocRepository.save(booking);
    }

    @Transactional
    public DatChoTruoc createBooking(String hoTen, String sdt, String bienSoXe, String maViTri) {
        return taoDatCho(hoTen, sdt, bienSoXe, maViTri);
    }

    @Override
    public String taoQrDatCoc(DatChoTruoc booking) {
        return vietQrService.generateQrUrl(booking.getTienCoc(), booking.getBienSoXe(), booking.getMaDatCho());
    }

    public String generateDepositQr(DatChoTruoc booking) {
        return taoQrDatCoc(booking);
    }
}

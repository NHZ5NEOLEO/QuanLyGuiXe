package com.example.demo.service;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.dto.CheckOutPreviewResponse;
import com.example.demo.dto.PhanHoiVeXeDto;
import com.example.demo.dto.TicketResponseDto;
import com.example.demo.dto.ThongTinXuatBai;
import com.example.demo.dto.YeuCauVaoBai;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.VeXe;

import java.time.LocalDateTime;

public interface XuLyGuiXeService {

    VeXe ghiNhanXeVao(YeuCauVaoBai yeuCau);
    default VeXe checkIn(CheckInRequest request) { return ghiNhanXeVao(request); }

    ThongTinXuatBai xemTruocXuatBai(String maHoacBienSo);
    default CheckOutPreviewResponse previewCheckOut(String identifier) {
        ThongTinXuatBai res = xemTruocXuatBai(identifier);
        if (res instanceof CheckOutPreviewResponse) {
            return (CheckOutPreviewResponse) res;
        }
        CheckOutPreviewResponse dto = new CheckOutPreviewResponse();
        dto.setVeXeId(res.getVeXeId());
        dto.setMaQR(res.getMaQR());
        dto.setBienSoXe(res.getBienSoXe());
        dto.setTenLoaiXe(res.getTenLoaiXe());
        dto.setMaViTri(res.getMaViTri());
        dto.setThoiGianVao(res.getThoiGianVao());
        dto.setThoiGianRa(res.getThoiGianRa());
        dto.setThoiLuong(res.getThoiLuong());
        dto.setLaVeThang(res.isLaVeThang());
        dto.setTenKhachHang(res.getTenKhachHang());
        dto.setSoTien(res.getSoTien());
        dto.setVietQrUrl(res.getVietQrUrl());
        dto.setAnhVaoUrl(res.getAnhVaoUrl());
        return dto;
    }

    VeXe xacNhanXuatBai(Integer veXeId, String hinhThucThanhToan, String anhRaData, String ghiChu);
    default VeXe confirmCheckOut(Integer veXeId, String hinhThucThanhToan, String anhRaData, String ghiChu) {
        return xacNhanXuatBai(veXeId, hinhThucThanhToan, anhRaData, ghiChu);
    }

    VeXe timVeTheoId(Integer id);
    default VeXe getTicketById(Integer id) { return timVeTheoId(id); }

    VeXe timVeTheoMa(String code);
    default VeXe getTicketByCode(String code) { return timVeTheoMa(code); }

    PhanHoiVeXeDto chuyenSangPhanHoiDto(VeXe veXe);
    default TicketResponseDto mapToTicketResponseDto(VeXe veXe) {
        PhanHoiVeXeDto res = chuyenSangPhanHoiDto(veXe);
        if (res instanceof TicketResponseDto) {
            return (TicketResponseDto) res;
        }
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(res.getId());
        dto.setTicketCode(res.getMaQR());
        dto.setQrCodeBase64(res.getQrCodeBase64());
        dto.setBienSoXe(res.getBienSoXe());
        dto.setLoaiXeId(res.getLoaiXeId());
        dto.setTenLoaiXe(res.getTenLoaiXe());
        dto.setMaViTri(res.getMaViTri());
        dto.setThoiGianVao(res.getThoiGianVao());
        dto.setThoiGianRa(res.getThoiGianRa());
        dto.setChiPhi(res.getChiPhi());
        dto.setHinhThucThanhToan(res.getHinhThucThanhToan());
        dto.setTrangThai(res.getTrangThai());
        dto.setLaVeThang(res.isLaVeThang());
        dto.setTenKhachHang(res.getTenKhachHang());
        dto.setGhiChu(res.getGhiChu());
        dto.setAnhVaoUrl(res.getAnhVaoUrl());
        dto.setAnhRaUrl(res.getAnhRaUrl());
        return dto;
    }

    double tinhPhi(LoaiXe loaiXe, LocalDateTime vao, LocalDateTime ra, long tongPhut);
    default double calculateFee(LoaiXe loaiXe, LocalDateTime vao, LocalDateTime ra, long totalMinutes) {
        return tinhPhi(loaiXe, vao, ra, totalMinutes);
    }
}

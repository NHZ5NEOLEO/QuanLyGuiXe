package com.example.demo.service;

import com.example.demo.dto.LuuLuongXeDto;
import com.example.demo.dto.ThongKeTongQuanDto;
import com.example.demo.dto.TrangThaiKhuVucDto;
import com.example.demo.model.VeXe;

import java.util.List;
import java.util.Map;

/**
 * Service báo cáo và phân tích dữ liệu thống kê bãi đỗ xe
 */
public interface BaoCaoService {

    ThongKeTongQuanDto layThongKeTongQuan();

    ThongKeTongQuanDto layThongKeTongQuan(String chuKy);

    List<VeXe> layHoatDongGanDay(int gioiHan);

    Map<String, Double> layDoanhThu7NgayGanNhat();

    Map<String, Long> layCoCauLoaiXe();

    Map<String, Double> layCoCauThanhToan();

    long demXeQuaHan();

    Map<String, Long> layLuuLuongTheoGio();

    Map<String, Double> layDoanhThuTheoLoaiXe();

    LuuLuongXeDto layHoatDongGiaoThong7Ngay();

    List<TrangThaiKhuVucDto> layTrangThaiKhuVuc();

    List<VeXe> layDanhSachXeQuaHan(int gioiHan);
}

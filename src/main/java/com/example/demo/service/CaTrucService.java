package com.example.demo.service;

import com.example.demo.model.CaTruc;

import java.util.List;

/**
 * Service quản lý ca trực và giao ban nhân viên bãi xe
 */
public interface CaTrucService {

    CaTruc layCaTrucHienTai();

    void capNhatThongKeCaTruc(CaTruc caTruc);

    CaTruc banGiaoCaTruc(Long caTrucId, String ghiChu);

    List<CaTruc> layCaTrucGanDay();
}

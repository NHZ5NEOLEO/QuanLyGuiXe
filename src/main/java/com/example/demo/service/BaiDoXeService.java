package com.example.demo.service;

import com.example.demo.model.KhuVuc;
import com.example.demo.model.LoaiXe;
import com.example.demo.model.ViTriDo;

import java.util.List;
import java.util.Optional;

public interface BaiDoXeService {

    List<KhuVuc> layTatCaKhuVuc();
    default List<KhuVuc> getAllKhuVuc() { return layTatCaKhuVuc(); }

    List<ViTriDo> layTatCaViTriDo();
    default List<ViTriDo> getAllViTriDo() { return layTatCaViTriDo(); }

    List<ViTriDo> layViTriDoTheoKhuVuc(Long khuVucId);
    default List<ViTriDo> getViTriDoByKhuVuc(Long khuVucId) { return layViTriDoTheoKhuVuc(khuVucId); }

    List<LoaiXe> layTatCaLoaiXe();
    default List<LoaiXe> getAllLoaiXe() { return layTatCaLoaiXe(); }

    List<ViTriDo> layViTriTrong();
    default List<ViTriDo> getAvailableSlots() { return layViTriTrong(); }

    Optional<ViTriDo> timViTriTrongChoXe(LoaiXe loaiXe);
    default Optional<ViTriDo> findAvailableSlotForVehicle(LoaiXe loaiXe) { return timViTriTrongChoXe(loaiXe); }

    void chiemViTri(ViTriDo viTri, String bienSoXe);
    default void occupySlot(ViTriDo slot, String bienSoXe) { chiemViTri(slot, bienSoXe); }

    void giaiPhongViTri(ViTriDo viTri);
    default void releaseSlot(ViTriDo slot) { giaiPhongViTri(slot); }

    void giaiPhongViTriTheoBienSo(String bienSoXe);
    default void releaseSlotByBienSo(String bienSoXe) { giaiPhongViTriTheoBienSo(bienSoXe); }

    long demTongViTri();
    default long getTotalSlots() { return demTongViTri(); }

    long demViTriTrong();
    default long getAvailableSlotsCount() { return demViTriTrong(); }

    long demViTriDangDo();
    default long getOccupiedSlotsCount() { return demViTriDangDo(); }

    long demViTriDaDat();
    default long getBookedSlotsCount() { return demViTriDaDat(); }
}

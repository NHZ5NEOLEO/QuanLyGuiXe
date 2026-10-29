package com.example.demo.repository;

import com.example.demo.model.VeXe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VeXeRepository extends JpaRepository<VeXe, Integer> {

    Optional<VeXe> findByMaQR(String maQR);

    Optional<VeXe> findFirstByBienSoXeAndTrangThaiOrderByThoiGianVaoDesc(String bienSoXe, String trangThai);

    List<VeXe> findByTrangThaiOrderByThoiGianVaoDesc(String trangThai);

    List<VeXe> findByBienSoXeOrderByThoiGianVaoDesc(String bienSoXe);

    List<VeXe> findTop10ByOrderByThoiGianVaoDesc();

    long countByTrangThai(String trangThai);

    @Query("SELECT COUNT(v) FROM VeXe v WHERE v.thoiGianRa IS NULL OR v.trangThai LIKE '%gửi%' OR v.trangThai LIKE '%gui%'")
    long countActiveVehicles();

    @Query("SELECT v FROM VeXe v WHERE (v.thoiGianRa IS NULL OR v.trangThai LIKE '%gửi%') AND (LOWER(v.bienSoXe) = LOWER(:plate) OR LOWER(v.bienSoXe) = LOWER(:cleanKey))")
    List<VeXe> findActiveByPlateList(@Param("plate") String plate, @Param("cleanKey") String cleanKey);

    long countByThoiGianVaoBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(v) FROM VeXe v WHERE v.thoiGianVao BETWEEN :start AND :end AND LOWER(v.loaiXe.tenLoai) LIKE '%ô tô%'")
    long countTodayCars(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(v) FROM VeXe v WHERE v.thoiGianVao BETWEEN :start AND :end AND (LOWER(v.loaiXe.tenLoai) LIKE '%máy%' OR LOWER(v.loaiXe.tenLoai) LIKE '%may%')")
    long countTodayMotorbikes(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<VeXe> findByThoiGianVaoBetween(LocalDateTime start, LocalDateTime end);

    long countByThoiGianRaBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(v) FROM VeXe v WHERE v.thoiGianRa BETWEEN :start AND :end AND LOWER(v.loaiXe.tenLoai) LIKE '%ô tô%'")
    long countTodayCarsOut(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(v) FROM VeXe v WHERE v.thoiGianRa BETWEEN :start AND :end AND (LOWER(v.loaiXe.tenLoai) LIKE '%máy%' OR LOWER(v.loaiXe.tenLoai) LIKE '%may%')")
    long countTodayMotorbikesOut(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(v.chiPhi), 0.0) FROM VeXe v WHERE v.thoiGianRa BETWEEN :start AND :end AND v.trangThai = :status")
    Double calculateRevenueBetweenAndStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("status") String status);

    default Double calculateRevenueBetween(LocalDateTime start, LocalDateTime end) {
        return calculateRevenueBetweenAndStatus(start, end, "Đã ra");
    }

    @Query("SELECT v FROM VeXe v WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(v.bienSoXe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(v.maQR) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:trangThai IS NULL OR :trangThai = '' OR v.trangThai = :trangThai) AND " +
           "(:loaiXeId IS NULL OR (v.loaiXe IS NOT NULL AND v.loaiXe.id = :loaiXeId)) AND " +
           "(:startDate IS NULL OR v.thoiGianVao >= :startDate) AND " +
           "(:endDate IS NULL OR v.thoiGianVao <= :endDate) " +
           "ORDER BY v.thoiGianVao DESC")
    List<VeXe> searchHistory(
            @Param("keyword") String keyword,
            @Param("trangThai") String trangThai,
            @Param("loaiXeId") Long loaiXeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}

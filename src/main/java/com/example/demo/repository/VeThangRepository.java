package com.example.demo.repository;

import com.example.demo.model.VeThang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VeThangRepository extends JpaRepository<VeThang, Long> {
    Optional<VeThang> findByMaThe(String maThe);

    Optional<VeThang> findFirstByBienSoXe(String bienSoXe);

    Optional<VeThang> findFirstByBienSoXeAndTrangThai(String bienSoXe, String trangThai);

    List<VeThang> findByBienSoXeAndTrangThai(String bienSoXe, String trangThai);

    List<VeThang> findByTrangThai(String trangThai);

    long countByTrangThai(String trangThai);

    List<VeThang> findByKhachHangIdOrderByNgayBatDauDesc(Long khachHangId);

    List<VeThang> findByKhachHangSoDienThoaiOrderByNgayBatDauDesc(String soDienThoai);

    @Query("SELECT COUNT(v) FROM VeThang v WHERE LOWER(v.bienSoXe) = LOWER(:bienSoXe) AND v.trangThai = 'HOAT_DONG' AND v.ngayHetHan >= :currentDate")
    long countActiveByBienSoXe(@Param("bienSoXe") String bienSoXe, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT COALESCE(SUM(v.giaTien), 0.0) FROM VeThang v WHERE v.ngayBatDau BETWEEN :startDate AND :endDate")
    Double calculateRevenueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT v FROM VeThang v WHERE " +
           "LOWER(v.bienSoXe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.maThe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "v.khachHang.soDienThoai LIKE CONCAT('%', :keyword, '%')")
    List<VeThang> searchByKeyword(@Param("keyword") String keyword);

    List<VeThang> findByNgayHetHanBefore(LocalDate date);
}

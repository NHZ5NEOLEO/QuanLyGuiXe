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

    List<VeThang> findByTrangThai(String trangThai);

    long countByTrangThai(String trangThai);

    @Query("SELECT v FROM VeThang v WHERE " +
           "LOWER(v.bienSoXe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.maThe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "v.khachHang.soDienThoai LIKE CONCAT('%', :keyword, '%')")
    List<VeThang> searchByKeyword(@Param("keyword") String keyword);

    List<VeThang> findByNgayHetHanBefore(LocalDate date);
}

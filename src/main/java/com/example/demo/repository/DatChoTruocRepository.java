package com.example.demo.repository;

import com.example.demo.model.DatChoTruoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DatChoTruocRepository extends JpaRepository<DatChoTruoc, Long> {
    Optional<DatChoTruoc> findByMaDatCho(String maDatCho);

    List<DatChoTruoc> findByTrangThai(String trangThai);

    Optional<DatChoTruoc> findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(String bienSoXe, String trangThai);

    Optional<DatChoTruoc> findFirstByBienSoXeAndTrangThaiAndThoiGianHetHanAfterOrderByThoiGianDatDesc(String bienSoXe, String trangThai, LocalDateTime now);

    List<DatChoTruoc> findByTrangThaiAndThoiGianHetHanBefore(String trangThai, LocalDateTime now);

    @Query("SELECT COALESCE(SUM(d.tienCoc), 0.0) FROM DatChoTruoc d WHERE d.thoiGianDat BETWEEN :start AND :end AND d.daThanhToanCoc = true AND d.trangThai IN ('DA_VAO', 'CHO_DEN')")
    Double calculateDepositRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

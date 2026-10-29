package com.example.demo.repository;

import com.example.demo.model.DatChoTruoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DatChoTruocRepository extends JpaRepository<DatChoTruoc, Long> {
    Optional<DatChoTruoc> findByMaDatCho(String maDatCho);
    List<DatChoTruoc> findByTrangThai(String trangThai);
    Optional<DatChoTruoc> findFirstByBienSoXeAndTrangThaiOrderByThoiGianDatDesc(String bienSoXe, String trangThai);
}

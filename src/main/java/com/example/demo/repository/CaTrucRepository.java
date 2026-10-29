package com.example.demo.repository;

import com.example.demo.model.CaTruc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CaTrucRepository extends JpaRepository<CaTruc, Long> {
    Optional<CaTruc> findFirstByTrangThaiOrderByThoiGianBatDauDesc(String trangThai);
    List<CaTruc> findTop10ByOrderByThoiGianBatDauDesc();
}

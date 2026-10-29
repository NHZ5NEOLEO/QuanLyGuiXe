package com.example.demo.repository;

import com.example.demo.model.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DanhGiaRepository extends JpaRepository<DanhGia, Long> {
    List<DanhGia> findTop10ByOrderByThoiGianDesc();
}

package com.example.demo.repository;

import com.example.demo.model.LichSuThayDoiGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LichSuThayDoiGiaRepository extends JpaRepository<LichSuThayDoiGia, Long> {
    List<LichSuThayDoiGia> findAllByOrderByThoiGianThayDoiDesc();
}

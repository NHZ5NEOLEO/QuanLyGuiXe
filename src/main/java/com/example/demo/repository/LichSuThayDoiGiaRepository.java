package com.example.demo.repository;

import com.example.demo.model.LichSuThayDoiGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuThayDoiGiaRepository extends JpaRepository<LichSuThayDoiGia, Long> {
    List<LichSuThayDoiGia> findAllByOrderByThoiGianThayDoiDesc();
}

package com.example.demo.repository;

import com.example.demo.model.LoaiXe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoaiXeRepository extends JpaRepository<LoaiXe, Long> {
    Optional<LoaiXe> findByTenLoai(String tenLoai);
}

package com.example.demo.repository;

import com.example.demo.model.DanhSachDen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DanhSachDenRepository extends JpaRepository<DanhSachDen, Long> {
    Optional<DanhSachDen> findByBienSoXe(String bienSoXe);
    boolean existsByBienSoXe(String bienSoXe);
}

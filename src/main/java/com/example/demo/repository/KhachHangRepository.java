package com.example.demo.repository;

import com.example.demo.model.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Long> {
    Optional<KhachHang> findBySoDienThoai(String soDienThoai);

    Optional<KhachHang> findFirstByEmailIgnoreCase(String email);

    List<KhachHang> findByHoTenContainingIgnoreCaseOrSoDienThoaiContaining(String hoTen, String soDienThoai);
}

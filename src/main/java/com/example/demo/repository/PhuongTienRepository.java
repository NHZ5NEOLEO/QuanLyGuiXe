package com.example.demo.repository;

import com.example.demo.model.PhuongTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PhuongTienRepository extends JpaRepository<PhuongTien, Long> {

    Optional<PhuongTien> findByBienSoXe(String bienSoXe);

    Optional<PhuongTien> findByMaPhuongTien(String maPhuongTien);

    boolean existsByBienSoXe(String bienSoXe);

    boolean existsByMaPhuongTien(String maPhuongTien);

    @Query("SELECT p FROM PhuongTien p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(p.bienSoXe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.chuXe) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.maPhuongTien) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.ngayTao DESC")
    List<PhuongTien> search(@Param("keyword") String keyword);

    List<PhuongTien> findByLoaiXeIdOrderByNgayTaoDesc(Long loaiXeId);
}

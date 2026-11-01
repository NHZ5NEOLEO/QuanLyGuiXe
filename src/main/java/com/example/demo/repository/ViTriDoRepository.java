package com.example.demo.repository;

import com.example.demo.model.ViTriDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ViTriDoRepository extends JpaRepository<ViTriDo, Long> {

    Optional<ViTriDo> findByMaViTri(String maViTri);

    Optional<ViTriDo> findFirstByBienSoXeDangDo(String bienSoXe);

    List<ViTriDo> findByKhuVucId(Long khuVucId);

    List<ViTriDo> findByTrangThai(String trangThai);

    List<ViTriDo> findByKhuVucIdAndTrangThai(Long khuVucId, String trangThai);

    long countByKhuVucIdAndTrangThai(Long khuVucId, String trangThai);

    @Query("SELECT v FROM ViTriDo v WHERE v.khuVuc.id = :khuVucId AND v.trangThai = 'TRONG' ORDER BY v.id ASC")
    List<ViTriDo> findAvailableSlotsInZone(@Param("khuVucId") Long khuVucId);
}


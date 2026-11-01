package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity vị trí ô đỗ thực tế trong các phân khu của bãi xe
 */
@Entity
@Table(name = "ViTriDo", uniqueConstraints = {
    @UniqueConstraint(name = "uk_vitrido_mavitri", columnNames = {"ma_vi_tri"})
})
public class ViTriDo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_vi_tri", nullable = false, length = 30)
    private String maViTri; // 2B-01, 4B-05, XD-12

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "khu_vuc_id")
    private KhuVuc khuVuc;

    @Column(name = "trang_thai", length = 30)
    private String trangThai = "TRONG"; // TRONG, DA_DO, BAO_TRI

    @Column(name = "bien_so_xe_dang_do", length = 30)
    private String bienSoXeDangDo;

    @Column(name = "thoi_gian_vao_do")
    private LocalDateTime thoiGianVaoDo;

    @Version
    private Long version;

    public ViTriDo() {
    }

    public ViTriDo(String maViTri, KhuVuc khuVuc, String trangThai) {
        this.maViTri = maViTri;
        this.khuVuc = khuVuc;
        this.trangThai = trangThai != null ? trangThai : "TRONG";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaViTri() {
        return maViTri;
    }

    public void setMaViTri(String maViTri) {
        this.maViTri = maViTri;
    }

    public KhuVuc getKhuVuc() {
        return khuVuc;
    }

    public void setKhuVuc(KhuVuc khuVuc) {
        this.khuVuc = khuVuc;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getBienSoXeDangDo() {
        return bienSoXeDangDo;
    }

    public void setBienSoXeDangDo(String bienSoXeDangDo) {
        this.bienSoXeDangDo = bienSoXeDangDo;
    }

    public LocalDateTime getThoiGianVaoDo() {
        return thoiGianVaoDo;
    }

    public void setThoiGianVaoDo(LocalDateTime thoiGianVaoDo) {
        this.thoiGianVaoDo = thoiGianVaoDo;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

package com.example.demo.model;

import java.time.LocalDateTime;

/**
 * Model biểu diễn vị trí đỗ / ô đỗ phục vụ hiển thị sơ đồ (nếu cần),
 * không lưu bảng vật lý trong CSDL quản lý gửi xe thông thường.
 */
public class ViTriDo {

    private Long id;
    private String maViTri; // A-01, B-05, C-12
    private KhuVuc khuVuc;
    private String trangThai = "TRONG"; // TRONG, DA_DO, BAO_TRI
    private String bienSoXeDangDo;
    private LocalDateTime thoiGianVaoDo;

    public ViTriDo() {
    }

    public ViTriDo(String maViTri, KhuVuc khuVuc, String trangThai) {
        this.maViTri = maViTri;
        this.khuVuc = khuVuc;
        this.trangThai = trangThai;
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
}

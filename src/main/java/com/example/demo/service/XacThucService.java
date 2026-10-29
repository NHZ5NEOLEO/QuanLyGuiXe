package com.example.demo.service;

import com.example.demo.model.NguoiDung;

import java.util.List;
import java.util.Optional;

/**
 * Service xác thực đăng nhập và quản lý tài khoản người dùng
 */
public interface XacThucService {

    Optional<NguoiDung> xacThuc(String tenDangNhap, String matKhau);

    List<NguoiDung> layTatCaNguoiDung();
}

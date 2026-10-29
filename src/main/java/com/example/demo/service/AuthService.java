package com.example.demo.service;

import com.example.demo.model.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("xacThucService")
public class AuthService implements XacThucService {

    private final NguoiDungRepository nguoiDungRepository;

    public AuthService(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Override
    public Optional<NguoiDung> xacThuc(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || matKhau == null) return Optional.empty();
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByTenDangNhap(tenDangNhap.trim());
        if (userOpt.isPresent()) {
            NguoiDung user = userOpt.get();
            if (matKhau.trim().equals(user.getMatKhau()) && Boolean.TRUE.equals(user.getTrangThai())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<NguoiDung> authenticate(String username, String password) {
        return xacThuc(username, password);
    }

    @Override
    public List<NguoiDung> layTatCaNguoiDung() {
        return nguoiDungRepository.findAll();
    }

    public List<NguoiDung> getAllUsers() {
        return layTatCaNguoiDung();
    }
}

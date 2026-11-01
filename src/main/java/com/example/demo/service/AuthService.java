package com.example.demo.service;

import com.example.demo.model.NguoiDung;
import com.example.demo.repository.NguoiDungRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("xacThucService")
public class AuthService implements XacThucService {

    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(NguoiDungRepository nguoiDungRepository, PasswordEncoder passwordEncoder) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<NguoiDung> xacThuc(String tenDangNhap, String matKhau) {
        if (tenDangNhap == null || matKhau == null) return Optional.empty();
        Optional<NguoiDung> userOpt = nguoiDungRepository.findByTenDangNhap(tenDangNhap.trim().toLowerCase());
        if (userOpt.isPresent()) {
            NguoiDung user = userOpt.get();
            if (passwordEncoder.matches(matKhau.trim(), user.getMatKhau()) && Boolean.TRUE.equals(user.getTrangThai())) {
                if (passwordEncoder.upgradeEncoding(user.getMatKhau())) {
                    user.setMatKhau(passwordEncoder.encode(matKhau.trim()));
                    nguoiDungRepository.save(user);
                }
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

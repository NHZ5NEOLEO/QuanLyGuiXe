package com.example.demo.controller;

import com.example.demo.model.KhachHang;
import com.example.demo.model.NguoiDung;
import com.example.demo.model.PasswordResetOtp;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.PasswordResetOtpRepository;
import com.example.demo.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.*;

@Controller
public class ForgotPasswordController {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordController.class);

    private final NguoiDungRepository nguoiDungRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public ForgotPasswordController(
            NguoiDungRepository nguoiDungRepository,
            KhachHangRepository khachHangRepository,
            PasswordResetOtpRepository otpRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.khachHangRepository = khachHangRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping({"/quen-mat-khau", "/forgot-password"})
    public String forgotPasswordPage(
            @RequestParam(value = "step", required = false, defaultValue = "1") String step,
            @RequestParam(value = "email", required = false) String email,
            Model model
    ) {
        model.addAttribute("step", step);
        model.addAttribute("email", email);
        return "quen-mat-khau";
    }

    /**
     * API gửi mã xác thực OTP qua mailbox.org
     */
    @PostMapping("/api/auth/forgot-password/send-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendOtpApi(@RequestBody Map<String, String> body) {
        String input = body.get("emailOrUsername");
        Map<String, Object> res = new HashMap<>();

        if (input == null || input.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Vui lòng nhập địa chỉ email hoặc tên đăng nhập của bạn!");
            return ResponseEntity.badRequest().body(res);
        }

        input = input.trim();
        ResolvedUser resolved = resolveUserAndEmail(input);

        if (resolved == null || resolved.email == null || resolved.email.isBlank()) {
            res.put("success", false);
            res.put("message", "Không tìm thấy tài khoản hoặc email tương ứng trong hệ thống!");
            return ResponseEntity.badRequest().body(res);
        }

        String targetEmail = resolved.email.trim().toLowerCase();

        // Vô hiệu hóa các OTP cũ chưa dùng cho email này
        List<PasswordResetOtp> oldOtps = otpRepository.findByEmailAndUsedFalse(targetEmail);
        for (PasswordResetOtp o : oldOtps) {
            o.setUsed(true);
        }
        otpRepository.saveAll(oldOtps);

        // Sinh mã OTP 6 chữ số ngẫu nhiên
        String otp = String.format("%06d", secureRandom.nextInt(1000000));
        PasswordResetOtp newOtp = new PasswordResetOtp(targetEmail, otp, 15);
        otpRepository.save(newOtp);

        try {
            emailService.sendPasswordResetOtp(targetEmail, otp, resolved.displayName);
            res.put("success", true);
            res.put("message", "Mã xác thực đã được gửi đến email " + maskEmail(targetEmail) + ". Vui lòng kiểm tra hộp thư!");
            res.put("targetEmail", targetEmail);
            res.put("maskedEmail", maskEmail(targetEmail));
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email qua mailbox.org: {}", e.getMessage(), e);
            res.put("success", false);
            res.put("message", "Gửi email thất bại: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    /**
     * API xác thực OTP và đặt lại mật khẩu mới
     */
    @PostMapping("/api/auth/forgot-password/verify-reset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyAndResetPasswordApi(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        Map<String, Object> res = new HashMap<>();

        if (email == null || email.trim().isEmpty() || otp == null || otp.trim().isEmpty()) {
            res.put("success", false);
            res.put("message", "Vui lòng nhập đầy đủ email và mã OTP!");
            return ResponseEntity.badRequest().body(res);
        }

        email = email.trim().toLowerCase();
        otp = otp.trim();

        if (newPassword == null || newPassword.length() < 6) {
            res.put("success", false);
            res.put("message", "Mật khẩu mới phải có ít nhất 6 ký tự!");
            return ResponseEntity.badRequest().body(res);
        }

        if (!newPassword.equals(confirmPassword)) {
            res.put("success", false);
            res.put("message", "Mật khẩu xác nhận không trùng khớp!");
            return ResponseEntity.badRequest().body(res);
        }

        Optional<PasswordResetOtp> otpOpt = otpRepository.findTopByEmailAndOtpAndUsedFalseOrderByCreatedAtDesc(email, otp);
        if (otpOpt.isEmpty() || !otpOpt.get().isValid()) {
            res.put("success", false);
            res.put("message", "Mã OTP không hợp lệ hoặc đã hết hạn (15 phút)! Vui lòng yêu cầu mã mới.");
            return ResponseEntity.badRequest().body(res);
        }

        ResolvedUser resolved = resolveUserAndEmail(email);
        if (resolved == null || resolved.user == null) {
            res.put("success", false);
            res.put("message", "Không tìm thấy tài khoản người dùng tương ứng để cập nhật mật khẩu!");
            return ResponseEntity.badRequest().body(res);
        }

        // Cập nhật mật khẩu mới mã hóa BCrypt
        NguoiDung user = resolved.user;
        user.setMatKhau(passwordEncoder.encode(newPassword));
        nguoiDungRepository.save(user);

        // Đánh dấu OTP đã sử dụng
        PasswordResetOtp usedOtp = otpOpt.get();
        usedOtp.setUsed(true);
        otpRepository.save(usedOtp);

        res.put("success", true);
        res.put("message", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay bằng mật khẩu mới.");
        res.put("redirectUrl", "/login?reset=success");
        return ResponseEntity.ok(res);
    }

    /**
     * Tìm NguoiDung và Email tương ứng dựa theo chuỗi đầu vào (email, tên đăng nhập hoặc số điện thoại)
     */
    private ResolvedUser resolveUserAndEmail(String input) {
        if (input == null || input.isBlank()) return null;
        input = input.trim();

        // 1. Tìm NguoiDung theo email trực tiếp
        Optional<NguoiDung> uOpt = nguoiDungRepository.findFirstByEmailIgnoreCase(input);
        if (uOpt.isPresent()) {
            return new ResolvedUser(uOpt.get(), input, uOpt.get().getHoTen());
        }

        // 2. Tìm KhachHang theo email
        Optional<KhachHang> khOpt = khachHangRepository.findFirstByEmailIgnoreCase(input);
        if (khOpt.isPresent()) {
            KhachHang kh = khOpt.get();
            Optional<NguoiDung> uByPhone = nguoiDungRepository.findBySoDienThoai(kh.getSoDienThoai());
            if (uByPhone.isPresent()) {
                return new ResolvedUser(uByPhone.get(), kh.getEmail(), kh.getHoTen());
            }
        }

        // 3. Tìm NguoiDung theo tên đăng nhập
        Optional<NguoiDung> uByUsername = nguoiDungRepository.findByTenDangNhap(input.toLowerCase());
        if (uByUsername.isPresent()) {
            NguoiDung u = uByUsername.get();
            if (u.getEmail() != null && !u.getEmail().isBlank()) {
                return new ResolvedUser(u, u.getEmail(), u.getHoTen());
            }
            if (u.getSoDienThoai() != null && !u.getSoDienThoai().isBlank()) {
                Optional<KhachHang> khByPhone = khachHangRepository.findBySoDienThoai(u.getSoDienThoai());
                if (khByPhone.isPresent() && khByPhone.get().getEmail() != null && !khByPhone.get().getEmail().isBlank()) {
                    return new ResolvedUser(u, khByPhone.get().getEmail(), u.getHoTen());
                }
            }
        }

        // 4. Tìm NguoiDung theo số điện thoại
        Optional<NguoiDung> uByPhone = nguoiDungRepository.findBySoDienThoai(input);
        if (uByPhone.isPresent()) {
            NguoiDung u = uByPhone.get();
            if (u.getEmail() != null && !u.getEmail().isBlank()) {
                return new ResolvedUser(u, u.getEmail(), u.getHoTen());
            }
            Optional<KhachHang> khByPhone = khachHangRepository.findBySoDienThoai(input);
            if (khByPhone.isPresent() && khByPhone.get().getEmail() != null && !khByPhone.get().getEmail().isBlank()) {
                return new ResolvedUser(u, khByPhone.get().getEmail(), u.getHoTen());
            }
        }

        return null;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf('@');
        String name = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (name.length() <= 2) {
            return name.charAt(0) + "***" + domain;
        }
        return name.substring(0, 2) + "***" + name.charAt(name.length() - 1) + domain;
    }

    private static class ResolvedUser {
        final NguoiDung user;
        final String email;
        final String displayName;

        ResolvedUser(NguoiDung user, String email, String displayName) {
            this.user = user;
            this.email = email;
            this.displayName = displayName;
        }
    }
}


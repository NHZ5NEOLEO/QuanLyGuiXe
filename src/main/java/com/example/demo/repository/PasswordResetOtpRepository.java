package com.example.demo.repository;

import com.example.demo.model.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByEmailAndOtpAndUsedFalseOrderByCreatedAtDesc(String email, String otp);

    List<PasswordResetOtp> findByEmailAndUsedFalse(String email);
}


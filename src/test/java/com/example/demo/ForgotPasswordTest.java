package com.example.demo;

import com.example.demo.controller.ForgotPasswordController;
import com.example.demo.model.NguoiDung;
import com.example.demo.model.PasswordResetOtp;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NguoiDungRepository;
import com.example.demo.repository.PasswordResetOtpRepository;
import com.example.demo.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private KhachHangRepository khachHangRepository;

    @Mock
    private PasswordResetOtpRepository otpRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ForgotPasswordController controller = new ForgotPasswordController(
                nguoiDungRepository,
                khachHangRepository,
                otpRepository,
                passwordEncoder,
                emailService
        );

        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void testGetForgotPasswordPage() throws Exception {
        mockMvc.perform(get("/quen-mat-khau"))
                .andExpect(status().isOk())
                .andExpect(view().name("quen-mat-khau"));
    }

    @Test
    void testSendOtp_NotFound() throws Exception {
        when(nguoiDungRepository.findFirstByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(khachHangRepository.findFirstByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(nguoiDungRepository.findByTenDangNhap(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailOrUsername\":\"notfound@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testSendOtp_Success() throws Exception {
        NguoiDung user = new NguoiDung("testuser", "hashed", "Test User", "KHACH_HANG", "0912345678", "test@example.com");
        when(nguoiDungRepository.findFirstByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(otpRepository.findByEmailAndUsedFalse("test@example.com")).thenReturn(Collections.emptyList());
        when(otpRepository.save(any(PasswordResetOtp.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).sendPasswordResetOtp(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/auth/forgot-password/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailOrUsername\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(emailService, times(1)).sendPasswordResetOtp(eq("test@example.com"), anyString(), anyString());
    }

    @Test
    void testVerifyAndReset_WrongOtp() throws Exception {
        when(otpRepository.findTopByEmailAndOtpAndUsedFalseOrderByCreatedAtDesc(anyString(), anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/forgot-password/verify-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"otp\":\"123456\",\"newPassword\":\"newpass123\",\"confirmPassword\":\"newpass123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testVerifyAndReset_Success() throws Exception {
        PasswordResetOtp otp = new PasswordResetOtp("test@example.com", "123456", 15);
        when(otpRepository.findTopByEmailAndOtpAndUsedFalseOrderByCreatedAtDesc("test@example.com", "123456"))
                .thenReturn(Optional.of(otp));

        NguoiDung user = new NguoiDung("testuser", "oldhashed", "Test User", "KHACH_HANG", "0912345678", "test@example.com");
        when(nguoiDungRepository.findFirstByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass123")).thenReturn("newhashed");

        mockMvc.perform(post("/api/auth/forgot-password/verify-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"otp\":\"123456\",\"newPassword\":\"newpass123\",\"confirmPassword\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.redirectUrl").value("/login?reset=success"));

        verify(nguoiDungRepository, times(1)).save(user);
    }
}


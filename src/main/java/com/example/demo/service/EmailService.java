package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Gửi mã xác thực OTP đặt lại mật khẩu qua hộp thư mailbox.org
     */
    public void sendPasswordResetOtp(String toEmail, String otp, String recipientName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("huter@mailbox.org", "Quản Lý Gửi Xe");
            helper.setTo(toEmail.trim());
            helper.setSubject("Mã xác thực đặt lại mật khẩu - Quản Lý Gửi Xe");

            String displayName = (recipientName != null && !recipientName.isBlank()) ? recipientName.trim() : "Quý khách";

            String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 24px; color: #1e293b; }
                        .container { max-width: 540px; margin: 0 auto; background: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 10px 25px -5px rgba(15, 23, 42, 0.08); }
                        .header { background: linear-gradient(135deg, #0f172a 0%, #1e3a8a 100%); color: #ffffff; padding: 32px 24px; text-align: center; }
                        .header-badge { display: inline-block; background: rgba(255, 255, 255, 0.16); padding: 4px 12px; border-radius: 9999px; font-size: 11px; font-weight: 700; letter-spacing: 0.05em; text-transform: uppercase; margin-bottom: 8px; }
                        .header-title { margin: 0; font-size: 20px; font-weight: 800; }
                        .content { padding: 32px 28px; }
                        .salutation { font-size: 15px; font-weight: 600; margin-bottom: 12px; }
                        .desc { font-size: 14px; line-height: 1.6; color: #475569; margin-bottom: 24px; }
                        .otp-box { background: #eff6ff; border: 2px dashed #3b82f6; border-radius: 12px; padding: 20px; text-align: center; margin-bottom: 24px; }
                        .otp-label { font-size: 12px; font-weight: 700; color: #2563eb; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 6px; }
                        .otp-code { font-family: 'Consolas', 'Courier New', monospace; font-size: 34px; font-weight: 800; color: #1e40af; letter-spacing: 6px; }
                        .warning-box { background: #fffbeb; border-left: 4px solid #f59e0b; padding: 12px 16px; border-radius: 6px; font-size: 13px; color: #92400e; line-height: 1.5; margin-bottom: 24px; }
                        .footer { background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 18px 24px; text-align: center; font-size: 12px; color: #94a3b8; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <span class="header-badge">HỆ THỐNG QUẢN LÝ GỬI XE</span>
                            <h1 class="header-title">Yêu Cầu Đặt Lại Mật Khẩu</h1>
                        </div>
                        <div class="content">
                            <div class="salutation">Xin chào <strong>{{DISPLAY_NAME}}</strong>,</div>
                            <p class="desc">
                                Bạn vừa gửi yêu cầu đặt lại mật khẩu cho tài khoản trên hệ thống Quản Lý Gửi Xe.
                                Vui lòng nhập mã xác thực OTP bên dưới để hoàn tất việc đổi mật khẩu mới:
                            </p>
                            <div class="otp-box">
                                <div class="otp-label">MÃ XÁC THỰC CỦA BẠN</div>
                                <div class="otp-code">{{OTP}}</div>
                            </div>
                            <div class="warning-box">
                                <strong>Lưu ý:</strong> Mã xác thực có hiệu lực trong vòng <strong>15 phút</strong>.
                                Tuyệt đối không chia sẻ mã này cho bất kỳ ai khác để bảo đảm an toàn tài khoản.
                            </div>
                            <p class="desc" style="font-size: 13px; color: #64748b; margin-bottom: 0;">
                                Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email hoặc liên hệ với ban quản lý để được hỗ trợ.
                            </p>
                        </div>
                        <div class="footer">
                            Email tự động được gửi từ máy chủ bảo mật mailbox.org &bull; Hệ Thống Quản Lý Gửi Xe
                        </div>
                    </div>
                </body>
                </html>
                """
                .replace("{{DISPLAY_NAME}}", displayName)
                .replace("{{OTP}}", otp);

            helper.setText(html, true);
            mailSender.send(message);
            log.info("Sent password reset OTP to email {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset OTP to email {}: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("Không thể gửi email xác thực qua máy chủ mail: " + e.getMessage(), e);
        }
    }
}

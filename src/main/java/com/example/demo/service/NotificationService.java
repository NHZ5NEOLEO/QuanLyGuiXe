package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service("thongBaoService")
public class NotificationService implements ThongBaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // Cấu hình Telegram Bot (có thể đặt trong application.properties)
    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.bot.chat-id:}")
    private String chatId;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public boolean guiThongBaoEmail(String emailNhan, String tieuDe, String noiDung) {
        if (emailNhan == null || emailNhan.trim().isEmpty()) {
            return false;
        }
        log.info("========== [EMAIL SERVICE - GỬI HÓA ĐƠN ĐIỆN TỬ] ==========");
        log.info("Người nhận: {}", emailNhan);
        log.info("Tiêu đề: {}", tieuDe);
        log.info("Nội dung: {}", noiDung);
        log.info("==========================================================");
        return true;
    }

    public boolean sendEmailNotification(String toEmail, String subject, String bodyContent) {
        return guiThongBaoEmail(toEmail, subject, bodyContent);
    }

    @Override
    public boolean guiCanhBaoAnNinhTelegram(String tieuDe, String chiTiet) {
        String message = String.format("🚨 *[%s]*\n%s\n⏰ *Thời gian:* %s",
                tieuDe, chiTiet, java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        log.warn("📢 [TELEGRAM SECURITY ALERT]:\n{}", message);

        if (botToken != null && !botToken.trim().isEmpty() && chatId != null && !chatId.trim().isEmpty()) {
            try {
                String url = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                        botToken.trim(),
                        chatId.trim(),
                        URLEncoder.encode(message, StandardCharsets.UTF_8));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(res -> log.info("Đã gửi tin nhắn Telegram: status = {}", res.statusCode()));
                return true;
            } catch (Exception e) {
                log.error("Lỗi khi gửi Telegram alert: {}", e.getMessage());
            }
        }
        return true;
    }

    public boolean sendTelegramSecurityAlert(String title, String details) {
        return guiCanhBaoAnNinhTelegram(title, details);
    }
}

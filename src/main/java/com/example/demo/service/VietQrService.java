package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class VietQrService {

    // Cấu hình tài khoản nhận thanh toán của Bãi xe (Ví MoMo / Napas 247 - NGUYỄN HUỆ)
    private final String bankId = "971025"; // MoMo Napas BIN
    private final String accountNo = "PSP2601617600000204"; // Số tài khoản / Mã ví thụ hưởng
    private final String accountName = "NGUYEN HUE";
    private final String defaultQrImage = "/images/momo-qr-nguyenhue.png";

    public String generateQrUrl(double amount, String bienSoXe, String maVe) {
        if (amount <= 0) {
            return defaultQrImage;
        }
        try {
            long intAmount = Math.round(amount);
            String addInfo = "XE RA " + (bienSoXe != null ? bienSoXe : "") + " " + (maVe != null ? maVe : "");
            String encodedAddInfo = URLEncoder.encode(addInfo.trim(), StandardCharsets.UTF_8);
            String encodedName = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
            return String.format(
                    "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    bankId, accountNo, intAmount, encodedAddInfo, encodedName
            );
        } catch (Exception e) {
            return defaultQrImage;
        }
    }

    public String getDefaultQrImage() {
        return defaultQrImage;
    }

    public String getBankId() {
        return bankId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getAccountName() {
        return accountName;
    }
}

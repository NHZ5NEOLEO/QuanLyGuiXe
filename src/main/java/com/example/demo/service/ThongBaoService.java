package com.example.demo.service;

/**
 * Service gửi thông báo email hóa đơn và cảnh báo an ninh Telegram
 */
public interface ThongBaoService {

    boolean guiThongBaoEmail(String emailNhan, String tieuDe, String noiDung);

    boolean guiCanhBaoAnNinhTelegram(String tieuDe, String chiTiet);
}

# HƯỚNG DẪN ĐƯA HỆ THỐNG GỬI XE LÊN MẠNG THẬT (ONLINE CLOUD DEMO)

---

## 🎯 MỤC TIÊU
Biến dự án chạy trên máy tính cá nhân (`localhost:8081`) thành **Website trực tuyến có địa chỉ HTTPS toàn cầu**. 
Trong buổi bảo vệ đồ án, bạn có thể:
1. Mở màn hình chiếu Slide và Web Quản trị trên máy tính.
2. Mời **Thầy Cô / Hội đồng giám khảo cầm chính điện thoại di động (iPhone / Android) quét mã QR** để vào trang Web Portal công khai.
3. Thầy Cô thử bấm **"Đặt chỗ đỗ xe trước"** hoặc **"Đăng ký vé tháng"** trên điện thoại -> Màn hình quản trị của bạn trên máy tính lập tức nhảy thông báo và đổi màu ô đỗ Realtime! Điều này tạo nên hiệu ứng ấn tượng vượt bậc (**WOW Factor 10/10**).

---

## 🚀 CÁCH 1: DÙNG CLOUDFLARE TUNNEL (KHUYÊN DÙNG - 1 CLICK, HOÀN TOÀN MIỄN PHÍ)

Cloudflare Tunnel là giải pháp cao cấp của tập đoàn Cloudflare (Mỹ). Không cần mở port modem, không cần tài khoản, hoàn toàn miễn phí và có chứng chỉ bảo mật SSL HTTPS xanh.

### Cách thực hiện siêu đơn giản:
1. Hãy chắc chắn ứng dụng Spring Boot đang chạy ở cổng `8081`.
2. Trong thư mục dự án `d:\ECLIPSE WORK\Spring\demo`, **nhấp đúp chuột vào file `start-online-demo.bat`**.
3. File script sẽ tự động tải công cụ `cloudflared.exe` (nếu chưa có) và khởi tạo đường truyền HTTPS.
4. Màn hình console sẽ xuất hiện một đường link có định dạng:
   ```text
   https://random-name-here.trycloudflare.com
   ```
5. Bạn sao chép đường link này:
   - Dán vào trình duyệt để test.
   - Bấm vào nút **"📱 Mở trên Điện Thoại (QR Code)"** trên thanh menu để hiện mã QR lớn cho hội đồng quét bằng camera điện thoại.

---

## 🌐 CÁCH 2: DÙNG NGROK (PHỔ BIẾN)

Nếu máy bạn đã cài sẵn `ngrok`:
1. Mở CMD hoặc PowerShell tại thư mục dự án.
2. Gõ lệnh:
   ```bash
   ngrok http 8081
   ```
3. Copy đường link `Forwarding` dạng: `https://xxxx.ngrok-free.app` để truy cập.

---

## 📱 KỊCH BẢN THUYẾT TRÌNH BẢO VỆ "ĐIỂM 10" TRÊN ĐIỆN THOẠI THẬT

- **Bước 1**: Bật `start-online-demo.bat`, lấy link HTTPS công khai và mở trên máy tính chiếu lên máy chiếu.
- **Bước 2**: Trên thanh Menu trang chủ, bấm **"📱 Quét mã truy cập điện thoại"** để hiện mã QR to rõ trên màn hình chiếu.
- **Bước 3**: Giới thiệu với hội đồng: 
  > *"Kính thưa quý Thầy Cô, hệ thống của em đã sẵn sàng cho môi trường thực tế. Em xin kính mời quý Thầy Cô dùng chính điện thoại thông minh quét mã QR trên màn hình chiếu để trải nghiệm dịch vụ bãi xe thông minh."*
- **Bước 4**: Thầy Cô dùng điện thoại:
  - Tra cứu biển số xe (`29A-12345` hoặc `29B1-88888`).
  - Chọn 1 ô đỗ còn trống trong Khu A và bấm **"Đặt giữ chỗ trước trực tuyến"** với tiền cọc VietQR 20.000đ.
  - Gửi đánh giá 5 sao về chất lượng bãi xe.
- **Bước 5**: Thầy cô gửi xong, lập tức màn hình Quản trị (`/dashboard` và `/parking-map`) trên máy tính của bạn tự động cập nhật ô đỗ sang màu **Vàng (ĐÃ ĐẶT TRƯỚC)** và nhận xét mới nhất hiển thị ngay lập tức!

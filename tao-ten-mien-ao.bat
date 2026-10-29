@echo off
chcp 65001 >nul
title Cấu hình Tên Miền .localhost cho Hệ Thống Quản Lý Bãi Xe
echo ======================================================================
echo    CẤU HÌNH TÊN MIỀN .LOCALHOST (BẢO MẬT & HỖ TRỢ CAMERA 100%%)
echo ======================================================================
echo.

:: Kiem tra quyen Administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [CẢNH BÁO] File cần được chạy bằng quyền Administrator để cập nhật file hosts!
    echo Vui lòng nhấp chuột phải vào file này và chọn "Run as administrator".
    echo.
    pause
    exit /b 1
)

set "HOSTS_FILE=%WINDIR%\System32\drivers\etc\hosts"

echo Đang kiểm tra và thêm các tên miền .localhost vào file hosts...
echo.

:: 1. baixe.localhost
findstr /i "baixe.localhost" "%HOSTS_FILE%" >nul || (
    echo 127.0.0.1   baixe.localhost >> "%HOSTS_FILE%"
    echo [+] Đã thêm: http://baixe.localhost
)

:: 2. quanlyguixe.localhost
findstr /i "quanlyguixe.localhost" "%HOSTS_FILE%" >nul || (
    echo 127.0.0.1   quanlyguixe.localhost >> "%HOSTS_FILE%"
    echo [+] Đã thêm: http://quanlyguixe.localhost
)

:: 3. parking.localhost
findstr /i "parking.localhost" "%HOSTS_FILE%" >nul || (
    echo 127.0.0.1   parking.localhost >> "%HOSTS_FILE%"
    echo [+] Đã thêm: http://parking.localhost
)

:: 4. guixe.localhost
findstr /i "guixe.localhost" "%HOSTS_FILE%" >nul || (
    echo 127.0.0.1   guixe.localhost >> "%HOSTS_FILE%"
    echo [+] Đã thêm: http://guixe.localhost
)

:: 5. smartparking.localhost
findstr /i "smartparking.localhost" "%HOSTS_FILE%" >nul || (
    echo 127.0.0.1   smartparking.localhost >> "%HOSTS_FILE%"
    echo [+] Đã thêm: http://smartparking.localhost
)

echo.
echo ======================================================================
echo >>> CẤU HÌNH HOÀN TẤT! Bạn có thể mở trình duyệt và truy cập ngay:
echo     1. http://baixe.localhost/gui-xe
echo     2. http://quanlyguixe.localhost/gui-xe
echo     3. http://parking.localhost/gui-xe
echo     4. http://guixe.localhost/gui-xe
echo.
echo     (Tất cả tên miền .localhost đều hỗ trợ Camera & an toàn bảo mật 100%%)
echo ======================================================================
echo.
pause

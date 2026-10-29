@echo off
chcp 65001 >nul
title Eco Smart Parking - Online Cloud Tunnel
color 0A

echo ======================================================================
echo    ECO SMART PARKING - KET NOI HE THONG LEN INTERNET (ONLINE DEMO)
echo ======================================================================
echo.
echo [1] Kiem tra ket noi den ung dung Spring Boot (Port 80)...
powershell -Command "try { $res = Invoke-WebRequest -Uri 'http://localhost' -TimeoutSec 3 -UseBasicParsing; Write-Host '>>> Spring Boot dang chay TOT tren cong 80!' -ForegroundColor Green } catch { Write-Host '>>> CANH BAO: Ban can chay ung dung Spring Boot tren port 80 truoc!' -ForegroundColor Yellow }"
echo.

echo [2] Kiem tra cong cu Cloudflare Tunnel (cloudflared.exe)...
if exist "cloudflared.exe" (
    echo >>> Da tim thay cloudflared.exe san sang!
    goto RUN_TUNNEL
)

where cloudflared >nul 2>nul
if %errorlevel% equ 0 (
    echo >>> Da tim thay cloudflared trong he thong!
    set "CF_CMD=cloudflared"
    goto START_CF
)

echo >>> Dang tu dong tai cloudflared.exe ban moi nhat tu Cloudflare GitHub...
powershell -Command "Write-Host 'Dang tai xuong (khoang 15MB)...'; [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe' -OutFile 'cloudflared.exe'"

if not exist "cloudflared.exe" (
    echo [LOI] Khong the tu dong tai cloudflared.exe!
    echo Vui long tham khao huong dan trong file HUONG_DAN_ONLINE_DEMO.md de cai dat thu cong.
    pause
    exit /b 1
)

:RUN_TUNNEL
set "CF_CMD=.\cloudflared.exe"

:START_CF
echo.
echo ======================================================================
echo >>> DANG KHOI TAO DUONG TRUYEN HTTPS CONG KHAI CHO HE THONG...
echo >>> Vui long tim dong chu mau vang co link: https://*.trycloudflare.com
echo >>> Copy duong link do de mo tren dien thoai hoac gui cho hoi dong!
echo ======================================================================
echo.

%CF_CMD% tunnel --url http://localhost:80

pause

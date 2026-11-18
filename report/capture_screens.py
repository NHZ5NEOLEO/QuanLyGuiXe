import os
import time
from playwright.sync_api import sync_playwright

dest_dir = os.path.abspath(r'report\assets\screenshots')
os.makedirs(dest_dir, exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, channel='msedge')
    context = browser.new_context(viewport={'width': 1920, 'height': 1080}, device_scale_factor=1)
    page = context.new_page()

    print('1. Capturing 01-login.png...')
    page.goto('http://localhost/login')
    page.wait_for_timeout(1000)
    page.screenshot(path=os.path.join(dest_dir, '01-login.png'), full_page=True)

    print('Logging in as admin...')
    page.fill('input#username', 'admin')
    page.fill('input#password', 'admin123')
    page.keyboard.press('Enter')
    page.wait_for_timeout(3000)

    print('2. Capturing 02-dashboard.png...')
    page.goto('http://localhost/tong-quan')
    page.wait_for_timeout(2000)
    page.screenshot(path=os.path.join(dest_dir, '02-dashboard.png'), full_page=True)

    print('3. Capturing 03-users.png...')
    page.goto('http://localhost/staff')
    page.wait_for_timeout(1500)
    page.screenshot(path=os.path.join(dest_dir, '03-users.png'), full_page=True)

    print('4. Capturing 04-employees.png (Shifts & Handover)...')
    page.goto('http://localhost/ca-truc')
    page.wait_for_timeout(1500)
    page.screenshot(path=os.path.join(dest_dir, '04-employees.png'), full_page=True)

    print('5. Capturing 05-customers.png (Monthly Pass)...')
    page.goto('http://localhost/ve-thang')
    page.wait_for_timeout(1500)
    page.screenshot(path=os.path.join(dest_dir, '05-customers.png'), full_page=True)

    print('6. Capturing 06-vehicles.png...')
    page.goto('http://localhost/phuong-tien')
    page.wait_for_timeout(1500)
    page.screenshot(path=os.path.join(dest_dir, '06-vehicles.png'), full_page=True)

    print('7. Capturing 07-vehicle-types.png (Pricing)...')
    page.goto('http://localhost/price-settings')
    page.wait_for_timeout(1500)
    page.screenshot(path=os.path.join(dest_dir, '07-vehicle-types.png'), full_page=True)

    print('8. Capturing 08-parking-tickets.png (Active Parking)...')
    page.goto('http://localhost/xe-trong-bai')
    page.wait_for_timeout(1500)
    page.screenshot(path=os.path.join(dest_dir, '08-parking-tickets.png'), full_page=True)

    print('9. Capturing 09-vehicle-entry.png...')
    page.goto('http://localhost/gui-xe')
    page.wait_for_timeout(2000)
    page.screenshot(path=os.path.join(dest_dir, '09-vehicle-entry.png'), full_page=True)

    print('10. Capturing 10-camera-recognition.png...')
    # Capture the camera / OCR card area
    cam_el = page.query_selector('.camera-card, .camera-container, .card:has(video), .card:has(#webcamVideo)')
    if cam_el:
        cam_el.screenshot(path=os.path.join(dest_dir, '10-camera-recognition.png'))
    else:
        page.screenshot(path=os.path.join(dest_dir, '10-camera-recognition.png'), full_page=False)

    print('11. Capturing 11-entry-confirmation.png...')
    # Click sample plate button to trigger confirmation box & barrier preview
    try:
        sample_btn = page.query_selector('button[onclick*=\"fillPlate\"], .sample-plate-btn, button:has-text(\"30A\"), button:has-text(\"29B\")')
        if sample_btn:
            sample_btn.click()
            page.wait_for_timeout(1200)
    except Exception as e:
        print('Sample plate click note:', e)
    page.screenshot(path=os.path.join(dest_dir, '11-entry-confirmation.png'), full_page=True)

    print('12. Capturing 12-qr-code.png...')
    # Look for ticket QR on lookup page
    try:
        page.goto('http://localhost/tra-cuu')
        page.wait_for_timeout(1000)
        plate_inp = page.query_selector('input[type=\"text\"], input[placeholder*=\"Biển số\"]')
        if plate_inp:
            plate_inp.fill('29B1-88888')
            page.keyboard.press('Enter')
            page.wait_for_timeout(2000)
    except Exception as e:
        print('QR lookup note:', e)
    page.screenshot(path=os.path.join(dest_dir, '12-qr-code.png'), full_page=True)

    print('13. Capturing 13-vehicle-exit.png...')
    page.goto('http://localhost/xe-ra')
    page.wait_for_timeout(1500)
    page.screenshot(path=os.path.join(dest_dir, '13-vehicle-exit.png'), full_page=True)

    print('14. Capturing 14-parking-fee.png...')
    try:
        inp = page.query_selector('input#searchInput, input#ticketQuery, input[name=\"code\"], input[name=\"query\"]')
        if inp:
            inp.fill('29B1-88888')
            page.keyboard.press('Enter')
            page.wait_for_timeout(2500)
    except Exception as e:
        print('Fee lookup note:', e)
    page.screenshot(path=os.path.join(dest_dir, '14-parking-fee.png'), full_page=True)

    print('15. Capturing 15-statistics.png...')
    page.goto('http://localhost/history')
    page.wait_for_timeout(1500)
    page.screenshot(path=os.path.join(dest_dir, '15-statistics.png'), full_page=True)

    browser.close()
    print('SUCCESS: All 15 real screenshots captured!')


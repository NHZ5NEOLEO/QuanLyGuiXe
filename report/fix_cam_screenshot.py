from playwright.sync_api import sync_playwright
import os

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, channel='msedge')
    page = browser.new_page(viewport={'width': 1920, 'height': 1080})
    page.goto('http://localhost/login')
    page.fill('input#username', 'admin')
    page.fill('input#password', 'admin123')
    page.keyboard.press('Enter')
    page.wait_for_timeout(2500)
    page.goto('http://localhost/gui-xe')
    page.wait_for_timeout(1500)
    
    # Try sample buttons if available
    try:
        sample_btns = page.query_selector_all('button')
        for b in sample_btns:
            if '59' in b.inner_text() or '30' in b.inner_text():
                b.click()
                break
    except:
        pass
        
    page.wait_for_timeout(800)
    cards = page.query_selector_all('.card')
    if len(cards) > 2:
        dest = os.path.abspath(r'report\assets\screenshots\10-camera-recognition.png')
        cards[2].screenshot(path=dest)
        print('Captured Card 2 to', dest)
    browser.close()


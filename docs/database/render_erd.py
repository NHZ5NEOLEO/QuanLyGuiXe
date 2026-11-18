import json
import os
from playwright.sync_api import sync_playwright

docs_db_dir = os.path.abspath(r'docs\database')
schema_file = os.path.join(docs_db_dir, 'schema.json')
fks_file = os.path.join(docs_db_dir, 'foreign_keys.json')

with open(schema_file, 'r', encoding='utf-8-sig') as f:
    cols_data = json.load(f)

with open(fks_file, 'r', encoding='utf-8-sig') as f:
    fks_data = json.load(f)

# Group columns by table
tables = {}
for col in cols_data:
    t_name = col['Table']
    if t_name not in tables:
        tables[t_name] = []
    tables[t_name].append(col)

# Metadata for tables: Vietnamese titles, domains, colors
table_meta = {
    'nguoi_dung': {
        'title': 'nguoi_dung (Người dùng & Phân quyền)',
        'header_bg': '#1E3A8A', # Deep Blue
        'header_accent': '#3B82F6',
        'domain': 'Tài khoản & Xác thực',
        'x': 50, 'y': 80, 'w': 340
    },
    'khach_hang': {
        'title': 'khach_hang (Hồ sơ Khách hàng / Cư dân)',
        'header_bg': '#1D4ED8', # Royal Blue
        'header_accent': '#60A5FA',
        'domain': 'Khách hàng & Thuê bao',
        'x': 50, 'y': 440, 'w': 340
    },
    'password_reset_otp': {
        'title': 'password_reset_otp (Mã OTP Khôi phục)',
        'header_bg': '#475569', # Slate
        'header_accent': '#94A3B8',
        'domain': 'Bảo mật & Xác thực',
        'x': 50, 'y': 780, 'w': 340
    },
    'ca_truc': {
        'title': 'ca_truc (Ca trực & Bàn giao tài chính)',
        'header_bg': '#0F766E', # Teal
        'header_accent': '#14B8A6',
        'domain': 'Quản lý Ca trực',
        'x': 50, 'y': 1070, 'w': 340
    },
    'danh_gia': {
        'title': 'danh_gia (Đánh giá & Góp ý dịch vụ)',
        'header_bg': '#854D0E', # Warm Amber
        'header_accent': '#EAB308',
        'domain': 'Chăm sóc Khách hàng',
        'x': 50, 'y': 1520, 'w': 340
    },
    'loai_xe': {
        'title': 'loai_xe (Loại phương tiện & Đơn giá)',
        'header_bg': '#065F46', # Emerald
        'header_accent': '#10B981',
        'domain': 'Phương tiện & Giá vé',
        'x': 450, 'y': 80, 'w': 350
    },
    'phuong_tien': {
        'title': 'phuong_tien (Hồ sơ Phương tiện quản lý)',
        'header_bg': '#047857', # Forest Green
        'header_accent': '#34D399',
        'domain': 'Phương tiện lưu thông',
        'x': 450, 'y': 440, 'w': 350
    },
    'danh_sach_den': {
        'title': 'danh_sach_den (Cảnh báo Blacklist)',
        'header_bg': '#991B1B', # Dark Crimson
        'header_accent': '#EF4444',
        'domain': 'An ninh & Cảnh báo',
        'x': 450, 'y': 850, 'w': 350
    },
    'dat_cho_truoc': {
        'title': 'dat_cho_truoc (Đặt chỗ đỗ trực tuyến)',
        'header_bg': '#7C2D12', # Russet
        'header_accent': '#F97316',
        'domain': 'Dịch vụ Cổng trực tuyến',
        'x': 450, 'y': 1240, 'w': 350
    },
    'khu_vuc': {
        'title': 'khu_vuc (Phân khu bãi giữ xe)',
        'header_bg': '#92400E', # Bronze
        'header_accent': '#F59E0B',
        'domain': 'Hạ tầng Bãi đỗ',
        'x': 860, 'y': 80, 'w': 340
    },
    'vi_tri_do': {
        'title': 'vi_tri_do (Ô đỗ xe & Cảm biến)',
        'header_bg': '#B45309', # Amber Dark
        'header_accent': '#FBBF24',
        'domain': 'Vị trí đỗ thông minh',
        'x': 860, 'y': 380, 'w': 340
    },
    've_thang': {
        'title': 've_thang (Thuê bao vé tháng định kỳ)',
        'header_bg': '#5B21B6', # Deep Violet
        'header_accent': '#8B5CF6',
        'domain': 'Vé gửi dài hạn',
        'x': 860, 'y': 720, 'w': 340
    },
    've_xe': {
        'title': 've_xe (Lượt gửi xe - Thực thể trung tâm)',
        'header_bg': '#312E81', # Midnight Indigo
        'header_accent': '#6366F1',
        'domain': 'Giao dịch, Camera OCR, Barie & VietQR',
        'x': 1260, 'y': 80, 'w': 400
    }
}

row_height = 26
header_height = 48

# Calculate heights and anchor points for each table
table_bounds = {}
for t_name, meta in table_meta.items():
    col_count = len(tables[t_name])
    t_h = header_height + col_count * row_height + 12
    table_bounds[t_name] = {
        'x': meta['x'],
        'y': meta['y'],
        'w': meta['w'],
        'h': t_h,
        'cols': tables[t_name]
    }

# Canvas size
canvas_width = 1720
canvas_height = 1900

# Generate SVG
svg_parts = []
svg_parts.append(f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {canvas_width} {canvas_height}" width="{canvas_width}" height="{canvas_height}" style="background:#ffffff; font-family:'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, Helvetica, Arial, sans-serif;">''')

# Definitions: Filters, Markers, Shadows
svg_parts.append('''
<defs>
    <filter id="cardShadow" x="-10%" y="-10%" width="125%" height="125%">
        <feDropShadow dx="2" dy="5" stdDeviation="6" flood-color="#0f172a" flood-opacity="0.10" />
    </filter>
    <marker id="arrow-start-1" markerWidth="10" markerHeight="10" refX="2" refY="5" orient="auto">
        <path d="M 1 2 L 1 8 M 4 2 L 4 8" stroke="#334155" stroke-width="2" fill="none" />
    </marker>
    <marker id="arrow-end-n" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
        <path d="M 2 2 L 10 6 L 2 10" stroke="#334155" stroke-width="2" fill="none" />
    </marker>
</defs>
''')

# Title Banner
svg_parts.append('''
<rect x="50" y="15" width="1610" height="48" rx="8" fill="#F8FAFC" stroke="#E2E8F0" stroke-width="1.5"/>
<text x="70" y="44" font-size="20" font-weight="700" fill="#0F172A">SƠ ĐỒ THỰC THỂ LIÊN KẾT (ERD) - HỆ THỐNG PHẦN MỀM QUẢN LÝ GỬI XE THÔNG MINH</text>
<text x="1450" y="44" font-size="13" font-weight="600" fill="#475569">Microsoft SQL Server 2022 | 13 Bảng</text>
''')

# Draw relationships (connecting lines)
def get_table_center(tb):
    return (tb['x'] + tb['w'] / 2, tb['y'] + tb['h'] / 2)

for fk in fks_data:
    from_t = fk['FromTable']
    to_t = fk['ToTable']
    if from_t in table_bounds and to_t in table_bounds:
        b_from = table_bounds[from_t]
        b_to = table_bounds[to_t]
        
        # Determine connection anchor points
        if b_from['x'] < b_to['x']:
            x1 = b_from['x'] + b_from['w']
            y1 = b_from['y'] + 60
            x2 = b_to['x']
            y2 = b_to['y'] + 60
        elif b_from['x'] > b_to['x']:
            x1 = b_from['x']
            y1 = b_from['y'] + 60
            x2 = b_to['x'] + b_to['w']
            y2 = b_to['y'] + 60
        else:
            x1 = b_from['x'] + b_from['w']
            y1 = b_from['y'] + 60
            x2 = b_to['x'] + b_to['w']
            y2 = b_to['y'] + 60

        # Draw cubic bezier connection curve
        dx = abs(x2 - x1) * 0.5
        c_x1 = x1 + dx if x1 < x2 else x1 - dx
        c_x2 = x2 - dx if x1 < x2 else x2 + dx
        path_d = f"M {x1} {y1} C {c_x1} {y1}, {c_x2} {y2}, {x2} {y2}"
        svg_parts.append(f'''<path d="{path_d}" fill="none" stroke="#64748B" stroke-width="1.8" stroke-dasharray="none" opacity="0.65"/>''')

# Draw Tables
for t_name, b in table_bounds.items():
    meta = table_meta[t_name]
    x, y, w, h = b['x'], b['y'], b['w'], b['h']
    header_bg = meta['header_bg']
    header_accent = meta['header_accent']
    
    # Outer card
    svg_parts.append(f'''
    <g id="table-{t_name}" filter="url(#cardShadow)">
        <rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" fill="#FFFFFF" stroke="#CBD5E1" stroke-width="1.5" />
        <path d="M {x} {y+10} Q {x} {y} {x+10} {y} L {x+w-10} {y} Q {x+w} {y} {x+w} {y+10} L {x+w} {y+header_height} L {x} {y+header_height} Z" fill="{header_bg}" />
        <line x1="{x}" y1="{y+header_height}" x2="{x+w}" y2="{y+header_height}" stroke="{header_accent}" stroke-width="3" />
        <text x="{x+15}" y="{y+26}" font-size="14" font-weight="700" fill="#FFFFFF">{t_name}</text>
        <text x="{x+15}" y="{y+42}" font-size="10.5" font-weight="500" fill="#E2E8F0">{meta['domain']}</text>
    ''')
    
    # Rows
    curr_y = y + header_height + 4
    for idx, col in enumerate(b['cols']):
        bg_row = "#F8FAFC" if idx % 2 == 1 else "#FFFFFF"
        svg_parts.append(f'''<rect x="{x+1}" y="{curr_y}" width="{w-2}" height="{row_height}" fill="{bg_row}" />''')
        
        # PK / FK icon
        badge_text = ""
        badge_color = "#64748B"
        if col['IsPK'] and col['IsFK']:
            badge_text = "PK,FK"
            badge_color = "#D97706"
        elif col['IsPK']:
            badge_text = "PK"
            badge_color = "#B45309"
        elif col['IsFK']:
            badge_text = "FK"
            badge_color = "#2563EB"
            
        if badge_text:
            svg_parts.append(f'''
            <rect x="{x+10}" y="{curr_y+4}" width="34" height="18" rx="4" fill="{badge_color}" opacity="0.15" />
            <text x="{x+27}" y="{curr_y+17}" font-size="9" font-weight="700" fill="{badge_color}" text-anchor="middle">{badge_text}</text>
            ''')
        
        # Column Name
        font_weight = "700" if col['IsPK'] else "500"
        col_color = "#0F172A" if (col['IsPK'] or col['IsFK']) else "#334155"
        svg_parts.append(f'''<text x="{x+52}" y="{curr_y+17}" font-size="11.5" font-weight="{font_weight}" fill="{col_color}">{col['Column']}</text>''')
        
        # Data Type & Nullability
        type_str = col['Type'].upper()
        if type_str in ['VARCHAR', 'NVARCHAR', 'CHAR']:
            if col['MaxLength'] == -1:
                type_str += "(MAX)"
            else:
                l = col['MaxLength'] // 2 if type_str == 'NVARCHAR' else col['MaxLength']
                type_str += f"({l})"
                
        null_indicator = "NULL" if col['IsNullable'] else "NOT NULL"
        null_color = "#94A3B8" if col['IsNullable'] else "#64748B"
        svg_parts.append(f'''<text x="{x+w-12}" y="{curr_y+17}" font-size="10" font-weight="500" fill="{null_color}" text-anchor="end">{type_str} {null_indicator}</text>''')
        
        curr_y += row_height
        
    svg_parts.append('</g>')

# Legend at bottom
leg_y = 1810
svg_parts.append(f'''
<g id="legend">
    <rect x="50" y="{leg_y}" width="1610" height="70" rx="8" fill="#F8FAFC" stroke="#CBD5E1" stroke-width="1.5" />
    <text x="70" y="{leg_y+26}" font-size="13" font-weight="700" fill="#0F172A">CHÚ GIẢI THIẾT KẾ ERD QUẢN LÝ GỬI XE:</text>
    
    <rect x="70" y="{leg_y+36}" width="30" height="18" rx="3" fill="#B45309" opacity="0.2"/>
    <text x="85" y="{leg_y+49}" font-size="10" font-weight="700" fill="#B45309" text-anchor="middle">PK</text>
    <text x="110" y="{leg_y+49}" font-size="11" fill="#334155">Primary Key (Khóa chính)</text>

    <rect x="290" y="{leg_y+36}" width="30" height="18" rx="3" fill="#2563EB" opacity="0.2"/>
    <text x="305" y="{leg_y+49}" font-size="10" font-weight="700" fill="#2563EB" text-anchor="middle">FK</text>
    <text x="330" y="{leg_y+49}" font-size="11" fill="#334155">Foreign Key (Khóa ngoại)</text>

    <circle cx="530" cy="{leg_y+45}" r="6" fill="#1E3A8A"/>
    <text x="545" y="{leg_y+49}" font-size="11" fill="#334155">Người dùng &amp; Cư dân</text>

    <circle cx="720" cy="{leg_y+45}" r="6" fill="#065F46"/>
    <text x="735" y="{leg_y+49}" font-size="11" fill="#334155">Phương tiện &amp; Bảng giá</text>

    <circle cx="915" cy="{leg_y+45}" r="6" fill="#92400E"/>
    <text x="930" y="{leg_y+49}" font-size="11" fill="#334155">Khu vực &amp; Vị trí đỗ</text>

    <circle cx="1100" cy="{leg_y+45}" r="6" fill="#312E81"/>
    <text x="1115" y="{leg_y+49}" font-size="11" fill="#334155">Vé xe, Camera &amp; VietQR</text>

    <circle cx="1310" cy="{leg_y+45}" r="6" fill="#991B1B"/>
    <text x="1325" y="{leg_y+49}" font-size="11" fill="#334155">Danh sách đen &amp; An ninh</text>
</g>
''')

svg_parts.append('</svg>')
svg_content = "\n".join(svg_parts)

svg_path = os.path.join(docs_db_dir, 'ERD_QuanLyGuiXe.svg')
png_path = os.path.join(docs_db_dir, 'ERD_QuanLyGuiXe.png')

with open(svg_path, 'w', encoding='utf-8') as f:
    f.write(svg_content)
print(f"Generated SVG: {svg_path} (size: {os.path.getsize(svg_path)} bytes)")

# Render to high-resolution PNG using Playwright
print("Rendering SVG to high-resolution PNG using Playwright...")
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, channel='msedge')
    page = browser.new_page(viewport={'width': canvas_width, 'height': canvas_height}, device_scale_factor=2)
    
    html = f"""<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<style>
    body {{ margin: 0; padding: 0; background: #ffffff; }}
</style>
</head>
<body>
{svg_content}
</body>
</html>"""
    page.set_content(html)
    page.wait_for_timeout(1000)
    page.screenshot(path=png_path, full_page=True)
    print(f"Generated PNG: {png_path} (size: {os.path.getsize(png_path)} bytes)")

    pdf_path = os.path.join(docs_db_dir, 'ERD_QuanLyGuiXe.pdf')
    page.pdf(path=pdf_path, width=f'{canvas_width}px', height=f'{canvas_height}px', print_background=True)
    print(f"Generated PDF: {pdf_path} (size: {os.path.getsize(pdf_path)} bytes)")
    browser.close()

print("ALL ERD FILES GENERATED SUCCESSFULLY!")

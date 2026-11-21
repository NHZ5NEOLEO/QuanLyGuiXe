import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def create_document():
    doc = docx.Document()
    
    # Page setup - A4 margins: Top 2cm, Bottom 2cm, Left 3cm, Right 2cm
    for section in doc.sections:
        section.top_margin = Inches(0.79)     # ~2.0 cm
        section.bottom_margin = Inches(0.79)  # ~2.0 cm
        section.left_margin = Inches(1.18)    # ~3.0 cm
        section.right_margin = Inches(0.79)   # ~2.0 cm

    # Base styles
    normal_style = doc.styles['Normal']
    normal_font = normal_style.font
    normal_font.name = 'Times New Roman'
    normal_font.size = Pt(13)
    normal_font.color.rgb = RGBColor(0x1F, 0x29, 0x37)

    def set_cell_background(cell, fill_hex):
        shading_elm = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
        cell._tc.get_or_add_tcPr().append(shading_elm)

    def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = OxmlElement('w:tcMar')
        for margin_name, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
            node = OxmlElement(f'w:{margin_name}')
            node.set(qn('w:w'), str(val))
            node.set(qn('w:type'), 'dxa')
            tcMar.append(node)
        tcPr.append(tcMar)

    def add_custom_heading(text, level, space_before=12, space_after=6):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(space_before)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Times New Roman'
        run.bold = True
        if level == 1:
            run.font.size = Pt(15)
            run.font.color.rgb = RGBColor(0x1E, 0x3A, 0x8A) # Deep blue
        elif level == 2:
            run.font.size = Pt(13.5)
            run.font.color.rgb = RGBColor(0x25, 0x63, 0xEB) # Royal blue
        elif level == 3:
            run.font.size = Pt(13)
            run.font.color.rgb = RGBColor(0x0F, 0x76, 0x6E) # Teal
            run.italic = True
        return p

    def add_callout(text, prefix="LƯU Ý QUAN TRỌNG: ", color_hex="EFF6FF", border_color="2563EB"):
        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        cell = tbl.cell(0, 0)
        cell.width = Inches(6.5)
        set_cell_background(cell, color_hex)
        set_cell_margins(cell, top=140, bottom=140, left=200, right=200)
        
        tcPr = cell._tc.get_or_add_tcPr()
        borders = parse_xml(f'''
            <w:tcBorders {nsdecls("w")}>
                <w:top w:val="none"/>
                <w:left w:val="single" w:sz="24" w:space="0" w:color="{border_color}"/>
                <w:bottom w:val="none"/>
                <w:right w:val="none"/>
            </w:tcBorders>
        ''')
        tcPr.append(borders)
        
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        run_p = p.add_run(prefix)
        run_p.font.name = 'Times New Roman'
        run_p.bold = True
        run_p.font.size = Pt(12)
        run_p.font.color.rgb = RGBColor(0x1E, 0x40, 0xAF)
        
        run_t = p.add_run(text)
        run_t.font.name = 'Times New Roman'
        run_t.font.size = Pt(12)
        run_t.font.color.rgb = RGBColor(0x1F, 0x29, 0x37)
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # HEADER BANNER
    header_p = doc.add_paragraph()
    header_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    header_p.paragraph_format.space_after = Pt(4)
    run_sch = header_p.add_run("TRƯỜNG ĐẠI HỌC TÀI NGUYÊN VÀ MÔI TRƯỜNG TP. HỒ CHÍ MINH\nKHOA CÔNG NGHỆ THÔNG TIN\n")
    run_sch.font.name = 'Times New Roman'
    run_sch.font.size = Pt(12)
    run_sch.bold = True

    title_p = doc.add_paragraph()
    title_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    title_p.paragraph_format.space_before = Pt(10)
    title_p.paragraph_format.space_after = Pt(6)
    r_title = title_p.add_run("TỔNG HỢP NỘI DUNG HIỆU CHỈNH SAI LỆCH, BỔ SUNG TÍNH NĂNG\nVÀ VIẾT LẠI CHUẨN XÁC NỘI DUNG ĐỒ ÁN CHUYÊN NGÀNH (DACN)")
    r_title.font.name = 'Times New Roman'
    r_title.font.size = Pt(15.5)
    r_title.bold = True
    r_title.font.color.rgb = RGBColor(0x1E, 0x3A, 0x8A)

    sub_p = doc.add_paragraph()
    sub_p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    sub_p.paragraph_format.space_after = Pt(16)
    r_sub = sub_p.add_run("Đề tài: XÂY DỰNG PHẦN MỀM QUẢN LÝ GỬI XE\nSinh viên: Nguyễn Huệ - MSSV: 1250080254 - Lớp: 12_ĐH_THMT\nGiảng viên hướng dẫn: ThS. Ngô Tân Khai\n(Dùng để tích hợp và sao chép trực tiếp vào báo cáo Word chính)")
    r_sub.font.name = 'Times New Roman'
    r_sub.font.size = Pt(12.5)
    r_sub.italic = True

    add_callout(
        "Tài liệu này giải quyết triệt để 2 vấn đề bạn vừa yêu cầu:\n"
        "1. Đã đồng bộ giao diện Đăng Nhập bổ sung đầy đủ thẻ 'An Toàn & Bảo Mật 24/7' giống 100% bên Đăng Ký.\n"
        "2. Viết lại toàn bộ nội dung 'Đăng ký vé tháng' chuẩn xác về mặt nghiệp vụ: Khẳng định rõ vé tháng là gói thuê bao định kỳ trọn gói tiết kiệm chi phí, KHÔNG GIỮ CHỖ CỐ ĐỊNH / KHÔNG CÓ Ô ĐỖ RIÊNG.",
        prefix="CẬP NHẬT MỚI NHẤT: ", color_hex="F0FDF4", border_color="16A34A"
    )

    # -------------------------------------------------------------
    # PHẦN 1: BẢNG TỔNG HỢP CÁC LỖI SAI SÓT & ĐIỂM CẦN SỬA
    # -------------------------------------------------------------
    add_custom_heading("PHẦN 1. BẢNG TỔNG HỢP CÁC ĐIỂM SAI SÓT, HIỂU NHẦM VÀ LỖI ĐỊNH DẠNG TRONG BÁO CÁO CŨ", 1)

    t1 = doc.add_table(rows=1, cols=4)
    t1.alignment = WD_TABLE_ALIGNMENT.CENTER
    t1.autofit = False
    headers = ["STT", "Hạng mục / Vị trí", "Hiện trạng trong Báo cáo Word cũ", "Thực tế Project & Giải pháp khắc phục chuẩn"]
    col_widths = [Inches(0.5), Inches(1.5), Inches(2.2), Inches(2.5)]
    
    hdr_cells = t1.rows[0].cells
    for i, h_text in enumerate(headers):
        hdr_cells[i].width = col_widths[i]
        set_cell_background(hdr_cells[i], "1E40AF")
        set_cell_margins(hdr_cells[i], top=100, bottom=100, left=120, right=120)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h_text)
        r.font.name = 'Times New Roman'
        r.bold = True
        r.font.size = Pt(11)
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

    issues = [
        ("1", "Hiểu nhầm về bản chất Vé tháng (Giao diện & Báo cáo)",
         "Giao diện Đăng nhập / Đăng ký cũ và văn bản báo cáo dùng từ 'Chủ động giữ chỗ gửi xe' gây hiểu nhầm rằng vé tháng sẽ dành riêng một chỗ đỗ cố định.",
         "Trong project này, vé tháng là hình thức thuê bao trọn gói không giới hạn lượt vào/ra với giá ưu đãi, HOÀN TOÀN KHÔNG GIỮ CHỖ CỐ ĐỊNH. Khi vào bãi xe, xe tự do tìm vị trí trống. Đã đổi thành: 'Gửi xe trọn gói không giới hạn lượt với biểu phí ưu đãi'."),
        ("2", "Thiếu thẻ Bảo mật bên Đăng nhập",
         "Giao diện Đăng nhập chỉ có 3 thẻ tính năng, thiếu thẻ 'An Toàn & Bảo Mật 24/7' dẫn đến không đồng bộ với giao diện Đăng Ký.",
         "Đã bổ sung thẻ thứ 4 'An Toàn & Bảo Mật 24/7 - Mã hóa thông tin tài khoản và dữ liệu an toàn' vào dang-nhap.html giống hệt bên dang-ky.html."),
        ("3", "Số lượng bảng CSDL mâu thuẫn (Mục 2.5.1, 2.5.2, 3.2.1)",
         "Báo cáo ghi mâu thuẫn: chỗ ghi 15 bảng, chỗ ghi 13 bảng; Bảng 2.2 lại có 15 dòng.",
         "Thực tế CSDL SQL Server có chuẩn xác 14 bảng thực thể. Cần thống nhất toàn bộ báo cáo là 14 bảng, bỏ 2 bảng ảo và thêm bảng audit log."),
        ("4", "Hai bảng ảo không có trong SQL (Bảng 2.2, Hàng 14 & 15)",
         "Bảng 2.2 liệt kê bảng 'thanh_toan' và 'lich_su_nhan_dien' như hai bảng riêng.",
         "Project thực tế KHÔNG tạo 2 bảng này. Dữ liệu thanh toán và ảnh nhận diện camera đã được tích hợp tối ưu trực tiếp vào bảng 've_xe' (hinh_thuc_thanh_toan, so_tien_thuc_thu, anh_vao_url, anh_ra_url)."),
        ("5", "Thiếu bảng Lịch sử sửa đổi biểu phí (Bảng thứ 14)",
         "Báo cáo hoàn toàn CHƯA ĐỀ CẬP bảng 'lich_su_thay_doi_gia'.",
         "Project đã cài đặt bảng 'dbo.lich_su_thay_doi_gia' trên SQL Server và có entity LichSuThayDoiGia.java để ghi vết tự động mỗi khi Admin cập nhật giá gửi xe."),
        ("6", "Sai tên bảng Tiếng Anh (Mục 2.5.2, 3.2.1)",
         "Báo cáo ghi tên bảng 'password_reset_otp' (tiếng Anh).",
         "Dự án đã chuẩn hóa 100% CSDL tiếng Việt. Bảng đã được đổi tên thành 'dbo.otp_dat_lai_mat_khau' và đồng bộ trong entity PasswordResetOtp.java."),
        ("7", "Lỗi nhảy số thứ tự hình ảnh (Chương 3)",
         "Sau 'Hình 3.15' nhảy vọt lên 'Hình 3.18' (thiếu mất Hình 3.16 và 3.17).",
         "Đã đánh số lại liên tục từ Hình 3.1 đến Hình 3.26 để đảm bảo tính khoa học của đồ án tốt nghiệp/chuyên ngành."),
        ("8", "Lỗi trùng lặp 4 lần 'Hình 3.23'",
         "Hình 3.23 bị lặp lại 4 lần liên tiếp: Đánh giá dịch vụ, Hồ sơ tài khoản, Chức năng vé tháng, Gia hạn vé tháng.",
         "Đã sửa lại thành các hình riêng biệt: Hình 3.22, Hình 3.23, Hình 3.24, Hình 3.25, Hình 3.26."),
        ("9", "Thiếu các tính năng công nghệ nổi bật",
         "Báo cáo chưa phân tích: Thanh toán VietQR NAPAS 24/7 sinh động, Cơ chế Smart Code tại cổng trực, Auto mask biển số xe, Giám sát xe tồn bãi > 24h, Bàn giao ca trực in A4.",
         "Đã biên soạn đầy đủ thành các mục văn bản hoàn chỉnh để đưa vào Chương 2 và Chương 3.")
    ]

    for row_idx, data in enumerate(issues):
        row = t1.add_row()
        bg_color = "FFFFFF" if row_idx % 2 == 0 else "F9FAFB"
        for c_idx, text in enumerate(data):
            cell = row.cells[c_idx]
            cell.width = col_widths[c_idx]
            set_cell_background(cell, bg_color)
            set_cell_margins(cell, top=70, bottom=70, left=90, right=90)
            p = cell.paragraphs[0]
            p.paragraph_format.space_before = Pt(0)
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(text)
            r.font.name = 'Times New Roman'
            r.font.size = Pt(10.5)
            if c_idx in (0, 1):
                r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(8)

    # -------------------------------------------------------------
    # PHẦN 2: VIẾT LẠI CHUẨN XÁC NỘI DUNG ĐĂNG KÝ VÉ THÁNG (KHÔNG GIỮ CHỖ)
    # -------------------------------------------------------------
    add_custom_heading("PHẦN 2. NỘI DUNG VIẾT LẠI CHUẨN XÁC: NGHIỆP VỤ & QUY TRÌNH ĐĂNG KÝ VÉ THÁNG (LÀM RÕ CHÍNH SÁCH KHÔNG GIỮ CHỖ CỐ ĐỊNH)", 1)

    add_callout(
        "Đoạn văn dưới đây được biên soạn để bạn dán trực tiếp vào Mục thiết kế nghiệp vụ (Chương 2) và Mục kết quả chức năng Vé tháng (Chương 3). "
        "Nội dung này làm rõ bản chất thuê bao định kỳ trọn gói và giải thích lý do vì sao bãi xe KHÔNG áp dụng giữ chỗ cố định cho vé tháng (tối ưu mặt bằng và hệ số quay vòng chỗ đỗ).",
        prefix="HƯỚNG DẪN SỬ DỤNG: ", color_hex="FEF3C7", border_color="D97706"
    )

    add_custom_heading("Nội dung đưa vào Báo cáo Word (Sao chép đoạn dưới đây):", 2)

    p_vethang_doc = doc.add_paragraph()
    p_vethang_doc.paragraph_format.space_after = Pt(8)
    p_vethang_doc.paragraph_format.line_spacing = 1.2
    
    r = p_vethang_doc.add_run("1. Bản chất và khái niệm Vé tháng trong hệ thống quản lý gửi xe\n")
    r.bold = True
    r.font.color.rgb = RGBColor(0x1E, 0x40, 0xAF)
    
    p_vethang_doc.add_run(
        "Trong phần mềm Quản lý gửi xe, 'Vé tháng' (được quản lý bởi thực thể bảng 've_thang') là gói dịch vụ gửi xe định kỳ theo thời hạn (thường là 1 tháng, 3 tháng hoặc 6 tháng) dành cho đối tượng cư dân, cán bộ công nhân viên hoặc khách hàng có nhu cầu gửi xe thường xuyên. "
        "Mỗi vé tháng được gắn liền định danh với duy nhất một biển số xe và một loại phương tiện cụ thể của chủ xe.\n\n"
        "Về mặt tài chính, vé tháng mang lại lợi ích kinh tế vượt trội cho người sử dụng: thay vì phải trả phí lũy tiến theo từng lượt gửi ban ngày (ví dụ 4.000 đ/lượt) và ban đêm (6.000 đ/lượt), khách hàng chỉ cần thanh toán một khoản cước phí trọn gói duy nhất vào đầu kỳ (ví dụ 100.000 đ/tháng đối với xe máy). Trong suốt thời gian hiệu lực của vé, chủ phương tiện được ra vào bãi không giới hạn số lượt và hoàn toàn miễn phí khi qua cổng đối soát.\n\n"
    )

    r2 = p_vethang_doc.add_run("2. Nguyên tắc vận hành: 'KHÔNG GIỮ CHỖ CỐ ĐỊNH / KHÔNG CÓ Ô ĐỖ RIÊNG' (No Reserved Bay Policy)\n")
    r2.bold = True
    r2.font.color.rgb = RGBColor(0x1E, 0x40, 0xAF)

    p_vethang_doc.add_run(
        "Một trong những điểm mấu chốt cần làm rõ trong kiến trúc nghiệp vụ của đề tài là: Dịch vụ Vé tháng HOÀN TOÀN KHÔNG PHẢI LÀ DỊCH VỤ GIỮ CHỖ HOẶC DÀNH RIÊNG Ô ĐỖ CỐ ĐỊNH. "
        "Chính sách này xuất phát từ các cơ sở khoa học và thực tiễn vận hành bãi xe như sau:\n"
        "• Tối ưu hóa hệ số quay vòng bãi đỗ (Parking Turnover Rate): Nếu cấp phát một ô đỗ cố định riêng cho mỗi vé tháng, vị trí đó sẽ bị bỏ trống và lãng phí trong toàn bộ thời gian chủ xe đi làm, ra ngoài hoặc đi công tác. Bằng việc áp dụng phương thức 'đỗ xe tự do theo phân khu' (First-Come, First-Served within Designated Zone), bãi xe có thể tiếp nhận số lượng phương tiện tối đa, khai thác 100% công suất thực tế của mặt bằng bãi xe.\n"
        "• Quy tắc phân luồng linh hoạt: Khi phương tiện đăng ký vé tháng vào bãi, hệ thống tự động hướng dẫn xe đi vào đúng Phân khu quy định (Ví dụ: Xe máy vào Khu 2 Bánh - 2B; Ô tô vào Khu 4 Bánh - 4B). Người lái xe tự do chọn vị trí ô đỗ còn trống trong khu vực đó.\n"
        "• Phân định rạch ròi với dịch vụ 'Đặt chỗ trước' (dat_cho_truoc): Trường hợp khách hàng vãng lai có nhu cầu giữ chắc chắn một ô đỗ cụ thể trong một khung giờ xác định trước khi đến bãi, hệ thống cung cấp một phân hệ nghiệp vụ độc lập là 'Đặt chỗ trước' (Booking trực tuyến có đặt cọc và thời hạn giữ chỗ ngắn hạn). Hai nghiệp vụ này được tách biệt rõ ràng cả về logic xử lý lẫn cấu trúc bảng cơ sở dữ liệu.\n\n"
    )

    r3 = p_vethang_doc.add_run("3. Quy trình Đăng ký vé tháng trực tuyến trên Cổng dịch vụ (Public Portal)\n")
    r3.bold = True
    r3.font.color.rgb = RGBColor(0x1E, 0x40, 0xAF)

    p_vethang_doc.add_run(
        "Quy trình đăng ký vé tháng được số hóa hoàn toàn qua các bước tinh gọn:\n"
        "• Bước 1 - Xác thực tài khoản: Khách hàng đăng nhập vào Cổng thông tin (hoặc đăng ký tài khoản mới trong 30 giây). Việc bắt buộc có tài khoản nhằm đảm bảo tính bảo mật và thuận tiện cho việc quản lý hồ sơ phương tiện cá nhân.\n"
        "• Bước 2 - Điền thông tin phương tiện & Đăng ký: Hệ thống tự động đồng bộ họ tên, số điện thoại từ tài khoản. Khách hàng nhập biển số xe (hệ thống tự động chuẩn hóa định dạng biển số Việt Nam, ví dụ: '59-S1 298.82'), lựa chọn loại phương tiện (Xe máy, Ô tô, Xe điện) và thời hạn đăng ký (từ 1 đến 12 tháng).\n"
        "• Bước 3 - Tính toán cước phí và Cam kết chính sách: Hệ thống tự động hiển thị công thức tính phí minh bạch: Tổng tiền = Đơn giá tháng × Số tháng đăng ký. Đồng thời, hiển thị hộp thoại quy định rõ ràng: 'Vé tháng áp dụng cho 1 biển số và 1 loại xe, không đảm bảo vị trí ô đỗ riêng hoặc giữ chỗ cố định' để khách hàng xác nhận trước khi gửi đơn.\n"
        "• Bước 4 - Thanh toán VietQR và Kích hoạt mã vé: Khách hàng quét mã VietQR NAPAS 24/7 để thanh toán trực tuyến hoặc nộp phí tại quầy trực. Sau khi giao dịch hoàn tất, hệ thống tự động sinh mã thẻ vé tháng định danh duy nhất (dạng 'VT-XXXXXXXX') kèm mã QR điện tử lưu vào hồ sơ cá nhân.\n\n"
    )

    r4 = p_vethang_doc.add_run("4. Cơ chế tự động đối soát và mở cổng Barie khi xe ra/vào bãi\n")
    r4.bold = True
    r4.font.color.rgb = RGBColor(0x1E, 0x40, 0xAF)

    p_vethang_doc.add_run(
        "Khi phương tiện đăng ký vé tháng lưu thông qua cổng bãi xe:\n"
        "• Tại làn xe vào (/xe-vao): Camera OCR nhận diện biển số (hoặc nhân viên quét mã QR thẻ vé). Hệ thống lập tức truy vấn bảng 've_thang' theo biển số xe. Nếu vé tháng còn hạn (ngay_het_han >= hiện tại và trang_thai = 'HOAT_DONG'), hệ thống tự động hiển thị huy hiệu 'Vé tháng - Cư dân', ghi nhận bản ghi vào bảng 've_xe' với trạng thái 'Đang gửi' và phát lệnh mô phỏng mở thanh chắn Barie ngay lập tức.\n"
        "• Tại làn xe ra (/xe-ra): Khi xe xuất bãi, hệ thống đối soát biển số và thời hạn vé tháng. Do phương tiện đã đóng phí trọn gói, hệ thống tự động gán chi_phi = 0 đ, hinh_thuc_thanh_toan = 'VE_THANG', trang_thai_thanh_toan = 'MIEN_PHI' và mở Barie cho xe qua cổng mà không cần thực hiện thu tiền mặt hay chuyển khoản từng lượt."
    )

    # -------------------------------------------------------------
    # PHẦN 3: BẢNG DỮ LIỆU CSDL CHUẨN HÓA 14 BẢNG
    # -------------------------------------------------------------
    add_custom_heading("PHẦN 3. BẢNG DỮ LIỆU CƠ SỞ DỮ LIỆU CHUẨN HÓA HỆ THỐNG (14 BẢNG THỰC TẾ)", 1)

    t2 = doc.add_table(rows=1, cols=4)
    t2.alignment = WD_TABLE_ALIGNMENT.CENTER
    t2.autofit = False
    h2 = ["STT", "Tên bảng trong SQL Server", "Tên Entity Java", "Chức năng nghiệp vụ & Nội dung lưu trữ"]
    w2 = [Inches(0.5), Inches(1.8), Inches(1.7), Inches(2.5)]
    
    for i, h_text in enumerate(h2):
        t2.rows[0].cells[i].width = w2[i]
        set_cell_background(t2.rows[0].cells[i], "1E40AF")
        set_cell_margins(t2.rows[0].cells[i], top=100, bottom=100, left=120, right=120)
        p = t2.rows[0].cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h_text)
        r.font.name = 'Times New Roman'
        r.bold = True
        r.font.size = Pt(11)
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

    tables_14 = [
        ("1", "dbo.nguoi_dung", "NguoiDung.java", "Quản lý tài khoản hệ thống, tên đăng nhập, mật khẩu mã hóa BCrypt, họ tên, vai trò (ADMIN, NHAN_VIEN, KHACH_HANG) và trạng thái."),
        ("2", "dbo.khach_hang", "KhachHang.java", "Lưu trữ hồ sơ cư dân / khách hàng gửi xe (CCCD, SĐT, Email, địa chỉ) và khóa ngoại liên kết tài khoản portal."),
        ("3", "dbo.loai_xe", "LoaiXe.java", "Danh mục loại phương tiện (Xe máy, Ô tô, Xe đạp điện, Xe tải nhỏ) cùng cấu hình đơn giá gửi ngày, đêm và thuê bao tháng."),
        ("4", "dbo.khu_vuc", "KhuVuc.java", "Phân khu bãi đỗ xe (Khu 2 bánh, Khu 4 bánh, Khu xe điện), sức chứa thiết kế và vị trí mặt bằng."),
        ("5", "dbo.vi_tri_do", "ViTriDo.java", "Quản lý các ô đỗ xe thông minh theo mã vị trí (2B-01, 4B-01), trạng thái ô (TRONG, DA_DO, BAO_TRI) và khóa lạc quan (Version)."),
        ("6", "dbo.phuong_tien", "PhuongTien.java", "Hồ sơ phương tiện đã đăng ký trong hệ thống, bao gồm biển số xe, loại xe, chủ xe và liên kết khách hàng."),
        ("7", "dbo.ve_thang", "VeThang.java", "Quản lý thuê bao vé tháng định kỳ trọn gói (không giữ chỗ riêng), mã thẻ QR, thời hạn hiệu lực, giá tiền và trạng thái kích hoạt."),
        ("8", "dbo.ca_truc", "CaTruc.java", "Quản lý phiên làm việc của nhân viên, thời gian ca trực, tổng lượt xe vào/ra, đối soát doanh thu tiền mặt vs chuyển khoản VietQR."),
        ("9", "dbo.danh_sach_den", "DanhSachDen.java", "Danh sách đen cảnh báo an ninh (Blacklist), lưu biển số xe vi phạm/mất cắp, lý do, mức độ nguy hiểm để cảnh báo tự động khi xe vào."),
        ("10", "dbo.dat_cho_truoc", "DatChoTruoc.java", "Dịch vụ đặt giữ chỗ đỗ xe trước ngắn hạn qua Portal, lưu mã booking, biển số, thời gian hẹn, tiền cọc và trạng thái check-in."),
        ("11", "dbo.danh_gia", "DanhGia.java", "Lưu phản hồi đánh giá dịch vụ từ khách hàng (thang điểm 1-5 sao, chủ đề tốc độ, an ninh, giá cả, thái độ phục vụ)."),
        ("12", "dbo.otp_dat_lai_mat_khau", "PasswordResetOtp.java", "Lưu trữ mã OTP ngẫu nhiên gửi qua Email để phục vụ tính năng khôi phục mật khẩu tài khoản an toàn."),
        ("13", "dbo.ve_xe", "VeXe.java", "Thực thể trung tâm lưu từng lượt gửi xe: mã QR vé, biển số nhận diện, thời gian vào/ra, chi phí, phương thức thanh toán, ảnh chụp camera 2 làn."),
        ("14", "dbo.lich_su_thay_doi_gia", "LichSuThayDoiGia.java", "Nhật ký kiểm toán (Audit Log) ghi nhận toàn bộ lịch sử chỉnh sửa biểu phí gửi xe trên hệ thống: giá cũ, giá mới, người sửa, thời điểm sửa.")
    ]

    for row_idx, data in enumerate(tables_14):
        row = t2.add_row()
        bg_color = "FFFFFF" if row_idx % 2 == 0 else "F9FAFB"
        for c_idx, text in enumerate(data):
            cell = row.cells[c_idx]
            cell.width = w2[c_idx]
            set_cell_background(cell, bg_color)
            set_cell_margins(cell, top=70, bottom=70, left=90, right=90)
            p = cell.paragraphs[0]
            p.paragraph_format.space_before = Pt(0)
            p.paragraph_format.space_after = Pt(0)
            if c_idx == 0:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(text)
            r.font.name = 'Times New Roman'
            r.font.size = Pt(10.5)
            if c_idx in (0, 1):
                r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(8)

    # -------------------------------------------------------------
    # PHẦN 4: BẢNG ĐIỀU CHỈNH DANH MỤC HÌNH ẢNH
    # -------------------------------------------------------------
    add_custom_heading("PHẦN 4. BẢNG HIỆU CHỈNH DANH MỤC HÌNH ẢNH VÀ ĐÁNH SỐ CHUẨN XÁC", 1)

    t3 = doc.add_table(rows=1, cols=3)
    t3.alignment = WD_TABLE_ALIGNMENT.CENTER
    t3.autofit = False
    h3 = ["Số hình cũ (Word)", "Số hình chuẩn mới", "Tên chú thích hình ảnh đầy đủ & Chuẩn hóa"]
    w3 = [Inches(1.5), Inches(1.5), Inches(3.5)]

    for i, h_text in enumerate(h3):
        t3.rows[0].cells[i].width = w3[i]
        set_cell_background(t3.rows[0].cells[i], "1E40AF")
        set_cell_margins(t3.rows[0].cells[i], top=100, bottom=100, left=120, right=120)
        p = t3.rows[0].cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(h_text)
        r.font.name = 'Times New Roman'
        r.bold = True
        r.font.size = Pt(11)
        r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

    figures = [
        ("Hình 3.1", "Hình 3.1", "Sơ đồ cơ sở dữ liệu (Database Diagrams) của phần mềm quản lý gửi xe"),
        ("Hình 3.2", "Hình 3.2", "Danh sách các bảng thực thể trong cơ sở dữ liệu SQL Server"),
        ("Hình 3.3", "Hình 3.3", "Giao diện trang Đăng nhập hệ thống (Bổ sung đầy đủ thẻ An toàn & Bảo mật 24/7)"),
        ("Hình 3.4", "Hình 3.4", "Giao diện trang Tổng quan bãi xe (Dashboard theo dõi thời gian thực)"),
        ("Hình 3.5", "Hình 3.5", "Giao diện Quản lý tài khoản người dùng và phân quyền truy cập"),
        ("Hình 3.6", "Hình 3.6", "Giao diện Quản lý ca trực, đối soát tiền mặt/chuyển khoản và in biên bản"),
        ("Hình 3.7", "Hình 3.7", "Giao diện Quản lý danh sách khách hàng và thuê bao vé tháng"),
        ("Hình 3.8", "Hình 3.8", "Giao diện Quản lý danh mục phương tiện đăng ký gửi trong bãi"),
        ("Hình 3.9", "Hình 3.9", "Giao diện Cấu hình loại xe và cài đặt biểu phí gửi xe niêm yết"),
        ("Bổ sung mới", "Hình 3.10", "Giao diện Lịch sử sửa đổi biểu phí gửi xe (Audit Log đồng bộ SQL)"),
        ("Hình 3.10", "Hình 3.11", "Giao diện Theo dõi danh sách phương tiện đang gửi thực tế trong bãi"),
        ("Hình 3.11", "Hình 3.12", "Giao diện Trực cổng tiếp nhận phương tiện vào bãi (Làn vào 01)"),
        ("Hình 3.12", "Hình 3.13", "Camera OCR nhận diện biển số phương tiện tự động tại làn vào"),
        ("Hình 3.13", "Hình 3.14", "Nhân viên xác nhận thông tin xe vào và in phiếu gửi xe tích hợp mã QR"),
        ("Hình 3.14", "Hình 3.15", "Giao diện tính phí gửi xe và thanh toán tự động qua mã VietQR NAPAS 24/7"),
        ("Hình 3.15", "Hình 3.16", "Giao diện Báo cáo thống kê doanh thu và phân tích lưu lượng bãi xe"),
        ("Hình 3.18", "Hình 3.17", "Giao diện Cảnh báo an ninh, kiểm soát phương tiện tồn bãi quá 24 giờ"),
        ("Hình 3.19", "Hình 3.18", "Giao diện Trang chủ Cổng thông tin công khai (Public Portal)"),
        ("Hình 3.20", "Hình 3.19", "Giao diện Trang tra cứu thông tin lượt gửi phương tiện trực tuyến"),
        ("Hình 3.21", "Hình 3.20", "Giao diện Bảng giá gửi xe niêm yết công khai theo từng loại phương tiện"),
        ("Hình 3.22", "Hình 3.21", "Giao diện Đăng ký thuê bao vé tháng trực tuyến dành cho cư dân"),
        ("Hình 3.23 (1)", "Hình 3.22", "Giao diện Trang đánh giá chất lượng dịch vụ gửi xe của khách hàng"),
        ("Hình 3.23 (2)", "Hình 3.23", "Giao diện Trang Hồ sơ tài khoản cá nhân và đổi mật khẩu an toàn"),
        ("Hình 3.23 (3)", "Hình 3.24", "Giao diện Quản lý danh sách vé tháng của khách hàng và thời hạn sử dụng"),
        ("Hình 3.23 (4)", "Hình 3.25", "Giao diện Chức năng gia hạn vé tháng trực tuyến"),
        ("Bổ sung mới", "Hình 3.26", "Giao diện Chức năng Quên mật khẩu và xác thực mã OTP qua Email")
    ]

    for row_idx, data in enumerate(figures):
        row = t3.add_row()
        bg_color = "FFFFFF" if row_idx % 2 == 0 else "F9FAFB"
        for c_idx, text in enumerate(data):
            cell = row.cells[c_idx]
            cell.width = w3[c_idx]
            set_cell_background(cell, bg_color)
            set_cell_margins(cell, top=60, bottom=60, left=80, right=80)
            p = cell.paragraphs[0]
            p.paragraph_format.space_before = Pt(0)
            p.paragraph_format.space_after = Pt(0)
            if c_idx in (0, 1):
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(text)
            r.font.name = 'Times New Roman'
            r.font.size = Pt(10.5)
            if c_idx == 1:
                r.bold = True

    doc.add_paragraph().paragraph_format.space_after = Pt(12)

    # Footer note
    footer_p = doc.add_paragraph()
    footer_p.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    r_foot = footer_p.add_run("TP. Hồ Chí Minh, ngày 21 tháng 11 năm 2026\nSinh viên thực hiện: Nguyễn Huệ")
    r_foot.font.name = 'Times New Roman'
    r_foot.font.size = Pt(11.5)
    r_foot.italic = True

    # Save docx with fallback if user has v1 open in Microsoft Word
    output_path = r"d:\ECLIPSE WORK\Spring\demo\NOI_DUNG_BO_SUNG_VA_SUA_LOI_BAO_CAO_DACN_V2.docx"
    doc.save(output_path)
    print(f"Document successfully created at: {output_path}")
    try:
        doc.save(r"d:\ECLIPSE WORK\Spring\demo\NOI_DUNG_BO_SUNG_VA_SUA_LOI_BAO_CAO_DACN.docx")
        print("Updated original docx as well.")
    except Exception as e:
        print(f"Original docx is currently open in Word ({e}), saved as V2 successfully.")

if __name__ == "__main__":
    create_document()

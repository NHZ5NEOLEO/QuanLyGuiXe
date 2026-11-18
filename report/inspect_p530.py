import docx

doc = docx.Document('Bao_cao_do_an_Quan_ly_gui_xe_Hoan_thien.docx')
p = doc.paragraphs[530]
print('P530 text:', repr(p.text))
for i, r in enumerate(p.runs):
    print(f'Run {i}: text={repr(r.text)} | drawing={bool(r._r.xpath(".//w:drawing"))}')


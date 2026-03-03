
import zipfile
import xml.etree.ElementTree as ET
import sys
import re

def extract_text_from_docx(docx_path):
    try:
        with zipfile.ZipFile(docx_path) as zf:
            xml_content = zf.read('word/document.xml')
        
        # Parse XML
        root = ET.fromstring(xml_content)
        
        # Namespaces
        ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
        
        text_parts = []
        
        body = root.find('w:body', ns)
        if body is None:
            return "No body found in docx"
            
        for p in body.findall('.//w:p', ns):
            paragraph_text = []
            for node in p.iter():
                if node.tag == f"{{{ns['w']}}}t":
                    if node.text:
                        paragraph_text.append(node.text)
                elif node.tag == f"{{{ns['w']}}}tab":
                    paragraph_text.append('\t')
                elif node.tag == f"{{{ns['w']}}}br":
                    paragraph_text.append('\n')
                elif node.tag == f"{{{ns['w']}}}cr":
                    paragraph_text.append('\n')
            
            if paragraph_text:
                text_parts.append(''.join(paragraph_text))
            
        return '\n'.join(text_parts)
    except Exception as e:
        return f"Error extracting text: {e}"

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python extract_docx.py <path_to_docx>")
        sys.exit(1)
    
    file_path = sys.argv[1]
    print(extract_text_from_docx(file_path))


import zipfile
import re
import sys

def extract_text_regex(docx_path):
    try:
        with zipfile.ZipFile(docx_path) as zf:
            # list all files to see if we are missing something
            # print(zf.namelist()) 
            content = zf.read('word/document.xml').decode('utf-8')
            
        # Regex to find text inside <w:t> tags
        # <w:t>Text</w:t> or <w:t xml:space="preserve">Text</w:t>
        matches = re.findall(r'<w:t[^>]*>(.*?)</w:t>', content)
        
        return ' '.join(matches)
    except Exception as e:
        return f"Error: {e}"

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python extract_docx_regex.py <path_to_docx>")
        sys.exit(1)
    
    print(extract_text_regex(sys.argv[1]))

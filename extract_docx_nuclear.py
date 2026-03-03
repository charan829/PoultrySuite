
import zipfile
import re
import sys

def extract_text_nuclear(docx_path):
    try:
        with zipfile.ZipFile(docx_path) as zf:
            content = zf.read('word/document.xml').decode('utf-8')
            
        # Remove all XML tags
        text = re.sub(r'<[^>]+>', ' ', content)
        
        # Collapse whitespace
        text = re.sub(r'\s+', ' ', text).strip()
        
        return text
    except Exception as e:
        return f"Error: {e}"

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python extract_docx_nuclear.py <path_to_docx>")
        sys.exit(1)
    
    print(extract_text_nuclear(sys.argv[1]))

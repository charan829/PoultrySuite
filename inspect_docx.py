
import zipfile
import sys

def inspect_docx(docx_path):
    try:
        with zipfile.ZipFile(docx_path) as zf:
            print("Files in zip:")
            for name in zf.namelist():
                print(name)
            
            print("\n--- First 2000 chars of word/document.xml ---")
            content = zf.read('word/document.xml').decode('utf-8')
            print(content[:2000])
            
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python inspect_docx.py <path_to_docx>")
        sys.exit(1)
    
    inspect_docx(sys.argv[1])

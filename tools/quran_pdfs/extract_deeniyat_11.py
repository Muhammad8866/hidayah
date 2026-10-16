"""Extract the unchanged reference PDF and navigation index from the inspected APK.

Usage: python tools/quran_pdfs/extract_deeniyat_11.py path/to/reference.apk
The APK itself is not part of the source repository.
"""
import hashlib
import json
from pathlib import Path
import re
import sys
import zipfile

APK_SHA256 = "61109c69b181ab20322756defcf5d29867fc08743b5ba36c85c173b4893504bd"
PDF_SHA256 = "1bcd044b35d9b514acee62bcc29508737ec3a8074253b70a83114cfd5d53e228"

def main():
    apk = Path(sys.argv[1])
    assert hashlib.sha256(apk.read_bytes()).hexdigest() == APK_SHA256, "Unexpected reference APK"
    with zipfile.ZipFile(apk) as archive:
        pdf = archive.read("res/raw/quran_11_line_v1.pdf")
        sql = archive.read("assets/databases/quran11.sql").decode("utf-8")
    assert hashlib.sha256(pdf).hexdigest() == PDF_SHA256, "Unexpected Quran PDF"
    # The shipped SQL has unescaped apostrophes and a malformed CREATE statement.
    # Read only the numeric navigation columns, without executing foreign SQL.
    surahs = [int(m.group(2)) for m in re.finditer(
        r"INSERT INTO SURAH.*?VALUES\s*\((\d+),.*?,\s*'(\d+)',\s*\d+\);", sql, re.I)]
    paras = [int(m.group(2)) for m in re.finditer(
        r"INSERT INTO PARA.*?VALUES\s*\((\d+),.*?,(\d+)\);", sql, re.I)]
    assert len(surahs) == 114 and len(paras) == 30
    assert surahs == sorted(surahs) and paras == sorted(paras)
    assert all(1 <= p <= 1410 for p in surahs + paras)
    root = Path(__file__).resolve().parents[2]
    (root / "app/src/main/assets/quran_deeniyat_11_lines.pdf").write_bytes(pdf)
    Path(__file__).with_name("deeniyat_11.json").write_text(json.dumps({
        "surahs": surahs, "paras": paras, "sha256": PDF_SHA256, "pageCount": 1410
    }, indent=2), encoding="utf-8")
    print("Verified original PDF and 114 surah / 30 para index entries")

if __name__ == "__main__":
    main()

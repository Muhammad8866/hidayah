"""Cache the official recommended-name section for editorial verification."""
import json
import re
import urllib.request
from pathlib import Path
from concurrent.futures import ThreadPoolExecutor
from bs4 import BeautifulSoup

BASE = "https://www.dawateislami.net/bookslibrary/ur/naam-rakhnay-kay-ahkam"
OUT = Path("tmp/baby-name-sources")
OUT.mkdir(parents=True, exist_ok=True)

def collect(page):
    url = f"{BASE}/page-{page}"
    path = OUT / f"{page}.html"
    if not path.exists():
        path.write_bytes(urllib.request.urlopen(url, timeout=60).read())
    content = BeautifulSoup(path.read_bytes(), "html.parser").select_one("#book__content__main")
    assert content is not None, url
    rows = [[cell.get_text(" ", strip=True) for cell in row.select("td")]
            for row in content.select("tr")]
    text = content.get_text(" ", strip=True)
    (OUT / f"{page}.txt").write_text(text, encoding="utf-8")
    return dict(page=page, url=url, text=text, rows=rows)

if __name__ == "__main__":
    with ThreadPoolExecutor(max_workers=4) as pool:
        pages = list(pool.map(collect, range(43, 62)))
    (OUT / "pages.json").write_text(json.dumps(pages, ensure_ascii=False, indent=2), encoding="utf-8")
    candidates = []
    for page in pages:
        content = BeautifulSoup((OUT / f"{page['page']}.html").read_bytes(), "html.parser").select_one("#book__content__main")
        for paragraph in content.select('p'):
            match = re.fullmatch(r'([^()]{1,45})\((.*)\)', paragraph.get_text('', strip=True))
            if match and not re.search(r'[۔!؟:0-9۰-۹]', match[1]):
                candidates.append(dict(id=len(candidates), page=page['page'], arabic=match[1].strip(), urdu=match[2].strip()))
    (OUT / "candidates.json").write_text(json.dumps(candidates, ensure_ascii=False, indent=2), encoding="utf-8")
    print([(p["page"], len(p["text"]), len(p["rows"])) for p in pages])

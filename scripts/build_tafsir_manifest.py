"""Extract official Sirat ul Jinan audio links from saved Dawat-e-Islami listing pages.

Save pages 1..10 from /medialibrary/category/35/?category=18&pn=N as
tafsir_categoryN.html in the repo root before running this script.
"""

import glob
import html
import json
import re
from urllib.request import Request, urlopen
from pathlib import Path


records = {}
pages = [Path(path).read_text(encoding="utf-8", errors="replace")
         for path in sorted(glob.glob("tafsir_category[0-9]*.html"))]
if not pages:
    for number in range(1, 11):
        url = f"https://www.dawateislami.net/medialibrary/category/35/?category=18&pn={number}"
        with urlopen(Request(url, headers={"User-Agent": "Mozilla/5.0"}), timeout=30) as response:
            pages.append(response.read().decode("utf-8", errors="replace"))

for page in pages:
    pattern = re.compile(
        r'<a href="/medialibrary/(\d+)" title="([^"]+)"[\s\S]*?'
        r"downloadURL\('(https://data2\.dawateislami\.net/download/tilawat-e-quran/ur/mp3/\d+/\d+\.mp3)",
        re.I,
    )
    for media_id, title, url in pattern.findall(page):
        title = html.unescape(title)
        if "Sirat-ul-Jinan" not in title:
            continue
        match = re.match(r"(\d+)\s+Surah\s+", title, re.I)
        if not match:
            continue
        surah = int(match.group(1))
        records[media_id] = {"surah": surah, "title": title, "url": url, "page": f"https://www.dawateislami.net/medialibrary/{media_id}"}

items = sorted(records.values(), key=lambda item: (item["surah"], item["title"]))
items.append({
    "surah": 83,
    "title": "83 Surah Al-Mutaffifin - Tilawat, Tarjama Aur Tafseer Sirat-ul-Jinan",
    "url": "",
    # Dawat-e-Islami's official index links this recording in its complete-Surah playlist:
    # https://data2.dawateislami.net/static/images/main-website/pdf/quran-with-tafseer.pdf
    "page": "https://www.youtube.com/watch?v=zzwqw_82zHk",
})
items.sort(key=lambda item: (item["surah"], item["title"]))
assert len(items) == 115 and {item["surah"] for item in items} == set(range(1, 115)), "Source catalog incomplete"
print(f"{len(items)} tracks; {len({item['surah'] for item in items})} surahs")
print("Missing:", sorted(set(range(1, 115)) - {item["surah"] for item in items}))
Path("app/src/main/assets/quran_tafsir_sirat_ul_jinan.json").write_text(
    json.dumps(items, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
)

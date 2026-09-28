"""Fetches word-by-word Quran data and caches raw API responses per chapter/language."""
import json, os, sys, time, urllib.request
from concurrent.futures import ThreadPoolExecutor

HERE = os.path.dirname(os.path.abspath(__file__))
RAW = os.path.join(HERE, "raw")
os.makedirs(RAW, exist_ok=True)
LANGS = ["en", "ur", "hi", "bn"]


def get(url):
    for attempt in range(6):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "deen-companion-build/1.0"})
            with urllib.request.urlopen(req, timeout=60) as r:
                return json.load(r)
        except Exception as e:  # retry transient failures
            time.sleep(2 + attempt * 3)
            last = e
    raise last


def chapter(args):
    ch, lang = args
    out = os.path.join(RAW, f"{ch:03d}_{lang}.json")
    if os.path.exists(out):
        return
    verses, page = [], 1
    while True:
        d = get(f"https://api.quran.com/api/v4/verses/by_chapter/{ch}?words=true&word_fields=text_indopak,text_uthmani"
                f"&language={lang}&per_page=50&page={page}")
        verses += d["verses"]
        nxt = d["pagination"]["next_page"]
        if not nxt:
            break
        page = nxt
    with open(out, "w", encoding="utf-8") as f:
        json.dump(verses, f, ensure_ascii=False)


def extras():
    kanz = os.path.join(RAW, "kanzuliman.json")
    if not os.path.exists(kanz):
        with open(kanz, "w", encoding="utf-8") as f:
            json.dump(get("https://api.alquran.cloud/v1/quran/ur.kanzuliman"), f, ensure_ascii=False)
    os.makedirs(os.path.join(RAW, "audio"), exist_ok=True)
    for ch in range(1, 115):  # Alafasy murattal surah audio with per-word timings
        out = os.path.join(RAW, "audio", f"{ch:03d}.json")
        if not os.path.exists(out):
            with open(out, "w", encoding="utf-8") as f:
                json.dump(get(f"https://api.qurancdn.com/api/qdc/audio/reciters/7/audio_files?chapter={ch}&segments=true"), f, ensure_ascii=False)
    morph = os.path.join(RAW, "morphology.txt")
    if not os.path.exists(morph):
        req = urllib.request.Request("https://raw.githubusercontent.com/mustafa0x/quran-morphology/master/quran-morphology.txt")
        with urllib.request.urlopen(req, timeout=120) as r, open(morph, "wb") as f:
            f.write(r.read())


if __name__ == "__main__":
    extras()
    jobs = [(c, l) for c in range(1, 115) for l in LANGS]
    with ThreadPoolExecutor(8) as ex:
        for i, _ in enumerate(ex.map(chapter, jobs)):
            if i % 40 == 0:
                print(i, "/", len(jobs), flush=True)
    print("done")

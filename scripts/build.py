"""Builds the compact offline word-by-word Quran assets from the cached raw downloads.

Output (app/src/main/assets/wbw/):
  001.json … 114.json   {"t": [ayah translation (Kanz-ul-Iman, Urdu)], "w": [[[ar, tr, en, ur, hi, bn, root, lemma], …] per ayah],
                         "a": [[from_ms, to_ms, [start_ms, end_ms per word (-1 when untimed)]] per ayah]}  (Alafasy murattal timings)
  index.json            {"audio": [[[mirror url, original url], bytes] per surah], "juz": [[surah, ayah] start of each juz], "roots": {root: [count, [[ar, en, ur, hi, bn, "s:a"], …]]},
                         "lemmas": {lemma: count}}
"""
import collections, json, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
RAW = os.path.join(HERE, "raw")
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
LANGS = ["en", "ur", "hi", "bn"]
# The app's own copy of the recitation (GitHub release), tried before the original host.
MIRROR = "https://github.com/Muhammad8866/deen-companion-quran/releases/download/wbw-audio-v1/{:03d}.mp3"
# Exact sizes of the mirrored files, written by get_audio.py (the API's file_size is only approximate).
MANIFEST = os.path.join(HERE, "audio_manifest.json")
sizes = {int(k): v[0] for k, v in json.load(open(MANIFEST)).items()} if os.path.exists(MANIFEST) else {}

morph = collections.defaultdict(dict)  # (s, a) -> {word: (root, lemma)}
for line in open(os.path.join(RAW, "morphology.txt"), encoding="utf-8"):
    loc, _form, _tag, feats = line.rstrip("\n").split("\t")
    s, a, w, _seg = map(int, loc.split(":"))
    root = lemma = None
    for f in feats.split("|"):
        if f.startswith("ROOT:"): root = f[5:]
        elif f.startswith("LEM:"): lemma = f[4:]
    cur = morph[(s, a)].get(w, ("", ""))
    # The stem segment carries the root; prefixes only carry a lemma.
    morph[(s, a)][w] = (root or cur[0], lemma if (root or not cur[1]) and lemma else cur[1])

kanz = {(s["number"], x["numberInSurah"]): x["text"] for s in json.load(open(os.path.join(RAW, "kanzuliman.json"), encoding="utf-8"))["data"]["surahs"] for x in s["ayahs"]}

clean = lambda t: " ".join((t or "").split())
ARABIC_JUNK = dict.fromkeys(map(ord, "  ​﻿‏"), None)
root_count, lemma_count = collections.Counter(), collections.Counter()
root_examples = collections.defaultdict(dict)  # root -> lemma -> example
juz_start = {}
audio_meta = []

for ch in range(1, 115):
    per_lang = {l: json.load(open(os.path.join(RAW, f"{ch:03d}_{l}.json"), encoding="utf-8")) for l in LANGS}
    base = per_lang["en"]
    texts, ayahs, timings = [], [], []
    audio_file = json.load(open(os.path.join(RAW, "audio", f"{ch:03d}.json"), encoding="utf-8"))["audio_files"][0]
    audio_meta.append([[MIRROR.format(ch), audio_file["audio_url"]], sizes.get(ch, int(audio_file["file_size"]))])
    verse_timings = audio_file["verse_timings"]
    for vi, verse in enumerate(base):
        s, a = map(int, verse["verse_key"].split(":"))
        juz_start.setdefault(verse["juz_number"], [s, a])
        glosses = {l: [x for x in per_lang[l][vi]["words"] if x["char_type_name"] == "word"] for l in LANGS}
        words = []
        for i, w in enumerate(glosses["en"]):
            root, lemma = morph[(s, a)].get(i + 1, ("", ""))
            en = clean(w["translation"]["text"])
            g = {l: clean((glosses[l][i].get("translation") or {}).get("text")) or en for l in LANGS}
            words.append([w["text_indopak"].translate(ARABIC_JUNK).strip(), clean(w["transliteration"]["text"]), g["en"], g["ur"], g["hi"], g["bn"], root, lemma])
            if root:
                root_count[root] += 1
                root_examples[root].setdefault(lemma, [w["text_indopak"].translate(ARABIC_JUNK).strip(), g["en"], g["ur"], g["hi"], g["bn"], f"{s}:{a}"])
            if lemma:
                lemma_count[lemma] += 1
        vt = verse_timings[vi]
        assert vt["verse_key"] == verse["verse_key"]
        seg = {}
        for x in vt["segments"]:
            if len(x) == 3: seg.setdefault(x[0], (x[1], x[2]))
        timings.append([vt["timestamp_from"], vt["timestamp_to"], [t for i in range(1, len(words) + 1) for t in seg.get(i, (-1, -1))]])
        texts.append(kanz[(s, a)])
        ayahs.append(words)
    with open(os.path.join(OUT, f"{ch:03d}.json"), "w", encoding="utf-8") as f:
        json.dump({"t": texts, "w": ayahs, "a": timings}, f, ensure_ascii=False, separators=(",", ":"))

index = {
    "audio": audio_meta,
    "juz": [juz_start[j] for j in range(1, 31)],
    "roots": {r: [root_count[r], list(root_examples[r].values())[:8]] for r in root_count},
    "lemmas": dict(lemma_count),
}
with open(os.path.join(OUT, "index.json"), "w", encoding="utf-8") as f:
    json.dump(index, f, ensure_ascii=False, separators=(",", ":"))
print("words", sum(root_count.values()), "roots", len(root_count), "lemmas", len(lemma_count))

"""Builds app/src/main/assets/qaidah/lessons.json.

Letter and harakat drills are generated from the alphabet. Word drills are real Quran words picked
from the bundled word-by-word data (assets/wbw), so their Arabic is copied exactly and each word can
play its Quran.com word recitation by its surah:ayah:word reference.

Run: python -I tools/build_qaidah.py
"""
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
WBW = ROOT / "app/src/main/assets/wbw"
OUT = ROOT / "app/src/main/assets/qaidah/lessons.json"

LETTERS = [
    ("ا", "Alif"), ("ب", "Ba"), ("ت", "Ta"), ("ث", "Tha"), ("ج", "Jeem"), ("ح", "Ha"), ("خ", "Kha"),
    ("د", "Dal"), ("ذ", "Dhal"), ("ر", "Ra"), ("ز", "Zay"), ("س", "Seen"), ("ش", "Sheen"), ("ص", "Sad"),
    ("ض", "Dad"), ("ط", "Taa"), ("ظ", "Zaa"), ("ع", "Ain"), ("غ", "Ghain"), ("ف", "Fa"), ("ق", "Qaf"),
    ("ك", "Kaf"), ("ل", "Lam"), ("م", "Meem"), ("ن", "Noon"), ("و", "Waw"), ("ه", "Haa"), ("ء", "Hamzah"),
    ("ي", "Ya"),
]
# Letters that join to the following letter.
JOINING = [c for c, _ in LETTERS if c not in "ادذرزوء"]
TATWEEL = "ـ"
FATHA, KASRA, DAMMA = "َ", "ِ", "ُ"
FATHATAN, KASRATAN, DAMMATAN = "ً", "ٍ", "ٌ"
SHADDA, SUKOON = "ّ", "ۡ"
DAGGER_ALIF, SUBSCRIPT_ALIF, INVERTED_DAMMA = "ٰ", "ٖ", "ٗ"
MUQATTAAT = ["2:1:1", "7:1:1", "10:1:1", "13:1:1", "19:1:1", "20:1:1", "26:1:1", "27:1:1",
             "36:1:1", "38:1:1", "40:1:1", "42:2:1", "50:1:1", "68:1:1"]

ARABIC_LETTER = "[ء-يٱ]"
SHORT_VOWEL = f"[{FATHA}{KASRA}{DAMMA}]"
MARKS_TO_IGNORE = re.compile("[ۖ-۠ۢ-ۭٕٓٔ]")
# A sukoon on any letter except a madd or leen waw/ya.
PLAIN_SUKOON = f"(?<![ويى]){SUKOON}"


def load_words():
    words = []
    for n in range(1, 115):
        data = json.loads((WBW / f"{n:03d}.json").read_text(encoding="utf-8"))
        for a, ayah in enumerate(data["w"], 1):
            for w, row in enumerate(ayah, 1):
                words.append((f"{n}:{a}:{w}", row[0], row[1]))
    return words


def core(word):
    return MARKS_TO_IGNORE.sub("", word)


def every_letter_vowelled(text, allowed):
    """Every letter carries exactly one mark from [allowed] (the last may be bare or sakin)."""
    pattern = f"(?:{ARABIC_LETTER}[{allowed}])+"
    return re.fullmatch(pattern, text) is not None


def pick(words, test, count=16, min_len=2, max_len=5):
    seen, out = set(), []
    for ref, ar, tr in words:
        c = core(ar)
        letters = len(re.findall(ARABIC_LETTER, c))
        if not (min_len <= letters <= max_len) or c in seen or not test(c):
            continue
        seen.add(c)
        out.append({"ar": ar, "tr": tr, "ref": ref})
        if len(out) == count:
            break
    return out


def lessons():
    words = load_words()
    by_ref = {ref: (ar, tr) for ref, ar, tr in words}
    letter = lambda c, name: {"ar": c, "tr": name}
    vowels = [(FATHA, "a"), (KASRA, "i"), (DAMMA, "u")]
    tanween = [(FATHATAN, "an"), (KASRATAN, "in"), (DAMMATAN, "un")]
    # Alif never carries a harakah; hamzah stands in for it.
    plain = [(c, n) for c, n in LETTERS if c != "ا"]

    def forms(c):
        if c in JOINING:
            return [c + TATWEEL, TATWEEL + c + TATWEEL, TATWEEL + c]
        return [TATWEEL + c]

    return [
        {"id": "letters", "title": "Single letters", "note": "The Arabic alphabet has 29 letters. Learn each letter's shape and name, and pronounce it from its correct point of articulation.",
         "items": [letter(c, n) for c, n in LETTERS]},
        {"id": "joined", "title": "Joined letter forms", "note": "Most letters change shape when joined: beginning, middle and end forms. Six letters never join to the letter after them.",
         "items": [{"ar": "  ".join(forms(c)), "tr": n} for c, n in LETTERS if c not in "ء"]},
        {"id": "muqattaat", "title": "Huroof Muqatta'at", "note": "Some surahs open with disjoined letters, read by their letter names. Tap a word to hear it recited from the Quran.",
         "items": [{"ar": by_ref[r][0], "tr": by_ref[r][1], "ref": r} for r in MUQATTAAT]},
        {"id": "harakat", "title": "Harakat: Fatha, Kasra, Damma", "note": "Fatha gives an 'a' sound, kasra an 'i' sound and damma a 'u' sound. Read each one short, without stretching it.",
         "items": [{"ar": c + v, "tr": n[0].lower() + s if c != "ء" else s} for c, n in plain for v, s in vowels]},
        {"id": "harakat_words", "title": "Words with harakat", "note": "Read these Quran words letter by letter with their short vowels, then join them smoothly.",
         "items": pick(words, lambda c: every_letter_vowelled(c, FATHA + KASRA + DAMMA), min_len=3)},
        {"id": "tanween", "title": "Tanween", "note": "Tanween is a doubled harakah that adds a 'n' sound at the end: an, in, un.",
         "items": [{"ar": c + v, "tr": n[0].lower() + s if c != "ء" else s} for c, n in plain for v, s in tanween]},
        {"id": "tanween_words", "title": "Words with tanween", "note": "Quran words that end in tanween. Notice the 'n' sound at the end of each word.",
         "items": pick(words, lambda c: re.search(f"[{FATHATAN}{KASRATAN}{DAMMATAN}]", c) and
                       every_letter_vowelled(c.replace("ا", "").replace("ى", ""), FATHA + KASRA + DAMMA + FATHATAN + KASRATAN + DAMMATAN))},
        {"id": "khari", "title": "Standing harakat", "note": "A standing fatha, standing kasra or inverted damma is read long, like a madd, for two counts.",
         "items": pick(words, lambda c: any(m in c for m in (DAGGER_ALIF, SUBSCRIPT_ALIF, INVERTED_DAMMA)) and SHADDA not in c, max_len=4)},
        {"id": "madd", "title": "Huroof Madd", "note": "Alif after fatha, waw sakin after damma and ya sakin after kasra stretch the sound for two counts.",
         "items": pick(words, lambda c: re.search(f"{FATHA}ا|{DAMMA}و{SUKOON}?|{KASRA}[يى]{SUKOON}?", c)
                       and SHADDA not in c and not re.search(PLAIN_SUKOON, c) and not re.search(f"{FATHA}[وي]{SUKOON}", c))},
        {"id": "leen", "title": "Huroof Leen", "note": "Waw or ya with sukoon after a fatha is read softly and gently, without stretching.",
         "items": pick(words, lambda c: re.search(f"{FATHA}[وي]{SUKOON}", c) and SHADDA not in c and not re.search(PLAIN_SUKOON, c))},
        {"id": "sukoon", "title": "Sukoon (Jazm)", "note": "A letter with sukoon (jazm) has no vowel; join it to the letter before it.",
         "items": pick(words, lambda c: re.search(PLAIN_SUKOON, c) and SHADDA not in c and not re.search(f"{FATHA}[وي]{SUKOON}", c), max_len=4)},
        {"id": "shaddah", "title": "Shaddah (Tashdeed)", "note": "A letter with shaddah is read twice: once with sukoon, then again with its harakah.",
         "items": pick(words, lambda c: SHADDA in c and not re.search(f"[نم]{SHADDA}", c), max_len=4)},
        {"id": "ghunnah", "title": "Ghunnah: Noon and Meem with Shaddah", "note": "Noon or meem with shaddah is read with ghunnah, a nasal sound held for about two counts.",
         "items": pick(words, lambda c: re.search(f"[نم]{SHADDA}", c), max_len=5)},
    ]


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    data = {
        "audio": "Word drills stream the Quran.com word-by-word recitation for each surah:ayah:word ref, "
                 "the same source the app's Quran word-by-word reader uses. Letter and harakat drills have no audio.",
        "lessons": lessons(),
    }
    OUT.write_text(json.dumps(data, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    for lesson in data["lessons"]:
        print(lesson["id"], len(lesson["items"]))


if __name__ == "__main__":
    main()

"""Builds the search index that lets the app open a Hadith or Islamic book PDF at the page a query is about.

    python tools/book_search/build_index.py extract <folder of mirror PDFs>...
    python tools/book_search/build_index.py build --out <output folder>

`extract` reads the PDFs once and keeps their chapters and page text in tools/book_search/.cache,
so `build` (needed after every change to concepts.txt) runs without the PDFs. The PDFs must carry
their mirror asset names (ar-bukhari-v1.pdf, en-bahar-e-shariat-v2.pdf, ...), because the app maps
an indexed book back to its catalog entry by that name. `build` writes book-search.db.gz and
book-search-version.txt, which are uploaded to the `search-v1` release of
Muhammad8866/deen-companion-hadith-pdfs, and refreshes the test vectors the app's unit test reads.

No OCR and no machine learning is involved: a page is indexed only when the PDF already carries
readable text, and a chapter only when the PDF has real bookmarks. Scanned books stay unindexed.
`normalize`, `romanize` and `sound_key` must stay identical to BookSearchText.kt in the app.
"""
import argparse
import gzip
import json
import math
import re
import shutil
import sqlite3
import time
import unicodedata
import zlib
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent
CONCEPTS = HERE / "concepts.txt"
CACHE = HERE / ".cache" / "extracted"
VECTORS = HERE.parents[1] / "app/src/test/resources/book_search_vectors.tsv"
FORMAT = 2
MAX_PHRASE = 6
HEADING_SCORE = 3000

CHAR_MAP = str.maketrans({
    "ى": "ي", "ی": "ي", "ے": "ي", "ک": "ك", "ہ": "ه", "ھ": "ه", "ة": "ه", "ۃ": "ه",
})
DROPPED = {0x640, 0x670, 0x93C, 0x200C, 0x200D, 0x27, 0x60, 0x2018, 0x2019, 0x2BC}
ARTICLE_PREFIXES = ("وال", "بال", "فال", "كال")

# Filler words of a question in the languages people type in. Never indexed, never searched.
STOPWORD_TEXT = """
a an the of and or to in on at by for from with as is are was were be been am it its this that these those
he she him his her they them their we us our you your i me my who whom which what when where why how
not no do does did done has have had will would shall should can could may might must if then than so
but about into over under up down out off there here also very any all some one said says say
ka ki ke ko se me mein main par pe hai hain tha thi the ho hota hoti hote kya kab kahan kaun kaise kese
kyun kyu aur ya bhi hi to tak liye wala wali wale kon konsa konsi konse kis kisi jo jab agar nahi nhi na
kar karna karne karte kare karen hum ap aap tum yeh ye woh wo ek batao bataiye btao chahiye
padhe padhen padhna padhte parhe parhna tarika tariqa tareeqa hukm bare baare
का की के को से में पर है हैं था थी थे क्या कब कहाँ कौन कैसे क्यों और या भी ही तो तक लिए जो जब अगर नहीं
કે કો થી માં પર છે છો શું ક્યારે ક્યાં કોણ કેવી કેમ અને પણ તો માટે જો નથી ની નો ના નું
কি কী কে কেন কখন কোথায় কিভাবে এবং ও বা না জন্য এর
کا کی کے کو سے میں پر ہے ہیں تھا تھی تھے کیا کب کہاں کون کیسے کیوں اور یا بھی ہی تو تک لیے جو جب اگر نہیں
في من على الى عن ما هل هو هي ان او
"""


def _dropped(cp: int) -> bool:
    return 0x300 <= cp <= 0x36F or 0x64B <= cp <= 0x65F or 0x6D6 <= cp <= 0x6ED or cp in DROPPED


def _strip_article(token: str) -> str:
    if len(token) > 5 and token.startswith(ARTICLE_PREFIXES):
        return token[3:]
    if len(token) > 4 and token.startswith(("ال", "لل")):
        return token[2:]
    return token


def normalize(text: str) -> list[str]:
    """Lower-cased words with accents, Arabic vowel marks and the Arabic article removed."""
    out = []
    for ch in unicodedata.normalize("NFD", text.lower()):
        if _dropped(ord(ch)):
            continue
        ch = ch.translate(CHAR_MAP)
        category = unicodedata.category(ch)
        out.append(ch if category[0] in "LM" or category == "Nd" else " ")
    return [_strip_article(token) for token in "".join(out).split()]


# The Indic scripts share one layout, each in its own 128-character block, so one table read by
# offset within the block covers Devanagari, Bengali, Gurmukhi, Gujarati, Odia, Tamil, Telugu,
# Kannada and Malayalam.
INDIC_FIRST, INDIC_LAST = 0x900, 0xD7F
INDIC_CONSONANTS = {
    0x15: "k", 0x16: "kh", 0x17: "g", 0x18: "gh", 0x19: "ng", 0x1A: "ch", 0x1B: "chh", 0x1C: "j", 0x1D: "jh",
    0x1E: "ny", 0x1F: "t", 0x20: "t", 0x21: "d", 0x22: "d", 0x23: "n", 0x24: "t", 0x25: "t", 0x26: "d",
    0x27: "d", 0x28: "n", 0x29: "n", 0x2A: "p", 0x2B: "ph", 0x2C: "b", 0x2D: "bh", 0x2E: "m", 0x2F: "y",
    0x30: "r", 0x31: "r", 0x32: "l", 0x33: "l", 0x34: "l", 0x35: "v", 0x36: "sh", 0x37: "sh", 0x38: "s",
    0x39: "h", 0x5C: "r", 0x5D: "r", 0x5F: "y",
}
INDIC_VOWELS = {
    0x05: "a", 0x06: "aa", 0x07: "i", 0x08: "ee", 0x09: "u", 0x0A: "oo", 0x0B: "ri", 0x0D: "e", 0x0E: "e",
    0x0F: "e", 0x10: "ai", 0x11: "o", 0x12: "o", 0x13: "o", 0x14: "au",
}
INDIC_SIGNS = {
    0x3E: "aa", 0x3F: "i", 0x40: "ee", 0x41: "u", 0x42: "oo", 0x43: "ri", 0x45: "e", 0x46: "e", 0x47: "e",
    0x48: "ai", 0x49: "o", 0x4A: "o", 0x4B: "o", 0x4C: "au",
}
INDIC_VIRAMA, INDIC_NUKTA = 0x4D, 0x3C
INDIC_NASALS = {0x01: "n", 0x02: "n", 0x03: "h"}


def _is_indic(ch: str) -> bool:
    return INDIC_FIRST <= ord(ch) <= INDIC_LAST


def romanize(token: str) -> str:
    """Roman spelling of a word in an Indic script, or the word unchanged when it is not one."""
    if not all(_is_indic(ch) for ch in token):
        return token
    # Syllables as [consonant, vowel, vowel is the unwritten "a"].
    units = []
    for ch in token:
        offset = ord(ch) & 0x7F
        if offset in INDIC_CONSONANTS:
            units.append([INDIC_CONSONANTS[offset], "a", True])
        elif offset in INDIC_VOWELS:
            units.append(["", INDIC_VOWELS[offset], False])
        elif offset in INDIC_SIGNS:
            if units:
                units[-1][1:] = [INDIC_SIGNS[offset], False]
        elif offset == INDIC_VIRAMA:
            if units:
                units[-1][1:] = ["", False]
        elif offset in INDIC_NASALS:
            units.append([INDIC_NASALS[offset], "", False])
        elif offset != INDIC_NUKTA:
            return token
    if not units:
        return token
    # The unwritten "a" is silent at the end of a word and between two voiced syllables
    # (रमज़ान is ramzan, not ramazana); deciding from the end backwards keeps मुसलमान "musalman".
    if len(units) > 1 and units[-1][2]:
        units[-1][1:] = ["", False]
    for index in range(len(units) - 2, 0, -1):
        if units[index][2] and units[index - 1][1] and units[index + 1][1]:
            units[index][1:] = ["", False]
    return "".join(consonant + vowel for consonant, vowel, _ in units)


def sound_key(token: str) -> str:
    """
    Folds the spellings of one word together: namaz, namaaz, nemaz, नमाज़, નમાઝ and নামাজ all give
    "namaj". Words in other scripts (Arabic, Urdu) pass through unchanged.
    """
    word = romanize(token)
    if not (word.isascii() and word.isalpha()):
        return token
    word = word.replace("ph", "f").replace("dh", "z").replace("th", "s").replace("ck", "k")
    word = word.replace("ee", "i").replace("oo", "u").replace("q", "k").replace("w", "v").replace("z", "j")
    word = re.sub(r"c(?!h)", "k", word)
    word = re.sub(r"(?<=[^aeiou])h", "", word)
    if len(word) > 2 and word.endswith("h"):
        word = word[:-1]
    word = word.translate(str.maketrans("eyo", "aiu"))
    return re.sub(r"(.)\1+", r"\1", word)


STOPWORDS = frozenset(normalize(STOPWORD_TEXT))


def keys_of(text: str) -> list[str]:
    return [sound_key(token) for token in normalize(text)]


def load_concepts():
    """Returns concept labels and a phrase table: first key -> [(key tuple, concept ids)], longest first."""
    labels, phrases = [], {}
    for line in CONCEPTS.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        _name, _, terms = line.partition(":")
        terms = [term.strip() for term in terms.split(",") if term.strip()]
        concept = len(labels)
        labels.append(terms[0])
        for term in terms:
            keys = tuple(keys_of(term))
            if keys and len(keys) <= MAX_PHRASE:
                phrases.setdefault(keys, set()).add(concept)
    table = {}
    for keys, concepts in phrases.items():
        table.setdefault(keys[0], []).append((keys, sorted(concepts)))
    for entries in table.values():
        entries.sort(key=lambda entry: -len(entry[0]))
    return labels, phrases, table


def count_concepts(keys: list[str], table) -> tuple[Counter, set]:
    """How often each concept occurs, and which concepts occur within the page's running header."""
    counts, early = Counter(), set()
    index = 0
    while index < len(keys):
        entries = table.get(keys[index])
        if entries is None and keys[index].endswith("s"):
            entries = [e for e in table.get(keys[index][:-1], []) if len(e[0]) == 1]
        step = 1
        for phrase, concepts in entries or ():
            if len(phrase) == 1 or tuple(keys[index:index + len(phrase)]) == phrase:
                counts.update(concepts)
                if index < 18:
                    early.update(concepts)
                step = len(phrase)
                break
        index += step
    return counts, early


# Bookmarks that only name a scanned page or a hadith number say nothing about the topic.
JUNK_TITLE = re.compile(
    r"_Page\s*\d+(-\d+)?$|^Page\s*\d+$|\bNumber\s+\d+|^Blank Page$|\.(pdf|jpe?g|png)$|^\d+[A-Za-z]?$|^[\W\d_]+$",
    re.IGNORECASE
)


def headings(doc) -> list[tuple[int, str]]:
    found, seen = [], set()
    for _level, title, page in doc.get_toc():
        title = " ".join(title.split())
        if len(title) < 4 or JUNK_TITLE.search(title) or not 1 <= page <= doc.page_count:
            continue
        if (title, page) not in seen:
            seen.add((title, page))
            found.append((page - 1, title))
    return found


def readable(asset: str, text: str) -> bool:
    """
    True for real page text. Only the English editions have one: the Arabic and some Hindi PDFs
    carry a text layer in a legacy font encoding that extracts as gibberish.
    """
    return asset.startswith("en-") and sum(ch.isalpha() for ch in text) >= 150


def extract(folders: list[Path]):
    import pymupdf

    CACHE.mkdir(parents=True, exist_ok=True)
    for path in sorted(path for folder in folders for path in folder.glob("*.pdf")):
        pdf = pymupdf.open(path)
        chapters = headings(pdf)
        pages = []
        for page in range(pdf.page_count):
            text = " ".join(pdf[page].get_text("text").split())
            if readable(path.stem, text):
                pages.append((page, text))
        target = CACHE / f"{path.stem}.json.gz"
        if chapters or pages:
            with gzip.open(target, "wt", encoding="utf-8") as out:
                json.dump({"asset": path.stem, "chapters": chapters, "pages": pages}, out, ensure_ascii=False)
        else:
            target.unlink(missing_ok=True)
        print(f"{path.stem}: {pdf.page_count} pages, {len(chapters)} chapters, {len(pages)} text pages", flush=True)
        pdf.close()


def searchable(text: str) -> str:
    """Sound keys of the words worth a full-text entry; leaving out filler words keeps the download small."""
    return " ".join(sound_key(token) for token in normalize(text) if len(token) > 1 and token not in STOPWORDS)


def write_vectors(phrases):
    """Inputs with the folding this script gives them; the app's unit test must reproduce every line."""
    samples = {
        "Kitāb al-Īmān, Qur'an!", "كِتَابُ الْوُضُوءِ", "الصلاة في السفر", "نماز کی فضیلت", "نَماز كي فضيلت",
        "namaz namaaz nemaz", "wuzu wudhu wuzoo", "taraweeh tarawih", "hadith hadees", "roza raza hajj haiz",
        "नमाज़ नमाज वुज़ू रमज़ान मग़रिब जन्नत फ़ज़ीलत तहज्जुद मुसलमान इंसाफ़ ज़ुहर क़ुरआन",
        "નમાઝ ગુસ્લ રોઝા ઝકાત", "নামাজ রোজা যাকাত হজ", "ਨਮਾਜ਼", "நோன்பு", "v2 abc123",
    }
    samples.update(" ".join(keys) for keys in phrases)
    for line in CONCEPTS.read_text(encoding="utf-8").splitlines():
        if line.strip() and not line.startswith("#"):
            samples.update(term.strip() for term in line.partition(":")[2].split(","))
    VECTORS.parent.mkdir(parents=True, exist_ok=True)
    with open(VECTORS, "w", encoding="utf-8", newline="\n") as out:
        for sample in sorted(samples):
            if sample and "\t" not in sample:
                tokens = normalize(sample)
                out.write(f"{sample}\t{' '.join(tokens)}\t{' '.join(map(sound_key, tokens))}\n")


def build(out: Path):
    labels, phrases, table = load_concepts()
    write_vectors(phrases)
    out.mkdir(parents=True, exist_ok=True)
    db_path = out / "book-search.db"
    db_path.unlink(missing_ok=True)
    db = sqlite3.connect(db_path)
    db.executescript("""
        PRAGMA page_size = 4096;
        CREATE TABLE meta(key TEXT PRIMARY KEY, value TEXT);
        CREATE TABLE book(id INTEGER PRIMARY KEY, asset TEXT NOT NULL);
        CREATE TABLE doc(id INTEGER PRIMARY KEY, book INTEGER NOT NULL, page INTEGER NOT NULL, heading TEXT, body BLOB);
        CREATE VIRTUAL TABLE words USING fts4(content="", text, tokenize=simple);
        CREATE TABLE stopword(token TEXT PRIMARY KEY) WITHOUT ROWID;
        CREATE TABLE concept(id INTEGER PRIMARY KEY, label TEXT NOT NULL);
        CREATE TABLE term(key TEXT NOT NULL, concept INTEGER NOT NULL, PRIMARY KEY(key, concept)) WITHOUT ROWID;
        CREATE TABLE hit(concept INTEGER NOT NULL, doc INTEGER NOT NULL, score INTEGER NOT NULL, PRIMARY KEY(concept, doc)) WITHOUT ROWID;
    """)
    db.executemany("INSERT INTO concept VALUES(?, ?)", list(enumerate(labels)))
    for keys, concepts in phrases.items():
        db.executemany("INSERT INTO term VALUES(?, ?)", [(" ".join(keys), concept) for concept in concepts])
    db.executemany("INSERT INTO stopword VALUES(?)", [(token,) for token in sorted(STOPWORDS)])

    doc_id = 0
    frequencies = []  # (concept, doc, weight)
    sources = sorted(CACHE.glob("*.json.gz"))
    if not sources:
        raise SystemExit("Nothing extracted yet: run `build_index.py extract <pdf folders>` first.")
    for book_id, source in enumerate(sources, start=1):
        with gzip.open(source, "rt", encoding="utf-8") as stream:
            book = json.load(stream)
        db.execute("INSERT INTO book VALUES(?, ?)", (book_id, book["asset"]))
        for page, title in book["chapters"]:
            doc_id += 1
            db.execute("INSERT INTO doc VALUES(?, ?, ?, ?, NULL)", (doc_id, book_id, page, title))
            db.execute("INSERT INTO words(docid, text) VALUES(?, ?)", (doc_id, searchable(title)))
            counts, _early = count_concepts(keys_of(title), table)
            # A front-matter bookmark ("Contents", "Bahaar") is no destination for a topic.
            weight = HEADING_SCORE * (0.4 if page < 3 else 1)
            frequencies += [(concept, doc_id, weight) for concept in counts]
        for page, text in book["pages"]:
            doc_id += 1
            db.execute("INSERT INTO doc VALUES(?, ?, ?, NULL, ?)",
                       (doc_id, book_id, page, zlib.compress(text.encode("utf-8"), 9)))
            db.execute("INSERT INTO words(docid, text) VALUES(?, ?)", (doc_id, searchable(text)))
            counts, early = count_concepts(keys_of(text), table)
            # A table-of-contents page names every topic once and explains none of them.
            listing = 0.3 if text[:40].lower().startswith("contents") or text.count("....") > 12 else 1
            for concept, tf in counts.items():
                # The running header names the book a page belongs to, a strong sign of its topic.
                weight = 1000 * tf / (tf + 2) * (1.5 if concept in early else 1) * listing
                frequencies.append((concept, doc_id, weight))
        print(f"{book['asset']}: {len(book['chapters'])} chapters, {len(book['pages'])} text pages", flush=True)

    # Rarer concepts weigh more, so "safar + namaz" ranks pages about travel above pages about prayer.
    spread = Counter(concept for concept, _doc, _weight in frequencies)
    for concept, doc, weight in frequencies:
        idf = math.log(1 + doc_id / spread[concept])
        db.execute("INSERT INTO hit VALUES(?, ?, ?)", (concept, doc, max(1, round(weight * idf))))

    version = str(int(time.time()))
    db.executemany("INSERT INTO meta VALUES(?, ?)",
                   [("version", version), ("format", str(FORMAT)), ("docs", str(doc_id))])
    db.execute("INSERT INTO words(words) VALUES('optimize')")
    db.commit()
    db.execute("VACUUM")
    db.close()

    with open(db_path, "rb") as source, gzip.open(out / "book-search.db.gz", "wb", compresslevel=9) as target:
        shutil.copyfileobj(source, target)
    (out / "book-search-version.txt").write_text(version, encoding="utf-8")
    unused = [labels[c] for c in range(len(labels)) if c not in spread]
    print(f"{len(sources)} books, {doc_id} entries, {len(labels)} concepts, version {version}")
    print(f"db {db_path.stat().st_size / 1e6:.1f} MB, gz {(out / 'book-search.db.gz').stat().st_size / 1e6:.1f} MB")
    if unused:
        print("concepts found on no page:", ", ".join(unused))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    commands = parser.add_subparsers(dest="command", required=True)
    extractor = commands.add_parser("extract")
    extractor.add_argument("folders", nargs="+", type=Path)
    builder = commands.add_parser("build")
    builder.add_argument("--out", required=True, type=Path)
    arguments = parser.parse_args()
    if arguments.command == "extract":
        extract(arguments.folders)
    else:
        build(arguments.out)

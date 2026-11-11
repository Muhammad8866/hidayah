"""Build the bundled seed of Dawat-e-Islami's library list for the World Library.

Writes app/src/main/assets/world_library/di-index.json in the exact on-disk cache
format that `DawateIslamiIndex.readCache` reads (version 1). The app shows this seed
at once when it has no saved list yet, so the first visit does not wait for the
library's 12 MB answer.

The seed's `updatedAt` is the time of the fetch, so it is always older than the
app's weekly limit: the app treats it as stale and fetches a fresh list in the
background, and only that fresh list may remove saved books.

Every rule below mirrors `DawateIslamiIndex.parseResponse`, `readSource` and
`categories` (app/src/main/java/com/example/hadith/DawateIslamiIndex.kt), so the
seed holds the same books a fresh in-app fetch would store. Change both together.

Rerun before each release, from any directory, with plain Python 3 (stdlib only):

    python -I tools/build_di_index_snapshot.py                 # fetch now
    python -I tools/build_di_index_snapshot.py --input di.json # a saved answer

An answer that is not one complete list is rejected and nothing is written.
"""
import argparse
import json
import math
import os
import re
import sys
import time
import unicodedata
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFAULT_OUTPUT = ROOT / "app/src/main/assets/world_library/di-index.json"

SEARCH_API = "https://www.dawateislami.net/searchapi/book-library/search"
FULL_REQUEST = '{"keyword":"","language":"","categories":[],"pn":1,"ps":10000}'
MAX_BOOKS = 20_000
MAX_BYTES = 24 << 20
MAX_STRING_BYTES = 64 * 1024
MAX_DEPTH = 32
MIN_ACCEPTED_SHARE = 0.99
CACHE_VERSION = 1

LONG_MIN, LONG_MAX = -(1 << 63), (1 << 63) - 1
INT_MIN, INT_MAX = -(1 << 31), (1 << 31) - 1

# Kotlin's Regex runs on java.util.regex: \d and \s are ASCII there.
PUBLICATION_FOLDER = re.compile(r"/Read/[a-z]+/([0-9]{4})/")
LANGUAGE_CODE = re.compile(r"[a-z]{2,3}")
JAVA_WHITESPACE_RUN = re.compile(r"[ \t\n\x0b\f\r]+")
# Kotlin String.toLongOrNull / toIntOrNull: optional sign, then digits (Character.digit, so any Nd digit).
KOTLIN_INTEGER = re.compile(r"[+-]?\d+")
# Java Double.parseDouble (decimal form) as screened by Kotlin's toDoubleOrNull; ASCII digits only.
JAVA_DOUBLE = re.compile(
    r"[+-]?(NaN|Infinity|(([0-9]+\.?[0-9]*|\.[0-9]+)([eE][+-]?[0-9]+)?)[fFdD]?)"
)


class Rejected(Exception):
    """The answer is not one complete, readable list."""


class Num(str):
    """A JSON number token, kept as its literal text (JsonReader.nextString returns that)."""


class Obj(list):
    """A JSON object as its (name, value) pairs in order, so repeated names behave as in JsonReader."""


def _reject_constant(name):
    raise Rejected(f"Non-standard JSON constant {name}")


DECODER = json.JSONDecoder(
    object_pairs_hook=Obj, parse_int=Num, parse_float=Num, parse_constant=_reject_constant
)


def kotlin_is_whitespace(ch: str) -> bool:
    """Char.isWhitespace(): Character.isWhitespace(ch) || Character.isSpaceChar(ch)."""
    return ch in "\t\n\x0b\f\r\x1c\x1d\x1e\x1f" or unicodedata.category(ch) in ("Zs", "Zl", "Zp")


def kotlin_trim(text: str) -> str:
    start, end = 0, len(text)
    while start < end and kotlin_is_whitespace(text[start]):
        start += 1
    while end > start and kotlin_is_whitespace(text[end - 1]):
        end -= 1
    return text[start:end]


def kotlin_is_blank(text: str) -> bool:
    return all(kotlin_is_whitespace(ch) for ch in text)


def kotlin_to_long_or_null(text: str, low: int = LONG_MIN, high: int = LONG_MAX):
    if not KOTLIN_INTEGER.fullmatch(text):
        return None
    value = int(text)
    return value if low <= value <= high else None


def java_double_or_null(text: str):
    if not JAVA_DOUBLE.fullmatch(text):
        return None
    body = text.rstrip("fFdD") if text[-1:] in "fFdD" and "Infinity" not in text and "NaN" not in text else text
    return float(body.replace("Infinity", "inf"))


def java_double_to_long(value: float) -> int:
    """Kotlin Double.toLong(): NaN is 0, out-of-range values saturate."""
    if math.isnan(value):
        return 0
    if value >= LONG_MAX:
        return LONG_MAX
    if value <= LONG_MIN:
        return LONG_MIN
    return int(value)


def is_string(value) -> bool:
    return isinstance(value, str) and not isinstance(value, Num)


def string_or_null(value):
    """JsonReader.stringOrNull(): text and numbers as written, booleans as words, anything else null."""
    if isinstance(value, str):  # a string, or a number's literal text
        return str(value)
    if isinstance(value, bool):
        return "true" if value else "false"
    return None


def long_or_zero(value) -> int:
    """JsonReader.longOrZero(): read as text first, so an odd value counts as zero."""
    if not isinstance(value, str):
        return 0
    text = kotlin_trim(str(value))
    parsed = kotlin_to_long_or_null(text)
    if parsed is not None:
        return parsed
    as_double = java_double_or_null(text)
    return java_double_to_long(as_double) if as_double is not None else 0


def java_next_long(value) -> int:
    """android.util.JsonReader.nextLong(): Long.parseLong, else an exact whole double."""
    if not isinstance(value, str):
        raise Rejected("Expected a number")
    text = str(value)
    parsed = kotlin_to_long_or_null(text)
    if parsed is not None:
        return parsed
    as_double = java_double_or_null(text)
    if as_double is None or math.isnan(as_double) or math.isinf(as_double):
        raise Rejected(f"Not a whole number: {text}")
    result = java_double_to_long(as_double)
    if result != as_double:
        raise Rejected(f"Not a whole number: {text}")
    return result


def clean(text) -> str:
    return kotlin_trim(JAVA_WHITESPACE_RUN.sub(" ", text or ""))


def check_capped(raw: bytes) -> None:
    """Mirrors DawateIslamiIndex.Capped: total size, one text's length and nesting depth."""
    if len(raw) > MAX_BYTES:
        raise Rejected(f"Answer larger than {MAX_BYTES} bytes")
    in_string = escaped = False
    string_bytes = depth = 0
    for byte in raw:
        if in_string:
            if escaped:
                escaped = False
            elif byte == 0x5C:
                escaped = True
            elif byte == 0x22:
                in_string = False
                continue
            string_bytes += 1
            if string_bytes > MAX_STRING_BYTES:
                raise Rejected("A text in the answer is too long")
            continue
        if byte == 0x22:
            in_string, string_bytes = True, 0
        elif byte in (0x7B, 0x5B):
            depth += 1
            if depth > MAX_DEPTH:
                raise Rejected("The answer is nested too deeply")
        elif byte in (0x7D, 0x5D):
            depth -= 1


def read_total(value) -> int:
    if isinstance(value, Num):
        return java_next_long(value)
    if not isinstance(value, Obj):
        raise Rejected("Unexpected total")
    total, relation = -1, "eq"
    for name, item in value:
        if name == "value":
            total = java_next_long(item)
        elif name == "relation":
            relation = string_or_null(item) or ""
    if relation != "eq":
        raise Rejected("The answer gives only a lower bound")
    return total


def read_source(source: Obj):
    """One book, or None when it lacks what is needed to build its PDF address."""
    book_id = file_id = pages = writer = reads = 0
    language = image = slug = title = native = normal = ""
    category_ids = category_names = published = created = ""
    enabled = True
    for name, value in source:
        if value is None:
            continue
        if name == "id":
            book_id = long_or_zero(value)
        elif name == "book_coma":
            file_id = long_or_zero(value)
        elif name == "total_pages":
            pages = min(max(long_or_zero(value), 0), INT_MAX)
        elif name == "writer_id":
            writer = min(max(long_or_zero(value), 0), INT_MAX)
        elif name == "read_count":
            reads = long_or_zero(value)
        elif name == "language_code":
            language = kotlin_trim(string_or_null(value) or "")
        elif name == "img_url":
            image = string_or_null(value) or ""
        elif name == "roman_url":
            slug = kotlin_trim(string_or_null(value) or "")
        elif name == "title":
            title = clean(string_or_null(value))
        elif name == "native_name":
            native = clean(string_or_null(value))
        elif name == "normal_name":
            normal = clean(string_or_null(value))
        elif name == "cat_ids":
            category_ids = string_or_null(value) or ""
        elif name == "category_name":
            category_names = string_or_null(value) or ""
        elif name == "published_date":
            published = string_or_null(value) or ""
        elif name == "created_date":
            created = string_or_null(value) or ""
        elif name == "book_enable":
            enabled = value if isinstance(value, bool) else True
    match = PUBLICATION_FOLDER.search(image)
    year = int(match.group(1)) if match else None
    if (not enabled or book_id <= 0 or file_id <= 0 or year is None or kotlin_is_blank(slug)
            or not LANGUAGE_CODE.fullmatch(language)):
        return None
    ids = [parsed for parsed in (kotlin_to_long_or_null(kotlin_trim(part), INT_MIN, INT_MAX)
                                 for part in category_ids.split(",")) if parsed is not None]
    names = [kotlin_trim(part) for part in category_names.split(",")]
    categories = list(zip(ids, names)) if len(names) == len(ids) else [(i, "") for i in ids]
    shown_title = title or native or normal or slug
    book = {
        "i": book_id, "l": language, "f": file_id, "y": year, "p": pages, "s": slug,
        "t": shown_title, "n": native, "m": normal, "w": writer, "r": reads,
        "d": created if kotlin_is_blank(published) else published,
        "c": list(dict.fromkeys(ids)),
    }
    return book, categories


def read_hit(hit):
    if not isinstance(hit, Obj):
        raise Rejected("A hit is not an object")
    result = None
    for name, value in hit:
        if name == "_source" and isinstance(value, Obj):
            result = read_source(value)
    return result


def parse_response(raw: bytes):
    """Returns (books, categories) exactly as DawateIslamiIndex.parseResponse would keep them."""
    check_capped(raw)
    text = raw.decode("utf-8", errors="replace")
    try:
        answer, _ = DECODER.raw_decode(text, len(text) - len(text.lstrip(" \t\n\r")))
    except json.JSONDecodeError as error:
        raise Rejected(f"Not JSON: {error}") from error
    if not isinstance(answer, Obj):
        raise Rejected("The answer is not an object")
    total, hits = -1, 0
    books = {}
    names = {}
    for name, value in answer:
        if name == "total":
            total = read_total(value)
        elif name == "hits":
            if not isinstance(value, list) or isinstance(value, Obj):
                raise Rejected("hits is not a list")
            for hit in value:
                hits += 1
                if hits > MAX_BOOKS:
                    raise Rejected("Too many books in the answer")
                found = read_hit(hit)
                if found is None:
                    continue
                book, categories = found
                if book["i"] in books:
                    raise Rejected(f"Book {book['i']} listed twice")
                books[book["i"]] = book
                for category_id, category_name in categories:
                    if not kotlin_is_blank(category_name):
                        names.setdefault(category_id, category_name)
    if total <= 0 or total > MAX_BOOKS or hits != total:
        raise Rejected(f"Incomplete answer: {hits} of {total} books")
    if len(books) < math.ceil(hits * MIN_ACCEPTED_SHARE):
        raise Rejected(f"Only {len(books)} of {hits} books could be read")
    counts = {}
    for book in books.values():
        for category_id in book["c"]:
            counts[category_id] = counts.get(category_id, 0) + 1
    categories = sorted(
        ({"id": i, "name": names.get(i, ""), "count": n} for i, n in counts.items()),
        key=lambda c: (-c["count"], c["id"]),
    )
    return list(books.values()), categories


def fetch() -> bytes:
    request = urllib.request.Request(
        SEARCH_API, data=FULL_REQUEST.encode("utf-8"), method="POST",
        headers={"Accept": "application/json", "Content-Type": "application/json"},
    )
    with urllib.request.urlopen(request, timeout=120) as response:
        if response.status != 200:
            raise Rejected(f"HTTP {response.status}")
        length = response.headers.get("Content-Length")
        if length and length.isdigit() and int(length) > MAX_BYTES:
            raise Rejected("Response too large")
        body = response.read(MAX_BYTES + 1)
    if len(body) > MAX_BYTES:
        raise Rejected("Response too large")
    return body


def write_snapshot(output: Path, updated_at: int, books, categories) -> int:
    snapshot = {"version": CACHE_VERSION, "updatedAt": updated_at, "categories": categories, "books": books}
    # Kotlin's UTF-8 writer turns an unpaired surrogate into '?'; errors="replace" does the same.
    data = json.dumps(snapshot, ensure_ascii=False, separators=(",", ":")).encode("utf-8", errors="replace")
    output.parent.mkdir(parents=True, exist_ok=True)
    temporary = output.with_name(output.name + ".tmp")
    temporary.write_bytes(data)
    os.replace(temporary, output)
    return len(data)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--input", type=Path, help="a saved search answer instead of fetching one")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--updated-at", type=int, help="ms since epoch (default: now, or the input's mtime)")
    args = parser.parse_args()
    try:
        if args.input:
            if args.input.stat().st_size > MAX_BYTES:
                raise Rejected("Answer too large")
            raw = args.input.read_bytes()
            updated_at = args.updated_at if args.updated_at is not None else int(args.input.stat().st_mtime * 1000)
        else:
            raw = fetch()
            updated_at = args.updated_at if args.updated_at is not None else int(time.time() * 1000)
        books, categories = parse_response(raw)
    except (Rejected, OSError) as error:
        print(f"Not written: {error}", file=sys.stderr)
        return 1
    size = write_snapshot(args.output, updated_at, books, categories)
    print(f"{len(books)} books, {len(categories)} categories, {size} bytes, updatedAt {updated_at} -> {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())

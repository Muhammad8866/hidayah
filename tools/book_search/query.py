"""Runs queries against a built index the same way the app does (BookSearchIndex.kt).

    python tools/book_search/query.py <book-search.db> "safar me namaz" [asset prefix, e.g. en-]
    python tools/book_search/query.py <book-search.db> --check

`--check` runs every line of eval_queries.tsv and reports the ones whose expected text is not in
the first results; run it after changing concepts.txt or the ranking, before uploading an index.
"""
import io
import sqlite3
import sys
import zlib
from collections import defaultdict
from pathlib import Path

from build_index import MAX_PHRASE, normalize, sound_key

EVAL = Path(__file__).with_name("eval_queries.tsv")
ARABIC_SCRIPT_BOOKS = ("ar-", "ur-", "fa-")


def edit_distance(a: str, b: str) -> int:
    previous, before = list(range(len(b) + 1)), None
    for i in range(1, len(a) + 1):
        current = [i] + [0] * len(b)
        for j in range(1, len(b) + 1):
            value = min(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + (a[i - 1] != b[j - 1]))
            if i > 1 and j > 1 and a[i - 1] == b[j - 2] and a[i - 2] == b[j - 1]:
                value = min(value, before[j - 2] + 1)
            current[j] = value
        before, previous = previous, current
    return previous[len(b)]


def concepts_of(db, keys):
    marks = ",".join("?" * len(keys))
    return [row[0] for row in db.execute(f"SELECT DISTINCT concept FROM term WHERE key IN ({marks})", keys)]


def corrected(db, key):
    if len(key) < 4 or db.execute("SELECT 1 FROM words WHERE words MATCH ? LIMIT 1", (key + "*",)).fetchone():
        return None
    limit = 1 if len(key) <= 6 else 2
    best, matches = limit + 1, []
    for (entry,) in db.execute("SELECT DISTINCT key FROM term WHERE key NOT LIKE '% %'"):
        if entry[0] != key[0] or abs(len(entry) - len(key)) > limit:
            continue
        distance = edit_distance(key, entry)
        if distance < best:
            best, matches = distance, [entry]
        elif distance == best and distance <= limit:
            matches.append(entry)
    return concepts_of(db, matches) if best <= limit else None


def parse(db, query):
    """Parts of the question: (concept ids, None) or (None, sound of a word)."""
    stopwords = {row[0] for row in db.execute("SELECT token FROM stopword")}
    tokens = normalize(query)
    keys = [sound_key(token) for token in tokens]
    parts, index = [], 0
    while index < len(tokens):
        for size in range(min(MAX_PHRASE, len(tokens) - index), 0, -1):
            candidates = [" ".join(keys[index:index + size])]
            if size == 1 and keys[index].endswith("s"):
                candidates.append(keys[index][:-1])
            concepts = concepts_of(db, candidates)
            if concepts:
                parts.append((concepts, None))
                index += size
                break
        else:
            if len(tokens[index]) > 1 and tokens[index] not in stopwords:
                fixed = corrected(db, keys[index])
                parts.append((fixed, None) if fixed else (None, keys[index]))
            index += 1
    unique = []
    for part in parts:
        if part not in unique:
            unique.append(part)
    return unique


def search(db, query: str, prefix: str = "", limit: int = 40):
    """Returns (labels of what was understood, [(asset, page, is heading, text)])."""
    parts = parse(db, query)
    labels = dict(db.execute("SELECT id, label FROM concept"))
    understood = ["/".join(labels[c] for c in concepts[:2]) if concepts else word for concepts, word in parts]
    matched, total, book_of = defaultdict(int), defaultdict(int), {}
    for concepts, word in parts:
        if concepts:
            marks = ",".join("?" * len(concepts))
            rows = db.execute(
                f"SELECT h.doc, d.book, MAX(h.score) FROM hit h JOIN doc d ON d.id = h.doc JOIN book b ON b.id = d.book "
                f"WHERE h.concept IN ({marks}) AND b.asset LIKE ? GROUP BY h.doc", (*concepts, prefix + "%"))
        else:
            rows = db.execute(
                "SELECT w.docid, d.book, CASE WHEN d.heading IS NULL THEN 1000 ELSE 3000 END FROM words w "
                "JOIN doc d ON d.id = w.docid JOIN book b ON b.id = d.book WHERE words MATCH ? AND b.asset LIKE ?",
                (word + "*", prefix + "%"))
        for doc, book, score in rows:
            matched[doc] += 1
            total[doc] += score
            book_of[doc] = book
    assets = dict(db.execute("SELECT id, asset FROM book"))
    arabic_query = any("؀" <= ch <= "ۿ" for ch in query)
    searched = {book for book, asset in assets.items() if asset.startswith(prefix)}
    preferred = {book for book in searched if assets[book].startswith(ARABIC_SCRIPT_BOOKS) == arabic_query}
    ranked = sorted(matched, key=lambda d: (-matched[d], -total[d] * (5 if book_of[d] in preferred else 4), d))
    per_book_limit = min(15, max(3, 40 // max(1, len(searched))))
    per_book, results = defaultdict(int), []
    for doc in ranked:
        if per_book[book_of[doc]] >= per_book_limit:
            continue
        per_book[book_of[doc]] += 1
        page, heading, body = db.execute("SELECT page, heading, body FROM doc WHERE id = ?", (doc,)).fetchone()
        results.append((assets[book_of[doc]], page, heading is not None,
                        heading or zlib.decompress(body).decode("utf-8")))
        if len(results) >= limit:
            break
    return understood, results


def check(db) -> int:
    failures = total = 0
    for line in EVAL.read_text(encoding="utf-8").splitlines():
        if not line.strip() or line.startswith("#"):
            continue
        query, prefix, expected, *rest = line.split("\t")
        depth = int(rest[0]) if rest else 5
        total += 1
        understood, results = search(db, query, prefix, limit=depth)
        wanted = [option.strip().lower() for option in expected.split("|")]
        if not any(any(option in f"{asset} {text}".lower() for option in wanted) for asset, _page, _h, text in results):
            failures += 1
            print(f"FAIL  {query}  [{prefix}]  understood={understood}  wanted={expected}")
            for asset, page, heading, text in results[:3]:
                print(f"        {asset} p{page + 1} {'#' if heading else ' '} {text[:90]}")
    print(f"{total - failures}/{total} queries found their expected result")
    return failures


if __name__ == "__main__":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")
    connection = sqlite3.connect(sys.argv[1])
    if sys.argv[2] == "--check":
        sys.exit(1 if check(connection) else 0)
    understood, results = search(connection, sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else "", limit=12)
    print("understood:", understood)
    for asset, page, heading, text in results:
        print(f"  {asset} p{page + 1} {'#' if heading else ' '} {text[:110]}")

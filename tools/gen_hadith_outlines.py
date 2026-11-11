"""Generates app/src/main/java/com/example/hadith/HadithOutlines.kt from the Hadith PDFs' bookmarks.

The 19 Kutub al-Sittah volumes carry an embedded outline. Bundling it lets the reader's chapter
list open at once instead of downloading the whole PDF first. The entries follow the same rules
as HadithPdfStore.extractOutline (PDFBox): a depth-first walk of /First and /Next, only explicit
page destinations (/Dest or a GoTo action with an array), a 1-based page number, a trimmed title,
two leading spaces per nesting level, blank titles and pageless items dropped, and duplicates of
(title, page) removed while keeping the first.

Titles are copied exactly as decoded from the PDF, with no normalisation.

Usage (the PDFs are the byte-identical mirror copies, named as in HadithBook.mirrorAssetName):
    python -I tools/gen_hadith_outlines.py <pdf-dir> [--lib <dir holding pypdf>] [--out <file.kt>]
"""
import argparse
import os
import sys
import unicodedata

# Same order and ids as HadithCatalog.arabicSets: (set id, number of volumes).
SETS = [
    ("bukhari", 2), ("muslim", 2), ("abu_dawud", 2),
    ("tirmidhi", 5), ("nasai", 6), ("ibn_majah", 2),
]
DEFAULT_OUT = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "app", "src", "main", "java", "com", "example", "hadith", "HadithOutlines.kt",
)
# Characters written as \\u escapes so the source stays readable and free of hidden direction marks.
INVISIBLE = set(range(0x00, 0x20)) | {0x7F} | set(range(0x80, 0xA0)) | {
    0x061C, 0x200B, 0x200C, 0x200D, 0x200E, 0x200F, 0x2028, 0x2029,
    0x202A, 0x202B, 0x202C, 0x202D, 0x202E, 0x2060, 0x2066, 0x2067, 0x2068, 0x2069, 0xFEFF,
}
PAGE_DEST_TYPES = {"/XYZ", "/Fit", "/FitB", "/FitH", "/FitBH", "/FitV", "/FitBV", "/FitR"}


def books():
    for index, (set_id, volumes) in enumerate(SETS):
        for volume in range(1, volumes + 1):
            yield "%02d-%s-%d" % (index, set_id, volume), "ar-%s-v%d.pdf" % (set_id.replace("_", "-"), volume)


def kotlin_trim(text):
    """Kotlin's String.trim(): strips Char.isWhitespace (Java whitespace or a space separator)."""
    def blank(ch):
        return ch in "\t\n\u000b\u000c\r\u001c\u001d\u001e\u001f" or \
            unicodedata.category(ch) in ("Zs", "Zl", "Zp")
    start, end = 0, len(text)
    while start < end and blank(text[start]):
        start += 1
    while end > start and blank(text[end - 1]):
        end -= 1
    return text[start:end]


def outline(path, generic):
    from pypdf import PdfReader
    reader = PdfReader(path, strict=False)
    page_index = {page.indirect_reference.idnum: i for i, page in enumerate(reader.pages)}
    root = reader.trailer["/Root"].get_object()
    entries, skipped, seen = [], 0, set()

    def page_of(item):
        dest = item.get("/Dest")
        if dest is None:
            action = item.get("/A")
            action = action.get_object() if action is not None else None
            if action is not None and action.get("/S") == "/GoTo":
                dest = action.get("/D")
        if dest is None:
            return None
        dest = dest.get_object()
        # PDFBox only resolves explicit arrays here; a named destination yields no page, and
        # anything else makes it give up on the whole outline, so the generator stops too.
        if isinstance(dest, (generic.NameObject, generic.TextStringObject, generic.ByteStringObject)):
            return None
        kind = dest[1].get_object() if isinstance(dest, generic.ArrayObject) and len(dest) > 1 else None
        if not isinstance(kind, generic.NameObject):
            raise ValueError("Destination PDFBox cannot read: %r" % (dest,))
        if str(kind) not in PAGE_DEST_TYPES:
            raise ValueError("Unknown destination type %s" % kind)
        target = dest[0]
        if isinstance(target, generic.IndirectObject):
            index = page_index.get(target.idnum, -1)
        elif isinstance(target, generic.NumberObject):
            index = int(target)
        else:
            return None
        return index + 1

    def visit(first, depth):
        nonlocal skipped
        item = first
        while item is not None:
            key = item.idnum if isinstance(item, generic.IndirectObject) else id(item)
            if key in seen:
                raise ValueError("Outline loop")
            seen.add(key)
            node = item.get_object()
            page = page_of(node)
            title = kotlin_trim(str(node.get("/Title", "")))
            if page is not None and page > 0 and title:
                entries.append(("  " * depth + title, page))
            else:
                skipped += 1
            visit(node.get("/First"), depth + 1)
            item = node.get("/Next")

    outlines = root.get("/Outlines")
    if outlines is not None:
        visit(outlines.get_object().get("/First"), 0)
    distinct, keys = [], set()
    for entry in entries:
        if entry not in keys:
            keys.add(entry)
            distinct.append(entry)
    return distinct, len(reader.pages), skipped


def literal(text):
    out = []
    for ch in text:
        code = ord(ch)
        if ch == "\\":
            out.append("\\\\")
        elif ch == '"':
            out.append('\\"')
        elif ch == "$":
            out.append("\\$")
        elif ch == "\n":
            out.append("\\n")
        elif ch == "\t":
            out.append("\\t")
        elif code in INVISIBLE or 0xD800 <= code <= 0xDFFF:
            out.append("\\u%04X" % code)
        elif code > 0xFFFF:
            code -= 0x10000
            out.append("\\u%04X\\u%04X" % (0xD800 + (code >> 10), 0xDC00 + (code & 0x3FF)))
        else:
            out.append(ch)
    return '"' + "".join(out) + '"'


def function_name(book_id):
    _, set_id, volume = book_id.split("-")
    parts = set_id.split("_")
    return parts[0] + "".join(p.capitalize() for p in parts[1:]) + volume


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("pdf_dir")
    parser.add_argument("--lib")
    parser.add_argument("--out", default=DEFAULT_OUT)
    args = parser.parse_args()
    if args.lib:
        sys.path.insert(0, args.lib)
    import logging
    import warnings
    warnings.filterwarnings("ignore")
    logging.disable(logging.CRITICAL)
    from pypdf import generic

    volumes = []
    for book_id, file_name in books():
        entries, pages, skipped = outline(os.path.join(args.pdf_dir, file_name), generic)
        if any(not 1 <= page <= pages for _, page in entries):
            raise SystemExit("%s: a bookmark points outside the %d pages" % (file_name, pages))
        volumes.append((book_id, entries))
        print("%s: %d entries, %d pages, %d skipped" % (book_id, len(entries), pages, skipped))

    lines = [
        "// GENERATED by tools/gen_hadith_outlines.py from the bookmarks embedded in the mirrored",
        "// Kutub al-Sittah PDFs. Do not edit by hand; re-run the script instead.",
        "package com.example.hadith",
        "",
        "/**",
        " * The bookmarks of the 19 Hadith volumes, read by the same rules as HadithPdfStore's outline",
        " * extraction, so a volume's chapter list opens without downloading the whole PDF first.",
        " */",
        "internal object HadithOutlines {",
        "    /** The chapters of the volume with [bookId], or an empty list when it has none. */",
        "    fun chapters(bookId: String): List<HadithChapter> = when (bookId) {",
    ]
    lines += ['        "%s" -> %s()' % (book_id, function_name(book_id)) for book_id, _ in volumes]
    lines += ["        else -> emptyList()", "    }"]
    for book_id, entries in volumes:
        lines += ["", "    private fun %s(): List<HadithChapter> = listOf(" % function_name(book_id)]
        lines += ["        HadithChapter(%s, %d)," % (literal(title), page) for title, page in entries]
        lines += ["    )"]
    lines += ["}", ""]
    with open(args.out, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("\n".join(lines))
    print("wrote", args.out)


if __name__ == "__main__":
    main()

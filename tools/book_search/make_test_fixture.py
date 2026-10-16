"""Writes the tiny index BookSearchIndexTest searches: the real dictionary over a few made-up pages.

    python tools/book_search/make_test_fixture.py

Run it after changing concepts.txt, the folding rules or the index layout.
"""
import gzip
import json
import shutil
import tempfile
from pathlib import Path

from build_index import build

TARGET = Path(__file__).resolve().parents[2] / "app/src/test/resources/book_search_fixture.db"
FILLER = " The narrator said that the people of the town gathered near the well before sunset." * 3

BOOKS = [
    {"asset": "en-bukhari-complete", "chapters": [[47, "Book 4: Ablutions (Wudu')"], [251, "Book 20: Shortening the Prayers"]],
     "pages": [
         [48, "BOOK 4: ABLUTIONS The Prophet performed ablution and washed each limb three times." + FILLER],
         [252, "BOOK 20: SHORTENING THE PRAYERS On a journey the prayer was shortened to two rak'at." + FILLER],
         [300, "Narrated Abu Hurairah that honey was mentioned as a cure." + FILLER],
         [431, "BOOK 31: FASTING Whoever observes the fast of Ramadan out of faith is forgiven." + FILLER],
     ]},
    {"asset": "en-muslim-complete", "chapters": [[404, "Chapter 99: Permissibility of combining two prayers on a journey"]],
     "pages": [[900, "Ahmad Raza narrated a report about trade in the market." + FILLER]] * 1
     + [[901 + n, "Raza was present when the caravan arrived." + FILLER] for n in range(3)]},
    {"asset": "ar-bukhari-v1", "chapters": [[44, "٤ - كتاب الوضوء"], [451, "٣٦ - كتاب الصوم"]], "pages": []},
]

if __name__ == "__main__":
    with tempfile.TemporaryDirectory() as folder:
        cache, out = Path(folder) / "cache", Path(folder) / "out"
        cache.mkdir()
        for book in BOOKS:
            with gzip.open(cache / f"{book['asset']}.json.gz", "wt", encoding="utf-8") as stream:
                json.dump(book, stream, ensure_ascii=False)
        build(out, cache)
        TARGET.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(out / "book-search.db", TARGET)
    print(f"{TARGET} {TARGET.stat().st_size / 1e3:.0f} kB")

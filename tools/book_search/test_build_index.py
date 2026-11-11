"""Checks the layout build_index.py writes and the app reads (format 3: a bundled core plus a page-text file).

    python -m unittest tools/book_search/test_build_index.py
"""
import gzip
import json
import sqlite3
import sys
import tempfile
import unittest
import zlib
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import build_index  # noqa: E402

BOOKS = [
    {"asset": "en-bukhari-v1", "chapters": [[4, "Book 4: Ablutions (Wudu')"]],
     "pages": [[5, "The Prophet performed ablution and washed each limb three times."],
               [6, "On a journey the prayer was shortened to two rak'at. Ünïcode ✓ text."]]},
    {"asset": "ar-bukhari-v1", "chapters": [[44, "٤ - كتاب الوضوء"]], "pages": []},
]


class BuildIndexTest(unittest.TestCase):
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory()
        # Cleanups run last-first, so databases opened later are closed before the folder goes.
        self.addCleanup(self.folder.cleanup)
        root = Path(self.folder.name)
        self.cache, self.out = root / "cache", root / "out"
        self.cache.mkdir()
        for book in BOOKS:
            with gzip.open(self.cache / f"{book['asset']}.json.gz", "wt", encoding="utf-8") as stream:
                json.dump(book, stream, ensure_ascii=False)
        # The test vectors belong to the app's unit test; a build here must not rewrite them.
        self.addCleanup(setattr, build_index, "VECTORS", build_index.VECTORS)
        build_index.VECTORS = root / "vectors.tsv"

    def built(self, assets=None):
        core, bodies = build_index.build(self.out, self.cache, assets)
        db = sqlite3.connect(core)
        self.addCleanup(db.close)
        return db, core, bodies

    def test_page_text_round_trips_through_offsets(self):
        db, _core, bodies = self.built()
        data = bodies.read_bytes()
        expected = {(book["asset"], page): text for book in BOOKS for page, text in book["pages"]}
        rows = db.execute("SELECT b.asset, d.page, d.body_at, d.body_len FROM doc d JOIN book b ON b.id = d.book "
                          "WHERE d.heading IS NULL ORDER BY d.id").fetchall()
        self.assertEqual(len(rows), len(expected))
        end = 0
        for asset, page, at, length in rows:
            self.assertEqual(at, end, "page text is stored back to back in doc order")
            self.assertEqual(zlib.decompress(data[at:at + length]).decode("utf-8"), expected[(asset, page)])
            end = at + length
        self.assertEqual(end, len(data))

    def test_headings_carry_no_page_text(self):
        db, _core, _bodies = self.built()
        rows = db.execute("SELECT body_at, body_len FROM doc WHERE heading IS NOT NULL").fetchall()
        self.assertEqual(len(rows), 2)
        self.assertTrue(all(row == (None, None) for row in rows))
        columns = [row[1] for row in db.execute("PRAGMA table_info(doc)")]
        self.assertEqual(columns, ["id", "book", "page", "heading", "body_at", "body_len"])

    def test_meta_names_the_matching_bodies_file(self):
        db, core, bodies = self.built()
        meta = dict(db.execute("SELECT key, value FROM meta"))
        self.assertEqual(meta["format"], "3")
        self.assertEqual(meta["docs"], "4")
        self.assertEqual(meta["bodies"], f"book-search-bodies-{meta['version']}.bin")
        self.assertEqual(bodies.name, meta["bodies"])
        self.assertEqual(int(meta["bodies_size"]), bodies.stat().st_size)
        self.assertEqual((self.out / "book-search-version.txt").read_text(encoding="utf-8"), meta["version"])
        self.assertEqual(core.name, "book-search-core.db")
        self.assertFalse(list(self.out.glob("*.gz")), "the whole-index download is gone")
        self.assertFalse(list(self.out.glob("*.part")))

    def test_build_without_assets_leaves_the_app_alone(self):
        before = {path: path.stat().st_mtime_ns for path in build_index.ASSETS.glob("*")} \
            if build_index.ASSETS.exists() else {}
        self.built()
        after = {path: path.stat().st_mtime_ns for path in build_index.ASSETS.glob("*")} \
            if build_index.ASSETS.exists() else {}
        self.assertEqual(before, after)

    def test_build_with_assets_copies_core_and_version(self):
        assets = Path(self.folder.name) / "assets" / "book_search"
        db, core, _bodies = self.built(assets)
        version = db.execute("SELECT value FROM meta WHERE key = 'version'").fetchone()[0]
        self.assertEqual((assets / "core.db").read_bytes(), core.read_bytes())
        self.assertEqual((assets / "version.txt").read_bytes(), version.encode("utf-8"))


if __name__ == "__main__":
    unittest.main()

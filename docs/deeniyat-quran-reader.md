# Deeniyat 11-line Quran reader

Reference: Quraan-E-Karim (11 Lines), Deeniyat Educational and Charitable Trust,
package `com.deeniyat.quraan10liner`, version 18.0.
Play listing: https://play.google.com/store/apps/details?id=com.deeniyat.quraan10liner

The inspected APK SHA-256 is
`61109c69b181ab20322756defcf5d29867fc08743b5ba36c85c173b4893504bd`.
It was downloaded from APKPure's download link and inspected as a ZIP, without installation.
The embedded `res/raw/quran_11_line_v1.pdf` is bundled byte-for-byte as
`app/src/main/assets/quran_deeniyat_11_lines.pdf`, with SHA-256
`1bcd044b35d9b514acee62bcc29508737ec3a8074253b70a83114cfd5d53e228`.
It has 1,410 PDF pages and is 118,192,240 bytes. This adds about 113 MiB to the app assets.

The 114 surah and 30 para start pages come from `assets/databases/quran11.sql`
in the same APK. The printed page labels match the PDF's 1-based page numbers,
including a cover on page 1 and Al-Fatiha on page 2. Pages 2, 3, 47, 1358,
and 1410 were rendered and visually inspected. Existing Quran editions remain available.

To reproduce extraction, run:

```powershell
python tools/quran_pdfs/extract_deeniyat_11.py tools/quran_reference/deeniyat-11.apk
```

Implemented reader behavior: offline original edition, surah/para navigation,
page jump, saved last position, pinch/double-tap zoom, per-edition page bookmarks,
surah favorites, gallery paging, vertical paging, and an animated page-turn mode.
Reading mode and bookmarks persist across reopening. The page-turn animation is
our Compose implementation; identical animation/UI behavior to the reference
has not been verified. Hardware volume/menu shortcuts have not been implemented.

The PDF is attributed to its original publisher. No open-source or redistribution
license was located for these publisher assets. This is a local integration;
publisher asset redistribution rights still need to be established before release.

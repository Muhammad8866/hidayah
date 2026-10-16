# Ladies / Muslimah library

Verified on 2026-10-05 against Dawat-e-Islami's book pages and its official
[Islamic sisters reading list](https://departments.dawateislami.net/islamibahan/books).

The Islamic Books screen now has an All books / Ladies / Muslimah filter. This
catalog contains 10 women-focused Ahle Sunnat titles, with all 20 language
editions listed on their source pages: Urdu, Hindi, Sindhi, English, Bengali,
Gujarati, Telugu and Portuguese. Language availability differs by title.

- Salah for Islamic Sisters (8 editions)
- Jannati Zevar (3 editions)
- Sunni Bahishti Zewar
- Khawateen Kay Masail
- Chahal Hadees Baraye Khawateen-e-Islam
- Chadar Aur Char Diwari
- Aurat Aur Parda
- Aurat Aur Aazadi
- Saas Bahu Main Sulha Ka Raaz (2 editions)
- Jannati Khawateen

General books recommended for sisters (for example Madani Panj Surah, Tarbiyat-e-Aulad
and Islami Taleem) and biographies alone are not included in this women-focused
catalog. Sunni Bahishti Zewar here is the Sunni work listed by Dawat-e-Islami,
not a different author's similarly named book.

This is a verified initial catalog, **not an exhaustive worldwide bibliography**.
Additional titles and translations require review before inclusion. No translation
is invented to fill a missing language. Original PDFs are read and saved using
the existing library reader. These editions have no GitHub mirrors; downloads
use their original source URLs.

## Refreshing

1. Review new titles and their women-focused audience and Sunni provenance.
2. Update the reviewed title list in `tools/collect_muslimah_books.py`.
3. Clear only the relevant cached source HTML in `tmp/muslimah-catalog` when refreshing.
4. Run `python tools/collect_muslimah_books.py` to update `muslimah-book-sources.json`.
5. Review the manifest, then run `python tools/generate_muslimah_catalog.py`.
6. Run `python tools/verify_muslimah_sources.py` to check original PDF signatures.

The publisher uses `si` for Sindhi; the app catalog uses `sd` while preserving
the original publisher download URL.

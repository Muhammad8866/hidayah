# Islamic Baby Names

The offline catalog contains 493 entries: 368 boys and 125 girls. Names are taken
from the **recommended-name lists**, online pages 43–61, of
[Naam Rakhne ke Ahkam](https://www.dawateislami.net/bookslibrary/ur/naam-rakhnay-kay-ahkam),
published by Maktaba-tul-Madinah / Dawat-e-Islami.

The publisher's [book introduction](https://news.dawateislami.net/Naam-Rakhnay-Kay-Ahkam-ka-taaruf)
states that Dar-ul-Ifta Ahle Sunnat's muftis checked the complete book. This is the
evidence for choosing the source; it does not mean the publisher reviewed this
app or its translations. The source also contains discouraged/prohibited names
in its rulings section. Those sections are **not** imported as suggestions.

Each entry includes its exact online source-page URL, source spelling, original
Urdu meaning/association, and concise English and Hinglish translations. Online
page numbers differ from the PDF's printed page numbers. Some source entries give
a historical association or a devotional explanation rather than a linguistic
etymology; the app keeps that distinction, especially for Fatima, Ahmad and
names of prophets. It does not label every entry a Quranic name.

`baby-name-sources.json` is the checked-in editorial evidence. The app's generated
`BabyNameCatalog.kt` works offline; only opening a source link needs internet.
Source links are available on every card. Urdu uses the source's original text.
Search indexes are cached and support Latin aliases, Urdu/Arabic script and both
translated meanings. A name used for both genders, such as Sariyah, has distinct
gender-qualified list IDs. Hasan (حسن) and Hassan (حسان) are distinct names;
alternate roman spellings are otherwise stored as aliases instead of padded
catalog entries.

## Changes to the starter list

21 of the old 30 display names remain, with source-specific meanings. 472 new
entries are added. Nine old entries are withheld because the selected Ahle Sunnat
source's recommended list did not supply an entry-specific reference for them:
Amir, Farid, Amal, Aminah, Amira, Basma, Farah, Huda and Latifa. This is a provenance
decision, **not a ruling that these names are impermissible**. Restore them only
with a checked entry-specific source and meaning. Amina (آمنة) remains; the separate
trustworthy Aminah (أمينة) entry is withheld.

## Rebuilding the data

1. Install Python `beautifulsoup4` if needed.
2. Run `python tools/collect_baby_name_sources.py` to cache the official online
   source section in `tmp/baby-name-sources/`.
3. Review `tools/baby_names_reviewed.tsv` against the cached source candidates.
   Candidate IDs follow source paragraph order; re-check them if the publisher
   changes its HTML. These are editorially translated rows, not automatic
   suggestions from an arbitrary names website.
4. Run `python tools/generate_baby_names.py` to regenerate the evidence JSON and
   Kotlin catalog. Small manually reviewed exceptions in the generator cover
   Ahmad's prose reference and names split across paragraph boundaries.
5. Run the `BabyNamesTest` and `BabyNamesScreenTest` unit tests.
6. Run `python tools/verify_baby_names.py` to audit unique IDs, required fields,
   generated catalog counts and the cached source text for every entry.

Data checked: 2026-10-10. The catalog does not invent compound names to inflate
the count, and does not import a fatwa merely because its question contains a name.

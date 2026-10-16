# World-language content completion

The requested sequence is Persian (`fa`), Turkish (`tr`), Indonesian (`id`),
Spanish (`es`), Russian (`ru`), Simplified Chinese (`zh`), Italian (`it`) and
Japanese (`ja`). Coverage is measured against the existing 298 dua IDs and
2,489 unique content keys in `tools/indic_translation_sources.json`.

This is technical catalog coverage, not a claim that every string in the
application has been independently audited or that machine translations have
received linguistic or religious approval. The language picker's review-pending
flags and the Arabic devotional originals remain intact.

## Offline integration

- Missing structured dua rows live in `Dua<Code>Draft*.kt`; existing registered
  rows are retained and are not regenerated.
- `WorldDraftPhrases` loads only the requested language. Whole phrases supplement
  existing UI translations, name meanings, lessons, festival/Salah guidance,
  More subtitles and the feature tour.
- Existing UI translations win over supplemental drafts. Surah proper names
  retain their identity instead of being translated as unrelated English words.
- `localization/translations/<code>.json` stores reproducible draft results.
  `localization/world-glossary.json` contains explicit terminology/corrections.
- English source copy is reused from the existing content inventory. No new
  English UI sentences were added and the legacy baseline was not expanded.
- App builds use the bundled Kotlin maps without a network translation request.

## Updating and checking

Run one language at a time:

```powershell
python tools/world_localization.py fa --online --integrate --check
python tools/audit_world_content_coverage.py fa
python tools/auto_translate.py --check
python -m unittest discover -s tools -p test_world_localization.py
python -m unittest discover -s tools -p test_auto_translate.py
```

`--online` uses the public web translation service for existing English app
content. It has no availability/quota guarantee; only validated results are
cached. It never reads or sends app secrets, user notes, coordinates or other
runtime user data. Prefer the project's credentialed build translation pipeline
for newly authored UI copy. Do not use `--refresh` on reviewed caches.

Without translation access, existing caches can be materialized and checked:

```powershell
python tools/world_localization.py fa --integrate --check
python tools/audit_world_content_coverage.py fa tr id es ru zh it ja
```

The checks require every expected loader/key and compare generated phrase values
with their cached source. They reject blank/truncated output, altered paragraph
counts, changed dynamic placeholders and changed Arabic devotional runs.
Protected source spans use reversible markers during translation. If a marker
is dropped, the line is retried with a different wrapping; malformed output is
never accepted as an English fallback.

`WorldDraftIntegrationTest`, `AppLocalizationTest` and
`IndicDraftIntegrationTest` verify Android lookup wiring, complete dua sets,
template substitution, existing translation precedence and regional regressions.
Device layout/accessibility checks and qualified translation review are separate
requirements. Source/coverage logs are under
`artifacts/world-localization-2026-10-14`.

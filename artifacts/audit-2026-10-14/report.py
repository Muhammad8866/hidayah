import json
from pathlib import Path
p=Path(__file__).parent
d=json.loads((p/'inventory.json').read_text(encoding='utf-8'))
bad=[x for x in d['languages'] if x['missing_duas']]
body='''# Deen Companion audit — 14 October 2026

This is a repository-wide inventory and targeted source/check audit of the current working tree. It is not certification that every feature, translation or runtime path is correct. Existing uncommitted changes were preserved. No product code was changed.

## Verified language findings

53 picker languages; 49 are explicitly marked `reviewPending = true`. Review status and technical coverage are different. 25 languages lack complete dedicated dua translations. 28 have canonical dua ID coverage, including English and Hinglish source content. ID presence alone does not establish accuracy, nonblank fields or religious approval.

| Language | Dedicated/source dua IDs | Missing out of 298 |
|---|---:|---:|
'''
for x in d['languages']:
    body+=f"| {x['code']} — {x['name']} | {x['dua_entries']} | {x['missing_duas']} |\n"
body+='''
Evidence: `MainViewModel.kt:163`, `domain/model/i18n/DuaTranslations.kt:22`, and `inventory.json`. Missing entries fall back through `DuasAdhkarScreen.kt:894–905` and `ContentLanguage.kt:14`; users can receive English/Roman Hindi content instead of their selected language. Persian has no dedicated dua loader; whole-phrase UI lookup could translate individual strings but is not a complete structured dua pack.

The existing Indic inventory covers only 23 South Asian languages. All its audited dua IDs, feature keys, names and extra-content keys are present. It does not test the 29 world-language packs or all runtime-generated copy. See `indic-coverage.json`.

## Prioritized findings

1. **High — Incomplete religious-content localization:** the 25 languages listed with missing IDs above need complete content integration and qualified review. Extend automated content coverage to every supported language, rather than accepting picker registration as completeness.
2. **High, conditional — Destructive user database upgrade fallback:** `MainViewModel.kt:293` uses `fallbackToDestructiveMigration(dropAllTables = true)` with only migration 2→3. An installation without a supported migration path can lose bookmarks, notes, Hifz progress and outbox rows. `data/local/DeenDatabase.kt:84–104` confirms these tables. Establish supported historical schemas and add preservation migrations. Community Masjid has the same fallback at `CmDatabase.kt:166`, mainly affecting its local cache.
3. **Medium — Localization coverage checker has blind spots:** `auto_translate.py --check` passes with **0 new phrases**; `localization/source.json` is empty and there are no translation JSON cache files. Legacy baseline exempts existing calls; regex scanning omits toast, notification, variable-first and multiline literals. This is a passing new-copy gate, not whole-app localization coverage. Do not regenerate the baseline to hide gaps.
4. **Medium — Confirmed untranslated UI outside Compose text:** Android <33 Kalima copy toast uses raw `"Copied"` (`KalimasScreenParts.kt:288`). Tafsir notification channel, fallback title and subtitle use raw English (`TafsirAudioService.kt:94–108`). Route these through persisted-language helpers and stable templates.
5. **Medium, security boundary limitation — Mosque proximity is client asserted:** `firestore.rules:55–57` checks caller-supplied distance/accuracy bounds. An authenticated modified client can submit in-range values without proving physical proximity. This limitation is documented in the rules; App Check deployment/enforcement has not been verified. Treat the 200 m check as a client claim, and validate abuse controls in an emulator/backend review.
6. **Medium — UI word substitution can produce mixed-language sentences:** `AppLocalization.kt:769–773` replaces matched fragments when no full phrase exists. This is explicitly different from whole-content lookup, but cannot establish grammatical full-sentence translation. Inventory dynamic sentences and migrate them to whole-sentence templates.
7. **Low — Setup documentation is stale:** README instructs setting GEMINI_API_KEY although no runtime Kotlin use was found; automatic translation reads TRANSLATION_API_KEY from the build environment. README has malformed introductory markup and no reproducible test/translation/release procedure.

## Checks and reviewed boundaries

- Python automatic localization check: PASS, but 0 new phrases checked.
- Python translation unit suite: 5 tests PASS.
- Baby-name verification: 798 entries, 305 reviewed additional entries and 19 cached source pages PASS.
- Audio verification: 114 bundled assets decode; source hashes/bitrates PASS. This check does not cover every remote audio URL or every azan/rabbana asset.
- Muslimah publisher URLs: all 21 checked URLs returned PDF content. This is signature/link validation, not full document or rights validation.
- Source inventory: 1,075 main Kotlin files / 134,549 lines; 89 JVM test files; 2 instrumentation files. Counts are not a measure of test coverage.
- Literal scanner: 105 unique raw Text/MaterialText English-containing strings. Some can be devotional transliteration, symbols or deliberate source text; this count is a review queue, not 105 confirmed bugs. Locations are in `inventory.json`.
- Manifest: media services and FileProvider are non-exported; launcher/widget exported as declared. RTL support and 53 locale declarations exist. Runtime layout, TalkBack, font scaling and clipping are unverified.
- Secret files `.env`, keystore.properties and my-upload-key.jks are ignored and not tracked. Values were not printed. Secrets plugin packages .env properties unless ignored; obsolete GEMINI credentials should be removed/ignored if present. No TRANSLATION_API_KEY reference was found in runtime source. APK credential absence was not verified for a fresh build.
- Network source spot review: shared FastNet uses standard TLS, timeouts and pooled connections; no trust-all implementation was found in the searched source. RangeFile validates PDF signatures, range responses and source validators. This is not a penetration test.
- Prayer source spot review: exact-alarm permission checks, SecurityException handling, reboot/time-change restoration and background playback fallback exist; delivery under Doze/OEM restrictions is unverified.
- Analytics starts automatically; no collection switch exists. Privacy policy explicitly discloses this behavior, so this audit does not label it an undisclosed collection defect. Legal/store compliance was not assessed.

## Unverified work needed for a complete release audit

No ADB device/emulator is connected. Firestore test emulator port 8088 is not listening. Device UI journeys, offline/reconnect, background playback, notification permission denial, Doze/reboot, migrations from historical releases, all language/RTL screenshots, accessibility, deployed Firebase rules/App Check, actual restricted API-key settings, Play delivery/update flows, current dependency advisories, fresh signed release/R8 and APK content checks remain unverified. Current test/lint outcome is recorded separately below; older generated reports are not treated as results of this audit.

## Android validation

See `gradle-check.log` for the current invocation of `:app:testDebugUnitTest :app:lintDebug --offline --no-daemon --max-workers=1`.
'''
(p/'AUDIT.md').write_text(body,encoding='utf-8')
print('Report written; incomplete languages:',len(bad))

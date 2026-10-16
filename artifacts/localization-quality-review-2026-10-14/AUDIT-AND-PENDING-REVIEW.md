# Localization quality review — 14 October 2026

Expert linguistic and religious approval remains pending for 49 languages. The user confirmed that no reviewer records exist. No approval flags were cleared: repository instructions explicitly state that generated drafts do not establish religious or linguistic approval.

## Work completed

- Exported the actual registered content of all 49 pending languages: 14,602 dua rows and 58,408 fields, with English source text, target text, file provenance and empty reviewer fields. See [packet manifest](packets/manifest.json) and [packet instructions](packets/README.md).
- Applied 28 high-confidence draft corrections across 14 languages and 23 generated Kotlin files. Corrected draft caches and regeneration overlays so future generation retains these changes. See [durable corrections](../../localization/quality-review-draft-corrections.json).
- Retained 17 sampled semantic findings requiring further investigation or qualified target-language review. See [unresolved findings](pending-semantic-findings.json). This is a sampled finding count, not a count of every remaining error.
- Rechecked coverage in the 12 affected world-language integrations: each has 298 canonical duas and 2,489 content keys, without missing content or cache entries.
- Preserved original non-draft translations, Arabic devotional originals, review status flags and the legacy baseline.

## Scope and limits

The 49-language packet export is a structural inventory of registered dua fields. The three semantic reviews sampled high-risk wording; they do not establish exhaustive linguistic correctness. UI text, other long-form content, pronunciation and broader doctrinal interpretation still need appropriate review. Differences in literal honorific symbols can reflect translated honorifics and are not automatically omissions.

The remaining samples include creed wording, the Uzbek Al-Fatiha negation, mistranslated divine titles, mixed-language passages, and Japanese/Vietnamese wording about dominion and praise. The English reference for Al-Fatiha distinguishes the [Master of the Day of Judgment](https://quran.com/1/4) and the [negated paths in its final verse](https://quran.com/1/7). The remembrance in [Sahih al-Bukhari 1154](https://sunnah.com/bukhari:1154) supports the possession wording for dominion and praise. These references help identify defects; they do not certify proposed translations in every target language.

Detailed review evidence: [Indic](indic-review.md), [world group A](world-a-review.md), [world group B](world-b-review.md).

## Validation

27 focused Python tests passed (17 world localization, 6 auto-translation and 4 quality-review tests), and `auto_translate.py --check` passed for all 53 languages. The retry completed `assembleDebug` and all 31 focused Android tests with zero failures and native exit code 0, in 10 minutes 13 seconds. See [validation results and fresh APK hash](validation-results.json) and [the build log](android-validation.log). The initial run crashed because native JVM memory allocation failed; its evidence is retained in [the failure log](android-validation-memory-failure.log). The successful retry used a smaller heap and fewer JVM processors. These checks validate integration and selected regression cases; they do not certify linguistic or religious correctness.

Earlier unrelated full-suite failures and Android lint findings remain documented in [the project audit](../audit-2026-10-14/AUDIT.md). This review does not resolve or certify those issues.

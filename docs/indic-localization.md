# Indian and neighboring language integration

Target languages: Hinglish, Hindi, Urdu, Assamese, Bengali, Bodo, Dogri,
Gujarati, Kannada, Kashmiri, Konkani, Maithili, Malayalam, Manipuri, Marathi,
Nepali, Odia, Punjabi, Sanskrit, Santali, Sindhi, Tamil and Telugu.

The existing Hinglish, Hindi and Urdu content remains in use. The other 20
languages now have bundled machine translation drafts, with `reviewPending`
enabled in the language picker. Linguistic and religious review is pending.

## Bundled coverage

- All 23 target languages have all 298 canonical dua entries. The integration
  added the 4,112 missing entries across the regional languages.
- Each new regional pack includes 2,267 UI/content phrase keys and 227 unique
  Allah-name, Prophet-name and Rabbana meaning keys.
- Content includes the seven feature phrase tables, onboarding, More subtitles,
  prayer prompts, lessons, festival guidance, all 485 Salah source phrases,
  Surah labels and static screen labels.
- Existing translations take precedence over drafts. Arabic devotional text,
  placeholders and lesson line breaks are preserved.
- Salah sentence composition and substitutions resolve in the selected language.
  Generated dua and festival quiz questions use the selected content pack.
- Script-aware locales include Arabic script for Kashmiri, Sindhi and Urdu,
  Bengali script for Manipuri, Gurmukhi for Punjabi and Ol Chiki for Santali.

Publisher PDFs, Quran editions, audio recordings and remote catalog contents
retain their source language. These source documents and recordings are not
machine translation drafts.

## Verification

Daily-tools follow-up (13 October 2026): added 66 newly introduced full-phrase
keys to each of the 20 regional draft packs (1,320 entries). The coverage audit
now reports no missing feature keys. This closes the identified daily-tools
coverage gap; it does not certify linguistic accuracy or translate the separate
English/Roman Qaida content. Review-pending labels remain enabled.

`regionalDailyToolsHaveEveryReferencePhrase` checks runtime pack coverage,
nonblank text, whole-phrase lookup and placeholder preservation.

Run `python tools/audit_indic_content_coverage.py --check` to check canonical
dua IDs, feature keys, name meanings, additional generated content keys and
draft labels. Additional generated-key checks apply to the 20 draft languages;
the three existing language packs retain their existing content tables.

`tools/indic_translation_sources.json` records the source corpus.
`tools/integrate_indic_drafts.py` materializes local translation caches from
`tmp/indic_translation_cache`. Translation caches and model files are excluded
from version control; the generated Kotlin packs are bundled into the app.

Integration tests cover content lookups, onboarding, names, lessons, festivals,
placeholder preservation, Salah composition and generated dua quiz options.

Final verification: debug APK assembly succeeded. The 39 localization,
Salah-content and event-bus tests passed with zero failures. The coverage audit
and cache validation passed. The broader test run encountered an unrelated
books-catalog assertion and stalled in a Salah UI scroll test; it is not a
passing full-suite result.

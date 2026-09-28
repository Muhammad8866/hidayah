# App language QA audit — 26 September 2026

## Repair status (after this audit)

- Fixed localized search refresh in Duas/Adhkar, localized Quran Surah search, and Arabic/Urdu Hadith collection aliases.
- Aligned immersive Quran page navigation with the standard reader; localized Hijri date presentation in Home, Ramadan, Settings, Calendar, Festivals, and prayer alerts.
- Converted Calendar/Naat controls and lesson/assistant/festival content display to whole-phrase lookup. English Festival guidance headings and Naat errors now have genuine English source copy. Missing religious translations remain explicit fallbacks instead of misleading partial word replacements.
- Dua meanings stored in the `englishTranslation` field are Roman Hindi. Their displayed language is now identified as Hinglish (Roman Hindi) in Duas/Adhkar and copied/shared text when no reviewed translation exists. Untranslated English titles are also identified. The underlying content still needs approved translations.
- Repaired malformed localization map syntax: 78 already truncated map fragments across six localization source files were removed, while complete entries were preserved. These fragments had no recoverable translation key or complete value; they cannot be counted as translated coverage.
- `:app:compileDebugKotlin` passed after the final copy/share label update. The focused run completed 19 tests with one failure: a missing regional Community Masjid label. That label was restored, and the localization class rerun passed all 14 tests. The other five focused tests had passed in the first run.

The severity and inventory below describe the **original audit findings**, before these repairs. Remaining content work is the 292 Dua titles and meanings, lessons, assistant responses, festival guidance, and other long prose across the 24 languages. Translation quality and religious meaning require an approved, reviewed source. A live device or emulator is still unavailable for layout and accessibility checks.

## Scope and verification

- Reviewed all 24 app language IDs and Android locale declarations. Both lists contain the same 24 entries (`MainViewModel.kt:158`, `res/xml/locales_config.xml`).
- Reviewed localization lookup, user-facing Compose screens, bundled religious content, search indexes, notifications, and existing language tests.
- Inspected available Urdu Home and Duas screenshot artifacts. No Android device or emulator was connected (`adb devices` empty; `emulator -list-avds` empty), so live device interaction, font rendering across all 24 languages, and screen-reader QA remain unverified.
- Existing automated tests check the shared Hindi reference UI keys across languages (`AppLocalizationTest.kt:90`), plus selected Urdu and RTL cases. They do not prove that every visible string or content item is translated.

## Severity 1 — Main content in the wrong language

1. **Duas and Adhkar: English is Roman Hindi/Urdu.** `Dua.translationFor` returns `englishTranslation` for every language except Urdu, but that dataset field contains Roman Hindi/Urdu (`DuasAdhkarScreen.kt:999`, `DuaModels.kt:73`, `MoreDuas.kt:10`). English users do not receive English meanings.
2. **Duas and Adhkar: almost all titles and supporting content fall back.** `localizePhrase` requires an exact whole-phrase key (`AppLocalization.kt:427`). Static extraction found 292 unique bundled dua titles and exact localization keys for only four titles anywhere in the localization files. The other 288 titles have no exact localization key. `context`, `benefit`, and full grading notes use the same fallback path (`DuasAdhkarScreen.kt:391`, `:441`, `:464`). The available Urdu screenshot visibly shows English titles and source lines next to Urdu controls and meanings.
3. **Lessons and AI Assistant: responses remain English.** Lesson titles, overviews, long lesson text, and takeaways are stored only in English (`LearningModels.kt:3-111`). The assistant's prepared answers are also English (`SourceConstrainedIslamicAssistant.kt:43-132`). `LocalizedText` cannot translate arbitrary dataset content; it substitutes only matching phrases or words (`AppLocalization.kt:300`, `:368`).
4. **Festivals: mixed English and Roman Hindi/Urdu in every language.** Festival descriptions are English (`IslamicFestivalRepository.kt:44-67`), while practices and cautions are Roman Hindi/Urdu (`FestivalAamal.kt:18`, `FestivalCautions.kt:6`). The UI displays these data fields directly through `LocalizedText` (`FestivalsScreen.kt:218-264`). Even English mode includes “Kya karein” and “Kis se bachein”; neither has a translation key.

## Severity 2 — Visible UI and search inconsistencies

5. **Islamic Calendar and Naat bypass localization.** Calendar labels such as “Islamic Calendar”, “Go to today”, weekday labels, and English Hijri month names use plain `Text` (`IslamicCalendarScreen.kt:74-155`). Naat heading, subtitle, statuses, footer, and Roman Hindi error messages also bypass the language lookup (`NaatScreen.kt:58-112`, `NaatAudioManager.kt:116-162`).
6. **Home, Ramadan, and Settings dates/statuses are mixed-language.** The Home Urdu screenshot shows an English Gregorian/Hijri date and English prayer names. Source code uses `monthNameEnglish` (`HomeScreen.kt:259`, `:276`; `RamadanScreen.kt:214`; `SettingsScreen.kt:171`). Other messages originate as fixed English strings in the ViewModel (`MainViewModel.kt:242`, `:599`, `:747-768`, `:846`).
7. **Dua, Quran, and Hadith search exclude translated display terms.** Dua search indexes original dataset fields, not localized phrases (`DuaModels.kt:155-181`). Quran audio and PDF Surah searches use number, English names/meaning, and Arabic (`QuranScreen.kt:335-341`, `QuranPdfScreens.kt:636-642`). Hadith search uses roman title and collection name (`HadithCatalog.kt:212-218`). A localized query can therefore return no match for a visible translated label.
8. **Additional UI copy lacks exact phrase keys.** A static scan of `LocalizedText` call sites found examples in Festivals, Hadith, Home, Learn, Masjid, Prayer Transparency, Qibla, Quran, Ramadan, Settings, and Community Masjid with no exact key in any language pack. Examples: “Source and translation policy”, “Choose a Mushaf”, “Masjid not in the list?”, and long explanation/error text. Word-fragment substitution may translate pieces, but does not establish a reviewed full translation and can leave mixed-language text (`AppLocalization.kt:292-310`, `:368`).
9. **Dua copy/share repeats the same language gaps.** Shared text uses the fallback title and meaning and appends an English source/grade line (`DuasAdhkarScreen.kt:1006-1015`).
10. **Quran page navigation changes order in immersive mode.** The standard reader places Previous left and Next right (`QuranScreen.kt:605-629`), whereas immersive controls place Next left and Previous right (`QuranScreen.kt:290-318`). The RTL UI test fails its position assertion (`QuranReaderUiTest.kt:66`). The intended reading order should be settled before changing either mode.

## Feature inventory

| Feature | QA result |
| --- | --- |
| Main navigation / language picker | The 24 supported IDs match Android locale tags; shared reference UI keys have an automated parity test. |
| Home / prayer times | Partial localization; English date, prayer name, and status examples verified. |
| Duas / Adhkar / Tasbeeh | Controls largely translated; dua content and search incomplete; English meaning mislabeled. |
| Ramadan | Partial localization; English Hijri month and explanatory content remain. Arabic dua text is intentionally unchanged. |
| Quran reader / audio / PDF | Arabic Mushaf intentionally unchanged; Surah meanings and some controls/status text remain English; localized search absent. |
| Hadith | Original PDF pages intentionally unchanged, per app policy. In-app catalog/control copy has gaps; PDFs available only in listed catalog languages. |
| Festivals | Mixed English and Roman Hindi/Urdu guidance; major content gap. |
| Lessons / Assistant | Major English-only content gap. |
| Masjid / Community Masjid | Shared labels are partially translated; unreviewed longer messages and dynamic data can remain English. Mosque names/addresses are external names and should not be automatically translated. |
| Qibla / Prayer Transparency | Some controls translated; long guidance and dynamically composed values can remain English. |
| Islamic Calendar / Naat | Most visible text bypasses localization. |
| Settings / More | Language picker works; Settings explanations and dynamic date/status strings have gaps. “More” is handled by a special lookup in `AppLocalization.kt:370`. |
| Prayer notifications | A persisted language lookup is used, but names/date/location inserted into messages may remain in their original language (`PrayerNotificationReceiver.kt:79-105`). Runtime channel behavior was not device-tested. |

## Test coverage and remaining QA

- The focused localization/reader suite ran 35 tests: 33 passed, 2 failed. The RTL Quran reader navigation assertion failed, consistent with the two different control orders described above. The Adhkar counting UI test hit an Espresso/Compose idle timeout, then passed in an isolated rerun. Treat that test as flaky; its initial failure does not establish a language defect.
- A full unit-suite run was stopped during the unrelated staged Hadith PDF test, which opens 70 files totaling approximately 991 MB. The focused suite above provides the relevant language test results; a full-suite pass was not established.
- Available screenshot checks: Urdu Duas list and Urdu Home inspected; both show mixed-language content. These artifacts are not a substitute for a current live device run.
- Untested: all 24 language layouts on device, text clipping/font fallback, TalkBack announcements, switching language mid-flow, clipboard/share targets, notification shade/channel labels, and native PDF display.
- Linguistic and religious translation accuracy requires qualified human review of each language. Code-level presence checks cannot certify the meaning of a translation.

## Recommended repair order

1. Separate genuine English, Roman Hindi/Hinglish, and Urdu meanings in the content model; add reviewed translations by stable content ID.
2. Translate all dua and lesson content, then festival guidance and assistant response templates. Keep Arabic source text intact.
3. Move Calendar/Naat text and remaining long UI copy into complete phrase resources, with placeholders for dynamic values.
4. Index selected-language display text for searches while preserving Arabic and original-language search.
5. Add exhaustive automated coverage for every user-facing message/content ID and language, then run screenshots and accessibility QA for all 24 languages on real devices or configured emulators.

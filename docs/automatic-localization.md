# Automatic localization of new features

Every Android build runs `generateAutoTranslations`. It detects new literal copy
in localization helpers and translates it into all 52 non-English app languages;
English uses the source. Translation JSON is cached in
`localization/translations/` and generated Kotlin is bundled for offline lookup.
This is build-time translation: users do not need a translation API key or an
internet connection to read the new copy.

Set `TRANSLATION_API_KEY` in the developer/CI environment once. Optional
`TRANSLATION_MODEL` selects the Gemini model (default `gemini-3.8-flash`). The key
is never read from Android `.env` or placed in the APK. If Python has a different
name, pass `-PtranslationPython=python3` to Gradle.

New copy written as `LocalizedText("New feature")` is detected automatically.
New raw `Text("New feature")` fails the build with a file/line diagnostic so the
developer must connect it to localization. Existing strings are grandfathered
in `legacy-baseline.json`; this does not certify their translation coverage.
The scanner handles direct quoted first arguments and `text =` arguments;
data objects, variables, multiline strings, indirect helper functions and remote
content must be registered explicitly in `source.json`. PDFs/audio are not
translated by this pipeline.

For content or dynamic copy, add an English string such as
`"Downloaded {count} pages"` to `localization/source.json`, then render:

```kotlin
Text(localizeUiTemplate("Downloaded {count} pages", language,
    mapOf("count" to count.toString())))
```

The pipeline validates nonblank values, placeholders, Arabic text and paragraph
structure. An API error, missing key, or invalid translation fails the build
without treating English fallback as completed translation. Successfully cached
batches are retained, so the next run resumes. Commit translation JSON together
with the feature; complete caches require no network/key for subsequent builds.

Run `python tools/auto_translate.py --check` for a read-only coverage check.
Machine drafts still need native-language/religious review; this automation does
not fix existing Qaida/Kalima content or legacy language gaps.

API contract: https://ai.google.dev/api/generate-content

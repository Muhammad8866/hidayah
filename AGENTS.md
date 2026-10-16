# Localization for new work

- Every new user-facing UI string must use `LocalizedText`, `LocalizedPhraseText`,
  `localizeUiText`, or `localizePhrase`. Do not add raw English `Text` literals.
- Add new long-form content and dynamic sentence templates to
  `localization/source.json` (English strings). Render these through localization
  helpers. For dynamic values use `{name}` placeholders and `localizeUiTemplate`;
  translate the entire sentence before substituting runtime values.
- `generateAutoTranslations` runs automatically during Android builds. Its Gemini
  credential is the build environment's `TRANSLATION_API_KEY`; never package this
  key in `.env`, BuildConfig, resources, or the APK.
- Preserve reviewed translations and Arabic devotional text. Generated text is a
  machine draft, and does not establish religious or linguistic approval.
- Do not extend or regenerate `localization/legacy-baseline.json` to suppress
  coverage errors. The baseline records existing copy only; legacy gaps still
  need their own migration.
- After adding copy run `python tools/auto_translate.py --check` and focused
  localization tests. Commit `localization/translations/*.json` with the feature
  so subsequent builds work offline.

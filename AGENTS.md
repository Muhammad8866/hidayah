# Advisor

- Always do what the advisor says. Consult the advisor before substantive work
  and before declaring a task done, then follow its guidance.
- Tell the advisor about every task and ask it before doing it. Do not start
  any work without first consulting the advisor.

# Core working rules

- Do every task in this project with forked agents (Agent tool, `subagent_type: "fork"`),
  however small. Split the task into its independent pieces and start all the
  forks together in one message so they run at the same time; finish as fast as
  possible. Do not use the Workflow tool. Gradle builds, emulators and heavy
  scanners still run one at a time (8GB RAM rule).
- Audit deeply and fix the root cause, aiming for 100% accuracy. Verify every
  claim against the code before reporting it.
- Never run builds (Gradle, assemble, install). The user always builds.
- Stay in scope: when solving a problem, change only what that problem needs.
  No unrelated refactors, cleanups or extra changes.

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

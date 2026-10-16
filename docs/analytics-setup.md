# Hidayah Analytics

## Status

Verified console configuration on 9 October 2026:
- Analytics account **Hidayah** (`411471660`), property **Hidayah Android** (`558201236`).
- Firebase project `hidayah-6d0cc`, Android app `1:379533400732:android:5f9c3afb11db4e084deb59`.
- Android stream **Hidayah Android** (`16096396496`), package `com.aistudio.deencompanion.quranj`.
- Reporting region India, India Time (UTC+05:30), currency INR.
- Eight event dimensions: `screen`, `feature`, `component`, `action`, `result`, `permission`, `setting`, `error_code`.
- Three event metrics: `duration_ms` (milliseconds), `milestone`, `status_code` (standard).

`app/google-services.json` targets the Hidayah Analytics project. Community Masjid and City Challenge
use a separate named Firebase app targeting the original `deen-companion-app` backend via
`FirebaseSetup`; its configuration was recovered from the existing tracked configuration.
No server data or security rules were migrated.

The debug APK and instrumentation APK build succeeded using a single worker and a 1536 MB heap.
All five `AnalyticsDispatcherTest` tests passed (consent gating, private-field rejection,
numeric parameter types, reserved lifecycle-event naming and backend-failure isolation).
Google DebugView received synthetic `app_foreground`, `ui_interaction`, `request_result` and
`media_action` events; the `screen=analytics_setup_test` parameter was inspected in the console.
The SDK upload returned HTTP 204. The first USB instrumentation smoke test passed on CPH2505;
USB was subsequently disconnected and the user requested completion without USB.
Final emulator verification logged both `screen_view` and the corrected `app_session_end` event
and uploaded the batch with HTTP 204. Screen names and numeric `duration_ms`/`status_code` values
were present in the SDK batch. Collection now starts automatically without a permission popup, including on upgrades.

## New console setup

1. The account **Hidayah** and GA4 property **Hidayah Android** are configured.
   Use India, Asia/Kolkata reporting time zone and INR currency. Leave optional Google product,
   benchmarking, technical-support and business-recommendation data sharing disabled.
2. The new Firebase project `hidayah-6d0cc` is linked to this property. The original community
   backend is configured separately; avoid replacing its resource values with Analytics-project values.
3. Confirm the Android stream uses package `com.aistudio.deencompanion.quranj` and matches the
   existing Firebase Android app. If the console offers a refreshed `google-services.json`, download
   it and compare project ID and Android app ID before replacing the local file.
4. Configure event-scoped custom dimensions: `screen`, `feature`, `component`, `action`, `result`,
   `permission`, `setting`, `error_code`. `screen_name` uses the standard screen reporting.
   Custom metrics: `duration_ms`, `milestone`, `status_code` (standard numeric event values).
   Do not mark every tap as a key event. Pick product outcomes after reviewing real usage.
5. Keep Google Signals, advertising personalization and user-provided data collection off.
   Set user/event data retention deliberately (recommended initial setting: 2 months).
6. Install a debug APK and enable DebugView with:

   ```powershell
   adb shell setprop debug.firebase.analytics.app com.aistudio.deencompanion.quranj
   ```

   Open the app, visit screens, search, play/pause audio and download content.
   Verify event names, parameters and numeric types in Firebase **Analytics > DebugView**.
   Disable debug mode afterward:

   ```powershell
   adb shell setprop debug.firebase.analytics.app .none.
   ```

For a synthetic end-to-end smoke test on a disposable emulator, build/install the debug and
Android-test APKs, enable debug mode above, and run:

```powershell
adb -s emulator-5554 shell am instrument -w -e analyticsSmoke true -e class com.example.core.analytics.AnalyticsSetupInstrumentedTest com.aistudio.deencompanion.quranj.test/androidx.test.runner.AndroidJUnitRunner
```

The opt-in test launches a real Activity, checks both Firebase project configurations, enables
collection for synthetic events and waits for a debug batch.
It does not enable collection when the regular test suite is run without `analyticsSmoke=true`.

## Tracking model

The CSV `analytics-controls.csv` maps static UI/feature observers to source locations.
Regenerate it after edits with `python tools/audit_analytics.py`.

| Event | Meaning |
| --- | --- |
| `screen_view` | Named Compose screen/tab/reader resumes |
| `screen_engagement` | Time from resume until pause or navigation |
| `ui_interaction` | Static control ID and tap/back/dismiss/refresh/toggle action |
| `search_usage` | Non-empty search remains unchanged for one second; no query text |
| `scroll_progress` | 25/50/75/100 milestones on instrumented remembered scroll states |
| `app_foreground`, `app_session_end` | Activity foreground intervals; avoids SDK-reserved `app_background` |
| `media_action` | Playback commands such as play, pause, seek, speed, next, previous, stop |
| `media_state` | Buffering, ready, completed, playback failure |
| `download_state` | Shared FastNet transfer start, bounded progress, completion/failure/cancel |
| `request_result` | Shared FastNet request status and time to response headers; no URL |
| `permission_result` | Location, notification or microphone permission outcome |
| `settings_change` | Fixed setting category; no private setting values |

Parent screen and nested tab/reader observers can coexist: their durations describe separate
scopes and must not be summed as total session time. Use app foreground duration for that.
Control IDs are fixed at the source call site. Rows rendered from lists reuse the same ID:
which item/surah/book/person was selected is intentionally not sent.
Searches count settled edits, not distinct search sessions. Scrolls can include programmatic moves.
Request timing ends at response headers, not the last downloaded byte.

## Collection and data protection

- Collection starts enabled in the manifest and Application initialization; no permission popup.
- Settings displays an Analytics disclosure; no collection switch is exposed.
- Legacy consent preferences no longer control collection.
- Ad storage, ad user data and ad personalization consent are always denied; Advertising ID
  collection is disabled; AD_ID and AdServices advertising-ID/attribution permissions are removed.
- Custom events use a closed schema. No user ID or community Firebase UID is attached.
- No query text, precise coordinates, URLs, content identifiers/titles, worship completion records,
  tasbeeh counts, recordings, notes, financial values or error exception messages are sent.
- Individual prayer tracker, counter and adhkar/tasbeeh control taps are excluded.
- Google still processes standard SDK/device/app-instance metadata and can infer approximate
  geography from connections. This is usage analytics, not anonymous or zero-data collection.
- Update the hosted privacy policy and Play
  Console Data safety declarations before distributing this version; the local policy is updated.

## Coverage limits

This is extensive explicit instrumentation, not a claim of every gesture being captured.
Forwarded callbacks do not emit a second control event. Some custom gestures,
search result counts, reader page milestones, non-FastNet
downloads and network clients are not instrumented. Crash collection is not included.
The tracking inventory currently contains 500 static sites across 44 source files. This is a
reviewable inventory, not a percentage of all possible user gestures. Inline scroll containers
and Naat next/repeat/shuffle commands are now included.


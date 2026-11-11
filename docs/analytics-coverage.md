# Analytics coverage

Android uses the existing Firebase Analytics integration and `AppAnalytics` event policy.

Added coverage:
- Qaida/Tajweed catalog and lesson screen views, engagement, scroll and search usage.
- Lesson selection, bookmarks, quiz completion, learning track, language and text-size controls.
- Qaida audio selection, queue controls, speed, repeat, practice mode, playback states and failures.
- Mirath screen views, engagement, scroll, calculate, reset, share, reference and review interactions.
- Credits, privacy policy and prayer transparency dialog views.

Removed generic `daily_tool_screen` and `tool_screen` views because each tool owns its named screen tracking. Scroll observers restart when their screen name changes, so the Qaida catalog and lesson report separately.

Search queries, estate amounts, family details, quiz scores, audio URLs and exception messages are not included in these events. Identifiers come from app catalogs; review interactions transmit only the control action.

## Funnels, streaks and performance

Onboarding funnel: `feature_milestone` with `feature=onboarding`, `item` = step, sent once per step in this order:
1. `language_chosen`
2. `location_set`
3. `notifications_answered`
4. `first_prayer_times`
5. `tour_finished`

To build it: Firebase Analytics > Explorations > Funnel exploration; add five steps, each `feature_milestone` filtered on `feature = onboarding` and `item = <step>`. Register `feature` and `item` as custom dimensions first.

Streaks: `feature_milestone` with `action=streak`, `feature` = tracker token (e.g. `roza_tracker`, `prayer_tracker`, `quran_goal`), `item` = bucket `d3`, `d7`, `d14`, `d30` or `d100`. Sent only when a bucket is reached; raw day counts are never sent.

Theme and text size are not user properties: the app follows the system theme and has no saved text size.

Audio load: `media_load` event with `feature`, `result` (ready / failed) and `duration_ms`, plus a Firebase Performance custom trace `audio_load` with the same attributes. No URLs or file names.

Already covered, no change needed:
- Crashlytics gets the current screen key and analytics event breadcrumbs.
- App start time is measured automatically by Firebase Performance.
- The Firebase Performance Gradle plugin is intentionally NOT applied, so network request URLs are not auto-collected.

Play Data Safety: no new category. Still App interactions + Diagnostics.

To check delivery on a connected development device:

```powershell
adb shell setprop debug.firebase.analytics.app com.example
```

Open Firebase Analytics DebugView and exercise the flows above. Turn off debug reporting afterwards:

```powershell
adb shell setprop debug.firebase.analytics.app .none.
```

Reference: https://firebase.google.com/docs/analytics/debugview

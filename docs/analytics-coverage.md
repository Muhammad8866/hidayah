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

To check delivery on a connected development device:

```powershell
adb shell setprop debug.firebase.analytics.app com.example
```

Open Firebase Analytics DebugView and exercise the flows above. Turn off debug reporting afterwards:

```powershell
adb shell setprop debug.firebase.analytics.app .none.
```

Reference: https://firebase.google.com/docs/analytics/debugview

package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val homeTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.HOME to listOf(
        TabTourStep("", "Welcome to Home", "Home shows today's prayer times, your daily progress and shortcuts to every main feature."),
        TabTourStep("home_location_chip", "Your city", "Prayer times are worked out for this city. Tap it to pick another city or use your device location."),
        TabTourStep("home_dates", "Today's dates", "See the Hijri and normal date. After Maghrib it also shows the Hijri date that has started tonight."),
        TabTourStep("home_error_card", "Prayer times problem", "This card appears when times can't be worked out, usually because location is missing.", optional = true),
        TabTourStep("home_error_fix", "Fix or retry", "Tap to fix location or try loading the prayer times again.", optional = true),
        TabTourStep("home_next_prayer", "Next prayer", "Shows the current or next prayer and its time. Tap the card for full prayer details."),
        TabTourStep("home_countdown", "Live countdown", "The ring counts down to the next prayer and fills as time passes.", optional = true),
        TabTourStep("home_prayer_info", "Why this time?", "Tap to see how today's times were calculated, step by step."),
        TabTourStep("home_other_times", "Other Islamic times", "Tap to show or hide Sehri end, Zawal, Iftar and other useful times.", optional = true),
        TabTourStep("home_prayer_schedule_card", "Today's schedule", "All five daily prayers with their times. Passed prayers fade and the next one is highlighted.", optional = true),
        TabTourStep("home_prayer_row", "Prayer row", "Each row shows one prayer and its start time for today.", optional = true),
        TabTourStep("home_schedule_view_all", "View all times", "Tap to also show Sunrise and extra times, or tap again to show less.", optional = true),
        TabTourStep("home_alert_prompt", "Prayer alerts", "Turn on reminders so the app can alert you at each prayer time.", optional = true),
        TabTourStep("home_alert_enable", "Turn on alerts", "Tap to allow notifications and start prayer time alerts. Tap Not now to hide this card.", optional = true),
        TabTourStep("home_for_you", "For you now", "Suggestions that fit this moment, like adhkar, Jumu'ah or Ramadan. Tap one to open it.", optional = true),
        TabTourStep("home_quick_access", "Quick access", "Shortcuts to the app's most used features, all in one place."),
        TabTourStep("home_continue_quran", "Continue Quran", "Opens the Quran right at the page where you stopped reading."),
        TabTourStep("home_action_adhkar", "Adhkar", "Opens morning and evening adhkar. Many have audio you can play."),
        TabTourStep("home_action_qibla", "Qibla", "Opens the compass that points you towards the Kaaba."),
        TabTourStep("home_action_tasbeeh", "Tasbeeh", "Opens a digital counter for your dhikr."),
        TabTourStep("home_action_hadith", "Hadith", "Opens hadith collections to read and search."),
        TabTourStep("home_action_calendar", "Calendar", "Opens the Islamic calendar with Hijri dates and events."),
        TabTourStep("home_action_ramadan", "Ramzan", "Opens Ramadan tools like Sehri and Iftar times and fasting tracker."),
        TabTourStep("home_action_naat", "Naat audio", "Opens the Naat section where you can listen to naats with audio playback."),
        TabTourStep("home_action_zakat", "Zakat", "Opens the Zakat calculator to work out how much to give."),
        TabTourStep("home_today_progress", "Today's progress", "Tracks your Salah, Quran pages and adhkar for today."),
        TabTourStep("home_progress_salah", "Salah progress", "Shows how many prayers you marked today. Tap to open the prayer tracker."),
        TabTourStep("home_progress_quran", "Quran goal", "Shows pages read against your daily goal. Tap to set or change the goal."),
        TabTourStep("home_progress_adhkar", "Adhkar progress", "Shows morning or evening adhkar done today. Tap to open adhkar."),
        TabTourStep("home_share_app", "Share app", "Tap to send this app to family and friends.")
    )
)

// The prayer-time sheet is a ModalBottomSheet (own window), so the overlay cannot point into it.
internal val homeScreenTourSteps: Map<String, List<TabTourStep>> = emptyMap()

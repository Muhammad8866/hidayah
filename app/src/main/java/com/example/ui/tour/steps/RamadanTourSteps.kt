package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val ramadanTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.RAMADAN to listOf(
        TabTourStep("", "Ramzan at a glance", "See Sehri and Iftar times for your city, a live countdown and the full Ramadan timetable. This screen has no audio."),
        TabTourStep("ramadan_refresh", "Refresh timings", "Tap to reload today's Hijri date and the Sehri and Iftar times for your city."),
        TabTourStep("ramadan_live_countdown", "Live countdown", "Shows time left until Sehri ends or Iftar begins today. It updates every second by itself.", optional = true),
        TabTourStep("ramadan_upcoming_countdown", "Ramadan countdown", "Before Ramadan, this counts the days and hours until the first night at Maghrib.", optional = true),
        TabTourStep("ramadan_first_times", "First Sehri and Iftar", "The estimated Sehri end and Iftar time for the first fast.", optional = true),
        TabTourStep("ramadan_complete", "Eid Mubarak card", "Shown when Ramadan is complete.", optional = true),
        TabTourStep("ramadan_shawwal", "Six fasts of Shawwal", "In Shawwal, a reminder about the six Sunnah fasts with its hadith reference.", optional = true),
        TabTourStep("ramadan_month_progress", "Month progress", "Shows which roza it is, how many fasts are done and the current Ashra.", optional = true),
        TabTourStep("ramadan_laylatul_qadr", "Laylatul Qadr", "In the last Ashra, this reminds you of the odd nights to seek Laylatul Qadr.", optional = true),
        TabTourStep("ramadan_duas", "Sehri and Iftar duas", "Read the intention for fasting and the dua for breaking the fast, with its reference.", optional = true),
        TabTourStep("ramadan_timetable", "Ramadan timetable", "The full month of Sehri and Iftar times. Column headers stay on top while you scroll."),
        TabTourStep("ramadan_jump_today", "Go to today", "Tap to scroll the timetable straight to today's roza.", optional = true),
        TabTourStep("ramadan_ashra", "Ashra sections", "The timetable is split into three Ashras of ten days each.", optional = true),
        TabTourStep("ramadan_today_row", "Today's row", "Today's roza is highlighted. Past days are faded.", optional = true),
        TabTourStep("ramadan_timetable_row", "Daily row", "Each row shows the roza number, date, Sehri end time and Iftar time.", optional = true),
        TabTourStep("ramadan_footnotes", "Notes on timings", "Explains the symbols and how times are worked out. Change the Hijri date in Settings if needed.", optional = true),
    )
)

internal val ramadanScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "ramadan_roza_tracker" to listOf(
        TabTourStep("", "Roza tracker", "Log each day's fast, track your Qaza balance and plan missed fasts. This tool has no audio."),
        TabTourStep("ramadan_roza_menu", "More options", "Tap the three dots to share a summary, back up or restore, mark many days or fix the balance."),
        TabTourStep("ramadan_roza_menu", "Share summary", "In the menu, Share summary sends your balance and streaks as text to any app."),
        TabTourStep("ramadan_roza_menu", "Save or restore backup", "Save backup writes your log to a file. Restore backup replaces your current log with a saved file."),
        TabTourStep("ramadan_roza_menu", "Mark a date range", "Pick a start and end date and one fast type to log up to 60 days at once."),
        TabTourStep("ramadan_roza_menu", "Correct Qaza balance", "Only to fix a count entered by mistake. Log real Qaza fasts on their day instead."),
        TabTourStep("ramadan_roza_balance", "Qaza balance", "The number of missed fasts you still need to make up."),
        TabTourStep("ramadan_roza_streaks", "Fasting streaks", "Your current and longest run of days in a row with a fast logged."),
        TabTourStep("ramadan_roza_add_missed", "Add old missed fasts", "Tap to add missed fasts from the past that are not in your daily log."),
        TabTourStep("ramadan_roza_stats", "All-time summary", "Totals of every fast type you have logged."),
        TabTourStep("ramadan_roza_ramadan_progress", "This Ramadan", "Shows how many Ramadan days you have logged this year.", optional = true),
        TabTourStep("ramadan_roza_day_nav", "Pick a day", "Use the arrows to move a day, or tap the date to open a calendar. Future days cannot be logged."),
        TabTourStep("ramadan_roza_today", "Back to today", "Tap to jump back to today's log.", optional = true),
        TabTourStep("ramadan_roza_occasion", "Special fast day", "Shown when the chosen day is a known Sunnah fast day, with its reference.", optional = true),
        TabTourStep("ramadan_roza_mark_options", "Mark the day", "Tap Ramadan, Nafl, Missed or Qaza. Missed adds to the balance and Qaza reduces it. Undo appears after saving."),
        TabTourStep("ramadan_roza_warning", "Day warning", "A warning appears if this fast type does not suit the chosen day.", optional = true),
        TabTourStep("ramadan_roza_note", "Add a note", "Write a short note for this day. It saves when you tap Done or leave the field."),
        TabTourStep("ramadan_roza_clear", "Clear the day", "Removes this day's entry after you confirm. Your Qaza balance updates too.", optional = true),
        TabTourStep("ramadan_roza_month_nav", "Change month", "Use the arrows to move between months in the calendar."),
        TabTourStep("ramadan_roza_month_stats", "Month totals", "Fasts and Qaza fasts completed in the shown month."),
        TabTourStep("ramadan_roza_days_logged", "Days logged", "The bar shows how many days of this month you have logged."),
        TabTourStep("ramadan_roza_calendar_day", "Calendar days", "Each day shows its fast type by colour. Tap a day to view or edit it. A dot means a note or Sunnah fast day."),
        TabTourStep("ramadan_roza_legend", "Colour key", "Explains which colour and icon stands for each fast type."),
        TabTourStep("ramadan_roza_qaza_plan", "Qaza plan", "Estimates when you will finish your Qaza fasts at your chosen pace."),
        TabTourStep("ramadan_roza_plan_pace", "Fasts per week", "Tap minus or plus to set how many Qaza fasts you keep each week. Weeks and finish date update.", optional = true),
        TabTourStep("ramadan_roza_sunnah", "Upcoming Sunnah fasts", "Next Sunnah fast days with their Gregorian and Hijri dates and references."),
        TabTourStep("ramadan_roza_recent", "Last 7 days", "Your log for the past week. Tap any day to open it in the editor above."),
    )
)

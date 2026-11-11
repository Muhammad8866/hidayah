package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val learnTabTourSteps: Map<TabTour, List<TabTourStep>> = emptyMap()

internal val learnScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "learn_muslimah" to listOf(
        TabTourStep("", "Muslimah mode", "A private cycle log that marks excused prayer days and tracks fasts to make up. No audio on this page."),
        TabTourStep("learn_muslimah_privacy", "Private and on-device", "Everything you log stays on this phone. Nothing is uploaded or synced."),
        TabTourStep("learn_muslimah_onboarding", "What this mode does", "Read how logging, excused prayer days and missed fasts work before you turn it on.", optional = true),
        TabTourStep("learn_muslimah_enable", "Turn on the mode", "Tap to start using Muslimah mode. You can turn it off any time.", optional = true),
        TabTourStep("learn_muslimah_status", "Current status", "Shows if a period is in progress and which day it is.", optional = true),
        TabTourStep("learn_muslimah_start_today", "Started today", "Tap to log that your period began today. It stays open until you end it.", optional = true),
        TabTourStep("learn_muslimah_start_earlier", "Started earlier", "Pick an earlier date if you forgot to log the start.", optional = true),
        TabTourStep("learn_muslimah_end_today", "Ended today", "Tap to close the open period with today as the last day.", optional = true),
        TabTourStep("learn_muslimah_end_earlier", "Ended earlier", "Pick an earlier end date if it finished before today.", optional = true),
        TabTourStep("learn_muslimah_ghusl", "Ghusl reminder", "A gentle reminder shown just after a period ends. Ask a scholar for your own situation.", optional = true),
        TabTourStep("learn_muslimah_stats", "Your patterns", "Your average period and cycle length, worked out from what you logged.", optional = true),
        TabTourStep("learn_muslimah_estimate", "Next start estimate", "A rough guess of your next start date. It is not medical advice.", optional = true),
        TabTourStep("learn_muslimah_prev_month", "Previous month", "Tap to see the month before on the calendar.", optional = true),
        TabTourStep("learn_muslimah_next_month", "Next month", "Tap to see the next month on the calendar.", optional = true),
        TabTourStep("learn_muslimah_calendar", "Cycle calendar", "Filled days are excused. Tap a past day to add or remove it.", optional = true),
        TabTourStep("learn_muslimah_legend", "Calendar key", "Explains the colours: filled is excused, a ring is the estimated start.", optional = true),
        TabTourStep("learn_muslimah_this_month", "Back to this month", "Tap to jump the calendar back to the current month.", optional = true),
        TabTourStep("learn_muslimah_fasts", "Fasts to make up", "Use minus and plus to change how many fasts you still owe. Shared with the Roza tracker.", optional = true),
        TabTourStep("learn_muslimah_add_ramadan", "Add Ramadan days", "Logged days that fell in Ramadan. Tap to add them to your fasts to make up.", optional = true),
        TabTourStep("learn_muslimah_history", "Logged periods", "A list of every period you logged, newest first.", optional = true),
        TabTourStep("learn_muslimah_edit", "Edit a period", "Tap the pencil to change the start or end date, or keep it ongoing.", optional = true),
        TabTourStep("learn_muslimah_delete", "Delete a period", "Tap the bin to remove that period. You will be asked to confirm.", optional = true),
        TabTourStep("learn_muslimah_data", "Privacy and data", "Controls for turning the mode off or removing your logs.", optional = true),
        TabTourStep("learn_muslimah_turn_off", "Turn off", "Hides the mode and stops excusing days. Your log stays on this phone.", optional = true),
        TabTourStep("learn_muslimah_delete_all", "Delete all logs", "Removes every logged day after you confirm. Your fasts balance is not changed.", optional = true),
        TabTourStep("learn_muslimah_hadith", "Hadith source", "The hadith about making up fasts, with its reference."),
        TabTourStep("learn_muslimah_disclaimer", "Planning only", "Estimates here are for planning. For your own case, ask a qualified scholar.")
    ),
    "learn_lessons" to listOf(
        TabTourStep("", "Daily micro-lessons", "Short lessons with sources you can read in a few minutes. Text only, no audio."),
        TabTourStep("learn_lessons_header", "About these lessons", "Each lesson is short, practical and cites its sources."),
        TabTourStep("learn_lessons_loading", "Loading lessons", "Lessons are loading. They will appear here in a moment.", optional = true),
        TabTourStep("learn_lessons_card", "Lesson card", "Shows the lesson title and a short overview. Tap the card to open it."),
        TabTourStep("learn_lessons_category", "Lesson topic", "The tag tells you what the lesson is about."),
        TabTourStep("learn_lessons_time", "Reading time", "About how many minutes the lesson takes to read."),
        TabTourStep("learn_lessons_open", "Read the lesson", "Opens the full lesson with key takeaways and its sources. Close it with the X.")
    )
)

package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val duasTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.DUAS to listOf(
        TabTourStep("", "Duas, Adhkar and Tasbeeh", "Find duas for every occasion, recite morning and evening adhkar, and count tasbeeh."),
        TabTourStep("duas_tabs", "Three sections", "Switch between Duas, Adhkar and Tasbeeh here. Each section has its own guide the first time you open it."),
        TabTourStep("duas_search", "Search duas", "Type a word to find a dua. Clear the box to come back."),
        TabTourStep("duas_all_banner", "All Duas", "Tap to see every dua in one long list, with its occasion shown on each card."),
        TabTourStep("duas_categories", "Choose an occasion", "Duas are grouped by occasion, such as sleep, travel or anxiety."),
        TabTourStep("duas_occasion_tile", "Occasion tile", "Each tile shows how many duas it holds. Tap it to open that list.")
    )
)

internal val duasScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "duas_list" to listOf(
        TabTourStep("", "Dua list", "These are the duas for your chosen occasion or search."),
        TabTourStep("duas_list_header", "Back and count", "The back arrow returns to the occasions. The pill shows how many duas are listed."),
        TabTourStep("duas_card", "Dua card", "Each card shows the title, a preview of the Arabic and meaning, and its source. Tap it to open the full dua."),
        TabTourStep("duas_grade", "Authenticity grade", "This badge shows the grade given by the source for this dua.", optional = true),
        TabTourStep("duas_repeat_chip", "Times to recite", "This chip shows how many times the dua is usually recited.", optional = true),
        TabTourStep("duas_clear_search", "Clear search", "No match found. Tap to clear your search and see duas again.", optional = true)
    ),
    "duas_detail" to listOf(
        TabTourStep("", "Full dua", "Read, listen to, copy or share this dua. Scroll down to see everything."),
        TabTourStep("duas_detail_close", "Close", "Tap to close this dua and go back to the list."),
        TabTourStep("duas_detail_repeat", "Times to recite", "This chip shows how many times the dua is usually recited.", optional = true),
        TabTourStep("duas_text_smaller", "Smaller Arabic", "Tap to make the Arabic text smaller."),
        TabTourStep("duas_text_larger", "Larger Arabic", "Tap to make the Arabic text bigger and easier to read."),
        TabTourStep("duas_detail_arabic", "Arabic text", "The dua in Arabic. Long-press to select and copy part of it."),
        TabTourStep("duas_detail_audio", "Listen to the dua", "This dua has audio. Tap Listen to hear it, and tap again to stop."),
        TabTourStep("duas_voice_slow", "Slow voice", "Turn on Slow to hear the Arabic device voice at a slower pace.", optional = true),
        TabTourStep("duas_voice_settings", "Voice settings", "If no Arabic voice is installed, tap Settings to add one on your phone.", optional = true),
        TabTourStep("duas_detail_text", "Pronunciation and meaning", "Read how to say the dua in Latin letters, then its translation in your language."),
        TabTourStep("duas_detail_benefit", "Virtue and benefit", "A short note on the virtue of this dua."),
        TabTourStep("duas_detail_source", "Source", "The book and reference this dua comes from."),
        TabTourStep("duas_copy", "Copy dua", "Copies this dua to your clipboard so you can paste it anywhere."),
        TabTourStep("duas_share", "Share dua", "Send this dua to others through any app on your phone."),
        TabTourStep("duas_previous", "Previous dua", "Go to the previous dua in the list without closing this page.", optional = true),
        TabTourStep("duas_next", "Next dua", "Go to the next dua in the list without closing this page.", optional = true)
    ),
    "duas_adhkar" to listOf(
        TabTourStep("", "Morning and evening adhkar", "Recite the daily adhkar one by one and keep count as you go."),
        TabTourStep("duas_adhkar_session", "Morning or evening", "Pick the morning or evening set. It is chosen for you from the current prayer time."),
        TabTourStep("duas_adhkar_progress", "Your progress", "The bar and number show how many adhkar you have finished today."),
        TabTourStep("duas_adhkar_reset_all", "Reset all", "Starts this set again from zero. You will be asked to confirm."),
        TabTourStep("duas_adhkar_card", "Dhikr card", "Tap anywhere on the card to count one recitation. A finished card folds up; tap it to open it again."),
        TabTourStep("duas_adhkar_repeat", "Times to recite", "This chip shows how many times this dhikr is recited.", optional = true),
        TabTourStep("duas_adhkar_listen", "Listen to the recording", "Audio is available. Tap Listen to play, tap again to pause. It carries on to the next dhikr."),
        TabTourStep("duas_adhkar_slow", "Slow audio", "Turn on Slow to play the recording at a slower speed."),
        TabTourStep("duas_adhkar_repeat_audio", "Repeat audio", "Turn on Repeat to play the same recording again and again."),
        TabTourStep("duas_adhkar_stop", "Stop audio", "Stops the recording that is playing.", optional = true),
        TabTourStep("duas_adhkar_retry", "Retry audio", "The audio could not play. Tap Retry to try again.", optional = true),
        TabTourStep("duas_voice_listen", "Device voice", "Where there is no recording, tap Listen to hear the Arabic in your phone's voice.", optional = true),
        TabTourStep("duas_voice_slow", "Slow voice", "Turn on Slow to hear the device voice at a slower pace.", optional = true),
        TabTourStep("duas_adhkar_benefit", "Virtue", "A short note on the virtue of this dhikr."),
        TabTourStep("duas_adhkar_counter", "Counter", "Tap the round counter to add one. The ring fills as you reach the target."),
        TabTourStep("duas_adhkar_reset_one", "Reset this dhikr", "Sets the count of this one dhikr back to zero.", optional = true),
        TabTourStep("duas_adhkar_continue", "Continue", "Takes you back to the dhikr you were on.", optional = true),
        TabTourStep("duas_adhkar_done", "All done", "You finished every dhikr in this set.", optional = true)
    ),
    "duas_tasbeeh" to listOf(
        TabTourStep("", "Tasbeeh counter", "A digital tasbeeh. Pick a dhikr and tap to count. The screen stays on while you count."),
        TabTourStep("duas_tasbeeh_presets", "Choose a dhikr", "Swipe and tap a card to pick a dhikr. Each card shows its target count."),
        TabTourStep("duas_tasbeeh_fatimi", "Tasbeeh-e-Fatimi steps", "Shows where you are in the three steps. Finished steps get a tick.", optional = true),
        TabTourStep("duas_tasbeeh_counter", "Tap to count", "Tap the big circle to count one. The ring fills and your phone vibrates at the target."),
        TabTourStep("duas_tasbeeh_next", "Next dhikr", "Round done. Tap to move to the next dhikr in the set.", optional = true),
        TabTourStep("duas_tasbeeh_stats", "Rounds and total", "Rounds shows full rounds finished. Total shows every count since the last reset."),
        TabTourStep("duas_tasbeeh_undo", "Undo", "Removes the last count if you tapped by mistake."),
        TabTourStep("duas_tasbeeh_reset", "Reset", "Sets the count and rounds back to zero. You will be asked to confirm."),
        TabTourStep("duas_tasbeeh_source", "Source", "The hadith reference for this dhikr.", optional = true)
    )
)

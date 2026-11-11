package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val recitationTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.RECITATION_COLLECTION to listOf(
        TabTourStep("", "Manzil, Ruqyah and Wazaif", "Read and listen to Quranic recitations and daily remembrance, and track what you finish each day."),
        TabTourStep("recitation_settings", "Reading options", "Tap to change text size, translation, transliteration, keep screen on, daily reset and vibration."),
        TabTourStep("recitation_collections", "Three collections", "Switch between Manzil, Ruqyah and Wazaif. Each tile shows how many items you finished today."),
        TabTourStep("recitation_progress", "Today's progress", "Shows the collection, its short summary and how many items are done today."),
        TabTourStep("recitation_streak", "Day streak", "How many days in a row you finished this collection.", optional = true),
        TabTourStep("recitation_continue", "Continue reading", "Tap to jump to the first item you have not finished yet.", optional = true),
        TabTourStep("recitation_reset_all", "Reset all", "Clears today's counts for every item in this collection. You will be asked first.", optional = true),
        TabTourStep("recitation_search", "Search collection", "Type a word to find a passage or dua by title, reference or text."),
        TabTourStep("recitation_favourites_filter", "Favourites only", "Tap to show only the items you saved with the heart. Tap again to show all."),
        TabTourStep("recitation_large_text", "Large text", "Tap to make the Arabic text bigger. Tap again to go back to normal size."),
        TabTourStep("recitation_translation_chip", "Translation", "Tap to show or hide the meaning under each dua.", optional = true),
        TabTourStep("recitation_categories", "Wazaif categories", "Pick a category to see only those wazaif. All shows everything.", optional = true),
        TabTourStep("recitation_player_bar", "Audio player", "This collection has audio recitation. The player stays at the top while you scroll.", optional = true),
        TabTourStep("recitation_play_all", "Play all", "Tap to hear every passage in this list one after another, read by the reciter shown.", optional = true),
        TabTourStep("recitation_prev_ayah", "Previous ayah", "Goes back one ayah in the recitation.", optional = true),
        TabTourStep("recitation_player_toggle", "Play or pause", "Pauses the recitation, or carries on from where it stopped.", optional = true),
        TabTourStep("recitation_next_ayah", "Next ayah", "Skips forward to the next ayah.", optional = true),
        TabTourStep("recitation_player_progress", "Listening progress", "Shows how far the recitation has gone through the queue.", optional = true),
        TabTourStep("recitation_player_options", "Audio options", "Shows speed, repeat and sleep timer. Tap to open or close these options.", optional = true),
        TabTourStep("recitation_speed", "Playback speed", "Tap to change how fast the recitation plays.", optional = true),
        TabTourStep("recitation_repeat", "Repeat", "Tap to choose how many times each passage plays. The infinity sign means it keeps repeating.", optional = true),
        TabTourStep("recitation_sleep_timer", "Sleep timer", "Tap to stop the audio by itself after some minutes. Tap again to change or turn it off.", optional = true),
        TabTourStep("recitation_player_stop", "Stop audio", "Stops the recitation completely.", optional = true),
        TabTourStep("recitation_favourite", "Save to favourites", "Tap the heart to save this item. Find it later with the Favourites filter.", optional = true),
        TabTourStep("recitation_item_menu", "More actions", "Open this menu to copy, share or reset the count of this item.", optional = true),
        TabTourStep("recitation_collapse", "Fold card", "This item is done. Tap to fold it back into a short row.", optional = true),
        TabTourStep("recitation_mushaf_text", "Quran text", "The passage in Arabic. Tap any ayah to start listening from it. The ayah being recited is highlighted.", optional = true),
        TabTourStep("recitation_listen", "Listen to passage", "This passage has audio. Tap Listen to hear it, and tap Pause to stop.", optional = true),
        TabTourStep("recitation_needs_internet", "Needs internet", "This audio is streamed, so it plays only when you are online.", optional = true),
        TabTourStep("recitation_dua_arabic", "Dua text", "The dua in Arabic, with its pronunciation and meaning below when turned on.", optional = true),
        TabTourStep("recitation_recorded_listen", "Listen to recording", "This dua has a recorded recitation. Tap Listen to play it, and tap again to pause.", optional = true),
        TabTourStep("recitation_recorded_slow", "Slow", "Turn on to play the recording at a slower pace.", optional = true),
        TabTourStep("recitation_recorded_repeat", "Repeat recording", "Turn on to keep playing the recording again and again.", optional = true),
        TabTourStep("recitation_recorded_stop", "Stop recording", "Stops the recording that is playing.", optional = true),
        TabTourStep("recitation_recorded_reciter", "Reciter", "The name of the person who recited this recording.", optional = true),
        TabTourStep("recitation_dua_voice", "Listen to dua", "Tap Listen to hear this dua read aloud by your phone's Arabic voice.", optional = true),
        TabTourStep("recitation_dhikr_counter", "Tap to count", "Tap this box each time you recite. The ring fills until you reach the target count.", optional = true),
        TabTourStep("recitation_mark_read", "Mark as read", "Tap when you finish reading. Hearing the full audio also marks it as read.", optional = true),
        TabTourStep("recitation_completed_row", "Finished item", "Finished items fold into a short row. Tap it to open the full card again.", optional = true)
    )
)

internal val recitationScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "recitation_settings" to listOf(
        TabTourStep("", "Reading options", "Change how the collection looks and counts. Changes save by themselves."),
        TabTourStep("recitation_settings_text_size", "Text size", "Use minus and plus to change the Arabic size. The sample below shows the size."),
        TabTourStep("recitation_settings_translation", "Translation", "Turn on to show the meaning under each dua."),
        TabTourStep("recitation_settings_transliteration", "Transliteration", "Turn on to show how to say each dua in Latin letters."),
        TabTourStep("recitation_settings_screen_on", "Keep screen on", "Turn on so the screen does not turn off while you read."),
        TabTourStep("recitation_settings_daily_reset", "Daily reset", "Turn on so all counts start from zero each new day."),
        TabTourStep("recitation_settings_vibration", "Vibration on count", "Turn on to feel a small vibration each time you count.")
    )
)

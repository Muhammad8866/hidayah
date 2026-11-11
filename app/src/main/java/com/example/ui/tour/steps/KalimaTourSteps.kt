package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val kalimaTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.RABBANA_DUAS to listOf(
        TabTourStep("", "40 Rabbana Duas", "The forty Quran duas that begin with Rabbana, with Arabic, transliteration, meaning and audio. Works offline."),
        TabTourStep("kalima_rabbana_header", "About this page", "The title and a short intro. Tap the back arrow to go back."),
        TabTourStep("kalima_rabbana_search", "Search duas", "Type a word, meaning, surah name or number. The list filters as you type."),
        TabTourStep("kalima_rabbana_empty", "No dua found", "Nothing matched your search. Tap Clear search to see all duas again.", optional = true),
        TabTourStep("kalima_rabbana_card", "Dua card", "Each card holds one dua: number, reference, Arabic, audio, transliteration and meaning."),
        TabTourStep("kalima_rabbana_number", "Dua number", "The position of this dua in the list of forty."),
        TabTourStep("kalima_rabbana_reference", "Quran reference", "The surah and ayah where this dua comes from."),
        TabTourStep("kalima_rabbana_arabic", "Arabic text", "The dua in Arabic, exactly as in the Quran."),
        TabTourStep("kalima_rabbana_audio", "Listen to the dua", "This dua has a recorded recitation. Tap Listen to play, tap again to pause, and use the stop button to end it."),
        TabTourStep("kalima_rabbana_audio", "Slow and Repeat", "Turn on Slow to hear it at a slower pace. Turn on Repeat to play it again and again. The reciter's name is shown below."),
        TabTourStep("kalima_rabbana_voice", "Device voice audio", "When no recording exists, Listen reads the Arabic with your phone's voice. Slow makes it slower.", optional = true),
        TabTourStep("kalima_rabbana_transliteration", "Transliteration", "The Arabic written in English letters to help you read it aloud."),
        TabTourStep("kalima_rabbana_meaning", "Meaning", "What the dua means, in your app language."),
        TabTourStep("kalima_rabbana_source", "Source", "Scroll to the end to see where the text and meanings come from.")
    ),
    TabTour.KALIMAS to listOf(
        TabTourStep("", "6 Kalime and Iman", "Learn the six Kalimas and Iman-e-Mujmal and Mufassal with meaning, audio, a reader and a quiz. Works offline."),
        TabTourStep("kalima_settings", "Reading settings", "Tap to change Arabic size, meaning language, and show or hide transliteration and meaning."),
        TabTourStep("kalima_progress", "Your progress", "Shows how many you have memorised out of the total, with a progress bar."),
        TabTourStep("kalima_resume", "Continue reading", "Opens the reader where you stopped, or the next one you have not memorised yet."),
        TabTourStep("kalima_play_all", "Play all audio", "Plays every Kalima one after another with your phone's Arabic voice. Tap Stop to end it."),
        TabTourStep("kalima_quiz", "Test yourself", "Opens a short quiz to check what you remember."),
        TabTourStep("kalima_search", "Search", "Type a name, word or meaning to find a Kalima quickly."),
        TabTourStep("kalima_filters", "Filters", "Show All, only Favourites, or only the ones Not memorised. The number shows how many are in each."),
        TabTourStep("kalima_empty", "Nothing to show", "No item matches. Clear the search or tap Show all.", optional = true),
        TabTourStep("kalima_section", "Section title", "Items are grouped. The tag shows how many in this group you have memorised."),
        TabTourStep("kalima_card", "Kalima card", "Shows the name, its meaning and the first Arabic line. Tap the card to open the reader."),
        TabTourStep("kalima_card_favourite", "Add to Favourites", "Tap the star to save this Kalima to Favourites. Tap again to remove it."),
        TabTourStep("kalima_card_memorised", "Mark as memorised", "Tap the circle when you have learnt it by heart. It counts toward your progress."),
        TabTourStep("kalima_about", "About this page", "A short note on this collection. It is available offline."),
        TabTourStep("kalima_now_playing", "Now playing bar", "Appears while audio plays. Shows which Kalima, which line and repeats left.", optional = true),
        TabTourStep("kalima_stop_all", "Stop audio", "Tap to stop the recitation.", optional = true)
    )
)

internal val kalimaScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "kalima_reader" to listOf(
        TabTourStep("", "Kalima reader", "Read one Kalima at a time, listen to it, count it and mark it memorised. Swipe to move between them."),
        TabTourStep("kalima_reader_back", "Back to list", "Tap to close the reader and go back to the list."),
        TabTourStep("kalima_reader_position", "Name and position", "The name of this Kalima and its number out of the total."),
        TabTourStep("kalima_reader_favourite", "Favourite", "Tap the star to add or remove this Kalima from Favourites."),
        TabTourStep("kalima_reader_share", "Share", "Send this Kalima's text and meaning to another app."),
        TabTourStep("kalima_reader_settings", "Reading settings", "Change Arabic size, meaning language and what text is shown."),
        TabTourStep("kalima_reader_modes", "Reading modes", "Full shows the whole text. Phrases splits it into lines you can tap to hear. Memorise hides lines so you can test yourself."),
        TabTourStep("kalima_reader_swipe", "Swipe pages", "Swipe left or right to go to the next or previous Kalima."),
        TabTourStep("kalima_reader_name", "Kalima name", "The Arabic name of this Kalima and what the name means."),
        TabTourStep("kalima_reader_full_text", "Full Arabic text", "The complete Kalima in Arabic. Long press to select and copy part of it.", optional = true),
        TabTourStep("kalima_reader_roman", "Transliteration", "The Arabic in English letters. Turn it on or off in settings.", optional = true),
        TabTourStep("kalima_reader_meaning", "Meaning", "What the Kalima means. Turn it on or off in settings.", optional = true),
        TabTourStep("kalima_reader_reveal_all", "Reveal or hide all", "In Memorise mode, show every hidden line at once, or hide them again.", optional = true),
        TabTourStep("kalima_reader_phrase", "Phrase line", "In Phrases mode, tap a line to hear just that line. In Memorise mode, tap to reveal or hide it.", optional = true),
        TabTourStep("kalima_reader_memorise_done", "Done memorising", "After checking every line, tap to mark this Kalima as memorised.", optional = true),
        TabTourStep("kalima_reader_note", "Note and reference", "Extra notes about this Kalima and its reference.", optional = true),
        TabTourStep("kalima_reader_count", "Tasbih counter", "Tap each time you recite it. Your count is saved for each Kalima."),
        TabTourStep("kalima_reader_count_reset", "Reset counter", "Tap to set the count back to zero.", optional = true),
        TabTourStep("kalima_reader_copy", "Copy", "Copy this Kalima's text and meaning to paste anywhere."),
        TabTourStep("kalima_audio_listen", "Listen to audio", "Plays this Kalima with your phone's Arabic voice. The line being read is highlighted. Tap again to stop."),
        TabTourStep("kalima_audio_slow", "Slow speed", "Turn on to hear the recitation at a slower pace, good for learning."),
        TabTourStep("kalima_audio_repeat", "Repeat count", "Tap to change how many times it plays: 1, 3, 5 or 10 times."),
        TabTourStep("kalima_audio_voice_settings", "Voice settings", "Audio needs an Arabic voice on your phone. Tap to open the phone's voice settings.", optional = true),
        TabTourStep("kalima_reader_prev", "Previous", "Go to the previous Kalima."),
        TabTourStep("kalima_reader_memorised", "Mark as memorised", "Tap when you know this Kalima by heart. Tap again to undo."),
        TabTourStep("kalima_reader_next", "Next", "Go to the next Kalima.")
    ),
    "kalima_settings_sheet" to listOf(
        TabTourStep("", "Reading settings", "Choose how the Kalimas look. Changes are saved and apply everywhere in this section."),
        TabTourStep("kalima_settings_preview", "Preview", "A sample line that shows your chosen Arabic size."),
        TabTourStep("kalima_settings_size", "Arabic text size", "Slide to make the Arabic smaller or bigger."),
        TabTourStep("kalima_settings_meaning", "Meaning language", "Pick Roman Urdu or English for the meaning."),
        TabTourStep("kalima_settings_roman", "Show transliteration", "Turn on to show the Arabic in English letters."),
        TabTourStep("kalima_settings_show_meaning", "Show meaning", "Turn on to show the meaning under the Arabic."),
        TabTourStep("kalima_settings_done", "Done", "Tap to close the settings.")
    ),
    "kalima_quiz" to listOf(
        TabTourStep("", "Test yourself", "A short quiz on the Kalimas. Pick an answer for each question and see your score at the end."),
        TabTourStep("kalima_quiz_progress", "Quiz progress", "Shows which question you are on out of the total.", optional = true),
        TabTourStep("kalima_quiz_question", "Question", "Read the question. Some show Arabic text to identify.", optional = true),
        TabTourStep("kalima_quiz_option", "Answer options", "Tap one answer. The right one turns green and a wrong pick turns red.", optional = true),
        TabTourStep("kalima_quiz_next", "Next question", "After answering, tap Next. On the last question it says Finish.", optional = true),
        TabTourStep("kalima_quiz_score", "Your score", "How many you got right, with a short tip.", optional = true),
        TabTourStep("kalima_quiz_try_again", "Try again", "Start a new round with fresh questions.", optional = true),
        TabTourStep("kalima_quiz_close", "Close", "Tap to leave the quiz.")
    )
)

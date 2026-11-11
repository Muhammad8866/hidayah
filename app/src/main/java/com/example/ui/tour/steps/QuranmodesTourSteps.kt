package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val quranmodesTabTourSteps: Map<TabTour, List<TabTourStep>> = emptyMap()

internal val quranmodesScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "quranmodes_hifz_coach" to listOf(
        TabTourStep("", "Hifz Coach", "Memorise new ayahs and revise old ones. The coach plans each day and tracks your streak and progress."),
        TabTourStep("quranmodes_hifz_plan", "Today's plan", "Shows your next step for today: ayahs due for revision first, then new ayahs to memorise."),
        TabTourStep("quranmodes_hifz_start_revision", "Start revision", "Tap to practise the ayahs that are due today, a few at a time.", optional = true),
        TabTourStep("quranmodes_hifz_start_memorising", "Start memorising", "Tap to begin a session on the next new ayahs of your chosen surah."),
        TabTourStep("quranmodes_hifz_change_passage", "Change passage", "Pick the surah, the starting ayah and how many ayahs to learn in each session.", optional = true),
        TabTourStep("quranmodes_hifz_weak", "Needs attention", "Ayahs you found hard recently. Tap Practise all, or tap one ayah to practise it alone.", optional = true),
        TabTourStep("quranmodes_hifz_end_session", "End session", "Leave the session at any time. Every ayah you already rated is saved.", optional = true),
        TabTourStep("quranmodes_hifz_listen", "Listen to the ayah", "This has audio. Tap Listen to hear the ayah recited, tap Stop to end it, and use the speed button to slow it down. Needs internet.", optional = true),
        TabTourStep("quranmodes_hifz_count_read", "Count your readings", "Read the ayah aloud and tap I read it each time. Three readings are suggested.", optional = true),
        TabTourStep("quranmodes_hifz_test_me", "Test me", "Hides the ayah so you can recite it from memory.", optional = true),
        TabTourStep("quranmodes_hifz_mic", "Check with microphone", "Tap and recite aloud. The app listens and marks which words matched. Tap again to finish.", optional = true),
        TabTourStep("quranmodes_hifz_show_ayah", "Show ayah", "Reveals the full ayah so you can compare. Use Hint first to show one word at a time.", optional = true),
        TabTourStep("quranmodes_hifz_grade", "Rate your recall", "Tap how well you remembered. This decides when the ayah comes back for revision.", optional = true)
    ),
    "quranmodes_wbw_picker" to listOf(
        TabTourStep("", "Word by word", "Learn the Quran one word at a time, with meaning, pronunciation and audio for every word."),
        TabTourStep("quranmodes_wbw_continue", "Continue learning", "Tap to jump back to the ayah where you stopped last time.", optional = true),
        TabTourStep("quranmodes_wbw_search", "Search surahs", "Type a surah name or number to find it quickly."),
        TabTourStep("quranmodes_wbw_tabs", "Surah, Juz, Downloads", "Switch between the surah list, the juz list and your downloaded audio."),
        TabTourStep("quranmodes_wbw_surah_row", "Open a surah", "Tap a surah to open it word by word."),
        TabTourStep("quranmodes_wbw_juz_row", "Open a juz", "Tap a juz to open it at its first ayah.", optional = true),
        TabTourStep("quranmodes_wbw_download", "Download audio", "Download the recitation audio to listen offline. Tap again to cancel, or tap the tick to remove it."),
        TabTourStep("quranmodes_wbw_download_all", "Download all audio", "Saves audio for every surah on this phone. The size is shown before it starts.", optional = true),
        TabTourStep("quranmodes_wbw_delete_all", "Delete all audio", "Removes all downloaded audio to free space. Audio can still play online.", optional = true)
    ),
    "quranmodes_wbw_reader" to listOf(
        TabTourStep("", "Surah reader", "Read the surah word by word. Every ayah and every word has audio."),
        TabTourStep("quranmodes_wbw_back", "Back to list", "Close this surah and return to the surah list."),
        TabTourStep("quranmodes_wbw_jump", "Jump to ayah", "Tap the surah name to go straight to any ayah number."),
        TabTourStep("quranmodes_wbw_reader_download", "Download surah audio", "Save this surah's audio so it plays without internet."),
        TabTourStep("quranmodes_wbw_speed", "Playback speed", "Change how fast the recitation plays, slower for learning."),
        TabTourStep("quranmodes_wbw_settings", "Display settings", "Show or hide meanings, pronunciation and translation, keep playing the next ayah, and change the Arabic size."),
        TabTourStep("quranmodes_wbw_ayah_play", "Play the ayah", "This has audio. Tap play to hear the ayah; each word lights up as it is recited. Tap again to stop."),
        TabTourStep("quranmodes_wbw_word", "Tap a word", "Opens the word with its meaning. Tap Listen to hear it, and mark it learned when you know it."),
        TabTourStep("quranmodes_wbw_next_surah", "Next surah", "At the end of the surah, tap to open the next one.", optional = true)
    ),
    "quranmodes_tafsir_audio" to listOf(
        TabTourStep("", "Tafseer audio", "Listen to the Sirat ul Jinan tafseer in Urdu, surah by surah or para by para."),
        TabTourStep("quranmodes_tafsir_collection", "Tilawat or Tafseer", "Switch between Quran recitation and tafseer audio."),
        TabTourStep("quranmodes_tafsir_resume", "Continue listening", "Tap play to resume the tafseer from where you stopped.", optional = true),
        TabTourStep("quranmodes_tafsir_mode", "Surah or Para wise", "Choose to list the tafseer by surah or by para."),
        TabTourStep("quranmodes_tafsir_offline_count", "Offline count", "Shows how many surahs of tafseer are saved on this phone."),
        TabTourStep("quranmodes_tafsir_search", "Search surahs", "Type a surah name or number to find its tafseer.", optional = true),
        TabTourStep("quranmodes_tafsir_surah_row", "Play a surah", "Tap a surah to play its tafseer audio. Tap again to pause, or resume where you left off.", optional = true),
        TabTourStep("quranmodes_tafsir_open_video", "Official video", "This surah has a video only. Tap to watch it on the official site.", optional = true),
        TabTourStep("quranmodes_tafsir_para_row", "Play a para", "Tap a para to play the tafseer of all its surahs in order.", optional = true),
        TabTourStep("quranmodes_tafsir_para_play", "Play para", "Starts the tafseer audio for this whole para.", optional = true),
        TabTourStep("quranmodes_tafsir_para_download", "Download para", "Saves this para's tafseer audio to listen offline.", optional = true),
        TabTourStep("quranmodes_tafsir_speed", "Playback speed", "Change how fast the tafseer plays.", optional = true),
        TabTourStep("quranmodes_tafsir_seek", "Seek bar", "Drag to move forward or back in the tafseer.", optional = true),
        TabTourStep("quranmodes_tafsir_stop", "Stop", "Stops the tafseer audio and closes the player.", optional = true),
        TabTourStep("quranmodes_tafsir_previous", "Previous surah", "Plays the tafseer of the previous surah.", optional = true),
        TabTourStep("quranmodes_tafsir_play_pause", "Play or pause", "Pause the tafseer, then tap again to continue.", optional = true),
        TabTourStep("quranmodes_tafsir_next", "Next surah", "Plays the tafseer of the next surah.", optional = true)
    )
)

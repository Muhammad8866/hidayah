package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val quranTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.QURAN to listOf(
        TabTourStep("", "Welcome to Al-Quran", "Read the Quran, listen to recitation, practise memorising and learn words, all in this tab."),
        TabTourStep("quran_segments", "Four Quran modes", "Read opens the Mushaf books, Listen plays recitation audio, Coach helps you memorise and Words teaches word meanings."),
        TabTourStep("quran_continue_reading", "Continue reading", "Shows the book and page you read last. Tap it to open that exact page again.", optional = true),
        TabTourStep("quran_translation", "Quran with translation", "Read every ayah with its translation, and hear Arabic recitation with spoken translation audio."),
        TabTourStep("quran_mushaf_title", "Choose a Mushaf", "These are the Quran books you can read. The count shows how many are available."),
        TabTourStep("quran_mushaf_row", "Open a Mushaf", "Tap a book to open it. Each card shows its name, page count, size and your last read page."),
        TabTourStep("quran_mushaf_status", "Online or offline", "Offline means the book is saved on your phone. Online means it needs internet to open."),
        TabTourStep("quran_download", "Download a Mushaf", "Tap the download button to save this book. A progress bar shows while it downloads.", optional = true),
        TabTourStep("quran_remove_download", "Remove a download", "Tap the bin to delete the saved copy and free space. The book still opens online.", optional = true),
        TabTourStep("quran_offline_hint", "Read without internet", "Download a Mushaf once and you can read it anytime without internet.")
    )
)

internal val quranScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "quran_pdf_reader" to listOf(
        TabTourStep("", "Mushaf reader", "Read the Quran page by page. Your page is saved, and pages read count towards your Quran goal."),
        TabTourStep("quran_pdf_close", "Close the book", "Tap the cross to go back to the list of Mushaf books."),
        TabTourStep("quran_pdf_navigator", "Go to surah or page", "Shows the current surah, para and page. Tap it to jump to any surah, para, page, bookmark or favorite."),
        TabTourStep("quran_pdf_bookmark", "Bookmark this page", "Tap to save this page. Tap again to remove it. Saved pages are in Bookmarks."),
        TabTourStep("quran_pdf_menu", "Reader menu", "Bookmarks, go to page, reading mode, share the PDF, share the app and the user guide are here."),
        TabTourStep("quran_pdf_full_screen", "Full screen", "Tap to hide the bars and see only the page. Tap the page to bring them back."),
        TabTourStep("", "Turn and zoom pages", "Swipe to turn pages. Pinch or double-tap a page to zoom in and out."),
        TabTourStep("quran_pdf_previous", "Previous page", "Tap to go back one page."),
        TabTourStep("quran_pdf_slider", "Page slider", "Drag the slider to move quickly through the book. The number shows the page you will land on.", optional = true),
        TabTourStep("quran_pdf_next", "Next page", "Tap to go forward one page."),
        TabTourStep("", "Volume key shortcut", "Press a Volume key or the Menu key to open the reader menu. You can turn this off in the menu.")
    ),
    "quran_translation_reader" to listOf(
        TabTourStep("", "Quran with translation", "Read each ayah in Arabic with its translation. You can also listen to audio for every ayah."),
        TabTourStep("quran_tr_back", "Go back", "Tap to close the translation reader. Any playing audio stops."),
        TabTourStep("quran_tr_bookmarks", "Your bookmarks", "Opens the list of ayahs you saved. Tap one to jump straight to it."),
        TabTourStep("quran_tr_settings", "Reading settings", "Show or hide Arabic, change text sizes, choose what the audio plays and turn on continue to next surah."),
        TabTourStep("quran_tr_pick_surah", "Choose a surah", "Shows the current surah. Tap it to pick any of the 114 surahs."),
        TabTourStep("quran_tr_pick_text", "Choose translation", "Tap to pick the language and translator of the text."),
        TabTourStep("quran_tr_pick_audio", "Choose audio voice", "This reader has audio. Tap to pick the voice and language of the spoken translation."),
        TabTourStep("quran_tr_note", "About translations", "Only the Arabic is the Quran. A translation shows its meaning."),
        TabTourStep("quran_tr_ayah_card", "Ayah card", "Each card shows the ayah number, the Arabic text and its translation. The ayah playing is highlighted."),
        TabTourStep("quran_tr_ayah_bookmark", "Bookmark an ayah", "Tap to save this ayah to your bookmarks. Tap again to remove it."),
        TabTourStep("quran_tr_ayah_copy", "Copy ayah", "Copies the Arabic, translation and reference so you can paste it anywhere."),
        TabTourStep("quran_tr_ayah_share", "Share ayah", "Sends the ayah and its translation to another app."),
        TabTourStep("quran_tr_ayah_status", "Status card", "Makes a picture of this ayah that you can share as a status."),
        TabTourStep("quran_tr_ayah_play", "Play this ayah", "Audio is available. Tap play to hear this ayah, and tap again to pause."),
        TabTourStep("quran_tr_surah_nav", "Next or previous surah", "At the end of a surah, tap these buttons to open the surah before or after it.", optional = true),
        TabTourStep("quran_tr_player", "Audio player", "This bar plays the surah aloud ayah by ayah. It shows which ayah is playing and the audio mode."),
        TabTourStep("quran_tr_speed", "Playback speed", "Tap to change how fast the audio plays. Each tap moves to the next speed."),
        TabTourStep("quran_tr_repeat", "Repeat ayah", "Turn on to hear the same ayah again and again. It lights up when on."),
        TabTourStep("quran_tr_prev_ayah", "Previous ayah", "While audio plays, tap to go back one ayah."),
        TabTourStep("quran_tr_play", "Play or pause", "Tap to start the audio from where you are reading. Tap again to pause."),
        TabTourStep("quran_tr_next_ayah", "Next ayah", "While audio plays, tap to skip to the next ayah."),
        TabTourStep("quran_tr_stop", "Stop audio", "Tap to stop the audio completely.", optional = true)
    ),
    "quran_audio_library" to listOf(
        TabTourStep("", "Quran recitation audio", "Listen to the full Quran recited surah by surah. Audio keeps playing in the background."),
        TabTourStep("quran_audio_resume", "Continue listening", "Starts the last recitation again from the second you stopped.", optional = true),
        TabTourStep("quran_audio_surah_wise", "Surah wise", "Shows all 114 surahs so you can play any one of them."),
        TabTourStep("quran_audio_para_wise", "Para wise", "Shows the 30 paras. Playing a para plays its surahs one after another."),
        TabTourStep("quran_audio_offline_count", "Offline audio count", "Shows how many of the 114 surahs are saved on your phone."),
        TabTourStep("quran_audio_search", "Search surah", "Type a surah name, meaning or number to find it quickly.", optional = true),
        TabTourStep("quran_audio_surah_row", "Surah row", "Tap a surah to play its recitation. It shows meaning, ayah count, download progress or resume time.", optional = true),
        TabTourStep("quran_audio_row_play", "Play or pause surah", "Tap play to listen to this surah. A spinner shows while it loads; tap again to pause.", optional = true),
        TabTourStep("quran_audio_row_download", "Download audio", "Tap to save this recitation so it plays without internet. Tap the spinner to cancel.", optional = true),
        TabTourStep("quran_audio_row_downloaded", "Saved audio", "The green tick means the audio is saved. Tap it to remove the download.", optional = true),
        TabTourStep("quran_audio_para_row", "Para row", "Shows the surahs in this para and how many are offline. Tap to play the whole para.", optional = true),
        TabTourStep("quran_audio_play_para", "Play para", "Tap to play all surahs of this para in order.", optional = true),
        TabTourStep("quran_audio_download_para", "Download para", "Tap to save every surah of this para for offline listening.", optional = true),
        TabTourStep("quran_audio_now_playing", "Now playing bar", "Appears while a surah plays. It shows the surah name and controls the audio.", optional = true),
        TabTourStep("quran_audio_speed", "Playback speed", "Tap to make the recitation slower or faster.", optional = true),
        TabTourStep("quran_audio_seek", "Seek bar", "Drag to jump to any point in the recitation. The times show played and total length.", optional = true),
        TabTourStep("quran_audio_stop", "Stop", "Tap to stop the recitation.", optional = true),
        TabTourStep("quran_audio_previous", "Previous surah", "Tap to play the surah before this one.", optional = true),
        TabTourStep("quran_audio_play_pause", "Play or pause", "Tap to pause the recitation, and tap again to carry on.", optional = true),
        TabTourStep("quran_audio_next", "Next surah", "Tap to play the next surah.", optional = true)
    )
)

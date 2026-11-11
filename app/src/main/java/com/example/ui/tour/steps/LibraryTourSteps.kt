package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

/** Steps shared by the Fateha and Jumu'ah Khutbah guides, which use the same screen layout. */
private fun guideSteps(overviewTitle: String, overviewBody: String): List<TabTourStep> = listOf(
    TabTourStep("", overviewTitle, overviewBody),
    TabTourStep("library_guide_intro", "About this guide", "A short note on what this guide covers and how to use it."),
    TabTourStep("library_guide_play_all", "Play all audio", "This guide has audio. Tap Play all to hear every Quran part in order; tap again to pause.", optional = true),
    TabTourStep("library_guide_retry", "Text did not load", "If the Quran text fails to load, tap Retry to try again.", optional = true),
    TabTourStep("library_guide_section", "Guide sections", "The guide is split into sections. Each heading starts a new part."),
    TabTourStep("library_guide_step", "Numbered steps", "Each card is one step, in order. It shows the Arabic, its reading and its meaning."),
    TabTourStep("library_guide_step", "Repeat count", "A pill like \"3 times\" on a step tells you how many times to recite it.", optional = true),
    TabTourStep("library_guide_listen", "Listen to a step", "Tap Listen to hear only this step's Quran recitation. Tap Pause to stop it.", optional = true),
    TabTourStep("library_guide_audio_error", "Audio problem", "Shown if the audio cannot play. Check your internet and tap play again.", optional = true),
    TabTourStep("library_guide_adab", "Etiquette and references", "Good manners for this practice, each with its source reference.", optional = true),
    TabTourStep("library_guide_sources", "Sources", "Where the text and translations in this guide come from."),
)

internal val libraryTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.FATEHA to guideSteps(
        "Fateha guide",
        "A step-by-step guide to reciting Fateha, with Quran audio you can play."
    ),
    TabTour.JUMUAH_KHUTBAH to guideSteps(
        "Jumu'ah Khutbah guide",
        "A step-by-step guide to the Friday khutbah, with Quran audio you can play."
    ),
    TabTour.BOOKS to listOf(
        TabTourStep("", "Islamic Books", "Find and read books by Ahle Sunnat scholars in many languages. No audio here."),
        TabTourStep("library_books_source_info", "About this library", "Tap the info button to learn how this library works."),
        TabTourStep("library_world_refresh", "Refresh the list", "Downloads the latest book list now. It also updates by itself once a week.", optional = true),
        TabTourStep("library_books_search", "Search books", "Type a book name, author or topic. Matching books and pages appear below."),
        TabTourStep("library_world_category", "Category filter", "Show one category, such as Hadees or Seerat, or all categories. It is remembered.", optional = true),
        TabTourStep("library_world_language", "Language filter", "Pick one language, or All languages, to narrow the list. It is remembered."),
        TabTourStep("library_world_sort", "Sort books", "Order the list by Most read, Title A-Z, Title Z-A or Newest."),
        TabTourStep("library_world_recent", "Recent searches", "Tap a past search to run it again.", optional = true),
        TabTourStep("library_world_clear_recent", "Clear searches", "Tap the trash button to forget all recent searches.", optional = true),
        TabTourStep("library_books_continue", "Continue reading", "Opens the book you read last, where you stopped.", optional = true),
        TabTourStep("library_books_recent", "Recent books", "Other books you opened recently. Tap one to carry on.", optional = true),
        TabTourStep("library_world_shelf", "Saved offline", "Books you downloaded. Tap to read without internet; the badge shows your last page.", optional = true),
        TabTourStep("library_world_saved_book", "Remove a saved book", "Long-press a saved book to delete its download.", optional = true),
        TabTourStep("library_books_shelves", "Choose a shelf", "Switch between All books and the Ladies / Muslimah shelf. Your choice is remembered.", optional = true),
        TabTourStep("library_books_cover", "Classical books", "Each cover is a book. It shows its languages and how many volumes are offline. Tap to open it.", optional = true),
        TabTourStep("library_books_row", "Muslimah titles", "Books for ladies. Tap a row to see its volumes and languages.", optional = true),
        TabTourStep("library_world_item", "Book card", "Shows the title, author, language and views. Tap to see its volumes.", optional = true),
        TabTourStep("library_world_load_more_retry", "Load more failed", "If more books fail to load, tap Retry to try again.", optional = true),
        TabTourStep("library_books_page_hit", "Page results", "Tap a page result to open that book right on that page.", optional = true),
    ),
)

internal val libraryScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "library_books_work" to listOf(
        TabTourStep("", "Book page", "See all volumes of this book, choose a language, read or download. No audio here."),
        TabTourStep("library_books_work_hero", "Book summary", "Shows the title, chosen language, number of volumes and how many are offline."),
        TabTourStep("library_books_read", "Read or resume", "Opens the volume you read last, or the first volume if you are new."),
        TabTourStep("library_books_download_all", "Download all", "Saves every volume in this language so you can read offline.", optional = true),
        TabTourStep("library_books_all_offline", "All offline", "Every volume in this language is already on your phone.", optional = true),
        TabTourStep("library_books_language", "Edition language", "Pick the language edition you want. Your choice is remembered for this book.", optional = true),
        TabTourStep("library_books_volume", "Volumes", "Each row is one volume with its page count and your last page. Tap to read it."),
        TabTourStep("library_books_volume_download", "Download a volume", "Tap the download icon to save this volume for offline reading. Needs internet.", optional = true),
        TabTourStep("library_books_volume_remove", "Remove a download", "Tap the tick icon to delete the offline copy. It stays readable online.", optional = true),
    ),
    "library_world_detail" to listOf(
        TabTourStep("", "Book details", "See this book's volumes, read it online or save it offline. No audio here."),
        TabTourStep("library_world_detail_about", "About the book", "The author, language, views and number of volumes."),
        TabTourStep("library_world_read", "Read or continue", "Opens the volume you read last, or the first one.", optional = true),
        TabTourStep("library_world_volume", "Volumes", "Each row is one volume. Tap to read, use the icon to download or remove it.", optional = true),
    ),
)

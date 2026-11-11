package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val hadithcalTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.HADITH to listOf(
        TabTourStep("", "Hadith library", "Read the original Hadith books as PDFs, online or saved offline. There is no audio here, only reading."),
        TabTourStep("hadithcal_source_info", "About this library", "Tap to read how these books are kept. The PDF pages are never changed."),
        TabTourStep("hadithcal_stats", "Library numbers", "Shows how many collections and volumes there are, and how many you saved offline.", optional = true),
        TabTourStep("hadithcal_search", "Search books", "Type a book name or a topic. Results show matching books and matching pages inside the books."),
        TabTourStep("hadithcal_language", "Book language", "Tap to show books in one language only, or all languages.", optional = true),
        TabTourStep("hadithcal_sort", "Sort the list", "Tap to sort by library order, title, number of volumes or offline books."),
        TabTourStep("hadithcal_topics", "Quick topics", "Tap a topic to search it at once.", optional = true),
        TabTourStep("hadithcal_continue", "Continue reading", "Opens the book you read last, at the page where you stopped.", optional = true),
        TabTourStep("hadithcal_recent", "Recently read", "Other books you opened lately. Tap one to open it again.", optional = true),
        TabTourStep("hadithcal_sittah", "Sihah Sittah", "The six main Hadith collections. Tap one to see its volumes."),
        TabTourStep("hadithcal_collection_card", "Other collections", "More Hadith collections. Tap a card to see its volumes and languages.", optional = true),
        TabTourStep("hadithcal_search_scope", "Books or pages", "While searching, switch between matching books and matching pages inside the books.", optional = true),
        TabTourStep("hadithcal_page_hit", "Page result", "A page that matches your search. Tap to open the book right at that page.", optional = true),
        TabTourStep("hadithcal_source_note", "Original PDFs", "Tap to read how these books are kept. The PDF pages are never changed.", optional = true),
    ),
    TabTour.FESTIVALS to listOf(
        TabTourStep("", "Islamic occasions", "See every Islamic occasion of the year, with a countdown and a list of good deeds for each."),
        TabTourStep("hadithcal_festivals_refresh", "Refresh dates", "Tap to reload the occasion dates for your city."),
        TabTourStep("hadithcal_festivals_next", "Next occasion", "The closest coming occasion with its date and countdown. Tap to open its details.", optional = true),
        TabTourStep("hadithcal_festivals_upcoming", "Upcoming occasions", "Later occasions, grouped by Hijri month.", optional = true),
        TabTourStep("hadithcal_festivals_row", "Occasion row", "Shows the date, time left and how many deeds you ticked. Tap to open its details.", optional = true),
        TabTourStep("hadithcal_festivals_earlier", "Earlier this year", "Tap to show or hide occasions that have already passed.", optional = true),
        TabTourStep("hadithcal_festivals_note", "About the dates", "Dates depend on local moon sighting and may move by a day.", optional = true),
    ),
    TabTour.CALENDAR to listOf(
        TabTourStep("", "Islamic calendar", "See today's Hijri date and browse the Hijri months with matching dates."),
        TabTourStep("hadithcal_calendar_today", "Today's date", "Today's Hijri and normal date for your city. It also shows the date after Maghrib."),
        TabTourStep("hadithcal_calendar_prev", "Previous month", "Tap to go back one Hijri month."),
        TabTourStep("hadithcal_calendar_month", "Month shown", "The Hijri month and year, with its matching normal dates below."),
        TabTourStep("hadithcal_calendar_next", "Next month", "Tap to go forward one Hijri month."),
        TabTourStep("hadithcal_calendar_weekdays", "Week days", "Days of the week. Friday is highlighted."),
        TabTourStep("hadithcal_calendar_day", "Pick a day", "Big number is the Hijri day, small number is the normal date. Tap a day to see it below."),
        TabTourStep("hadithcal_calendar_go_today", "Go to today", "Appears after you move away. Tap to jump back to today.", optional = true),
        TabTourStep("hadithcal_calendar_selected", "Selected day", "Full Hijri and normal date of the day you picked. Date corrections in Settings show here."),
    ),
)

internal val hadithcalScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "hadithcal_collection" to listOf(
        TabTourStep("", "Collection volumes", "All volumes of this collection. Open one to read, or save it for offline use."),
        TabTourStep("hadithcal_collection_header", "Collection summary", "Shows the Arabic name, number of volumes, languages and pages."),
        TabTourStep("hadithcal_collection_resume", "Continue this collection", "Opens the volume you read last in this collection.", optional = true),
        TabTourStep("hadithcal_collection_language", "Choose language", "Tap a language to show only volumes in that language.", optional = true),
        TabTourStep("hadithcal_download_all", "Download all volumes", "Saves every volume so you can read without internet. Needs internet to download."),
        TabTourStep("hadithcal_volume", "Volume row", "Shows if it is offline, its language and pages. Tap to open the book."),
        TabTourStep("hadithcal_volume_download", "Download one volume", "Tap to save just this volume for offline reading.", optional = true),
        TabTourStep("hadithcal_volume_delete", "Remove download", "Tap to delete the saved copy. You can still read it online.", optional = true),
    ),
    "hadithcal_reader" to listOf(
        TabTourStep("", "Book reader", "Read the original PDF page by page. Your last page is saved automatically."),
        TabTourStep("hadithcal_reader_close", "Close the book", "Tap to go back to the library."),
        TabTourStep("hadithcal_reader_title", "Go to a page", "Shows the volume, page and chapter. Tap to type a page number and jump there.", optional = true),
        TabTourStep("hadithcal_reader_bookmark", "Bookmark page", "Tap to bookmark this page, tap again to remove it.", optional = true),
        TabTourStep("hadithcal_reader_index", "Index and bookmarks", "Opens the book's chapter list and your bookmarks. Tap one to jump to it.", optional = true),
        TabTourStep("hadithcal_reader_menu", "More options", "Share this book as a PDF or switch to full screen.", optional = true),
        TabTourStep("hadithcal_reader_page", "Book page", "Swipe to turn pages. Pinch or double tap to zoom. Tap once for full screen."),
        TabTourStep("hadithcal_reader_fullscreen_bar", "Full screen controls", "Turn pages or tap the exit icon to leave full screen.", optional = true),
        TabTourStep("hadithcal_reader_prev", "Previous page", "Tap to go back one page.", optional = true),
        TabTourStep("hadithcal_reader_slider", "Page slider", "Drag to move quickly through the book.", optional = true),
        TabTourStep("hadithcal_reader_page_number", "Page number", "The current page. Tap it to type a page and jump there.", optional = true),
        TabTourStep("hadithcal_reader_next", "Next page", "Tap to go forward one page.", optional = true),
    ),
    "hadithcal_festival_detail" to listOf(
        TabTourStep("", "Occasion details", "Learn about this occasion and tick the good deeds you have done."),
        TabTourStep("hadithcal_festival_title", "Occasion name", "Name in English and Arabic, with how long until it starts."),
        TabTourStep("hadithcal_festival_when", "When it is", "The normal date, Hijri date and, for nights, the evening it begins."),
        TabTourStep("hadithcal_festival_about", "Why it matters", "A short note on the importance of this occasion."),
        TabTourStep("hadithcal_festival_aamal", "What to do", "Recommended deeds for this occasion, with your progress.", optional = true),
        TabTourStep("hadithcal_festival_deed", "Tick a deed", "Tap a deed to mark it done. Ticks are saved for this year.", optional = true),
        TabTourStep("hadithcal_festival_cautions", "What to avoid", "Things to stay away from on this occasion, with references.", optional = true),
    ),
)

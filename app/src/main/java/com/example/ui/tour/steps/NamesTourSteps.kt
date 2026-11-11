package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val namesTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.ALLAH_NAMES to listOf(
        TabTourStep("", "99 Names of Allah", "All 99 names of Allah with their meanings. Works fully offline, with no audio on this page."),
        TabTourStep("names_allah_hadith", "Virtue of the names", "A hadith about the reward for learning these names, with its source."),
        TabTourStep("names_allah_search", "Search names", "Type a name, meaning, Arabic word or number. The list filters as you type."),
        TabTourStep("names_allah_empty", "No match found", "Nothing matched your search. Change or clear the words to see the names again.", optional = true),
        TabTourStep("names_allah_row", "Name card", "Each card shows the number, the name in English letters, its meaning and the Arabic."),
        TabTourStep("names_allah_source", "List source", "Scroll to the end to see where this list and its meanings come from.")
    ),
    TabTour.PROPHET_NAMES to listOf(
        TabTourStep("", "Names of the Prophet", "99 blessed names of the Prophet with their meanings. Works fully offline, with no audio on this page."),
        TabTourStep("names_prophet_hadith", "Virtue of the names", "A hadith about these blessed names, with its source."),
        TabTourStep("names_prophet_search", "Search names", "Type a name, meaning, Arabic word or number. The list filters as you type."),
        TabTourStep("names_prophet_empty", "No match found", "Nothing matched your search. Change or clear the words to see the names again.", optional = true),
        TabTourStep("names_prophet_row", "Name card", "Each card shows the number, the name in English letters, its meaning and the Arabic."),
        TabTourStep("names_prophet_source", "List source", "Scroll to the end to see where this list and its meanings come from.")
    ),
    TabTour.BABY_NAMES to listOf(
        TabTourStep("", "Islamic Baby Names", "Find a name for a boy or girl with its Arabic spelling and meaning. Works offline, with no audio on this page."),
        TabTourStep("names_baby_search", "Search names", "Type a name or a meaning. The list filters as you type."),
        TabTourStep("names_baby_gender", "Boys or girls", "Tap All, Boys or Girls to show only those names. Works together with search."),
        TabTourStep("names_baby_count", "Names shown", "Shows how many names match out of the full list."),
        TabTourStep("names_baby_reset", "Reset filters", "Nothing matched. Tap to clear the search and the boy or girl filter.", optional = true),
        TabTourStep("names_baby_row", "Name card", "Each card shows the name, its Arabic spelling and its meaning."),
        TabTourStep("names_baby_gender_pill", "Boy or girl tag", "This tag tells you if the name is for a boy or a girl."),
        TabTourStep("names_baby_arabic", "Arabic spelling", "The name written in Arabic letters.")
    )
)

internal val namesScreenTourSteps: Map<String, List<TabTourStep>> = emptyMap()

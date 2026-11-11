package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val guidesTabTourSteps: Map<TabTour, List<TabTourStep>> = emptyMap()

internal val guidesScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "guides_qibla" to listOf(
        TabTourStep("", "Qibla direction", "This screen shows which way to face the Kaaba for prayer. It has no audio."),
        TabTourStep("guides_qibla_no_location", "Location needed", "Qibla needs your city. Tap the button to use your device location, or pick a city on Home.", optional = true),
        TabTourStep("guides_qibla_status", "Turn guidance", "Tells you to turn left or right, or that you are facing the Kaaba. The phone buzzes once when lined up.", optional = true),
        TabTourStep("guides_qibla_hint", "How to point", "Point the top of your phone toward the Kaaba. Without a compass it shows the angle from true north.", optional = true),
        TabTourStep("guides_qibla_dial", "Compass dial", "The arrow points to the Qibla and the dial turns with your phone. It lights up when you are aligned.", optional = true),
        TabTourStep("guides_qibla_metrics", "Bearing and distance", "Shows the exact Qibla angle and how far the Kaaba is from you.", optional = true),
        TabTourStep("guides_qibla_bearing", "Qibla bearing", "Degrees clockwise from true north. Use it with any reliable compass.", optional = true),
        TabTourStep("guides_qibla_distance", "Kaaba distance", "The straight distance from your location to the Kaaba in kilometres.", optional = true),
        TabTourStep("guides_qibla_calibrate", "Compass help", "Appears when the compass is missing or weak. Move the phone in a figure eight, away from metal.", optional = true),
        TabTourStep("guides_qibla_tip", "Best reading", "Hold your phone flat and away from metal or magnets for an accurate direction.", optional = true),
        TabTourStep("guides_qibla_use_location", "Use current location", "Tap to switch from your chosen city to your exact device location.", optional = true)
    ),
    "guides_salah" to listOf(
        TabTourStep("", "Salah guide", "Learn how to pray, rak'at counts, recitations and rules. This guide is text only, with no audio."),
        TabTourStep("guides_salah_madhhab", "Change school", "Tap Change to pick your madhhab. Rak'at counts and steps update to match it."),
        TabTourStep("guides_salah_tabs", "Salah sections", "Switch between daily rak'at, the step guide, recitations, rules and more.", optional = true),
        TabTourStep("guides_salah_tab_daily", "Daily rak'at", "See the rak'at of each daily prayer, split into Fard, Sunnah and Witr.", optional = true),
        TabTourStep("guides_salah_tab_guide", "Step-by-step guide", "Walk through a full prayer one step at a time.", optional = true),
        TabTourStep("guides_salah_tab_duas", "Recitations", "Read every recitation of Salah in Arabic with transliteration and meaning.", optional = true),
        TabTourStep("guides_salah_tab_rules", "Rules", "Conditions, obligations and things that break the prayer, explained simply.", optional = true),
        TabTourStep("guides_salah_tab_special", "More topics", "Qibla, special prayers and other topics like travel and missed prayers.", optional = true),
        TabTourStep("guides_salah_prayer_card", "Prayer card", "Each card shows a prayer and its rak'at parts. Tap it to see full details and the niyyah.", optional = true),
        TabTourStep("guides_salah_learn_fard", "Learn the Fard", "Opens the step guide set to this prayer's Fard rak'at.", optional = true),
        TabTourStep("guides_salah_learn_unit", "Learn this part", "Opens the step guide for just this Sunnah or Fard part.", optional = true),
        TabTourStep("guides_salah_guide_controls", "Guide settings", "Choose what the step guide should show before you start.", optional = true),
        TabTourStep("guides_salah_form", "Number of rak'at", "Pick which prayer form to learn, like 2, 3 or 4 rak'at or Witr.", optional = true),
        TabTourStep("guides_salah_worshipper", "Man or woman", "Some positions differ for men and women. Pick one to see the right steps.", optional = true),
        TabTourStep("guides_salah_praying_as", "Alone or behind imam", "Steps change slightly when you pray behind an imam.", optional = true),
        TabTourStep("guides_salah_beginner", "Beginner mode", "On shows one step at a time. Off shows the whole prayer as one list.", optional = true),
        TabTourStep("guides_salah_step_progress", "Step progress", "This bar shows how far you are through the prayer.", optional = true),
        TabTourStep("guides_salah_previous", "Previous step", "Go back one step.", optional = true),
        TabTourStep("guides_salah_next", "Next step", "Go to the next step. On the last step it starts again.", optional = true),
        TabTourStep("guides_salah_recitation_toggle", "Show or hide", "Tap the recitation title to open or close its text.", optional = true),
        TabTourStep("guides_salah_recitation_card", "Recitation text", "Arabic text with transliteration, meaning and source. Read along; there is no audio here.", optional = true),
        TabTourStep("guides_salah_topic_card", "Topic card", "Tap a topic to read its points, school notes and reference.", optional = true),
        TabTourStep("guides_salah_topic_learn", "Learn it", "Opens the step guide for the prayer this topic is about.", optional = true),
        TabTourStep("guides_salah_qibla", "Open Qibla", "Opens the Qibla compass to find the prayer direction.", optional = true)
    ),
    "guides_hajj_umrah" to listOf(
        TabTourStep("", "Hajj and Umrah", "A step-by-step guide with duas, round counters and a checklist. It has no audio."),
        TabTourStep("guides_hajj_tabs", "Guide sections", "Switch between the Umrah guide, the Hajj guide, a quick counter and essentials."),
        TabTourStep("guides_hajj_tab_umrah", "Umrah", "Every Umrah step in order, from Ihram to the end.", optional = true),
        TabTourStep("guides_hajj_tab_hajj", "Hajj", "Every Hajj step in order for the Hajj type you choose.", optional = true),
        TabTourStep("guides_hajj_tab_counter", "Counter", "A big tap counter for Tawaf and Sa'i rounds.", optional = true),
        TabTourStep("guides_hajj_tab_essentials", "Essentials", "Packing checklist, key tips and a visit to Madinah.", optional = true),
        TabTourStep("guides_hajj_type", "Type of Hajj", "Pick Tamattu', Qiran or Ifrad. The steps change and each type keeps its own progress.", optional = true),
        TabTourStep("guides_hajj_progress", "Your progress", "Shows how many steps you have done and the next one. It is saved on this phone.", optional = true),
        TabTourStep("guides_hajj_start_again", "Start again", "Clears ticked steps and round counters for this guide, after you confirm.", optional = true),
        TabTourStep("guides_hajj_jump", "Jump to a step", "Tap a chip to open that step. A tick shows steps already done.", optional = true),
        TabTourStep("guides_hajj_step_card", "Step card", "Tap a step to open how to do it, its dua and a counter if needed.", optional = true),
        TabTourStep("guides_hajj_round_counter", "Round counter", "Counts your Tawaf, Sa'i or pebble rounds. The count is saved automatically.", optional = true),
        TabTourStep("guides_hajj_round_add", "Count a round", "Tap once each time you finish a round or throw a pebble.", optional = true),
        TabTourStep("guides_hajj_round_undo", "Undo", "Removes the last counted round if you tapped by mistake.", optional = true),
        TabTourStep("guides_hajj_round_reset", "Reset counter", "Sets the count back to zero, after you confirm.", optional = true),
        TabTourStep("guides_hajj_round_mark_done", "Rounds complete", "When all rounds are counted, tap to mark this step as done.", optional = true),
        TabTourStep("guides_hajj_dua", "Dua to recite", "Arabic dua with transliteration, meaning and source. Read it; there is no audio.", optional = true),
        TabTourStep("guides_hajj_step_done", "Mark step done", "Tick this step when you finish it. The next step opens by itself.", optional = true),
        TabTourStep("guides_hajj_step_undone", "Mark not done", "Untick a step if you marked it by mistake.", optional = true),
        TabTourStep("guides_hajj_quick_kind", "Tawaf or Sa'i", "Choose what you are counting. Each keeps its own count.", optional = true),
        TabTourStep("guides_hajj_quick_pad", "Tap to count", "Tap this big pad once per round. The phone buzzes when all seven are done.", optional = true),
        TabTourStep("guides_hajj_quick_undo", "Undo last tap", "Removes the last round you counted.", optional = true),
        TabTourStep("guides_hajj_quick_reset", "Reset count", "Starts the count again from zero, after you confirm.", optional = true),
        TabTourStep("guides_hajj_prep", "Preparation checklist", "Tap to open or close the checklist. The bar shows how ready you are.", optional = true),
        TabTourStep("guides_hajj_prep_item", "Tick an item", "Tap an item to tick it as ready. Ticks are saved on this phone.", optional = true),
        TabTourStep("guides_hajj_section", "Guide sections", "Tap a card to open tips like Ihram rules and what to avoid.", optional = true)
    )
)

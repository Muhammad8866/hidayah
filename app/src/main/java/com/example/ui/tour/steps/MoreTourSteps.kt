package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val moreTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.MORE to listOf(
        TabTourStep("", "Welcome to More", "All extra tools, guides, audio collections and app settings live here."),
        TabTourStep("more_settings", "Settings button", "Tap the gear to open Settings for language, prayer alerts and prayer times."),
        TabTourStep("more_search", "Search every feature", "Type a word to find any tool or page on this screen. Results show as one list."),
        TabTourStep("more_clear_search", "Clear search", "Nothing matched your words. Tap here to clear the search and see every feature again.", optional = true),
        TabTourStep("more_daily_tools", "Daily tools", "Everyday helpers for prayer, fasting, counting and learning. The number shows how many there are."),
        TabTourStep("more_salah_row", "Namaz / Salah", "Opens a step-by-step prayer guide, including special prayers."),
        TabTourStep("more_prayer_tracker_row", "Prayer tracker", "Log each prayer you offer and keep count of missed (Qaza) prayers."),
        TabTourStep("more_roza_tracker_row", "Roza tracker", "Log your fasts and keep a balance of missed (Qaza) fasts."),
        TabTourStep("more_tally_counter_row", "Tally counter", "A tap counter for dhikr. Each tap adds one to the count."),
        TabTourStep("more_quran_goal_row", "Quran goal", "Set daily pages and track your progress toward a full khatm."),
        TabTourStep("more_qaida_tajweed_row", "Noorani Qaida / Tajweed", "Learn Arabic reading and recitation rules, lesson by lesson."),
        TabTourStep("more_zakat_row", "Zakat calculator", "Enter your savings and assets to get an estimate of your 2.5% Zakat."),
        TabTourStep("more_mirath_row", "Inheritance calculator", "Add family members to see estimated inheritance shares."),
        TabTourStep("more_hajj_umrah_row", "Hajj & Umrah", "A step-by-step guide to the rites of Hajj and Umrah."),
        TabTourStep("more_muslimah_row", "Muslimah mode", "A private, cycle-aware log that stays on your phone."),
        TabTourStep("more_quiz_row", "Islamic quiz", "Answer questions to test and grow your knowledge."),
        TabTourStep("more_challenges_row", "City challenges", "Good-deed challenges where your area competes with the whole city."),
        TabTourStep("more_explore", "Explore section", "Reading, listening and learning resources. Tap any row to open it."),
        TabTourStep("more_qibla_row", "Qibla Direction", "Shows the direction of the Kaaba from where you are."),
        TabTourStep("more_calendar_row", "Islamic Calendar", "See Hijri dates and the key Islamic days of the year."),
        TabTourStep("more_ramadan_row", "Ramzan Timetable", "Sehri and Iftar times for your city."),
        TabTourStep("more_hadith_row", "Hadith", "Read sayings of the Prophet with translation."),
        TabTourStep("more_allah_names_row", "99 Names of Allah", "Asma ul Husna, each name with its meaning."),
        TabTourStep("more_prophet_names_row", "Names of the Prophet", "The 99 blessed names of the Prophet with their meanings."),
        TabTourStep("more_rabbana_row", "40 Rabbana Duas", "Duas from the Quran that begin with Rabbana, with meaning."),
        TabTourStep("more_books_row", "Islamic Books", "Read Islamic books such as Bahar-e-Shariat inside the app."),
        TabTourStep("more_naat_row", "Naat audio", "Voice-only naats with no music. Open it to play and listen."),
        TabTourStep("more_azan_row", "Azan of the world", "Listen to azans and set one as your prayer alert sound."),
        TabTourStep("more_recitation_collection_row", "Manzil, Ruqyah, Wazaif", "Quranic recitations and daily remembrance, with audio to listen to."),
        TabTourStep("more_festivals_row", "Festivals", "Islamic occasions and the good deeds (aamal) for each."),
        TabTourStep("more_kalimas_row", "6 Kalime & Iman", "The six Kalimas, Iman-e-Mujmal and Iman-e-Mufassal with meaning."),
        TabTourStep("more_qaidah_row", "Madani Qaidah", "Learn to read the Quran step by step."),
        TabTourStep("more_fateha_row", "Fateha (Isal-e-Sawab)", "A step-by-step method for Fateha with the Quran text to read."),
        TabTourStep("more_jumuah_khutbah_row", "Jumu'ah Khutbah", "The Arabic Friday khutbah with its meaning."),
        TabTourStep("more_baby_names_row", "Islamic Baby Names", "Find baby names with their meanings."),
        TabTourStep("more_settings_row", "Settings & Privacy", "Change language, prayer alerts, prayer time methods and privacy."),
        TabTourStep("more_app_tour_row", "App tour", "Replay the quick guide to every main feature of the app."),
        TabTourStep("more_privacy_policy_row", "Privacy Policy", "Read what data the app uses and why."),
        TabTourStep("more_support_developer_row", "Support the Developer", "Make a voluntary contribution of any amount through Razorpay or PayPal.")
    )
)

internal val moreScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "more_settings" to listOf(
        TabTourStep("", "Settings & Privacy", "Set your language, prayer alerts and how prayer times are worked out."),
        TabTourStep("more_settings_header", "Back and title", "Tap the back arrow to return to More."),
        TabTourStep("more_settings_language", "App language", "Tap to pick the language the whole app is shown in."),
        TabTourStep("more_settings_prayer_alerts", "Prayer alerts switch", "Turn on to get an alert when each prayer time starts."),
        TabTourStep("more_settings_exact_alarm", "Exact alerts", "Allow alarms and reminders so the Azan alert comes exactly on time.", optional = true),
        TabTourStep("more_settings_battery", "Battery settings", "If alerts stop or come late, set battery use to Unrestricted for this app.", optional = true),
        TabTourStep("more_settings_azan_tone", "Choose Azan sound", "Opens the Azan list. Listen to each azan and pick the one your prayer alerts play."),
        TabTourStep("more_settings_test_alert", "Test prayer alert", "Sends a test alert now so you can hear the sound and check alerts work."),
        TabTourStep("more_settings_calc_method", "Calculation Authority", "Pick the method used to work out prayer times."),
        TabTourStep("more_settings_asr_method", "Asr calculation", "Choose how the Asr time is worked out."),
        TabTourStep("more_settings_high_latitude", "High-latitude adjustment", "A rule for places far north or south where nights are very short."),
        TabTourStep("more_settings_hijri_calendar", "Hijri calendar", "Choose which regional Hijri calendar the app follows, or keep it automatic."),
        TabTourStep("more_settings_hijri_adjust", "Moon sighting adjustment", "Tap -1 d or +1 d to shift the Hijri date to match local moon sighting."),
        TabTourStep("more_settings_madhhab", "Madhhab", "Sets the notes shown in the Salah guide. It does not change prayer times."),
        TabTourStep("more_settings_share", "Share app", "Send a link to this app to family and friends.")
    )
)

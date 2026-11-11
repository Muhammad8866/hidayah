package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val qaidaTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.QAIDAH to listOf(
        TabTourStep("", "Madani Qaidah", "Step-by-step reading lessons, from single letters to joined words. Many lessons have audio you can tap to hear."),
        TabTourStep("", "Lessons done count", "The line under the title shows how many lessons you have marked as done."),
        TabTourStep("qaida_madani_row", "Lesson card", "Tap a lesson to open it. A speaker icon means it has audio, and a tick means you marked it done.")
    )
)

internal val qaidaScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "qaida_madani_lesson" to listOf(
        TabTourStep("", "Qaidah lesson", "One lesson with its letters or words. Tap any word that has audio to hear how it is read."),
        TabTourStep("qaida_madani_note", "Lesson note", "A short note on what this lesson teaches and how to practise it."),
        TabTourStep("qaida_madani_play_all", "Play all audio", "Plays every word of this lesson one after another. Tap again to stop. Internet is needed the first time.", optional = true),
        TabTourStep("qaida_madani_done", "Mark lesson done", "Tap when you have learnt this lesson. Tap again to unmark it."),
        TabTourStep("qaida_madani_cell", "Word tile", "Shows the Arabic with its reading in English letters. Tap to hear it; the tile lights up while it plays.")
    ),
    "qaida_tajweed" to listOf(
        TabTourStep("", "Qaida and Tajweed", "Learn Noorani Qaida and Tajweed rules with audio, drills and short quizzes. Your progress is saved on this phone."),
        TabTourStep("qaida_language", "Explanation language", "Choose English or Roman Urdu / Hindi for the lesson text. Arabic stays the same."),
        TabTourStep("qaida_hero", "Your progress", "Shows your streak, how many lessons you finished and the lesson to do next."),
        TabTourStep("qaida_track_progress", "Track progress", "How many lessons of this track you have completed."),
        TabTourStep("qaida_continue", "Continue learning", "Opens the next lesson you have not finished yet."),
        TabTourStep("qaida_track_qaida", "Noorani Qaida track", "Shows the basic reading lessons, from letters to joining them."),
        TabTourStep("qaida_track_tajweed", "Tajweed track", "Shows lessons on Tajweed rules such as Ikhfa, Idgham and Madd."),
        TabTourStep("qaida_search", "Search lessons", "Type a lesson name, rule or letter. Results come from both tracks."),
        TabTourStep("qaida_bookmarks_filter", "Bookmarks filter", "Tap to show only lessons you bookmarked. The number shows how many you saved."),
        TabTourStep("qaida_empty", "Nothing to show", "No lesson matches your search or bookmarks. Change the search or turn off the filter.", optional = true),
        TabTourStep("qaida_lesson_row", "Lesson path", "Each card is one lesson in order. It shows if it is done, in progress or next, and your best quiz score."),
        TabTourStep("qaida_open_quran", "Open Quran", "Opens the Quran so you can practise reading real ayahs."),
        TabTourStep("qaida_download_audio", "Download all audio", "Saves every lesson recording to your phone so audio plays without internet. Shows download progress."),
        TabTourStep("qaida_reset", "Reset progress", "Clears completed lessons, scores, bookmarks and streak. You are asked to confirm first.", optional = true),
        TabTourStep("qaida_reset_confirm", "Confirm reset", "Tap to clear all your Qaida and Tajweed progress. This cannot be undone.", optional = true)
    ),
    "qaida_tajweed_lesson" to listOf(
        TabTourStep("", "Lesson page", "Read the rule, listen to the audio, practise and then take a short quiz."),
        TabTourStep("qaida_all_lessons", "All lessons", "Goes back to the full list of lessons."),
        TabTourStep("qaida_bookmark", "Bookmark lesson", "Tap to save this lesson in your bookmarks. Tap again to remove it."),
        TabTourStep("qaida_rule_card", "Rule card", "Explains the rule of this lesson, with a tip when there is one."),
        TabTourStep("qaida_rule_letter", "Rule letters", "Tap a letter to hear how it sounds.", optional = true),
        TabTourStep("qaida_audio_bar", "Audio player", "This lesson has audio. Use this player to listen and repeat after the reciter."),
        TabTourStep("qaida_audio_status", "Audio status", "Shows what is playing, loading or any error. The first play needs internet."),
        TabTourStep("qaida_audio_prev", "Previous recording", "While playing all, jumps back to the previous recording.", optional = true),
        TabTourStep("qaida_audio_toggle", "Play or pause", "Starts, pauses or retries the audio. If nothing was picked, it plays the whole lesson.", optional = true),
        TabTourStep("qaida_audio_next", "Next recording", "While playing all, skips to the next recording.", optional = true),
        TabTourStep("qaida_audio_play_all", "Play all", "Plays every recording of this lesson in order.", optional = true),
        TabTourStep("qaida_audio_speed", "Audio speed", "Pick a slower or faster speed. Slow speed helps you catch each sound.", optional = true),
        TabTourStep("qaida_audio_repeat", "Repeat audio", "Turn on to keep repeating the recording until you stop it.", optional = true),
        TabTourStep("qaida_audio_stop", "Stop audio", "Stops the audio fully.", optional = true),
        TabTourStep("qaida_font_smaller", "Smaller Arabic", "Makes the Arabic text smaller.", optional = true),
        TabTourStep("qaida_font_larger", "Larger Arabic", "Makes the Arabic text bigger and easier to read.", optional = true),
        TabTourStep("qaida_sound_card", "Letter tile", "Tap a letter or syllable to hear it. The tile is outlined while it plays.", optional = true),
        TabTourStep("qaida_clip_row", "Ayah recording", "Tap to hear a full ayah that shows this rule. Tap again to pause.", optional = true),
        TabTourStep("qaida_examples", "Reading practice", "Read these Arabic lines aloud slowly, ideally to a teacher.", optional = true),
        TabTourStep("qaida_drill", "Listening drill", "Hear a sound and pick the matching letter to test your ear.", optional = true),
        TabTourStep("qaida_drill_play", "Play the sound", "Plays the sound for this round. Listen, then pick an answer below.", optional = true),
        TabTourStep("qaida_drill_option", "Pick the letter", "After the sound plays, tap the letter you heard. Green means right, red means wrong.", optional = true),
        TabTourStep("qaida_drill_score", "Drill score", "Shows how many sounds you got right.", optional = true),
        TabTourStep("qaida_drill_next", "Next round", "Moves to the next sound.", optional = true),
        TabTourStep("qaida_drill_again", "Play drill again", "Starts the drill again with new rounds.", optional = true),
        TabTourStep("qaida_quiz", "Quick practice", "Answer a few questions on this lesson. Get all right to complete the lesson."),
        TabTourStep("qaida_quiz_answer", "Answer choice", "Tap the answer you think is right. The correct answer is then highlighted.", optional = true),
        TabTourStep("qaida_quiz_next", "Next question", "Moves to the next question after you answer.", optional = true),
        TabTourStep("qaida_quiz_score", "Quiz score", "Your score for this quiz. Your best score is saved.", optional = true),
        TabTourStep("qaida_quiz_retry", "Try again", "Starts the quiz again from the first question.", optional = true),
        TabTourStep("qaida_quiz_next_lesson", "Go to next lesson", "Opens the next lesson after a full score.", optional = true),
        TabTourStep("qaida_nav_prev", "Previous lesson", "Opens the lesson before this one."),
        TabTourStep("qaida_nav_next", "Next lesson", "Opens the lesson after this one.")
    )
)

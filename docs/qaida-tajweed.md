# Noorani Qaida / Tajweed

Open **More → Noorani Qaida / Tajweed**. The course has two tracks, Noorani Qaida (Arabic reading) and Tajweed (recitation rules), with an English / Roman Urdu-Hindi explanation switch. Lessons and quizzes work offline. Audio and the teaching-reference link require internet access.

The Arabic alphabet includes letter names. Reading drills are original practice examples, not a reproduction of a published Noorani Qaida book. Roman explanations are not pronunciation transcriptions. A teacher should check articulation and recitation.

## Lesson list

- A segmented control switches between the two tracks; the chosen track is remembered (`track_tajweed`).
- The header card shows progress for the selected track, overall progress and the daily streak. **Continue** resumes through `qaidaNextLesson`.
- Search looks across both tracks. The **Bookmarks** chip limits the list to bookmarked lessons. Both have empty states.
- Each lesson card shows its number in the track, completed / in-progress status, the best quiz score and a bookmark marker.
- **Reset** clears completed lessons, scores, bookmarks and the streak after a confirmation dialog.

## Lesson view

- Header row: back to all lessons, Arabic text size (24–44 sp, remembered as `arabic_size`) and a bookmark toggle.
- Rule card, optional tip card, and the rule's letters as chips that play the letter name when tapped.
- Audio player: play/pause, previous/next, stop, repeat, **Play all** (sequential queue) and speed chips (0.5×, 0.75×, 1×), with loading and error-with-retry states. The playing letter, syllable or recording is highlighted.
- The alphabet and syllable grids adapt their column count to the screen width and are laid out right to left.
- Quiz: one question at a time with progress dots. Each question is answered once and the correct option is highlighted. A perfect score completes the lesson; otherwise **Try again** restarts the quiz. Wrong answers give haptic feedback.
- Previous / Next buttons move within the lesson's own track. System back returns from a lesson to the list, and clears an active search.

## Storage

Progress lives in the dedicated `qaida_learning` preferences through `QaidaProgress` (completed lessons, bookmarks, best scores, last opened lesson, streak). The same file holds the UI settings `roman`, `track_tajweed` and `arabic_size`.

## Audio

Tap alphabet cards to hear letter names. Vowel, sukoon and shadda lessons offer letter recordings from [QuranFor.me](https://quranfor.me/learn/arabic/alphabet). Other lessons offer contextual full-ayah recitations by Al-Husary from [EveryAyah](https://everyayah.com/data/Husary_64kbps/), rather than recordings of the original written drills. Playback stops when changing lessons or tracks, pauses when leaving the foreground, and releases the player when leaving the feature. First playback needs internet; the shared disk cache can replay retained audio. No third-party audio is bundled. Automatic pronunciation assessment is not included.

## References

References used to check the basic teaching rules:

- [Reach the Goal via Tajweed Rules](https://download.understandquran.com/fileadmin/user_upload/extras/tajweed/Tajweed_Rules.pdf), hosted by Understand Al-Qur'an Academy.
- [Learn Tajweed: The Easy Way](https://download.understandquran.com/fileadmin/user_upload/courses/tajweed/english/Tajweed_Text_Book.pdf), Understand Al-Qur'an Academy.

## Validation

Run `:app:testDebugUnitTest --tests com.example.ui.screens.QaidaTajweedScreenTest`. The tests cover the quiz flow (imperfect attempt, retry, completion), saved progress and continuation, separate tracks and the remembered track, alphabet ordering, the narrow dark Roman flow, the audio player (play/pause, speed, repeat, queue, stop on lesson exit) with a fake controller, bookmarks, reset, search, lesson navigation and clip/quiz data sanity. Results for the redesigned screen have not been recorded here yet; device speaker playback has not been manually verified.

package com.example.ui.tour.steps

import com.example.ui.tour.TabTour
import com.example.ui.tour.TabTourStep

internal val naatazanTabTourSteps: Map<TabTour, List<TabTourStep>> = mapOf(
    TabTour.NAAT to listOf(
        TabTourStep("", "Naat audio", "Listen to naats without music from several reciters. Play online or download them to hear offline."),
        TabTourStep("naatazan_naat_continue", "Continue listening", "Tap to resume the last naat from where you stopped.", optional = true),
        TabTourStep("naatazan_naat_reciters", "Choose a reciter", "Swipe sideways and tap a reciter card to see their naats. Each card shows how many naats and how many are offline."),
        TabTourStep("naatazan_naat_search", "Search naats", "Type a few letters of a naat title to find it in this reciter's list."),
        TabTourStep("naatazan_naat_filters", "Filter chips", "Show all naats, one category, your Favourites or only Downloaded naats."),
        TabTourStep("naatazan_naat_play_all", "Play all", "This has audio. Tap to play every naat in the list, one after another."),
        TabTourStep("naatazan_naat_shuffle_all", "Shuffle play", "Plays the naats in this list in a random order."),
        TabTourStep("naatazan_naat_download_all", "Download all", "Saves every naat in this list to your phone so it plays without internet. You confirm first.", optional = true),
        TabTourStep("naatazan_naat_stop_downloads", "Stop downloads", "Shown while naats are downloading. Tap to stop them.", optional = true),
        TabTourStep("naatazan_naat_delete_all", "Delete all downloads", "In the Downloaded filter, removes every offline naat here to free space. You confirm first.", optional = true),
        TabTourStep("naatazan_naat_recent", "Recently played", "Your last naats, newest first. Tap a card to play it again.", optional = true),
        TabTourStep("naatazan_naat_track", "Play a naat", "Tap any naat to start its audio. The playing naat is highlighted with moving bars."),
        TabTourStep("naatazan_naat_favourite", "Favourite", "Tap the heart to save a naat in Favourites. Tap again to remove it."),
        TabTourStep("naatazan_naat_download", "Download one naat", "Tap to save this naat's audio offline. A ring shows progress, a retry icon shows after a failure, and a bin deletes it."),
        TabTourStep("naatazan_naat_mini_player", "Mini player", "Shows the naat now playing. Tap it to open the full player with seek bar, speed and repeat.", optional = true),
        TabTourStep("naatazan_naat_mini_play", "Play or pause", "Pauses or resumes the naat audio.", optional = true),
        TabTourStep("naatazan_naat_mini_next", "Next naat", "Skips to the next naat in the queue.", optional = true),
        TabTourStep("naatazan_naat_mini_close", "Close player", "Stops the audio and hides the player.", optional = true)
    ),
    TabTour.AZAN to listOf(
        TabTourStep("", "Azan of the world", "Listen to azans from many countries and pick one as your prayer alert tone."),
        TabTourStep("naatazan_azan_alert_card", "Prayer alert tone", "Shows the azan chosen for Fajr and for the other prayers. Prayer alerts must be on in Settings to hear it."),
        TabTourStep("naatazan_azan_clear_tone", "Use default sound", "Tap the cross to go back to the normal notification sound for those prayers.", optional = true),
        TabTourStep("naatazan_azan_search", "Search azans", "Search by azan name, muezzin or country."),
        TabTourStep("naatazan_azan_filters", "Filter chips", "Show all azans, only Fajr azans, only downloaded ones, or azans from one country."),
        TabTourStep("naatazan_azan_track", "Listen to an azan", "This has audio. Tap an azan to hear a preview, and tap it again to stop. Audio stops when you leave this screen."),
        TabTourStep("naatazan_azan_bell", "Set as alert tone", "Tap the bell to play this azan when the prayer time enters. Tap a lit bell to remove it."),
        TabTourStep("naatazan_azan_download", "Download azan", "Saves this azan's audio on your phone so it plays without internet."),
        TabTourStep("naatazan_azan_delete", "Delete download", "Removes the offline copy of this azan to free space.", optional = true)
    )
)

internal val naatazanScreenTourSteps: Map<String, List<TabTourStep>> = mapOf(
    "naatazan_now_playing" to listOf(
        TabTourStep("", "Now playing", "The full naat player. Control the audio, speed and repeat here."),
        TabTourStep("naatazan_np_artwork", "Reciter artwork", "Shows the reciter's initials. Moving bars mean the audio is playing."),
        TabTourStep("naatazan_np_favourite", "Favourite", "Tap the heart to keep this naat in Favourites."),
        TabTourStep("naatazan_np_download", "Download", "Save this naat to play offline, or delete the saved copy."),
        TabTourStep("naatazan_np_speed", "Playback speed", "Tap to make the naat audio play slower or faster."),
        TabTourStep("naatazan_np_seek", "Seek bar", "Drag to jump to any part of the naat. Times show where you are and the full length."),
        TabTourStep("naatazan_np_shuffle", "Shuffle", "Turn on to play the next naats in a random order."),
        TabTourStep("naatazan_np_previous", "Previous", "Goes back to the previous naat."),
        TabTourStep("naatazan_np_play", "Play or pause", "Pauses or resumes the naat audio."),
        TabTourStep("naatazan_np_next", "Next", "Skips to the next naat in the queue."),
        TabTourStep("naatazan_np_repeat", "Repeat", "Tap to cycle: repeat off, repeat the list, or repeat this one naat.")
    )
)

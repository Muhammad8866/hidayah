package com.example.quran

import android.view.KeyEvent

/**
 * Lets the open Quran reader take the Menu and Volume keys, which reach the activity rather than
 * the Compose tree. The reader sets [onMenuKey] while it is on screen and clears it on leaving.
 */
object ReaderKeys {
    /** Opens the reader menu; null when no reader is on screen. */
    @Volatile var onMenuKey: (() -> Unit)? = null

    /** Off when the reader leaves the volume keys to the system. */
    @Volatile var volumeEnabled: Boolean = true

    /** True when [event] belongs to the reader and must not reach the system. */
    fun handle(event: KeyEvent): Boolean {
        val open = onMenuKey ?: return false
        val volume = event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        if (event.keyCode != KeyEvent.KEYCODE_MENU && !(volume && volumeEnabled)) return false
        // Key-up and repeats are swallowed with the press so the volume panel never appears.
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) open()
        return true
    }
}

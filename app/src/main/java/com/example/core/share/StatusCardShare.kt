package com.example.core.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/** Renders a [StatusCard] to a PNG in the app cache and opens the share sheet (WhatsApp "My status" etc.). */
object StatusCardShare {
    private const val DIR = "status_cards"
    private const val FILE = "status_card.png"

    // A second tap while a card is rendering would write the same file twice and hold two large bitmaps.
    internal val inFlight = AtomicBoolean(false)

    /** Always overwrites one file, so repeated shares never grow the cache. */
    fun write(context: Context, card: StatusCard): File {
        val dir = File(context.cacheDir, DIR).apply { mkdirs() }
        val file = File(dir, FILE)
        val bitmap = StatusCardRenderer.render(context, card)
        try {
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally {
            bitmap.recycle()
        }
        return file
    }

    fun sendIntent(uri: Uri, caption: String): Intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /** Renders off the main thread, then shows the chooser. Returns false if anything failed; ignores taps while busy. */
    suspend fun share(context: Context, card: StatusCard, caption: String): Boolean {
        if (!inFlight.compareAndSet(false, true)) return true
        try {
            val file = withContext(Dispatchers.Default) { runCatching { write(context, card) }.getOrNull() } ?: return false
            return runCatching {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val chooser = Intent.createChooser(sendIntent(uri, caption), null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }.isSuccess
        } finally {
            inFlight.set(false)
        }
    }
}

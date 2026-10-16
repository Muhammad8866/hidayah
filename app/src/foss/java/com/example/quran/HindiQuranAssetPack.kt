package com.example.quran

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** The F-Droid APK bundles the Hindi PDF in its assets and copies it out on first open. */
internal object HindiQuranAssetPack {
    private const val PDF = "quran_hindi_kanzul_iman.pdf"

    private fun target(context: Context) = File(context.filesDir, "hindi_quran/$PDF")

    fun file(context: Context): File? = target(context).takeIf(File::isFile)

    suspend fun fetch(context: Context, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        file(context)?.let { return@withContext it }
        val out = target(context)
        out.parentFile?.mkdirs()
        val partial = File(out.path + ".part")
        context.assets.openFd(PDF).use { fd ->
            val total = fd.length
            fd.createInputStream().use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var copied = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        copied += read
                        if (total > 0) onProgress((copied.toFloat() / total).coerceIn(0f, 1f))
                    }
                }
            }
        }
        partial.renameTo(out)
        out
    }

    fun remove(context: Context, onComplete: () -> Unit) {
        target(context).delete()
        onComplete()
    }
}

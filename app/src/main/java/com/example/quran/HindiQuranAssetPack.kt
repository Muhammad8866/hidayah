package com.example.quran

import android.content.Context
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** The large Hindi PDF is delivered only when a Play-installed user chooses to read it. */
internal object HindiQuranAssetPack {
    private const val PACK = "hindi_quran_pdf"
    private const val PDF = "quran_hindi_kanzul_iman.pdf"

    fun file(context: Context): File? = runCatching {
        AssetPackManagerFactory.getInstance(context).getPackLocation(PACK)
            ?.assetsPath()?.let { File(it, PDF) }?.takeIf(File::isFile)
    }.getOrNull()

    suspend fun fetch(context: Context, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        file(context)?.let { return@withContext it }
        val manager = AssetPackManagerFactory.getInstance(context)
        Tasks.await(manager.fetch(listOf(PACK)))
        while (true) {
            currentCoroutineContext().ensureActive()
            file(context)?.let { return@withContext it }
            val state = Tasks.await(manager.getPackStates(listOf(PACK))).packStates()[PACK]
                ?: throw IOException("Hindi Quran download is unavailable from Google Play")
            when (state.status()) {
                AssetPackStatus.PENDING, AssetPackStatus.DOWNLOADING, AssetPackStatus.TRANSFERRING -> {
                    val total = state.totalBytesToDownload()
                    if (total > 0) onProgress((state.bytesDownloaded().toFloat() / total).coerceIn(0f, 1f))
                }
                AssetPackStatus.COMPLETED -> return@withContext file(context)
                    ?: throw IOException("Downloaded Hindi Quran file is missing")
                AssetPackStatus.WAITING_FOR_WIFI -> throw IOException("Connect to Wi-Fi to download the Hindi Quran")
                AssetPackStatus.REQUIRES_USER_CONFIRMATION -> throw IOException("Install the app from Google Play to download the Hindi Quran")
                AssetPackStatus.FAILED, AssetPackStatus.CANCELED, AssetPackStatus.NOT_INSTALLED,
                AssetPackStatus.UNKNOWN -> throw IOException("Hindi Quran download failed (${state.errorCode()})")
            }
            delay(1_000)
        }
        @Suppress("UNREACHABLE_CODE")
        throw IOException("Hindi Quran download stopped")
    }

    fun remove(context: Context, onComplete: () -> Unit) {
        runCatching {
            AssetPackManagerFactory.getInstance(context).removePack(PACK)
                .addOnCompleteListener { onComplete() }
        }.onFailure { onComplete() }
    }
}

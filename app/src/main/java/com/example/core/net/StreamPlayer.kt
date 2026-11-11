package com.example.core.net

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import android.net.Uri
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.mp3.Mp3Extractor
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Builds the players used for streamed audio. They start after a quarter second of buffered audio
 * instead of several seconds, and keep what was streamed on disk so replaying or seeking back does
 * not touch the network again.
 */
@OptIn(UnstableApi::class)
object StreamPlayer {
    private const val CACHE_BYTES = 256L * 1024 * 1024
    @Volatile private var cache: SimpleCache? = null

    @Synchronized
    private fun cache(context: Context): SimpleCache = cache ?: SimpleCache(
        File(context.cacheDir, "stream_cache"),
        LeastRecentlyUsedCacheEvictor(CACHE_BYTES),
        StandaloneDatabaseProvider(context)
    ).also { cache = it }

    /**
     * [exactSeek] builds an exact seek index for MP3s so a seek lands on the requested millisecond.
     * Use it for short clips whose text is highlighted by position; on long streamed files it would
     * make each seek wait for the file to be read up to that point.
     */
    fun build(context: Context, exactSeek: Boolean = false): ExoPlayer {
        val app = context.applicationContext
        val http = http()
        // Only network streams go through the cache; files and assets are read directly.
        val cachedHttp = CacheDataSource.Factory()
            .setCache(cache(app))
            .setUpstreamDataSourceFactory(http)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(30_000, 120_000, 250, 1_000)
            .build()
        val dataSource = DefaultDataSource.Factory(app, cachedHttp)
        val sources = if (exactSeek) {
            DefaultMediaSourceFactory(
                dataSource,
                DefaultExtractorsFactory().setMp3ExtractorFlags(Mp3Extractor.FLAG_ENABLE_INDEX_SEEKING)
            )
        } else {
            DefaultMediaSourceFactory(dataSource)
        }
        return ExoPlayer.Builder(app)
            .setMediaSourceFactory(sources)
            .setLoadControl(loadControl)
            .build()
            .also { if (exactSeek) it.setSeekParameters(SeekParameters.EXACT) }
    }

    /**
     * Streams [url] into the shared cache without playing it, so a later play works offline.
     * Runs on [Dispatchers.IO]. Throws on network failure. Cancelling the calling coroutine
     * cancels the in-flight [CacheWriter], so a left screen does not keep downloading.
     */
    suspend fun prefetch(context: Context, url: String) = withContext(Dispatchers.IO) {
        val source = CacheDataSource.Factory()
            .setCache(cache(context.applicationContext))
            .setUpstreamDataSourceFactory(http())
            .createDataSource()
        val writer = CacheWriter(source, DataSpec(Uri.parse(url)), null, null)
        runCancellingOnJobCancel(writer::cancel) { writer.cache() }
    }

    private fun http() = DefaultHttpDataSource.Factory()
        .setAllowCrossProtocolRedirects(true)
        .setConnectTimeoutMs(10_000)
        .setReadTimeoutMs(20_000)
}

/**
 * Runs blocking [work] while a watcher on [Dispatchers.IO] calls [cancel] if the calling coroutine
 * is cancelled first. The watcher stops once [work] returns or throws.
 */
internal suspend fun <T> runCancellingOnJobCancel(cancel: () -> Unit, work: () -> T): T = coroutineScope {
    val done = AtomicBoolean(false)
    // UNDISPATCHED: the watcher is already waiting before [work] starts. A DEFAULT start could be
    // cancelled before it ever ran, skipping the finally and leaving [work] running.
    val watcher = launch(Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
        try { awaitCancellation() } finally { if (!done.get()) cancel() }
    }
    try { work() } finally { done.set(true); watcher.cancel() }
}

/** A player position or duration as milliseconds, 0 while it is still unknown. */
fun Long.playerMillis(): Int =
    if (this == C.TIME_UNSET || this < 0) 0 else coerceAtMost(Int.MAX_VALUE.toLong()).toInt()


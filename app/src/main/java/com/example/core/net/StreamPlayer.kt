package com.example.core.net

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import java.io.File

/**
 * Builds the players used for streamed audio. They start after a quarter second of buffered audio
 * instead of several seconds, and keep what was streamed on disk so replaying or seeking back does
 * not touch the network again.
 */
@OptIn(UnstableApi::class)
object StreamPlayer {
    private const val CACHE_BYTES = 256L * 1024 * 1024
    private var cache: SimpleCache? = null

    @Synchronized
    private fun cache(context: Context): SimpleCache = cache ?: SimpleCache(
        File(context.cacheDir, "stream_cache"),
        LeastRecentlyUsedCacheEvictor(CACHE_BYTES),
        StandaloneDatabaseProvider(context)
    ).also { cache = it }

    fun build(context: Context): ExoPlayer {
        val app = context.applicationContext
        val http = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(20_000)
        // Only network streams go through the cache; files and assets are read directly.
        val cachedHttp = CacheDataSource.Factory()
            .setCache(cache(app))
            .setUpstreamDataSourceFactory(http)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(30_000, 120_000, 250, 1_000)
            .build()
        return ExoPlayer.Builder(app)
            .setMediaSourceFactory(DefaultMediaSourceFactory(DefaultDataSource.Factory(app, cachedHttp)))
            .setLoadControl(loadControl)
            .build()
    }
}

/** A player position or duration as milliseconds, 0 while it is still unknown. */
fun Long.playerMillis(): Int =
    if (this == C.TIME_UNSET || this < 0) 0 else coerceAtMost(Int.MAX_VALUE.toLong()).toInt()


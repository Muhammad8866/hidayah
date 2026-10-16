package com.example.core.pdf

import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.LruCache

/**
 * The one bounded page-bitmap cache behind every PDF reader. The Quran and book readers used to
 * keep a sixth of the heap each, so having both open held a third of it in rendered pages.
 */
object PdfPageCache {
    private const val HEAP_FRACTION = 6
    private const val LOW_RAM_HEAP_FRACTION = 10
    /** Standard heap, in MB, at or below which a phone gets the smaller budget. */
    private const val LOW_RAM_MEMORY_CLASS = 128

    private var counter = 0L
    private var registered = false
    private val memory = object : LruCache<Triple<Long, Int, Int>, Bitmap>(budget(HEAP_FRACTION)) {
        override fun sizeOf(key: Triple<Long, Int, Int>, value: Bitmap): Int = value.byteCount
    }

    private fun budget(fraction: Int): Int = (Runtime.getRuntime().maxMemory() / fraction).toInt()

    /** Sizes the cache for this phone and hands pages back when the system runs short of memory. */
    @Synchronized fun init(context: Context) {
        if (registered) return
        registered = true
        val app = context.applicationContext
        val manager = app.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (manager != null && (manager.isLowRamDevice || manager.memoryClass <= LOW_RAM_MEMORY_CLASS)) {
            memory.resize(budget(LOW_RAM_HEAP_FRACTION))
        }
        app.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit

            @Deprecated("Deprecated in Java")
            override fun onLowMemory() = memory.evictAll()

            override fun onTrimMemory(level: Int) {
                // The page on screen is held by its reader, so dropping the cache never blanks it.
                when {
                    level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> memory.evictAll()
                    level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> memory.trimToSize(memory.maxSize() / 2)
                }
            }
        })
    }

    /** Unique per opened document, so entries of different documents never collide. */
    @Synchronized fun nextId(): Long = ++counter
    fun get(id: Long, index: Int, width: Int): Bitmap? = memory.get(Triple(id, index, width))
    fun put(id: Long, index: Int, width: Int, bitmap: Bitmap) { memory.put(Triple(id, index, width), bitmap) }
    fun evict(id: Long) = memory.snapshot().keys.filter { it.first == id }.forEach { memory.remove(it) }
}

package com.example.core.share

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlinx.coroutines.runBlocking
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StatusCardShareTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val card = StatusCard.of("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "In the name of Allah, the Most Merciful.", "Quran 1:1")

    @Test
    fun `render produces a 9 by 16 status image`() {
        val bitmap = StatusCardRenderer.render(context, card)
        assertEquals(StatusCardRenderer.WIDTH, bitmap.width)
        assertEquals(StatusCardRenderer.HEIGHT, bitmap.height)
        bitmap.recycle()
    }

    @Test
    fun `render survives very long and empty text`() {
        StatusCardRenderer.render(context, StatusCard.of("ا".repeat(5000), "", "")).recycle()
        StatusCardRenderer.render(context, StatusCard.of("", "word ".repeat(3000), "ref")).recycle()
    }

    @Test
    fun `write saves a png inside the app cache only`() {
        val file = StatusCardShare.write(context, card)
        assertTrue(file.exists())
        assertTrue(file.canonicalPath.startsWith(File(context.cacheDir, "status_cards").canonicalPath))
        assertNotNull(BitmapFactory.decodeFile(file.path))
    }

    @Test
    fun `write reuses one file so cache does not grow`() {
        repeat(5) { StatusCardShare.write(context, card) }
        assertEquals(1, File(context.cacheDir, "status_cards").listFiles()!!.size)
    }

    @Test
    fun `share intent carries image stream with read grant`() {
        val uri = Uri.parse("content://${context.packageName}.fileprovider/status_cards/status_card.png")
        val send = StatusCardShare.sendIntent(uri, "Hidayah app")
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("image/png", send.type)
        assertEquals(uri, send.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
        assertEquals("Hidayah app", send.getStringExtra(Intent.EXTRA_TEXT))
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
    }

    @Test
    fun `file provider exposes the status card file`() {
        val file = StatusCardShare.write(context, card)
        // FileProvider matches roots with a hard-coded '/', so no root can match on a Windows test host.
        org.junit.Assume.assumeTrue(File.separatorChar == '/')
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        assertTrue(uri.toString().contains("status_cards"))
    }

    @Test
    fun `second tap while rendering is ignored and writes nothing`() = runBlocking {
        File(context.cacheDir, "status_cards").deleteRecursively()
        StatusCardShare.inFlight.set(true)
        try {
            assertTrue(StatusCardShare.share(context, card, "x"))
            assertTrue(!File(context.cacheDir, "status_cards/status_card.png").exists())
        } finally {
            StatusCardShare.inFlight.set(false)
        }
    }

    @Test
    fun `scale - 200 sequential renders stay bounded`() {
        val rt = Runtime.getRuntime()
        val start = System.nanoTime()
        repeat(200) { StatusCardShare.write(context, card) }
        val ms = (System.nanoTime() - start) / 1_000_000
        rt.gc()
        val usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024)
        println("status card scale: 200 renders in ${ms}ms, heap after gc ${usedMb}MB, files=${File(context.cacheDir, "status_cards").listFiles()!!.size}")
        assertEquals(1, File(context.cacheDir, "status_cards").listFiles()!!.size)
    }
}

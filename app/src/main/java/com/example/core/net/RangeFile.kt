package com.example.core.net

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/** Seekable HTTP file. Only complete, validated blocks enter the disk cache. */
internal class RangeFile(
    private val url: String,
    private val directory: File,
    private val client: OkHttpClient = FastNet.client
) {
    // A larger block amortizes the redirect and TLS round trip for adjacent PDF reads.
    companion object { const val BLOCK = 1024 * 1024 }
    private var source = url
    private var validator: String? = null
    private var size = 0L
    private lateinit var blocks: File

    fun prepare(): Long {
        client.newCall(request(url, 0, BLOCK - 1L)).execute().use { response ->
            val range = parseRange(response.header("Content-Range"))
            if (response.code != 206 || range == null || range.first != 0L ||
                range.second != minOf(range.third, BLOCK.toLong()) - 1) throw IOException("Byte ranges unavailable")
            size = range.third
            source = response.request.url.toString()
            validator = response.header("ETag")?.takeUnless { it.startsWith("W/") }
                ?: response.header("Last-Modified")
            // Without a validator, never mix bytes from different sessions.
            val identity = "$url/$size/${validator ?: System.nanoTime()}"
            val hash = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray())
                .joinToString("") { "%02x".format(it) }
            blocks = File(directory, hash).apply { mkdirs() }
            val bytes = response.body?.bytes() ?: throw IOException("Empty range")
            if (bytes.size.toLong() != range.second + 1 || !bytes.take(5).toByteArray()
                    .contentEquals("%PDF-".toByteArray())) throw IOException("Invalid PDF range")
            File(blocks, "0").writeBytes(bytes)
            trimCache()
        }
        return size
    }

    @Synchronized fun read(offset: Long, count: Int, destination: ByteArray): Int {
        if (offset < 0 || count < 0 || count > destination.size) throw IOException("Invalid read")
        if (offset >= size) return 0
        val wanted = minOf(count.toLong(), size - offset).toInt()
        var copied = 0
        while (copied < wanted) {
            val position = offset + copied
            val index = position / BLOCK
            val start = index * BLOCK
            val expected = minOf(BLOCK.toLong(), size - start).toInt()
            val file = File(blocks, index.toString())
            val bytes = file.takeIf { it.length() == expected.toLong() }?.readBytes() ?: fetch(start, expected).also {
                blocks.mkdirs()
                val temporary = File(blocks, "$index.tmp")
                temporary.writeBytes(it)
                if (!temporary.renameTo(file)) { temporary.delete(); throw IOException("Could not cache range") }
            }
            val within = (position - start).toInt()
            val length = minOf(wanted - copied, bytes.size - within)
            bytes.copyInto(destination, copied, within, within + length)
            copied += length
        }
        return copied
    }

    private fun trimCache() {
        val root = directory.parentFile ?: return
        val entries = root.listFiles()?.filter { it.isDirectory }?.flatMap { book ->
            book.listFiles()?.filter { it.isDirectory }.orEmpty()
        }.orEmpty()
        val sizes = entries.associateWith { entry -> entry.listFiles()?.sumOf { it.length() } ?: 0L }
        var used = sizes.values.sum()
        for (entry in entries.sortedBy { it.lastModified() }) {
            if (used <= 256L * 1024 * 1024) break
            if (entry == blocks) continue
            if (entry.deleteRecursively()) used -= sizes.getValue(entry)
        }
    }

    private fun fetch(start: Long, count: Int): ByteArray {
        var failure: IOException? = null
        repeat(2) { attempt ->
            try {
                client.newCall(request(if (attempt == 0) source else url, start, start + count - 1)).execute().use { response ->
                    val range = parseRange(response.header("Content-Range"))
                    if (response.code != 206 || range != Triple(start, start + count - 1, size))
                        throw IOException("Source changed or ignored range")
                    val current = response.header("ETag")?.takeUnless { it.startsWith("W/") }
                        ?: response.header("Last-Modified")
                    if (validator != null && current != validator) throw IOException("PDF changed at source")
                    val bytes = response.body?.bytes() ?: throw IOException("Empty range")
                    if (bytes.size != count) throw IOException("Incomplete range")
                    source = response.request.url.toString()
                    return bytes
                }
            } catch (error: IOException) { failure = error }
        }
        throw failure ?: IOException("Range failed")
    }

    private fun request(address: String, start: Long, end: Long) = Request.Builder().url(address)
        .header("Accept-Encoding", "identity").header("Range", "bytes=$start-$end")
        .apply { validator?.let { header("If-Range", it) } }.build()

    private fun parseRange(value: String?): Triple<Long, Long, Long>? {
        val match = Regex("bytes (\\d+)-(\\d+)/(\\d+)").matchEntire(value.orEmpty()) ?: return null
        val (start, end, total) = match.destructured
        return Triple(start.toLong(), end.toLong(), total.toLong()).takeIf { it.first <= it.second && it.second < it.third }
    }
}

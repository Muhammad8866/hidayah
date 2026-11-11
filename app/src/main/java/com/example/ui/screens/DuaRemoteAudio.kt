package com.example.ui.screens

import android.content.Context
import com.example.core.net.FastNet
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.security.MessageDigest

internal data class RemoteDuaRecording(
    val duaId: String,
    val url: String,
    val sha256: String,
    val wordingSource: String,
    val license: String,
    val licenseEvidenceUrl: String
)

/**
 * Licensed dua recordings listed in assets/dua_audio_remote.json. Nothing is bundled: a recording
 * is fetched over HTTPS the first time it is played, kept only if its sha256 matches, and played
 * from the cache after that. Unlisted duas, and listed ones that cannot be fetched, use the device voice.
 */
internal object DuaRemoteAudio {
    private const val MAPPING = "dua_audio_remote.json"
    @Volatile private var entries: Map<String, RemoteDuaRecording>? = null

    /** Entries without an HTTPS url, a 64-hex sha256 and licence evidence are dropped. */
    fun parse(json: String): Map<String, RemoteDuaRecording> {
        val list = JSONObject(json).optJSONArray("recordings") ?: return emptyMap()
        return (0 until list.length()).mapNotNull { index ->
            val item = list.getJSONObject(index)
            RemoteDuaRecording(
                item.optString("duaId"), item.optString("url"), item.optString("sha256").lowercase(),
                item.optString("wordingSource"), item.optString("license"), item.optString("licenseEvidenceUrl")
            ).takeIf {
                it.duaId.isNotBlank() && it.url.startsWith("https://") && it.sha256.matches(Regex("[0-9a-f]{64}")) &&
                    it.license.isNotBlank() && it.licenseEvidenceUrl.startsWith("https://")
            }
        }.associateBy { it.duaId }
    }

    fun load(context: Context) {
        if (entries != null) return
        entries = runCatching {
            context.assets.open(MAPPING).bufferedReader().use { parse(it.readText()) }
        }.getOrDefault(emptyMap())
    }

    fun has(duaId: String) = entries?.containsKey(duaId) == true

    private fun file(context: Context, recording: RemoteDuaRecording) =
        File(File(context.cacheDir, "dua_audio"), "${recording.duaId}-${recording.sha256.take(12)}.mp3")

    /** The cached, verified recording for [duaId], or null when it has not been downloaded yet. */
    fun cached(context: Context, duaId: String): File? =
        entries?.get(duaId)?.let { file(context, it) }?.takeIf { it.isFile && it.length() > 0 }

    /** Downloads and verifies the recording for [duaId]; blocking, so call it off the main thread. */
    fun fetch(context: Context, duaId: String, client: OkHttpClient = FastNet.client): File {
        val recording = entries?.get(duaId) ?: throw IOException("No licensed recording")
        val target = file(context, recording)
        if (target.isFile && target.length() > 0) return target
        target.parentFile?.mkdirs()
        val partial = File(target.path + ".part")
        val digest = MessageDigest.getInstance("SHA-256")
        client.newCall(Request.Builder().url(recording.url).build()).execute().use { response ->
            if (!response.isSuccessful || !response.request.url.isHttps) throw IOException("Download failed")
            val body = response.body ?: throw IOException("Empty download")
            partial.outputStream().use { out ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        digest.update(buffer, 0, read)
                        out.write(buffer, 0, read)
                    }
                }
            }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        if (actual != recording.sha256) { partial.delete(); throw IOException("Recording changed at source") }
        if (!partial.renameTo(target)) { partial.delete(); throw IOException("Could not cache recording") }
        return target
    }
}

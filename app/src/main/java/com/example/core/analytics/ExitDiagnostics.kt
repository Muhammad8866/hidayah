package com.example.core.analytics

/**
 * Pure helpers behind the "why did the last process die" report. A background ANR leaves no
 * stack, logs or keys, so the next launch reads the system's exit record (its subject names the
 * broadcast or service that timed out) plus the wake source this process wrote for itself.
 * Kept free of Android and Firebase so it can be tested on the JVM. Only fixed component
 * names, intent action names and system text go in; never user data.
 */
internal object ExitDiagnostics {
    /** ActivityManager.setProcessStateSummary rejects anything longer. */
    const val SUMMARY_MAX_BYTES = 128
    const val DESCRIPTION_MAX_CHARS = 300
    const val WAKE_LABEL_MAX_CHARS = 80
    private const val TOKEN_MAX_CHARS = 48
    private const val NONE = "-"
    private val nonIdentifier = Regex("[^A-Za-z0-9_]")
    private val whitespace = Regex("\\s+")

    /**
     * One ANR exit. [description] can be only a kill label ("bg anr") for background ANRs, so
     * [subject] carries the trace's own "Subject:" line when the system kept a trace.
     */
    data class ExitRecord(
        val timestamp: Long,
        val description: String?,
        val importance: Int,
        val summary: String?,
        val subject: String? = null
    )

    /** Bytes of the ANR trace read for its subject; the line is near the top. */
    const val TRACE_HEAD_BYTES = 8 * 1024
    private const val SUBJECT_PREFIX = "Subject:"

    /** A fixed identifier: letters, digits and underscores only, `-` when nothing is left. */
    fun token(raw: String?): String {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) return NONE
        return trimmed.replace(nonIdentifier, "_").take(TOKEN_MAX_CHARS)
    }

    /** "component:ACTION", e.g. `alarm_restore:TIME_SET`, from the intent action's last segment. */
    fun wakeLabel(component: String, action: String?): String =
        "${token(component)}:${token(action?.substringAfterLast('.'))}".take(WAKE_LABEL_MAX_CHARS)

    fun processSummary(wake: String?, phase: String?): String = "w=${wake ?: NONE};p=${phase ?: NONE}"

    /** UTF-8 bytes of [text], cut to [maxBytes] without ever splitting a character. */
    fun truncateUtf8(text: String, maxBytes: Int): ByteArray {
        if (maxBytes <= 0 || text.isEmpty()) return ByteArray(0)
        val all = text.toByteArray(Charsets.UTF_8)
        if (all.size <= maxBytes) return all
        val out = java.io.ByteArrayOutputStream(maxBytes)
        var index = 0
        while (index < text.length) {
            val codePoint = text.codePointAt(index)
            val chunk = String(Character.toChars(codePoint)).toByteArray(Charsets.UTF_8)
            if (out.size() + chunk.size > maxBytes) break
            out.write(chunk, 0, chunk.size)
            index += Character.charCount(codePoint)
        }
        return out.toByteArray()
    }

    fun decodeSummary(bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) return null
        return flatten(String(bytes, Charsets.UTF_8), SUMMARY_MAX_BYTES).ifEmpty { null }
    }

    /** ANR exits not yet reported, oldest first. */
    fun newAnrExits(exits: List<ExitRecord>, lastReported: Long): List<ExitRecord> =
        exits.filter { it.timestamp > 0 && it.timestamp > lastReported }.sortedBy { it.timestamp }

    /**
     * The text of the first "Subject:" line of an ANR trace head, e.g. "Broadcast of Intent { act=…
     * cmp=… }" or "executing service …", which names what timed out. Null when there is none.
     */
    fun anrSubject(traceHead: String?): String? {
        if (traceHead.isNullOrEmpty()) return null
        val line = traceHead.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith(SUBJECT_PREFIX) } ?: return null
        return flatten(line.removePrefix(SUBJECT_PREFIX), DESCRIPTION_MAX_CHARS).ifEmpty { null }
    }

    fun formatAnrReport(record: ExitRecord): String {
        val base = "prior_anr time=${record.timestamp} importance=${record.importance} " +
            "wake=${record.summary?.let { flatten(it, SUMMARY_MAX_BYTES) }?.ifEmpty { null } ?: NONE} " +
            "reason=${record.description?.let { flatten(it, DESCRIPTION_MAX_CHARS) }?.ifEmpty { null } ?: NONE}"
        val subject = record.subject?.let { flatten(it, DESCRIPTION_MAX_CHARS) }?.ifEmpty { null }
        return if (subject == null) base else "$base subject=$subject"
    }

    /** One line: control characters and runs of whitespace become single spaces, then capped. */
    private fun flatten(text: String, maxChars: Int): String = text
        .map { if (it.isISOControl()) ' ' else it }
        .joinToString("")
        .replace(whitespace, " ")
        .trim()
        .take(maxChars)
}

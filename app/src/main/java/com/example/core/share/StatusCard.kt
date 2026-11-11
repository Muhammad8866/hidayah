package com.example.core.share

/** Text shown on a WhatsApp-style 9:16 status image. Build with [of] so every field is cleaned and bounded. */
data class StatusCard(val arabic: String, val translation: String, val reference: String, val footer: String) {
    companion object {
        fun of(arabic: String, translation: String, reference: String, footer: String = "") = StatusCard(
            arabic = StatusCardLayout.clamp(arabic, StatusCardLayout.MAX_ARABIC),
            translation = StatusCardLayout.clamp(translation, StatusCardLayout.MAX_TRANSLATION),
            reference = StatusCardLayout.clamp(reference, StatusCardLayout.MAX_LINE),
            footer = StatusCardLayout.clamp(footer, StatusCardLayout.MAX_LINE)
        )
    }
}

/** Pure text-fitting rules, kept free of Android so they are unit-testable. */
object StatusCardLayout {
    const val MAX_ARABIC = 900
    const val MAX_TRANSLATION = 700
    const val MAX_LINE = 120

    /** Trims, drops control characters (keeping newlines) and cuts on a word boundary with an ellipsis. */
    fun clamp(text: String, max: Int): String {
        val clean = text.filter { it == '\n' || !it.isISOControl() }.trim()
        if (clean.length <= max) return clean
        val cut = clean.take(max - 1)
        val space = cut.lastIndexOf(' ')
        return (if (space > 0) cut.take(space) else cut).trimEnd() + "…"
    }

    /** Largest text size from [start] down to [min] (2px steps) whose measured height fits [maxHeight]. */
    fun fitTextSize(start: Float, min: Float, maxHeight: Int, measure: (Float) -> Int): Float {
        var size = start
        while (size > min && measure(size) > maxHeight) size = (size - 2f).coerceAtLeast(min)
        return size
    }
}

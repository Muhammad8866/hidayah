package com.example.quran

import java.text.Normalizer

data class HifzAssessment(val words: List<String>, val incorrectIndices: Set<Int>, val score: Int)

/** Aligns recognized Arabic with the verse so a missed word does not shift every later word. */
object HifzAssessor {
    private fun normalize(word: String): String = Normalizer.normalize(word, Normalizer.Form.NFKD)
        .replace(Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]"), "")
        .replace(Regex("[أإآٱ]"), "ا")
        .replace('ى', 'ي')
        .replace(Regex("[^\\p{L}]"), "")

    fun assess(reference: String, recognized: String): HifzAssessment {
        val words = reference.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val heard = recognized.trim().split(Regex("\\s+")).map(::normalize).filter { it.isNotBlank() }
        val expected = words.map(::normalize)
        val dp = Array(expected.size + 1) { IntArray(heard.size + 1) }
        for (i in 1..expected.size) dp[i][0] = i
        for (j in 1..heard.size) dp[0][j] = j
        for (i in 1..expected.size) for (j in 1..heard.size) {
            dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + if (expected[i - 1] == heard[j - 1]) 0 else 1)
        }
        val wrong = mutableSetOf<Int>()
        var i = expected.size
        var j = heard.size
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && expected[i - 1] == heard[j - 1] && dp[i][j] == dp[i - 1][j - 1]) {
                i--; j--
            } else if (i > 0 && j > 0 && dp[i][j] == dp[i - 1][j - 1] + 1) {
                wrong += i - 1; i--; j--
            } else if (i > 0 && dp[i][j] == dp[i - 1][j] + 1) {
                wrong += i - 1; i--
            } else j--
        }
        val score = if (words.isEmpty()) 0 else ((expected.size - dp[expected.size][heard.size]).coerceAtLeast(0) * 100 / expected.size)
        return HifzAssessment(words, wrong, score)
    }
}

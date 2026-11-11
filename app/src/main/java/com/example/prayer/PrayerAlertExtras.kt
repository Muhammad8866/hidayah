package com.example.prayer

import android.content.Context
import com.example.domain.model.AdhkarDatasetRepository
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.ui.screens.PrayerTrackerStore
import com.example.ui.screens.currentStreak
import com.example.ui.screens.displayMeaning
import com.example.ui.screens.displayTitle
import com.example.ui.screens.quranGoalStore
import java.time.LocalDate

/** The screen a prayer alert's button opens; [key] travels in the launch intent. */
enum class PrayerAlertAction(val key: String, val label: String) {
    ADHKAR("adhkar", "Read Adhkar"),
    QURAN("quran", "Continue Quran");

    companion object {
        fun from(key: String?): PrayerAlertAction? = entries.firstOrNull { it.key == key }
    }
}

sealed interface PrayerAlertExtra {
    data class Dhikr(val evening: Boolean, val index: Int) : PrayerAlertExtra
    data class PagesLeft(val pages: Int) : PrayerAlertExtra
    data class Streak(val days: Int) : PrayerAlertExtra
}

data class PrayerAlertPlan(val extra: PrayerAlertExtra?, val action: PrayerAlertAction?)

/**
 * One extra line and at most one button per prayer alert, so the 5 daily alerts carry a reason to
 * open the app without sending more notifications. Read at delivery time, so progress is current.
 */
object PrayerAlertExtras {

    /** Pure choice of what to show; [dayIndex] rotates the dhikr so each day differs. */
    fun plan(
        prayerName: String,
        dayIndex: Int,
        morningCount: Int,
        eveningCount: Int,
        quranPagesToday: Int,
        quranGoal: Int,
        prayerStreak: Int
    ): PrayerAlertPlan {
        fun dhikr(evening: Boolean): PrayerAlertExtra? {
            val count = if (evening) eveningCount else morningCount
            return if (count <= 0) null else PrayerAlertExtra.Dhikr(evening, Math.floorMod(dayIndex, count))
        }
        return when (prayerName) {
            "Fajr" -> PrayerAlertPlan(dhikr(evening = false), PrayerAlertAction.ADHKAR)
            "Dhuhr" -> PrayerAlertPlan(dhikr(evening = false), null)
            "Asr" -> PrayerAlertPlan(dhikr(evening = true), PrayerAlertAction.ADHKAR)
            "Maghrib" -> {
                val left = quranGoal - quranPagesToday.coerceAtLeast(0)
                PrayerAlertPlan(if (left > 0) PrayerAlertExtra.PagesLeft(left) else null, PrayerAlertAction.QURAN)
            }
            "Isha" -> PrayerAlertPlan(if (prayerStreak > 0) PrayerAlertExtra.Streak(prayerStreak) else null, null)
            else -> PrayerAlertPlan(null, null)
        }
    }

    /** Reads the local stores and builds today's plan; any failure just drops the extra. */
    fun planFor(context: Context, prayerName: String, today: LocalDate = LocalDate.now()): PrayerAlertPlan {
        if (!PrayerSettingsStore.alertExtrasEnabled(PrayerSettingsStore.prefs(context))) return PrayerAlertPlan(null, null)
        return runCatching {
            val goal = quranGoalStore(context)
            val tracker = PrayerTrackerStore(context.getSharedPreferences("daily_tools", Context.MODE_PRIVATE))
            plan(
                prayerName = prayerName,
                dayIndex = today.toEpochDay().toInt(),
                morningCount = AdhkarDatasetRepository.MORNING_ADHKAR.size,
                eveningCount = AdhkarDatasetRepository.EVENING_ADHKAR.size,
                quranPagesToday = if (prayerName == "Maghrib") goal.pagesOn(today) else 0,
                quranGoal = if (prayerName == "Maghrib") goal.targetOn(today, today) else 0,
                prayerStreak = if (prayerName == "Isha") currentStreak(today, tracker::day, limit = 400) else 0
            )
        }.getOrElse { PrayerAlertPlan(null, null) }
    }

    /** The localized line for [extra]; dua text comes from the reviewed dua dataset, not machine copy. */
    fun text(extra: PrayerAlertExtra, language: String): String? = when (extra) {
        is PrayerAlertExtra.Dhikr -> {
            val list = if (extra.evening) AdhkarDatasetRepository.EVENING_ADHKAR else AdhkarDatasetRepository.MORNING_ADHKAR
            list.getOrNull(extra.index)?.let { dua ->
                // Title and meaning only, kept short: some adhkar are whole surahs; the button opens the full text.
                val meaning = dua.displayMeaning(language).text.let { if (it.length > 200) it.take(200).trimEnd() + "…" else it }
                listOf(dua.displayTitle(language), meaning).filter { it.isNotBlank() }.joinToString("\n")
            }
        }
        is PrayerAlertExtra.PagesLeft -> localizeUiTemplate(
            "{pages} pages left for today's Quran goal", language, mapOf("pages" to extra.pages.toString()))
        is PrayerAlertExtra.Streak -> localizeUiTemplate(
            "{days}-day prayer streak. Keep it going tonight", language, mapOf("days" to extra.days.toString()))
    }

    fun actionLabel(action: PrayerAlertAction, language: String): String = localizeUiText(action.label, language)
}

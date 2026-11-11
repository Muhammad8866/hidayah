package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.core.analytics.AppAnalytics
import com.example.domain.model.Ayah
import com.example.quran.FullQuranCatalog
import com.example.quran.HifzQuranTextRepository
import com.example.quran.QuranTranslationRepository
import com.example.ui.components.AppCard
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.tour.tourTarget
import kotlinx.coroutines.CancellationException

@Composable
fun FatehaScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("fateha_screen")
    GuideScreen(FatehaGuide.content, onBack, modifier)
}

@Composable
fun JumuahKhutbahScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("jumuah_khutbah_screen")
    GuideScreen(KhutbahGuide.content, onBack, modifier)
}

/** A read-only step-by-step guide whose Quranic parts come from the verified dataset and can be heard. */
@Composable
private fun GuideScreen(content: FkGuideContent, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val audio = remember(context) { RecitationAudio(context) }
    val playback by audio.state.collectAsState()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(audio, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); audio.close() }
    }
    val surahs = remember(content) { content.passages.map { it.surah }.distinct() }
    var verses by remember { mutableStateOf<Map<Int, List<Ayah>>>(emptyMap()) }
    var translations by remember { mutableStateOf<Map<Pair<Int, Int>, String>>(emptyMap()) }
    var failed by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(retry) {
        failed = false
        try {
            verses = surahs.associateWith {
                HifzQuranTextRepository.loadSurah(context, it).also { rows -> require(rows.isNotEmpty()) }
            }
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { failed = true }
    }
    // Meanings come from the reader's chosen Quran translation; offline without a cached copy they are omitted.
    LaunchedEffect(content) {
        val edition = QuranTranslationRepository.selectedId(context)
        for (surah in surahs) {
            val total = FullQuranCatalog.surahs.firstOrNull { it.number == surah }?.totalAyat ?: continue
            try {
                val rows = QuranTranslationRepository.loadSurah(surah, total, edition)
                translations = translations + rows.associate { (it.surah to it.number) to it.translation }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { }
        }
    }

    LazyColumn(
        modifier.fillMaxSize().testTag("${content.screenTag}_screen"),
        contentPadding = PaddingValues(horizontal = Spacing.screen, vertical = Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        item(key = "header") { ScreenHeader(content.title, subtitle = content.subtitle, onBack = onBack) }
        item(key = "intro") {
            AppCard(Modifier.fillMaxWidth().tourTarget("library_guide_intro")) {
                LocalizedText(content.intro, style = MaterialTheme.typography.bodyMedium)
                if (content.passages.isNotEmpty()) {
                    Spacer(Modifier.height(Spacing.sm))
                    val queueActive = content.passages.any { it.id == playback.id }
                    FilledTonalButton(onClick = AppAnalytics.trackItemAction("${content.screenTag}_screen", "play_all", content.screenTag) {
                        if (queueActive) audio.toggle() else audio.playAll(content.passages)
                    }, modifier = Modifier.tourTarget("library_guide_play_all")) {
                        val pause = queueActive && playback.playing
                        Icon(if (pause) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(Spacing.xs))
                        LocalizedText(if (pause) "Pause" else "Play all", maxLines = 1)
                    }
                }
            }
        }
        if (failed) item(key = "error") {
            AppCard(Modifier.fillMaxWidth().tourTarget("library_guide_retry")) {
                LocalizedText("Quran text could not load. Try again.", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { AppAnalytics.interaction("${content.screenTag}_screen", "retry_quran_text"); retry++ }) { LocalizedText("Retry") }
            }
        }
        content.sections.forEach { section ->
            item(key = "section_${section.heading}") {
                LocalizedText(section.heading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = Spacing.sm).semantics { heading() }.tourTarget("library_guide_section"))
            }
            items(section.steps, key = { it.id }) { step ->
                val number = content.sections.flatMap { it.steps }.indexOf(step) + 1
                GuideStepCard(step, number, language, verses, translations,
                    active = step is FkQuranGuideStep && step.passages.any { it.id == playback.id },
                    playing = playback.playing,
                    onPlay = AppAnalytics.trackItemAction("${content.screenTag}_screen", "play_step", step.id) {
                        val quran = step as? FkQuranGuideStep ?: return@trackItemAction
                        if (quran.passages.any { it.id == playback.id }) audio.toggle() else audio.playAll(quran.passages)
                    })
            }
        }
        if (playback.failed) item(key = "audio_error") {
            LocalizedText("Audio could not play. Try again.", color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.tourTarget("library_guide_audio_error"))
        }
        content.adabHeading?.let { adabTitle ->
            item(key = "adab_heading") {
                LocalizedText(adabTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = Spacing.sm).semantics { heading() })
            }
            items(content.adab, key = { "adab_${it.title}" }) { adab ->
                AppCard(Modifier.fillMaxWidth().tourTarget("library_guide_adab")) {
                    LocalizedText(adab.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(Spacing.xs))
                    LocalizedText(adab.detail, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(Spacing.xs))
                    Text(localizeUiTemplate("Reference: {source}", language, mapOf("source" to adab.reference)),
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item(key = "sources") {
            LocalizedText(content.sources, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = Spacing.md).tourTarget("library_guide_sources"))
        }
    }
}

@Composable
private fun GuideStepCard(
    step: FkGuideStep,
    number: Int,
    language: String,
    verses: Map<Int, List<Ayah>>,
    translations: Map<Pair<Int, Int>, String>,
    active: Boolean,
    playing: Boolean,
    onPlay: () -> Unit
) {
    AppCard(Modifier.fillMaxWidth().testTag("guide_${step.id}").tourTarget("library_guide_step")) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            TonalPill(number.toString())
            LocalizedText(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f))
            if (step.repeat > 1) TonalPill(localizeUiTemplate("{count} times", language, mapOf("count" to step.repeat.toString())))
        }
        step.note?.let {
            Spacer(Modifier.height(Spacing.xs))
            LocalizedText(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(Spacing.sm))
        when (step) {
            is FkQuranGuideStep -> {
                step.passages.forEach { passage ->
                    val rows = verses[passage.surah].orEmpty().filter { it.ayahNumber in passage.first..passage.last }
                    Text(localizeUiText(passage.title, language) + " • " + passage.reference,
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    GuideArabic(rows.joinToString(" ") { it.arabicText + " " + recitationAyahMark(it.ayahNumber) })
                    val meaning = rows.mapNotNull { translations[passage.surah to it.ayahNumber] }.joinToString(" ")
                    if (meaning.isNotBlank()) Text(meaning, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(Spacing.sm))
                }
                FilledTonalButton(onClick = onPlay, modifier = Modifier.tourTarget("library_guide_listen")) {
                    val pause = active && playing
                    Icon(if (pause) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(Spacing.xs))
                    LocalizedText(if (pause) "Pause" else "Listen", maxLines = 1)
                }
            }
            is FkTextGuideStep -> {
                if (step.arabic.isNotBlank()) GuideArabic(step.arabic)
                if (step.transliteration.isNotBlank()) Text(step.transliteration, fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Spacing.xs))
                LocalizedText(step.meaning, style = MaterialTheme.typography.bodyMedium)
            }
            is FkDuaGuideStep -> {
                GuideArabic(step.dua.arabicText)
                Text(step.dua.transliteration, fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Spacing.xs))
                val meaning = step.dua.displayMeaning(language).text
                Text(meaning, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun GuideArabic(text: String) {
    if (text.isBlank()) return
    Text(text, Modifier.fillMaxWidth().padding(vertical = Spacing.xs), fontFamily = ArabicFontFamily,
        style = TextStyle(fontSize = 24.sp, lineHeight = 44.sp, textDirection = TextDirection.Rtl))
}

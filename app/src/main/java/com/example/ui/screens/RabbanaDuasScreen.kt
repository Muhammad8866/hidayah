package com.example.ui.screens

import com.example.ui.tour.tourTarget

import com.example.ui.components.AppSearchField
import com.example.ui.components.ScreenHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.search.AppSearch
import com.example.names.RabbanaDua
import com.example.names.RabbanaDuas
import com.example.ui.components.StateMessage
import com.example.ui.components.AppCard
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiText
import com.example.ui.theme.ArabicFontFamily

/**
 * The forty duas made searchable once for [language] (see [AppSearch]): the transliteration counts
 * most, then the Arabic and the surah reference, then every meaning; a typed number opens that dua.
 */
internal fun rabbanaDuasSearch(language: String): AppSearch.Ranker<RabbanaDua> = AppSearch.Ranker(RabbanaDuas.duas) { dua ->
    AppSearch.Fields(
        title = dua.transliteration,
        nativeTitle = dua.arabic,
        author = dua.reference,
        other = listOf(dua.english, dua.urdu, dua.hindi, dua.hinglish, dua.meaning(language)).distinct(),
        number = dua.number
    )
}

/** The forty Quranic "Rabbana" supplications with transliteration and meaning, fully offline. */
@Composable
fun RabbanaDuasScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("rabbana_duas_screen")

    val audio = rememberDuaSpeech()
    val recorded = rememberRecordedDuaAudio()
    val language = LocalAppLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("rabbana_duas_screen", query)
    val search = remember(language) { rabbanaDuasSearch(language) }
    val shown = remember(search, query) { search.search(query) }

    LazyColumn(
        modifier = modifier.fillMaxSize().imePadding().testTag("rabbana_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(Modifier.tourTarget("kalima_rabbana_header")) { ScreenHeader("40 Rabbana Duas", subtitle = RabbanaDuas.INTRO, onBack = onBack) }
        }
        item {
            AppSearchField(query, { query = it }, "Search dua, meaning or surah", Modifier.testTag("rabbana_search").tourTarget("kalima_rabbana_search"))
        }
        if (shown.isEmpty()) item {
            StateMessage(
                modifier = Modifier.tourTarget("kalima_rabbana_empty"),
                icon = Icons.Default.SearchOff,
                title = "No dua found.",
                actionText = "Clear search",
                onAction = { query = "" }
            )
        }
        items(shown, key = RabbanaDua::number) { dua -> RabbanaDuaCard(dua, dua.meaning(language), audio, recorded) }
        item {
            LocalizedText(RabbanaDuas.SOURCE,
                modifier = Modifier.padding(top = 8.dp).tourTarget("kalima_rabbana_source"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RabbanaDuaCard(dua: RabbanaDua, meaning: String, audio: DuaSpeech, recorded: RecordedDuaAudio) {
    AppCard(
        modifier = Modifier.fillMaxWidth().testTag("rabbana_dua_${dua.number}").tourTarget("kalima_rabbana_card"),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(Spacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // The number sits in the same rounded badge shape used across the app.
            Box(
                modifier = Modifier
                    .size(34.dp).tourTarget("kalima_rabbana_number")
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(dua.number.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.width(10.dp))
            TonalPill(text = dua.reference, icon = Icons.AutoMirrored.Filled.MenuBook, modifier = Modifier.tourTarget("kalima_rabbana_reference"))
        }
        // Arabic on a soft panel, right-aligned in the Nastaleeq face.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp).tourTarget("kalima_rabbana_arabic")
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = dua.arabic,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = ArabicFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                style = TextStyle(fontSize = 25.sp, lineHeight = 48.sp, textDirection = TextDirection.Rtl, textAlign = TextAlign.Start)
            )
        }
        val audioId = "rabbana_${dua.number}"
        if (ManzilRuqyahAudioCatalog.hasDuaRecording(audioId)) RecordedDuaControls(audioId, recorded, Modifier.padding(top = 8.dp).tourTarget("kalima_rabbana_audio")) { audio.stop() }
        else DuaAudioControls(audioId, dua.arabic, audio, Modifier.tourTarget("kalima_rabbana_voice"))
        Text(dua.transliteration,
            modifier = Modifier.padding(top = 8.dp).tourTarget("kalima_rabbana_transliteration"),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(meaning,
            modifier = Modifier.tourTarget("kalima_rabbana_meaning"),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
    }
}


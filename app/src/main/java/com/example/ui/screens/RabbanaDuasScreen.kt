package com.example.ui.screens

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
import com.example.domain.model.DuaDatasetRepository
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

/** The forty Quranic "Rabbana" supplications with transliteration and meaning, fully offline. */
@Composable
fun RabbanaDuasScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("rabbana_duas_screen")

    val audio = rememberDuaSpeech()
    val language = LocalAppLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("rabbana_duas_screen", query)
    val shown = remember(query, language) {
        // Same folding as the Duas tab, so a search ignores harakat and transliteration marks.
        val words = DuaDatasetRepository.normalizeForSearch(query).split(' ').filter(String::isNotBlank)
        RabbanaDuas.duas.filter { dua ->
            val text = DuaDatasetRepository.normalizeForSearch(
                "${dua.number} ${dua.reference} ${dua.transliteration} ${dua.english} ${dua.meaning(language)} ${dua.arabic}"
            )
            words.all(text::contains)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().imePadding().testTag("rabbana_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader("40 Rabbana Duas", subtitle = RabbanaDuas.INTRO, onBack = onBack)
        }
        item {
            AppSearchField(query, { query = it }, "Search dua, meaning or surah", Modifier.testTag("rabbana_search"))
        }
        if (shown.isEmpty()) item {
            StateMessage(
                icon = Icons.Default.SearchOff,
                title = "No dua found.",
                actionText = "Clear search",
                onAction = { query = "" }
            )
        }
        items(shown, key = RabbanaDua::number) { dua -> RabbanaDuaCard(dua, dua.meaning(language), audio) }
        item {
            LocalizedText(RabbanaDuas.SOURCE,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RabbanaDuaCard(dua: RabbanaDua, meaning: String, audio: DuaSpeech) {
    AppCard(
        modifier = Modifier.fillMaxWidth().testTag("rabbana_dua_${dua.number}"),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(Spacing.lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // The number sits in the same rounded badge shape used across the app.
            Box(
                modifier = Modifier
                    .size(34.dp)
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
            TonalPill(text = dua.reference, icon = Icons.AutoMirrored.Filled.MenuBook)
        }
        // Arabic on a soft panel, right-aligned in the Nastaleeq face.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
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
        DuaAudioControls("rabbana_${dua.number}", dua.arabic, audio)
        Text(dua.transliteration,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(meaning,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
    }
}


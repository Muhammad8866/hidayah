package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedMoreSubtitle
import com.example.ui.localization.localizedUiText

private data class MoreFeature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val tag: String,
    val onClick: () -> Unit
)

@Composable
fun MoreScreen(
    onNavigateToRamadan: () -> Unit,
    onNavigateToHadith: () -> Unit,
    onNavigateToFestivals: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToNaat: () -> Unit = {},
    onOpenDailyTool: (DailyTool) -> Unit = {}
) {
    val dailyTools = DailyTool.entries.map { tool ->
        MoreFeature(tool.title, tool.subtitle, tool.icon, tool.accent, "more_${tool.tag}") { onOpenDailyTool(tool) }
    }
    val features = listOf(
        MoreFeature("Qibla Direction", "Direction of the Kaaba", Icons.Default.Explore,
            Color(0xFF2E8B6E), "more_qibla", onNavigateToQibla),
        MoreFeature("Islamic Calendar", "Hijri dates & key days", Icons.Default.CalendarMonth,
            Color(0xFF3F7CAC), "more_calendar", onNavigateToCalendar),
        MoreFeature("Ramzan Timetable", "Sehri & Iftar times", Icons.Default.NightsStay,
            Color(0xFF6B5BAE), "more_ramadan", onNavigateToRamadan),
        MoreFeature("Hadith", "Sayings of the Prophet ﷺ", Icons.Default.AutoStories,
            Color(0xFFB08A2E), "more_hadith", onNavigateToHadith),
        MoreFeature("Naat (without music)", "Voice-only recitations", Icons.Default.Headphones,
            Color(0xFF2A8C8C), "more_naat", onNavigateToNaat),
        MoreFeature("Festivals", "Islamic occasions & aamal", Icons.Default.Celebration,
            Color(0xFFC0664A), "more_festivals", onNavigateToFestivals)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("more_screen"),
        contentPadding = PaddingValues(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)) {
                LocalizedText(
                    text = "More",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                MoreSubtitle(
                    text = "Tools & resources",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item { MoreSectionLabel("Daily tools") }
        featureGrid(dailyTools)
        item { MoreSectionLabel("Explore") }
        featureGrid(features)
        item {
            Spacer(Modifier.height(4.dp))
            MoreSettingsRow(onClick = onOpenSettings)
        }
    }
}

private fun LazyListScope.featureGrid(features: List<MoreFeature>) {
    items(features.chunked(2)) { pair ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            pair.forEach { feature ->
                MoreFeatureTile(
                    feature = feature,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            if (pair.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun MoreSectionLabel(text: String) {
    LocalizedText(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

/** Mid-tone accents are lifted toward white on dark surfaces so icons keep their contrast. */
@Composable
internal fun themedAccent(accent: Color): Color =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) lerp(accent, Color.White, 0.3f) else accent

@Composable
private fun MoreSubtitle(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    // Subtitles are hidden, not shown in English, for languages without a reviewed translation.
    val localized = localizedMoreSubtitle(text, LocalAppLanguage.current) ?: return
    Text(
        text = localized,
        style = style,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun MoreFeatureTile(feature: MoreFeature, modifier: Modifier = Modifier) {
    val accent = themedAccent(feature.accent)
    Card(
        onClick = feature.onClick,
        modifier = modifier
            .heightIn(min = 132.dp)
            .testTag(feature.tag),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(14.dp)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(11.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            LocalizedText(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            MoreSubtitle(
                text = feature.subtitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun MoreSettingsRow(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .testTag("more_settings"),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(11.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    text = "Settings & Privacy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                MoreSubtitle(
                    text = "Language, alerts & prayer times",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = localizedUiText("Open"),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.example.names.BabyGender
import com.example.names.BabyNames
import com.example.ui.components.AppCard
import com.example.ui.components.AppSearchField
import com.example.ui.components.Spacing
import com.example.ui.components.TonalPill
import com.example.ui.components.accentTint
import com.example.ui.localization.localizedUiText
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.ScreenHeader
import com.example.ui.components.StateMessage
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.theme.ArabicFontFamily

@Composable
fun BabyNamesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("baby_names_screen")
    val language = LocalAppLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf<BabyGender?>(null) }
    val shown = remember(query, gender) { BabyNames.search(query, gender) }

    LazyColumn(
        modifier.fillMaxSize().imePadding().testTag("baby_names_screen"),
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item { ScreenHeader("Islamic Baby Names", onBack = onBack) }
        item { AppSearchField(query, { query = it }, "Search name or meaning", Modifier.testTag("baby_names_search")) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(null to "All", BabyGender.BOY to "Boys", BabyGender.GIRL to "Girls").forEach { (value, label) ->
                    FilterChip(selected = gender == value, onClick = { gender = value },
                        label = { LocalizedText(label) }, modifier = Modifier.testTag("baby_names_$label"))
                }
            }
        }
        item {
            Text("${shown.size} / ${BabyNames.names.size}", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (shown.isEmpty()) item {
            StateMessage(icon = Icons.Default.SearchOff, title = "No name found.", actionText = "Reset filters",
                onAction = { query = ""; gender = null })
        }
        items(shown, key = { it.id }) { name ->
            val accent = accentTint(if (name.gender == BabyGender.BOY) BoyAccent else GirlAccent)
            AppCard(
                modifier = Modifier.fillMaxWidth().testTag("baby_name_${name.name}"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(name.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        TonalPill(
                            text = localizedUiText(if (name.gender == BabyGender.BOY) "Boy" else "Girl"),
                            containerColor = accent.copy(alpha = 0.14f),
                            contentColor = accent
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(name.arabic, fontFamily = ArabicFontFamily, color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall.merge(TextStyle(textDirection = TextDirection.Rtl)))
                }
                Text(name.meaning(language), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
        }
        item {
            LocalizedText("Names and meanings: Naam Rakhne ke Ahkam · Maktaba-tul-Madinah (Dawat-e-Islami)",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            LocalizedText("Additional names and meanings: QuranicNames · Arabic name research",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private val BoyAccent = Color(0xFF3B6FA8)
private val GirlAccent = Color(0xFFB0507A)

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.ui.components.HeroSurface
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.GoldAccent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.localization.isRtlAppLanguage
import com.example.ui.localization.localizeUiText
import com.example.viewmodel.AppLanguage

/**
 * Shown once on a fresh install, before anything else, so the whole app opens in the user's own
 * language. The device language is pre-selected; the heading and button follow the highlighted
 * row so the user can read the screen in the language they are about to pick.
 */
@Composable
fun LanguageSelectionScreen(
    detectedLanguageCode: String,
    onLanguageChosen: (String) -> Unit
) {
    var selectedCode by rememberSaveable { mutableStateOf(detectedLanguageCode) }
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsScreen("language_selection_screen")
    com.example.core.analytics.AnalyticsSearch("language_selection_screen", query)
    // The device language leads the list so most users only need to tap Continue.
    val languages = remember(detectedLanguageCode) {
        AppLanguage.supported.sortedByDescending { it.code == detectedLanguageCode }
    }
    val visibleLanguages = remember(languages, query) {
        val needle = query.trim()
        if (needle.isEmpty()) languages
        else languages.filter {
            it.nativeName.contains(needle, ignoreCase = true) || it.code.equals(needle, ignoreCase = true)
        }
    }
    val layoutDirection = if (isRtlAppLanguage(selectedCode)) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Surface(
            modifier = Modifier.fillMaxSize().testTag("language_selection_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    Modifier.fillMaxSize().widthIn(max = 560.dp).padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(12.dp))
                    // A warm welcome: the emerald hero with the greeting the whole app opens with.
                    HeroSurface(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GoldAccent.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Language, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(26.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = "السَّلَامُ عَلَيْكُمْ",
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = ArabicFontFamily,
                                color = GoldAccent
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            text = localizeUiText("Choose app language", selectedCode),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = localizeUiText("The selected language applies across the app.", selectedCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        placeholder = { Text(localizeUiText("Search", selectedCode)) },
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("language_search")
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().selectableGroup(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(visibleLanguages, key = { it.code }) { language ->
                            val isSelected = language.code == selectedCode
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // Keeps the ripple inside the rounded row.
                                    .clip(RoundedCornerShape(16.dp))
                                    .selectable(
                                        selected = isSelected,
                                        role = Role.RadioButton,
                                        onClick = {
                                            com.example.core.analytics.AppAnalytics.interaction("language_selection_screen", "language_option", item = language.code)
                                            selectedCode = language.code
                                        }
                                    )
                                    .testTag("language_${language.code}")
                            ) {
                                Row(
                                    Modifier.heightIn(min = 56.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = null)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            language.nativeName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (language.reviewPending) {
                                            Text(
                                                "Machine-translated — review pending",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            com.example.core.analytics.AppAnalytics.setting("app_language", selectedCode)
                            onLanguageChosen(selectedCode)
                        },
                        shape = CircleShape,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("language_continue")
                    ) {
                        Text(
                            text = localizeUiText("Continue", selectedCode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}


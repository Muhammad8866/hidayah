package com.example.ui.screens
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalizedPhraseText
import com.example.ui.localization.localizedUiText
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.completeContentLanguage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.MicroLesson
import com.example.ui.components.AppCard
import com.example.ui.components.IconBadge
import com.example.ui.components.ProvenanceCard
import com.example.ui.components.Spacing
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LearnAndAssistantScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    com.example.core.analytics.AnalyticsScreen("learn_and_assistant_screen")

    var selectedLessonForSheet by remember { mutableStateOf<MicroLesson?>(null) }
    val focusManager = LocalFocusManager.current
    val appLanguage = LocalAppLanguage.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("learn_assistant_screen")
    ) {
            // Micro Learning
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = Spacing.lg, bottom = Spacing.xxl),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                item {
                    Row(
                        modifier = Modifier.padding(bottom = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(icon = Icons.Default.School, accent = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(Spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            LocalizedText(
                                text = "Authentic Daily Micro-Lessons",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            LocalizedText(
                                text = "Bite-sized, source-cited lessons designed for practical daily application.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Lessons come from the ViewModel; a spinner, not a blank page, while they arrive.
                if (uiState.microLessons.isEmpty()) {
                    item(key = "lessons_loading") { com.example.ui.components.LoadingState() }
                }
                items(uiState.microLessons.size, key = { i -> uiState.microLessons[i].id }) { i ->
                    val lesson = uiState.microLessons[i]
                    val lessonLanguage = completeContentLanguage(appLanguage,
                        listOf(lesson.title, lesson.overview, lesson.contentMarkdown) + lesson.practicalTakeaways)
                    AppCard(
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("learn_and_assistant_screen", "open_lesson", "tap"); selectedLessonForSheet = lesson },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lesson_card_${lesson.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    LocalizedText(
                                        text = lesson.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                LocalizedText(
                                    text = "${lesson.durationMinutes} min read",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            LocalizedPhraseText(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                language = lessonLanguage
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LocalizedPhraseText(
                                text = lesson.overview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                language = lessonLanguage
                            )
                            if (lessonLanguage == "en" && appLanguage != "en") {
                                com.example.ui.localization.LocalizedText("Lesson available in English", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            LocalizedText(
                                text = "Read Lesson & Sources →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
    }

    // Micro Lesson Modal Sheet
    selectedLessonForSheet?.let { lesson ->
        val lessonLanguage = completeContentLanguage(appLanguage,
            listOf(lesson.title, lesson.overview, lesson.contentMarkdown) + lesson.practicalTakeaways)
        ModalBottomSheet(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("learn_and_assistant_screen", "dismiss_lesson", "dismiss"); selectedLessonForSheet = null },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            // The default sheet colour is Material's lavender tint, not the app's palette.
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .testTag("lesson_sheet")
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LocalizedPhraseText(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            language = lessonLanguage
                        )
                        IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("learn_and_assistant_screen", "close_lesson", "tap"); selectedLessonForSheet = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = localizedUiText("Close"))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    if (lessonLanguage == "en" && appLanguage != "en") {
                        com.example.ui.localization.LocalizedText("Lesson available in English", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LocalizedPhraseText(
                        text = lesson.contentMarkdown,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp,
                        language = lessonLanguage
                    )

                    Spacer(modifier = Modifier.height(Spacing.section))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(Spacing.lg)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                LocalizedText(
                                    text = "Key Takeaways:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            lesson.practicalTakeaways.forEach { pt ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    LocalizedText("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    LocalizedPhraseText(pt, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), language = lessonLanguage)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.section))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        LocalizedText(
                            text = "Scholarly Sources & References:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    lesson.scholarlySources.forEach { src ->
                        LocalizedPhraseText(
                            text = src,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            language = lessonLanguage,
                            modifier = Modifier.padding(start = 26.dp, bottom = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

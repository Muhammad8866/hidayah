package com.example.ui.screens
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.LocalizedPhraseText
import com.example.ui.localization.localizedUiText

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
import com.example.ui.components.ProvenanceCard
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LearnAndAssistantScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Micro-Learning, 1: Source-Based Islamic Assistant
    var promptInput by remember { mutableStateOf("") }
    var selectedLessonForSheet by remember { mutableStateOf<MicroLesson?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("learn_assistant_screen")
    ) {
        PrimaryTabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { LocalizedText("Lessons", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { LocalizedText("AI Assistant", maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }

        if (activeTab == 0) {
            // Micro Learning
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
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

                items(uiState.microLessons.size) { i ->
                    val lesson = uiState.microLessons[i]
                    Card(
                        onClick = { selectedLessonForSheet = lesson },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lesson_card_${lesson.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    LocalizedText(
                                        text = lesson.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                LocalizedText(
                                    text = "${lesson.durationMinutes} min read",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            LocalizedPhraseText(
                                text = lesson.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LocalizedPhraseText(
                                text = lesson.overview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
        } else {
            // Source-First Islamic AI Assistant
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Trust Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = localizedUiText("Trust Guarantee"),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        LocalizedText(
                            modifier = Modifier.weight(1f),
                            text = "Retrieval-constrained: Answers are generated strictly with verifiable Quran, Hadith, and scholarly citations. High-stakes legal/divorce questions are deferred to human scholars.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Query Input
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    label = { LocalizedText("Ask about an ayah, hadith, or concept...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    placeholder = { LocalizedText("e.g., What does the Quran say about patience?") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.askAiAssistant(promptInput)
                            },
                            enabled = promptInput.isNotBlank() && !uiState.isAiThinking
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = localizedUiText("Submit"))
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (promptInput.isNotBlank() && !uiState.isAiThinking) {
                            focusManager.clearFocus()
                            viewModel.askAiAssistant(promptInput)
                        }
                    }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_query_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Suggested chips (wrap onto next line on narrow phones)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SuggestionChip(
                        onClick = {
                            promptInput = "Patience and anxiety"
                            viewModel.askAiAssistant(promptInput)
                        },
                        label = { LocalizedText("Patience (Sabr)") }
                    )
                    SuggestionChip(
                        onClick = {
                            promptInput = "Rights of parents"
                            viewModel.askAiAssistant(promptInput)
                        },
                        label = { LocalizedText("Parents") }
                    )
                    SuggestionChip(
                        onClick = {
                            promptInput = "Essentials of Wudu"
                            viewModel.askAiAssistant(promptInput)
                        },
                        label = { LocalizedText("Wudu rules") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.isAiThinking) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    }
                } else if (uiState.aiResponse != null) {
                    val resp = uiState.aiResponse
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        run {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    LocalizedText(
                                        text = "Source-Backed Explanation",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LocalizedPhraseText(
                                        text = resp.summaryAnswer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 22.sp
                                    )

                                    if (resp.scholarlyContext != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        LocalizedText(
                                            text = "Scholarly Context:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        LocalizedPhraseText(
                                            text = resp.scholarlyContext,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (resp.recognizedDifferences != null) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                LocalizedText(
                                                    text = "Recognized Scholarly Differences:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                LocalizedPhraseText(
                                                    text = resp.recognizedDifferences,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }

                                    if (resp.requiresPersonalScholar) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = localizedUiText("Personal Scholar Consultation"),
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                LocalizedText(
                                                    modifier = Modifier.weight(1f),
                                                    text = "High-stakes personal fiqh questions require in-person consultation with a qualified local Islamic scholar.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    LocalizedText(
                                        text = "Primary Sources Retrieved:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    resp.primarySources.forEach { cit ->
                                        ProvenanceCard(
                                            sourceText = "${cit.sourceTitle}: ${cit.referenceText}",
                                            collectionOrReference = cit.collectionOrChapter,
                                            authenticityGrade = cit.authenticityOrType
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Micro Lesson Modal Sheet
    selectedLessonForSheet?.let { lesson ->
        ModalBottomSheet(
            onDismissRequest = { selectedLessonForSheet = null },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedLessonForSheet = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = localizedUiText("Close"))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    LocalizedPhraseText(
                        text = lesson.contentMarkdown,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    LocalizedText(
                        text = "Key Takeaways:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    lesson.practicalTakeaways.forEach { pt ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            LocalizedText("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            LocalizedPhraseText(pt, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    LocalizedText(
                        text = "Scholarly Sources & References:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    lesson.scholarlySources.forEach { src ->
                        LocalizedPhraseText(
                            text = "– $src",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AdhkarDatasetRepository
import com.example.domain.model.Dua
import com.example.domain.model.PrayerType
import com.example.domain.model.TasbeehPreset
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiText
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeroGradientBottom
import com.example.ui.theme.HeroGradientTop
import com.example.viewmodel.MainUiState
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/** Keeps the display awake while shown, so a long recitation is not cut off by the screen timeout. */
@Composable
private fun KeepScreenOn() {
    val window = (LocalView.current.context.findHostActivity())?.window ?: return
    DisposableEffect(window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

/** A number that rolls upward when it grows and downward when it shrinks (undo). */
@Composable
private fun RollingCount(value: Int, content: @Composable (Int) -> Unit) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            val up = targetState > initialState
            (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith
                (slideOutVertically { if (up) -it else it } + fadeOut())
        },
        label = "rolling_count"
    ) { content(it) }
}

@Composable
fun AdhkarTab(uiState: MainUiState, viewModel: MainViewModel) {
    val language = LocalAppLanguage.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    // Evening adhkar are recited from Asr onwards, morning adhkar from Fajr.
    var isMorning by rememberSaveable {
        mutableStateOf(uiState.currentPrayer !in setOf(PrayerType.ASR, PrayerType.MAGHRIB, PrayerType.ISHA))
    }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.refreshAdhkarDay() }
    // A new session starts from its first dhikr; a list already at the top is left as it is.
    LaunchedEffect(isMorning) {
        if (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0) {
            listState.scrollToItem(0)
        }
    }
    KeepScreenOn()

    val session = if (isMorning) "morning" else "evening"
    val adhkar = if (isMorning) AdhkarDatasetRepository.MORNING_ADHKAR else AdhkarDatasetRepository.EVENING_ADHKAR
    fun keyOf(dua: Dua) = "$session:${dua.id}"
    fun countOf(dua: Dua) = uiState.adhkarProgress[keyOf(dua)] ?: 0
    val completed = adhkar.count { countOf(it) >= it.recommendedCount }
    val allDone = completed == adhkar.size
    val anyCounted = adhkar.any { countOf(it) > 0 }
    // The first unfinished dhikr is where the reciter left off.
    val currentIndex = adhkar.indexOfFirst { countOf(it) < it.recommendedCount }
    val currentKey = adhkar.getOrNull(currentIndex)?.let(::keyOf)
    // While any dhikr is unfinished there is no banner above the list, so dhikr i is list item i.
    fun scrollToDhikr(index: Int) {
        scope.launch { listState.animateScrollToItem(index) }
    }

    val currentVisible by remember(currentKey) {
        derivedStateOf { currentKey == null || listState.layoutInfo.visibleItemsInfo.any { it.key == currentKey } }
    }
    val currentIsAbove by remember(currentIndex) {
        derivedStateOf { listState.firstVisibleItemIndex > currentIndex }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // The header stays above the list, so the session switch and progress are always in reach.
        AdhkarHeader(
            isMorning = isMorning,
            onSessionChange = { isMorning = it },
            completed = completed,
            total = adhkar.size,
            canReset = anyCounted,
            showHint = !anyCounted,
            onReset = { confirmReset = true },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("adhkar_list"),
                // Room at the bottom so the floating "Continue" pill never hides the last card.
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (allDone) {
                    item(key = "adhkar_done_$session") { AdhkarDoneBanner() }
                }

                itemsIndexed(adhkar, key = { _, dua -> keyOf(dua) }) { index, dua ->
                    val count = countOf(dua)
                    val target = dua.recommendedCount
                    AdhkarCard(
                        position = index + 1,
                        dua = dua,
                        count = count,
                        isCurrent = index == currentIndex && anyCounted,
                        onCount = {
                            if (count < target) {
                                val finishing = count + 1 == target
                                haptics.performHapticFeedback(
                                    if (finishing) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                                )
                                viewModel.countAdhkar(keyOf(dua), target)
                                // Finishing one dhikr brings the next unfinished one into view.
                                if (finishing) {
                                    val next = (index + 1 until adhkar.size)
                                        .firstOrNull { countOf(adhkar[it]) < adhkar[it].recommendedCount }
                                    if (next != null) scrollToDhikr(next)
                                }
                            }
                        },
                        onReset = { viewModel.resetAdhkar(listOf(keyOf(dua))) },
                        language = language
                    )
                }
            }

            // Leads back to the dhikr in progress after scrolling away or reopening the screen.
            ContinuePill(
                visible = !currentVisible && currentIndex >= 0,
                position = currentIndex + 1,
                pointsUp = currentIsAbove,
                onClick = { if (currentIndex >= 0) scrollToDhikr(currentIndex) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            )
        }
    }

    if (confirmReset) {
        ResetConfirmDialog(
            onConfirm = { viewModel.resetAdhkar(adhkar.map(::keyOf)) },
            onDismiss = { confirmReset = false }
        )
    }
}

/** A floating "Continue · n" button that scrolls back to the dhikr in progress. */
@Composable
private fun ContinuePill(
    visible: Boolean,
    position: Int,
    pointsUp: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shadowElevation = 6.dp,
            modifier = Modifier.testTag("adhkar_continue")
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (pointsUp) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${localizeUiText("Continue", language)} · $position",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AdhkarHeader(
    isMorning: Boolean,
    onSessionChange: (Boolean) -> Unit,
    completed: Int,
    total: Int,
    canReset: Boolean,
    showHint: Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val language = LocalAppLanguage.current
    val progress by animateFloatAsState(
        targetValue = if (total == 0) 0f else completed / total.toFloat(),
        animationSpec = tween(400),
        label = "adhkar_progress"
    )
    Surface(color = colors.background, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)) {
            Surface(
                shape = CircleShape,
                color = colors.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    SessionOption(
                        selected = isMorning,
                        label = "Morning (صباح)",
                        icon = Icons.Default.WbSunny,
                        onClick = { onSessionChange(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adhkar_morning_chip")
                    )
                    SessionOption(
                        selected = !isMorning,
                        label = "Evening (مساء)",
                        icon = Icons.Default.NightsStay,
                        onClick = { onSessionChange(false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("adhkar_evening_chip")
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = colors.primary,
                    trackColor = colors.primary.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$completed / $total",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                IconButton(
                    onClick = onReset,
                    enabled = canReset,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("adhkar_reset_all")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = localizeUiText("Reset", language),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (showHint) {
                LocalizedText(
                    text = "Tap a card to count",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                )
            }
        }
    }
}

/** One half of the Morning / Evening switch; the selected half fills with the brand emerald. */
@Composable
private fun SessionOption(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(if (selected) colors.primary else Color.Transparent, label = "session_bg")
    val content by animateColorAsState(if (selected) colors.onPrimary else colors.onSurfaceVariant, label = "session_fg")
    Surface(
        selected = selected,
        onClick = onClick,
        shape = CircleShape,
        color = container,
        contentColor = content,
        modifier = modifier.fillMaxHeight()
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            LocalizedText(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdhkarDoneBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(HeroGradientTop, HeroGradientBottom)))
            .padding(18.dp)
            .testTag("adhkar_done_banner")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = GoldAccent.copy(alpha = 0.2f),
                contentColor = GoldAccent,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                LocalizedText(
                    text = "All adhkar completed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "الْحَمْدُ لِلَّهِ",
                    style = TextStyle(fontSize = 20.sp, textDirection = TextDirection.Rtl),
                    color = GoldAccent
                )
            }
        }
    }
}

@Composable
private fun AdhkarCard(
    position: Int,
    dua: Dua,
    count: Int,
    isCurrent: Boolean,
    onCount: () -> Unit,
    onReset: () -> Unit,
    language: String
) {
    val target = dua.recommendedCount
    val title = dua.displayTitle(language)
    val done = count >= target
    val colors = MaterialTheme.colorScheme
    // A finished dhikr folds away so the next one is close at hand; tapping it opens it again.
    var reopened by rememberSaveable(dua.id, done) { mutableStateOf(false) }
    val collapsed = done && !reopened

    // The whole card is the counter, so a tap anywhere while reciting counts once.
    Card(
        onClick = { if (collapsed) reopened = true else onCount() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (done) colors.primaryContainer.copy(alpha = 0.35f) else colors.surface
        ),
        border = when {
            done -> BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f))
            isCurrent -> BorderStroke(2.dp, colors.primary)
            else -> BorderStroke(1.dp, colors.outline.copy(alpha = 0.6f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("adhkar_card_${dua.id}")
    ) {
        if (collapsed) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$position. $title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$count / $target",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                IconButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = localizeUiText("Reset", language), modifier = Modifier.size(20.dp))
                }
            }
        } else {
            AdhkarCardBody(
                position = position,
                dua = dua,
                title = title,
                count = count,
                isCurrent = isCurrent,
                onCount = onCount,
                onReset = onReset,
                language = language
            )
        }
    }
}

@Composable
private fun AdhkarCardBody(
    position: Int,
    dua: Dua,
    title: String,
    count: Int,
    isCurrent: Boolean,
    onCount: () -> Unit,
    onReset: () -> Unit,
    language: String
) {
    val target = dua.recommendedCount
    val colors = MaterialTheme.colorScheme
    val meaning = dua.displayMeaning(language)
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (isCurrent) colors.primary else colors.primaryContainer,
                contentColor = if (isCurrent) colors.onPrimary else colors.onPrimaryContainer
            ) {
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(min = 28.dp)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (target > 1) {
                Spacer(modifier = Modifier.width(8.dp))
                RepeatChip(target)
            }
        }
        if (language != "en" && title == dua.title) {
            LocalizedText("English", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(14.dp))
        ArabicText(text = dua.arabicText, fontSize = 24.sp, lineHeight = 46.sp)

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = dua.transliteration,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            color = colors.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = meaning.text, style = MaterialTheme.typography.bodyMedium)
        meaning.fallbackLanguage?.let { label ->
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.primary)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Surface(color = goldContainer(), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.tertiary,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dua.displayBenefit(language),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurface
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            LocalizedText(
                text = dua.displaySource(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = colors.outline.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (count > 0) {
                IconButton(onClick = onReset) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = localizeUiText("Reset", language),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            // Dots for small counts show at a glance how many recitations are left.
            if (target in 2..10) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    repeat(target) { i ->
                        val dot by animateColorAsState(
                            if (i < count) colors.primary else colors.primary.copy(alpha = 0.18f),
                            label = "adhkar_dot"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(dot)
                        )
                    }
                }
            } else if (target > 10) {
                val progress by animateFloatAsState(count / target.toFloat(), label = "adhkar_card_progress")
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = colors.primary,
                    trackColor = colors.primary.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.width(12.dp))
            AdhkarCounterButton(count = count, target = target, onCount = onCount, language = language)
        }
    }
}

/** A round counter with a progress ring, where the thumb rests at the bottom of each card. */
@Composable
private fun AdhkarCounterButton(count: Int, target: Int, onCount: () -> Unit, language: String) {
    val colors = MaterialTheme.colorScheme
    val done = count >= target
    val progress by animateFloatAsState((count / target.toFloat()).coerceIn(0f, 1f), label = "adhkar_ring")
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.9f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "adhkar_press"
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(68.dp)) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = colors.primary,
            strokeWidth = 4.dp,
            trackColor = colors.primary.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp
        )
        Surface(
            onClick = onCount,
            enabled = !done,
            shape = CircleShape,
            color = if (done) colors.primary else colors.primaryContainer,
            contentColor = if (done) colors.onPrimary else colors.onPrimaryContainer,
            interactionSource = interaction,
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    done -> Icon(Icons.Default.Check, contentDescription = localizeUiText("Done", language))
                    target == 1 -> Icon(Icons.Default.TouchApp, contentDescription = localizeUiText("Count", language))
                    else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RollingCount(count) {
                            Text("$it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                        Text(
                            "/$target",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
        title = { LocalizedText("Reset count?") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                modifier = Modifier.testTag("reset_confirm_button")
            ) { LocalizedText("Reset") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { LocalizedText("Cancel") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun TasbeehTab(
    uiState: MainUiState,
    viewModel: MainViewModel
) {
    val language = LocalAppLanguage.current
    val haptics = LocalHapticFeedback.current
    val preset = TasbeehPreset.forPhrase(uiState.tasbeehPhrase)
    val presetIndex = preset?.let { TasbeehPreset.ALL.indexOf(it) } ?: -1
    val count = uiState.tasbeehCount
    val goal = uiState.tasbeehGoal
    val rounds = uiState.tasbeehRounds
    val roundDone = count >= goal
    val colors = MaterialTheme.colorScheme
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    // Tasbeeh-e-Fatimi runs Subhan Allah → Alhamdulillah → Allahu Akbar, so offer the next one.
    val nextPreset = if (presetIndex in 0..1) TasbeehPreset.ALL[presetIndex + 1] else null
    // Everything recited since the last reset: finished rounds plus the round in progress.
    val total = rounds * goal + if (roundDone) 0 else count
    KeepScreenOn()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TasbeehPresetRow(
            selectedPhrase = uiState.tasbeehPhrase,
            onSelect = { viewModel.setTasbeehPreset(it.phrase, it.goal) }
        )

        if (presetIndex in 0..2) {
            Spacer(modifier = Modifier.height(12.dp))
            FatimiSteps(currentIndex = presetIndex, roundDone = roundDone)
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            if (preset != null) {
                // Longer dhikr get a smaller face so they stay within a few lines above the counter.
                val long = preset.arabic.length > 30
                ArabicText(
                    text = preset.arabic,
                    fontSize = if (long) 24.sp else 36.sp,
                    lineHeight = if (long) 44.sp else 60.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = preset.transliteration,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = uiState.tasbeehPhrase,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            TasbeehCounter(
                count = count,
                goal = goal,
                roundDone = roundDone,
                onTap = {
                    val next = if (count >= goal) 1 else count + 1
                    haptics.performHapticFeedback(
                        if (next == goal) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
                    )
                    viewModel.incrementTasbeeh()
                }
            )

            if (count == 0 && rounds == 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LocalizedText(
                    text = "Tap the circle to count",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (roundDone && nextPreset != null) {
                Button(
                    onClick = { viewModel.setTasbeehPreset(nextPreset.phrase, nextPreset.goal) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .testTag("tasbeeh_next_preset")
                ) {
                    Text(
                        text = "${localizeUiText("Next", language)}: ${nextPreset.transliteration} · ${nextPreset.goal}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatPill(
                    icon = Icons.Default.Repeat,
                    text = "Rounds: $rounds",
                    modifier = Modifier.testTag("tasbeeh_rounds")
                )
                StatPill(
                    icon = Icons.Default.Functions,
                    text = "Total: $total",
                    modifier = Modifier.testTag("tasbeeh_total")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.undoTasbeeh()
                    },
                    enabled = count > 0,
                    modifier = Modifier.testTag("tasbeeh_undo")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    LocalizedText("Undo", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { confirmReset = true },
                    enabled = count > 0 || rounds > 0,
                    modifier = Modifier.testTag("tasbeeh_reset")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    LocalizedText("Reset", maxLines = 1)
                }
            }

            if (preset != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = localizeUiText("Source citation", language),
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = preset.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (confirmReset) {
        ResetConfirmDialog(onConfirm = viewModel::resetTasbeeh, onDismiss = { confirmReset = false })
    }
}

/** Swipeable cards for the dhikr presets, each with its Arabic, name and hadith count. */
@Composable
private fun TasbeehPresetRow(selectedPhrase: String, onSelect: (TasbeehPreset) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val rowState = rememberLazyListState()
    val selectedIndex = TasbeehPreset.ALL.indexOfFirst { it.phrase == selectedPhrase }
    // Brings the chosen dhikr into view, e.g. after "Next" moves along Tasbeeh-e-Fatimi,
    // but leaves the row alone when that card is already fully on screen.
    LaunchedEffect(selectedIndex) {
        if (selectedIndex < 0) return@LaunchedEffect
        val layout = rowState.layoutInfo
        val card = layout.visibleItemsInfo.firstOrNull { it.index == selectedIndex }
        val fullyVisible = card != null &&
            card.offset >= layout.viewportStartOffset &&
            card.offset + card.size <= layout.viewportEndOffset
        if (!fullyVisible) rowState.animateScrollToItem(selectedIndex)
    }
    LazyRow(
        state = rowState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tasbeeh_presets")
    ) {
        itemsIndexed(TasbeehPreset.ALL, key = { _, option -> option.phrase }) { _, option ->
            val selected = option.phrase == selectedPhrase
            Card(
                onClick = { if (!selected) onSelect(option) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) colors.primaryContainer else colors.surface
                ),
                border = if (selected) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, colors.outline.copy(alpha = 0.6f)),
                modifier = Modifier
                    .width(150.dp)
                    .testTag("tasbeeh_preset_${option.goal}_${option.transliteration.replace(' ', '_').lowercase()}")
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = option.arabic,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textDirection = TextDirection.Rtl,
                            textAlign = TextAlign.Right
                        ),
                        color = colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = option.transliteration,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "×${option.goal}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                }
            }
        }
    }
}

/** Where the reciter is in Tasbeeh-e-Fatimi: 33 Subhan Allah, 33 Alhamdulillah, 34 Allahu Akbar. */
@Composable
private fun FatimiSteps(currentIndex: Int, roundDone: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Tasbeeh-e-Fatimi",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.testTag("tasbeeh_fatimi_steps")) {
            (0..2).forEach { i ->
                val step = TasbeehPreset.ALL[i]
                val passed = i < currentIndex || (i == currentIndex && roundDone)
                val active = i == currentIndex
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        passed -> colors.primary
                        active -> colors.primaryContainer
                        else -> colors.surfaceVariant
                    },
                    contentColor = when {
                        passed -> colors.onPrimary
                        active -> colors.onPrimaryContainer
                        else -> colors.onSurfaceVariant
                    },
                    border = if (active && !passed) BorderStroke(1.5.dp, colors.primary) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                    ) {
                        if (passed) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${step.transliteration} ${step.goal}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/** The big tap target: a progress ring around a counter that springs under the thumb. */
@Composable
private fun TasbeehCounter(count: Int, goal: Int, roundDone: Boolean, onTap: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val progress by animateFloatAsState(
        targetValue = (count / goal.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(250),
        label = "tasbeeh_ring"
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tasbeeh_press"
    )
    val face = if (roundDone) {
        Brush.linearGradient(listOf(HeroGradientTop, HeroGradientBottom))
    } else {
        Brush.verticalGradient(listOf(colors.primaryContainer.copy(alpha = 0.55f), colors.primaryContainer))
    }
    val numberColor = if (roundDone) Color.White else colors.primary

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .widthIn(max = 320.dp)
            .aspectRatio(1f)
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = if (roundDone) GoldAccent else colors.primary,
            strokeWidth = 12.dp,
            trackColor = colors.primary.copy(alpha = 0.12f),
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp
        )
        Surface(
            onClick = onTap,
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = 6.dp,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .testTag("tasbeeh_counter_btn")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(face)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedVisibility(visible = roundDone, enter = scaleIn() + fadeIn(), exit = fadeOut()) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(32.dp))
                    }
                    RollingCount(count) {
                        Text(
                            text = "$it",
                            fontSize = 72.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = numberColor
                        )
                    }
                    Text(
                        text = "/ $goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (roundDone) Color.White.copy(alpha = 0.8f) else colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        contentColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            LocalizedText(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = modifier
            )
        }
    }
}

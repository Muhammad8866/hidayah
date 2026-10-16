package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import com.example.ui.components.islamicPattern
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeroGradientBottom
import com.example.ui.theme.HeroGradientMiddle
import com.example.ui.theme.HeroGradientTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.AppDestination
import com.example.ui.localization.FeatureTourCopy
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.featureTourCopy

/** One stop of the tour: the real screen shown behind the card while it is explained. */
class FeatureTourStep(
    val destination: AppDestination,
    val icon: ImageVector,
    /** Set for steps that point at tiles inside More rather than at a bottom tab. */
    val moreSection: MoreTourSection? = null
)

/** Order matches the title/body pairs in FeatureTourTranslations. */
val featureTourSteps = listOf(
    FeatureTourStep(AppDestination.HOME, Icons.Filled.WavingHand),
    FeatureTourStep(AppDestination.HOME, Icons.Filled.AccessTime),
    FeatureTourStep(AppDestination.QURAN, Icons.Filled.MenuBook),
    FeatureTourStep(AppDestination.DUAS, Icons.Filled.Favorite),
    FeatureTourStep(AppDestination.COMMUNITY_MASJID, Icons.Filled.Mosque),
    FeatureTourStep(AppDestination.MORE, Icons.Filled.CheckCircle, MoreTourSection.DAILY_FIRST),
    FeatureTourStep(AppDestination.MORE, Icons.Filled.Quiz, MoreTourSection.DAILY_SECOND),
    FeatureTourStep(AppDestination.MORE, Icons.Filled.Explore, MoreTourSection.EXPLORE_FIRST),
    FeatureTourStep(AppDestination.MORE, Icons.Filled.AutoAwesome, MoreTourSection.EXPLORE_SECOND),
    FeatureTourStep(AppDestination.MORE, Icons.Filled.CollectionsBookmark, MoreTourSection.EXPLORE_THIRD),
    FeatureTourStep(AppDestination.MORE, Icons.Filled.Settings, MoreTourSection.SETTINGS)
)

private val CardSideMargin = 16.dp
private val CardMaxWidth = 560.dp
/** Space between the spotlight and the card, filled by the pointer arrow. */
private val ArrowGap = 18.dp

/**
 * First-launch guide drawn over the running app. The host switches the screen behind it to
 * [FeatureTourStep.destination] and passes the root bounds of what is being explained as
 * [spotlight]; this layer dims everything else and swallows touches so nothing underneath is
 * opened by accident mid-tour. The welcome step has no spotlight.
 *
 * A spotlight on a bottom tab gets the card docked just above it with a pointer. When the
 * spotlight is on screen content instead, the host passes the top of the bottom bar as
 * [cardBottomY] and the card docks there, leaving the content above it in view.
 */
@Composable
fun FeatureTourOverlay(
    stepIndex: Int,
    spotlight: Rect?,
    cardBottomY: Float?,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val copy = remember(language) { featureTourCopy(language) }
    val index = stepIndex.coerceIn(0, featureTourSteps.lastIndex)
    com.example.core.analytics.AnalyticsScreen("feature_tour_step_$index")
    val density = LocalDensity.current
    val onContent = cardBottomY != null
    val target = when {
        index == 0 -> null
        // Tiles get a little breathing room; a tab is trimmed to stay inside the bar.
        onContent -> spotlight?.inflate(with(density) { 6.dp.toPx() })
        else -> spotlight?.deflate(with(density) { 4.dp.toPx() })
    }
    val arrowAlpha by animateFloatAsState(if (onContent) 0f else 1f, tween(200), label = "arrowAlpha")

    // The hole glides between tabs, but appears in place the first time rather than flying in.
    val hole = remember { Animatable(Rect.Zero, Rect.VectorConverter) }
    LaunchedEffect(target) {
        if (target != null) {
            if (hole.value == Rect.Zero) hole.snapTo(target) else hole.animateTo(target, tween(350))
        }
    }
    val holeAlpha by animateFloatAsState(if (target != null) 1f else 0f, tween(300), label = "holeAlpha")
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "pulse"
    )
    val cardColor = MaterialTheme.colorScheme.surface
    val ringColor = MaterialTheme.colorScheme.primary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .testTag("feature_tour")
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                // Offscreen so the spotlight punches through the scrim only, not the app below.
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(Color.Black.copy(alpha = 0.66f))
            if (holeAlpha > 0f) {
                val rect = hole.value
                val corner = 20.dp.toPx()
                drawRoundRect(
                    color = Color.Black.copy(alpha = holeAlpha),
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(corner),
                    blendMode = BlendMode.DstOut
                )
                val grow = pulse * 12.dp.toPx()
                drawRoundRect(
                    color = ringColor.copy(alpha = (1f - pulse) * holeAlpha),
                    topLeft = rect.topLeft - Offset(grow, grow),
                    size = Size(rect.width + 2 * grow, rect.height + 2 * grow),
                    cornerRadius = CornerRadius(corner + grow),
                    style = Stroke(3.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.9f * holeAlpha),
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(corner),
                    style = Stroke(2.dp.toPx())
                )
                // Pointer from the card down to the tab, kept clear of the card's rounded corners.
                val cardWidth = minOf(size.width - 2 * CardSideMargin.toPx(), CardMaxWidth.toPx())
                val cardLeft = (size.width - cardWidth) / 2
                val inset = 40.dp.toPx()
                val arrowX = if (cardWidth > 2 * inset) {
                    rect.center.x.coerceIn(cardLeft + inset, cardLeft + cardWidth - inset)
                } else rect.center.x
                val baseY = rect.top - ArrowGap.toPx()
                val half = 11.dp.toPx()
                val arrow = Path().apply {
                    // Starts just inside the card so no seam shows between the two.
                    moveTo(arrowX - half, baseY - 1f)
                    lineTo(arrowX + half, baseY - 1f)
                    lineTo(arrowX, rect.top - 6.dp.toPx())
                    close()
                }
                drawPath(arrow, cardColor.copy(alpha = holeAlpha * arrowAlpha))
            }
        }

        val bottomPadding = when {
            index == 0 -> CardSideMargin
            cardBottomY != null -> with(density) { (constraints.maxHeight - cardBottomY).toDp() } + 12.dp
            target != null -> with(density) { (constraints.maxHeight - target.top).toDp() } + ArrowGap
            // Bounds not measured yet: clear a standard bottom bar.
            else -> 110.dp
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    if (index == 0) WindowInsets.safeDrawing
                    else WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                )
                .padding(start = CardSideMargin, end = CardSideMargin, top = CardSideMargin, bottom = bottomPadding),
            contentAlignment = if (index == 0) Alignment.Center else Alignment.BottomCenter
        ) {
            TourCard(index = index, copy = copy, onNext = onNext, onSkip = onSkip)
        }
    }
}

@Composable
private fun TourCard(index: Int, copy: FeatureTourCopy, onNext: () -> Unit, onSkip: () -> Unit) {
    val isLast = index == featureTourSteps.lastIndex
    Card(
        modifier = Modifier.widthIn(max = CardMaxWidth).fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column {
            AnimatedContent(
                targetState = index,
                // Shrinks and scrolls so the buttons stay reachable at large font sizes and in landscape.
                modifier = Modifier.weight(1f, fill = false),
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(tween(280)) { direction * it / 3 } + fadeIn(tween(280)))
                        .togetherWith(slideOutHorizontally(tween(200)) { -direction * it / 3 } + fadeOut(tween(120)))
                        .using(SizeTransform(clip = false))
                },
                label = "tourStep"
            ) { shown ->
                TourStepContent(shown, copy)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                featureTourSteps.indices.forEach { dot ->
                    val width by animateDpAsState(if (dot == index) 22.dp else 7.dp, label = "dot")
                    Box(
                        Modifier
                            .size(width = width, height = 7.dp)
                            .background(
                                color = when {
                                    dot == index -> MaterialTheme.colorScheme.primary
                                    dot < index -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, top = 10.dp, end = 16.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isLast) {
                    TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("feature_tour", "skip", item = "step_$index"); onSkip() }, modifier = Modifier.testTag("feature_tour_skip")) {
                        Text(copy.skip, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("feature_tour", "next", item = "step_$index"); onNext() },
                    shape = CircleShape,
                    modifier = Modifier.heightIn(min = 50.dp).testTag("feature_tour_next")
                ) {
                    Text(
                        text = if (isLast) copy.done else copy.next,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isLast) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TourStepContent(index: Int, copy: FeatureTourCopy) {
    val step = featureTourSteps[index]
    val (title, body) = copy.steps[index]
    val isWelcome = index == 0
    // The same deep-emerald hero as Home's prayer card, so the tour looks like the app it introduces.
    val onPrimary = Color.White
    val banner = Brush.linearGradient(listOf(HeroGradientTop, HeroGradientMiddle, HeroGradientBottom))

    if (step.moreSection != null) {
        // Compact layout: these steps point at tiles on the screen, so the card must not cover them.
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(46.dp).background(banner, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(step.icon, contentDescription = null, tint = onPrimary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "${index + 1} / ${featureTourSteps.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isWelcome) 148.dp else 104.dp)
                .background(banner)
                .islamicPattern(Color.White.copy(alpha = 0.06f))
        ) {
            // Oversized ghost of the icon gives the banner depth without an image asset.
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = onPrimary.copy(alpha = 0.12f),
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 28.dp, y = 14.dp).size(150.dp)
            )
            Surface(
                shape = CircleShape,
                color = onPrimary.copy(alpha = 0.2f),
                modifier = Modifier.align(Alignment.Center).size(if (isWelcome) 84.dp else 64.dp)
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = onPrimary,
                    modifier = Modifier.padding(if (isWelcome) 20.dp else 16.dp)
                )
            }
            Surface(
                shape = CircleShape,
                color = onPrimary.copy(alpha = 0.2f),
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Text(
                    text = "${index + 1} / ${featureTourSteps.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldAccent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 22.dp, top = 18.dp, end = 22.dp, bottom = 10.dp),
            horizontalAlignment = if (isWelcome) Alignment.CenterHorizontally else Alignment.Start
        ) {
            Text(
                text = title,
                style = if (isWelcome) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = if (isWelcome) TextAlign.Center else TextAlign.Start,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (isWelcome) TextAlign.Center else TextAlign.Start
            )
        }
    }
}

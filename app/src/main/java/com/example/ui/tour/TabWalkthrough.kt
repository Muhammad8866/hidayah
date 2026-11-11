@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.ui.tour

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.AppDestination
import com.example.prayer.PrayerSettingsStore
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizeUiTemplate
import com.example.ui.localization.localizeUiText
import com.example.ui.tour.steps.*
import kotlinx.coroutines.delay

/**
 * Per-tab coach marks. Screens tag the composables worth explaining with [tourTarget]; the
 * [TabWalkthroughHost] in MainActivity spotlights them one by one the first time a tab is opened.
 * Targets that are absent (another flavor, an empty state) or scrolled off screen are skipped.
 */
enum class TabTour(val destination: AppDestination, val title: String) {
    HOME(AppDestination.HOME, "Home"),
    QURAN(AppDestination.QURAN, "Quran"),
    DUAS(AppDestination.DUAS, "Duas"),
    MASJID(AppDestination.COMMUNITY_MASJID, "Masjid"),
    MORE(AppDestination.MORE, "More"),
    RAMADAN(AppDestination.RAMADAN, "Ramzan"),
    HADITH(AppDestination.HADITH, "Hadith"),
    FESTIVALS(AppDestination.FESTIVALS, "Festivals"),
    CALENDAR(AppDestination.CALENDAR, "Calendar"),
    NAAT(AppDestination.NAAT, "Naat"),
    AZAN(AppDestination.AZAN, "Azan"),
    ALLAH_NAMES(AppDestination.ALLAH_NAMES, "99 Names of Allah"),
    BABY_NAMES(AppDestination.BABY_NAMES, "Islamic Baby Names"),
    PROPHET_NAMES(AppDestination.PROPHET_NAMES, "99 Names of the Prophet ﷺ"),
    RECITATION_COLLECTION(AppDestination.RECITATION_COLLECTION, "Manzil / Ruqyah / Wazaif"),
    RABBANA_DUAS(AppDestination.RABBANA_DUAS, "40 Rabbana Duas"),
    KALIMAS(AppDestination.KALIMAS, "6 Kalime & Iman"),
    QAIDAH(AppDestination.QAIDAH, "Madani Qaidah"),
    FATEHA(AppDestination.FATEHA, "Fateha (Isal-e-Sawab)"),
    JUMUAH_KHUTBAH(AppDestination.JUMUAH_KHUTBAH, "Jumu'ah Khutbah"),
    BOOKS(AppDestination.BOOKS, "Islamic Books"),
    DAILY_TOOLS(AppDestination.DAILY_TOOLS, "Daily tools");

    val prefKey: String get() = "tab_tour_seen_${name.lowercase()}"

    companion object {
        fun of(destination: AppDestination): TabTour? = entries.firstOrNull { it.destination == destination }
    }
}

/** Seen-flag key for the guide of sub-screen [id]; see [TourScreen] and [screenTourSteps]. */
internal fun screenTourPrefKey(id: String): String = "tab_tour_seen_screen_$id"

/**
 * Declares that the sub-screen [id] (Prayer tracker, Qibla, Settings, a Quran mode...) is showing.
 * While it is, its guide from [screenTourSteps] takes the place of the tab's guide and runs the
 * first time the screen is opened.
 */
@Composable
fun TourScreen(id: String) {
    DisposableEffect(id) {
        TabWalkthrough.openScreens.add(id)
        onDispose { TabWalkthrough.openScreens.remove(id) }
    }
}

/**
 * One coach mark: the [target] key passed to [tourTarget] and the English copy shown for it.
 * A target that is not on screen (scrolled away, not loaded yet) is still explained with a
 * centred card so nothing is left out; [optional] steps (flavor- or state-only controls) are
 * dropped instead. An empty [target] is a plain explanation card.
 */
class TabTourStep(val target: String, val title: String, val body: String, val optional: Boolean = false)

/** English source copy, one file per screen group in ui/tour/steps; rendered through localizeUiText. */
val tabTourSteps: Map<TabTour, List<TabTourStep>> =
    homeTabTourSteps +
    quranTabTourSteps +
    quranmodesTabTourSteps +
    duasTabTourSteps +
    moreTabTourSteps +
    masjidTabTourSteps +
    ramadanTabTourSteps +
    hadithcalTabTourSteps +
    naatazanTabTourSteps +
    namesTabTourSteps +
    recitationTabTourSteps +
    kalimaTabTourSteps +
    qaidaTabTourSteps +
    libraryTabTourSteps +
    dailytoolsTabTourSteps +
    calcTabTourSteps +
    guidesTabTourSteps +
    learnTabTourSteps

/** Sub-screen guides keyed by the id passed to [TourScreen]. */
val screenTourSteps: Map<String, List<TabTourStep>> =
    homeScreenTourSteps +
    quranScreenTourSteps +
    quranmodesScreenTourSteps +
    duasScreenTourSteps +
    moreScreenTourSteps +
    masjidScreenTourSteps +
    ramadanScreenTourSteps +
    hadithcalScreenTourSteps +
    naatazanScreenTourSteps +
    namesScreenTourSteps +
    recitationScreenTourSteps +
    kalimaScreenTourSteps +
    qaidaScreenTourSteps +
    libraryScreenTourSteps +
    dailytoolsScreenTourSteps +
    calcScreenTourSteps +
    guidesScreenTourSteps +
    learnScreenTourSteps

/** Where each tagged composable currently sits, in root coordinates. */
object TourTargets {
    private class Entry(val owner: Any, val rect: Rect)
    private val entries = mutableStateMapOf<String, Entry>()

    internal fun update(key: String, owner: Any, rect: Rect) {
        val current = entries[key]
        if (rect.isEmpty) {
            if (current?.owner === owner) entries.remove(key)
            return
        }
        // Several rows can share a key (e.g. every download button); the topmost one wins.
        if (current == null || current.owner === owner || rect.top < current.rect.top) {
            if (current?.owner !== owner || current.rect != rect) entries[key] = Entry(owner, rect)
        }
    }

    internal fun remove(key: String, owner: Any) {
        if (entries[key]?.owner === owner) entries.remove(key)
    }

    fun rectOf(key: String): Rect? = entries[key]?.rect

    // Kept apart from [entries]: a target scrolled fully off screen has no rect but can still be scrolled to.
    @OptIn(ExperimentalFoundationApi::class)
    private val requesters = mutableMapOf<String, MutableList<Pair<Any, BringIntoViewRequester>>>()

    @OptIn(ExperimentalFoundationApi::class)
    internal fun register(key: String, owner: Any, requester: BringIntoViewRequester) {
        requesters.getOrPut(key) { mutableListOf() }.add(owner to requester)
    }

    internal fun unregister(key: String, owner: Any) {
        requesters[key]?.let { list -> list.removeAll { it.first === owner }; if (list.isEmpty()) requesters.remove(key) }
    }

    /** Scrolls the target's parent so it is on screen; the spotlight then follows its new rect. */
    @OptIn(ExperimentalFoundationApi::class)
    internal suspend fun bringIntoView(key: String) {
        val list = requesters[key] ?: return
        val shown = entries[key]?.owner
        val requester = (list.firstOrNull { it.first === shown } ?: list.first()).second
        runCatching { requester.bringIntoView() }
    }
}

/** Marks this composable as a coach-mark target for the per-tab walkthrough. */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.tourTarget(key: String): Modifier = composed {
    val owner = remember { Any() }
    val requester = remember { BringIntoViewRequester() }
    DisposableEffect(key) {
        TourTargets.register(key, owner, requester)
        onDispose { TourTargets.remove(key, owner); TourTargets.unregister(key, owner) }
    }
    bringIntoViewRequester(requester).onGloballyPositioned { coordinates ->
        TourTargets.update(key, owner, if (coordinates.isAttached) coordinates.boundsInRoot() else Rect.Zero)
    }
}

/** Seen flags and replay requests. */
object TabWalkthrough {
    /** Sub-screen ids currently composed, newest last; see [TourScreen]. */
    internal val openScreens = androidx.compose.runtime.mutableStateListOf<String>()

    /** A tour asked for from More > App tour; runs even when already seen. */
    var requested by mutableStateOf<TabTour?>(null)
        internal set

    fun request(tour: TabTour) { requested = tour }

    internal fun isSeen(context: Context, prefKey: String): Boolean =
        PrayerSettingsStore.prefs(context).getBoolean(prefKey, false)

    internal fun markSeen(context: Context, prefKey: String) {
        PrayerSettingsStore.prefs(context).edit().putBoolean(prefKey, true).apply()
    }
}

/** Steps to show now: targets on screen are spotlighted, missing ones become cards, optional ones drop. */
internal fun visibleSteps(all: List<TabTourStep>, isOnScreen: (String) -> Boolean): List<TabTourStep> =
    all.filter { it.target.isEmpty() || !it.optional || isOnScreen(it.target) }

private fun Rect.usableIn(width: Float, height: Float): Rect? {
    val clipped = intersect(Rect(0f, 0f, width, height))
    return clipped.takeIf { it.width > 8f && it.height > 8f }
}

/**
 * Runs the walkthrough for [currentDestination]. [enabled] is false while something else covers
 * the tab (the first-launch tour, a dialog-like secondary screen), so coach marks never point at
 * hidden content.
 */
@Composable
fun TabWalkthroughHost(currentDestination: AppDestination, enabled: Boolean, overlayOpen: Boolean = false) {
    val context = LocalContext.current
    val screenId = TabWalkthrough.openScreens.lastOrNull { screenTourSteps.containsKey(it) }
    // Qibla / Settings cover the tab; without a guide of their own nothing is shown over them.
    val tour = if (screenId == null && !overlayOpen) TabTour.of(currentDestination) else null
    val prefKey: String? = screenId?.let(::screenTourPrefKey) ?: tour?.prefKey
    var active by remember { mutableStateOf<String?>(null) }
    var steps by remember { mutableStateOf<List<TabTourStep>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val requested = TabWalkthrough.requested

        LaunchedEffect(prefKey, enabled, requested) {
            if (active != null && active != prefKey) active = null
            if (prefKey == null || !enabled || active != null) return@LaunchedEffect
            val replay = tour != null && requested == tour
            if (!replay && TabWalkthrough.isSeen(context, prefKey)) return@LaunchedEffect
            // Let the screen finish its entry animation and first layout before measuring.
            delay(700)
            val all = if (screenId != null) screenTourSteps[screenId].orEmpty() else tabTourSteps[tour].orEmpty()
            val available = visibleSteps(all) { TourTargets.rectOf(it)?.usableIn(width, height) != null }
            if (replay) TabWalkthrough.requested = null
            TabWalkthrough.markSeen(context, prefKey)
            if (available.isEmpty()) return@LaunchedEffect
            com.example.core.analytics.AppAnalytics.interaction("tab_walkthrough", "start", item = screenId ?: tour!!.name.lowercase())
            steps = available
            index = 0
            active = prefKey
        }

        val running = active
        if (running != null && running == prefKey && enabled && steps.isNotEmpty()) {
            val finish = { active = null }
            TabWalkthroughOverlay(
                steps = steps,
                index = index.coerceIn(0, steps.lastIndex),
                rootWidth = width,
                rootHeight = height,
                onBack = { if (index > 0) { com.example.core.analytics.AppAnalytics.interaction("tab_walkthrough", "previous_step"); index-- } },
                onNext = {
                    if (index < steps.lastIndex) { com.example.core.analytics.AppAnalytics.interaction("tab_walkthrough", "next_step"); index++ }
                    else { com.example.core.analytics.AppAnalytics.interaction("tab_walkthrough", "complete", item = screenId ?: tour?.name?.lowercase()); finish() }
                },
                onSkip = {
                    com.example.core.analytics.AppAnalytics.interaction("tab_walkthrough", "skip", item = "${screenId ?: tour?.name?.lowercase()}_$index")
                    finish()
                }
            )
            BackHandler { if (index > 0) index-- else finish() }
        }
    }
}

private val CardMargin = 16.dp
private val CardMaxWidth = 520.dp
private val Gap = 18.dp

@Composable
private fun TabWalkthroughOverlay(
    steps: List<TabTourStep>,
    index: Int,
    rootWidth: Float,
    rootHeight: Float,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val language = LocalAppLanguage.current
    val density = LocalDensity.current
    val step = steps[index]
    // Re-read each frame the map changes, so a target that scrolls or disappears is followed.
    val target = step.target.takeIf { it.isNotEmpty() }?.let { TourTargets.rectOf(it) }?.usableIn(rootWidth, rootHeight)
        ?.inflate(with(density) { 6.dp.toPx() })

    // Scroll the screen to the step's target (e.g. a Home card below the fold) before spotlighting it.
    LaunchedEffect(step.target) {
        if (step.target.isNotEmpty()) TourTargets.bringIntoView(step.target)
    }
    val hole = remember { Animatable(Rect.Zero, Rect.VectorConverter) }
    LaunchedEffect(target) {
        if (target != null) {
            if (hole.value == Rect.Zero) hole.snapTo(target) else hole.animateTo(target, tween(320))
        }
    }
    val pulse by rememberInfiniteTransition(label = "tabTourPulse").animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart), label = "tabTourPulse"
    )
    val ringColor = MaterialTheme.colorScheme.primary
    val cardColor = MaterialTheme.colorScheme.surface
    // The card sits on the roomier side of the spotlight.
    val cardBelow = target != null && target.center.y < rootHeight / 2

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {}
            .testTag("tab_walkthrough")
    ) {
        Canvas(
            Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(Color.Black.copy(alpha = 0.66f))
            if (target != null) {
                val rect = hole.value
                val corner = 18.dp.toPx()
                drawRoundRect(Color.Black, rect.topLeft, rect.size, CornerRadius(corner), blendMode = BlendMode.DstOut)
                val grow = pulse * 12.dp.toPx()
                drawRoundRect(
                    color = ringColor.copy(alpha = 1f - pulse),
                    topLeft = rect.topLeft - Offset(grow, grow),
                    size = Size(rect.width + 2 * grow, rect.height + 2 * grow),
                    cornerRadius = CornerRadius(corner + grow),
                    style = Stroke(3.dp.toPx())
                )
                drawRoundRect(Color.White.copy(alpha = 0.9f), rect.topLeft, rect.size, CornerRadius(corner), style = Stroke(2.dp.toPx()))
                // Pointer from the card to the spotlight.
                val half = 11.dp.toPx()
                val inset = 40.dp.toPx()
                val arrowX = rect.center.x.coerceIn(inset, (size.width - inset).coerceAtLeast(inset))
                val arrow = Path().apply {
                    if (cardBelow) {
                        val baseY = rect.bottom + Gap.toPx()
                        moveTo(arrowX - half, baseY + 1f); lineTo(arrowX + half, baseY + 1f)
                        lineTo(arrowX, rect.bottom + 6.dp.toPx())
                    } else {
                        val baseY = rect.top - Gap.toPx()
                        moveTo(arrowX - half, baseY - 1f); lineTo(arrowX + half, baseY - 1f)
                        lineTo(arrowX, rect.top - 6.dp.toPx())
                    }
                    close()
                }
                drawPath(arrow, cardColor)
            }
        }

        val (topPad, bottomPad, alignment) = with(density) {
            when {
                target == null -> Triple(CardMargin, CardMargin, Alignment.Center)
                cardBelow -> Triple(target.bottom.toDp() + Gap, CardMargin, Alignment.TopCenter)
                else -> Triple(CardMargin, (rootHeight - target.top).toDp() + Gap, Alignment.BottomCenter)
            }
        }
        Box(
            Modifier.fillMaxSize().padding(start = CardMargin, end = CardMargin, top = topPad, bottom = bottomPad),
            contentAlignment = alignment
        ) {
            val title = localizeUiText(step.title, language)
            val body = localizeUiText(step.body, language)
            val counter = localizeUiTemplate(
                "Step {current} of {total}", language,
                mapOf("current" to "${index + 1}", "total" to "${steps.size}")
            )
            Card(
                modifier = Modifier.widthIn(max = CardMaxWidth).fillMaxWidth()
                    .semantics { paneTitle = title },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(Modifier.padding(start = 20.dp, top = 16.dp, end = 12.dp, bottom = 10.dp)) {
                    // One merged, polite live region so TalkBack reads each new step aloud.
                    Column(
                        Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(end = 8.dp)
                            .semantics(mergeDescendants = true) {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = "$counter. $title. $body"
                            }
                    ) {
                        Text(counter, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp).semantics { heading() }
                        )
                        Text(
                            body, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onSkip, modifier = Modifier.testTag("tab_walkthrough_skip")) {
                            LocalizedText("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.weight(1f))
                        if (index > 0) {
                            TextButton(onClick = onBack, modifier = Modifier.testTag("tab_walkthrough_back")) {
                                LocalizedText("Back")
                            }
                        }
                        Button(
                            onClick = onNext, shape = CircleShape,
                            modifier = Modifier.heightIn(min = 48.dp).testTag("tab_walkthrough_next")
                        ) {
                            LocalizedText(if (index == steps.lastIndex) "Done" else "Next", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * More > App tour: pick the full first-launch tour or the guide for one tab. Choosing a tab
 * opens it and replays its walkthrough there.
 */
@Composable
fun TourPickerDialog(
    onFullTour: () -> Unit,
    onTabTour: (TabTour) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("tour_picker"),
        title = { LocalizedText("App tour") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LocalizedText(
                    "Choose a guide. Each tab guide points out the main buttons on that screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                PickerRow("Full app tour", "tour_picker_full", onFullTour)
                TabTour.entries.forEach { tour ->
                    PickerRow(tour.title, "tour_picker_${tour.name.lowercase()}") { onTabTour(tour) }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { LocalizedText("Cancel") } }
    )
}

@Composable
private fun PickerRow(label: String, tag: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp)
            .testTag(tag),
        contentAlignment = Alignment.CenterStart
    ) {
        LocalizedText(label, style = MaterialTheme.typography.bodyLarge)
    }
}

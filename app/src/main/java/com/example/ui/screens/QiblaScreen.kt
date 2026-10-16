package com.example.ui.screens

import androidx.compose.material.icons.filled.LocationOff
import com.example.ui.components.StateMessage
import com.example.ui.components.ScreenHeader
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import com.example.ui.components.AppCard
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.prayer.QiblaGuidance
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import com.example.ui.theme.GoldAccent
import com.example.viewmodel.MainUiState
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun QiblaScreen(
    uiState: MainUiState,
    onBack: (() -> Unit)? = null,
    onUseCurrentLocation: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    com.example.core.analytics.AnalyticsScreen("qibla_screen")

    if (!uiState.hasConfirmedLocation) {
        Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            ScreenHeader("Qibla Direction", onBack = onBack, backTestTag = "qibla_back_btn")
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                StateMessage(
                    icon = Icons.Default.LocationOff,
                    title = "Choose a city on Home or use device location for Qibla direction.",
                    actionText = "Use device location".takeIf { onUseCurrentLocation != null },
                    onAction = onUseCurrentLocation
                )
            }
        }
        return
    }
    val bearing = uiState.qiblaBearing
    val sensorUnreliable = uiState.compassAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
    val sensorLow = uiState.compassAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW
    val trueHeading = if (!sensorUnreliable) {
        uiState.deviceHeading?.let { heading ->
            uiState.magneticDeclination?.let { QiblaGuidance.trueHeading(heading.toDouble(), it.toDouble()) }
        }
    } else null
    val turn = trueHeading?.let { QiblaGuidance.turnDegrees(bearing, it) }
    val aligned = !sensorLow && turn?.let(QiblaGuidance::isAligned) == true
    val direction = when {
        aligned -> "Facing the Kaaba"
        sensorUnreliable || sensorLow -> "Calibrate compass for accuracy"
        turn != null && turn > 0 -> "Turn right ${abs(turn).roundToInt()}°"
        turn != null -> "Turn left ${abs(turn).roundToInt()}°"
        uiState.deviceHeading == null -> "Bearing from true north"
        else -> "Waiting for compass"
    }

    // A short buzz the moment the phone lines up with the Kaaba, so eyes can stay on the dial.
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(aligned) { if (aligned) haptics.performHapticFeedback(HapticFeedbackType.LongPress) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(com.example.core.analytics.rememberAnalyticsScrollState("qibla_screen"))
            .padding(horizontal = 20.dp, vertical = 16.dp).testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenHeader(
            title = "Qibla Direction",
            subtitle = if (uiState.isLiveLocation) "Current location · ${uiState.selectedCity.name}"
            else "City location · ${uiState.selectedCity.name}",
            onBack = onBack,
            backTestTag = "qibla_back_btn"
        )

        Spacer(Modifier.height(20.dp))
        // The guidance sits in a status pill whose colour says at a glance: aligned, needs calibration, or turning.
        val needsCalibration = !aligned && (sensorUnreliable || sensorLow)
        val pillColor by animateColorAsState(
            when {
                aligned -> MaterialTheme.colorScheme.primaryContainer
                needsCalibration -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            label = "qiblaPill"
        )
        val pillContent = when {
            aligned -> MaterialTheme.colorScheme.onPrimaryContainer
            needsCalibration -> MaterialTheme.colorScheme.onTertiaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        }
        Surface(shape = CircleShape, color = pillColor) {
            Row(
                Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        aligned -> Icons.Default.CheckCircle
                        needsCalibration -> Icons.Default.CompassCalibration
                        else -> Icons.Default.Explore
                    },
                    contentDescription = null,
                    tint = if (aligned) MaterialTheme.colorScheme.primary else pillContent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                LocalizedText(
                    text = direction,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = pillContent,
                    textAlign = TextAlign.Center,
                    // Long translations wrap inside the pill instead of pushing the icon out.
                    modifier = Modifier.weight(1f, fill = false).testTag("qibla_guidance")
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LocalizedText(
            text = if (turn != null) "Point the top of your phone toward the Kaaba"
            else "${bearing.roundToInt()}° clockwise from true north",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))
        QiblaDial(
            pointerDegrees = rememberSmoothAngle((turn ?: bearing).toFloat()),
            northDegrees = rememberSmoothAngle(-(trueHeading ?: 0.0).toFloat()),
            live = turn != null,
            aligned = aligned,
            description = localizedUiText(direction)
        )
        Spacer(Modifier.height(22.dp))

        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Metric("Qibla bearing", "${bearing.roundToInt()}°", "true north", Modifier.weight(1f).padding(horizontal = 4.dp).testTag("qibla_bearing"))
                VerticalDivider(modifier = Modifier.height(42.dp))
                Metric("Kaaba distance", "${uiState.distanceToMakkahKm.roundToLong()} km", "great circle", Modifier.weight(1f).padding(horizontal = 4.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        when {
            uiState.deviceHeading == null -> InfoCard(
                icon = { Icon(Icons.Default.CompassCalibration, contentDescription = null) },
                title = "Compass unavailable",
                body = "Use the bearing above with a reliable compass. Hold the phone flat for live guidance."
            )
            sensorUnreliable || uiState.compassAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW -> InfoCard(
                icon = { Icon(Icons.Default.CompassCalibration, contentDescription = null) },
                title = "Calibrate the compass",
                body = "Move your phone in a figure eight, away from magnets and metal, then check the direction again."
            )
            else -> LocalizedText(
                "Hold your phone flat and away from metal or magnets for the best reading.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (!uiState.isLiveLocation && onUseCurrentLocation != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("qibla_screen", "use_current_location", onUseCurrentLocation), modifier = Modifier.heightIn(min = 48.dp)) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText("Use current location")
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Metric(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        LocalizedText(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        LocalizedText(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, maxLines = 1)
        LocalizedText(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun InfoCard(icon: @Composable () -> Unit, title: String, body: String) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
        outlined = false,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onTertiaryContainer) {
                Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                LocalizedText(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                LocalizedText(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Follows a compass angle without jitter and without spinning the long way round when the
 * reading crosses north (359° -> 1°). The returned value is unwrapped, which is fine for rotate().
 */
@Composable
private fun rememberSmoothAngle(target: Float): Float {
    val unwrapped = remember { mutableFloatStateOf(target) }
    LaunchedEffect(target) {
        // Shortest signed turn from where the dial is pointing to the new reading.
        val diff = target - unwrapped.floatValue
        unwrapped.floatValue += ((diff % 360f) + 540f) % 360f - 180f
    }
    val animated by animateFloatAsState(
        targetValue = unwrapped.floatValue,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "compassAngle"
    )
    return animated
}

@Composable
private fun QiblaDial(pointerDegrees: Float, northDegrees: Float, live: Boolean, aligned: Boolean, description: String) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val face = MaterialTheme.colorScheme.surface
    val faceGlow = MaterialTheme.colorScheme.primaryContainer
    // The outer ring warms to gold while the phone faces the Kaaba.
    val ringColor by animateColorAsState(if (aligned) GoldAccent else outline, label = "qiblaRing")
    Box(
        modifier = Modifier.fillMaxWidth(0.86f).widthIn(max = 330.dp).aspectRatio(1f)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .testTag("compass_dial"),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.fillMaxSize().rotate(northDegrees)) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 10.dp.toPx()
                drawCircle(
                    Brush.radialGradient(listOf(face, faceGlow.copy(alpha = 0.55f)), center = center, radius = radius),
                    radius, center
                )
                drawCircle(ringColor, radius, center, style = Stroke(if (aligned) 4.dp.toPx() else 2.dp.toPx()))
                drawCircle(outline.copy(alpha = 0.25f), radius * 0.75f, center, style = Stroke(1.dp.toPx()))
                repeat(36) { index ->
                    val angle = Math.toRadians(index * 10.0)
                    val inner = radius - (if (index % 9 == 0) 18.dp.toPx() else 9.dp.toPx())
                    val start = Offset(center.x + inner * kotlin.math.sin(angle).toFloat(), center.y - inner * kotlin.math.cos(angle).toFloat())
                    val end = Offset(center.x + radius * kotlin.math.sin(angle).toFloat(), center.y - radius * kotlin.math.cos(angle).toFloat())
                    drawLine(if (index % 9 == 0) primary else outline, start, end, if (index % 9 == 0) 2.dp.toPx() else 1.dp.toPx())
                }
            }
            // West and east are physical sides of the dial, so they must not swap in RTL languages.
            Box(Modifier.fillMaxSize().padding(29.dp), contentAlignment = Alignment.TopCenter) { LocalizedText("N", fontWeight = FontWeight.Bold, color = primary) }
            Box(Modifier.fillMaxSize().padding(29.dp), contentAlignment = Alignment.BottomCenter) { LocalizedText("S", fontWeight = FontWeight.Bold, color = outline) }
            Box(Modifier.fillMaxSize().padding(29.dp), contentAlignment = AbsoluteAlignment.CenterLeft) { LocalizedText("W", fontWeight = FontWeight.Bold, color = outline) }
            Box(Modifier.fillMaxSize().padding(29.dp), contentAlignment = AbsoluteAlignment.CenterRight) { LocalizedText("E", fontWeight = FontWeight.Bold, color = outline) }
        }

        Canvas(Modifier.fillMaxSize().rotate(pointerDegrees)) {
            val center = Offset(size.width / 2, size.height / 2)
            val tip = Offset(center.x, 30.dp.toPx())
            val baseY = center.y - 22.dp.toPx()
            val needle = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(center.x - 13.dp.toPx(), baseY)
                lineTo(center.x + 13.dp.toPx(), baseY)
                close()
            }
            drawPath(needle, if (aligned) GoldAccent else primary)
            drawCircle(primary, 7.dp.toPx(), center)
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp,
            modifier = Modifier.size(74.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Navigation, contentDescription = localizedUiText("Kaaba Direction"), tint = primary,
                    modifier = Modifier.size(20.dp))
                LocalizedText(if (live) "QIBLA" else "NORTH UP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

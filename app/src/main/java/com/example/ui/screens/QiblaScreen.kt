package com.example.ui.screens

import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    if (!uiState.hasConfirmedLocation) {
        Column(modifier = modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            LocalizedText("Choose a city on Home or use device location for Qibla direction.",
                style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            onUseCurrentLocation?.let { callback ->
                Spacer(Modifier.height(16.dp))
                Button(onClick = callback) { LocalizedText("Use device location") }
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

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp).testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("qibla_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizedUiText("Back"))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                LocalizedText("Qibla Direction", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                LocalizedText(
                    text = if (uiState.isLiveLocation) "Current location · ${uiState.selectedCity.name}"
                    else "City location · ${uiState.selectedCity.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        LocalizedText(
            text = direction,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (aligned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("qibla_guidance")
        )
        LocalizedText(
            text = if (turn != null) "Point the top of your phone toward the Kaaba"
            else "${bearing.roundToInt()}° clockwise from true north",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))
        QiblaDial((turn ?: bearing).toFloat(), -(trueHeading ?: 0.0).toFloat(), turn != null, aligned)
        Spacer(Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Metric("Qibla bearing", "${bearing.roundToInt()}°", "true north", Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.height(42.dp))
                Metric("Kaaba distance", "${uiState.distanceToMakkahKm.roundToLong()} km", "great circle", Modifier.weight(1f))
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
            TextButton(onClick = onUseCurrentLocation) {
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
        LocalizedText(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LocalizedText(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        LocalizedText(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoCard(icon: @Composable () -> Unit, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) { icon() }
            Spacer(Modifier.width(12.dp))
            Column {
                LocalizedText(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                LocalizedText(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun QiblaDial(pointerDegrees: Float, northDegrees: Float, live: Boolean, aligned: Boolean) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    Box(
        modifier = Modifier.fillMaxWidth(0.86f).widthIn(max = 330.dp).aspectRatio(1f).testTag("compass_dial"),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.fillMaxSize().rotate(northDegrees)) {
            Canvas(Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 - 10.dp.toPx()
                drawCircle(outline, radius, center, style = Stroke(2.dp.toPx()))
                drawCircle(outline.copy(alpha = 0.25f), radius * 0.75f, center, style = Stroke(1.dp.toPx()))
                repeat(36) { index ->
                    val angle = Math.toRadians(index * 10.0)
                    val inner = radius - (if (index % 9 == 0) 18.dp.toPx() else 9.dp.toPx())
                    val start = Offset(center.x + inner * kotlin.math.sin(angle).toFloat(), center.y - inner * kotlin.math.cos(angle).toFloat())
                    val end = Offset(center.x + radius * kotlin.math.sin(angle).toFloat(), center.y - radius * kotlin.math.cos(angle).toFloat())
                    drawLine(if (index % 9 == 0) primary else outline, start, end, if (index % 9 == 0) 2.dp.toPx() else 1.dp.toPx())
                }
            }
            Box(Modifier.fillMaxSize().padding(29.dp), contentAlignment = Alignment.TopCenter) { LocalizedText("N", fontWeight = FontWeight.Bold, color = primary) }
            Box(Modifier.fillMaxSize().padding(29.dp), contentAlignment = Alignment.BottomCenter) { LocalizedText("S", fontWeight = FontWeight.Bold, color = outline) }
            Box(Modifier.fillMaxSize().padding(29.dp), contentAlignment = Alignment.CenterStart) { LocalizedText("W", fontWeight = FontWeight.Bold, color = outline) }
            Box(Modifier.fillMaxSize().padding(29.dp), contentAlignment = Alignment.CenterEnd) { LocalizedText("E", fontWeight = FontWeight.Bold, color = outline) }
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
            drawPath(needle, if (aligned) Color(0xFFD4AF37) else primary)
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

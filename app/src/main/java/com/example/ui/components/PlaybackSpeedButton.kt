package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.audio.PlaybackSpeed
import com.example.ui.localization.localizedUiText

/** Compact "1x" pill that opens a menu of playback speeds. */
@Composable
fun PlaybackSpeedButton(
    speed: Float,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current
) {
    var open by remember { mutableStateOf(false) }
    val title = localizedUiText("Playback speed")
    Box(modifier) {
        Surface(
            onClick = { com.example.core.analytics.AppAnalytics.interaction("playback_speed_button", "open_speed_menu", "tap"); open = true },
            shape = CircleShape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, color.copy(alpha = .6f)),
            modifier = Modifier.testTag("playback_speed")
                .semantics { contentDescription = "$title ${PlaybackSpeed.label(speed)}" }
        ) {
            Text(
                PlaybackSpeed.label(speed), color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        DropdownMenu(open, onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("playback_speed_button", "dismiss_speed_menu", "dismiss"); open = false }) {
            Text(
                title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            PlaybackSpeed.options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            if (option == PlaybackSpeed.NORMAL) "${PlaybackSpeed.label(option)} · ${localizedUiText("Normal")}"
                            else PlaybackSpeed.label(option),
                            fontWeight = if (option == speed) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("playback_speed_button", "select_speed", "tap", item = "speed_${PlaybackSpeed.label(option)}"); open = false; onSpeedChange(option) },
                    leadingIcon = { if (option == speed) Icon(Icons.Default.Check, null, Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("playback_speed_${PlaybackSpeed.label(option)}")
                )
            }
        }
    }
}

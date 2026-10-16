package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.net.Connectivity
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText

/*
 * Shared loading, empty, error and offline presentation. Every screen uses these so the whole app
 * reacts to the same situations in the same way. Signatures are frozen: add new helpers rather than
 * changing these, because many screens call them.
 */

/** True while the phone has a validated internet connection; recomposes on change. */
@Composable
fun rememberIsOnline(): State<Boolean> = Connectivity.isOnline.collectAsStateWithLifecycle()

/**
 * Runs [action] each time the connection comes back while this composable is on screen, so a
 * screen that failed offline reloads by itself instead of waiting for a tap on Retry.
 */
@Composable
fun OnReconnect(enabled: Boolean = true, action: suspend () -> Unit) {
    val isOnline by rememberIsOnline()
    val latest by rememberUpdatedState(action)
    var wasOffline = false
    LaunchedEffect(isOnline, enabled) {
        if (!enabled) return@LaunchedEffect
        if (!isOnline) wasOffline = true
        else if (wasOffline) { wasOffline = false; latest() }
    }
}

/**
 * Slim notice that slides in when the phone goes offline. Place it at the top of a screen whose
 * content depends on the network; screens that work fully offline do not need it.
 */
@Composable
fun OfflineBanner(
    modifier: Modifier = Modifier,
    message: String = "Saved content still works. Downloads and live data resume when you reconnect."
) {
    val isOnline by rememberIsOnline()
    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .testTag("offline_banner"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText(
                    text = "You're offline",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                LocalizedText(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

/** Centered spinner with an optional line under it. Use for a whole screen or a section. */
@Composable
fun LoadingState(modifier: Modifier = Modifier, message: String? = "Loading…") {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
        if (message != null) {
            Spacer(Modifier.height(12.dp))
            LocalizedText(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Friendly "nothing to show" block for lists, searches and trackers. */
@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    icon: ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) = StateMessage(
    icon = icon,
    title = title,
    modifier = modifier,
    message = message,
    actionText = actionText,
    onAction = onAction
)

/**
 * Failure block with a Retry button. When the phone is offline it explains that instead of the
 * raw error, and the retry fires by itself once the connection returns.
 */
@Composable
fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    message: String? = null,
    retryText: String = "Retry"
) {
    val isOnline by rememberIsOnline()
    OnReconnect { onRetry() }
    StateMessage(
        icon = if (isOnline) Icons.Default.ErrorOutline else Icons.Default.CloudOff,
        title = if (isOnline) title else "You're offline",
        modifier = modifier.testTag("error_state"),
        message = if (isOnline) message else "Saved content still works. Downloads and live data resume when you reconnect.",
        actionText = retryText,
        onAction = onRetry
    )
}

/** One-line inline failure row for a section inside a longer screen. */
@Composable
fun InlineError(message: String, onRetry: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        LocalizedText(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f)
        )
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                LocalizedText(
                    text = "Retry",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/** Small pill that marks a card or row as needing the network while the phone is offline. */
@Composable
fun NeedsInternetChip(modifier: Modifier = Modifier) {
    val isOnline by rememberIsOnline()
    if (isOnline) return
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        LocalizedText(
            text = "Needs internet",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Keeps the Retry button text consistent for callers that build their own layout. */
@Composable
fun RetryButton(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(onClick = onRetry, modifier = modifier) { LocalizedText(text = localizedUiText("Retry")) }
}

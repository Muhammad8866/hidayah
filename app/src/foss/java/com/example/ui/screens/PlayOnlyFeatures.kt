package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.localization.LocalizedText
import com.example.viewmodel.MainUiState

/** Shown in the F-Droid build for features that rely on the Google Firebase backend. */
@Composable
internal fun PlayOnlyFeature(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LocalizedText(
            "This feature is available only in the Google Play version.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onBack) { LocalizedText("Back") }
    }
}

@Composable
fun CityChallengesScreen(onBack: () -> Unit, uiState: MainUiState?, modifier: Modifier = Modifier) =
    PlayOnlyFeature(onBack, modifier)

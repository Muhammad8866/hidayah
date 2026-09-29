package com.example.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.localization.LocalizedText

/** Hosted privacy policy (source: docs/privacy-policy.html). Also entered in Play Console. */
const val PRIVACY_POLICY_URL = "https://muhammad8866.github.io/deen-companion/privacy-policy.html"

private data class Credit(val name: String, val usage: String, val url: String)

private val credits = listOf(
    Credit("Tanzil", "Quran Arabic text (quiz & search)", "https://tanzil.net"),
    Credit("Quran.com / QuranCDN", "Page layout data and word-by-word audio", "https://quran.com"),
    Credit("EveryAyah", "Mishary Rashid Alafasy verse-by-verse audio", "https://everyayah.com"),
    Credit("QuranicAudio", "Mishary Rashid Alafasy full surah audio", "https://quranicaudio.com"),
    Credit("AlQuran Cloud", "Surah text API", "https://alquran.cloud"),
    Credit("Dawat-e-Islami", "Kanzul Iman, Sirat-ul-Jinan tafsir, books and naats", "https://www.dawateislami.net"),
    Credit("Internet Archive", "Public book scans", "https://archive.org"),
    Credit("OpenStreetMap contributors", "Nearby masjid locations via Overpass API (ODbL)", "https://www.openstreetmap.org/copyright"),
    Credit("Currency API (fawazahmed0)", "Gold/silver & currency rates for Zakat", "https://github.com/fawazahmed0/exchange-api"),
    Credit("Google Firebase", "Community masjid timings (anonymous)", "https://firebase.google.com")
)

fun openExternalUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
    }
}

@Composable
fun CreditsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().testTag("credits_screen"), color = MaterialTheme.colorScheme.background) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LocalizedText(
                        text = "Sources & Credits",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Content belongs to its respective publishers. Jazakallah khair to everyone below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(credits, key = { it.name }) { credit ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { openExternalUrl(context, credit.url) }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(credit.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(credit.usage, style = MaterialTheme.typography.bodySmall)
                        Text(credit.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                item {
                    TextButton(onClick = { openExternalUrl(context, PRIVACY_POLICY_URL) }) { LocalizedText("Privacy Policy") }
                    TextButton(onClick = onDismiss) { LocalizedText("Close") }
                }
            }
        }
    }
}

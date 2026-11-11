package com.example.ui.screens

import com.example.ui.components.ScreenHeader
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.BackIconButton
import com.example.ui.components.AppCard
import com.example.ui.components.IconBadge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import com.example.ui.localization.LocalizedText

/** Hosted privacy policy (source: docs/privacy-policy.html). Also entered in Play Console. */
const val PRIVACY_POLICY_URL = "https://muhammad8866.github.io/hidayah/privacy-policy.html"

const val SUPPORT_DEVELOPER_URL = "https://rzp.io/rzp/hidayah-app"
const val SUPPORT_DEVELOPER_PAYPAL_URL = "https://www.paypal.com/ncp/payment/U3FAR43PQFMVC"

const val CONTACT_EMAIL = "misuhrobir@gmail.com"
const val CONTACT_PHONE = "+91 8866389636"

fun openExternalUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
    }
}

/** Prefer Chrome for checkout; otherwise use the device's default link handler. */
fun openPaymentPage(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setPackage("com.android.chrome")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        openExternalUrl(context, url)
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    com.example.core.analytics.AnalyticsScreen("privacy_policy_dialog")
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().testTag("privacy_policy_screen"), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                ScreenHeader("Privacy Policy", onBack = com.example.core.analytics.AppAnalytics.trackAction("privacy_policy_dialog", "back", onDismiss))
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = false
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                    openExternalUrl(context, request.url.toString())
                                    return true
                                }
                            }
                            val html = context.assets.open("privacy-policy.html")
                                .bufferedReader(Charsets.UTF_8).use { it.readText() }
                            loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    onRelease = { view -> view.stopLoading(); view.destroy() }
                )
            }
        }
    }
}

@Composable
fun CreditsDialog(onDismiss: () -> Unit) {
    com.example.core.analytics.AnalyticsScreen("credits_dialog")
    val context = LocalContext.current
    var showPrivacyPolicy by rememberSaveable { mutableStateOf(false) }
    Dialog(onDismissRequest = com.example.core.analytics.AppAnalytics.trackAction("credits_screen", "dismiss_credits", onDismiss), properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize().testTag("credits_screen"), color = MaterialTheme.colorScheme.background) {
            LazyColumn(
                // Keeps the list clear of the status bar, cutouts and gesture bar on edge-to-edge devices.
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    ScreenHeader("Contact", onBack = com.example.core.analytics.AppAnalytics.trackAction("credits_screen", "credits_back", onDismiss))
                }
                item {
                    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        AppCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) {
                            ContactRow(Icons.Default.Email, CONTACT_EMAIL) {
                                com.example.core.analytics.AppAnalytics.interaction("credits_screen", "contact_email", "tap"); openExternalUrl(context, "mailto:$CONTACT_EMAIL")
                            }
                            HorizontalDivider(Modifier.padding(start = 54.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            ContactRow(Icons.Default.Phone, CONTACT_PHONE) {
                                com.example.core.analytics.AppAnalytics.interaction("credits_screen", "contact_phone", "tap"); openExternalUrl(context, "tel:${CONTACT_PHONE.replace(" ", "")}")
                            }
                        }
                    }
                }
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("credits_screen", "open_privacy_policy", "tap"); showPrivacyPolicy = true },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) { LocalizedText("Privacy Policy") }
                        Button(
                            onClick = com.example.core.analytics.AppAnalytics.trackAction("credits_screen", "close_credits", onDismiss),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) { LocalizedText("Close") }
                    }
                }
            }
        }
    }
    if (showPrivacyPolicy) PrivacyPolicyDialog(onDismiss = { showPrivacyPolicy = false })
}

/** A tappable contact line: icon badge and the address or number itself. */
@Composable
private fun ContactRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, accent = MaterialTheme.colorScheme.primary, size = 40.dp, iconSize = 20.dp)
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}

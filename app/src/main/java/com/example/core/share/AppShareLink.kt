package com.example.core.share

import android.content.Context
import android.content.Intent

/** One place for the Play Store link that every share from the app carries. */
object AppShareLink {
    fun storeUrl(packageName: String): String = "https://play.google.com/store/apps/details?id=$packageName"

    /** Appends the store link once; text that already has it is left as is. */
    fun withLink(text: String, packageName: String): String {
        val url = storeUrl(packageName)
        val body = text.trimEnd()
        return when {
            body.isBlank() -> url
            body.contains(url) -> body
            else -> "$body\n\n$url"
        }
    }

    fun withLink(context: Context, text: String): String = withLink(text, context.packageName)

    fun shareAppIntent(appName: String, message: String, packageName: String): Intent =
        Intent(Intent.ACTION_SEND).setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, appName)
            .putExtra(Intent.EXTRA_TEXT, withLink(message, packageName))

    /** Opens the share sheet with the app invite and store link. */
    fun shareApp(context: Context, message: String) {
        val appName = context.getString(com.example.R.string.app_name)
        val chooser = Intent.createChooser(shareAppIntent(appName, message, context.packageName), appName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(chooser) }
    }
}

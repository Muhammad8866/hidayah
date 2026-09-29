package com.example.core.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck

/** Single entry point for Firebase, so App Check is installed before any Firestore/Auth call. */
object FirebaseSetup {
    @Volatile private var app: FirebaseApp? = null

    fun app(context: Context): FirebaseApp? = app ?: synchronized(this) {
        app ?: runCatching {
            val instance = FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
            instance?.also {
                FirebaseAppCheck.getInstance(it).installAppCheckProviderFactory(appCheckProviderFactory())
            }
        }.getOrNull().also { app = it }
    }
}

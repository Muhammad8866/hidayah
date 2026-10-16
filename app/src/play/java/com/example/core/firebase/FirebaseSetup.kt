package com.example.core.firebase

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.example.R

/** Single entry point for Firebase, so App Check is installed before any Firestore/Auth call. */
object FirebaseSetup {
    @Volatile private var app: FirebaseApp? = null

    fun app(context: Context): FirebaseApp? = app ?: synchronized(this) {
        app ?: runCatching {
            val projectId = context.getString(R.string.community_firebase_project_id)
            val instance = FirebaseApp.getApps(context).firstOrNull { it.options.projectId == projectId }
                ?: FirebaseApp.initializeApp(context, FirebaseOptions.Builder()
                    .setProjectId(projectId)
                    .setApplicationId(context.getString(R.string.community_firebase_app_id))
                    .setApiKey(context.getString(R.string.community_firebase_api_key))
                    .setStorageBucket(context.getString(R.string.community_firebase_storage_bucket))
                    .setGcmSenderId(context.getString(R.string.community_firebase_sender_id))
                    .build(), "community_backend")
            instance?.also {
                FirebaseAppCheck.getInstance(it).installAppCheckProviderFactory(appCheckProviderFactory())
            }
        }.getOrNull().also { app = it }
    }
}

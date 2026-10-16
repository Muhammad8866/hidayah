package com.example.feature.citychallenge

import android.content.Context

/** City Challenges need the Firebase backend, which the F-Droid build does not include. */
class CcLocalStore private constructor() {
    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun addDhikr(context: Context, count: Int = 1) = Unit
    }
}

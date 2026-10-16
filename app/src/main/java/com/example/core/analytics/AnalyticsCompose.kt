package com.example.core.analytics

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import android.os.SystemClock
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

/** Inline scroll containers use this so new dialogs and panels keep the same bounded coverage. */
@Composable
fun rememberAnalyticsScrollState(screen: String): ScrollState {
    val state = rememberScrollState()
    AnalyticsScroll(screen, state)
    return state
}

@Composable
fun rememberAnalyticsLazyListState(screen: String): LazyListState {
    val state = rememberLazyListState()
    AnalyticsScroll(screen, state)
    return state
}

/** Screen duration ends on navigation AND backgrounding, and restarts on resume. */
@Composable
fun AnalyticsScreen(name: String) {
    val choice by AppAnalytics.choice.collectAsState()
    if (choice == true) LifecycleResumeEffect(name) {
        val interval = AppAnalytics.startScreen(name)
        onPauseOrDispose { AppAnalytics.endScreen(interval) }
    }
}

@Composable
fun AnalyticsSearch(screen: String, query: String) {
    val choice by AppAnalytics.choice.collectAsState()
    LaunchedEffect(query, choice) {
        if (choice != true || query.isBlank()) return@LaunchedEffect
        delay(1000)
        // No query content or length is transmitted.
        AppAnalytics.emit("search_usage", mapOf("screen" to screen, "action" to "search_edited"))
    }
}

@Composable
fun AnalyticsScroll(screen: String, state: LazyListState) {
    val choice by AppAnalytics.choice.collectAsState()
    LaunchedEffect(screen, state, choice) {
        if (choice != true) return@LaunchedEffect
        val seen = mutableSetOf<Int>()
        snapshotFlow {
            val info = state.layoutInfo
            val index = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            if (!state.canScrollBackward && !state.canScrollForward || info.totalItemsCount == 0) 0
            else if (!state.canScrollForward) 100
            else ((index.toDouble() / info.totalItemsCount * 100).toInt() / 25 * 25).coerceIn(0, 100)
        }.distinctUntilChanged().collect { milestone ->
            if (milestone > 0 && seen.add(milestone)) AppAnalytics.emit("scroll_progress",
                mapOf("screen" to screen, "milestone" to milestone.toLong()))
        }
    }
}

@Composable
fun AnalyticsScroll(screen: String, state: ScrollState) {
    val choice by AppAnalytics.choice.collectAsState()
    LaunchedEffect(screen, state, choice) {
        if (choice != true) return@LaunchedEffect
        val seen = mutableSetOf<Int>()
        snapshotFlow {
            if (state.maxValue <= 0 || state.maxValue == Int.MAX_VALUE) 0
            else (state.value.toDouble() / state.maxValue * 100).toInt() / 25 * 25
        }.distinctUntilChanged().collect { milestone ->
            if (milestone > 0 && seen.add(milestone)) AppAnalytics.emit("scroll_progress",
                mapOf("screen" to screen, "milestone" to milestone.toLong()))
        }
    }
}

/** One reading sitting: counts page turns while the reader is resumed and reports them on pause/leave. */
@Composable
fun AnalyticsReaderSession(feature: String, page: Int) {
    val choice by AppAnalytics.choice.collectAsState()
    val turns = remember { mutableIntStateOf(0) }
    var lastPage by remember { mutableIntStateOf(page) }
    if (page != lastPage) { lastPage = page; turns.intValue++ }
    if (choice == true) LifecycleResumeEffect(feature) {
        val startedAt = SystemClock.elapsedRealtime()
        turns.intValue = 0
        onPauseOrDispose { AppAnalytics.readerSession(feature, turns.intValue, startedAt) }
    }
}

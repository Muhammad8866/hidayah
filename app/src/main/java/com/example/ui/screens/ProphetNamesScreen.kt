package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.names.ProphetName
import com.example.names.ProphetNames
import com.example.ui.components.AppSearchField
import com.example.ui.components.EmptyState
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText

/** Asma un-Nabi ﷺ: ninety-nine blessed names of the Prophet ﷺ with their meanings, fully offline. */
@Composable
fun ProphetNamesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("prophet_names_screen")
    val language = LocalAppLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("prophet_names_screen", query)
    val shown = remember(query, language) {
        val words = query.lowercase().split(' ').filter(String::isNotBlank)
        ProphetNames.names.filter { name ->
            val text = "${name.number} ${name.transliteration} ${name.english} ${name.meaning(language)} ${name.arabic}".lowercase()
            words.all(text::contains)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("prophet_names_screen"),
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item {
            ScreenHeader(
                title = "99 Names of the Prophet ﷺ",
                subtitle = "Asma un-Nabi ﷺ",
                localizeSubtitle = false,
                onBack = onBack
            )
        }
        item { NamesHadithHero(ProphetNames.HADITH, ProphetNames.HADITH_SOURCE, "prophet_names_hadith") }
        item {
            AppSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search name or meaning",
                modifier = Modifier.padding(vertical = 4.dp).testTag("prophet_names_search")
            )
        }
        if (shown.isEmpty()) item {
            EmptyState(title = "No name found.", icon = Icons.Default.SearchOff)
        }
        items(shown, key = ProphetName::number) { name ->
            SacredNameRow(
                number = name.number,
                transliteration = name.transliteration,
                meaning = name.meaning(language),
                arabic = name.arabic,
                modifier = Modifier.testTag("prophet_name_${name.number}")
            )
        }
        item {
            LocalizedText(ProphetNames.LIST_SOURCE,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

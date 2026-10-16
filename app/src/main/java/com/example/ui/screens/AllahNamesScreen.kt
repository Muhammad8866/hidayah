package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.names.AllahName
import com.example.names.AsmaUlHusna
import com.example.ui.components.AppCard
import com.example.ui.components.AppSearchField
import com.example.ui.components.EmptyState
import com.example.ui.components.HeroSurface
import com.example.ui.components.ScreenHeader
import com.example.ui.components.Spacing
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalizedText
import com.example.ui.theme.ArabicFontFamily
import com.example.ui.theme.GoldAccent

/** Al-Asma ul-Husna: the ninety-nine names of Allah with their meanings, fully offline. */
@Composable
fun AllahNamesScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    com.example.core.analytics.AnalyticsScreen("allah_names_screen")
    val language = LocalAppLanguage.current
    var query by rememberSaveable { mutableStateOf("") }
    com.example.core.analytics.AnalyticsSearch("allah_names_screen", query)
    val shown = remember(query, language) {
        val words = query.lowercase().split(' ').filter(String::isNotBlank)
        AsmaUlHusna.names.filter { name ->
            val text = "${name.number} ${name.transliteration} ${name.english} ${name.meaning(language)} ${name.arabic}".lowercase()
            words.all(text::contains)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().testTag("allah_names_screen"),
        contentPadding = PaddingValues(start = Spacing.screen, end = Spacing.screen, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        item {
            ScreenHeader(
                title = "99 Names of Allah",
                subtitle = "Al-Asma ul-Husna",
                localizeSubtitle = false,
                onBack = onBack
            )
        }
        item { NamesHadithHero(AsmaUlHusna.HADITH, AsmaUlHusna.HADITH_SOURCE, "allah_names_hadith") }
        item {
            AppSearchField(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search name or meaning",
                modifier = Modifier.padding(vertical = 4.dp).testTag("allah_names_search")
            )
        }
        if (shown.isEmpty()) item {
            EmptyState(title = "No name found.", icon = Icons.Default.SearchOff)
        }
        items(shown, key = AllahName::number) { name ->
            SacredNameRow(
                number = name.number,
                transliteration = name.transliteration,
                meaning = name.meaning(language),
                arabic = name.arabic,
                modifier = Modifier.testTag("allah_name_${name.number}")
            )
        }
        item {
            LocalizedText(AsmaUlHusna.LIST_SOURCE,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** The hadith on the virtue of the names, set on the emerald hero so it frames the list. */
@Composable
internal fun NamesHadithHero(hadith: String, source: String, tag: String) {
    HeroSurface(
        modifier = Modifier.fillMaxWidth().testTag(tag),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        LocalizedText(hadith,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.92f))
        Text(source,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = GoldAccent)
    }
}

/** Number set in an eight-pointed star: two rounded squares, one turned 45 degrees. */
@Composable
internal fun NumberStarBadge(number: Int) {
    val fill = MaterialTheme.colorScheme.primaryContainer
    Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(30.dp).rotate(45f).background(fill, RoundedCornerShape(7.dp)))
        Box(Modifier.size(30.dp).background(fill, RoundedCornerShape(7.dp)))
        Text(number.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

/** One blessed name: star-numbered badge, transliteration and meaning, and the Arabic set large. */
@Composable
internal fun SacredNameRow(
    number: Int,
    transliteration: String,
    meaning: String,
    arabic: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 10.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NumberStarBadge(number)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(transliteration, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(meaning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = arabic,
                fontFamily = ArabicFontFamily,
                color = MaterialTheme.colorScheme.primary,
                style = TextStyle(fontSize = 28.sp, lineHeight = 52.sp, textDirection = TextDirection.Rtl)
            )
        }
    }
}

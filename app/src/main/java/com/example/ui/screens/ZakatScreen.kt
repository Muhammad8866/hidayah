@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.ui.screens

import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.ProvenanceCard
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import com.example.prayer.HijriCalendarHelper
import com.example.prayer.HijriRegion
import java.time.Instant
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.cancellation.CancellationException

private val zakatCurrencies = listOf(
    "PKR", "INR", "BDT", "USD", "GBP", "EUR", "CAD", "AUD", "SAR", "AED", "QAR", "KWD", "OMR", "BHD", "MYR", "IDR", "TRY", "ZAR",
    "EGP", "NGN", "MAD", "DZD", "TND", "JOD", "IQD", "LKR", "NPR", "MVR", "AFN", "SGD", "BND", "KES", "TZS", "CHF", "SEK", "NOK",
    "DKK", "NZD", "RUB", "KZT", "UZS", "CNY", "JPY"
)

/** The currency of the country the device is set to; the rate source prices metals in every currency. */
private fun regionCurrency(locale: Locale): String? =
    runCatching { Currency.getInstance(locale).currencyCode }.getOrNull()
        ?: runCatching { Currency.getInstance(Locale.getDefault()).currencyCode }.getOrNull()

private fun defaultZakatCurrency(locale: Locale): String = regionCurrency(locale) ?: "USD"

/** LIVE fills the rates from the internet; typing a rate switches to the person's own local rate. */
private enum class ZakatRateSource { LIVE, MANUAL }

private enum class RateFetch { IDLE, LOADING, FAILED }

private fun ZakatInput.withLiveRates(snapshot: ZakatMetalRates.Snapshot, currency: String): ZakatInput = copy(
    goldRate = snapshot.rate(gold = true, currency, unit)?.toPlainString().orEmpty(),
    silverRate = snapshot.rate(gold = false, currency, unit)?.toPlainString().orEmpty()
)

private fun todayHijri(): HijriDay =
    HijriCalendarHelper.getHijriDate(Calendar.getInstance()).let { HijriDay(it.year, it.month, it.day) }

private inline fun <reified T : Enum<T>> SharedPreferences.enumValue(key: String, default: T): T =
    getString(key, null)?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

/** Figures stay on the device so next year's calculation starts from last year's. */
private fun SharedPreferences.loadZakatInput() = ZakatInput(
    cash = getString("zakat_cash", "").orEmpty(),
    goldWeight = getString("zakat_gold_weight", "").orEmpty(),
    silverWeight = getString("zakat_silver_weight", "").orEmpty(),
    business = getString("zakat_business", "").orEmpty(),
    receivables = getString("zakat_receivables", "").orEmpty(),
    investments = getString("zakat_investments", "").orEmpty(),
    otherAssets = getString("zakat_other_assets", "").orEmpty(),
    debts = getString("zakat_debts", "").orEmpty(),
    bills = getString("zakat_bills", "").orEmpty(),
    installments = getString("zakat_installments", "").orEmpty(),
    goldRate = getString("zakat_gold_rate", "").orEmpty(),
    silverRate = getString("zakat_silver_rate", "").orEmpty(),
    goldKarat = enumValue("zakat_gold_karat", GoldKarat.K24),
    unit = enumValue("zakat_unit", MetalUnit.TOLA),
    nisabMode = enumValue("zakat_nisab_mode", ZakatNisabMode.AUTO)
    // The lunar-year answer is asked afresh each time so an old "yes" is never reused.
)

private fun SharedPreferences.saveZakatInput(input: ZakatInput) {
    edit()
        .putString("zakat_cash", input.cash)
        .putString("zakat_gold_weight", input.goldWeight)
        .putString("zakat_silver_weight", input.silverWeight)
        .putString("zakat_business", input.business)
        .putString("zakat_receivables", input.receivables)
        .putString("zakat_investments", input.investments)
        .putString("zakat_other_assets", input.otherAssets)
        .putString("zakat_debts", input.debts)
        .putString("zakat_bills", input.bills)
        .putString("zakat_installments", input.installments)
        .putString("zakat_gold_rate", input.goldRate)
        .putString("zakat_silver_rate", input.silverRate)
        .putString("zakat_gold_karat", input.goldKarat.name)
        .putString("zakat_unit", input.unit.name)
        .putString("zakat_nisab_mode", input.nisabMode.name)
        .apply()
}

@Composable
internal fun ZakatScreen(onBack: () -> Unit, modifier: Modifier) {
    com.example.core.analytics.AnalyticsScreen("zakat_screen")

    val prefs = rememberToolPrefs()
    val context = LocalContext.current
    val locale = LocalAppLocale.current
    val number = remember(locale) {
        NumberFormat.getNumberInstance(locale).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
    }
    var form by remember { mutableStateOf(prefs.loadZakatInput()) }
    var currency by remember { mutableStateOf(prefs.getString("zakat_currency", null) ?: defaultZakatCurrency(locale)) }
    // The device's own currency is always offered, even when it is not one of the common ones.
    val currencies = remember(locale, currency) { (zakatCurrencies + listOfNotNull(regionCurrency(locale), currency)).distinct() }
    val symbols = remember(locale) { DecimalFormatSymbols.getInstance(locale) }
    val grouping = remember(symbols) { GroupingTransformation(symbols.groupingSeparator, symbols.decimalSeparator) }
    var ratesDate by remember { mutableStateOf(prefs.getString("zakat_rates_date", null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }) }
    var showMoreAssets by remember { mutableStateOf(form.investments.isNotBlank() || form.otherAssets.isNotBlank()) }
    var showMoreLiabilities by remember { mutableStateOf(form.bills.isNotBlank() || form.installments.isNotBlank()) }
    var confirmClear by rememberSaveable { mutableStateOf(false) }

    var rateSource by remember { mutableStateOf(prefs.enumValue("zakat_rate_source", ZakatRateSource.LIVE)) }
    var liveRates by remember { mutableStateOf(ZakatMetalRates.load(prefs)) }
    var rateFetch by remember { mutableStateOf(RateFetch.IDLE) }
    var retry by remember { mutableIntStateOf(0) }
    var handledRetry by remember { mutableIntStateOf(0) }
    var hawlStart by remember { mutableStateOf(HijriDay.parse(prefs.getString("zakat_hawl_start", null))) }
    var pickHawlDate by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(form) { prefs.saveZakatInput(form) }
    LaunchedEffect(currency) { prefs.edit().putString("zakat_currency", currency).apply() }
    LaunchedEffect(rateSource) { prefs.edit().putString("zakat_rate_source", rateSource.name).apply() }
    LaunchedEffect(hawlStart) { prefs.edit().putString("zakat_hawl_start", hawlStart?.toString()).apply() }

    // Live rates: refresh when stale (or on retry), then keep the fields in step with currency and unit.
    LaunchedEffect(rateSource, retry, currency) {
        if (rateSource != ZakatRateSource.LIVE) return@LaunchedEffect
        // A snapshot saved before this currency was offered has no price for it.
        val covered = liveRates?.let { currency in it.goldPerOunce && currency in it.silverPerOunce } == true
        if (retry == handledRetry && covered && liveRates?.isStale() == false) return@LaunchedEffect
        handledRetry = retry
        rateFetch = RateFetch.LOADING
        rateFetch = runCatching { ZakatMetalRates.fetch(currencies) }
            .onSuccess { liveRates = it; ZakatMetalRates.save(prefs, it) }
            .fold({ RateFetch.IDLE }, { if (it is CancellationException) throw it else RateFetch.FAILED })
    }
    // A fetch that failed offline is repeated by itself once the connection returns.
    com.example.ui.components.OnReconnect(enabled = rateSource == ZakatRateSource.LIVE && rateFetch == RateFetch.FAILED) { retry++ }
    LaunchedEffect(rateSource, liveRates, currency, form.unit) {
        val snapshot = liveRates
        if (rateSource == ZakatRateSource.LIVE && snapshot != null) {
            form = form.withLiveRates(snapshot, currency)
            ratesDate = snapshot.date
            prefs.edit().putString("zakat_rates_date", snapshot.date.toString()).apply()
        }
    }

    fun updateRates(next: ZakatInput) {
        form = next
        rateSource = ZakatRateSource.MANUAL
        ratesDate = LocalDate.now()
        prefs.edit().putString("zakat_rates_date", ratesDate.toString()).apply()
    }

    val todayHijri = remember { todayHijri() }
    val hawl = hawlStart?.let { hawlState(it, todayHijri) }
    val hijriRegion = remember { HijriRegion.forTimeZone(TimeZone.getDefault().id) }
    val dateFormat = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }
    fun hijriText(day: HijriDay) = HijriCalendarHelper.format(HijriCalendarHelper.create(day.day, day.month, day.year))
    fun gregorianText(day: HijriDay) = HijriCalendarHelper.hijriToGregorian(day.year, day.month, day.day, hijriRegion)
        .let { (y, m, d) -> LocalDate.of(y, m, d).format(dateFormat) }
    fun daysUntil(day: HijriDay) = HijriCalendarHelper.daysUntil(
        HijriCalendarHelper.create(todayHijri.day, todayHijri.month, todayHijri.year), day.year, day.month, day.day, hijriRegion
    )

    val result = calculateZakat(if (hawl != null) form.copy(lunarYearPassed = hawl.passed) else form)
    // Only that the calculator produced a result in this visit: no amounts and not whether zakat is due.
    val calculated = result.status != ZakatStatus.EMPTY
    LaunchedEffect(calculated) { if (calculated) com.example.core.analytics.AppAnalytics.milestone("zakat", "calculated") }
    fun money(value: BigDecimal) = "$currency ${number.format(value)}"
    val unitLabel = localizedUiText(if (form.unit == MetalUnit.TOLA) "Tola" else "Gram").lowercase(locale)
    val basisMetal = localizedUiText(if (result.basis == ZakatNisabBasis.SILVER) "Silver" else "Gold")
    val basisWeight = "${nisabWeight(result.basis, form.unit).toPlainString()} $unitLabel ${basisMetal.lowercase(locale)}"

    ToolScreen(DailyTool.ZAKAT, onBack, modifier) { accent ->
        item(key = "result") {
            val shareTitle = localizedUiText("Zakat calculator")
            val shareLines = listOf(
                localizedUiText("Total assets") to money(result.assets),
                localizedUiText("Deductible liabilities") to money(result.liabilities),
                localizedUiText("Net zakatable wealth") to money(result.net),
                "${localizedUiText("Nisab")} ($basisWeight)" to (result.nisab?.let(::money) ?: "—"),
                localizedUiText("Zakat due") to money(result.due ?: BigDecimal.ZERO)
            )
            val statusText = localizedUiText(statusMessage(result.status))
            HeroCard(accent) {
                LocalizedText("Zakat due", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                Text(
                    when (result.status) {
                        ZakatStatus.EMPTY -> "—"
                        else -> money(result.due ?: BigDecimal.ZERO)
                    },
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (result.due != null) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("zakat_due")
                )
                Spacer(Modifier.height(8.dp))
                StatusPill(result.status, statusText, accent)
                if (result.status == ZakatStatus.AWAITING_HAWL) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${localizedUiText("Payable after the lunar year")}: ${money(result.estimate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                val progress = result.nisabProgress
                if (progress != null && result.status != ZakatStatus.EMPTY && result.nisab != null) {
                    Spacer(Modifier.height(14.dp))
                    NisabMeter(progress, result.net, result.nisab, basisWeight, accent, ::money)
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = accent.copy(alpha = 0.2f))
                SummaryLine("Total assets", money(result.assets))
                SummaryLine("Deductible liabilities", "‎− ${money(result.liabilities)}")
                SummaryLine("Net zakatable wealth", money(result.net), emphasize = true)
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "share_result", "tap");
                            val text = buildString {
                                appendLine(shareTitle)
                                appendLine(statusText)
                                shareLines.forEach { (label, value) -> appendLine("$label: $value") }
                            }.trim()
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, shareTitle)
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        },
                        enabled = result.status != ZakatStatus.EMPTY
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        LocalizedText("Share result")
                    }
                    TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "open_clear_figures", "tap"); confirmClear = true }, enabled = result.status != ZakatStatus.EMPTY || form.debts.isNotBlank()) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        LocalizedText("Clear figures")
                    }
                }
            }
        }

        item(key = "rates") {
            ToolCard {
                StepTitle(1, "Today's rates", accent)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    CurrencyPicker(currency, currencies, { currency = it }, Modifier.weight(1f))
                    SingleChoiceSegmentedButtonRow(Modifier.weight(1.4f)) {
                        MetalUnit.entries.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = form.unit == option,
                                onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "select_weight_unit", "tap", item = option.name);
                                    if (form.unit != option) form = form.convertUnit(option)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index, MetalUnit.entries.size)
                            ) { LocalizedText(if (option == MetalUnit.TOLA) "Tola" else "Gram", maxLines = 1) }
                        }
                    }
                }
                MoneyField(
                    grouping,
                    "Gold rate (24K)",
                    form.goldRate,
                    { updateRates(form.copy(goldRate = it)) },
                    prefix = currency,
                    suffix = "/ $unitLabel",
                    isError = result.status == ZakatStatus.NEED_GOLD_RATE
                )
                MoneyField(
                    grouping,
                    "Silver rate",
                    form.silverRate,
                    { updateRates(form.copy(silverRate = it)) },
                    prefix = currency,
                    suffix = "/ $unitLabel",
                    isError = result.status == ZakatStatus.NEED_SILVER_RATE
                )
                if (rateSource == ZakatRateSource.LIVE) {
                    LiveRateStatus(
                        fetch = rateFetch,
                        date = liveRates?.date?.format(dateFormat),
                        accent = accent,
                        onRefresh = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "refresh_metal_rates", "refresh"); retry++ }
                    )
                } else {
                    val staleDate = ratesDate?.takeIf { it.isBefore(LocalDate.now()) && (form.goldRate.isNotBlank() || form.silverRate.isNotBlank()) }
                    if (staleDate != null) {
                        Text(
                            "${localizedUiText("Rates last updated")}: ${staleDate.format(dateFormat)}. ${localizedUiText("Please enter today's rates.")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LocalizedText(
                            "Using your own rates",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "use_live_rates", "tap"); rateSource = ZakatRateSource.LIVE; retry++ }) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            LocalizedText("Use live rates")
                        }
                    }
                }
                LocalizedText(
                    "Live rates are the international market price of pure (24K) gold and silver. Your local market rate can differ a little; to use it, just type it in. The silver rate is needed to check the nisab for mixed wealth, even if you own no silver.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "metals") {
            ToolCard {
                StepTitle(2, "Gold & silver", accent)
                LocalizedText("Gold purity", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    GoldKarat.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = form.goldKarat == option,
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "select_gold_karat", "tap", item = option.name); form = form.copy(goldKarat = option) },
                            shape = SegmentedButtonDefaults.itemShape(index, GoldKarat.entries.size),
                            modifier = Modifier.testTag("zakat_karat_${option.karat}")
                        ) { Text("${option.karat}K", maxLines = 1) }
                    }
                }
                MoneyField(
                    grouping,
                    "Gold you own",
                    form.goldWeight,
                    { form = form.copy(goldWeight = it) },
                    suffix = unitLabel,
                    supporting = result.goldValue?.takeIf { it.signum() > 0 }?.let { "≈ ${money(it)}" }
                )
                MoneyField(
                    grouping,
                    "Silver you own",
                    form.silverWeight,
                    { form = form.copy(silverWeight = it) },
                    suffix = unitLabel,
                    supporting = result.silverValue?.takeIf { it.signum() > 0 }?.let { "≈ ${money(it)}" }
                )
                LocalizedText(
                    "Enter the weight and pick the karat (most jewellery is 22K or 21K). In Hanafi fiqh jewellery is included, even if it is worn; leave out stones and non-gold parts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "assets") {
            ToolCard {
                StepTitle(3, "Cash & other assets", accent)
                MoneyField(grouping, "Cash & bank balance", form.cash, { form = form.copy(cash = it) }, prefix = currency)
                MoneyField(grouping, "Business stock", form.business, { form = form.copy(business = it) }, prefix = currency)
                MoneyField(grouping, "Money owed to you", form.receivables, { form = form.copy(receivables = it) }, prefix = currency)
                AnimatedVisibility(showMoreAssets) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MoneyField(grouping, "Zakatable investments and shares", form.investments, { form = form.copy(investments = it) }, prefix = currency)
                        MoneyField(grouping, "Other zakatable assets", form.otherAssets, { form = form.copy(otherAssets = it) }, prefix = currency)
                    }
                }
                ExpandToggle(showMoreAssets, "More assets", "Hide more assets") { showMoreAssets = !showMoreAssets }
                LocalizedText(
                    "Enter current values in one currency. Include receivables you expect to recover; avoid counting an asset twice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "liabilities") {
            ToolCard {
                StepTitle(4, "What you owe", accent)
                MoneyField(grouping, "Debts due now", form.debts, { form = form.copy(debts = it) }, prefix = currency)
                AnimatedVisibility(showMoreLiabilities) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        MoneyField(grouping, "Bills and taxes already due", form.bills, { form = form.copy(bills = it) }, prefix = currency)
                        MoneyField(grouping, "Upcoming principal instalments", form.installments, { form = form.copy(installments = it) }, prefix = currency)
                        LocalizedText(
                            "For long-term debts, enter only eligible near-term principal payments, not the full balance or interest. Scholars differ on the deductible period.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                ExpandToggle(showMoreLiabilities, "More liabilities", "Hide more liabilities") { showMoreLiabilities = !showMoreLiabilities }
            }
        }

        item(key = "nisab") {
            ToolCard {
                StepTitle(5, "Nisab & lunar year", accent)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ZakatNisabMode.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = form.nisabMode == option,
                            onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "select_nisab_mode", "tap", item = option.name); form = form.copy(nisabMode = option) },
                            shape = SegmentedButtonDefaults.itemShape(index, ZakatNisabMode.entries.size)
                        ) {
                            LocalizedText(
                                when (option) {
                                    ZakatNisabMode.AUTO -> "Auto"
                                    ZakatNisabMode.SILVER -> "Silver"
                                    ZakatNisabMode.GOLD -> "Gold"
                                },
                                maxLines = 1
                            )
                        }
                    }
                }
                Text(
                    "${localizedUiText("Nisab in use")}: $basisWeight" + (result.nisab?.let { " = ${money(it)}" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LocalizedText(
                    when (form.nisabMode) {
                        ZakatNisabMode.AUTO -> "Auto follows Hanafi fiqh: if you own only gold, the gold nisab (7.5 tola) applies; if gold is mixed with cash, silver or trade goods, the silver nisab (52.5 tola) applies, as it benefits more of the poor."
                        ZakatNisabMode.SILVER -> "The silver nisab (52.5 tola) is commonly used in Hanafi fiqh for mixed wealth, as it benefits more of the poor."
                        ZakatNisabMode.GOLD -> "The gold nisab (7.5 tola) applies in Hanafi fiqh when gold is the only zakatable wealth."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                val start = hawlStart
                if (start != null && hawl != null) {
                    HawlTracker(
                        startHijri = hijriText(start),
                        startGregorian = gregorianText(start),
                        hawl = hawl,
                        hijriText = ::hijriText,
                        gregorianText = ::gregorianText,
                        daysUntil = ::daysUntil,
                        accent = accent,
                        onChange = { pickHawlDate = true },
                        onRemove = { hawlStart = null }
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            LocalizedText("A full lunar (Hijri) year has passed on nisab-level wealth", style = MaterialTheme.typography.bodyLarge)
                            LocalizedText(
                                "Count from the Hijri date you first owned the nisab. Wealth added during the year is included.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = form.lunarYearPassed,
                            onCheckedChange = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "toggle_lunar_year_passed", "toggle"); form = form.copy(lunarYearPassed = it) },
                            modifier = Modifier.testTag("zakat_hawl")
                        )
                    }
                    OutlinedButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "open_hawl_date_picker", "tap"); pickHawlDate = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        LocalizedText("Set my zakat date")
                    }
                    LocalizedText(
                        "Pick the date you first owned the nisab (or your fixed yearly zakat date). The app then tracks the lunar year and tells you when zakat falls due.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item(key = "hadith_rate") {
            ProvenanceCard(
                sourceText = "Give a quarter of a tenth: one dirham from every forty.",
                collectionOrReference = "Sunan Abi Dawud 1572"
            )
        }
        item(key = "hadith_nisab") {
            ProvenanceCard(
                sourceText = "When you have two hundred dirhams and a year has passed on them, five dirhams are due. Nothing is due on gold until you have twenty dinars.",
                collectionOrReference = "Sunan Abi Dawud 1573"
            )
        }
        item(key = "note") {
            InfoNote("Your figures are saved only on this phone. This is an estimate; for personal cases, ask a trusted mufti.")
        }
    }

    if (pickHawlDate) {
        HawlDatePicker(
            initial = hawlStart?.let { day ->
                HijriCalendarHelper.hijriToGregorian(day.year, day.month, day.day, hijriRegion).let { (y, m, d) -> LocalDate.of(y, m, d) }
            },
            onPick = { date ->
                val calendar = Calendar.getInstance().apply { clear(); set(date.year, date.monthValue - 1, date.dayOfMonth) }
                hawlStart = HijriCalendarHelper.getHijriDate(calendar).let { HijriDay(it.year, it.month, it.day) }
                pickHawlDate = false
            },
            onDismiss = { pickHawlDate = false }
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "dismiss_clear_figures", "dismiss"); confirmClear = false },
            title = { LocalizedText("Clear figures?") },
            text = { LocalizedText("All amounts and weights will be removed. Your currency and rates stay.") },
            confirmButton = {
                TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "confirm_clear_figures", "tap");
                    form = ZakatInput(goldRate = form.goldRate, silverRate = form.silverRate, unit = form.unit, nisabMode = form.nisabMode)
                    showMoreAssets = false
                    showMoreLiabilities = false
                    confirmClear = false
                }) { LocalizedText("Clear all") }
            },
            dismissButton = { TextButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "cancel_clear_figures", "tap"); confirmClear = false }) { LocalizedText("Cancel") } }
        )
    }
}

private fun statusMessage(status: ZakatStatus): String = when (status) {
    ZakatStatus.EMPTY -> "Add your gold, silver, cash or other assets to begin."
    ZakatStatus.NEED_GOLD_RATE -> "Enter today's gold rate to value your gold and check the nisab."
    ZakatStatus.NEED_SILVER_RATE -> "Enter today's silver rate to check the nisab."
    ZakatStatus.BELOW_NISAB -> "Below nisab. Zakat is not due."
    ZakatStatus.AWAITING_HAWL -> "Above nisab. Zakat becomes due once a lunar year passes."
    ZakatStatus.DUE -> "Zakat is due: 2.5% of your net zakatable wealth."
}

/** Switching tola ↔ gram converts entered weights and rates so the totals stay the same. */
private fun ZakatInput.convertUnit(target: MetalUnit): ZakatInput {
    val factor = BigDecimal(unit.grams).divide(BigDecimal(target.grams), 10, java.math.RoundingMode.HALF_UP)
    fun weight(value: String) = value.toBigDecimalOrNull()?.multiply(factor)
        ?.setScale(3, java.math.RoundingMode.HALF_UP)?.stripTrailingZeros()?.toPlainString() ?: value
    fun rate(value: String) = value.toBigDecimalOrNull()?.divide(factor, 2, java.math.RoundingMode.HALF_UP)
        ?.stripTrailingZeros()?.toPlainString() ?: value
    return copy(
        unit = target,
        goldWeight = weight(goldWeight),
        silverWeight = weight(silverWeight),
        goldRate = rate(goldRate),
        silverRate = rate(silverRate)
    )
}

@Composable
private fun StatusPill(status: ZakatStatus, text: String, accent: Color) {
    val (icon: ImageVector, tint: Color) = when (status) {
        ZakatStatus.DUE -> Icons.Default.CheckCircle to SuccessGreen
        ZakatStatus.AWAITING_HAWL -> Icons.Default.HourglassTop to accent
        ZakatStatus.BELOW_NISAB -> Icons.Default.Info to MaterialTheme.colorScheme.onSurfaceVariant
        ZakatStatus.NEED_GOLD_RATE, ZakatStatus.NEED_SILVER_RATE -> Icons.Default.Edit to MaterialTheme.colorScheme.error
        ZakatStatus.EMPTY -> Icons.Default.Calculate to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = MaterialTheme.shapes.medium, color = tint.copy(alpha = 0.12f)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.testTag("zakat_status"))
        }
    }
}

@Composable
private fun NisabMeter(
    progress: Float,
    net: BigDecimal,
    nisab: BigDecimal,
    basisWeight: String,
    accent: Color,
    money: (BigDecimal) -> String
) {
    val reached = net >= nisab
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                "${localizedUiText("Nisab")} · $basisWeight",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Text(money(nisab), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = if (reached) SuccessGreen else accent,
            trackColor = accent.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round,
            drawStopIndicator = {}
        )
        Text(
            if (reached) "${localizedUiText("Above nisab by")} ${money(net - nisab)}"
            else "${localizedUiText("Short of nisab by")} ${money(nisab - net)}",
            style = MaterialTheme.typography.bodySmall,
            color = if (reached) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepTitle(number: Int, title: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepNumberBadge(number, accent, filled = true, size = 30.dp)
        Spacer(Modifier.width(12.dp))
        SectionTitle(title)
    }
}

@Composable
private fun ExpandToggle(expanded: Boolean, showLabel: String, hideLabel: String, onClick: () -> Unit) {
    TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("zakat_screen", "toggle_more_fields", onClick), contentPadding = PaddingValues(horizontal = 4.dp)) {
        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(4.dp))
        LocalizedText(if (expanded) hideLabel else showLabel)
    }
}

@Composable
private fun CurrencyPicker(currency: String, currencies: List<String>, onSelect: (String) -> Unit, modifier: Modifier) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(
            onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "open_currency_menu", "tap"); open = true },
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text(currency, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = localizedUiText("Currency"))
        }
        DropdownMenu(expanded = open, onDismissRequest = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "dismiss_currency_menu", "dismiss"); open = false }) {
            currencies.forEach { code ->
                DropdownMenuItem(
                    text = { Text(code, fontWeight = if (code == currency) FontWeight.Bold else FontWeight.Normal) },
                    onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "select_currency", "tap", item = code); onSelect(code); open = false }
                )
            }
        }
    }
}

@Composable
private fun MoneyField(
    grouping: VisualTransformation,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String? = null,
    suffix: String? = null,
    supporting: String? = null,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val cleaned = buildString {
                input.forEach { character ->
                    when {
                        character == '.' || character == ',' || character == '٫' -> append('.')
                        Character.digit(character, 10) >= 0 -> append(Character.digit(character, 10))
                    }
                }
            }
            if (cleaned.length <= 16 && cleaned.count { it == '.' } <= 1) onValueChange(cleaned)
        },
        label = { LocalizedText(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        prefix = prefix?.let { { Text("$it ", color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        suffix = suffix?.let { { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) } },
        supportingText = supporting?.let { { Text(it) } },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "clear_money_field", "tap"); onValueChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = localizedUiText("Clear field"))
                }
            }
        } else null,
        isError = isError && value.isBlank(),
        visualTransformation = grouping,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Shows 1250000.5 as 1,250,000.5 (or 1.250.000,5 where that is the local convention) while the
 * stored value stays plain digits with a dot.
 */
private class GroupingTransformation(private val group: Char, private val decimal: Char) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val integerDigits = raw.indexOf('.').let { if (it < 0) raw.length else it }
        fun separatorsBefore(offset: Int) = (1..minOf(offset, integerDigits - 1)).count { (integerDigits - it) % 3 == 0 }
        val grouped = buildString {
            raw.forEachIndexed { index, character ->
                if (index in 1 until integerDigits && (integerDigits - index) % 3 == 0) append(group)
                append(if (character == '.') decimal else character)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = offset + separatorsBefore(offset)
            override fun transformedToOriginal(offset: Int): Int =
                (0..raw.length).lastOrNull { originalToTransformed(it) <= offset } ?: 0
        }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

@Composable
private fun SummaryLine(label: String, value: String, emphasize: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        LocalizedText(
            label,
            style = if (emphasize) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            color = if (emphasize) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (emphasize) FontWeight.SemiBold else null,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LiveRateStatus(fetch: RateFetch, date: String?, accent: Color, onRefresh: () -> Unit) {
    val (text, color) = when {
        fetch == RateFetch.LOADING -> localizedUiText("Getting today's live rates…") to MaterialTheme.colorScheme.onSurfaceVariant
        fetch == RateFetch.FAILED && date != null ->
            "${localizedUiText("No internet. Using saved rates of")} $date" to MaterialTheme.colorScheme.error
        fetch == RateFetch.FAILED -> localizedUiText("Could not get live rates. Check the internet or type today's rates.") to MaterialTheme.colorScheme.error
        date != null -> "${localizedUiText("Live rates")} · $date" to SuccessGreen
        else -> localizedUiText("Getting today's live rates…") to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.testTag("zakat_rate_status")) {
        if (fetch == RateFetch.LOADING) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = accent)
        } else {
            Icon(
                if (fetch == RateFetch.FAILED) Icons.Default.CloudOff else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        IconButton(onClick = onRefresh, enabled = fetch != RateFetch.LOADING) {
            Icon(Icons.Default.Refresh, contentDescription = localizedUiText("Refresh rates"))
        }
    }
}

@Composable
private fun HawlTracker(
    startHijri: String,
    startGregorian: String,
    hawl: HawlState,
    hijriText: (HijriDay) -> String,
    gregorianText: (HijriDay) -> String,
    daysUntil: (HijriDay) -> Int,
    accent: Color,
    onChange: () -> Unit,
    onRemove: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.testTag("zakat_hawl_tracker")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Event, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                LocalizedText("Nisab first owned on", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(startHijri, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(startGregorian, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("zakat_screen", "change_hawl_date", onChange)) { LocalizedText("Change") }
            IconButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("zakat_screen", "remove_hawl_date", onRemove)) {
                Icon(Icons.Default.Close, contentDescription = localizedUiText("Remove zakat date"))
            }
        }
        val next = hawl.nextDue
        val daysLeft = daysUntil(next)
        val lastDue = hawl.lastDue
        if (lastDue != null) {
            val dueToday = daysUntil(lastDue) == 0
            Surface(shape = RoundedCornerShape(12.dp), color = SuccessGreen.copy(alpha = 0.12f)) {
                Column(Modifier.fillMaxWidth().padding(10.dp)) {
                    Text(
                        if (dueToday) localizedUiText("Your lunar year completes today. Zakat is due.")
                        else "${localizedUiText("Lunar year complete. Zakat was due on")} ${hijriText(lastDue)} (${gregorianText(lastDue)}).",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${localizedUiText("Next zakat date")}: ${hijriText(next)} · $daysLeft ${localizedUiText("days left")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val lunarYearDays = 354f
            Text(
                "${localizedUiText("Zakat falls due on")} ${hijriText(next)} (${gregorianText(next)})",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            LinearProgressIndicator(
                progress = { (1f - daysLeft / lunarYearDays).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {}
            )
            Text(
                "$daysLeft ${localizedUiText("days left in your lunar year")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HawlDatePicker(initial: LocalDate?, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val todayMillis = remember { LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        yearRange = (LocalDate.now().year - 60)..LocalDate.now().year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { com.example.core.analytics.AppAnalytics.interaction("zakat_screen", "save_hawl_date", "tap");
                    state.selectedDateMillis?.let { onPick(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()) }
                },
                enabled = state.selectedDateMillis != null
            ) { LocalizedText("Save") }
        },
        dismissButton = { TextButton(onClick = com.example.core.analytics.AppAnalytics.trackAction("zakat_screen", "cancel_hawl_date", onDismiss)) { LocalizedText("Cancel") } }
    ) {
        DatePicker(
            state = state,
            title = {
                LocalizedText(
                    "When did you first own the nisab?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}

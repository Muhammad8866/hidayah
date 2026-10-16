@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.analytics.AppAnalytics
import com.example.ui.localization.LocalAppLocale
import com.example.ui.localization.LocalizedText
import com.example.ui.localization.localizedUiText
import java.text.NumberFormat
import java.math.BigDecimal
import java.math.RoundingMode
import android.content.Intent

@Composable
internal fun MirathScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var estate by rememberSaveable { mutableStateOf("") }
    var funeral by rememberSaveable { mutableStateOf("") }
    var debts by rememberSaveable { mutableStateOf("") }
    var bequest by rememberSaveable { mutableStateOf("") }
    var male by rememberSaveable { mutableStateOf(true) }
    var spouses by rememberSaveable { mutableIntStateOf(0) }
    var sons by rememberSaveable { mutableIntStateOf(0) }
    var daughters by rememberSaveable { mutableIntStateOf(0) }
    var siblings by rememberSaveable { mutableIntStateOf(0) }
    var father by rememberSaveable { mutableStateOf(false) }
    var mother by rememberSaveable { mutableStateOf(false) }
    var others by rememberSaveable { mutableStateOf(false) }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    var requested by rememberSaveable { mutableStateOf(false) }
    val input = MirathInput(estate, funeral, debts, bequest, male, spouses, sons, daughters, father, mother, siblings, others, confirmed)
    var submittedInput by remember { mutableStateOf<MirathInput?>(null) }
    val result = if (requested && submittedInput == input) calculateMirath(input) else null
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val listState = rememberLazyListState()
    LaunchedEffect(result) {
        if (result != null) listState.animateScrollToItem(5)
    }
    val locale = LocalAppLocale.current
    val money = remember(locale) { NumberFormat.getNumberInstance(locale).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 } }
    val percent = remember(locale) { NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 2 } }
    val amounts = listOf(estate, funeral, debts, bequest).map(::parseMirathAmount)
    val available = if (amounts.all { it != null }) amounts[0]!! - amounts[1]!! - amounts[2]!! else null
    val bequestLimit = available?.takeIf { it.signum() > 0 }?.divide(BigDecimal(3), 2, RoundingMode.DOWN)
    ToolScreen(DailyTool.MIRATH, AppAnalytics.trackAction("mirath_screen", "back", onBack), modifier, listState = listState) { accent ->
        item {
            HeroCard(accent) {
                LocalizedText("Sunni Hanafi • Family inheritance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                LocalizedText("1. Estate  •  2. Family  •  3. Shares", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                LocalizedText(
                    "Supports spouse, parents and direct children. Other heirs or special circumstances require scholar review. Verify the result before distributing an estate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            ToolCard {
                MirathStepTitle(1, "Estate & deductions", accent)
                MirathHint("Use the same currency for all amounts. Include only assets owned by the deceased.")
                MirathAmount("Total estate", estate, "mirath_estate") { estate = it }
                MirathAmount("Funeral expenses", funeral, "mirath_funeral") { funeral = it }
                MirathAmount("Outstanding debts", debts, "mirath_debts") { debts = it }
                MirathAmount("Wasiyyat (bequest)", bequest, "mirath_bequest") { bequest = it }
                if (bequestLimit != null || available != null) {
                    Surface(shape = MaterialTheme.shapes.medium, color = accent.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (bequestLimit != null) {
                                MirathSummaryRow("Maximum supported bequest", money.format(bequestLimit))
                                if (amounts[3]!! > bequestLimit) MirathWarning("Wasiyyat exceeds one third after expenses and debts. Scholar review is required.")
                            }
                            if (available != null) {
                                MirathSummaryRow("Distributable estate", money.format((available - amounts[3]!!).max(BigDecimal.ZERO)), emphasize = true, accent = accent)
                                if (estate.isNotBlank() && available.signum() <= 0) MirathWarning("No distributable estate remains after expenses and debts.")
                            }
                        }
                    }
                }
                MirathHint("Only a valid bequest to non-heirs, up to one third after expenses and debts, is supported. Bequests to heirs need scholar review.")
            }
        }
        item {
            ToolCard {
                MirathStepTitle(2, "Surviving heirs", accent)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MirathGenderChip("Deceased: male", male) { if (!male) { male = true; spouses = 0; confirmed = false } }
                    MirathGenderChip("Deceased: female", !male) { if (male) { male = false; spouses = 0; confirmed = false } }
                }
                MirathCount(if (male) "Wives" else "Husband", spouses, if (male) 4 else 1) { spouses = it; confirmed = false }
                MirathCount("Sons", sons) { sons = it; confirmed = false }
                MirathCount("Daughters", daughters) { daughters = it; confirmed = false }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                MirathToggle("Father alive", father) { father = it; confirmed = false }
                MirathToggle("Mother alive", mother) { mother = it; confirmed = false }
                MirathCount("Brothers & sisters (all types)", siblings) { siblings = it; confirmed = false }
                MirathHint("Siblings can reduce the mother's share even when excluded by the father. Without a father or son, sibling cases need scholar review.")
                MirathToggle("Other heirs or special circumstances", others) { others = it; confirmed = false }
                MirathHint("Select this for grandchildren, grandparents, other relatives, pregnancy, missing heirs, disputed eligibility or a bequest to an heir.")
                if (others || (siblings > 0 && !father && sons == 0)) {
                    MirathWarning("This family includes heirs outside this calculator's scope. Ask a qualified inheritance scholar for a complete calculation.")
                }
            }
        }
        // Confirmation and actions get their own block so they stay reachable after a long heirs list.
        item {
            ToolCard {
                MirathToggle("I have checked all surviving heirs and no special circumstances apply", confirmed) { confirmed = it }
                if (requested && result == null) MirathHint("Details changed. Calculate again to update your shares.")
                Button(
                    onClick = { focus.clearFocus(); submittedInput = input; requested = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("mirath_calculate"),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = MaterialTheme.colorScheme.surface)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    LocalizedText("Calculate shares")
                }
                TextButton(onClick = {
                    estate = ""; funeral = ""; debts = ""; bequest = ""; male = true
                    spouses = 0; sons = 0; daughters = 0; siblings = 0
                    father = false; mother = false; others = false; confirmed = false; requested = false
                }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    LocalizedText("Reset")
                }
            }
        }
        if (result != null) item {
            ToolCard {
                MirathStepTitle(3, "Inheritance breakdown", accent, Modifier.testTag("mirath_result"))
                if (result.error != null) {
                    LocalizedText(result.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("mirath_error"))
                } else {
                    Surface(shape = MaterialTheme.shapes.medium, color = accent.copy(alpha = 0.10f), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LocalizedText("Distributable estate", style = MaterialTheme.typography.labelLarge, color = accent, fontWeight = FontWeight.SemiBold)
                            Text(money.format(result.net), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = accent)
                            Spacer(Modifier.height(4.dp))
                            MirathSummaryRow("Total estate", money.format(amounts[0]))
                            MirathSummaryRow("Funeral expenses", money.format(amounts[1]))
                            MirathSummaryRow("Outstanding debts", money.format(amounts[2]))
                            MirathSummaryRow("Wasiyyat (bequest)", money.format(amounts[3]))
                        }
                    }
                    result.adjustment?.let { MirathHint(it) }
                    result.shares.forEach { share ->
                        val ratio = share.fraction.numerator.toDouble() / share.fraction.denominator
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${localizedUiText(share.heir)} × ${share.count}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(percent.format(ratio), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = accent)
                                }
                                LinearProgressIndicator(
                                    progress = { ratio.toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                    color = accent,
                                    trackColor = accent.copy(alpha = 0.15f),
                                    strokeCap = StrokeCap.Round,
                                    drawStopIndicator = {}
                                )
                                Text("${share.fraction} • ${money.format(share.fraction.amount(result.net))}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                if (share.count > 1) Text(
                                    "${localizedUiText("Each")}: ${share.each} • ${money.format(share.each.amount(result.net))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                MirathHint(share.rule)
                            }
                        }
                    }
                    if (siblings > 0) MirathHint("Brothers and sisters are excluded by the surviving father or son.")
                    if (result.remainder.numerator > 0) {
                        MirathWarning("Unallocated remainder — scholar review required")
                        Text("${result.remainder} • ${money.format(result.remainder.amount(result.net))}")
                    }
                    if (result.roundingDifference.signum() != 0) MirathSummaryRow("Rounding difference", money.format(result.roundingDifference))
                    MirathHint("Fractions are exact. Displayed amounts are rounded; reconcile minor currency differences before distribution.")
                    val shareTitle = localizedUiText("Share breakdown")
                    val report = buildString {
                            appendLine(localizedUiText("Sunni Hanafi • Family inheritance"))
                            appendLine("${localizedUiText("Distributable estate")}: ${money.format(result.net)}")
                            listOf("Total estate", "Funeral expenses", "Outstanding debts", "Wasiyyat (bequest)").forEachIndexed { i, label ->
                                appendLine("${localizedUiText(label)}: ${money.format(amounts[i])}")
                            }
                            result.shares.forEach { share ->
                                appendLine("${localizedUiText(share.heir)} × ${share.count}: ${share.fraction} • ${money.format(share.fraction.amount(result.net))}")
                                appendLine("${localizedUiText("Each")}: ${share.each} • ${money.format(share.each.amount(result.net))}")
                                appendLine(localizedUiText(share.rule))
                            }
                            result.adjustment?.let { appendLine(localizedUiText(it)) }
                            if (result.remainder.numerator > 0) appendLine("${localizedUiText("Unallocated remainder — scholar review required")}: ${result.remainder} • ${money.format(result.remainder.amount(result.net))}")
                            if (result.roundingDifference.signum() != 0) appendLine("${localizedUiText("Rounding difference")}: ${money.format(result.roundingDifference)}")
                            appendLine(localizedUiText("Fractions are exact. Displayed amounts are rounded; reconcile minor currency differences before distribution."))
                            appendLine(localizedUiText("Verify the result with a qualified scholar before distribution."))
                            appendLine("https://legacy.quran.com/4/11-12")
                    }
                    FilledTonalButton(onClick = {
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, report)
                        }, shareTitle))
                    }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("mirath_share")) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        LocalizedText("Share breakdown")
                    }
                }
            }
        }
        item {
            ToolCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    SectionTitle("Sources")
                }
                MirathHint("Quran: Surah An-Nisa 4:11–12. Hanafi rules for Awl, Radd and Umariyyatain are applied within the supported family scope.")
                TextButton(onClick = { openExternalUrl(context, "https://legacy.quran.com/4/11-12") }, contentPadding = PaddingValues(horizontal = 0.dp)) { LocalizedText("Read Quran 4:11–12") }
            }
        }
    }
}

/** Numbered section heading matching the Zakat calculator's steps. */
@Composable
private fun MirathStepTitle(number: Int, title: String, accent: Color, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepNumberBadge(number, accent, filled = true, size = 30.dp)
        Spacer(Modifier.width(12.dp))
        LocalizedText(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = modifier.semantics { heading() }
        )
    }
}

@Composable
private fun MirathHint(text: String) {
    LocalizedText(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun MirathWarning(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.WarningAmber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 2.dp).size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        LocalizedText(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun MirathGenderChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { LocalizedText(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null
    )
}

@Composable
private fun MirathAmount(label: String, value: String, tag: String, onChange: (String) -> Unit) {
    val error = mirathAmountError(value)
    OutlinedTextField(value, onChange, label = { LocalizedText(label) }, singleLine = true,
        isError = error != null,
        shape = RoundedCornerShape(14.dp),
        supportingText = { LocalizedText(error ?: if (tag == "mirath_estate") "Required • up to 2 decimal places" else "Optional • leave empty for zero") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth().testTag(tag))
}

@Composable
private fun MirathCount(label: String, count: Int, max: Int = 50, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LocalizedText(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        val decrease = "${localizedUiText("Decrease")}: ${localizedUiText(label)}"
        val increase = "${localizedUiText("Increase")}: ${localizedUiText(label)}"
        FilledTonalIconButton(onClick = { onChange(count - 1) }, enabled = count > 0, modifier = Modifier.semantics { contentDescription = decrease }.testTag("mirath_${label}_decrease")) {
            Icon(Icons.Default.Remove, contentDescription = null)
        }
        Text(
            NumberFormat.getIntegerInstance(LocalAppLocale.current).format(count),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 36.dp)
        )
        FilledTonalIconButton(onClick = { onChange(count + 1) }, enabled = count < max, modifier = Modifier.semantics { contentDescription = increase }.testTag("mirath_${label}_increase")) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }
}

@Composable
private fun MirathToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(value = checked, role = Role.Checkbox, onValueChange = { AppAnalytics.interaction("mirath_screen", "family_review", "toggle"); onChange(it) }), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, null)
        LocalizedText(label, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun MirathSummaryRow(label: String, value: String, emphasize: Boolean = false, accent: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        LocalizedText(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (emphasize) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.SemiBold,
            color = if (emphasize) accent else Color.Unspecified
        )
    }
}

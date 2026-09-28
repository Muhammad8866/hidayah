package com.example.ui.screens

import java.math.BigDecimal
import java.math.RoundingMode

/** Nisab weights: 52.5 tola (200 dirhams) of silver and 7.5 tola (20 mithqal) of gold. */
internal enum class ZakatNisabBasis(val grams: String) {
    SILVER("612.36"), GOLD("87.48")
}

/**
 * AUTO follows the common Hanafi ruling: if the only zakatable wealth is gold, the gold nisab
 * applies; as soon as gold is mixed with silver, cash, trade stock or anything else, the values
 * are combined and measured against the silver nisab.
 */
internal enum class ZakatNisabMode { AUTO, SILVER, GOLD }

internal enum class MetalUnit(val grams: String) {
    TOLA("11.664"), GRAM("1")
}

/** Gold purity: the live rate is for pure (24K) gold, jewellery is usually 22K or 21K. */
internal enum class GoldKarat(val karat: Int) {
    K24(24), K22(22), K21(21), K18(18);

    /** The price of this karat from the pure-gold [rate]. */
    fun priceOf(rate: BigDecimal): BigDecimal = rate.multiply(BigDecimal(karat)).divide(BigDecimal(24), 10, RoundingMode.HALF_UP)
}

internal enum class ZakatStatus {
    EMPTY, NEED_GOLD_RATE, NEED_SILVER_RATE, BELOW_NISAB, AWAITING_HAWL, DUE
}

internal data class ZakatInput(
    val cash: String = "",
    val goldWeight: String = "",
    val silverWeight: String = "",
    val business: String = "",
    val receivables: String = "",
    val investments: String = "",
    val otherAssets: String = "",
    val debts: String = "",
    val bills: String = "",
    val installments: String = "",
    /** Rates are per [unit] of pure metal: 24K gold and fine silver. */
    val goldRate: String = "",
    val silverRate: String = "",
    val goldKarat: GoldKarat = GoldKarat.K24,
    val unit: MetalUnit = MetalUnit.TOLA,
    val nisabMode: ZakatNisabMode = ZakatNisabMode.AUTO,
    val lunarYearPassed: Boolean = false
)

internal data class ZakatResult(
    val goldValue: BigDecimal?,
    val silverValue: BigDecimal?,
    val assets: BigDecimal,
    val liabilities: BigDecimal,
    val net: BigDecimal,
    val basis: ZakatNisabBasis,
    val nisab: BigDecimal?,
    val estimate: BigDecimal,
    val due: BigDecimal?,
    val status: ZakatStatus
) {
    /** How far net wealth has reached the nisab, 0..1; null until the nisab is known. */
    val nisabProgress: Float?
        get() = nisab?.takeIf { it > BigDecimal.ZERO }
            ?.let { net.divide(it, 4, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f) }
}

internal fun parseAmount(value: String): BigDecimal =
    value.toBigDecimalOrNull()?.takeIf { it >= BigDecimal.ZERO } ?: BigDecimal.ZERO

private fun parseRate(value: String): BigDecimal? =
    value.toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }

/** Nisab weight expressed in [unit], e.g. 52.5 tola or 612.36 g of silver. */
internal fun nisabWeight(basis: ZakatNisabBasis, unit: MetalUnit): BigDecimal =
    BigDecimal(basis.grams).divide(BigDecimal(unit.grams), 2, RoundingMode.HALF_UP).stripTrailingZeros()

internal fun calculateZakat(input: ZakatInput): ZakatResult {
    val goldRate = parseRate(input.goldRate)
    val silverRate = parseRate(input.silverRate)
    val goldWeight = parseAmount(input.goldWeight)
    val silverWeight = parseAmount(input.silverWeight)
    val karatRate = goldRate?.let(input.goldKarat::priceOf)
    val goldValue = if (goldWeight.signum() == 0) BigDecimal.ZERO
        else karatRate?.multiply(goldWeight)?.setScale(2, RoundingMode.HALF_UP)
    val silverValue = if (silverWeight.signum() == 0) BigDecimal.ZERO else silverRate?.multiply(silverWeight)

    val otherAssets = listOf(input.cash, input.business, input.receivables, input.investments, input.otherAssets)
        .fold(BigDecimal.ZERO) { sum, value -> sum + parseAmount(value) }
    val assets = otherAssets + (goldValue ?: BigDecimal.ZERO) + (silverValue ?: BigDecimal.ZERO)
    val liabilities = listOf(input.debts, input.bills, input.installments)
        .fold(BigDecimal.ZERO) { sum, value -> sum + parseAmount(value) }
    val net = (assets - liabilities).max(BigDecimal.ZERO)

    val onlyGold = goldWeight.signum() > 0 && silverWeight.signum() == 0 && otherAssets.signum() == 0
    val basis = when (input.nisabMode) {
        ZakatNisabMode.SILVER -> ZakatNisabBasis.SILVER
        ZakatNisabMode.GOLD -> ZakatNisabBasis.GOLD
        ZakatNisabMode.AUTO -> if (onlyGold) ZakatNisabBasis.GOLD else ZakatNisabBasis.SILVER
    }
    // Hanafi: jewellery that is mostly gold counts as gold by its full weight, so gold alone reaches
    // the nisab at 7.5 tola of its own karat; mixed wealth is measured against pure gold.
    val basisRate = if (basis == ZakatNisabBasis.GOLD) (if (onlyGold) karatRate else goldRate) else silverRate
    val nisab = basisRate?.multiply(BigDecimal(basis.grams))
        ?.divide(BigDecimal(input.unit.grams), 2, RoundingMode.HALF_UP)

    val estimate = net.multiply(BigDecimal("0.025")).setScale(2, RoundingMode.HALF_UP)
    val hasInput = goldWeight.signum() > 0 || silverWeight.signum() > 0 || otherAssets.signum() > 0
    val status = when {
        !hasInput -> ZakatStatus.EMPTY
        goldValue == null -> ZakatStatus.NEED_GOLD_RATE
        silverValue == null -> ZakatStatus.NEED_SILVER_RATE
        nisab == null -> if (basis == ZakatNisabBasis.GOLD) ZakatStatus.NEED_GOLD_RATE else ZakatStatus.NEED_SILVER_RATE
        net < nisab -> ZakatStatus.BELOW_NISAB
        !input.lunarYearPassed -> ZakatStatus.AWAITING_HAWL
        else -> ZakatStatus.DUE
    }
    return ZakatResult(
        goldValue = goldValue,
        silverValue = silverValue,
        assets = assets,
        liabilities = liabilities,
        net = net,
        basis = basis,
        nisab = nisab,
        estimate = estimate,
        due = estimate.takeIf { status == ZakatStatus.DUE },
        status = status
    )
}

/** A Hijri date as (year, month, day), compared in calendar order. */
internal data class HijriDay(val year: Int, val month: Int, val day: Int) : Comparable<HijriDay> {
    override fun compareTo(other: HijriDay) = compareValuesBy(this, other, { it.year }, { it.month }, { it.day })

    override fun toString() = "$year-$month-$day"

    companion object {
        fun parse(value: String?): HijriDay? = value?.split('-')?.mapNotNull { it.toIntOrNull() }
            ?.takeIf { it.size == 3 && it[1] in 1..12 && it[2] in 1..30 }?.let { HijriDay(it[0], it[1], it[2]) }
    }
}

/**
 * Where the zakat year stands: a lunar year after the date the nisab was first owned, zakat falls
 * due, and again on the same Hijri date every year after that.
 */
internal data class HawlState(val passed: Boolean, val lastDue: HijriDay?, val nextDue: HijriDay)

internal fun hawlState(start: HijriDay, today: HijriDay): HawlState {
    val firstDue = start.copy(year = start.year + 1)
    if (today < firstDue) return HawlState(passed = false, lastDue = null, nextDue = firstDue)
    val anniversaryThisYear = start.copy(year = today.year)
    val lastDue = if (anniversaryThisYear <= today) anniversaryThisYear else start.copy(year = today.year - 1)
    return HawlState(passed = true, lastDue = lastDue, nextDue = lastDue.copy(year = lastDue.year + 1))
}

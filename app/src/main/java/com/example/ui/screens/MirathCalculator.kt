package com.example.ui.screens

import java.math.BigDecimal
import java.math.RoundingMode

internal data class MirathInput(
    val estate: String = "", val funeral: String = "", val debts: String = "", val bequest: String = "",
    val male: Boolean = true, val spouses: Int = 0, val sons: Int = 0, val daughters: Int = 0,
    val father: Boolean = false, val mother: Boolean = false, val siblings: Int = 0,
    val otherHeirs: Boolean = false, val confirmed: Boolean = false
)

/** Accept plain decimal amounts and local digit scripts, without ambiguous separators/exponents. */
internal fun parseMirathAmount(raw: String): BigDecimal? {
    if (raw.isBlank()) return BigDecimal.ZERO
    val normalized = raw.trim().map { c ->
        if (c.isDigit()) Character.digit(c, 10).digitToChar() else c
    }.joinToString("")
    if (!Regex("[0-9]+(?:\\.[0-9]{1,2})?").matches(normalized)) return null
    return normalized.toBigDecimalOrNull()?.takeIf { it.precision() <= 15 }
}

internal fun mirathAmountError(raw: String): String? =
    if (parseMirathAmount(raw) == null) "Enter valid non-negative amounts with up to 2 decimal places (no commas)." else null

/** Exact fractions keep awl, radd and per-person shares independent of monetary rounding. */
internal data class MirathFraction private constructor(val numerator: Long, val denominator: Long) {
    operator fun plus(b: MirathFraction) = of(numerator * b.denominator + b.numerator * denominator, denominator * b.denominator)
    operator fun minus(b: MirathFraction) = of(numerator * b.denominator - b.numerator * denominator, denominator * b.denominator)
    operator fun times(b: MirathFraction) = of(numerator * b.numerator, denominator * b.denominator)
    operator fun div(b: MirathFraction) = of(numerator * b.denominator, denominator * b.numerator)
    fun amount(estate: BigDecimal): BigDecimal = estate.multiply(BigDecimal(numerator)).divide(BigDecimal(denominator), 2, RoundingMode.HALF_UP)
    override fun toString() = "$numerator/$denominator"
    companion object {
        fun of(n: Long, d: Long = 1): MirathFraction {
            require(d > 0)
            fun gcd(a: Long, b: Long): Long = if (b == 0L) kotlin.math.abs(a) else gcd(b, a % b)
            val g = gcd(n, d)
            return MirathFraction(n / g, d / g)
        }
    }
}

internal data class MirathShare(val heir: String, val count: Int, val fraction: MirathFraction, val rule: String) {
    val each get() = fraction / MirathFraction.of(count.toLong())
}
internal data class MirathResult(
    val net: BigDecimal = BigDecimal.ZERO, val shares: List<MirathShare> = emptyList(),
    val error: String? = null, val adjustment: String? = null, val remainder: MirathFraction = MirathFraction.of(0)
) {
    /** Signed difference after rounding each recipient, including any unallocated balance. */
    val roundingDifference: BigDecimal get() = net - shares.fold(remainder.amount(net)) { total, share ->
        total + share.each.amount(net).multiply(BigDecimal(share.count))
    }
}

internal fun calculateMirath(input: MirathInput): MirathResult {
    fun fail(message: String) = MirathResult(error = message)
    val values = listOf(input.estate, input.funeral, input.debts, input.bequest).map(::parseMirathAmount)
    if (values.any { it == null || it.signum() < 0 || it.scale() > 2 || it.precision() > 15 })
        return fail("Enter valid non-negative amounts with up to 2 decimal places (no commas).")
    val (estate, funeral, debts, bequest) = values.map { it!! }
    if (estate.signum() <= 0) return fail("Enter the total estate to calculate shares.")
    val available = estate - funeral - debts
    if (available.signum() <= 0) return fail("No distributable estate remains after expenses and debts.")
    if (bequest.multiply(BigDecimal(3)) > available) return fail("Wasiyyat exceeds one third after expenses and debts. Scholar review is required.")
    if (input.spouses !in 0..(if (input.male) 4 else 1) || listOf(input.sons, input.daughters, input.siblings).any { it !in 0..50 })
        return fail("Check the number of heirs.")
    if (input.otherHeirs || (input.siblings > 0 && !input.father && input.sons == 0))
        return fail("This family includes heirs outside this calculator's scope. Ask a qualified inheritance scholar for a complete calculation.")
    if (!input.confirmed) return fail("Confirm that all surviving heirs and special circumstances have been checked.")
    val net = available - bequest
    val zero = MirathFraction.of(0)
    val one = MirathFraction.of(1)
    fun f(n: Long, d: Long) = MirathFraction.of(n, d)
    val children = input.sons + input.daughters > 0
    val rows = mutableListOf<MirathShare>()
    fun add(heir: String, count: Int, share: MirathFraction, rule: String) { rows += MirathShare(heir, count, share, rule) }
    val spouse = if (input.spouses == 0) zero else if (input.male) f(1, if (children) 8 else 4) else f(1, if (children) 4 else 2)
    if (input.spouses > 0) add(if (input.male) "Wife" else "Husband", input.spouses, spouse, "Spouse's fixed share; multiple wives share equally.")
    if (input.mother) {
        val reduced = children || input.siblings >= 2
        val umari = !reduced && input.father && input.spouses > 0
        add("Mother", 1, if (reduced) f(1, 6) else if (umari) (one - spouse) * f(1, 3) else f(1, 3),
            if (umari) "One third of the remainder after the spouse (Umariyyatain)." else "One sixth with children or two or more siblings; otherwise one third.")
    }
    if (input.father && children) add("Father", 1, f(1, 6), "One sixth with children; also takes residue when there is no son.")
    if (input.sons == 0 && input.daughters > 0) add("Daughter", input.daughters, if (input.daughters == 1) f(1, 2) else f(2, 3), "One daughter: half. Two or more: two thirds shared equally.")
    var total = rows.fold(zero) { a, r -> a + r.fraction }
    var adjustment: String? = null
    if (total.numerator > total.denominator) {
        rows.indices.forEach { i -> rows[i] = rows[i].copy(fraction = rows[i].fraction / total) }
        adjustment = "Awl: fixed shares exceed the estate and are reduced proportionally."
    } else {
        val residue = one - total
        if (input.sons > 0) {
            val unit = residue / MirathFraction.of((2 * input.sons + input.daughters).toLong())
            add("Son", input.sons, unit * MirathFraction.of(2L * input.sons), "Residue: each son receives twice each daughter's share.")
            if (input.daughters > 0) add("Daughter", input.daughters, unit * MirathFraction.of(input.daughters.toLong()), "Residue: each daughter receives one unit; each son two.")
        } else if (input.father) {
            val i = rows.indexOfFirst { it.heir == "Father" }
            if (i >= 0) rows[i] = rows[i].copy(fraction = rows[i].fraction + residue)
            else add("Father", 1, residue, "Father receives the residue after fixed shares.")
        } else if (residue.numerator > 0) {
            val eligible = total - spouse
            if (eligible.numerator > 0) {
                rows.indices.filter { rows[it].heir != "Wife" && rows[it].heir != "Husband" }.forEach { i ->
                    rows[i] = rows[i].copy(fraction = rows[i].fraction + residue * rows[i].fraction / eligible)
                }
                adjustment = "Radd (Hanafi): surplus returns proportionally to blood heirs; spouse is excluded."
            }
        }
    }
    total = rows.fold(zero) { a, r -> a + r.fraction }
    return MirathResult(net, rows, adjustment = adjustment, remainder = one - total)
}

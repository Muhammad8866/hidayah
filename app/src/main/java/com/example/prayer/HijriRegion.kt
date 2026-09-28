package com.example.prayer

/**
 * The moon-sighting convention that decides when each Hijri month starts for the user's location.
 *
 * Saudi Arabia, the Gulf and most other places follow the Umm al-Qura calendar. India, Pakistan,
 * Bangladesh, Nepal, Sri Lanka and Morocco begin a month only after the crescent has actually been
 * sighted in the country, often a day after Saudi Arabia; for them the start of each month is
 * predicted from crescent visibility (Odeh criterion) at the places where their committees receive
 * sightings. Indonesia, Malaysia, Brunei and Singapore follow the Neo-MABIMS criterion.
 */
class HijriRegion private constructor(
    val id: String,
    val label: String,
    /** Places where the national committee accepts sightings; empty for Umm al-Qura. */
    internal val sightingPlaces: List<Pair<Double, Double>>,
    /** Minimum Odeh V at any sighting place for the crescent to count as sighted. */
    internal val visibilityThreshold: Double,
    /** Uses the Neo-MABIMS altitude/elongation criterion instead of Odeh's V. */
    internal val usesMabims: Boolean = false,
    /** Neo-MABIMS altitude measured to the Moon's upper limb (Indonesia) rather than its centre (Malaysia). */
    private val mabimsUpperLimb: Boolean = false
) {
    val followsLocalSighting: Boolean get() = sightingPlaces.isNotEmpty()

    val methodDescription: String
        get() = when {
            usesMabims -> "Each month starts after the evening the Moon is at least 3° high with 6.4° elongation " +
                "at sunset in $id (Neo-MABIMS criterion), matching the official calendar."
            followsLocalSighting -> "Each month starts after the evening the new crescent is predicted visible in $id (Odeh criterion), " +
                "matching local moon-sighting announcements."
            else -> "Umm al-Qura calendar of Saudi Arabia."
        }

    internal fun isCrescentSighted(eveningJdn: Long): Boolean = sightingPlaces.any { (latitude, longitude) ->
        if (usesMabims) {
            CrescentVisibility.meetsMabims(latitude, longitude, eveningJdn, mabimsUpperLimb)
        } else {
            (CrescentVisibility.odehV(latitude, longitude, eveningJdn) ?: Double.NEGATIVE_INFINITY) >= visibilityThreshold
        }
    }

    companion object {
        val UMM_AL_QURA = HijriRegion("Saudi Arabia", "Umm al-Qura calendar", emptyList(), 0.0)

        // Naked-eye threshold; checked against every Indian announcement found for 1444–1448 AH.
        val INDIA = HijriRegion(
            "India", "India (local moon sighting)",
            listOf(23.023 to 72.571, 19.076 to 72.878, 28.614 to 77.209, 26.847 to 80.946, 17.385 to 78.487),
            CrescentVisibility.NAKED_EYE_V
        )

        // Pakistan's Central Ruet-e-Hilal Committee accepts sightings from the Balochistan coast
        // with optical aid; 5.15 separates every sighted and unsighted evening from 1444–1448 AH.
        val PAKISTAN = HijriRegion(
            "Pakistan", "Pakistan (local moon sighting)",
            listOf(25.05 to 61.74, 24.86 to 67.01, 30.18 to 66.99, 31.55 to 74.34, 34.01 to 71.58),
            5.15
        )

        val BANGLADESH = HijriRegion(
            "Bangladesh", "Bangladesh (local moon sighting)",
            listOf(23.81 to 90.41, 22.36 to 91.78, 24.37 to 88.60, 22.85 to 89.54),
            CrescentVisibility.NAKED_EYE_V
        )

        val NEPAL = HijriRegion(
            "Nepal", "Nepal (local moon sighting)",
            listOf(27.72 to 85.32, 28.05 to 81.62, 27.01 to 84.88),
            CrescentVisibility.NAKED_EYE_V
        )

        val SRI_LANKA = HijriRegion(
            "Sri Lanka", "Sri Lanka (local moon sighting)",
            listOf(6.93 to 79.86, 9.66 to 80.02, 7.29 to 80.63),
            CrescentVisibility.NAKED_EYE_V
        )

        val MOROCCO = HijriRegion(
            "Morocco", "Morocco (local moon sighting)",
            listOf(30.43 to -9.60, 31.63 to -8.01, 33.57 to -7.59, 34.02 to -6.84),
            CrescentVisibility.NAKED_EYE_V
        )

        // Kemenag applies the criterion anywhere in Indonesia, so sample the whole archipelago:
        // Aceh to Papua, with the west coasts of Sumatra and Java where the Moon sets last.
        val INDONESIA = HijriRegion(
            "Indonesia", "Indonesia (Neo-MABIMS)",
            listOf(
                5.89 to 95.32, 5.55 to 95.32, 4.14 to 96.13, 2.0 to 98.9, -0.95 to 100.35, -3.80 to 102.27,
                -5.45 to 105.26, -6.99 to 106.55, -6.20 to 106.85, -7.80 to 110.37, -7.25 to 112.75,
                -8.65 to 115.22, -10.17 to 123.60, -5.14 to 119.43, 1.49 to 124.84, -3.70 to 128.18, -8.50 to 140.40
            ),
            0.0, usesMabims = true, mabimsUpperLimb = true
        )

        // Also followed by Brunei and Singapore.
        val MALAYSIA = HijriRegion(
            "Malaysia", "Malaysia, Brunei, Singapore (Neo-MABIMS)",
            listOf(6.33 to 99.85, 3.14 to 101.69, 1.35 to 103.82, 5.98 to 116.07, 4.94 to 114.95),
            0.0, usesMabims = true
        )

        val ALL = listOf(UMM_AL_QURA, INDIA, PAKISTAN, BANGLADESH, NEPAL, SRI_LANKA, MOROCCO, INDONESIA, MALAYSIA)

        /**
         * Calendar chosen by the user in Settings, overriding the one implied by the location's time
         * zone (e.g. someone abroad who follows their home country's committee). Null means automatic.
         */
        @Volatile
        var preferred: HijriRegion? = null

        fun byId(id: String?): HijriRegion? = ALL.firstOrNull { it.id == id }

        fun forTimeZone(timeZoneId: String): HijriRegion = preferred ?: automaticFor(timeZoneId)

        fun automaticFor(timeZoneId: String): HijriRegion = when (timeZoneId) {
            "Asia/Kolkata", "Asia/Calcutta" -> INDIA
            "Asia/Karachi" -> PAKISTAN
            "Asia/Dhaka", "Asia/Dacca" -> BANGLADESH
            "Asia/Kathmandu", "Asia/Katmandu" -> NEPAL
            "Asia/Colombo" -> SRI_LANKA
            "Africa/Casablanca", "Africa/El_Aaiun" -> MOROCCO
            "Asia/Jakarta", "Asia/Pontianak", "Asia/Makassar", "Asia/Ujung_Pandang", "Asia/Jayapura" -> INDONESIA
            "Asia/Kuala_Lumpur", "Asia/Kuching", "Asia/Brunei", "Asia/Singapore", "Singapore" -> MALAYSIA
            else -> UMM_AL_QURA
        }
    }
}

/**
 * First day (Julian Day Number) of each Hijri month in a [HijriRegion].
 *
 * Local-sighting months are built the way a moon-sighting committee works: on the evening of the
 * 29th the crescent is either seen, and the next month starts the following day, or the month is
 * completed to 30 days. The chain starts at 1 Muharram 1446 (8 July 2024 in both India and Pakistan).
 *
 * [confirmedStarts] (month index -> first day) are starts the user entered from a local announcement
 * that differed from the prediction, e.g. when clouds hid a crescent that was astronomically visible.
 * Later months continue from a confirmed start by the same 29th-evening rule, so a correction lasts
 * exactly as long as the committee's calendar stays shifted.
 */
internal class HijriMonthStarts(val region: HijriRegion, val confirmedStarts: Map<Int, Long>) {
    /** Month starts from [EPOCH_INDEX] onwards. */
    private val chain = ArrayList<Long>()
    private val beforeEpoch = HashMap<Int, Long>()

    /** Start of the month at [index]; [UmmAlQuraCalendar.MONTH_COUNT] gives the day after the last month. */
    @Synchronized
    fun start(index: Int): Long {
        confirmedStarts[index]?.let { return it }
        if (!region.followsLocalSighting) return UmmAlQuraCalendar.monthStart(index)
        if (index < EPOCH_INDEX) {
            // Rarely needed (dates before mid-2024): start two months earlier and follow the 29th-evening rule.
            return beforeEpoch.getOrPut(index) {
                val anchor = (index - 2).coerceAtLeast(0)
                var start = confirmedStarts[anchor] ?: firstVisibleStart(anchor)
                for (i in anchor until index) start = confirmedStarts[i + 1] ?: nextStart(i, start)
                start
            }
        }
        if (chain.isEmpty()) chain.add(firstVisibleStart(EPOCH_INDEX))
        while (EPOCH_INDEX + chain.size <= index) {
            val previousIndex = EPOCH_INDEX + chain.size - 1
            chain.add(confirmedStarts[previousIndex + 1] ?: nextStart(previousIndex, start(previousIndex)))
        }
        return chain[index - EPOCH_INDEX]
    }

    /** Next month's start after the month at [index] that began on [start]. */
    private fun nextStart(index: Int, start: Long): Long {
        val next = if (isCrescentSighted(region, start + 28)) start + 29 else start + 30
        // Local months never run more than a day ahead of, or three days behind, Umm al-Qura.
        val ummAlQura = UmmAlQuraCalendar.monthStart(index + 1)
        return if (next in (ummAlQura - 1)..(ummAlQura + 3) || index + 1 >= UmmAlQuraCalendar.MONTH_COUNT) {
            next
        } else {
            firstVisibleStart(index + 1)
        }
    }

    /** Day after the first evening, around the Umm al-Qura start, on which the crescent is sighted. */
    private fun firstVisibleStart(index: Int): Long {
        val ummAlQura = UmmAlQuraCalendar.monthStart(index)
        for (evening in (ummAlQura - 2)..(ummAlQura + 2)) {
            if (isCrescentSighted(region, evening)) return evening + 1
        }
        return ummAlQura + 3
    }

    companion object {
        private val EPOCH_INDEX = UmmAlQuraCalendar.monthIndex(1446, 1)!!

        private val active = HashMap<String, HijriMonthStarts>()

        /** Crescent sightings per region id and evening, shared by every calendar of that region. */
        private val sightings = HashMap<String, HashMap<Long, Boolean>>()

        /** The calendar in use for [region], including the user's confirmed month starts. */
        @Synchronized
        fun of(region: HijriRegion): HijriMonthStarts = active.getOrPut(region.id) { HijriMonthStarts(region, emptyMap()) }

        @Synchronized
        fun confirm(region: HijriRegion, starts: Map<Int, Long>) {
            active[region.id] = HijriMonthStarts(region, starts.toMap())
        }

        @Synchronized
        private fun isCrescentSighted(region: HijriRegion, eveningJdn: Long): Boolean =
            sightings.getOrPut(region.id) { HashMap() }.getOrPut(eveningJdn) {
                region.isCrescentSighted(eveningJdn)
            }
    }
}

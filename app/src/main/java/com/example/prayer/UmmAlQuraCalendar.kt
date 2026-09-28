package com.example.prayer

/**
 * Official Umm al-Qura calendar of Saudi Arabia for 1300–1600 AH (1882–2174 CE).
 *
 * Month lengths are stored as three hex digits per Hijri year: bit (month - 1) is set when that
 * month has 30 days. The table was generated from java.time.chrono.HijrahChronology and checked
 * month by month against ICU's islamic-umalqura calendar and the AlAdhan API.
 * Dates are exchanged as Julian Day Numbers (JDN) so no time zone is involved.
 */
internal object UmmAlQuraCalendar {
    const val FIRST_YEAR = 1300
    const val LAST_YEAR = 1600
    const val MONTH_COUNT = (LAST_YEAR - FIRST_YEAR + 1) * 12

    /** 1 Muharram 1300 AH = 12 November 1882. */
    private const val FIRST_MONTH_START_JDN = 2408762L

    private const val MONTH_LENGTHS =
        "5552ab9372b657636cb55aaa95649e95d2ba5b53aab4ba9652e2ad56db5a" +
        "752f25e8ad16a56ab56b4da9b92b2564ba9b35a6d95d4da5d4aa95536975" +
        "2f46e96d46a953525d4bd9ba3b4b69b2aa554ada5d2da6d9eaae94d2ac56" +
        "4aea6d56ad55d4aa9352ba5b53a6b5ea9d52d29a554ad56daea6e4ed1da2" +
        "aaa95a2da5b9bb27646c95552ab4dbaba5b4da9d52aa592d26d8ed2daad5" +
        "aa5a4b4979372b6975d69d52c9592b25b4db9d55d2da5d4aa9554daad3aa" +
        "bd2bc4b89a9552d5adb6a6d4dc9d92aa69562ae56d36ab55aaa94d49d95d" +
        "2ba5b55aad55a9a92e26e55dada6d46a5b27a4d4ad56db5a754f49e92d26" +
        "a563566b5baab92b2568ba9b55aada5b4da9b52a9a536276575af26d46a9" +
        "5552ad4bd9ba574b69b52a9552da5d4daad96b2e95e2ac9692eaad56ad65" +
        "d4ad1562bc5b53a6b5db2d64d29a554ad96daea6e8ed1da4d4aa6a2da5b9" +
        "b72b686d16554ab95b2ba5b5da9d52ca694e46e95d4daad5aaaa4d49b937" +
        "4b6975d6ad52aa594b2ab55bad95d2dc5d92b25555ab55b4ba97a2745593" +
        "aab4d69d65d2ba5b4aa954ad15d2dd9da5b45a952d25b8b717656db6aaca" +
        "a9652b15b2bb5b6daab94d46a8d52da9d55a755749f13e4aa965566b5baa" +
        "b94"

    /** JDN of the first day of every month, plus one entry for the day after the table ends. */
    private val monthStarts: LongArray by lazy {
        val starts = LongArray(MONTH_COUNT + 1)
        starts[0] = FIRST_MONTH_START_JDN
        for (yearIndex in 0 until MONTH_COUNT / 12) {
            val thirtyDayMonths = MONTH_LENGTHS.substring(yearIndex * 3, yearIndex * 3 + 3).toInt(16)
            for (month in 0 until 12) {
                val index = yearIndex * 12 + month
                starts[index + 1] = starts[index] + if (thirtyDayMonths and (1 shl month) != 0) 30 else 29
            }
        }
        starts
    }

    /** Months are numbered consecutively from Muharram 1300 so neighbouring months differ by one. */
    fun monthIndex(year: Int, month: Int): Int? =
        if (year in FIRST_YEAR..LAST_YEAR && month in 1..12) (year - FIRST_YEAR) * 12 + month - 1 else null

    fun yearOf(index: Int): Int = FIRST_YEAR + index / 12

    fun monthOf(index: Int): Int = index % 12 + 1

    /** First day of the month at [index]; [MONTH_COUNT] gives the day after the table ends. */
    fun monthStart(index: Int): Long = monthStarts[index]

    /** Index of the month containing [jdn], or null outside 1300–1600 AH. */
    fun monthIndexOf(jdn: Long): Int? {
        if (jdn < monthStarts[0] || jdn >= monthStarts[MONTH_COUNT]) return null
        val found = monthStarts.binarySearch(jdn, 0, MONTH_COUNT)
        return if (found >= 0) found else -found - 2
    }
}

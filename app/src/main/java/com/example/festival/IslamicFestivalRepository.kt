package com.example.festival

import com.example.prayer.HijriCalendarHelper
import com.example.prayer.HijriRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone

/** A fixed Hijri observance. [isNight] observances begin at Maghrib of the previous Gregorian evening. */
data class FestivalDefinition(
    val id: String,
    val name: String,
    val localName: String,
    val arabicName: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val isNight: Boolean,
    val description: String
)

data class IslamicFestival(
    val definition: FestivalDefinition,
    val hijriYear: Int,
    val gregorianYear: Int,
    val gregorianMonth: Int, // 1-12
    val gregorianDay: Int,
    val daysRemaining: Int
)

data class FestivalSchedule(
    val festivals: List<IslamicFestival>,
    val gregorianYear: Int,
    val regionLabel: String
)

/**
 * Builds the full running Gregorian year's calendar of Islamic occasions.
 *
 * Dates come from the same Hijri calendar as the home screen ([HijriCalendarHelper]): the
 * moon-sighting convention of the user's region plus any month starts the user confirmed, so they
 * recalculate automatically every year and on every location change, even offline.
 */
object IslamicFestivalRepository {

    val FESTIVALS = listOf(
        FestivalDefinition("new_year", "Islamic New Year", "Hijri Naya Saal", "رأس السنة الهجرية", 1, 1, false,
            "First day of Muharram and the start of the new Hijri year."),
        FestivalDefinition("ashura", "Day of Ashura", "Youm-e-Ashura", "عاشوراء", 1, 10, false,
            "10th Muharram. Sunnah fast (with 9th or 11th); remembrance of Imam Husain (RA) and Karbala."),
        FestivalDefinition("mawlid", "Mawlid an-Nabi", "Eid Milad-un-Nabi ﷺ", "المولد النبوي", 3, 12, false,
            "12th Rabi' al-Awwal, commemoration of the birth of the Prophet Muhammad ﷺ."),
        FestivalDefinition("gyarhvi", "Gyarhvi Sharif", "Giyarhvi Sharif", "الحادي عشر", 4, 11, false,
            "11th Rabi' al-Thani, remembrance of Shaykh Abdul Qadir Jilani (RA)."),
        FestivalDefinition("rajab_start", "Start of Rajab", "Rajab ka Aaghaz", "بداية رجب", 7, 1, false,
            "Beginning of Rajab, one of the four sacred months."),
        FestivalDefinition("miraj", "Isra and Mi'raj", "Shab-e-Meraj", "الإسراء والمعراج", 7, 27, true,
            "Night of the Prophet's ﷺ night journey and ascension (27th Rajab)."),
        FestivalDefinition("baraat", "Mid-Sha'ban", "Shab-e-Baraat", "ليلة النصف من شعبان", 8, 15, true,
            "Night of 15th Sha'ban, a night of forgiveness and worship."),
        FestivalDefinition("ramadan", "Start of Ramadan", "Ramzan ka Pehla Roza", "رمضان", 9, 1, false,
            "First fast of Ramadan. Taraweeh begins the night before."),
        FestivalDefinition("qadr", "Laylat al-Qadr", "Shab-e-Qadr", "ليلة القدر", 9, 27, true,
            "Commonly observed on the 27th night; seek it in the odd nights of the last ten."),
        FestivalDefinition("eid_fitr", "Eid al-Fitr", "Eid-ul-Fitr", "عيد الفطر", 10, 1, false,
            "1st Shawwal. Pay Sadaqat al-Fitr before the Eid prayer."),
        FestivalDefinition("arafah", "Day of Arafah", "Youm-e-Arafah", "يوم عرفة", 12, 9, false,
            "9th Dhu al-Hijjah. Fasting expiates sins of two years (Sahih Muslim)."),
        FestivalDefinition("eid_adha", "Eid al-Adha", "Eid-ul-Adha (Bakra Eid)", "عيد الأضحى", 12, 10, false,
            "10th Dhu al-Hijjah. Qurbani from the 10th to the 12th.")
    )

    /** How far ahead the schedule looks, so the next occasions are always listed, even in December. */
    const val UPCOMING_WINDOW_DAYS = 370

    suspend fun load(timeZoneId: String): FestivalSchedule =
        withContext(Dispatchers.Default) {
            build(timeZoneId, Calendar.getInstance(TimeZone.getTimeZone(timeZoneId)))
        }

    /**
     * Every occasion from 1 January of [today]'s Gregorian year up to [UPCOMING_WINDOW_DAYS] ahead,
     * soonest first. Earlier occasions of this year come back with negative [IslamicFestival.daysRemaining];
     * an occasion can appear twice (once already passed this year, once upcoming next year).
     */
    internal fun build(timeZoneId: String, today: Calendar): FestivalSchedule {
        val region = HijriRegion.forTimeZone(timeZoneId)
        val gYear = today.get(Calendar.YEAR)
        val todayJdn = HijriCalendarHelper.julianDayNumber(today)
        val januaryFirst = (today.clone() as Calendar).apply { set(gYear, Calendar.JANUARY, 1) }
        val windowStart = HijriCalendarHelper.julianDayNumber(januaryFirst)
        val windowEnd = todayJdn + UPCOMING_WINDOW_DAYS

        // The window spans up to three Hijri years; check every one of them.
        val startHijriYear = HijriCalendarHelper.getHijriDate(januaryFirst, region = region).year
        val festivals = (startHijriYear..startHijriYear + 2).flatMap { hy ->
            FESTIVALS.mapNotNull { def ->
                val jdn = HijriCalendarHelper.toJulianDay(hy, def.hijriMonth, def.hijriDay, region)
                if (jdn < windowStart || jdn > windowEnd) return@mapNotNull null
                val (year, month, day) = HijriCalendarHelper.julianDayToGregorian(jdn)
                IslamicFestival(def, hy, year, month, day, (jdn - todayJdn).toInt())
            }
        }.sortedWith(compareBy({ it.daysRemaining }, { it.definition.id }))

        return FestivalSchedule(festivals = festivals, gregorianYear = gYear, regionLabel = region.label)
    }
}

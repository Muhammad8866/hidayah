package com.example.hadith

/**
 * Sirat-ul-Jinan, the Quran with its tafseer, as the publisher's own PDF volumes (jild). Every
 * volume is a row of the dawateislami.net library list with the very identity
 * [WorldLibrary.diBook] gives it (id "di-n", collection "wl-di:n", the same download address), so
 * a volume downloaded or read here is the same book in the Islamic Books library. The files are
 * read exactly as published; nothing is edited, re-encoded or re-hosted. SiratUlJinanPdfsTest
 * checks every row against the list shipped with the app.
 */
object SiratUlJinanPdfs {
    private const val DOWNLOAD = "https://data2.dawateislami.net/Data/Books/Download"

    /**
     * One published volume: [jild] of the work, explaining paras [firstPara]..[lastPara].
     * [sizeBytes] is the published file's size, shown before a download; [book] is the library's book.
     */
    class Volume(val book: HadithBook, val jild: Int, val firstPara: Int, val lastPara: Int, val sizeBytes: Long)

    private fun volume(
        diId: Long, libraryCode: String, fileId: Int, year: Int, pages: Int, slug: String,
        title: String, nativeName: String, jild: Int, sizeBytes: Long
    ): Volume {
        val language = DawateIslamiIndex.appLanguage(libraryCode)
        val book = HadithBook(
            id = "di-$diId",
            collectionId = "wl-di:$diId",
            collectionName = title,
            romanTitle = title,
            // As the library list gives it: the script title only when it differs from the title.
            nativeTitle = nativeName.takeIf { !it.equals(title, ignoreCase = true) },
            languageCode = language,
            languageName = WorldLibrary.languageName(language).orEmpty(),
            pageCount = pages,
            publicationYear = year,
            fileId = fileId,
            slug = "di:$diId",
            volume = 1,
            volumeCount = 1,
            remoteUrl = "$DOWNLOAD/$libraryCode/pdf/$year/$fileId-1.pdf",
            sourceUrl = DawateIslamiIndex.LIBRARY_URL + libraryCode + "/" + slug,
            mirrored = false
        )
        return Volume(book, jild, firstPara = jild * 3 - 2, lastPara = jild * 3, sizeBytes = sizeBytes)
    }

    /** The Urdu work, complete in ten jild of three paras each. */
    val urdu: List<Volume> = listOf(
        volume(1238, "ur", 752, 2013, 179, "sirat-ul-jinan-jild-1", "Sirat ul Jinan-Jild-1-Para-1-To-3",
            "صراط الجنان فی تفسیر القران جلد-اول-پارہ ایک تا تین", 1, 290_105_943),
        volume(1484, "ur", 891, 2013, 171, "sirat-ul-jinan-jild-2", "Sirat ul Jinan-Jild-2-Para-4-To-6",
            "صراط الجنان فی تفسیر القران جلد-دوئم- پارہ چار تا چھ", 2, 119_728_864),
        volume(1852, "ur", 1159, 2014, 191, "sirat-ul-jinan-jild-3", "Sirat ul Jinan-Jild-3-Para-7-To-9",
            "صراط الجنان فی تفسیر القران جلد-سوئم- پارہ سات تا نو", 3, 137_002_042),
        volume(1933, "ur", 1192, 2014, 193, "sirat-ul-jinan-jild-4", "Sirat ul Jinan-Jild-4-Para-10-To-12",
            "صراط الجنان فی تفسیر القران جلد-چہارم- پارہ دس تا بارہ", 4, 65_546_114),
        volume(2121, "ur", 1250, 2015, 202, "sirat-ul-jinan-jild-5", "Sirat ul Jinan-Jild-5-Para-13-To-15",
            "صراط الجنان فی تفسیر القران جلد-پنجم- پارہ تیرہ تا پندرہ", 5, 140_706_927),
        volume(2408, "ur", 1363, 2015, 235, "sirat-ul-jinan-jild-6", "Sirat ul Jinan-Jild-6-Para-16-To-18",
            "صراط الجنان فی تفسیر القران جلد-ششم- پارہ سولہ تا اٹھارہ", 6, 160_844_203),
        volume(2644, "ur", 1448, 2015, 201, "sirat-ul-jinan-jild-7", "Sirat ul Jinan-Jild-7-Para-19-To-21",
            "صراط الجنان فی تفسیر القران جلد-ہفتم- پارہ انیس تا اکیس", 7, 134_800_009),
        volume(3055, "ur", 1572, 2016, 220, "sirat-ul-jinan-jild-8", "Sirat ul Jinan-Jild-8-Para-22-To-24",
            "صراط الجنان فی تفسیر القران جلد-ہشتم- پارہ بائیس تا چوبیس", 8, 149_489_540),
        volume(3183, "ur", 1600, 2016, 290, "sirat-ul-jinan-jild-9", "Sirat ul Jinan-Jild-9-Para-25-To-27",
            "صراط الجنان فی تفسیر القران جلد-نہم- پارہ پچیس تا ستائیس", 9, 168_905_391),
        volume(3310, "ur", 1646, 2017, 285, "sirat-ul-jinan-jild-10", "Sirat ul Jinan-Jild-10-Para-28-To-30",
            "صراط الجنان فی تفسیر القران جلد-دہم- پارہ اٹھائیس تا تیس", 10, 361_065_279)
    )

    /** The Hindi edition; only its first three jild (paras 1 to 9) are published so far. */
    val hindi: List<Volume> = listOf(
        volume(8801, "hi", 752, 2026, 605, "sirat-ul-jinan-jild-1", "सिरातुल जिनान फ़ी तफ़्सीरिल क़ुरआन जिल्द-ए-अव्वल",
            "सिरातुल जिनान फ़ी तफ़्सीरिल क़ुरआन जिल्द-ए-अव्वल", 1, 16_679_508),
        volume(8809, "hi", 891, 2026, 556, "sirat-ul-jinan-jild-2", "सिरातुल जिनान फ़ी तफ़्सीरिल क़ुरआन वॉल्यूम 2",
            "सिरातुल जिनान फ़ी तफ़्सीरिल क़ुरआन वॉल्यूम 2", 2, 16_268_894),
        volume(8810, "hi", 1159, 2026, 576, "sirat-ul-jinan-jild-3", "सिरातुल जिनान फ़ी तफ़्सीरिल क़ुरआन वॉल्यूम 3",
            "सिरातुल जिनान फ़ी तफ़्सीरिल क़ुरआन वॉल्यूम 3", 3, 39_975_479)
    )

    val all: List<Volume> = urdu + hindi

    fun byId(bookId: String?): Volume? = all.firstOrNull { it.book.id == bookId }

    /**
     * [volumes] without any the loaded library list no longer has, as the curated shelves do.
     * Before a list has loaded, [isListed] answers true and every volume is shown.
     */
    fun available(volumes: List<Volume>, isListed: (Long) -> Boolean = WorldLibrary::isListed): List<Volume> =
        volumes.filter { isListed(it.book.id.removePrefix("di-").toLong()) }
}

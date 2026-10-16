package com.example.quran

/** A translation of the meanings of the Quran served by alquran.cloud under [id]. */
data class QuranTranslation(val id: String, val language: String, val translator: String)

/** Verse-by-verse spoken translation; [bitrate] is part of its CDN path. */
data class QuranAudioTranslation(val id: String, val language: String, val voice: String, val bitrate: Int) {
    fun ayahUrl(globalAyah: Int): String {
        require(globalAyah in 1..6236)
        return "https://cdn.islamic.network/quran/audio/$bitrate/$id/$globalAyah.mp3"
    }
}

/**
 * Ahle Sunnat translations only. Shia, Qadiani/Ahmadi-linked, Quranist and non-Muslim
 * orientalist renderings offered by the API are deliberately left out. Kanzul Iman stays first.
 */
object QuranTranslationCatalog {
    const val DEFAULT_ID = "ur.kanzuliman"

    val text: List<QuranTranslation> = listOf(
        QuranTranslation("ur.kanzuliman", "Urdu", "Kanzul Iman - Imam Ahmad Raza Khan"),
        QuranTranslation("ur.qadri", "Urdu", "Irfan-ul-Quran - Tahir-ul-Qadri"),
        QuranTranslation("ur.jalandhry", "Urdu", "Fateh Muhammad Jalandhari"),
        QuranTranslation("ur.junagarhi", "Urdu", "Muhammad Junagarhi"),
        QuranTranslation("ur.maududi", "Urdu", "Abul A'la Maududi"),
        QuranTranslation("en.ahmedraza", "English", "Kanzul Iman - Imam Ahmad Raza Khan"),
        QuranTranslation("en.sahih", "English", "Saheeh International"),
        QuranTranslation("en.pickthall", "English", "Marmaduke Pickthall"),
        QuranTranslation("en.yusufali", "English", "Abdullah Yusuf Ali"),
        QuranTranslation("en.hilali", "English", "Hilali & Muhsin Khan"),
        QuranTranslation("en.daryabadi", "English", "Abdul Majid Daryabadi"),
        QuranTranslation("en.mubarakpuri", "English", "Safiur Rahman Mubarakpuri"),
        QuranTranslation("en.qaribullah", "English", "Qaribullah & Darwish"),
        QuranTranslation("en.itani", "English", "Talal Itani"),
        QuranTranslation("en.maududi", "English", "Abul A'la Maududi"),
        QuranTranslation("hi.hindi", "Hindi", "Suhel Farooq Khan & Saifur Rahman Nadwi"),
        QuranTranslation("hi.farooq", "Hindi", "Muhammad Farooq Khan & Muhammad Ahmed"),
        QuranTranslation("bn.bengali", "Bengali", "Muhiuddin Khan"),
        QuranTranslation("sd.amroti", "Sindhi", "Taj Mehmood Amroti"),
        QuranTranslation("ps.abdulwali", "Pashto", "Abdul Wali"),
        QuranTranslation("ta.tamil", "Tamil", "Jan Trust Foundation"),
        QuranTranslation("ml.abdulhameed", "Malayalam", "Abdul Hameed & Kunhi Mohammed"),
        QuranTranslation("ml.karakunnu", "Malayalam", "Karakunnu & Elayavoor"),
        QuranTranslation("si.naseemismail", "Sinhala", "Naseem Ismail"),
        QuranTranslation("dv.divehi", "Dhivehi", "Office of the President of Maldives"),
        QuranTranslation("id.indonesian", "Indonesian", "Ministry of Religious Affairs"),
        QuranTranslation("id.muntakhab", "Indonesian", "Quraish Shihab et al."),
        QuranTranslation("id.jalalayn", "Indonesian", "Tafsir Jalalayn"),
        QuranTranslation("ms.basmeih", "Malay", "Abdullah Muhammad Basmeih"),
        QuranTranslation("th.thai", "Thai", "King Fahd Quran Complex"),
        QuranTranslation("my.ghazi", "Burmese", "Ghazi Muhammad Hashim"),
        QuranTranslation("zh.majian", "Chinese", "Ma Jian"),
        QuranTranslation("zh.mazhonggang", "Chinese", "Ma Zhonggang"),
        QuranTranslation("ja.japanese", "Japanese", "Japanese translation"),
        QuranTranslation("ko.korean", "Korean", "Korean translation"),
        QuranTranslation("tr.diyanet", "Turkish", "Diyanet Isleri"),
        QuranTranslation("tr.vakfi", "Turkish", "Diyanet Vakfi"),
        QuranTranslation("tr.yazir", "Turkish", "Elmalili Hamdi Yazir"),
        QuranTranslation("tr.ates", "Turkish", "Suleyman Ates"),
        QuranTranslation("tr.bulac", "Turkish", "Ali Bulac"),
        QuranTranslation("tr.yildirim", "Turkish", "Suat Yildirim"),
        QuranTranslation("az.mammadaliyev", "Azerbaijani", "Mammadaliyev & Bunyadov"),
        QuranTranslation("az.musayev", "Azerbaijani", "Alikhan Musayev"),
        QuranTranslation("uz.sodik", "Uzbek", "Muhammad Sodik Muhammad Yusuf"),
        QuranTranslation("tt.nugman", "Tatar", "Yakub Ibn Nugman"),
        QuranTranslation("ug.saleh", "Uyghur", "Muhammad Saleh"),
        QuranTranslation("ku.asan", "Kurdish", "Burhan Muhammad-Amin"),
        QuranTranslation("ce.magomedov", "Chechen", "Magomedov"),
        QuranTranslation("ru.kuliev", "Russian", "Elmir Kuliev"),
        QuranTranslation("ru.kuliev-alsaadi", "Russian", "Kuliev & as-Saadi"),
        QuranTranslation("ru.osmanov", "Russian", "Magomed-Nuri Osmanov"),
        QuranTranslation("ru.abuadel", "Russian", "Abu Adel"),
        QuranTranslation("ru.muntahab", "Russian", "Ministry of Awqaf, Egypt"),
        QuranTranslation("ru.porokhova", "Russian", "Valeria Porokhova"),
        QuranTranslation("bs.korkut", "Bosnian", "Besim Korkut"),
        QuranTranslation("bs.mlivo", "Bosnian", "Mustafa Mlivo"),
        QuranTranslation("ba.mehanovic", "Bosnian", "Muhamed Mehanovic"),
        QuranTranslation("sq.ahmeti", "Albanian", "Sherif Ahmeti"),
        QuranTranslation("sq.mehdiu", "Albanian", "Feti Mehdiu"),
        QuranTranslation("sq.nahi", "Albanian", "Hasan Efendi Nahi"),
        QuranTranslation("fr.hamidullah", "French", "Muhammad Hamidullah"),
        QuranTranslation("de.bubenheim", "German", "Bubenheim & Elyas"),
        QuranTranslation("de.aburida", "German", "Abu Rida Muhammad Rassoul"),
        QuranTranslation("de.zaidan", "German", "Amir Zaidan"),
        QuranTranslation("es.garcia", "Spanish", "Isa Garcia"),
        QuranTranslation("pt.elhayek", "Portuguese", "Samir El-Hayek"),
        QuranTranslation("it.piccardo", "Italian", "Hamza Roberto Piccardo"),
        QuranTranslation("nl.siregar", "Dutch", "Sofian Siregar"),
        QuranTranslation("sv.bernstrom", "Swedish", "Knut Bernstrom"),
        QuranTranslation("sw.barwani", "Swahili", "Ali Muhsin Al-Barwani"),
        QuranTranslation("ha.gumi", "Hausa", "Abubakar Mahmoud Gumi"),
        QuranTranslation("so.abduh", "Somali", "Mahmud Muhammad Abduh"),
        QuranTranslation("am.sadiq", "Amharic", "Sadiq & Sani Habib"),
        QuranTranslation("ber.mensur", "Amazigh", "At Mensur"),
    )

    val audio: List<QuranAudioTranslation> = listOf(
        QuranAudioTranslation("ur.khan", "Urdu", "Shamshad Ali Khan", 64),
        QuranAudioTranslation("en.walk", "English", "Ibrahim Walk (Saheeh International)", 192),
        QuranAudioTranslation("tr.vakfi-audio", "Turkish", "Diyanet Vakfi", 128),
        QuranAudioTranslation("fr.leclerc", "French", "Youssouf Leclerc", 128),
        QuranAudioTranslation("ru.kuliev-audio", "Russian", "Elmir Kuliev", 128),
        QuranAudioTranslation("uz.sodik-audio", "Uzbek", "Muhammad Sodik Muhammad Yusuf", 128),
        QuranAudioTranslation("kk.khalifahaltai-audio", "Kazakh", "Khalifah Altai", 128),
        QuranAudioTranslation("zh.chinese", "Chinese", "Chinese translation", 128),
    )

    val languages: List<String> = text.map { it.language }.distinct()

    fun byId(id: String?): QuranTranslation = text.firstOrNull { it.id == id } ?: text.first()
    fun audioById(id: String?): QuranAudioTranslation? = audio.firstOrNull { it.id == id }
}

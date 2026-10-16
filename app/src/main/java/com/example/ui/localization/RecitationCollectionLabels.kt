package com.example.ui.localization

private val recitationLabels by lazy {
    listOf("hinglish", "hi", "ur").associateWith(::buildRecitationCollectionLabels)
}

internal fun recitationCollectionLabel(text: String, language: String): String? =
    recitationLabels[language]?.get(text) ?: recitationAudioLabel(text, language)

private fun buildRecitationCollectionLabels(language: String): Map<String, String> {
    val labels = when (language) {
        "hinglish" -> mapOf(
            "Manzil / Ruqyah / Wazaif" to "Manzil / Ruqyah / Wazaif",
            "Quranic recitations & daily remembrance" to "Qurani tilawat aur rozana azkar",
            "Search collection" to "Collection mein talash karein",
            "Traditional Manzil collection of Quranic passages. Read in the order shown." to "Qurani ayat ki riwayati Manzil collection. Di gayi tarteeb se parhein.",
            "Quranic passages and duas for seeking Allah's protection and healing." to "Allah se hifazat aur shifa maangne ke liye Qurani ayat aur duas.",
            "Daily adhkar and duas with their existing source references and recitation counts." to "Rozana azkar aur duas, hawalon aur parhne ki tadaad ke saath.",
            "Manzil collection source" to "Manzil collection ka hawala",
            "Clear filters" to "Filters hataein",
            "Unable to load recitations" to "Tilawat ka matn load nahi ho saka"
        )
        "hi" -> mapOf(
            "Manzil / Ruqyah / Wazaif" to "मंज़िल / रुक़्या / वज़ाइफ़",
            "Quranic recitations & daily remembrance" to "क़ुरआनी तिलावत और रोज़ाना अज़कार",
            "Search collection" to "संग्रह में खोजें",
            "Traditional Manzil collection of Quranic passages. Read in the order shown." to "क़ुरआनी आयतों का पारंपरिक मंज़िल संग्रह। दिए गए क्रम में पढ़ें।",
            "Quranic passages and duas for seeking Allah's protection and healing." to "अल्लाह से हिफ़ाज़त और शिफ़ा माँगने के लिए क़ुरआनी आयतें और दुआएँ।",
            "Daily adhkar and duas with their existing source references and recitation counts." to "रोज़ाना अज़कार और दुआएँ, हवाले और पढ़ने की संख्या के साथ।",
            "Manzil collection source" to "मंज़िल संग्रह का स्रोत",
            "Clear filters" to "फ़िल्टर हटाएँ",
            "Unable to load recitations" to "तिलावत का पाठ लोड नहीं हो सका"
        )
        "ur" -> mapOf(
            "Manzil / Ruqyah / Wazaif" to "منزل / رقیہ / وظائف",
            "Quranic recitations & daily remembrance" to "قرآنی تلاوت اور روزانہ اذکار",
            "Search collection" to "مجموعے میں تلاش کریں",
            "Traditional Manzil collection of Quranic passages. Read in the order shown." to "قرآنی آیات کا روایتی منزل مجموعہ۔ دی گئی ترتیب سے پڑھیں۔",
            "Quranic passages and duas for seeking Allah's protection and healing." to "اللہ سے حفاظت اور شفا مانگنے کے لیے قرآنی آیات اور دعائیں۔",
            "Daily adhkar and duas with their existing source references and recitation counts." to "روزانہ اذکار اور دعائیں، حوالوں اور پڑھنے کی تعداد کے ساتھ۔",
            "Manzil collection source" to "منزل مجموعے کا حوالہ",
            "Clear filters" to "فلٹر ہٹائیں",
            "Unable to load recitations" to "تلاوت کا متن لوڈ نہیں ہو سکا"
        )
        else -> emptyMap()
    }
    val controls = when (language) {
        "hinglish" -> mapOf("Completed" to "Mukammal", "Tap to count" to "Parhne ke baad tap karein", "Large text" to "Bara matn", "Continue reading" to "Parhna jari rakhein")
        "hi" -> mapOf("Completed" to "पूरा हुआ", "Tap to count" to "पढ़ने के बाद गिनें", "Large text" to "बड़ा पाठ", "Continue reading" to "पढ़ना जारी रखें")
        "ur" -> mapOf("Completed" to "مکمل", "Tap to count" to "پڑھنے کے بعد گنیں", "Large text" to "بڑا متن", "Continue reading" to "پڑھنا جاری رکھیں")
        else -> emptyMap()
    }
    val reader = when (language) {
        "hinglish" -> mapOf(
            "Manzil" to "Manzil", "Ruqyah" to "Ruqyah", "Wazaif" to "Wazaif",
            "Day streak" to "din lagataar",
            "Reset all" to "Sab dobara shuru karein",
            "Reset progress for this tab?" to "Is tab ki ginti dobara shuru karein?",
            "This clears the counts of every item in this tab." to "Is tab ki har cheez ki ginti saaf ho jayegi.",
            "All done for today. May Allah accept it." to "Aaj ka sab mukammal. Allah qubool farmaye.",
            "Loading recitations…" to "Tilawat ka matn load ho raha hai…",
            "Tap the heart on any card to save it here." to "Kisi bhi card par dil dabayein, woh yahan mehfooz ho jayega.",
            "Reading options" to "Parhne ki settings",
            "Text size" to "Matn ka size",
            "Smaller text" to "Matn chhota karein",
            "Larger text" to "Matn bara karein",
            "Transliteration" to "Roman talaffuz",
            "Daily reset" to "Rozana nayi ginti",
            "Counts start fresh every day" to "Har din ginti naye sire se shuru hoti hai",
            "Vibration on count" to "Ginti par vibration",
            "Keep screen on" to "Screen on rakhein",
            "Mark as read" to "Parh liya",
            "Copy" to "Copy karein", "Share" to "Share karein", "Copied" to "Copy ho gaya",
            "Add to favourites" to "Pasandeeda mein shamil karein",
            "Remove from favourites" to "Pasandeeda se hataein",
            "Play from this ayah" to "Is ayat se sunein"
        )
        "hi" -> mapOf(
            "Manzil" to "मंज़िल", "Ruqyah" to "रुक़्या", "Wazaif" to "वज़ाइफ़",
            "Day streak" to "दिन लगातार",
            "Reset all" to "सब फिर शुरू करें",
            "Reset progress for this tab?" to "इस टैब की गिनती फिर शुरू करें?",
            "This clears the counts of every item in this tab." to "इस टैब की हर चीज़ की गिनती मिट जाएगी।",
            "All done for today. May Allah accept it." to "आज का सब पूरा हुआ। अल्लाह क़बूल फ़रमाए।",
            "Loading recitations…" to "तिलावत का पाठ लोड हो रहा है…",
            "Tap the heart on any card to save it here." to "किसी भी कार्ड पर दिल दबाएँ, वह यहाँ सहेज लिया जाएगा।",
            "Reading options" to "पढ़ने की सेटिंग्स",
            "Text size" to "पाठ का आकार",
            "Smaller text" to "पाठ छोटा करें",
            "Larger text" to "पाठ बड़ा करें",
            "Transliteration" to "रोमन उच्चारण",
            "Daily reset" to "रोज़ाना नई गिनती",
            "Counts start fresh every day" to "हर दिन गिनती नए सिरे से शुरू होती है",
            "Vibration on count" to "गिनती पर वाइब्रेशन",
            "Keep screen on" to "स्क्रीन चालू रखें",
            "Mark as read" to "पढ़ लिया",
            "Copy" to "कॉपी करें", "Share" to "शेयर करें", "Copied" to "कॉपी हो गया",
            "Add to favourites" to "पसंदीदा में जोड़ें",
            "Remove from favourites" to "पसंदीदा से हटाएँ",
            "Play from this ayah" to "इस आयत से सुनें"
        )
        "ur" -> mapOf(
            "Manzil" to "منزل", "Ruqyah" to "رقیہ", "Wazaif" to "وظائف",
            "Day streak" to "دن مسلسل",
            "Reset all" to "سب دوبارہ شروع کریں",
            "Reset progress for this tab?" to "اس ٹیب کی گنتی دوبارہ شروع کریں؟",
            "This clears the counts of every item in this tab." to "اس ٹیب کی ہر چیز کی گنتی مٹ جائے گی۔",
            "All done for today. May Allah accept it." to "آج کا سب مکمل۔ اللہ قبول فرمائے۔",
            "Loading recitations…" to "تلاوت کا متن لوڈ ہو رہا ہے…",
            "Tap the heart on any card to save it here." to "کسی بھی کارڈ پر دل دبائیں، وہ یہاں محفوظ ہو جائے گا۔",
            "Reading options" to "پڑھنے کی ترتیبات",
            "Text size" to "متن کا سائز",
            "Smaller text" to "متن چھوٹا کریں",
            "Larger text" to "متن بڑا کریں",
            "Transliteration" to "رومن تلفظ",
            "Daily reset" to "روزانہ نئی گنتی",
            "Counts start fresh every day" to "ہر دن گنتی نئے سرے سے شروع ہوتی ہے",
            "Vibration on count" to "گنتی پر وائبریشن",
            "Keep screen on" to "اسکرین آن رکھیں",
            "Mark as read" to "پڑھ لیا",
            "Copy" to "کاپی کریں", "Share" to "شیئر کریں", "Copied" to "کاپی ہو گیا",
            "Add to favourites" to "پسندیدہ میں شامل کریں",
            "Remove from favourites" to "پسندیدہ سے ہٹائیں",
            "Play from this ayah" to "اس آیت سے سنیں"
        )
        else -> emptyMap()
    }
    return labels + controls + reader
}


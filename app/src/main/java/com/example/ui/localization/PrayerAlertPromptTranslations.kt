package com.example.ui.localization

/**
 * Home's "turn on prayer alerts" card, as whole phrases. Languages not listed fall back to the
 * word-level packs.
 */
internal val prayerAlertPromptLabels: Map<String, Map<String, String>> = mapOf(
    "hinglish" to prompt("Namaz ke waqt ki alert paayein", "Har namaz ka waqt shuru hone par ek halki notification.", "Alert on karein", "Abhi nahin"),
    "hi" to prompt("नमाज़ के समय की सूचना पाएँ", "हर नमाज़ का समय शुरू होने पर एक हल्की सूचना।", "सूचना चालू करें", "अभी नहीं"),
    "ur" to prompt("نماز کے وقت کی اطلاع پائیں", "ہر نماز کا وقت شروع ہونے پر ایک ہلکی اطلاع۔", "اطلاع آن کریں", "ابھی نہیں"),
    "bn" to prompt("নামাজের সময়ের সতর্কতা পান", "প্রতিটি নামাজের সময় শুরু হলে একটি মৃদু বিজ্ঞপ্তি।", "সতর্কতা চালু করুন", "এখন নয়"),
    "mr" to prompt("नमाजाच्या वेळेची सूचना मिळवा", "प्रत्येक नमाजाची वेळ सुरू झाल्यावर एक सौम्य सूचना.", "सूचना चालू करा", "आता नाही"),
    "gu" to prompt("નમાઝના સમયની સૂચના મેળવો", "દરેક નમાઝનો સમય શરૂ થાય ત્યારે એક હળવી સૂચના.", "સૂચના ચાલુ કરો", "હમણાં નહીં"),
    "ta" to prompt("தொழுகை நேர அறிவிப்புகளைப் பெறுங்கள்", "ஒவ்வொரு தொழுகை நேரமும் தொடங்கும்போது ஒரு மென்மையான அறிவிப்பு.", "அறிவிப்புகளை இயக்கு", "இப்போது வேண்டாம்"),
    "te" to prompt("నమాజ్ సమయ హెచ్చరికలు పొందండి", "ప్రతి నమాజ్ సమయం మొదలైనప్పుడు ఒక మృదువైన నోటిఫికేషన్.", "హెచ్చరికలు ఆన్ చేయండి", "ఇప్పుడు కాదు"),
    "kn" to prompt("ನಮಾಜ್ ಸಮಯದ ಎಚ್ಚರಿಕೆಗಳನ್ನು ಪಡೆಯಿರಿ", "ಪ್ರತಿ ನಮಾಜ್ ಸಮಯ ಆರಂಭವಾದಾಗ ಒಂದು ಸೌಮ್ಯ ಅಧಿಸೂಚನೆ.", "ಎಚ್ಚರಿಕೆ ಆನ್ ಮಾಡಿ", "ಈಗ ಬೇಡ"),
    "ml" to prompt("നമസ്കാര സമയ അറിയിപ്പുകൾ നേടുക", "ഓരോ നമസ്കാര സമയവും തുടങ്ങുമ്പോൾ ഒരു ലഘു അറിയിപ്പ്.", "അറിയിപ്പുകൾ ഓണാക്കുക", "ഇപ്പോൾ വേണ്ട"),
    "ar" to prompt("احصل على تنبيهات أوقات الصلاة", "إشعار لطيف عند دخول وقت كل صلاة.", "تفعيل التنبيهات", "ليس الآن")
)

private fun prompt(title: String, body: String, enable: String, notNow: String) = mapOf(
    "Get prayer time alerts" to title,
    "A gentle notification when each prayer time begins." to body,
    "Turn on alerts" to enable,
    "Not now" to notNow
)

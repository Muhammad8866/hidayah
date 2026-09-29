package com.example.ui.localization

private const val SUGGESTED = "Suggested"
private const val ENGLISH = "English"
private const val DOWNLOAD_STOPPED = "Download stopped. Check your internet connection and try again."
private const val WORD_AUDIO_OFFLINE = "No internet. This word's audio needs a connection."

private fun labels(suggested: String, english: String, downloadStopped: String, wordAudioOffline: String) = mapOf(
    SUGGESTED to suggested,
    ENGLISH to english,
    DOWNLOAD_STOPPED to downloadStopped,
    WORD_AUDIO_OFFLINE to wordAudioOffline
)

/**
 * Whole-string labels (badges and toasts) that word-by-word packs would translate badly,
 * matched exactly before the language packs.
 */
internal val auditUiLabels: Map<String, Map<String, String>> = mapOf(
    "hinglish" to labels("Mashwara", "English", "Download ruk gaya. Internet check karke dobara koshish karein.", "Internet nahi hai. Is lafz ki audio ke liye connection chahiye."),
    "hi" to labels("सुझाया गया", "अंग्रेज़ी", "डाउनलोड रुक गया। इंटरनेट कनेक्शन जाँचें और दोबारा कोशिश करें।", "इंटरनेट नहीं है। इस शब्द की ऑडियो के लिए कनेक्शन चाहिए।"),
    "ur" to labels("تجویز کردہ", "انگریزی", "ڈاؤن لوڈ رک گیا۔ انٹرنیٹ کنکشن چیک کر کے دوبارہ کوشش کریں۔", "انٹرنیٹ نہیں ہے۔ اس لفظ کی آڈیو کے لیے کنکشن ضروری ہے۔"),
    "as" to labels("পৰামৰ্শ দিয়া", "ইংৰাজী", "ডাউনলোড বন্ধ হ'ল। ইণ্টাৰনেট সংযোগ পৰীক্ষা কৰি পুনৰ চেষ্টা কৰক।", "ইণ্টাৰনেট নাই। এই শব্দৰ অডিঅ'ৰ বাবে সংযোগ লাগিব।"),
    "bn" to labels("প্রস্তাবিত", "ইংরেজি", "ডাউনলোড থেমে গেছে। ইন্টারনেট সংযোগ দেখে আবার চেষ্টা করুন।", "ইন্টারনেট নেই। এই শব্দের অডিওর জন্য সংযোগ দরকার।"),
    "brx" to labels("बुंथि होनाय", "इंगराजि", "डाउनल'ड थादों। इन्टारनेट खाथियाव नायना फिन नाजा।", "इन्टारनेट गैया। बे सोदोबनि अडिअ'नि थाखाय खाथि नांगो।"),
    "doi" to labels("सुझाया गेआ", "अंग्रेज़ी", "डाउनलोड रुकी गेआ। इंटरनेट कनेक्शन दिक्खो ते फ्ही कोशश करो।", "इंटरनेट नेईं। इस शब्द दी ऑडियो आस्तै कनेक्शन चाहिदा।"),
    "gu" to labels("સૂચવેલ", "અંગ્રેજી", "ડાઉનલોડ અટકી ગયું. ઇન્ટરનેટ કનેક્શન તપાસી ફરી પ્રયાસ કરો.", "ઇન્ટરનેટ નથી. આ શબ્દના ઑડિયો માટે કનેક્શન જરૂરી છે."),
    "kn" to labels("ಸೂಚಿಸಲಾಗಿದೆ", "ಇಂಗ್ಲಿಷ್", "ಡೌನ್‌ಲೋಡ್ ನಿಂತಿದೆ. ಇಂಟರ್ನೆಟ್ ಸಂಪರ್ಕ ಪರಿಶೀಲಿಸಿ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.", "ಇಂಟರ್ನೆಟ್ ಇಲ್ಲ. ಈ ಪದದ ಆಡಿಯೊಗೆ ಸಂಪರ್ಕ ಬೇಕು."),
    "ks" to labels("تجویز کَرنہٕ آمُت", "انگریزی", "ڈاؤن لوڈ گَو بند۔ انٹرنیٹ کنکشن وُچھِو تہٕ دوبارٕ کوشش کٔرِو۔", "انٹرنیٹ چھُنہٕ۔ یَتھ لفظچہِ آڈیو خٲطرٕ چھُ کنکشن ضروری۔"),
    "kok" to labels("सुचयिल्लें", "इंग्लीश", "डाउनलोड थांबलें. इंटरनेट कनेक्शन तपासून परत यत्न करात.", "इंटरनेट ना. ह्या उतराच्या ऑडियोखातीर कनेक्शन जाय."),
    "mai" to labels("सुझाओल गेल", "अंग्रेजी", "डाउनलोड रुकि गेल। इंटरनेट कनेक्शन जाँचू आ फेर प्रयास करू।", "इंटरनेट नहि अछि। एहि शब्दक ऑडियो लेल कनेक्शन चाही।"),
    "ml" to labels("നിർദ്ദേശിച്ചത്", "ഇംഗ്ലീഷ്", "ഡൗൺലോഡ് നിന്നു. ഇന്റർനെറ്റ് കണക്ഷൻ പരിശോധിച്ച് വീണ്ടും ശ്രമിക്കുക.", "ഇന്റർനെറ്റ് ഇല്ല. ഈ വാക്കിന്റെ ഓഡിയോയ്ക്ക് കണക്ഷൻ വേണം."),
    "mni" to labels("পাউতাক পীবা", "ইংলিস", "ডাউনলোড লেপখ্রে। ইন্টরনেট কনেক্সন য়েংলগা অমুক হন্না হোৎনবিয়ু।", "ইন্টরনেট লৈতে। ৱাহৈ অসিগী অডিওগীদমক কনেক্সন মথৌ তাই।"),
    "mr" to labels("सुचवलेले", "इंग्रजी", "डाउनलोड थांबले. इंटरनेट कनेक्शन तपासा आणि पुन्हा प्रयत्न करा.", "इंटरनेट नाही. या शब्दाच्या ऑडिओसाठी कनेक्शन आवश्यक आहे."),
    "ne" to labels("सुझाव गरिएको", "अंग्रेजी", "डाउनलोड रोकियो। इन्टरनेट जडान जाँच गरी फेरि प्रयास गर्नुहोस्।", "इन्टरनेट छैन। यो शब्दको अडियोका लागि जडान चाहिन्छ।"),
    "or" to labels("ପରାମର୍ଶିତ", "ଇଂରାଜୀ", "ଡାଉନଲୋଡ୍ ବନ୍ଦ ହେଲା। ଇଣ୍ଟରନେଟ୍ ସଂଯୋଗ ଯାଞ୍ଚ କରି ପୁଣି ଚେଷ୍ଟା କରନ୍ତୁ।", "ଇଣ୍ଟରନେଟ୍ ନାହିଁ। ଏହି ଶବ୍ଦର ଅଡିଓ ପାଇଁ ସଂଯୋଗ ଆବଶ୍ୟକ।"),
    "pa" to labels("ਸੁਝਾਇਆ ਗਿਆ", "ਅੰਗਰੇਜ਼ੀ", "ਡਾਊਨਲੋਡ ਰੁਕ ਗਿਆ। ਇੰਟਰਨੈੱਟ ਕਨੈਕਸ਼ਨ ਜਾਂਚੋ ਅਤੇ ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ।", "ਇੰਟਰਨੈੱਟ ਨਹੀਂ ਹੈ। ਇਸ ਸ਼ਬਦ ਦੀ ਆਡੀਓ ਲਈ ਕਨੈਕਸ਼ਨ ਚਾਹੀਦਾ ਹੈ।"),
    "sa" to labels("सूचितम्", "आङ्ग्लभाषा", "अवतरणं स्थगितम्। अन्तर्जालसम्पर्कं परीक्ष्य पुनः प्रयतताम्।", "अन्तर्जालं नास्ति। अस्य शब्दस्य ध्वनये सम्पर्कः आवश्यकः।"),
    "sat" to labels("ᱩᱯᱚᱫᱮᱥ ᱮᱢ ᱟᱠᱟᱱ", "ᱤᱝᱞᱤᱥ", "ᱰᱟᱩᱱᱞᱚᱰ ᱛᱷᱟᱢᱟᱱ ᱮᱱᱟ᱾ ᱤᱱᱴᱟᱨᱱᱮᱴ ᱧᱮᱞ ᱛᱟᱭᱚᱢ ᱫᱚᱦᱲᱟ ᱪᱮᱥᱴᱟᱭ ᱢᱮ᱾", "ᱤᱱᱴᱟᱨᱱᱮᱴ ᱵᱟᱹᱱᱩᱜᱼᱟ᱾ ᱱᱚᱶᱟ ᱟᱹᱲᱟᱹ ᱨᱮᱭᱟᱜ ᱚᱰᱤᱭᱚ ᱞᱟᱹᱜᱤᱫ ᱡᱩᱲᱟᱹᱣ ᱞᱟᱹᱠᱛᱤ᱾"),
    "sd" to labels("تجويز ڪيل", "انگريزي", "ڊائونلوڊ رڪجي ويو. انٽرنيٽ ڪنيڪشن چيڪ ڪري ٻيهر ڪوشش ڪريو.", "انٽرنيٽ ناهي. هن لفظ جي آڊيو لاءِ ڪنيڪشن گهرجي."),
    "ta" to labels("பரிந்துரைக்கப்பட்டது", "ஆங்கிலம்", "பதிவிறக்கம் நின்றது. இணைய இணைப்பைச் சரிபார்த்து மீண்டும் முயலவும்.", "இணையம் இல்லை. இந்தச் சொல்லின் ஒலிக்கு இணைப்பு தேவை."),
    "te" to labels("సూచించబడింది", "ఇంగ్లీష్", "డౌన్‌లోడ్ ఆగిపోయింది. ఇంటర్నెట్ కనెక్షన్ తనిఖీ చేసి మళ్లీ ప్రయత్నించండి.", "ఇంటర్నెట్ లేదు. ఈ పదం ఆడియోకు కనెక్షన్ అవసరం."),
    "ar" to labels("مقترح", "الإنجليزية", "توقف التنزيل. تحقق من اتصال الإنترنت وحاول مرة أخرى.", "لا يوجد إنترنت. صوت هذه الكلمة يحتاج إلى اتصال."),
    "fa" to labels("پیشنهادی", "انگلیسی", "دانلود متوقف شد. اتصال اینترنت را بررسی کنید و دوباره تلاش کنید.", "اینترنت وصل نیست. صوت این کلمه به اتصال نیاز دارد."),
    "tr" to labels("Önerilen", "İngilizce", "İndirme durdu. İnternet bağlantınızı kontrol edip tekrar deneyin.", "İnternet yok. Bu kelimenin sesi için bağlantı gerekir."),
    "id" to labels("Disarankan", "Inggris", "Unduhan berhenti. Periksa koneksi internet lalu coba lagi.", "Tidak ada internet. Audio kata ini memerlukan koneksi."),
    "fr" to labels("Suggéré", "Anglais", "Téléchargement interrompu. Vérifiez votre connexion Internet et réessayez.", "Pas d'Internet. L'audio de ce mot nécessite une connexion."),
    "es" to labels("Sugerido", "Inglés", "La descarga se detuvo. Revisa tu conexión a internet e inténtalo de nuevo.", "Sin internet. El audio de esta palabra necesita conexión."),
    "ru" to labels("Рекомендуется", "Английский", "Загрузка остановлена. Проверьте подключение к интернету и повторите попытку.", "Нет интернета. Для аудио этого слова нужно подключение."),
    "de" to labels("Empfohlen", "Englisch", "Download angehalten. Prüfe deine Internetverbindung und versuche es erneut.", "Kein Internet. Das Audio dieses Wortes benötigt eine Verbindung."),
    "pt" to labels("Sugerido", "Inglês", "O download parou. Verifique sua conexão com a internet e tente novamente.", "Sem internet. O áudio desta palavra precisa de conexão."),
    "zh" to labels("推荐", "英语", "下载已停止。请检查网络连接后重试。", "无网络。此词的音频需要联网。"),
    "sw" to labels("Inapendekezwa", "Kiingereza", "Upakuaji umesimama. Angalia muunganisho wa intaneti kisha ujaribu tena.", "Hakuna intaneti. Sauti ya neno hili inahitaji muunganisho."),
    "ha" to labels("Shawara", "Turanci", "Saukewa ya tsaya. Duba haɗin intanet ɗinka ka sake gwadawa.", "Babu intanet. Sautin wannan kalma yana buƙatar haɗi."),
    "ms" to labels("Dicadangkan", "Inggeris", "Muat turun terhenti. Semak sambungan internet dan cuba lagi.", "Tiada internet. Audio perkataan ini memerlukan sambungan."),
    "it" to labels("Consigliato", "Inglese", "Download interrotto. Controlla la connessione a Internet e riprova.", "Nessuna connessione. L'audio di questa parola richiede Internet."),
    "nl" to labels("Aanbevolen", "Engels", "Download gestopt. Controleer je internetverbinding en probeer het opnieuw.", "Geen internet. De audio van dit woord heeft een verbinding nodig."),
    "vi" to labels("Đề xuất", "Tiếng Anh", "Tải xuống đã dừng. Hãy kiểm tra kết nối internet và thử lại.", "Không có internet. Âm thanh của từ này cần kết nối."),
    "th" to labels("แนะนำ", "ภาษาอังกฤษ", "การดาวน์โหลดหยุดลง ตรวจสอบการเชื่อมต่ออินเทอร์เน็ตแล้วลองอีกครั้ง", "ไม่มีอินเทอร์เน็ต เสียงของคำนี้ต้องเชื่อมต่อ"),
    "tl" to labels("Iminumungkahi", "Ingles", "Huminto ang pag-download. Suriin ang koneksyon sa internet at subukang muli.", "Walang internet. Kailangan ng koneksyon para sa audio ng salitang ito."),
    "ps" to labels("وړاندیز شوی", "انګلیسي", "ډاونلوډ ودرېد. د انټرنیټ اړیکه وګورئ او بیا هڅه وکړئ.", "انټرنیټ نشته. د دې کلمې غږ ته اړیکه اړینه ده."),
    "ja" to labels("おすすめ", "英語", "ダウンロードが停止しました。インターネット接続を確認して再試行してください。", "インターネットに接続されていません。この単語の音声には接続が必要です。"),
    "ko" to labels("추천", "영어", "다운로드가 중지되었습니다. 인터넷 연결을 확인하고 다시 시도하세요.", "인터넷 연결이 없습니다. 이 단어의 오디오는 연결이 필요합니다."),
    "el" to labels("Προτεινόμενο", "Αγγλικά", "Η λήψη σταμάτησε. Ελέγξτε τη σύνδεση στο διαδίκτυο και δοκιμάστε ξανά.", "Χωρίς internet. Ο ήχος αυτής της λέξης χρειάζεται σύνδεση."),
    "pl" to labels("Sugerowane", "Angielski", "Pobieranie zatrzymane. Sprawdź połączenie z internetem i spróbuj ponownie.", "Brak internetu. Dźwięk tego słowa wymaga połączenia."),
    "uk" to labels("Рекомендовано", "Англійська", "Завантаження зупинено. Перевірте підключення до інтернету та спробуйте ще раз.", "Немає інтернету. Для аудіо цього слова потрібне підключення."),
    "so" to labels("La soo jeediyay", "Ingiriis", "Soo dejintu way istaagtay. Hubi xiriirka internetka oo isku day mar kale.", "Internet ma jiro. Codka eraygan wuxuu u baahan yahay xiriir."),
    "am" to labels("የተጠቆመ", "እንግሊዝኛ", "ማውረዱ ቆሟል። የኢንተርኔት ግንኙነትዎን አረጋግጠው እንደገና ይሞክሩ።", "ኢንተርኔት የለም። የዚህ ቃል ድምፅ ግንኙነት ይፈልጋል።"),
    "uz" to labels("Tavsiya etilgan", "Ingliz tili", "Yuklab olish to'xtadi. Internet aloqasini tekshirib, qayta urinib ko'ring.", "Internet yo'q. Bu so'z audiosi uchun aloqa kerak."),
    "he" to labels("מומלץ", "אנגלית", "ההורדה נעצרה. בדקו את החיבור לאינטרנט ונסו שוב.", "אין אינטרנט. השמע של מילה זו דורש חיבור."),
    "si" to labels("යෝජිත", "ඉංග්‍රීසි", "බාගැනීම නතර විය. අන්තර්ජාල සම්බන්ධතාවය පරීක්ෂා කර නැවත උත්සාහ කරන්න.", "අන්තර්ජාලය නැත. මෙම වචනයේ ශ්‍රව්‍යයට සම්බන්ධතාවයක් අවශ්‍යයි.")
)

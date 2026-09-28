package com.example.ui.localization

/**
 * Controls added to the Duas screen: search, the occasion picker, the dua reader (pronunciation,
 * text size, copy/share) and the tasbeeh round counter.
 * The Hindi keys join the reference UI copy, so every language must translate all of them.
 * Phrase keys also match inside longer UI text, so they avoid bare common words ("Share").
 */
internal val duaScreenPhrases: Map<String, Map<String, String>> = mapOf(
    "hinglish" to mapOf(
        "Search duas" to "Duaein khojein", "Clear search" to "Search saaf karein",
        "No duas found" to "Koi dua nahi mili", "Copy dua" to "Copy karein", "Share dua" to "Share karein",
        "Copied" to "Copy ho gaya", "Rounds" to "Daur",
        "Pronunciation" to "Talaffuz", "Text size" to "Text ka size", "Choose an occasion" to "Mauqa chunein",
        "Tap a card to count" to "Ginti ke liye card par tap karein", "All adhkar completed" to "Saare azkaar mukammal",
        "Tap the circle to count" to "Ginti ke liye gole par tap karein", "Reset count?" to "Ginti reset karein?"
    ),
    "hi" to mapOf(
        "Search duas" to "दुआएँ खोजें", "Clear search" to "खोज साफ़ करें",
        "No duas found" to "कोई दुआ नहीं मिली", "Copy dua" to "कॉपी करें", "Share dua" to "शेयर करें",
        "Copied" to "कॉपी हो गया", "Rounds" to "दौर",
        "Pronunciation" to "उच्चारण", "Text size" to "अक्षर का आकार", "Choose an occasion" to "मौक़ा चुनें",
        "Tap a card to count" to "गिनने के लिए कार्ड पर टैप करें", "All adhkar completed" to "सारे अज़कार पूरे हुए",
        "Tap the circle to count" to "गिनने के लिए गोले पर टैप करें", "Reset count?" to "गिनती रीसेट करें?"
    ),
    "ur" to mapOf(
        "Search duas" to "دعائیں تلاش کریں", "Clear search" to "تلاش صاف کریں",
        "No duas found" to "کوئی دعا نہیں ملی", "Copy dua" to "کاپی کریں", "Share dua" to "شیئر کریں",
        "Copied" to "کاپی ہو گیا", "Rounds" to "دور",
        "Pronunciation" to "تلفظ", "Text size" to "متن کا سائز", "Choose an occasion" to "موقع منتخب کریں",
        "Tap a card to count" to "گننے کے لیے کارڈ پر ٹیپ کریں", "All adhkar completed" to "تمام اذکار مکمل",
        "Tap the circle to count" to "گننے کے لیے دائرے پر ٹیپ کریں", "Reset count?" to "گنتی ری سیٹ کریں؟"
    ),
    "as" to mapOf(
        "Search duas" to "দুআ সন্ধান কৰক", "Clear search" to "সন্ধান মচক",
        "No duas found" to "কোনো দুআ পোৱা নগ'ল", "Copy dua" to "কপি কৰক", "Share dua" to "শ্বেয়াৰ কৰক",
        "Copied" to "কপি কৰা হ'ল", "Rounds" to "পাক",
        "Pronunciation" to "উচ্চাৰণ", "Text size" to "আখৰৰ আকাৰ", "Choose an occasion" to "উপলক্ষ বাছনি কৰক",
        "Tap a card to count" to "গণিবলৈ কাৰ্ডত টেপ কৰক", "All adhkar completed" to "সকলো আজকাৰ সম্পূৰ্ণ",
        "Tap the circle to count" to "গণিবলৈ বৃত্তত টেপ কৰক", "Reset count?" to "গণনা ৰিছেট কৰিবনে?"
    ),
    "bn" to mapOf(
        "Search duas" to "দোয়া খুঁজুন", "Clear search" to "খোঁজা মুছুন",
        "No duas found" to "কোনো দোয়া পাওয়া যায়নি", "Copy dua" to "কপি করুন", "Share dua" to "শেয়ার করুন",
        "Copied" to "কপি হয়েছে", "Rounds" to "রাউন্ড",
        "Pronunciation" to "উচ্চারণ", "Text size" to "লেখার আকার", "Choose an occasion" to "উপলক্ষ বেছে নিন",
        "Tap a card to count" to "গুনতে কার্ডে ট্যাপ করুন", "All adhkar completed" to "সব আযকার সম্পূর্ণ",
        "Tap the circle to count" to "গুনতে বৃত্তে ট্যাপ করুন", "Reset count?" to "গণনা রিসেট করবেন?"
    ),
    "brx" to mapOf(
        "Search duas" to "दुआ नागिर", "Clear search" to "नागिरनाय खोमोर",
        "No duas found" to "जेबो दुआ मोनाखै", "Copy dua" to "कपि खालाम", "Share dua" to "शेयार खालाम",
        "Copied" to "कपि जाबाय", "Rounds" to "राउन्ड",
        "Pronunciation" to "उच्चारण", "Text size" to "आखर महर", "Choose an occasion" to "समाव सायख",
        "Tap a card to count" to "सान्नो कार्डआव टेप खालाम", "All adhkar completed" to "गासै आजकार जोबबाय",
        "Tap the circle to count" to "सान्नो गोलाआव टेप खालाम", "Reset count?" to "सानखान्थि रिसेट खालामनो?"
    ),
    "doi" to mapOf(
        "Search duas" to "दुआं तुप्पो", "Clear search" to "तुप्पना साफ करो",
        "No duas found" to "कोई दुआ नेईं लब्भी", "Copy dua" to "कापी करो", "Share dua" to "शेयर करो",
        "Copied" to "कापी होई गेआ", "Rounds" to "गेड़े",
        "Pronunciation" to "उच्चारण", "Text size" to "अक्खरें दा साइज़", "Choose an occasion" to "मौका चुनो",
        "Tap a card to count" to "गिनने आस्तै कार्ड पर टैप करो", "All adhkar completed" to "सारे अज़कार पूरे होई गे",
        "Tap the circle to count" to "गिनने आस्तै गोले पर टैप करो", "Reset count?" to "गिनती रीसेट करनी ऐ?"
    ),
    "gu" to mapOf(
        "Search duas" to "દુઆઓ શોધો", "Clear search" to "શોધ સાફ કરો",
        "No duas found" to "કોઈ દુઆ મળી નથી", "Copy dua" to "કૉપિ કરો", "Share dua" to "શેર કરો",
        "Copied" to "કૉપિ થયું", "Rounds" to "રાઉન્ડ",
        "Pronunciation" to "ઉચ્ચાર", "Text size" to "અક્ષરનું કદ", "Choose an occasion" to "પ્રસંગ પસંદ કરો",
        "Tap a card to count" to "ગણવા માટે કાર્ડ પર ટૅપ કરો", "All adhkar completed" to "બધા અઝકાર પૂર્ણ",
        "Tap the circle to count" to "ગણવા માટે વર્તુળ પર ટૅપ કરો", "Reset count?" to "ગણતરી રીસેટ કરવી?"
    ),
    "kn" to mapOf(
        "Search duas" to "ದುವಾಗಳನ್ನು ಹುಡುಕಿ", "Clear search" to "ಹುಡುಕಾಟ ಅಳಿಸಿ",
        "No duas found" to "ಯಾವುದೇ ದುವಾ ಸಿಗಲಿಲ್ಲ", "Copy dua" to "ನಕಲಿಸಿ", "Share dua" to "ಹಂಚಿಕೊಳ್ಳಿ",
        "Copied" to "ನಕಲಿಸಲಾಗಿದೆ", "Rounds" to "ಸುತ್ತುಗಳು",
        "Pronunciation" to "ಉಚ್ಚಾರಣೆ", "Text size" to "ಅಕ್ಷರದ ಗಾತ್ರ", "Choose an occasion" to "ಸಂದರ್ಭ ಆಯ್ಕೆಮಾಡಿ",
        "Tap a card to count" to "ಎಣಿಸಲು ಕಾರ್ಡ್ ಟ್ಯಾಪ್ ಮಾಡಿ", "All adhkar completed" to "ಎಲ್ಲಾ ಅಜ್ಕಾರ್ ಪೂರ್ಣ",
        "Tap the circle to count" to "ಎಣಿಸಲು ವೃತ್ತವನ್ನು ಟ್ಯಾಪ್ ಮಾಡಿ", "Reset count?" to "ಎಣಿಕೆ ಮರುಹೊಂದಿಸಬೇಕೇ?"
    ),
    "ks" to mapOf(
        "Search duas" to "دُعا ژھانڈِو", "Clear search" to "ژھانڈُن صاف کٔرِو",
        "No duas found" to "کانٛہہ دُعا مِلیٚ نہٕ", "Copy dua" to "کاپی کٔرِو", "Share dua" to "شیئر کٔرِو",
        "Copied" to "کاپی سپُد", "Rounds" to "دور",
        "Pronunciation" to "تلفُظ", "Text size" to "متنُک سائز", "Choose an occasion" to "موقع ژارِو",
        "Tap a card to count" to "گَنزراونہٕ خٲطرٕ کارڈس پؠٹھ ٹیپ کٔرِو", "All adhkar completed" to "سٲری اذکار مکمل",
        "Tap the circle to count" to "گَنزراونہٕ خٲطرٕ دایرس پؠٹھ ٹیپ کٔرِو", "Reset count?" to "گَنزرُن ری سیٹ کرُن؟"
    ),
    "kok" to mapOf(
        "Search duas" to "दुवा सोदात", "Clear search" to "सोद साफ करात",
        "No duas found" to "एकूय दुवा मेळूंक ना", "Copy dua" to "कॉपी करात", "Share dua" to "शेअर करात",
        "Copied" to "कॉपी जालें", "Rounds" to "फेरे",
        "Pronunciation" to "उच्चार", "Text size" to "अक्षरांचो आकार", "Choose an occasion" to "प्रसंग वेंचात",
        "Tap a card to count" to "मेजपाक कार्डाचेर टॅप करात", "All adhkar completed" to "सगळे अज़कार पुराय",
        "Tap the circle to count" to "मेजपाक वाटकुळाचेर टॅप करात", "Reset count?" to "मेजणी रिसेट करची?"
    ),
    "mai" to mapOf(
        "Search duas" to "दुआ खोजू", "Clear search" to "खोज साफ करू",
        "No duas found" to "कोनो दुआ नहि भेटल", "Copy dua" to "कॉपी करू", "Share dua" to "शेयर करू",
        "Copied" to "कॉपी भ' गेल", "Rounds" to "फेरा",
        "Pronunciation" to "उच्चारण", "Text size" to "आखरक आकार", "Choose an occasion" to "अवसर चुनू",
        "Tap a card to count" to "गनबाक लेल कार्ड पर टैप करू", "All adhkar completed" to "सभ अज़कार पूरा भेल",
        "Tap the circle to count" to "गनबाक लेल गोला पर टैप करू", "Reset count?" to "गिनती रीसेट करू?"
    ),
    "ml" to mapOf(
        "Search duas" to "ദുആകൾ തിരയുക", "Clear search" to "തിരയൽ മായ്ക്കുക",
        "No duas found" to "ദുആകളൊന്നും കണ്ടെത്തിയില്ല", "Copy dua" to "പകർത്തുക", "Share dua" to "പങ്കിടുക",
        "Copied" to "പകർത്തി", "Rounds" to "റൗണ്ടുകൾ",
        "Pronunciation" to "ഉച്ചാരണം", "Text size" to "അക്ഷര വലിപ്പം", "Choose an occasion" to "സന്ദർഭം തിരഞ്ഞെടുക്കുക",
        "Tap a card to count" to "എണ്ണാൻ കാർഡിൽ ടാപ്പ് ചെയ്യുക", "All adhkar completed" to "എല്ലാ അദ്കാറും പൂർത്തിയായി",
        "Tap the circle to count" to "എണ്ണാൻ വൃത്തത്തിൽ ടാപ്പ് ചെയ്യുക", "Reset count?" to "എണ്ണം റീസെറ്റ് ചെയ്യണോ?"
    ),
    "mni" to mapOf(
        "Search duas" to "দুআ থিবীয়ু", "Clear search" to "থিবা মুত্থৎপীয়ু",
        "No duas found" to "দুআ অমত্তা ফংদ্রে", "Copy dua" to "কোপি তৌবীয়ু", "Share dua" to "শেয়র তৌবীয়ু",
        "Copied" to "কোপি তৌরে", "Rounds" to "রাউন্দ",
        "Pronunciation" to "উচ্চারণ", "Text size" to "ময়েক্কী চাং", "Choose an occasion" to "মতম খল্লু",
        "Tap a card to count" to "মশিং থীনবা কার্দ তেপ তৌবিয়ু", "All adhkar completed" to "আজকার পুম্নমক লোইরে",
        "Tap the circle to count" to "মশিং থীনবা কোয়াবদা তেপ তৌবিয়ু", "Reset count?" to "মশিং রিসেট তৌগদ্রা?"
    ),
    "mr" to mapOf(
        "Search duas" to "दुआ शोधा", "Clear search" to "शोध साफ करा",
        "No duas found" to "एकही दुआ सापडली नाही", "Copy dua" to "कॉपी करा", "Share dua" to "शेअर करा",
        "Copied" to "कॉपी झाले", "Rounds" to "फेऱ्या",
        "Pronunciation" to "उच्चार", "Text size" to "अक्षरांचा आकार", "Choose an occasion" to "प्रसंग निवडा",
        "Tap a card to count" to "मोजण्यासाठी कार्डवर टॅप करा", "All adhkar completed" to "सर्व अज़कार पूर्ण",
        "Tap the circle to count" to "मोजण्यासाठी वर्तुळावर टॅप करा", "Reset count?" to "मोजणी रीसेट करायची?"
    ),
    "ne" to mapOf(
        "Search duas" to "दुआ खोज्नुहोस्", "Clear search" to "खोज हटाउनुहोस्",
        "No duas found" to "कुनै दुआ भेटिएन", "Copy dua" to "प्रतिलिपि गर्नुहोस्", "Share dua" to "सेयर गर्नुहोस्",
        "Copied" to "प्रतिलिपि भयो", "Rounds" to "फेरा",
        "Pronunciation" to "उच्चारण", "Text size" to "अक्षरको आकार", "Choose an occasion" to "अवसर छान्नुहोस्",
        "Tap a card to count" to "गन्न कार्डमा ट्याप गर्नुहोस्", "All adhkar completed" to "सबै अज़कार पूरा भयो",
        "Tap the circle to count" to "गन्न वृत्तमा ट्याप गर्नुहोस्", "Reset count?" to "गणना रिसेट गर्ने?"
    ),
    "or" to mapOf(
        "Search duas" to "ଦୁଆ ଖୋଜନ୍ତୁ", "Clear search" to "ସନ୍ଧାନ ଲିଭାନ୍ତୁ",
        "No duas found" to "କୌଣସି ଦୁଆ ମିଳିଲା ନାହିଁ", "Copy dua" to "କପି କରନ୍ତୁ", "Share dua" to "ସେୟାର କରନ୍ତୁ",
        "Copied" to "କପି ହେଲା", "Rounds" to "ରାଉଣ୍ଡ",
        "Pronunciation" to "ଉଚ୍ଚାରଣ", "Text size" to "ଅକ୍ଷର ଆକାର", "Choose an occasion" to "ଅବସର ବାଛନ୍ତୁ",
        "Tap a card to count" to "ଗଣିବା ପାଇଁ କାର୍ଡରେ ଟାପ୍ କରନ୍ତୁ", "All adhkar completed" to "ସମସ୍ତ ଆଜକାର ସମ୍ପୂର୍ଣ୍ଣ",
        "Tap the circle to count" to "ଗଣିବା ପାଇଁ ବୃତ୍ତରେ ଟାପ୍ କରନ୍ତୁ", "Reset count?" to "ଗଣନା ରିସେଟ୍ କରିବେ?"
    ),
    "pa" to mapOf(
        "Search duas" to "ਦੁਆਵਾਂ ਖੋਜੋ", "Clear search" to "ਖੋਜ ਸਾਫ਼ ਕਰੋ",
        "No duas found" to "ਕੋਈ ਦੁਆ ਨਹੀਂ ਮਿਲੀ", "Copy dua" to "ਕਾਪੀ ਕਰੋ", "Share dua" to "ਸਾਂਝਾ ਕਰੋ",
        "Copied" to "ਕਾਪੀ ਹੋ ਗਿਆ", "Rounds" to "ਗੇੜੇ",
        "Pronunciation" to "ਉਚਾਰਨ", "Text size" to "ਅੱਖਰਾਂ ਦਾ ਆਕਾਰ", "Choose an occasion" to "ਮੌਕਾ ਚੁਣੋ",
        "Tap a card to count" to "ਗਿਣਨ ਲਈ ਕਾਰਡ 'ਤੇ ਟੈਪ ਕਰੋ", "All adhkar completed" to "ਸਾਰੇ ਅਜ਼ਕਾਰ ਪੂਰੇ",
        "Tap the circle to count" to "ਗਿਣਨ ਲਈ ਚੱਕਰ 'ਤੇ ਟੈਪ ਕਰੋ", "Reset count?" to "ਗਿਣਤੀ ਰੀਸੈੱਟ ਕਰਨੀ ਹੈ?"
    ),
    "sa" to mapOf(
        "Search duas" to "दुआः अन्विष्यत", "Clear search" to "अन्वेषणं लोपयतु",
        "No duas found" to "काऽपि दुआ न लब्धा", "Copy dua" to "प्रतिलिपिं करोतु", "Share dua" to "प्रेषयतु",
        "Copied" to "प्रतिलिपिः कृता", "Rounds" to "आवृत्तयः",
        "Pronunciation" to "उच्चारणम्", "Text size" to "अक्षरपरिमाणम्", "Choose an occasion" to "अवसरं चिनुत",
        "Tap a card to count" to "गणनाय पत्रं स्पृशतु", "All adhkar completed" to "सर्वे अज़काराः पूर्णाः",
        "Tap the circle to count" to "गणनाय वृत्तं स्पृशतु", "Reset count?" to "गणनां पुनः आरभताम्?"
    ),
    "sat" to mapOf(
        "Search duas" to "ᱫᱩᱣᱟ ᱯᱟᱱᱛᱮ", "Clear search" to "ᱯᱟᱱᱛᱮ ᱢᱮᱴᱟᱣ",
        "No duas found" to "ᱪᱮᱫ ᱦᱚᱸ ᱫᱩᱣᱟ ᱵᱟᱝ ᱧᱟᱢ ᱮᱱᱟ", "Copy dua" to "ᱠᱚᱯᱤ", "Share dua" to "ᱦᱟᱹᱴᱤᱧ",
        "Copied" to "ᱠᱚᱯᱤ ᱮᱱᱟ", "Rounds" to "ᱯᱷᱮᱨᱟ",
        "Pronunciation" to "ᱩᱪᱟᱨᱚᱱ", "Text size" to "ᱟᱠᱷᱚᱨ ᱢᱟᱯ", "Choose an occasion" to "ᱚᱠᱛᱚ ᱵᱟᱪᱷᱟᱣ ᱢᱮ",
        "Tap a card to count" to "ᱞᱮᱠᱷᱟ ᱞᱟᱹᱜᱤᱫ ᱠᱟᱨᱰ ᱴᱮᱯ ᱢᱮ", "All adhkar completed" to "ᱡᱷᱚᱛᱚ ᱟᱡᱠᱟᱨ ᱯᱩᱨᱟᱹᱣᱮᱱᱟ",
        "Tap the circle to count" to "ᱞᱮᱠᱷᱟ ᱞᱟᱹᱜᱤᱫ ᱜᱩᱞᱤ ᱴᱮᱯ ᱢᱮ", "Reset count?" to "ᱞᱮᱠᱷᱟ ᱨᱤᱥᱮᱴ ᱢᱮ?"
    ),
    "sd" to mapOf(
        "Search duas" to "دعائون ڳوليو", "Clear search" to "ڳولا صاف ڪريو",
        "No duas found" to "ڪا به دعا نه ملي", "Copy dua" to "ڪاپي ڪريو", "Share dua" to "شيئر ڪريو",
        "Copied" to "ڪاپي ٿي وئي", "Rounds" to "چڪر",
        "Pronunciation" to "تلفظ", "Text size" to "متن جي ماپ", "Choose an occasion" to "موقعو چونڊيو",
        "Tap a card to count" to "ڳڻڻ لاءِ ڪارڊ تي ٽيپ ڪريو", "All adhkar completed" to "سڀ اذڪار مڪمل",
        "Tap the circle to count" to "ڳڻڻ لاءِ دائري تي ٽيپ ڪريو", "Reset count?" to "ڳڻپ ري سيٽ ڪجي؟"
    ),
    "ta" to mapOf(
        "Search duas" to "துஆக்களைத் தேடு", "Clear search" to "தேடலை அழி",
        "No duas found" to "துஆ எதுவும் கிடைக்கவில்லை", "Copy dua" to "நகலெடு", "Share dua" to "பகிர்",
        "Copied" to "நகலெடுக்கப்பட்டது", "Rounds" to "சுற்றுகள்",
        "Pronunciation" to "உச்சரிப்பு", "Text size" to "எழுத்து அளவு", "Choose an occasion" to "சந்தர்ப்பத்தைத் தேர்ந்தெடுக்கவும்",
        "Tap a card to count" to "எண்ண கார்டைத் தட்டவும்", "All adhkar completed" to "அனைத்து அத்கார்களும் நிறைவு",
        "Tap the circle to count" to "எண்ண வட்டத்தைத் தட்டவும்", "Reset count?" to "எண்ணிக்கையை மீட்டமைக்கவா?"
    ),
    "te" to mapOf(
        "Search duas" to "దువాలను వెతకండి", "Clear search" to "శోధనను తొలగించండి",
        "No duas found" to "ఏ దువా దొరకలేదు", "Copy dua" to "కాపీ చేయండి", "Share dua" to "షేర్ చేయండి",
        "Copied" to "కాపీ అయింది", "Rounds" to "రౌండ్లు",
        "Pronunciation" to "ఉచ్చారణ", "Text size" to "అక్షర పరిమాణం", "Choose an occasion" to "సందర్భాన్ని ఎంచుకోండి",
        "Tap a card to count" to "లెక్కించడానికి కార్డ్‌పై నొక్కండి", "All adhkar completed" to "అన్ని అజ్కార్ పూర్తయ్యాయి",
        "Tap the circle to count" to "లెక్కించడానికి వృత్తంపై నొక్కండి", "Reset count?" to "లెక్కను రీసెట్ చేయాలా?"
    ),
    "ar" to mapOf(
        "Search duas" to "ابحث عن الأدعية", "Clear search" to "مسح البحث",
        "No duas found" to "لم يتم العثور على أدعية", "Copy dua" to "نسخ الدعاء", "Share dua" to "مشاركة الدعاء",
        "Copied" to "تم النسخ", "Rounds" to "جولات",
        "Pronunciation" to "النطق", "Text size" to "حجم النص", "Choose an occasion" to "اختر مناسبة",
        "Tap a card to count" to "اضغط على البطاقة للعد", "All adhkar completed" to "تم إكمال جميع الأذكار",
        "Tap the circle to count" to "اضغط على الدائرة للعد", "Reset count?" to "إعادة تعيين العدد؟"
    ),
    "fa" to mapOf(
        "Search duas" to "جستجوی دعاها", "Clear search" to "پاک کردن جستجو",
        "No duas found" to "دعایی یافت نشد", "Copy dua" to "کپی دعا", "Share dua" to "اشتراک‌گذاری دعا",
        "Copied" to "کپی شد", "Rounds" to "دورها",
        "Pronunciation" to "تلفظ", "Text size" to "اندازه متن", "Choose an occasion" to "انتخاب یک مناسبت",
        "Tap a card to count" to "برای شمارش روی کارت ضربه بزنید", "All adhkar completed" to "همه اذکار کامل شد",
        "Tap the circle to count" to "برای شمارش روی دایره ضربه بزنید", "Reset count?" to "شمارش بازنشانی شود؟"
    ),
    "tr" to mapOf(
        "Search duas" to "Duaları ara", "Clear search" to "Aramayı temizle",
        "No duas found" to "Dua bulunamadı", "Copy dua" to "Duayı kopyala", "Share dua" to "Duayı paylaş",
        "Copied" to "Kopyalandı", "Rounds" to "Turlar",
        "Pronunciation" to "Telaffuz", "Text size" to "Metin boyutu", "Choose an occasion" to "Bir durum seçin",
        "Tap a card to count" to "Saymak için karta dokunun", "All adhkar completed" to "Tüm zikirler tamamlandı",
        "Tap the circle to count" to "Saymak için daireye dokunun", "Reset count?" to "Sayaç sıfırlansın mı?"
    ),
    "id" to mapOf(
        "Search duas" to "Cari doa", "Clear search" to "Hapus pencarian",
        "No duas found" to "Doa tidak ditemukan", "Copy dua" to "Salin doa", "Share dua" to "Bagikan doa",
        "Copied" to "Disalin", "Rounds" to "Putaran",
        "Pronunciation" to "Pelafalan", "Text size" to "Ukuran teks", "Choose an occasion" to "Pilih acara",
        "Tap a card to count" to "Ketuk kartu untuk menghitung", "All adhkar completed" to "Semua dzikir selesai",
        "Tap the circle to count" to "Ketuk lingkaran untuk menghitung", "Reset count?" to "Atur ulang hitungan?"
    ),
    "fr" to mapOf(
        "Search duas" to "Rechercher des douas", "Clear search" to "Effacer la recherche",
        "No duas found" to "Aucune doua trouvée", "Copy dua" to "Copier la doua", "Share dua" to "Partager la doua",
        "Copied" to "Copié", "Rounds" to "Tours",
        "Pronunciation" to "Prononciation", "Text size" to "Taille du texte", "Choose an occasion" to "Choisir une occasion",
        "Tap a card to count" to "Touchez une carte pour compter", "All adhkar completed" to "Tous les adhkar terminés",
        "Tap the circle to count" to "Touchez le cercle pour compter", "Reset count?" to "Réinitialiser le compte ?"
    ),
    "es" to mapOf(
        "Search duas" to "Buscar súplicas", "Clear search" to "Borrar búsqueda",
        "No duas found" to "No se encontraron súplicas", "Copy dua" to "Copiar súplica", "Share dua" to "Compartir súplica",
        "Copied" to "Copiado", "Rounds" to "Rondas",
        "Pronunciation" to "Pronunciación", "Text size" to "Tamaño del texto", "Choose an occasion" to "Elegir una ocasión",
        "Tap a card to count" to "Toca una tarjeta para contar", "All adhkar completed" to "Todos los adhkar completados",
        "Tap the circle to count" to "Toca el círculo para contar", "Reset count?" to "¿Reiniciar el conteo?"
    ),
    "ru" to mapOf(
        "Search duas" to "Поиск дуа", "Clear search" to "Очистить поиск",
        "No duas found" to "Дуа не найдены", "Copy dua" to "Копировать дуа", "Share dua" to "Поделиться дуа",
        "Copied" to "Скопировано", "Rounds" to "Круги",
        "Pronunciation" to "Произношение", "Text size" to "Размер текста", "Choose an occasion" to "Выберите повод",
        "Tap a card to count" to "Нажмите на карточку для счёта", "All adhkar completed" to "Все азкары завершены",
        "Tap the circle to count" to "Нажмите на круг для счёта", "Reset count?" to "Сбросить счёт?"
    ),
    "de" to mapOf(
        "Search duas" to "Bittgebete suchen", "Clear search" to "Suche löschen",
        "No duas found" to "Keine Bittgebete gefunden", "Copy dua" to "Dua kopieren", "Share dua" to "Dua teilen",
        "Copied" to "Kopiert", "Rounds" to "Runden",
        "Pronunciation" to "Aussprache", "Text size" to "Textgröße", "Choose an occasion" to "Anlass wählen",
        "Tap a card to count" to "Zum Zählen auf eine Karte tippen", "All adhkar completed" to "Alle Adhkar abgeschlossen",
        "Tap the circle to count" to "Zum Zählen auf den Kreis tippen", "Reset count?" to "Zähler zurücksetzen?"
    ),
    "pt" to mapOf(
        "Search duas" to "Buscar súplicas", "Clear search" to "Limpar busca",
        "No duas found" to "Nenhuma súplica encontrada", "Copy dua" to "Copiar súplica", "Share dua" to "Compartilhar súplica",
        "Copied" to "Copiado", "Rounds" to "Rodadas",
        "Pronunciation" to "Pronúncia", "Text size" to "Tamanho do texto", "Choose an occasion" to "Escolher uma ocasião",
        "Tap a card to count" to "Toque em um cartão para contar", "All adhkar completed" to "Todos os adhkar concluídos",
        "Tap the circle to count" to "Toque no círculo para contar", "Reset count?" to "Redefinir contagem?"
    ),
    "zh" to mapOf(
        "Search duas" to "搜索祈祷词", "Clear search" to "清除搜索",
        "No duas found" to "未找到祈祷词", "Copy dua" to "复制祈祷词", "Share dua" to "分享祈祷词",
        "Copied" to "已复制", "Rounds" to "轮数",
        "Pronunciation" to "发音", "Text size" to "字体大小", "Choose an occasion" to "选择场合",
        "Tap a card to count" to "点击卡片计数", "All adhkar completed" to "所有赞辞已完成",
        "Tap the circle to count" to "点击圆圈计数", "Reset count?" to "重置计数？"
    ),
    "sw" to mapOf(
        "Search duas" to "Tafuta dua", "Clear search" to "Futa utafutaji",
        "No duas found" to "Hakuna dua iliyopatikana", "Copy dua" to "Nakili dua", "Share dua" to "Shiriki dua",
        "Copied" to "Imenakiliwa", "Rounds" to "Mizunguko",
        "Pronunciation" to "Matamshi", "Text size" to "Ukubwa wa Maandishi", "Choose an occasion" to "Chagua tukio",
        "Tap a card to count" to "Gusa kadi ili kuhesabu", "All adhkar completed" to "Adhkar zote zimekamilika",
        "Tap the circle to count" to "Gusa mduara ili kuhesabu", "Reset count?" to "Weka upya hesabu?"
    ),
    "ha" to mapOf(
        "Search duas" to "Bincika addu'o'i", "Clear search" to "Share bincike",
        "No duas found" to "Ba a sami addu'a ba", "Copy dua" to "Kwafi addu'a", "Share dua" to "Raba addu'a",
        "Copied" to "An Kwafa", "Rounds" to "Zagaye",
        "Pronunciation" to "Furuci", "Text size" to "Girman Rubutu", "Choose an occasion" to "Zaɓi lokaci",
        "Tap a card to count" to "Taɓa katin domin ƙidayawa", "All adhkar completed" to "An kammala duk azkari",
        "Tap the circle to count" to "Taɓa da'ira domin ƙidayawa", "Reset count?" to "Sake saita ƙidaya?"
    ),
    "ms" to mapOf(
        "Search duas" to "Cari doa", "Clear search" to "Kosongkan carian",
        "No duas found" to "Tiada doa dijumpai", "Copy dua" to "Salin doa", "Share dua" to "Kongsi doa",
        "Copied" to "Disalin", "Rounds" to "Pusingan",
        "Pronunciation" to "Sebutan", "Text size" to "Saiz Teks", "Choose an occasion" to "Pilih acara",
        "Tap a card to count" to "Ketik kad untuk mengira", "All adhkar completed" to "Semua zikir selesai",
        "Tap the circle to count" to "Ketik bulatan untuk mengira", "Reset count?" to "Tetapkan semula kiraan?"
    ),
    "it" to mapOf(
        "Search duas" to "Cerca suppliche", "Clear search" to "Cancella ricerca",
        "No duas found" to "Nessuna supplica trovata", "Copy dua" to "Copia supplica", "Share dua" to "Condividi supplica",
        "Copied" to "Copiato", "Rounds" to "Giri",
        "Pronunciation" to "Pronuncia", "Text size" to "Dimensione testo", "Choose an occasion" to "Scegli un'occasione",
        "Tap a card to count" to "Tocca una carta per contare", "All adhkar completed" to "Tutti gli adhkar completati",
        "Tap the circle to count" to "Tocca il cerchio per contare", "Reset count?" to "Reimpostare il conteggio?"
    ),
    "nl" to mapOf(
        "Search duas" to "Zoek smeekbeden", "Clear search" to "Zoekopdracht wissen",
        "No duas found" to "Geen smeekbeden gevonden", "Copy dua" to "Smeekbede kopiëren", "Share dua" to "Smeekbede delen",
        "Copied" to "Gekopieerd", "Rounds" to "Rondes",
        "Pronunciation" to "Uitspraak", "Text size" to "Tekstgrootte", "Choose an occasion" to "Kies een gelegenheid",
        "Tap a card to count" to "Tik op een kaart om te tellen", "All adhkar completed" to "Alle adhkar voltooid",
        "Tap the circle to count" to "Tik op de cirkel om te tellen", "Reset count?" to "Telling resetten?"
    ),
    "vi" to mapOf(
        "Search duas" to "Tìm kiếm lời cầu nguyện", "Clear search" to "Xóa tìm kiếm",
        "No duas found" to "Không tìm thấy lời cầu nguyện nào", "Copy dua" to "Sao chép lời cầu nguyện", "Share dua" to "Chia sẻ lời cầu nguyện",
        "Copied" to "Đã sao chép", "Rounds" to "Vòng",
        "Pronunciation" to "Cách phát âm", "Text size" to "Cỡ chữ", "Choose an occasion" to "Chọn dịp",
        "Tap a card to count" to "Chạm vào thẻ để đếm", "All adhkar completed" to "Tất cả adhkar đã hoàn thành",
        "Tap the circle to count" to "Chạm vào vòng tròn để đếm", "Reset count?" to "Đặt lại số đếm?"
    ),
    "th" to mapOf(
        "Search duas" to "ค้นหาดุอาอ์", "Clear search" to "ล้างการค้นหา",
        "No duas found" to "ไม่พบดุอาอ์", "Copy dua" to "คัดลอกดุอาอ์", "Share dua" to "แชร์ดุอาอ์",
        "Copied" to "คัดลอกแล้ว", "Rounds" to "รอบ",
        "Pronunciation" to "การออกเสียง", "Text size" to "ขนาดตัวอักษร", "Choose an occasion" to "เลือกโอกาส",
        "Tap a card to count" to "แตะการ์ดเพื่อนับ", "All adhkar completed" to "อัซการ์ทั้งหมดเสร็จสมบูรณ์",
        "Tap the circle to count" to "แตะวงกลมเพื่อนับ", "Reset count?" to "รีเซ็ตการนับ?"
    ),
    "tl" to mapOf(
        "Search duas" to "Maghanap ng dua", "Clear search" to "I-clear ang paghahanap",
        "No duas found" to "Walang nahanap na dua", "Copy dua" to "Kopyahin ang dua", "Share dua" to "Ibahagi ang dua",
        "Copied" to "Nakopya", "Rounds" to "Mga Round",
        "Pronunciation" to "Bigkas", "Text size" to "Laki ng Teksto", "Choose an occasion" to "Pumili ng okasyon",
        "Tap a card to count" to "I-tap ang card para bilangin", "All adhkar completed" to "Nakumpleto na ang lahat ng adhkar",
        "Tap the circle to count" to "I-tap ang bilog para bilangin", "Reset count?" to "I-reset ang bilang?"
    ),
    "ps" to mapOf(
        "Search duas" to "دعاګانې ولټوئ", "Clear search" to "لټون پاک کړئ",
        "No duas found" to "هېڅ دعا و نه موندل شوه", "Copy dua" to "دعا کاپي کړئ", "Share dua" to "دعا شریکه کړئ",
        "Copied" to "کاپي شو", "Rounds" to "پړاوونه",
        "Pronunciation" to "تلفظ", "Text size" to "د متن اندازه", "Choose an occasion" to "مناسبت وټاکئ",
        "Tap a card to count" to "د شمېرلو لپاره کارت کېکاږئ", "All adhkar completed" to "ټول اذکار بشپړ شول",
        "Tap the circle to count" to "د شمېرلو لپاره دایره کېکاږئ", "Reset count?" to "شمېر بیا تنظیم شي؟"
    ),
    "ja" to mapOf(
        "Search duas" to "ドゥアを検索", "Clear search" to "検索をクリア",
        "No duas found" to "ドゥアが見つかりません", "Copy dua" to "ドゥアをコピー", "Share dua" to "ドゥアを共有",
        "Copied" to "コピーしました", "Rounds" to "回数",
        "Pronunciation" to "発音", "Text size" to "文字サイズ", "Choose an occasion" to "場面を選択",
        "Tap a card to count" to "カードをタップして数える", "All adhkar completed" to "すべてのアズカールが完了しました",
        "Tap the circle to count" to "円をタップして数える", "Reset count?" to "カウントをリセットしますか？"
    ),
    "ko" to mapOf(
        "Search duas" to "두아 검색", "Clear search" to "검색 지우기",
        "No duas found" to "두아를 찾을 수 없습니다", "Copy dua" to "두아 복사", "Share dua" to "두아 공유",
        "Copied" to "복사됨", "Rounds" to "라운드",
        "Pronunciation" to "발음", "Text size" to "글자 크기", "Choose an occasion" to "상황 선택",
        "Tap a card to count" to "카드를 눌러 세기", "All adhkar completed" to "모든 아드카르 완료",
        "Tap the circle to count" to "원을 눌러 세기", "Reset count?" to "카운트를 초기화할까요?"
    ),
    "el" to mapOf(
        "Search duas" to "Αναζήτηση Ντούα", "Clear search" to "Καθαρισμός αναζήτησης",
        "No duas found" to "Δεν βρέθηκαν Ντούα", "Copy dua" to "Αντιγραφή Ντούα", "Share dua" to "Κοινοποίηση Ντούα",
        "Copied" to "Αντιγράφηκε", "Rounds" to "Γύροι",
        "Pronunciation" to "Προφορά", "Text size" to "Μέγεθος κειμένου", "Choose an occasion" to "Επιλέξτε περίσταση",
        "Tap a card to count" to "Πατήστε μια κάρτα για μέτρηση", "All adhkar completed" to "Όλα τα Άντκαρ ολοκληρώθηκαν",
        "Tap the circle to count" to "Πατήστε τον κύκλο για μέτρηση", "Reset count?" to "Επαναφορά μέτρησης;"
    ),
    "pl" to mapOf(
        "Search duas" to "Szukaj Dua", "Clear search" to "Wyczyść wyszukiwanie",
        "No duas found" to "Nie znaleziono Dua", "Copy dua" to "Kopiuj Dua", "Share dua" to "Udostępnij Dua",
        "Copied" to "Skopiowano", "Rounds" to "Rundy",
        "Pronunciation" to "Wymowa", "Text size" to "Rozmiar tekstu", "Choose an occasion" to "Wybierz okazję",
        "Tap a card to count" to "Dotknij karty, aby liczyć", "All adhkar completed" to "Wszystkie Adhkar ukończone",
        "Tap the circle to count" to "Dotknij koła, aby liczyć", "Reset count?" to "Zresetować licznik?"
    ),
    "uk" to mapOf(
        "Search duas" to "Пошук дуа", "Clear search" to "Очистити пошук",
        "No duas found" to "Дуа не знайдено", "Copy dua" to "Копіювати дуа", "Share dua" to "Поділитися дуа",
        "Copied" to "Скопійовано", "Rounds" to "Раунди",
        "Pronunciation" to "Вимова", "Text size" to "Розмір тексту", "Choose an occasion" to "Виберіть привід",
        "Tap a card to count" to "Торкніться картки, щоб рахувати", "All adhkar completed" to "Усі азкари завершено",
        "Tap the circle to count" to "Торкніться кола, щоб рахувати", "Reset count?" to "Скинути лічильник?"
    ),
    "so" to mapOf(
        "Search duas" to "Raadi ducooyin", "Clear search" to "Tirtir raadinta",
        "No duas found" to "Ducooyin lama helin", "Copy dua" to "Koobi duca", "Share dua" to "Wadaag duca",
        "Copied" to "La koobiyeeyay", "Rounds" to "Wareegyo",
        "Pronunciation" to "Ku dhawaaqista", "Text size" to "Cabbirka qoraalka", "Choose an occasion" to "Dooro munaasabad",
        "Tap a card to count" to "Taabo kaadhka si aad u tirisid", "All adhkar completed" to "Adhkaarka oo dhan waa la dhammeeyay",
        "Tap the circle to count" to "Taabo goobaha si aad u tirisid", "Reset count?" to "Dib u deji tirinta?"
    ),
    "am" to mapOf(
        "Search duas" to "ዱዓዎችን ፈልግ", "Clear search" to "ፍለጋን አጽዳ",
        "No duas found" to "ምንም ዱዓ አልተገኘም", "Copy dua" to "ዱዓ ቅዳ", "Share dua" to "ዱዓ አጋራ",
        "Copied" to "ተቀድቷል", "Rounds" to "ዙሮች",
        "Pronunciation" to "አነባበብ", "Text size" to "የጽሁፍ መጠን", "Choose an occasion" to "አጋጣሚ ይምረጡ",
        "Tap a card to count" to "ለመቁጠር ካርድ ይንኩ", "All adhkar completed" to "ሁሉም አድካር ተጠናቅቋል",
        "Tap the circle to count" to "ለመቁጠር ክቡን ይንኩ", "Reset count?" to "ቆጠራውን እንደገና ላስጀምር?"
    ),
    "uz" to mapOf(
        "Search duas" to "Dualarni qidirish", "Clear search" to "Qidiruvni tozalash",
        "No duas found" to "Dualar topilmadi", "Copy dua" to "Duani nusxalash", "Share dua" to "Duani ulashish",
        "Copied" to "Nusxalandi", "Rounds" to "Davralar",
        "Pronunciation" to "Talaffuz", "Text size" to "Matn o'lchami", "Choose an occasion" to "Vaziyatni tanlang",
        "Tap a card to count" to "Sanash uchun kartani bosing", "All adhkar completed" to "Barcha zikrlar tugallandi",
        "Tap the circle to count" to "Sanash uchun doirani bosing", "Reset count?" to "Hisobni qayta tiklashmi?"
    ),
    "he" to mapOf(
        "Search duas" to "חפש דועא", "Clear search" to "נקה חיפוש",
        "No duas found" to "לא נמצאו דועא", "Copy dua" to "העתק דועא", "Share dua" to "שתף דועא",
        "Copied" to "הועתק", "Rounds" to "סבבים",
        "Pronunciation" to "הגייה", "Text size" to "גודל טקסט", "Choose an occasion" to "בחר אירוע",
        "Tap a card to count" to "הקש על כרטיס כדי לספור", "All adhkar completed" to "כל האדכר הושלם",
        "Tap the circle to count" to "הקש על העיגול כדי לספור", "Reset count?" to "לאפס את הספירה?"
    ),
    "si" to mapOf(
        "Search duas" to "දුආ සොයන්න", "Clear search" to "සෙවීම හිස් කරන්න",
        "No duas found" to "දුආ හමු නොවීය", "Copy dua" to "දුආ පිටපත් කරන්න", "Share dua" to "දුආ බෙදාගන්න",
        "Copied" to "පිටපත් කරන ලදී", "Rounds" to "වටයන්",
        "Pronunciation" to "උච්චාරණය", "Text size" to "අකුරු ප්‍රමාණය", "Choose an occasion" to "අවස්ථාවක් තෝරන්න",
        "Tap a card to count" to "ගණන් කිරීමට කාඩ්පත ස්පර්ශ කරන්න", "All adhkar completed" to "සියලුම අද්කාර් සම්පූර්ණයි",
        "Tap the circle to count" to "ගණන් කිරීමට කවය ස්පර්ශ කරන්න", "Reset count?" to "ගණන යළි සකසන්නද?"
    )
)

/**
 * Hadith grading terms, written in each language's script. Kept out of the phrase packs on
 * purpose: as phrase keys they would also rewrite "Sahih" inside "Sahih Muslim" and other text.
 */
internal val hadithGradeWords: Map<String, Map<String, String>> = mapOf(
    "hinglish" to mapOf("Sahih" to "Sahih", "Hasan" to "Hasan", "Mauqoof" to "Mauqoof"),
    "hi" to mapOf("Sahih" to "सहीह", "Hasan" to "हसन", "Mauqoof" to "मौक़ूफ़"),
    "ur" to mapOf("Sahih" to "صحیح", "Hasan" to "حسن", "Mauqoof" to "موقوف"),
    "as" to mapOf("Sahih" to "ছহীহ", "Hasan" to "হাছান", "Mauqoof" to "মৌকুফ"),
    "bn" to mapOf("Sahih" to "সহীহ", "Hasan" to "হাসান", "Mauqoof" to "মাওকুফ"),
    "brx" to mapOf("Sahih" to "सहीह", "Hasan" to "हसन", "Mauqoof" to "मौकुफ"),
    "doi" to mapOf("Sahih" to "सहीह", "Hasan" to "हसन", "Mauqoof" to "मौकूफ"),
    "gu" to mapOf("Sahih" to "સહીહ", "Hasan" to "હસન", "Mauqoof" to "મૌકૂફ"),
    "kn" to mapOf("Sahih" to "ಸಹೀಹ್", "Hasan" to "ಹಸನ್", "Mauqoof" to "ಮೌಕೂಫ್"),
    "ks" to mapOf("Sahih" to "صحیح", "Hasan" to "حسن", "Mauqoof" to "موقوف"),
    "kok" to mapOf("Sahih" to "सहीह", "Hasan" to "हसन", "Mauqoof" to "मौकूफ"),
    "mai" to mapOf("Sahih" to "सहीह", "Hasan" to "हसन", "Mauqoof" to "मौकूफ"),
    "ml" to mapOf("Sahih" to "സഹീഹ്", "Hasan" to "ഹസൻ", "Mauqoof" to "മൗഖൂഫ്"),
    "mni" to mapOf("Sahih" to "সহীহ", "Hasan" to "হসন", "Mauqoof" to "মৌকুফ"),
    "mr" to mapOf("Sahih" to "सहीह", "Hasan" to "हसन", "Mauqoof" to "मौकूफ"),
    "ne" to mapOf("Sahih" to "सहीह", "Hasan" to "हसन", "Mauqoof" to "मौकुफ"),
    "or" to mapOf("Sahih" to "ସହୀହ", "Hasan" to "ହସନ", "Mauqoof" to "ମୌକୁଫ"),
    "pa" to mapOf("Sahih" to "ਸਹੀਹ", "Hasan" to "ਹਸਨ", "Mauqoof" to "ਮੌਕੂਫ਼"),
    "sa" to mapOf("Sahih" to "सहीह", "Hasan" to "हसन", "Mauqoof" to "मौकूफ"),
    "sat" to mapOf("Sahih" to "ᱥᱟᱦᱤᱦ", "Hasan" to "ᱦᱟᱥᱟᱱ", "Mauqoof" to "ᱢᱟᱣᱠᱩᱯᱷ"),
    "sd" to mapOf("Sahih" to "صحيح", "Hasan" to "حسن", "Mauqoof" to "موقوف"),
    "ta" to mapOf("Sahih" to "ஸஹீஹ்", "Hasan" to "ஹஸன்", "Mauqoof" to "மவ்கூஃப்"),
    "te" to mapOf("Sahih" to "సహీహ్", "Hasan" to "హసన్", "Mauqoof" to "మౌకూఫ్"),
    "ar" to mapOf("Sahih" to "صحیح", "Hasan" to "حسن", "Mauqoof" to "موقوف"),
    "fa" to mapOf("Sahih" to "صحیح", "Hasan" to "حسن", "Mauqoof" to "موقوف"),
    "tr" to mapOf("Sahih" to "Sahih (güvenilir)", "Hasan" to "Hasen", "Mauqoof" to "Mevkuf"),
    "id" to mapOf("Sahih" to "Sahih (autentik)", "Hasan" to "Hasan (baik)", "Mauqoof" to "Mauquf"),
    "fr" to mapOf("Sahih" to "Sahih (authentique)", "Hasan" to "Hasan (bon)", "Mauqoof" to "Mawquf"),
    "es" to mapOf("Sahih" to "Sahih (auténtico)", "Hasan" to "Hasan (bueno)", "Mauqoof" to "Mauquf"),
    "ru" to mapOf("Sahih" to "Сахих", "Hasan" to "Хасан", "Mauqoof" to "Мавкуф"),
    "de" to mapOf("Sahih" to "Sahih (authentisch)", "Hasan" to "Hasan (gut)", "Mauqoof" to "Mauquf"),
    "pt" to mapOf("Sahih" to "Sahih (autêntico)", "Hasan" to "Hasan (bom)", "Mauqoof" to "Mauquf"),
    "zh" to mapOf("Sahih" to "健全（صحیح）", "Hasan" to "良好（حسن）", "Mauqoof" to "中断传述（موقوف）"),
    "sw" to mapOf("Sahih" to "Sahihi", "Hasan" to "Hasani", "Mauqoof" to "Mauquf"),
    "ha" to mapOf("Sahih" to "Sahihi", "Hasan" to "Mai Kyau (Hasan)", "Mauqoof" to "Mauquf"),
    "ms" to mapOf("Sahih" to "Sahih (Autentik)", "Hasan" to "Hasan (Baik)", "Mauqoof" to "Mauquf"),
    "it" to mapOf("Sahih" to "Sahih (autentico)", "Hasan" to "Hasan (buono)", "Mauqoof" to "Mauquf"),
    "nl" to mapOf("Sahih" to "Sahih (authentiek)", "Hasan" to "Hasan (goed)", "Mauqoof" to "Mauquf"),
    "vi" to mapOf("Sahih" to "Sahih (chính xác)", "Hasan" to "Hasan (tốt)", "Mauqoof" to "Mauquf"),
    "th" to mapOf("Sahih" to "เศาะฮีห์", "Hasan" to "หะซัน", "Mauqoof" to "เมากูฟ"),
    "tl" to mapOf("Sahih" to "Sahih (Tunay)", "Hasan" to "Hasan (Mabuti)", "Mauqoof" to "Mauquf"),
    "ps" to mapOf("Sahih" to "صحیح", "Hasan" to "حسن", "Mauqoof" to "موقوف"),
    "ja" to mapOf("Sahih" to "サヒーフ（真正）", "Hasan" to "ハサン（良好）", "Mauqoof" to "マウクーフ"),
    "ko" to mapOf("Sahih" to "사히흐(정확)", "Hasan" to "하산(양호)", "Mauqoof" to "마우쿠프"),
    "el" to mapOf("Sahih" to "Σαχίχ (γνήσιο)", "Hasan" to "Χασάν (καλό)", "Mauqoof" to "Μαουκούφ"),
    "pl" to mapOf("Sahih" to "Sahih (autentyczny)", "Hasan" to "Hasan (dobry)", "Mauqoof" to "Mauquf"),
    "uk" to mapOf("Sahih" to "Сахіх (достовірний)", "Hasan" to "Хасан (добрий)", "Mauqoof" to "Мавкуф"),
    "so" to mapOf("Sahih" to "Saxiix", "Hasan" to "Xasan", "Mauqoof" to "Mawquuf"),
    "am" to mapOf("Sahih" to "ሰሒሕ (እውነተኛ)", "Hasan" to "ሐሰን (መልካም)", "Mauqoof" to "መውቁፍ"),
    "uz" to mapOf("Sahih" to "Sahih (ishonchli)", "Hasan" to "Hasan (yaxshi)", "Mauqoof" to "Mavquf"),
    "he" to mapOf("Sahih" to "סחיח (אותנטי)", "Hasan" to "חסן (טוב)", "Mauqoof" to "מאוקוף"),
    "si" to mapOf("Sahih" to "සහීහ් (සත්‍ය)", "Hasan" to "හසන් (යහපත්)", "Mauqoof" to "මව්කූෆ්")
)

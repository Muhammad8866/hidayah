package com.example.domain.model

/** Additional Quranic supplications and morning/evening remembrance, kept separate from older entries. */
internal object MoreDuas6 {
    private fun entry(
        id: String, category: String, title: String, context: String,
        arabic: String, transliteration: String, meaning: String, urdu: String,
        source: String, reference: String, grade: String, count: Int = 1
    ) = Dua(
        id = id, category = category, title = title, context = context,
        arabicText = arabic, transliteration = transliteration,
        englishTranslation = meaning, urduTranslation = urdu,
        benefit = context, sourceCollection = source, sourceReference = reference,
        authenticityGrading = grade, recommendedCount = count
    )

    val LIST = listOf(
        entry(
            "dua_q_not_among_wrongdoers", "Quranic Duas", "Do Not Place Me Among Wrongdoers",
            "Allah se zulm karne walon se alag rakhne ki dua.",
            "رَبِّ فَلَا تَجْعَلْنِي فِي الْقَوْمِ الظَّالِمِينَ",
            "Rabbi fa-la taj'alni fil-qawmiz-zalimin",
            "Mere Rab, mujhe zalim logon mein shamil na kar.",
            "اے میرے رب! مجھے ظالم لوگوں میں شامل نہ کر",
            Dua.QURAN_COLLECTION, "Surah Al-Mu'minun (23:94)", "Mutawatir (Quranic Verse)"
        ),
        entry(
            "dua_q_do_not_make_us_trial", "Quranic Duas", "Do Not Make Us a Trial",
            "Hazrat Ibrahim (AS) aur unke saathiyon ki dua.",
            "رَبَّنَا لَا تَجْعَلْنَا فِتْنَةً لِلَّذِينَ كَفَرُوا وَاغْفِرْ لَنَا رَبَّنَا إِنَّكَ أَنْتَ الْعَزِيزُ الْحَكِيمُ",
            "Rabbana la taj'alna fitnatan lilladhina kafaru waghfir lana Rabbana innaka antal-'Azizul-Hakim",
            "Aye hamare Rab, humein kafiron ke liye aazmaish na bana aur humein bakhsh de; Tu ghalib, hikmat wala hai.",
            "اے ہمارے رب! ہمیں کافروں کے لیے آزمائش نہ بنا اور ہمیں بخش دے، اے ہمارے رب! بے شک تو ہی غالب، حکمت والا ہے",
            Dua.QURAN_COLLECTION, "Surah Al-Mumtahanah (60:5)", "Mutawatir (Quranic Verse)"
        ),
        entry(
            "dua_morning_witness", "Morning & Evening", "Morning Witness to Tawhid",
            "Subah chaar martaba padhne ka zikr riwayat mein aaya hai.",
            "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
            "Allahumma inni asbahtu ushhiduka wa ushhidu hamalata 'arshika wa mala'ikataka wa jami'a khalqika annaka antallahu la ilaha illa anta wahdaka la sharika laka wa anna Muhammadan 'abduka wa rasuluka",
            "Ya Allah, main Tujhe, Arsh uthane walon, farishton aur makhluq ko gawah banata hoon ke Tu hi Allah hai aur Muhammad Tere bande aur Rasool hain.",
            "اے اللہ! میں نے صبح کی، میں تجھے، تیرے عرش اٹھانے والوں، تیرے فرشتوں اور تیری تمام مخلوق کو گواہ بناتا ہوں کہ تو ہی اللہ ہے، تیرے سوا کوئی معبود نہیں، تو اکیلا ہے، تیرا کوئی شریک نہیں، اور محمد ﷺ تیرے بندے اور رسول ہیں",
            "Hisn al-Muslim", "80; Sunan Abi Dawud 4/317", "Hasan", 4
        ),
        entry(
            "dua_evening_witness", "Morning & Evening", "Evening Witness to Tawhid",
            "Shaam chaar martaba padhne ka zikr riwayat mein aaya hai.",
            "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ وَأُشْهِدُ حَمَلَةَ عَرْشِكَ وَمَلَائِكَتَكَ وَجَمِيعَ خَلْقِكَ أَنَّكَ أَنْتَ اللَّهُ لَا إِلَهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
            "Allahumma inni amsaytu ushhiduka wa ushhidu hamalata 'arshika wa mala'ikataka wa jami'a khalqika annaka antallahu la ilaha illa anta wahdaka la sharika laka wa anna Muhammadan 'abduka wa rasuluka",
            "Ya Allah, is shaam main Tujhe, Arsh uthane walon, farishton aur makhluq ko gawah banata hoon ke Tu hi Allah hai aur Muhammad Tere bande aur Rasool hain.",
            "اے اللہ! میں نے شام کی، میں تجھے، تیرے عرش اٹھانے والوں، تیرے فرشتوں اور تیری تمام مخلوق کو گواہ بناتا ہوں کہ تو ہی اللہ ہے، تیرے سوا کوئی معبود نہیں، تو اکیلا ہے، تیرا کوئی شریک نہیں، اور محمد ﷺ تیرے بندے اور رسول ہیں",
            "Hisn al-Muslim", "80; evening wording noted there", "Hasan", 4
        ),
        entry(
            "dua_morning_blessing", "Morning & Evening", "Morning Thanks for Blessings",
            "Subah Allah ki nematon par shukr ka zikr.",
            "اللَّهُمَّ مَا أَصْبَحَ بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
            "Allahumma ma asbaha bi min ni'matin aw bi-ahadin min khalqika fa-minka wahdaka la sharika laka fa-lakal-hamdu wa lakash-shukr",
            "Ya Allah, mujhe ya Teri kisi makhluq ko jo nemat mili woh sirf Teri taraf se hai; Teri hi hamd aur shukr hai.",
            "اے اللہ! صبح کے وقت مجھے یا تیری مخلوق میں سے کسی کو جو بھی نعمت ملی، وہ صرف تیری طرف سے ہے، تیرا کوئی شریک نہیں، پس تیرے ہی لیے تعریف ہے اور تیرا ہی شکر ہے",
            "Hisn al-Muslim", "81; Sunan Abi Dawud 4/318", "Hasan"
        ),
        entry(
            "dua_evening_blessing", "Morning & Evening", "Evening Thanks for Blessings",
            "Shaam Allah ki nematon par shukr ka zikr.",
            "اللَّهُمَّ مَا أَمْسَى بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
            "Allahumma ma amsa bi min ni'matin aw bi-ahadin min khalqika fa-minka wahdaka la sharika laka fa-lakal-hamdu wa lakash-shukr",
            "Ya Allah, is shaam mujhe ya Teri kisi makhluq ko jo nemat mili woh sirf Teri taraf se hai; Teri hi hamd aur shukr hai.",
            "اے اللہ! شام کے وقت مجھے یا تیری مخلوق میں سے کسی کو جو بھی نعمت ملی، وہ صرف تیری طرف سے ہے، تیرا کوئی شریک نہیں، پس تیرے ہی لیے تعریف ہے اور تیرا ہی شکر ہے",
            "Hisn al-Muslim", "81; evening wording noted there", "Hasan"
        )
    )
}

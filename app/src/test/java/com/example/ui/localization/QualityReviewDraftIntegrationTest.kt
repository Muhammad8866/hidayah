package com.example.ui.localization

import com.example.domain.model.i18n.DuaTranslations
import org.junit.Assert.assertTrue
import org.junit.Test

class QualityReviewDraftIntegrationTest {
    @Test fun assameseCreedKeepsPassiveWorthinessOfWorship() {
        val meaning = DuaTranslations.of("as").getValue("dua_tahleel_100").meaning!!
        assertTrue(meaning.contains("উপাসনাৰ যোগ্য"))
        assertTrue(meaning.contains("কোনো অংশীদাৰ নাই"))
    }

    @Test fun malayalamCreedAffirmsOnenessAndDeniesPartners() {
        val meaning = DuaTranslations.of("ml").getValue("dua_tahleel_100").meaning!!
        assertTrue(meaning.contains("അവൻ ഏകനാണ്"))
        assertTrue(meaning.contains("പങ്കാളികളില്ല"))
    }

    @Test fun estimatedFirstFastHasTheFastingSenseInVietnamese() {
        assertTrue(localizeUiText("Estimated first fast", "vi").contains("nhịn chay"))
    }
}

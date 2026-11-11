package com.example.ui.localization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutoTranslationPhrasesTest {
    @Test fun `pack lines unescape tabs newlines and backslashes`() {
        val pack = AutoTranslationPhrases.parse("a\\tb\\nc\\\\d\tx\\ty\nPlain\tसादा\n".byteInputStream())
        assertEquals("x\ty", pack["a\tb\nc\\d"])
        assertEquals("सादा", pack["Plain"])
    }

    @Test fun `malformed lines are skipped and do not break the pack`() {
        val pack = AutoTranslationPhrases.parse("no separator\n\n\tempty key\nOk\tठीक\ntrailing\\".byteInputStream())
        assertEquals(mapOf("Ok" to "ठीक"), pack)
    }

    @Test fun `unknown language returns null instead of throwing`() {
        assertNull(AutoTranslationPhrases.lookup("Home", "zz_missing"))
        assertNull(AutoTranslationPhrases.lookup("Home", "../../secret"))
    }
}

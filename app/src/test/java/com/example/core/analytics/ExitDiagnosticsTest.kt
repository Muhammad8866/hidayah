package com.example.core.analytics

import com.example.core.analytics.ExitDiagnostics.ExitRecord
import org.junit.Assert.*
import org.junit.Test

class ExitDiagnosticsTest {
    // wakeLabel

    @Test fun `wake label keeps the component and the last part of the action`() {
        assertEquals("alarm_restore:TIME_SET", ExitDiagnostics.wakeLabel("alarm_restore", "android.intent.action.TIME_SET"))
        assertEquals("widget:APPWIDGET_UPDATE", ExitDiagnostics.wakeLabel("widget", "android.appwidget.action.APPWIDGET_UPDATE"))
        assertEquals("azan_service:start", ExitDiagnostics.wakeLabel("azan_service", "start"))
    }

    @Test fun `wake label marks a missing or blank action`() {
        assertEquals("prayer_alarm:-", ExitDiagnostics.wakeLabel("prayer_alarm", null))
        assertEquals("prayer_alarm:-", ExitDiagnostics.wakeLabel("prayer_alarm", "   "))
        assertEquals("prayer_alarm:-", ExitDiagnostics.wakeLabel("prayer_alarm", "com.example."))
    }

    @Test fun `wake label strips characters that are not identifiers and caps its length`() {
        val label = ExitDiagnostics.wakeLabel("quran service!", "a.b.ACTION WITH spaces/and:colons")
        assertEquals("quran_service_:ACTION_WITH_spaces_and_colons", label)
        val long = ExitDiagnostics.wakeLabel("c".repeat(500), "x." + "A".repeat(500))
        assertTrue(long.length <= ExitDiagnostics.WAKE_LABEL_MAX_CHARS)
        assertTrue(long.startsWith("ccc"))
    }

    @Test fun `wake label survives an empty component`() {
        assertEquals("-:TIME_SET", ExitDiagnostics.wakeLabel("", "android.intent.action.TIME_SET"))
    }

    // processSummary

    @Test fun `process summary carries the wake source and the startup phase`() {
        assertEquals("w=widget:TIME_SET;p=profile_synced", ExitDiagnostics.processSummary("widget:TIME_SET", "profile_synced"))
        assertEquals("w=-;p=app_create", ExitDiagnostics.processSummary(null, "app_create"))
        assertEquals("w=-;p=-", ExitDiagnostics.processSummary(null, null))
    }

    @Test fun `process summary always fits the system limit once truncated`() {
        val summary = ExitDiagnostics.processSummary("w".repeat(400), "p".repeat(400))
        assertTrue(ExitDiagnostics.truncateUtf8(summary, ExitDiagnostics.SUMMARY_MAX_BYTES).size <= 128)
        assertEquals(128, ExitDiagnostics.SUMMARY_MAX_BYTES)
    }

    // truncateUtf8

    @Test fun `short text is encoded unchanged`() {
        assertArrayEquals("w=widget:-".toByteArray(Charsets.UTF_8), ExitDiagnostics.truncateUtf8("w=widget:-", 128))
    }

    @Test fun `long ascii text is cut at the byte limit`() {
        assertEquals(128, ExitDiagnostics.truncateUtf8("a".repeat(300), 128).size)
    }

    @Test fun `a multi byte character on the boundary is dropped whole, never split`() {
        // "é" is 2 bytes, "ह" is 3 bytes, the emoji is 4 bytes (a surrogate pair).
        val bytes = ExitDiagnostics.truncateUtf8("a".repeat(127) + "é", 128)
        assertEquals(127, bytes.size)
        assertEquals("a".repeat(127), String(bytes, Charsets.UTF_8))
        val hindi = ExitDiagnostics.truncateUtf8("ह".repeat(50), 128)
        assertEquals(126, hindi.size)
        assertEquals("ह".repeat(42), String(hindi, Charsets.UTF_8))
        val emoji = ExitDiagnostics.truncateUtf8("ab" + "😀".repeat(40), 9)
        assertEquals("ab" + "😀", String(emoji, Charsets.UTF_8))
    }

    @Test fun `zero or negative limits give an empty array instead of throwing`() {
        assertEquals(0, ExitDiagnostics.truncateUtf8("abc", 0).size)
        assertEquals(0, ExitDiagnostics.truncateUtf8("abc", -5).size)
        assertEquals(0, ExitDiagnostics.truncateUtf8("", 128).size)
    }

    @Test fun `a lone surrogate does not break truncation`() {
        val bytes = ExitDiagnostics.truncateUtf8("ab\uD800cd", 128)
        assertTrue(bytes.size <= 128)
        assertTrue(String(bytes, Charsets.UTF_8).startsWith("ab"))
    }

    // decodeSummary

    @Test fun `summary bytes decode back to text`() {
        assertEquals("w=widget:TIME_SET;p=profile_synced",
            ExitDiagnostics.decodeSummary("w=widget:TIME_SET;p=profile_synced".toByteArray(Charsets.UTF_8)))
    }

    @Test fun `missing or empty summary decodes to null`() {
        assertNull(ExitDiagnostics.decodeSummary(null))
        assertNull(ExitDiagnostics.decodeSummary(ByteArray(0)))
    }

    @Test fun `garbage summary bytes decode safely without control characters`() {
        val decoded = ExitDiagnostics.decodeSummary(byteArrayOf(0xFF.toByte(), 0x0A, 0x41, 0x00, 0x42))
        assertNotNull(decoded)
        assertFalse(decoded!!.any { it.isISOControl() })
        assertTrue(decoded.contains("A"))
    }

    // newAnrExits

    private fun exit(timestamp: Long, description: String? = "Broadcast of Intent { act=android.intent.action.TIME_SET }") =
        ExitRecord(timestamp, description, importance = 400, summary = "w=alarm_restore:TIME_SET;p=profile_synced")

    @Test fun `only exits newer than the marker are new, oldest first`() {
        val exits = listOf(exit(300), exit(100), exit(200))
        assertEquals(listOf(200L, 300L), ExitDiagnostics.newAnrExits(exits, lastReported = 100).map { it.timestamp })
    }

    @Test fun `first run with no marker reports every retained exit`() {
        assertEquals(listOf(5L, 9L), ExitDiagnostics.newAnrExits(listOf(exit(9), exit(5)), lastReported = 0).map { it.timestamp })
    }

    @Test fun `equal timestamps newer than the marker are both kept and the marker itself is not`() {
        val fresh = ExitDiagnostics.newAnrExits(listOf(exit(50), exit(50), exit(40)), lastReported = 40)
        assertEquals(listOf(50L, 50L), fresh.map { it.timestamp })
    }

    @Test fun `nothing is new for an empty list or a marker newer than every exit`() {
        assertTrue(ExitDiagnostics.newAnrExits(emptyList(), lastReported = 0).isEmpty())
        assertTrue(ExitDiagnostics.newAnrExits(listOf(exit(10), exit(20)), lastReported = 99).isEmpty())
    }

    @Test fun `exits with a nonsensical timestamp are ignored`() {
        assertEquals(listOf(7L), ExitDiagnostics.newAnrExits(listOf(exit(-1), exit(0), exit(7)), lastReported = -10).map { it.timestamp })
    }

    // formatAnrReport

    @Test fun `report names the system reason, wake source, importance and time`() {
        val report = ExitDiagnostics.formatAnrReport(exit(1_762_581_141_000))
        assertTrue(report.contains("Broadcast of Intent { act=android.intent.action.TIME_SET }"))
        assertTrue(report.contains("w=alarm_restore:TIME_SET;p=profile_synced"))
        assertTrue(report.contains("importance=400"))
        assertTrue(report.contains("1762581141000"))
    }

    @Test fun `report truncates a long description and flattens line breaks`() {
        val report = ExitDiagnostics.formatAnrReport(exit(1, "line one\nline two\t" + "x".repeat(1_000)))
        assertFalse(report.contains('\n'))
        assertFalse(report.contains('\t'))
        assertTrue(report.contains("line one line two"))
        assertTrue(report.length < ExitDiagnostics.DESCRIPTION_MAX_CHARS + 200)
    }

    @Test fun `report copes with a missing description and summary`() {
        val report = ExitDiagnostics.formatAnrReport(ExitRecord(5, null, importance = 100, summary = null))
        assertTrue(report.contains("reason=-"))
        assertTrue(report.contains("wake=-"))
    }

    // anrSubject: the "Subject:" line at the head of the ANR trace names the timed-out component

    private val traceHead = """
        ----- pid 12923 at 2026-11-08 10:22:21.123456789+0530 -----
        Subject: Broadcast of Intent { act=android.intent.action.TIME_SET flg=0x25200010 cmp=com.aistudio.deencompanion.quranj/com.example.prayer.PrayerAlarmRestoreReceiver }
        Build fingerprint: 'vivo/PD2432F/V2432:15/AP3A.240905.015.A2/compiler:user/release-keys'
    """.trimIndent()

    @Test fun `subject is read from among the other trace lines`() {
        assertEquals(
            "Broadcast of Intent { act=android.intent.action.TIME_SET flg=0x25200010 cmp=com.aistudio.deencompanion.quranj/com.example.prayer.PrayerAlarmRestoreReceiver }",
            ExitDiagnostics.anrSubject(traceHead)
        )
    }

    @Test fun `subject is found when it is not on the first line`() {
        val head = "line one\nline two\n\nSubject: executing service com.aistudio.deencompanion.quranj/com.example.azan.AzanAlertService\nmore"
        assertEquals("executing service com.aistudio.deencompanion.quranj/com.example.azan.AzanAlertService", ExitDiagnostics.anrSubject(head))
    }

    @Test fun `subject tolerates crlf line ends and leading spaces`() {
        val head = "----- pid 1 -----\r\n   Subject:   Input dispatching timed out (Application does not have a focused window)  \r\nBuild: x\r\n"
        assertEquals("Input dispatching timed out (Application does not have a focused window)", ExitDiagnostics.anrSubject(head))
    }

    @Test fun `only the first subject line is used`() {
        assertEquals("first", ExitDiagnostics.anrSubject("Subject: first\nSubject: second"))
    }

    @Test fun `no subject line, null or empty input gives null`() {
        assertNull(ExitDiagnostics.anrSubject("----- pid 1 -----\nBuild fingerprint: x\nsubjects: lowercase does not count"))
        assertNull(ExitDiagnostics.anrSubject(null))
        assertNull(ExitDiagnostics.anrSubject(""))
    }

    @Test fun `a blank subject gives null`() {
        assertNull(ExitDiagnostics.anrSubject("Subject:   \nBuild: x"))
        assertNull(ExitDiagnostics.anrSubject("Subject:"))
    }

    @Test fun `a very long subject is capped and stays on one line`() {
        val subject = ExitDiagnostics.anrSubject("Subject: Broadcast of Intent { act=" + "A\tB".repeat(500) + " }\nnext")
        assertNotNull(subject)
        assertEquals(ExitDiagnostics.DESCRIPTION_MAX_CHARS, subject!!.length)
        assertFalse(subject.any { it.isISOControl() })
        assertTrue(subject.startsWith("Broadcast of Intent { act=A B"))
    }

    @Test fun `report carries the trace subject when there is one`() {
        val record = ExitRecord(9, "bg anr", importance = 400, summary = "w=prayer_alarm:-;p=profile_synced",
            subject = "Broadcast of Intent { act=com.example.PRAYER cmp=x/com.example.prayer.PrayerNotificationReceiver }")
        val report = ExitDiagnostics.formatAnrReport(record)
        assertTrue(report.contains("reason=bg anr"))
        assertTrue(report.contains("subject=Broadcast of Intent { act=com.example.PRAYER cmp=x/com.example.prayer.PrayerNotificationReceiver }"))
        assertFalse(report.contains('\n'))
    }

    @Test fun `report without a subject is unchanged`() {
        val withoutSubject = ExitRecord(9, "bg anr", importance = 400, summary = "w=-;p=-")
        val report = ExitDiagnostics.formatAnrReport(withoutSubject)
        assertEquals("prior_anr time=9 importance=400 wake=w=-;p=- reason=bg anr", report)
        assertFalse(report.contains("subject="))
        assertEquals(report, ExitDiagnostics.formatAnrReport(withoutSubject.copy(subject = "   ")))
    }

    // startup phase token

    @Test fun `phase token keeps identifier characters only`() {
        assertEquals("profile_synced", ExitDiagnostics.token("profile_synced"))
        assertEquals("app_create_", ExitDiagnostics.token("app create!"))
        assertEquals("-", ExitDiagnostics.token(""))
    }
}

package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.example.core.net.StreamPlayer
import com.example.ui.components.OnReconnect
import com.example.ui.components.rememberIsOnline
import com.example.ui.localization.LocalizedText
import com.example.ui.tour.tourTarget
import com.example.ui.localization.localizedUiText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

/**
 * Recorded recitations for the Manzil / Ruqyah / Wazaif screen, bundled in assets/recitation_audio
 * so they play offline.
 *
 * ayah/SSSAAA.mp3 — original Mishary Rashid Alafasy recordings (everyayah.com, 128 kbps).
 * dua/<dua id>.mp3 — the Hisn al-Muslim readings published at hisnmuslim.com. Only duas whose
 * recording reads the same wording as the bundled Arabic are listed; the rest keep the device voice.
 */
internal object ManzilRuqyahAudioCatalog {
    const val QURAN_RECITER = "Mishary Rashid Alafasy"
    const val DUA_RECITER = "Recorded recitation • Hisn al-Muslim"

    private val HISN_RECORDED = setOf(
        "dua_waking_up", "dua_waking_afani", "dua_before_sleep", "dua_sleep_qina_adhab", "dua_sleep_atamana",
        "dua_bad_dream", "dua_abu_bakr", "dua_ma_qaddamtu_akhkhartu", "dua_majlis_kaffarah",
        "dua_astaghfirullah_atubu", "dua_salawat", "dua_tahleel_100", "dua_beneficial_knowledge",
        "dua_visiting_sick", "dua_seeing_afflicted", "dua_fear_people", "dua_shirk_protection",
        "dua_adhan_response", "dua_adhan_shahadah", "dua_after_eating", "dua_after_rain",
        "dua_after_salah_istighfar", "dua_after_witr", "dua_against_enemy_ahzab", "dua_anger", "dua_arafah",
        "dua_ayatul_kursi", "dua_between_sajdas", "dua_breaking_fast", "dua_condolence",
        "dua_difficulty_sahl", "dua_dogs_barking", "dua_dressing", "dua_drinking_milk", "dua_entering_home",
        "dua_entering_market", "dua_entering_mosque", "dua_entering_town", "dua_evil_eye",
        "dua_farewell_traveller", "dua_for_one_i_cursed", "dua_for_who_gave_drink", "dua_four_beloved_words",
        "dua_hamdan_kathiran", "dua_hasbunallah", "dua_host", "dua_iftar_host", "dua_iftar_ibn_umar",
        "dua_ikfinihim", "dua_ilbas_jadidan", "dua_intimacy", "dua_ism_azam_ahad_samad", "dua_istiftah",
        "dua_istisqa", "dua_jamarat", "dua_jannah_nar", "dua_la_bas_tahur", "dua_la_hawla_kanz",
        "dua_la_mania", "dua_leaving_home", "dua_musibah", "dua_new_clothes", "dua_newlywed",
        "dua_non_muslim_sneezes", "dua_qadarullah", "dua_rabbana_atina", "dua_rafiq_ala", "dua_rain",
        "dua_reply_ahl_kitab_salam", "dua_ruku", "dua_ruku_sujood_subhanaka", "dua_safa_marwa",
        "dua_seeing_first_fruit", "dua_sell_buy_debt_repaid", "dua_sneezing", "dua_subbuhun_quddus",
        "dua_sujood", "dua_sujood_dhanbi_kullah", "dua_sujood_laka_sajadtu", "dua_sujood_ridaka",
        "dua_thanking", "dua_thunder", "dua_toilet_enter", "dua_toilet_exit", "dua_traveller_to_resident",
        "dua_two_light_words", "dua_undressing", "dua_vehicle_stumbles", "dua_waswasa", "dua_waswasa_salah",
        "dua_wind", "dua_wudu_before", "dua_yamani_hajar", "dua_yunus",
        "dua_allahumma_bika_asbahna", "dua_anxiety_distress", "dua_bismillah_la_yadurru", "dua_hasbiyallah", "dua_morning_blessing",
        "dua_morning_witness", "dua_raditu_billahi", "dua_rahmataka_arju", "dua_subhanallah_adada_khalqihi", "dua_subhanallah_wa_bihamdihi",
        "dua_talqeen", "dua_ya_hayyu_aslih", "dua_zawwadakallah",
        "dua_debt", "dua_hamm_ibn_masud", "dua_istisqa_ibadak", "dua_karb", "dua_leaving_home_adilla",
        "dua_after_salah_help_dhikr", "dua_allahu_rabbi", "dua_inni_saim", "dua_love_for_allah",
        "dua_rabba_jibrail", "dua_talbiyah", "dua_tashahhud", "dua_traveller_sahar"
    )

    /** Duas that are a Quran ayah, played from the ayah they cite. */
    private val DUA_AYAHS = mapOf(
        "dua_adam_forgiveness" to RecitationPassage(7, 23, 23),
        "dua_rabbighfir_warham" to RecitationPassage(23, 118, 118),
        "dua_ibrahim_prayer_family" to RecitationPassage(14, 40, 41),
        "dua_la_tuzigh" to RecitationPassage(3, 8, 8),
        "dua_q_jamiun_nas" to RecitationPassage(3, 9, 9),
        "dua_q_hawariyyun" to RecitationPassage(3, 53, 53),
        "dua_q_sami_na_munadiyan" to RecitationPassage(3, 193, 193),
        "dua_q_atina_ma_waadtana" to RecitationPassage(3, 194, 194),
        "dua_q_muslimayni_laka" to RecitationPassage(2, 128, 128),
        "dua_q_not_among_wrongdoers" to RecitationPassage(23, 94, 94),
        "dua_q_hab_li_hukman" to RecitationPassage(26, 83, 85),
        "dua_q_la_tukhzini" to RecitationPassage(26, 87, 87),
        "dua_q_lut_najjini_ahli" to RecitationPassage(26, 169, 169),
        "dua_q_hab_li_salihin" to RecitationPassage(37, 100, 100),
        "dua_q_do_not_make_us_trial" to RecitationPassage(60, 5, 5),
        "dua_musa_sharh_sadr" to RecitationPassage(20, 25, 28),
        "dua_shaitan_hamazat" to RecitationPassage(23, 97, 98),
        "dua_q_la_tajalna_fitnah" to RecitationPassage(10, 85, 86),
        "dua_q_nuh_ma_laysa_li" to RecitationPassage(11, 47, 47),
        "dua_q_mudkhala_sidq" to RecitationPassage(17, 80, 80),
        "dua_q_munzalan_mubarakan" to RecitationPassage(23, 29, 29),
        "dua_q_lut_unsurni" to RecitationPassage(29, 30, 30),
        "dua_q_la_akuna_zahiran" to RecitationPassage(28, 17, 17),
        "dua_q_nuh_unsurni" to RecitationPassage(23, 26, 26),
        "dua_q_ihkum_bil_haqq" to RecitationPassage(21, 112, 112),
        "dua_q_musa_li_akhi" to RecitationPassage(7, 151, 151)
    )

    /**
     * Duas that are only part of an ayah, played from the dua's first word to its last. The times
     * are quran.com's word timings for these same Alafasy files, with a short margin.
     */
    private val CLIPPED_DUA_AYAHS = mapOf(
        "dua_family_coolness" to ClippedPassage(25, AyahClip(74, startMs = 2705)),
        "dua_righteous_children" to ClippedPassage(3, AyahClip(38, startMs = 5345)),
        "dua_rabbi_zidni_ilma" to ClippedPassage(20, AyahClip(114, startMs = 14335)),
        "dua_la_tuakhidhna" to ClippedPassage(2, AyahClip(286, startMs = 9565)),
        "dua_boarding_ship" to ClippedPassage(11, AyahClip(41, startMs = 2615)),
        "dua_parents_nuh" to ClippedPassage(71, AyahClip(28, endMs = 10990)),
        "dua_shukr_parents" to ClippedPassage(46, AyahClip(15, startMs = 30705)),
        "dua_sabr" to ClippedPassage(2, AyahClip(250, startMs = 5425)),
        "dua_rabbana_innana_amanna" to ClippedPassage(3, AyahClip(16, startMs = 2635)),
        "dua_rabbana_hab_lana_rahmah" to ClippedPassage(18, AyahClip(10, startMs = 3965)),
        "dua_musa_faqir" to ClippedPassage(28, AyahClip(24, startMs = 7765)),
        "dua_ayyub" to ClippedPassage(21, AyahClip(83, startMs = 5065)),
        "dua_husn_khatimah" to ClippedPassage(12, AyahClip(101, startMs = 11435)),
        "dua_q_israfana" to ClippedPassage(3, AyahClip(147, startMs = 6445)),
        "dua_q_maash_shahidin" to ClippedPassage(5, AyahClip(83, startMs = 16685)),
        "dua_q_la_tajalna_zalimin" to ClippedPassage(7, AyahClip(47, startMs = 10615)),
        "dua_q_tawaffana_muslimin" to ClippedPassage(7, AyahClip(126, startMs = 18055)),
        "dua_q_la_tadharni_fardan" to ClippedPassage(21, AyahClip(89, startMs = 5145)),
        "dua_q_amanna_faghfir" to ClippedPassage(23, AyahClip(109, startMs = 6765)),
        "dua_q_isrif_jahannam" to ClippedPassage(25, AyahClip(65, startMs = 2645), AyahClip(66)),
        "dua_q_sulaiman_awzini" to ClippedPassage(27, AyahClip(19, startMs = 6445)),
        "dua_q_musa_zalamtu" to ClippedPassage(28, AyahClip(16, startMs = 685, endMs = 6010)),
        "dua_q_musa_najjini" to ClippedPassage(28, AyahClip(21, startMs = 7785)),
        "dua_q_wasita_kulla_shay" to ClippedPassage(40, AyahClip(7, startMs = 13875)),
        "dua_q_ikhwanina" to ClippedPassage(59, AyahClip(10, startMs = 7755)),
        "dua_q_alayka_tawakkalna" to ClippedPassage(60, AyahClip(4, startMs = 51805), AyahClip(5)),
        "dua_q_atmim_nurana" to ClippedPassage(66, AyahClip(8, startMs = 59725)),
        "dua_q_asiya_baytan" to ClippedPassage(66, AyahClip(11, startMs = 7285)),
        "dua_q_iftah_baynana" to ClippedPassage(7, AyahClip(89, startMs = 34115)),
        "dua_q_ufawwidu_amri" to ClippedPassage(40, AyahClip(44, startMs = 5655)),
        "dua_parents" to ClippedPassage(17, AyahClip(24, startMs = 5325)),
        "dua_ibrahim_children_protection" to ClippedPassage(14, AyahClip(35, startMs = 2815)),
        "dua_q_ma_khalaqta_batila" to ClippedPassage(3, AyahClip(191, startMs = 17535)),
        "dua_q_aqraba_rashada" to ClippedPassage(18, AyahClip(24, startMs = 11835)),
        "dua_travel" to ClippedPassage(43, AyahClip(13, startMs = 13065), AyahClip(14)),
        "dua_rabbana_taqabbal" to ClippedPassage(2, AyahClip(127, startMs = 6955), AyahClip(128, startMs = 16955)),
        "dua_q_ashku_bathhi" to ClippedPassage(12, AyahClip(86, startMs = 605, endMs = 9880)),
        "dua_q_khayrun_hafiza" to ClippedPassage(12, AyahClip(64, startMs = 13065)),
        "dua_q_sawa_as_sabil" to ClippedPassage(28, AyahClip(22, startMs = 6615))
    )

    /** The three Quls, played from their bundled ayahs. */
    private val MUAWWIDHAT = listOf(RecitationPassage(112, 1, 4), RecitationPassage(113, 1, 5), RecitationPassage(114, 1, 6))

    private val bundledAyahs: Set<Pair<Int, Int>> by lazy {
        (RecitationCollection.manzil + RecitationCollection.ruqyah + DUA_AYAHS.values)
            .flatMap { passage -> (passage.first..passage.last).map { passage.surah to it } }.toSet() +
            CLIPPED_DUA_AYAHS.values.flatMap { passage -> passage.clips.map { passage.surah to it.ayah } }
    }

    /** The bundled recording of an ayah, or the stream for ayahs that are not bundled. */
    fun ayahUri(surah: Int, ayah: Int): String =
        if (surah to ayah in bundledAyahs) "asset:///recitation_audio/ayah/%03d%03d.mp3".format(Locale.ROOT, surah, ayah)
        else "https://everyayah.com/data/Alafasy_128kbps/%03d%03d.mp3".format(Locale.ROOT, surah, ayah)

    /** The Rabbana duas by "rabbana_<number>", each played from the ayahs its reference cites. */
    private val RABBANA_AYAHS: Map<String, RecitationPassage> by lazy {
        com.example.names.RabbanaDuas.duas.mapNotNull { dua ->
            Regex("""(\d+):(\d+)(?:-(\d+))?$""").find(dua.reference)?.destructured?.let { (surah, first, last) ->
                "rabbana_${dua.number}" to RecitationPassage(surah.toInt(), first.toInt(), (last.ifEmpty { first }).toInt())
            }
        }.toMap()
    }

    /** What to play for [duaId] in order, or null when the dua has no recorded recitation. */
    fun duaUris(duaId: String): List<String>? = when {
        duaId in HISN_RECORDED -> listOf("asset:///recitation_audio/dua/$duaId.mp3")
        duaId in DUA_AYAHS -> recitationPlaybackUris(DUA_AYAHS.getValue(duaId))
        duaId in CLIPPED_DUA_AYAHS -> CLIPPED_DUA_AYAHS.getValue(duaId).let { passage ->
            passage.clips.map { clip -> ayahUri(passage.surah, clip.ayah) + clip.fragment() }
        }
        duaId in RABBANA_AYAHS -> recitationPlaybackUris(RABBANA_AYAHS.getValue(duaId))
        duaId == "dua_muawwidhat" -> MUAWWIDHAT.flatMap(::recitationPlaybackUris)
        else -> null
    }

    fun hasDuaRecording(duaId: String) = duaUris(duaId) != null

    fun duaReciter(duaId: String) =
        if (duaId in DUA_AYAHS || duaId in CLIPPED_DUA_AYAHS || duaId in RABBANA_AYAHS || duaId == "dua_muawwidhat") QURAN_RECITER else DUA_RECITER
}

/** One ayah of a [ClippedPassage]; a null bound plays from the ayah's start or to its end. */
internal data class AyahClip(val ayah: Int, val startMs: Long? = null, val endMs: Long? = null) {
    /** A media fragment ("#t=start,end" in ms) that [duaMediaItem] reads back. */
    fun fragment() = if (startMs == null && endMs == null) "" else "#t=${startMs ?: 0},${endMs ?: ""}"
}

internal class ClippedPassage(val surah: Int, vararg clips: AyahClip) { val clips = clips.toList() }

/** A player item for one of [ManzilRuqyahAudioCatalog.duaUris], clipped when it carries a "#t=" fragment. */
internal fun duaMediaItem(uri: String, mediaId: String = ""): MediaItem {
    val builder = MediaItem.Builder().setUri(uri.substringBefore("#t=")).setMediaId(mediaId)
    val times = uri.substringAfter("#t=", "").takeIf { it.isNotEmpty() }?.split(",") ?: return builder.build()
    val clipping = MediaItem.ClippingConfiguration.Builder()
        .setStartPositionMs(times[0].toLong())
    times.getOrNull(1)?.takeIf { it.isNotEmpty() }?.let { clipping.setEndPositionMs(it.toLong()) }
    return builder.setClippingConfiguration(clipping.build()).build()
}

/** Like [recitationAudioUrls], but reads the copies bundled with the app when there are any. */
internal fun recitationPlaybackUris(passage: RecitationPassage): List<String> =
    (passage.first..passage.last).map { ManzilRuqyahAudioCatalog.ayahUri(passage.surah, it) }

internal data class RecordedDuaState(val id: String? = null, val playing: Boolean = false,
    val failed: Boolean = false, val slow: Boolean = false, val loading: Boolean = false,
    val repeat: Boolean = false)

/** Plays the recorded recitation of one dua at a time. */
internal class RecordedDuaAudio(context: Context) {
    val state = MutableStateFlow(RecordedDuaState())
    private val engine = lazy { StreamPlayer.build(context.applicationContext).apply {
        setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (state.value.id == null || state.value.failed) return
                if (player.playbackState == Player.STATE_READY) audioLoad?.ready()
                if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) && player.playbackState == Player.STATE_ENDED) {
                    com.example.core.analytics.AppAnalytics.mediaState("recorded_dua_audio", "completed")
                    state.value = state.value.copy(id = null)
                    stop()
                    return
                }
                state.value = state.value.copy(playing = player.playWhenReady &&
                    player.playbackState != Player.STATE_IDLE,
                    loading = player.playbackState == Player.STATE_BUFFERING)
            }
            override fun onPlayerError(error: PlaybackException) {
                if (state.value.id == null) return
                audioLoad?.failed()
                com.example.core.analytics.AppAnalytics.mediaState("recorded_dua_audio", "failed", "playback_error")
                state.value = state.value.copy(playing = false, loading = false, failed = true)
            }
        })
    } }
    private val player get() = engine.value
    private var audioLoad: com.example.core.analytics.AppAnalytics.AudioLoad? = null

    /** Starts [duaId]; pauses or resumes it when it is already the active one. */
    fun play(duaId: String) {
        val current = state.value
        if (current.id == duaId && !current.failed) {
            com.example.core.analytics.AppAnalytics.media("recorded_dua_audio", if (player.playWhenReady) "pause" else "resume")
            if (player.playWhenReady) player.pause() else player.play()
            return
        }
        val uris = ManzilRuqyahAudioCatalog.duaUris(duaId) ?: return
        com.example.core.analytics.AppAnalytics.media("recorded_dua_audio", if (current.id == duaId) "retry" else "play")
        com.example.core.analytics.AppAnalytics.content("recorded_dua_audio", duaId, "play")
        com.example.quran.QuranAudioManager.stop()
        com.example.quran.TafsirAudioManager.stop()
        com.example.naat.NaatAudioManager.stop()
        com.example.azan.AzanAudioManager.stop()
        state.value = current.copy(id = duaId, playing = true, loading = true, failed = false)
        player.setPlaybackSpeed(if (current.slow) .75f else 1f)
        player.repeatMode = if (current.repeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        player.setMediaItems(uris.map { duaMediaItem(it) })
        audioLoad = com.example.core.analytics.AppAnalytics.audioLoad("recorded_dua_audio")
        player.prepare()
        player.play()
    }

    fun setSlow(slow: Boolean) {
        com.example.core.analytics.AppAnalytics.setting("recorded_dua_slow", slow.toString())
        state.value = state.value.copy(slow = slow)
        if (engine.isInitialized()) player.setPlaybackSpeed(if (slow) .75f else 1f)
    }

    fun setRepeat(repeat: Boolean) {
        com.example.core.analytics.AppAnalytics.setting("recorded_dua_repeat", repeat.toString())
        state.value = state.value.copy(repeat = repeat)
        if (engine.isInitialized()) player.repeatMode = if (repeat) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }

    fun stop() {
        if (state.value.id != null) com.example.core.analytics.AppAnalytics.media("recorded_dua_audio", "stop")
        // Reset first so the listener ignores the events the player emits while stopping.
        state.value = RecordedDuaState(slow = state.value.slow, repeat = state.value.repeat)
        if (engine.isInitialized()) { player.pause(); player.stop() }
    }

    fun close() { if (engine.isInitialized()) player.release() }
}

/** A [RecordedDuaAudio] that stops when the app goes to the background and is freed with the screen. */
@Composable
internal fun rememberRecordedDuaAudio(): RecordedDuaAudio {
    val context = LocalContext.current
    val audio = remember(context) { RecordedDuaAudio(context) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(audio, owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) audio.stop() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); audio.stop(); audio.close() }
    }
    return audio
}

/** Listen / pause, slow, repeat and stop for a dua that has a recorded recitation, in one compact row. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecordedDuaControls(id: String, audio: RecordedDuaAudio, modifier: Modifier = Modifier,
    onStart: () -> Unit = {}) {
    val state by audio.state.collectAsState()
    val active = state.id == id
    val isOnline by rememberIsOnline()
    // A recording that failed while offline resumes by itself when the connection returns.
    OnReconnect(enabled = active && state.failed) { audio.play(id) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledTonalButton(onClick = { if (!active) onStart(); audio.play(id) },
                modifier = Modifier.heightIn(min = 40.dp).testTag("collection_recorded_$id").tourTarget("recitation_recorded_listen"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                Icon(if (active && state.playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                LocalizedText(if (active && state.playing) "Pause" else "Listen")
            }
            FilterChip(state.slow, { audio.setSlow(!state.slow) }, label = { LocalizedText("Slow") },
                modifier = Modifier.tourTarget("recitation_recorded_slow"))
            FilterChip(state.repeat, { audio.setRepeat(!state.repeat) }, label = { LocalizedText("Repeat") },
                modifier = Modifier.tourTarget("recitation_recorded_repeat"))
            if (active) IconButton(onClick = { audio.stop() }, modifier = Modifier.tourTarget("recitation_recorded_stop")) {
                Icon(Icons.Default.Stop, contentDescription = localizedUiText("Stop"))
            }
        }
        if (active && state.loading) LinearProgressIndicator(Modifier.fillMaxWidth().height(2.dp))
        if (active && state.failed) Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            LocalizedText(if (isOnline) "Audio could not play. Try again." else "You're offline",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f))
            TextButton(onClick = { audio.play(id) }) { LocalizedText("Retry") }
        }
        LocalizedText(ManzilRuqyahAudioCatalog.duaReciter(id), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.tourTarget("recitation_recorded_reciter"))
    }
}

"""Refresh matched dua recordings from original, accessible audio sources.

Preserves each source MP3 byte for byte (no transcoding or artificial upscaling).
Only complete recordings matching the displayed wording are mapped. Longer
versions and combined duas are deliberately excluded; no guessed clip boundaries.
Run from any directory with Python + mutagen installed.
"""
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import hashlib
import json
import urllib.request
import urllib.parse

from mutagen.mp3 import MP3

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
CACHE = ROOT / "tools/audio_sources/hisnul_muslim_invocations.json"
MIRROR = "https://raw.githubusercontent.com/saliouseck2009/hisnul-muslim-mburoja-api/master/"
FRENCH = "https://www.islam-a-tous.com/wp-content/uploads/2011/07/27-invocation-Du-matin-et-du-soir-"
FRENCH_PAGE = "https://www.islam-a-tous.com/2011/07/22/invocation-du-matin-et-du-soir/"

# One whole original recording per card. Source-page numbering is different
# from the Hisn al-Muslim numbering used in the app's reference labels.
ADHKAR = {
    "dua_allahumma_bika_amsayna": (8, None),
    "dua_sayyidul_istighfar": (9, "027_06"),
    "dua_evening_witness": (11, None),
    "dua_evening_blessing": (13, None),
    "dua_fitratil_islam": (23, "027_17"),
    "dua_amsayna_fitrah": (24, None),
    "dua_kalimat_tammat": (30, "027_24"),
}
# Official hisnmuslim.com recordings whose published Arabic matches the app text word for word
# (the GitHub mirror's transcript differs for these, so they are taken from the original site).
HISNMUSLIM = "https://www.hisnmuslim.com/audio/ar/"
HISNMUSLIM_DUAS = {
    "dua_after_salah_help_dhikr": 59,
    "dua_allahu_rabbi": 125,
    "dua_inni_saim": 186,
    "dua_love_for_allah": 200,
    "dua_rabba_jibrail": 30,
    "dua_talbiyah": 233,
    "dua_tashahhud": 52,
    "dua_traveller_sahar": 215,
}
EXTRA_DUAS = {
    "dua_abu_bakr": (
        "https://www.islam-a-tous.com/wp-content/uploads/2011/07/24-invocation-Après-le-dernier-Tachahhud-juste-avant-les-salutations-03.mp3",
        "https://www.islam-a-tous.com/2011/07/22/invocation-apres-le-dernier-tachahhud-juste-avant-les-salutations/",
    ),
    "dua_salawat": (
        "https://www.islam-a-tous.com/wp-content/uploads/2011/07/23-invocation-La-prière-sur-le-Prophète-après-lattestation-de-foi-1.mp3",
        "https://www.islam-a-tous.com/2011/07/22/invocation-la-priere-sur-le-prophete-apres-lattestation-de-foi/",
    ),
}
DUAS = {
    "dua_astaghfirullah_atubu": "027_23",
    "dua_bad_dream": "042_02",
    "dua_before_sleep": "028_07",
    "dua_beneficial_knowledge": "025_10",
    "dua_fear_people": "036_01",
    "dua_majlis_kaffarah": "009_03",
    "dua_ma_qaddamtu_akhkhartu": "024_04",
    "dua_seeing_afflicted": "083_01",
    "dua_shirk_protection": "092_01",
    "dua_sleep_atamana": "028_10",
    "dua_sleep_qina_adhab": "028_06",
    "dua_tahleel_100": "027_19",
    "dua_visiting_sick": "049_02",
    "dua_waking_afani": "001_03",
    "dua_waking_up": "001_01",
    # Same Arabic letters as the app text once diacritics and punctuation are ignored.
    "dua_debt": "041_01",
    "dua_hamm_ibn_masud": "034_01",
    "dua_istisqa_ibadak": "063_03",
    "dua_karb": "035_01",
    "dua_leaving_home_adilla": "010_02",
    "dua_adhan_response": "130_06",
    "dua_adhan_shahadah": "015_02",
    "dua_after_eating": "070_01",
    "dua_after_rain": "065_01",
    "dua_after_salah_istighfar": "025_01",
    "dua_after_witr": "033_01",
    "dua_against_enemy_ahzab": "038_01",
    "dua_anger": "042_02",
    "dua_arafah": "027_19",
    "dua_ayatul_kursi": "025_08",
    "dua_between_sajdas": "020_01",
    "dua_breaking_fast": "068_01",
    "dua_condolence": "057_01",
    "dua_difficulty_sahl": "043_01",
    "dua_dogs_barking": "042_02",
    "dua_dressing": "002_01",
    "dua_drinking_milk": "069_04",
    "dua_entering_home": "005_01",
    "dua_entering_market": "098_01",
    "dua_entering_mosque": "013_03",
    "dua_entering_town": "027_24",
    "dua_evil_eye": "048_01",
    "dua_farewell_traveller": "101_01",
    "dua_for_one_i_cursed": "112_01",
    "dua_for_who_gave_drink": "072_01",
    "dua_four_beloved_words": "130_04",
    "dua_hamdan_kathiran": "018_02",
    "dua_hasbunallah": "036_03",
    "dua_host": "071_01",
    "dua_iftar_host": "073_01",
    "dua_iftar_ibn_umar": "068_02",
    "dua_ikfinihim": "039_01",
    "dua_ilbas_jadidan": "004_02",
    "dua_intimacy": "081_01",
    "dua_ism_azam_ahad_samad": "024_11",
    "dua_istiftah": "016_02",
    "dua_istisqa": "063_02",
    "dua_jamarat": "122_02",
    "dua_jannah_nar": "024_07",
    "dua_la_bas_tahur": "049_01",
    "dua_la_hawla_kanz": "130_06",
    "dua_la_mania": "025_02",
    "dua_leaving_home": "010_01",
    "dua_musibah": "053_01",
    "dua_new_clothes": "003_01",
    "dua_newlywed": "079_01",
    "dua_non_muslim_sneezes": "078_01",
    "dua_qadarullah": "046_01",
    "dua_rabbana_atina": "117_01",
    "dua_rafiq_ala": "051_01",
    "dua_rain": "064_01",
    "dua_reply_ahl_kitab_salam": "109_01",
    "dua_ruku": "017_01",
    "dua_ruku_sujood_subhanaka": "017_02",
    "dua_safa_marwa": "118_02",
    "dua_seeing_first_fruit": "076_01",
    "dua_sell_buy_debt_repaid": "090_01",
    "dua_sneezing": "077_01",
    "dua_subbuhun_quddus": "017_03",
    "dua_sujood": "019_01",
    "dua_sujood_dhanbi_kullah": "019_06",
    "dua_sujood_laka_sajadtu": "019_04",
    "dua_sujood_ridaka": "019_07",
    "dua_thanking": "087_01",
    "dua_thunder": "062_01",
    "dua_toilet_enter": "006_01",
    "dua_toilet_exit": "007_01",
    "dua_traveller_to_resident": "100_01",
    "dua_two_light_words": "130_03",
    "dua_undressing": "005_01",
    "dua_vehicle_stumbles": "005_01",
    "dua_waswasa": "040_01",
    "dua_waswasa_salah": "042_02",
    "dua_wind": "061_02",
    "dua_wudu_before": "005_01",
    "dua_yamani_hajar": "117_01",
    "dua_yunus": "035_03",
    # Morning/evening adhkar now taken from the mirror (its wording matches the app exactly).
    "dua_allahumma_bika_asbahna": "027_05",
    "dua_anxiety_distress": "034_02",
    "dua_bismillah_la_yadurru": "027_13",
    "dua_hasbiyallah": "027_10",
    "dua_morning_blessing": "027_08",
    "dua_morning_witness": "027_07",
    "dua_raditu_billahi": "027_14",
    "dua_rahmataka_arju": "035_02",
    "dua_subhanallah_adada_khalqihi": "027_21",
    "dua_subhanallah_wa_bihamdihi": "027_18",
    "dua_talqeen": "126_01",
    "dua_ya_hayyu_aslih": "027_15",
    "dua_zawwadakallah": "101_02",
}


def download(url):
    # hisnmuslim.com refuses urllib's default User-Agent.
    request = urllib.request.Request(urllib.parse.quote(url, safe=":/%"), headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(request, timeout=35) as response:
        return response.read()


def refresh(job):
    asset, urls, wording_source = job
    path = ASSETS / asset
    pending = path.with_suffix(".download.mp3")
    candidates = []
    errors = []
    for url in urls:
        try:
            data = download(url)
            pending.write_bytes(data)
            info = MP3(pending).info
            if info.length < 0.5 or info.bitrate < 32_000:
                raise ValueError("Invalid or low-quality recording")
            candidates.append((info.bitrate, info.sample_rate, data, url))
        except Exception as error:
            errors.append(f"{url}: {error}")
    if not candidates:
        pending.unlink(missing_ok=True)
        raise RuntimeError("; ".join(errors))
    # MP3 parsers can report a nominal 128 kbps as 127999 bps due to
    # frame-duration rounding. Compare nominal kbps rather than that one bit.
    bitrate, sample_rate, data, url = max(candidates, key=lambda item: (round(item[0] / 1000), item[1]))
    # Do not downgrade a superior recording already bundled with the app.
    if path.exists():
        previous = MP3(path).info
        if (round(previous.bitrate / 1000), previous.sample_rate) > (round(bitrate / 1000), sample_rate):
            pending.unlink(missing_ok=True)
            return {"asset": asset, "status": "kept higher quality existing recording"}
    pending.write_bytes(data)
    info = MP3(pending).info
    pending.replace(path)
    return dict(asset=asset, source=url, wordingSource=wording_source,
                durationSeconds=round(info.length, 3), bitrate=info.bitrate,
                sampleRate=info.sample_rate, channels=info.channels,
                sha256=hashlib.sha256(data).hexdigest())


def main():
    CACHE.parent.mkdir(parents=True, exist_ok=True)
    CACHE.write_bytes(download(MIRROR + "invocations.json"))
    jobs = []
    for dua_id, (number, mirror_id) in ADHKAR.items():
        urls = [FRENCH + f"{number:02}.mp3"]
        if mirror_id:
            urls.append(MIRROR + f"audios/{mirror_id}.mp3")
        jobs.append((f"adhkar_audio/{dua_id}.mp3", urls, FRENCH_PAGE))
    for dua_id, number in DUAS.items():
        jobs.append((f"recitation_audio/dua/{dua_id}.mp3",
                     [MIRROR + f"audios/{number}.mp3"], MIRROR + "invocations.json"))
    for dua_id, number in HISNMUSLIM_DUAS.items():
        jobs.append((f"recitation_audio/dua/{dua_id}.mp3", [HISNMUSLIM + f"{number}.mp3"],
                     "https://www.hisnmuslim.com/api/ar/husn_ar.json"))
    for dua_id, (url, page) in EXTRA_DUAS.items():
        jobs.append((f"recitation_audio/dua/{dua_id}.mp3", [url], page))
    with ThreadPoolExecutor(max_workers=4) as pool:
        records = list(pool.map(refresh, jobs))
    manifest = {
        "method": "Original source MP3s retained without transcoding",
        "recordings": records,
        "excluded": {
            "dua_afini_fi_badani": "Source contains body and kufr duas together",
            "dua_kufr_faqr": "Source contains body and kufr duas together",
            "dua_afiyah_morning": "Source contains the longer complete dua",
            "dua_alimal_ghayb": "Source includes an extra final sentence",
            "dua_morning_asbahna": "Source contains the longer complete dua",
            "dua_evening_amsayna": "Source contains the longer complete dua",
            "dua_tahleel_10_fajr": "Mirror and hisnmuslim.com transcripts disagree on yuhyi wa yumit for this recording",
        },
    }
    (ASSETS / "dua_recording_sources.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for record in records:
        print(record["asset"], record.get("bitrate", record.get("status")), record.get("durationSeconds", ""))


if __name__ == "__main__":
    main()

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
    "dua_allahumma_bika_asbahna": (7, "027_05"),
    "dua_allahumma_bika_amsayna": (8, None),
    "dua_sayyidul_istighfar": (9, "027_06"),
    "dua_morning_witness": (10, "027_07"),
    "dua_evening_witness": (11, None),
    "dua_morning_blessing": (12, "027_08"),
    "dua_evening_blessing": (13, None),
    "dua_hasbiyallah": (15, "027_10"),
    "dua_bismillah_la_yadurru": (18, "027_13"),
    "dua_raditu_billahi": (19, "027_14"),
    "dua_ya_hayyu_aslih": (20, "027_15"),
    "dua_fitratil_islam": (23, "027_17"),
    "dua_amsayna_fitrah": (24, None),
    "dua_subhanallah_wa_bihamdihi": (25, "027_18"),
    "dua_subhanallah_adada_khalqihi": (27, "027_21"),
    "dua_kalimat_tammat": (30, "027_24"),
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
}


def download(url):
    with urllib.request.urlopen(urllib.parse.quote(url, safe=":/%"), timeout=35) as response:
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
            "dua_tahleel_10_fajr": "Source lacks the app's yuhyi wa yumit phrase",
        },
    }
    (ASSETS / "dua_recording_sources.json").write_text(
        json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for record in records:
        print(record["asset"], record.get("bitrate", record.get("status")), record.get("durationSeconds", ""))


if __name__ == "__main__":
    main()

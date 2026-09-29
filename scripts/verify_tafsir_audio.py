"""Check official Sirat ul Jinan catalog coverage and remote MP3 availability."""

from concurrent.futures import ThreadPoolExecutor, as_completed
import json
from pathlib import Path
import subprocess


tracks = json.loads(Path("app/src/main/assets/quran_tafsir_sirat_ul_jinan.json").read_text(encoding="utf-8"))
assert len(tracks) == 115, len(tracks)
assert {track["surah"] for track in tracks} == set(range(1, 115))
assert len({track["url"] for track in tracks if track["url"]}) == 114
assert len([track for track in tracks if track["surah"] == 2]) == 2
assert [track for track in tracks if track["surah"] == 83][0]["page"] == "https://www.youtube.com/watch?v=zzwqw_82zHk"


def check(track):
    if not track["url"]:
        return track["surah"], "official video"
    result = subprocess.run(["curl.exe", "-sSIL", "--max-time", "20", "-o", "NUL", "-w", "%{http_code} %{content_type}", track["url"]],
                            capture_output=True, text=True)
    return track["surah"], result.stdout.strip() if result.returncode == 0 else result.stderr.strip()


with ThreadPoolExecutor(max_workers=8) as pool:
    results = sorted(future.result() for future in as_completed(pool.submit(check, track) for track in tracks))

for surah, status in results:
    if not status.startswith("200 audio/") and status != "official video":
        print(f"Surah {surah}: {status}")

good = sum(status.startswith("200 audio/") for _, status in results)
print(f"{good}/114 MP3 recordings reachable; Surah 83 uses the official video link")
assert good == 114

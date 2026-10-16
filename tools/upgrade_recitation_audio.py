"""Fetch original 128 kbps Quran recordings; never upscale compressed recordings.

Run from the repository root. Requires mutagen and imageio-ffmpeg for validation
and joining the three Quls without recompressing them.
"""
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
import hashlib
import json
import subprocess
import tempfile
import urllib.request

from mutagen.mp3 import MP3
import imageio_ffmpeg

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
SOURCE = "https://everyayah.com/data/Alafasy_128kbps/"


def fetch(path):
    url = SOURCE + path.name
    with urllib.request.urlopen(url, timeout=40) as response:
        data = response.read()
    pending = path.with_suffix(".download.mp3")
    pending.write_bytes(data)
    info = MP3(pending).info
    if info.length < .5 or info.bitrate < 120_000:
        pending.unlink()
        raise ValueError(f"Invalid original recording: {url}")
    pending.replace(path)
    return dict(asset=str(path.relative_to(ASSETS)).replace("\\", "/"), source=url,
                durationSeconds=round(info.length, 3), bitrate=info.bitrate,
                sha256=hashlib.sha256(data).hexdigest())


def main():
    with ThreadPoolExecutor(max_workers=4) as executor:
        records = list(executor.map(fetch, sorted((ASSETS / "recitation_audio/ayah").glob("*.mp3"))))
    with tempfile.TemporaryDirectory() as directory:
        temp = Path(directory)
        paths = [temp / f"{surah:03}{ayah:03}.mp3" for surah, count in [(112, 4), (113, 5), (114, 6)]
                 for ayah in range(1, count + 1)]
        with ThreadPoolExecutor(max_workers=4) as executor:
            # Temporary files are validated identically, but aren't manifest assets.
            def download(path):
                with urllib.request.urlopen(SOURCE + path.name, timeout=40) as response:
                    path.write_bytes(response.read())
                assert MP3(path).info.bitrate >= 120_000
            list(executor.map(download, paths))
        listing = temp / "concat.txt"
        listing.write_text("\n".join(f"file '{p.as_posix()}'" for p in paths), encoding="utf-8")
        target = ASSETS / "adhkar_audio/dua_muawwidhat.mp3"
        subprocess.run([imageio_ffmpeg.get_ffmpeg_exe(), "-v", "error", "-y", "-f", "concat",
                        "-safe", "0", "-i", str(listing), "-c", "copy", str(target)], check=True)
        info = MP3(target).info
        records.append(dict(asset="adhkar_audio/dua_muawwidhat.mp3", sources=[SOURCE + p.name for p in paths],
                            durationSeconds=round(info.length, 3), bitrate=info.bitrate,
                            sha256=hashlib.sha256(target.read_bytes()).hexdigest()))
    manifest = ASSETS / "recitation_audio/sources.json"
    manifest.write_text(json.dumps(dict(reciter="Mishary Rashid Alafasy", recordings=records),
                                   indent=2) + "\n", encoding="utf-8")
    print(f"Validated {len(records)} original recordings at 128 kbps; manifest: {manifest}")


if __name__ == "__main__":
    main()

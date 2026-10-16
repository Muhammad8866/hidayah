"""Validate every bundled recitation decodes and upgraded source hashes match.

Requires mutagen and imageio-ffmpeg. Run after either refresh script.
"""
from pathlib import Path
import hashlib
import json
import subprocess
import sys
import zipfile
from mutagen.mp3 import MP3
import imageio_ffmpeg

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
files = sorted((ASSETS / "adhkar_audio").glob("*.mp3")) + sorted((ASSETS / "recitation_audio").rglob("*.mp3"))
for path in files:
    info = MP3(path).info
    assert info.length > .5, f"Empty/truncated: {path}"
    subprocess.run([ffmpeg, "-v", "error", "-xerror", "-i", str(path), "-f", "null", "-"],
                   check=True, capture_output=True)
for manifest in [ASSETS / "recitation_audio/sources.json", ASSETS / "dua_recording_sources.json"]:
    document = json.loads(manifest.read_text(encoding="utf-8"))
    records = document["recordings"]
    for recording in records:
        if "sha256" not in recording:
            continue
        path = ASSETS / recording["asset"]
        assert hashlib.sha256(path.read_bytes()).hexdigest() == recording["sha256"], path
        info = MP3(path).info
        assert info.bitrate >= 120_000, f"Not original high bitrate: {path}"
print(f"PASS: {len(files)} audio assets fully decode; original-source checksums and bitrates match.")
if len(sys.argv) == 3 and sys.argv[1] == "--apk":
    with zipfile.ZipFile(sys.argv[2]) as apk:
        for path in files:
            entry = "assets/" + path.relative_to(ASSETS).as_posix()
            assert hashlib.sha256(apk.read(entry)).digest() == hashlib.sha256(path.read_bytes()).digest(), entry
    print(f"PASS: all {len(files)} verified recordings are included byte for byte in the APK.")

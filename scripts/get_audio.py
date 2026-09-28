"""Downloads the 114 Alafasy murattal surah files (the ones the word timings belong to) and verifies them."""
import hashlib, json, os, sys, urllib.request, time
from concurrent.futures import ThreadPoolExecutor

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "audio_files")
os.makedirs(OUT, exist_ok=True)
index = json.load(open(os.path.join(HERE, "out", "index.json"), encoding="utf-8"))


def fetch(n):
    url = index["audio"][n - 1][0]
    path = os.path.join(OUT, f"{n:03d}.mp3")
    for attempt in range(6):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "deen-companion-build/1.0"})
            with urllib.request.urlopen(req, timeout=120) as r:
                expected = int(r.headers["Content-Length"])
                if os.path.exists(path) and os.path.getsize(path) == expected:
                    return n, expected
                with open(path + ".part", "wb") as f:
                    while chunk := r.read(1 << 20):
                        f.write(chunk)
            if os.path.getsize(path + ".part") != expected:
                raise IOError("short read")
            os.replace(path + ".part", path)
            return n, expected
        except Exception as e:
            err = e
            time.sleep(3 + attempt * 5)
    raise RuntimeError(f"{n}: {err}")


with ThreadPoolExecutor(6) as ex:
    done = 0
    for n, size in ex.map(fetch, range(1, 115)):
        done += 1
        if done % 10 == 0:
            print(done, "/ 114", flush=True)

manifest = {}
for n in range(1, 115):
    p = os.path.join(OUT, f"{n:03d}.mp3")
    manifest[n] = [os.path.getsize(p), hashlib.sha256(open(p, "rb").read()).hexdigest()]
json.dump(manifest, open(os.path.join(HERE, "audio_manifest.json"), "w"))
print("total bytes", sum(v[0] for v in manifest.values()))

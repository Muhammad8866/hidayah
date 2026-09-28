"""Builds the bundled 15-line Tajweedi Mushaf used by the Quran Read tab.

Source: the page scans published at https://quranhost.com/reading-quran-online-juz-01 ... juz-30.

Every regular 15-line page is straightened, its 15 text rows are cut out exactly between the ruled
lines (ornamental border, margins and the tajweed legend are dropped), the ruled lines are erased
and the rows are stacked into one image upscaled to 1080 px phone width (Lanczos + light sharpening).
The other pages (tajweed guide, indexes, the ornamental Fatiha / Baqarah openings, khatam dua,
certificate) are cut into bands at their blank gaps and ruled lines instead. The app spreads the
bands of every page over the phone screen. Only the cover stays whole, on its own border colour.

Usage (Python 3 with Pillow and numpy):
    python build_pages.py [--download] <scan dir> <app assets dir>
Writes <assets>/tajweedi/pXX-YYY.webp and manifest.json (file, SHA-256 and band heights per page),
then prints the manifest SHA-256 to paste into TajweediMushaf.MANIFEST_SHA256.
"""
import hashlib, json, os, shutil, sys, urllib.request
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageFilter

JUZ_PAGES = [30] + [20] * 27 + [24, 30]
ROWS, ROW_BOX, WIDTH, QUALITY, SPECIAL_QUALITY = 15, 88, 1080, 72, 80
SPECIAL = {'p01-%03d' % i for i in range(1, 12)} | {'p30-%03d' % i for i in range(25, 31)}
COVER = 'p01-001'
# The Fatiha and Baqarah openings keep their ornamental surah header, which starts right below
# the double frame line.
TOP_OVERRIDE = {'p01-010': 104, 'p01-011': 113}
NAMES = ['p%02d-%03d' % (j + 1, p + 1) for j, n in enumerate(JUZ_PAGES) for p in range(n)]


def download(scan_dir):
    os.makedirs(scan_dir, exist_ok=True)
    for name in NAMES:
        path = os.path.join(scan_dir, name + '.jpg')
        if os.path.exists(path): continue
        req = urllib.request.Request('https://quranhost.com/uploads/quran/%s.jpg' % name, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as r, open(path, 'wb') as f: shutil.copyfileobj(r, f)


def runs(mask):
    idx = np.flatnonzero(mask)
    if idx.size == 0: return []
    out, s, p = [], idx[0], idx[0]
    for i in idx[1:]:
        if i != p + 1: out.append((s, p)); s = i
        p = i
    out.append((s, p)); return out


def skew_angle(g):
    """Rotation that makes the ruled lines horizontal (sharpest row profile)."""
    mask = Image.fromarray(((g[120:1290, 230:850] < 170) * 255).astype(np.uint8))
    def score(a):
        r = np.asarray(mask.rotate(a, resample=Image.BILINEAR, fillcolor=0), dtype=np.float32)
        return float(np.square(np.diff(r[60:-60, 60:-60].sum(axis=1))).sum())
    best = max(np.arange(-3, 3.01, 0.25), key=score)
    return round(float(max(np.arange(best - 0.25, best + 0.251, 0.02), key=score)), 2)


def frame(g):
    """Top/bottom inner frame lines that enclose 15 rows of ~80 px, and the white text box edges."""
    rowp = (g[:, 320:760] < 160).mean(axis=1)
    tops = [y for y in range(50, 175) if rowp[y] > 0.75]
    bots = [y for y in range(1225, 1385) if rowp[y] > 0.75]
    best = None
    for t in tops:
        for b in bots:
            s = (b - t) / ROWS
            if not 78.5 <= s <= 81.5: continue
            seps = sum(rowp[int(round(t + k * s)) - 3:int(round(t + k * s)) + 4].max() > 0.3 for k in range(1, ROWS))
            key = (seps, t, -b)
            if best is None or key > best[0]: best = (key, t, b)
    if best is None: return None
    _, t, b = best
    white = (g[t + 5:b - 5] > 200).mean(axis=0)
    x0 = 540
    while x0 > 0 and white[x0 - 1] > 0.45: x0 -= 1
    x1 = 540
    while x1 < g.shape[1] - 1 and white[x1 + 1] > 0.45: x1 += 1
    if not 650 <= x1 - x0 <= 800: return None
    return t, b, x0, x1


def separators(g, t, b, x0, x1):
    """Actual y of each ruled line; faint or hidden ones are interpolated from their neighbours."""
    step = (b - t) / ROWS
    band = g[:, x0 + 12:x1 - 12] < 230
    ys, sure = [t], [True]
    for k in range(1, ROWS):
        yp = int(round(t + k * step))
        prof = band[yp - 9:yp + 10].mean(axis=1)
        ok = prof.max() > 0.7
        ys.append(yp - 9 + int(prof.argmax()) if ok else yp); sure.append(ok)
    ys.append(b); sure.append(True)
    ys = np.array(ys, dtype=float)
    known = [i for i in range(ROWS + 1) if sure[i]]
    for i in range(ROWS + 1):
        if not sure[i]: ys[i] = np.interp(i, known, ys[known])
    return [int(round(y)) for y in ys]


def whiten_rules(block):
    """Erases what remains of ruled lines at the top/bottom and frame lines at the sides of a row."""
    gray = block.mean(axis=2)
    h, w = gray.shape
    line = gray < 222
    erase = np.zeros_like(line)
    for y in list(range(0, min(8, h))) + list(range(max(0, h - 8), h)):
        for a, c in runs(line[y]):
            if c - a >= 24: erase[y, a:c + 1] = True
    for x in list(range(0, 10)) + list(range(w - 10, w)):
        for a, c in runs(line[:, x]):
            if c - a >= 16: erase[a:c + 1, x] = True
    grown = erase | np.roll(erase, 1, axis=0) | np.roll(erase, -1, axis=0)
    # Short grey frame ticks at the row ends; black and coloured text is left alone.
    edge = np.zeros_like(line)
    edge[:, :6] = edge[:, -6:] = True
    greyish = (block.max(axis=2) - block.min(axis=2) < 40) & (gray > 140)
    block[(grown & (gray > 110)) | erase | (edge & greyish)] = 255


def inner_edge(prof, reverse=False):
    """First light position after the dark frame, scanning from one side of a profile."""
    state = 0
    for i in (range(len(prof) - 1, -1, -1) if reverse else range(len(prof))):
        if state == 0 and prof[i] < 0.35: state = 1
        elif state == 1 and prof[i] > 0.6: return i
    return None


def content_box(arr):
    """Area inside the ornamental page frame, found by walking in from each edge."""
    light = arr.mean(axis=2) > 200
    h, w = light.shape
    colf = light[int(h * .25):int(h * .75)].mean(axis=0)
    x0, x1 = inner_edge(colf), inner_edge(colf, True)
    if x0 is None or x1 is None: return None
    rowf = light[:, x0 + 10:x1 - 10].mean(axis=1)
    y0, y1 = inner_edge(rowf), inner_edge(rowf, True)
    if y0 is None or y1 is None: return None
    return x0, y0, x1, y1


def band_cuts(a, min_band=22):
    """Cut rows: centres of blank gaps and of thin full-width rules (table column lines ignored)."""
    a = a.astype(np.int16)
    g = a.mean(axis=2)
    ink = (g < 200) | (a.max(axis=2) - a.min(axis=2) > 70)
    keep = ink.mean(axis=0) < 0.5
    for x in np.flatnonzero(~keep):
        keep[max(0, x - 2):x + 3] = False
    f = ink[:, keep].mean(axis=1) if keep.any() else ink.mean(axis=1)
    cands = [((s + e) // 2, None) for s, e in runs(f < 0.004)]
    cands += [((s + e) // 2, (s, e)) for s, e in runs(f > 0.8) if e - s <= 6]
    cuts, rules = [0], []
    for c, r in sorted(cands):
        if c - cuts[-1] >= min_band and len(g) - c >= min_band:
            cuts.append(c)
            if r: rules.append(r)
    return cuts + [len(g)], rules


def upscale(block):
    img = Image.fromarray(block).resize((WIDTH, round(block.shape[0] * WIDTH / block.shape[1])), Image.LANCZOS)
    return img.filter(ImageFilter.UnsharpMask(radius=1.1, percent=55, threshold=2))


def build_special(name, src, out):
    arr = np.asarray(Image.open(src).convert('RGB')).copy()
    if name == COVER:
        edge = np.concatenate([arr[:12].reshape(-1, 3), arr[-12:].reshape(-1, 3)]).mean(axis=0)
        Image.fromarray(arr).save(os.path.join(out, name + '.webp'), 'WEBP', quality=SPECIAL_QUALITY, method=6)
        return dict(bands=[arr.shape[0]], background='#%02x%02x%02x' % tuple(int(v) for v in edge))
    x0, y0, x1, y1 = content_box(arr)
    a = arr[TOP_OVERRIDE.get(name, y0) + 2:y1 - 1, x0 + 2:x1 - 1].copy()
    cuts, rules = band_cuts(a)
    for s, e in rules:  # a ruled line on a cut is replaced by the reader's own faint rule
        seg = a[max(0, s - 2):e + 3]
        seg[(seg.mean(axis=2) < 225) & (seg.max(axis=2) - seg.min(axis=2) < 60)] = 255
    bands = [upscale(a[cuts[i]:cuts[i + 1]]) for i in range(len(cuts) - 1)]
    stacked = Image.new('RGB', (WIDTH, sum(b.height for b in bands)), 'white')
    y = 0
    for b in bands:
        stacked.paste(b, (0, y)); y += b.height
    stacked.save(os.path.join(out, name + '.webp'), 'WEBP', quality=SPECIAL_QUALITY, method=6)
    return dict(bands=[b.height for b in bands])


def build(args):
    name, scan_dir, out = args
    src = os.path.join(scan_dir, name + '.jpg')
    if name in SPECIAL:
        return dict(file=name + '.webp', **build_special(name, src, out))
    im = Image.open(src).convert('RGB')
    angle = skew_angle(np.asarray(im.convert('L')))
    if abs(angle) >= 0.08:
        im = im.rotate(angle, resample=Image.BICUBIC, fillcolor=(255, 255, 255))
    arr = np.asarray(im).copy()
    g = arr.mean(axis=2)
    f = frame(g)
    ys = separators(g, *f) if f else None
    heights = [ys[k + 1] - ys[k] for k in range(ROWS)] if ys else [0]
    if not f or min(heights) < 70 or max(heights) > ROW_BOX:
        raise ValueError('%s: 15-row grid not found %s' % (name, heights))
    _, _, x0, x1 = f
    width = x1 - x0 - 1
    rows = []
    for k in range(ROWS):
        row = arr[ys[k]:ys[k + 1] + 1, x0 + 1:x1].copy()
        whiten_rules(row)
        box = np.full((ROW_BOX, width, 3), 255, np.uint8)
        off = (ROW_BOX - row.shape[0]) // 2
        box[off:off + row.shape[0]] = row
        rows.append(box)
    block = np.concatenate(rows, axis=0)
    block[(block > 228).all(axis=2)] = 255
    row_out = round(ROW_BOX * WIDTH / width)
    img = Image.fromarray(block).resize((WIDTH, row_out * ROWS), Image.LANCZOS)
    img = img.filter(ImageFilter.UnsharpMask(radius=1.1, percent=55, threshold=2))
    img.save(os.path.join(out, name + '.webp'), 'WEBP', quality=QUALITY, method=6)
    return dict(file=name + '.webp', bands=[row_out] * ROWS)


def main():
    args = sys.argv[1:]
    if args and args[0] == '--download':
        args = args[1:]; download(args[0])
    scan_dir, assets = args
    out = os.path.join(assets, 'tajweedi')
    shutil.rmtree(out, ignore_errors=True)
    os.makedirs(out)
    with ProcessPoolExecutor() as ex:
        pages = list(ex.map(build, [(n, scan_dir, out) for n in NAMES]))
    for page in pages:
        page['sha256'] = hashlib.sha256(open(os.path.join(out, page['file']), 'rb').read()).hexdigest()
    manifest = json.dumps({'pages': pages}, separators=(',', ':')).encode()
    open(os.path.join(out, 'manifest.json'), 'wb').write(manifest)
    print('pages', len(pages), 'special', sum(p['file'][:-5] in SPECIAL for p in pages))
    print('MANIFEST_SHA256', hashlib.sha256(manifest).hexdigest())


if __name__ == '__main__':
    main()

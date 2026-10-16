"""Refresh reviewed women-focused titles and all publisher-listed language editions.

Requires network access. Never guesses a PDF URL or treats a generic sisters
reading recommendation (e.g. Quran, biographies) as a women-only book.
"""
import concurrent.futures
import html
import json
import pathlib
import re
import subprocess

ROOT = pathlib.Path(__file__).resolve().parents[1]
CACHE = ROOT / 'tmp' / 'muslimah-catalog'
CACHE.mkdir(parents=True, exist_ok=True)
TITLES = {
    'islami-behno-ki-namaz': 'Salah for Islamic Sisters',
    'jannati-zevar': 'Jannati Zevar',
    'sunni-bahishti-zewar': 'Sunni Bahishti Zewar',
    'khawateen-kay-masail': 'Khawateen Kay Masail',
    'chahal-hadees-baraye-khawateen-e-islam': 'Chahal Hadees Baraye Khawateen-e-Islam',
    'chadar-aur-char-diwari': 'Chadar Aur Char Diwari',
    'aurat-aur-parda': 'Aurat Aur Parda',
    'aurat-aur-aazadi': 'Aurat Aur Aazadi',
    'saas-bahu-main-sulha-ka-raaz': 'Saas Bahu Main Sulha Ka Raaz',
    'jannati-khawateen': 'Jannati Khawateen',
}

def fetch(path):
    target = CACHE / (path.strip('/').replace('/', '_') + '.html')
    if not target.exists():
        subprocess.run(['curl.exe', '-f', '-sS', '-L', '--retry', '2', '--max-time', '45',
                        'https://www.dawateislami.net' + path, '-o', str(target)], check=True)
    return target.read_text(encoding='utf-8')

def plain(value):
    return html.unescape(re.sub('<[^>]+>', '', value)).strip()

def parse(slug, path):
    content = fetch(path)
    links = re.findall(r'<a\b[^>]*href="([^"]+)"[^>]*>(.*?)</a>', content, re.S)
    pdfs = [html.unescape(url).split('?')[0] for url, _ in links
            if '/Data/Books/Download/' in url and '.pdf' in url]
    if len(set(pdfs)) != 1:
        raise ValueError(f'Expected one publisher PDF: {path}: {pdfs}')
    language = path.split('/')[2]
    native = re.search(r'<h1[^>]*>(.*?)</h1>', content, re.S)
    pages = re.search(r'(?:PDF Pages:|پی ڈی ایف صفحات:)\s*</[^>]+>\s*<span>\s*(\d+)', content)
    row = dict(collection=slug.replace('-', '_'), title=TITLES[slug], language=language,
               nativeTitle=plain(native[1]) if native else '', pages=int(pages[1]) if pages else 0,
               source='https://www.dawateislami.net' + path, pdf=pdfs[0])
    section = content.split('book-languages-box', 1)[-1].split('book-summary', 1)[0]
    editions = dict((url, plain(label)) for url, label in re.findall(
        r'<a\b[^>]*href="(/bookslibrary/[^"#]+)"[^>]*>(.*?)</a>', section, re.S)
        if not url.endswith('/page-1'))
    return row, editions

def collect(slug):
    path = '/bookslibrary/ur/' + slug
    base, editions = parse(slug, path)
    rows = [base]
    for edition, label in editions.items():
        if edition == path:
            continue
        row, _ = parse(slug, edition)
        row['languageName'] = label
        rows.append(row)
    for row in rows:
        row.setdefault('languageName', 'Urdu')
    return rows

if __name__ == '__main__':
    rows = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool:
        for slug, result in zip(TITLES, pool.map(collect, TITLES)):
            rows.extend(result)
            print(slug, len(result), flush=True)
    output = ROOT / 'docs' / 'muslimah-book-sources.json'
    output.write_text(json.dumps(dict(verified='2026-10-05', editions=rows), ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('Total editions:', len(rows))

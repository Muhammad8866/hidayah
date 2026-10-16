"""Audit the checked-in catalog and, when present, cached official source pages."""
import json
import unicodedata
from pathlib import Path
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
data = json.loads((ROOT / 'docs/baby-name-sources.json').read_text(encoding='utf-8'))
names = data['names']
assert len(names) == 798
assert Counter(n['gender'] for n in names) == {'BOY': 497, 'GIRL': 301}
assert len({(n['gender'], n['name']) for n in names}) == len(names)
assert names == sorted(names, key=lambda n:(n['name'],n['gender']))

def normal(text):
    # Ignore whitespace and vowel marks when comparing extracted source HTML.
    return ''.join(c for c in unicodedata.normalize('NFD', text)
                   if not c.isspace() and unicodedata.category(c) != 'Mn')

pages_path = ROOT / 'tmp/baby-name-sources/pages.json'
pages = {p['url']: normal(p['text']) for p in json.loads(pages_path.read_text(encoding='utf-8'))} if pages_path.exists() else {}
additional = {}
reviewed_lines = []
for filename in ('baby_names_additional_reviewed.tsv', 'baby_names_expansion_reviewed.tsv', 'baby_names_devotional_reviewed.tsv'):
    reviewed_lines.extend((ROOT / 'tools' / filename).read_text(encoding='utf-8').splitlines()[1:])
for line in reviewed_lines:
    name, gender, arabic, english, hinglish, urdu, aliases, source = line.split('|')
    for resolved_gender in (('BOY', 'GIRL') if gender == 'BOTH' else (gender,)):
        additional[(resolved_gender, name)] = dict(arabic=arabic, english=english,
            hinglish=hinglish, sourceMeaningUrdu=urdu, aliases=aliases,
            sourceUrl=f'https://quranicnames.com/{source}/', sourceSpelling=arabic,
            sourceTitle='QuranicNames — Arabic name research')
assert len(additional) == 305
assert sum(n['sourceUrl'].startswith(data['source'] + '/page-') for n in names) == 493
for name in names:
    assert all(name[k].strip() for k in ('name','arabic','english','hinglish','sourceSpelling','sourceMeaningUrdu','sourceTitle'))
    key = (name['gender'], name['name'])
    if key in additional:
        # These Urdu meanings are editorial translations; compare against the
        # reviewed input rather than treating them as quotations from Urdu HTML.
        assert all(name[k] == value for k, value in additional[key].items()), name['name']
        continue
    assert name['sourceUrl'].startswith(data['source'] + '/page-')
    assert 43 <= int(name['sourceUrl'].rsplit('-',1)[1]) <= 61
    assert name['sourceTitle'] == 'Naam Rakhne ke Ahkam — Maktaba-tul-Madinah'
    assert all(term not in name['english'].lower() for term in ('todo','placeholder','unknown meaning'))
    if pages:
        page = pages[name['sourceUrl']]
        assert normal(name['sourceSpelling']) in page, name['name']
        # Ahmad's explanatory prose is abridged; all other Urdu meanings are extracts.
        if name['name'] != 'Ahmad':
            assert normal(name['sourceMeaningUrdu']) in page, name['name']

catalog = (ROOT / 'app/src/main/java/com/example/names/BabyNameCatalog.kt').read_text(encoding='utf-8')
assert catalog.count('        BabyName(') == len(names)
print(f"PASS: {len(names)} catalog entries, unique IDs, required meanings and reviewed per-name sources")
print(f"PASS: {len(additional)} additional entries match reviewed meanings, genders and source links")
print(f"PASS: {len(pages)} cached source pages checked" if pages else "Cached page check skipped; run collect_baby_name_sources.py for source-text verification")

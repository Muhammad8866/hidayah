import sys, json, re, collections
from pathlib import Path
root = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(root / 'tools'))
import auto_translate as auto
import audit_indic_content_coverage as indic
langs = auto.languages()
canonical = indic.source_ids()
loaders = indic.loader_files()
rows = []
for code, name in langs.items():
    ids = set()
    for path in loaders.get(code, []):
        ids.update(re.findall(r'"(dua_[^"]+)"\s+to\s+DuaText', path.read_text(encoding='utf-8')))
    if code in ('en', 'hinglish'):
        ids = canonical
    rows.append(dict(code=code, name=name, dua_entries=len(ids & canonical), missing_duas=len(canonical-ids)))
found = auto.scan()
raw = [dict(text=v[0], location=v[2]) for v in found.values() if v[1]=='raw']
files = list((root/'app/src/main/java').rglob('*.kt'))
tests = list((root/'app/src/test').rglob('*.kt'))
result = dict(language_count=len(langs), languages=rows, raw_literal_unique_count=len(raw), raw_literals=raw,
              kotlin_files=len(files), kotlin_lines=sum(len(p.read_text(encoding='utf-8').splitlines()) for p in files),
              unit_test_files=len(tests), instrumentation_test_files=len(list((root/'app/src/androidTest').rglob('*.kt'))),
              source_catalog_entries=len(auto.read_json(root/'localization/source.json', [])),
              translation_cache_files=len(list((root/'localization/translations').glob('*.json'))))
(Path(__file__).parent/'inventory.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k not in ('languages','raw_literals')},indent=2))
for row in rows:
    print(row['code'], row['dua_entries'], 'missing', row['missing_duas'])

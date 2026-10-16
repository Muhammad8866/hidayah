"""Verify the eight requested world languages against existing content keys.

This inventories technical completeness, not translation/religious approval.
"""
import argparse
import json
from pathlib import Path
import re
import world_localization as world

def audit(code):
    source=world.read(world.SOURCE,{})
    registry=(world.MODEL/'DuaTranslations.kt').read_text(encoding='utf-8')
    loader=re.search(rf'"{code}" to \{{([^}}]+)\}}',registry)
    ids=set()
    cache=world.read(world.cache_path(code),{})
    mismatched_duas=[]
    for obj in re.findall(r'(Dua\w+)\.MAP',loader[1] if loader else ''):
        body=(world.MODEL/f'{obj}.kt').read_text(encoding='utf-8')
        ids.update(re.findall(r'"(dua_[^"]+)"\s+to\s+DuaText\(',body))
        if 'Draft' in obj:
            parsed=set()
            pattern=r'(?m)^\s*("dua_[^"]+")\s+to\s+DuaText\('+r'\s*,\s*'.join([f'({quoted})']*4)+r'\),'
            for match in re.findall(pattern,body):
                row=[json.loads(value.replace('\\$','$')) for value in match]
                parsed.add(row[0])
                if row[1:]!=[cache.get(text) for text in source['duas'][row[0]]]:
                    mismatched_duas.append(row[0])
            mismatched_duas.extend(sorted(set(re.findall(r'"(dua_[^"]+)"\s+to\s+DuaText\(',body))-parsed))
    expected_ui={value for row in source['features'].values() for value in row}
    keys=set()
    mismatched=[]
    world_registry=(world.UI/'WorldDraftPhrases.kt').read_text(encoding='utf-8') if (world.UI/'WorldDraftPhrases.kt').exists() else ''
    ui_loader=re.search(rf'"{code}" to \{{([^}}]+)\}}',world_registry)
    quoted=r'"(?:\\.|[^"\\])*"'
    for obj in re.findall(r'(World\w+)\.MAP',ui_loader[1] if ui_loader else ''):
        path=world.UI/f'{obj}.kt'
        for key,value in re.findall(r'(?m)^\s*('+quoted+r')\s+to\s+('+quoted+r'),',path.read_text(encoding='utf-8')):
            key=json.loads(key.replace('\\$','$'))
            value=json.loads(value.replace('\\$','$'))
            keys.add(key)
            if cache.get(key)!=value: mismatched.append(key)
    for text in world.required(code)&cache.keys(): world.validate(text,cache[text])
    untranslated=[text for text in world.required(code)&cache.keys() if world.untranslated_phrase(text,cache[text],code)]
    return dict(language=code,dua_entries=len(ids&source['duas'].keys()),missing_duas=sorted(source['duas'].keys()-ids),
                ui_content_keys=len(keys&expected_ui),missing_ui_content=sorted(expected_ui-keys),
                missing_cache=len(world.required(code)-cache.keys()),mismatched_cache=mismatched,mismatched_duas=mismatched_duas,
                untranslated_english=untranslated,loader_present=ui_loader is not None)

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('codes',nargs='*',default=list(world.CODES),choices=world.CODES)
    p.add_argument('--json',action='store_true')
    args=p.parse_args()
    rows=[audit(code) for code in args.codes]
    if args.json: print(json.dumps(rows,ensure_ascii=False,indent=2))
    else:
        for row in rows:
            print(f"{row['language']}: duas={row['dua_entries']}/298, content_keys={row['ui_content_keys']}, missing_content={len(row['missing_ui_content'])}, missing_cache={row['missing_cache']}, loader={row['loader_present']}")
    return int(any(row['missing_duas'] or row['missing_ui_content'] or row['missing_cache'] or row['mismatched_cache'] or row['mismatched_duas'] or row['untranslated_english'] or not row['loader_present'] for row in rows))

if __name__=='__main__': raise SystemExit(main())

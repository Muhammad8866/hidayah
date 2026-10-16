"""Export actual registered duas for human linguistic/religious review.

Coverage and token checks are not approval. This command never changes app flags.
"""
import argparse,hashlib,json,re
from pathlib import Path
import world_localization as w

FIELDS=('title','meaning','context','benefit')
QUOTED=r'"(?:\\.|[^"\\])*"'

def decode(value):
    return None if value=='null' else ''.join(json.loads(token.replace('\\$','$')) for token in re.findall(QUOTED,value))

def pending_languages():
    body=(w.ROOT/'app/src/main/java/com/example/viewmodel/MainViewModel.kt').read_text(encoding='utf-8')
    return [code for code in re.findall(r'AppLanguage\("([^"]+)",[^\n]+reviewPending\s*=\s*true\)',body)]

def registered_rows(code):
    body=(w.MODEL/'DuaTranslations.kt').read_text(encoding='utf-8')
    loader=re.search(rf'"{code}" to \{{([^}}]+)\}}',body)
    if not loader: raise ValueError('Missing registered language '+code)
    rows={}; paths=[]
    expression=QUOTED+r'(?:\s*\+\s*'+QUOTED+r')*'
    field=f'({expression}|null)'
    pattern=r'('+QUOTED+r')\s+to\s+DuaText\(\s*'+r'\s*,\s*'.join([field]*3)+r'(?:\s*,\s*'+field+r')?\s*,?\s*\)'
    for obj in re.findall(r'(Dua\w+)\.MAP',loader[1]):
        path=w.MODEL/f'{obj}.kt'; paths.append(path)
        text=path.read_text(encoding='utf-8')
        for m in re.finditer(pattern,text):
            key=decode(m[1]); target=[decode(m[i]) for i in range(2,5)]
            target.append(decode(m[5]) if m[5] is not None else target[2])
            rows[key]=(target,str(path.relative_to(w.ROOT)),text.count('\n',0,m.start())+1,'Draft' in obj)
    return rows,paths

def export(output):
    output.mkdir(parents=True,exist_ok=True)
    source=w.read(w.SOURCE,{})['duas']; languages=pending_languages()
    summaries=[]; hashes={}
    for code in languages:
        rows,paths=registered_rows(code)
        absent=set(source)-rows.keys()
        if absent: raise ValueError(f'Unparsed/missing {code}: {len(absent)} rows')
        issues=[]; count=0
        with (output/f'{code}-review.jsonl').open('w',encoding='utf-8') as stream:
            for key,english in source.items():
                targets,path,line,draft=rows[key]
                fields=[]
                for field,original,target in zip(FIELDS,english,targets):
                    flags=[]
                    if target is None: flags.append('intentional_nullable_or_missing_requires_context_review')
                    else:
                        for label,pattern in (
                            ('placeholder_count_difference',r'\{[A-Za-z0-9_]+\}|%\d*\$?[sdfox]'),
                            ('literal_honorific_difference_not_proven_omission',r'ﷺ|\((?:RA|RH|SAW|AS)\)'),
                        ):
                            from collections import Counter
                            if Counter(re.findall(pattern,original))!=Counter(re.findall(pattern,target)): flags.append(label)
                        if original.count('\n')!=target.count('\n'): flags.append('paragraph_count_difference')
                        if '\ufffd' in target or re.search(r'\?{3,}',target): flags.append('possible_encoding_corruption')
                    if flags: issues.append(dict(id=key,field=field,flags=flags))
                    fields.append(dict(field=field,english=original,translation=target,flags=flags))
                    count+=1
                record=dict(id=key,language=code,location=f'{path}:{line}',machine_draft_file=draft,
                            fields=fields,linguistic_reviewer=None,religious_reviewer=None,approval='pending')
                stream.write(json.dumps(record,ensure_ascii=False)+'\n')
        for path in paths: hashes[str(path.relative_to(w.ROOT))]=hashlib.sha256(path.read_bytes()).hexdigest()
        summaries.append(dict(language=code,registered_duas=len(rows),exported_fields=count,
                              structural_review_flags=issues,approval='pending_human_review'))
    manifest=dict(languages=len(languages),dua_rows=sum(r['registered_duas'] for r in summaries),
                  fields=sum(r['exported_fields'] for r in summaries),scope='actual_registered_dua_fields_only; UI and other long-form content require additional review',
                  expert_approvals=0,review_pending_flags_cleared=0,results=summaries,source_hashes=hashes)
    w.write(output/'manifest.json',manifest)
    (output/'README.md').write_text('''# Human review packets

Each language JSONL contains actual registered dua translations with English source, file/line provenance and four fields per row. The manifest inventories49 pending languages. Literal honorific differences can be translated honorifics; they are review cues, not proof of omission. Nulls likewise require context.

No reviewer has signed these packets. No native-language or religious approval is claimed, and app reviewPending flags stay unchanged. Automated checks and coverage cannot certify grammar, theology, hadith attribution or jurisprudence.

For each row a native-language reviewer checks fluency, sense, negation, roles and templates; a qualified religious reviewer checks Arabic/source fidelity, attribution, conditions, rewards and doctrinal/fiqh context. Record reviewer names, date, source references, decision and the exact hash of the approved text. UI, names, lessons, festivals and guidance need separate review as well.

Priority source references used for sampled findings: [Al-Fatiha1:4](https://quran.com/1/4), [Al-Fatiha1:7](https://quran.com/1/7), [Bukhari1154](https://sunnah.com/bukhari:1154). These references support specific sampled meanings, not certification of the entire catalog.
''',encoding='utf-8')
    return dict(languages=len(languages),dua_rows=manifest['dua_rows'],fields=manifest['fields'],expert_approvals=0)

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('--output',type=Path,default=w.ROOT/'artifacts/localization-quality-review-2026-10-14/packets')
    args=p.parse_args(); print(json.dumps(export(args.output),indent=2))

if __name__=='__main__': main()

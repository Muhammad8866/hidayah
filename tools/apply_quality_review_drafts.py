"""Apply confident draft corrections; this never grants review approval."""
import argparse,json,re
from pathlib import Path
import world_localization as world

ROOT=world.ROOT
CORRECTIONS=ROOT/'localization/quality-review-draft-corrections.json'
QUOTED=r'"(?:\\.|[^"\\])*"'

def replace_targets(body,old,new):
    pair=r'(?m)(^\s*'+QUOTED+r'\s+to\s+)('+QUOTED+r')'
    body=re.sub(pair,lambda m:m[1]+(new if m[2]==old else m[2]),body)
    dua=r'(DuaText\()('+r'\s*,\s*'.join([QUOTED]*4)+r')(\))'
    return re.sub(dua,lambda m:m[1]+m[2].replace(old,new)+m[3],body)

def proposals(paths):
    result=[]
    for path in paths:
        data=json.loads(path.read_text(encoding='utf-8'))
        rows=data if isinstance(data,list) else data.get('findings',[])
        for row in rows:
            value=row.get('proposed',row.get('suggested_replacement'))
            confidence=row.get('confidence',row.get('proposal_confidence',''))
            if not value or not confidence.startswith('high') or row.get('protected_original',False): continue
            current=row.get('current',row.get('actual'))
            if not isinstance(current,str): continue
            if '\ufffd' in value or re.search(r'\?{2,}',value): raise ValueError('Corrupted proposal')
            world.validate(row['source'],value)
            result.append(dict(language=row['language'],source=row['source'],current=current,proposed=value))
    return result

def apply(rows):
    corrections=world.read(CORRECTIONS,{})
    plans=[]
    # Only machine-draft files are eligible; original and reviewed rows stay intact.
    for row in rows:
        code=row['language']; name=code.capitalize()
        old=world.quote(row['current']); new=world.quote(row['proposed'])
        matches=[]
        for directory in (world.MODEL,world.UI,world.NAMES):
            for path in directory.glob(f'*{name}Draft*.kt'):
                body=path.read_text(encoding='utf-8')
                if replace_targets(body,old,new)!=body: matches.append((path,body))
        if not matches: raise ValueError(f'No actual draft matches: {code}/{row["source"][:50]}')
        corrections.setdefault(code,{})[row['source']]=row['proposed']
        plans.append((row,old,new,[path for path,_ in matches]))
    # Read again for each replacement because several fixes may share a file.
    changed=set()
    for row,old,new,paths in plans:
        for path in paths:
            body=path.read_text(encoding='utf-8')
            world.write_kotlin(path,replace_targets(body,old,new))
            changed.add(str(path.relative_to(ROOT)))
        for path in (world.cache_path(row['language']),ROOT/f'tmp/indic_translation_cache/{row["language"]}.json'):
            cache=world.read(path,{})
            if cache.get(row['source'])==row['current']:
                cache[row['source']]=row['proposed']; world.write(path,cache)
    world.write(CORRECTIONS,corrections)
    return dict(corrections=len(plans),changed_draft_files=sorted(changed),approval='pending_human_linguistic_and_religious_review')

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('reports',nargs='+',type=Path)
    p.add_argument('--apply',action='store_true')
    args=p.parse_args(); rows=proposals(args.reports)
    print(json.dumps(apply(rows) if args.apply else {'eligible_corrections':len(rows)},ensure_ascii=False,indent=2))

if __name__=='__main__': main()

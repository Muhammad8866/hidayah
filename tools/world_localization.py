"""Complete existing world-language content with resumable, offline machine drafts.

Existing dua rows and non-English UI phrases take precedence. This does not grant
linguistic/religious approval and does not change the legacy localization baseline.
"""
import argparse
from collections import Counter
import json
from pathlib import Path
import re
import time
import urllib.parse
import urllib.request
import urllib.error

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'tools/indic_translation_sources.json'
MODEL = ROOT / 'app/src/main/java/com/example/domain/model/i18n'
UI = ROOT / 'app/src/main/java/com/example/ui/localization'
NAMES = ROOT / 'app/src/main/java/com/example/names'
CODES = ('fa', 'tr', 'id', 'es', 'ru', 'zh', 'it', 'ja')
PROTECTED = re.compile(r'\{[A-Za-z0-9_]+\}|%\d*\$?[sdfox]|[\u0600-\u06ff]+(?:[ \t]+[\u0600-\u06ff]+)*|[\ufdfa]|\((?:RA|RH|SAW|AS)\)')
PUBLIC_TRANSLATOR_ENDPOINT = 'single'

def read(path, default):
    return json.loads(path.read_text(encoding='utf-8')) if path.exists() else default

def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    text=json.dumps(value,ensure_ascii=False,indent=2,sort_keys=True)+'\n'
    if not path.exists() or path.read_text(encoding='utf-8') != text:
        temporary=path.with_suffix('.tmp')
        temporary.write_text(text,encoding='utf-8')
        temporary.replace(path)

def write_kotlin(path, text):
    if not path.exists() or path.read_text(encoding='utf-8')!=text:
        path.write_text(text,encoding='utf-8')

def existing_ids(code):
    return {m for p in MODEL.glob(f'Dua{code.capitalize()}*.kt') if 'Draft' not in p.name
            for m in re.findall(r'"(dua_[^"]+)"\s+to\s+DuaText\(', p.read_text(encoding='utf-8'))}

def required(code):
    source=read(SOURCE,{})
    existing=existing_ids(code)
    return {value for key,row in source['duas'].items() if key not in existing for value in row} | {
        value for row in source['features'].values() for value in row}

def validate(source, target):
    if not isinstance(target,str) or not target.strip(): raise ValueError('Blank translation')
    placeholders=re.compile(r'\{[A-Za-z0-9_]+\}|%\d*\$?[sdfox]')
    if Counter(placeholders.findall(source)) != Counter(placeholders.findall(target)):
        raise ValueError('Placeholders changed')
    # Persian translations naturally introduce Arabic-script words. Preserve the
    # original devotional runs without treating new Persian text as source Arabic.
    arabic_runs=re.findall(r'[\u0600-\u06ff]+(?:[ \t]+[\u0600-\u06ff]+)*|[\ufdfa]',source)
    for run,count in Counter(arabic_runs).items():
        exact=re.compile(r'(?<![\u0600-\u06ff])'+re.escape(run)+r'(?![\u0600-\u06ff])')
        if len(exact.findall(target))<count: raise ValueError('Protected devotional text changed')
    for run,count in Counter(re.findall(r'\((?:RA|RH|SAW|AS)\)',source)).items():
        if target.count(run)<count: raise ValueError('Devotional abbreviation changed')
    if source.count('\n') != target.count('\n'): raise ValueError('Paragraph structure changed')
    if len(source)>100 and len(target)<len(source)*(.10 if code_script(target) else .20):
        raise ValueError('Suspiciously truncated translation')

def code_script(text):
    return bool(re.search(r'[\u3040-\u30ff\u3400-\u9fff]',text))

def quote(value):
    quoted=json.dumps(value,ensure_ascii=False).replace('$','\\$')
    # Retain display controls while making their presence explicit in source.
    for point in (0x061c,0x200e,0x200f,0x202a,0x202b,0x202c,0x202d,0x202e,0x2066,0x2067,0x2068,0x2069,0xfeff):
        quoted=quoted.replace(chr(point),f'\\u{point:04x}')
    return quoted

def cache_path(code): return ROOT / f'localization/translations/{code}.json'

def untranslated_sentence(source, target):
    # Latin transliterations of book titles/authors are proper names, not English
    # fallback sentences. Detect actual English grammar rather than script alone.
    words=r'\b(?:the|whoever|our lord|your|their|this|that|then|with|from|after|before|between|against|without|should|must|can|will|does|have|has|when|and|for)\b'
    return len(source)>40 and source==target and re.search(words,source,re.I) is not None

def untranslated_phrase(source, target, code):
    if untranslated_sentence(source,target): return True
    if code not in ('tl','ko','th') or source!=target: return False
    # Short UI labels and dua headings also need whole-phrase translations.
    # Explicit glossary identities (Surah names and valid borrowed controls)
    # remain valid; devotional abbreviations are not English grammar.
    if glossary(code).get(source)==source: return False
    plain=PROTECTED.sub('',source)
    words=r'\b(?:a|an|the|of|for|from|with|after|before|between|upon|on|at|in|to|my|your|our|us|which|what|how|all|asking|allow|abundant|prayer|children|contentment|establishers|final|first|sitting|night|other|assets|rectify|refresh|remembering|review|seeing|audio|available|beginner|books|custom|day|display|essentials|excused|focus|leaderboard|mode|index|pebble|counter|recitations|search|stream|tally|world|library|tour|righteous|mercy|meaning|mean)\b'
    # "an-/at-" are Arabic name prefixes, and rak'at/Jama'at are
    # transliterations, rather than the English articles/preposition.
    return bool(re.search(r"(?<!['‘’-])"+words+r'(?!-)',plain,re.I))

def normalize_terminology(source, target, code):
    if re.search(r'\bcounters?\b',source,re.I):
        if code=='zh': target=target.replace('柜台','计数器')
        if code=='fa': target=target.replace('پیشخوان','شمارنده')
    return target

def glossary(code):
    values=read(ROOT/'localization/world-glossary.json',{}).get(code,{})
    for filename in ('remaining-ps-el-pl-glossary.json','remaining-uk-so-am-glossary.json','remaining-uz-he-si-glossary.json','remaining-el-root-glossary.json'):
        values.update(read(ROOT/'localization'/filename,{}).get(code,{}))
    if code=='fa': values.update(read(ROOT/'localization/world-fa-meaning-corrections.json',{}))
    values.update(read(ROOT/'localization/quality-review-draft-corrections.json',{}).get(code,{}))
    # Surah names are proper names, not English vocabulary (Sad, Hud, Qaf).
    catalog=(ROOT/'app/src/main/java/com/example/quran/FullQuranCatalog.kt').read_text(encoding='utf-8')
    for arabic,latin in re.findall(r'Entry\("([^"]+)","([^"]+)"',catalog):
        values[latin]=arabic if code=='fa' else latin
    # AH denotes a Hijri year, not "hours" or a unit of electric charge.
    formats={'fa':'{n} هجری قمری','tr':'Hicrî {n}','id':'{n} H',
             'es':'año {n} de la Hégira','ru':'{n} г. хиджры','zh':'伊斯兰历{n}年',
             'it':'{n} dell’Egira','ja':'ヒジュラ暦{n}年'}
    for text in required(code):
        match=re.fullmatch(r'(\d+) AH',text)
        if match:
            number=match[1].translate(str.maketrans('0123456789','۰۱۲۳۴۵۶۷۸۹')) if code=='fa' else match[1]
            values[text]=formats[code].format(n=number)
    return values

def restore_markers(value, held, code, source, bracketed=False):
    for token, content in held.items():
        # Persian/Arabic translators may localize digits or insert group separators.
        digits=[f'[{digit}{chr(0x660+int(digit))}{chr(0x6f0+int(digit))}]' for digit in token]
        number=r'(?<![0-9٠-٩۰-۹])'+r'[ ,٬]*'.join(digits)+r'(?![0-9٠-٩۰-۹])'
        pattern=re.compile(number)
        if bracketed:
            pattern=re.compile(r'[\[［(（]\s*'+number+r'\s*[\]］)）]|'+number)
        if len(pattern.findall(value))!=1:
            raise ValueError(f'{code}: protected marker changed in {source[:60]}: {value}')
        value=pattern.sub(lambda _:content,value)
    return value

def request_translation(query, code):
    global PUBLIC_TRANSLATOR_ENDPOINT
    for attempt in range(4):
        try:
            endpoint=PUBLIC_TRANSLATOR_ENDPOINT
            params=dict(client='gtx' if endpoint=='single' else 'dict-chrome-ex',sl='en',tl=code,q=query)
            if endpoint=='single': params['dt']='t'
            url='https://translate.googleapis.com/translate_a/'+endpoint+'?'+urllib.parse.urlencode(params)
            request=urllib.request.Request(url,headers={'User-Agent':'Mozilla/5.0'})
            with urllib.request.urlopen(request,timeout=45) as response: payload=json.load(response)
            if endpoint=='t':
                if not isinstance(payload,list) or not all(isinstance(row,str) for row in payload):
                    raise ValueError('Unexpected public text translation response')
                return ''.join(payload)
            return ''.join(row[0] for row in payload[0] if row[0])
        except urllib.error.HTTPError as exc:
            if exc.code==429 and PUBLIC_TRANSLATOR_ENDPOINT=='single':
                PUBLIC_TRANSLATOR_ENDPOINT='t'
                time.sleep(2)
                continue
            if attempt==3: raise
            time.sleep(min(2**attempt,8))
        except (OSError,ValueError):
            if attempt==3: raise
            time.sleep(min(2**attempt,8))

def mask_line(original, bracketed=False):
    held={}
    def protect(match):
        token=str(123456+len(held)*111111)
        held[token]=match[0]
        return '['+token+']' if bracketed else token
    masked=PROTECTED.sub(protect,original)
    return masked,held

def retry_protected_line(original, code):
    masked,held=mask_line(original,bracketed=True)
    value=request_translation(masked,code).strip()
    return restore_markers(value,held,code,original,bracketed=True)

def chunks(items,size):
    return [items[i:i+size] for i in range(0,len(items),size)]

def materialize(code):
    source=read(SOURCE,{})
    cache=read(cache_path(code),{})
    cache={text:normalize_terminology(text,value,code) for text,value in cache.items()}
    cache.update({key:value for key,value in glossary(code).items() if key in required(code)})
    write(cache_path(code),cache)
    for text in required(code):
        if text not in cache: raise ValueError(f'{code}: missing source {text[:60]}')
        validate(text,cache[text])
    name=code.capitalize()
    dua_rows=[(key,row) for key,row in source['duas'].items() if key not in existing_ids(code)]
    objects=[]
    for index,group in enumerate(chunks(dua_rows,40),1):
        obj=f'Dua{name}Draft{index}'
        objects.append(obj)
        body=f'package com.example.domain.model.i18n\n\n/** Machine draft; linguistic and religious review pending. */\ninternal object {obj} {{\n    val MAP: Map<String, DuaText> = mapOf(\n'
        body+=''.join(f'        {quote(key)} to DuaText({", ".join(quote(cache[text]) for text in row)}),\n' for key,row in group)
        write_kotlin(MODEL/f'{obj}.kt',body+'    )\n}\n')
    registry_path=MODEL/'DuaTranslations.kt'
    registry=registry_path.read_text(encoding='utf-8')
    pattern=rf'("{code}" to \{{)([^}}]+)(\}})'
    match=re.search(pattern,registry)
    old=[] if match is None else [x for x in re.split(r'\s*\+\s*',match[2].strip()) if not re.fullmatch(rf'Dua{name}Draft\d+\.MAP',x)]
    entry=f'"{code}" to {{ {" + ".join(old+[obj+".MAP" for obj in objects])} }}'
    if match: registry=registry[:match.start()]+entry+registry[match.end():]
    else:
        marker='    private val loaders: Map<String, () -> Map<String, DuaText>> = mapOf(\n'
        if marker not in registry: raise ValueError('Dua registry marker missing')
        registry=registry.replace(marker,marker+'        '+entry+',\n',1)
    write_kotlin(registry_path,registry)
    ui_keys=list(dict.fromkeys(value for row in source['features'].values() for value in row))
    # Include name meanings in whole-phrase lookup as well as the name-specific registry.
    ui_objects=[]
    for index,group in enumerate(chunks(ui_keys,70),1):
        obj=f'World{name}Draft{index}'
        ui_objects.append(obj)
        body=f'package com.example.ui.localization\n\n/** Machine draft; review pending. */\ninternal object {obj} {{\n    val MAP: Map<String, String> = mapOf(\n'
        body+=''.join(f'        {quote(key)} to {quote(cache[key])},\n' for key in group)
        write_kotlin(UI/f'{obj}.kt',body+'    )\n}\n')
    definitions={}
    for selected in CODES:
        paths=sorted(UI.glob(f'World{selected.capitalize()}Draft*.kt'),key=lambda p:int(re.search(r'Draft(\d+)',p.stem)[1]))
        if paths: definitions[selected]=[p.stem+'.MAP' for p in paths]
    body='package com.example.ui.localization\n\n/** Offline machine drafts; review pending. Packs load only when requested. */\ninternal object WorldDraftPhrases {\n    private val loaders: Map<String, () -> Map<String, String>> = mapOf(\n'
    body+=''.join(f'        "{c}" to {{ buildMap {{ {"; ".join("putAll("+obj+")" for obj in objs)} }} }},\n' for c,objs in definitions.items())
    body+='    )\n    private val loaded = java.util.concurrent.ConcurrentHashMap<String, Map<String, String>>()\n    fun forLanguage(code: String): Map<String, String> =\n        loaders[code]?.let { load -> loaded.getOrPut(code) { load() } } ?: emptyMap()\n}\n'
    write_kotlin(UI/'WorldDraftPhrases.kt',body)
    print(f'{code}: integrated {len(dua_rows)} missing duas and {len(ui_keys)} whole-content phrases',flush=True)

def translate_online(code, refresh=False):
    """Translate existing public English catalog text; never send credentials/user data.

    This public web translator has no availability guarantee. Cache valid output
    immediately; refuse altered tokens rather than making English fallback packs.
    """
    cache={} if refresh else read(cache_path(code),{})
    invalid=[]
    for source,value in cache.items():
        try:
            validate(source,value)
            if untranslated_phrase(source,value,code):
                raise ValueError('English sentence was not translated')
        except ValueError: invalid.append(source)
    for source in invalid: del cache[source]
    if invalid: print(f'{code}: retrying {len(invalid)} invalid cached entries',flush=True)
    cache.update({key:value for key,value in glossary(code).items() if key in required(code)})
    write(cache_path(code),cache)
    pending=sorted(required(code)-cache.keys(),key=lambda text:(len(text),text))
    start=0
    print(f'{code}: {len(pending)} online phrases pending',flush=True)
    while start<len(pending):
        batch=[]
        total=0
        for source in pending[start:]:
            if batch and (total+len(source)>3000 or len(batch)>=24): break
            batch.append(source)
            total+=len(source)+3
        # Each line remains a complete sentence/template; preserve paragraph
        # separators outside translation so Markdown and blank lines are exact.
        groups=[]
        lines=[]
        for source in batch:
            parts=[]
            for part in re.split(r'(\n+)',source):
                if not part or part.isspace(): parts.append((part,None,{})); continue
                masked,held=mask_line(part)
                if not re.search('[A-Za-z]',masked): parts.append((part,None,{})); continue
                parts.append((part,len(lines),held))
                lines.append(masked)
            groups.append(parts)
        query='\n|\n'.join(lines)
        target=request_translation(query,code)
        translated=re.split(r'\s*\n\s*\|\s*\n\s*',target)
        if len(translated)!=len(lines): raise ValueError(f'{code}: batch boundaries changed')
        updates={}
        for source,parts in zip(batch,groups):
            values=[]
            for original,index,held in parts:
                if index is None: values.append(original); continue
                value=translated[index].strip()
                try: value=restore_markers(value,held,code,source)
                except ValueError: value=retry_protected_line(original,code)
                if untranslated_phrase(original,value,code):
                    value=retry_protected_line(original,code)
                values.append(value)
            value=normalize_terminology(source,''.join(values),code)
            validate(source,value)
            updates[source]=value
        cache.update(updates)
        write(cache_path(code),cache)
        start+=len(batch)
        print(f'{code}: {start}/{len(pending)} online phrases saved',flush=True)
        time.sleep(.15)

def main():
    p=argparse.ArgumentParser(description=__doc__)
    p.add_argument('code',choices=CODES)
    p.add_argument('--online',action='store_true')
    p.add_argument('--refresh',action='store_true',help='Replace only generated cache for the selected language')
    p.add_argument('--integrate',action='store_true')
    p.add_argument('--check',action='store_true')
    args=p.parse_args()
    if args.online: translate_online(args.code,args.refresh)
    if args.integrate: materialize(args.code)
    cache=read(cache_path(args.code),{})
    missing=required(args.code)-cache.keys()
    for text in required(args.code)&cache.keys(): validate(text,cache[text])
    print(f'{args.code}: {len(required(args.code))-len(missing)}/{len(required(args.code))} required source phrases cached')
    if args.check and missing: raise SystemExit(1)

if __name__=='__main__': main()

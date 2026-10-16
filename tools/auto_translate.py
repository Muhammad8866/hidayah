"""Build-time translations for new UI copy. Credentials never enter the APK.

Legacy copy is inventoried once; new literals must use localization helpers.
Long-form/dynamic copy goes in localization/source.json using {name} tokens.
"""
import argparse
from collections import Counter
import json
import os
from pathlib import Path
import re
import sys
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
QUOTED = r'"(?:\\.|[^"\\])*"'
TOKENS = re.compile(r'\{[A-Za-z0-9_]+\}|%\d*\$?[sdfox]|[\u0600-\u06ff]+')
CALL = re.compile(r'\b(LocalizedText|LocalizedPhraseText|localizedUiText|localizeUiText|localizeUiTemplate|localizePhrase|Text|MaterialText)\s*\(\s*(?:(?:text|template)\s*=\s*)?(' + QUOTED + r')')

# QUOTED stops at quotes nested inside ${...}, so a value may end in an unclosed interpolation.
INTERPOLATION = re.compile(r'\$\{(?:[^{}]|\{[^{}]*\})*(?:\}|$)|\$[A-Za-z_]\w*')

def read_json(path, default):
    return json.loads(path.read_text(encoding='utf-8')) if path.exists() else default

def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    content = json.dumps(data, ensure_ascii=False, indent=2, sort_keys=True) + '\n'
    if not path.exists() or path.read_text(encoding='utf-8') != content:
        temporary = path.with_suffix('.tmp')
        temporary.write_text(content, encoding='utf-8')
        temporary.replace(path)

def languages():
    source = (ROOT / 'app/src/main/java/com/example/viewmodel/MainViewModel.kt').read_text(encoding='utf-8')
    block = source.split('val supported = listOf(', 1)[1].split('\n        )', 1)[0]
    return dict(re.findall(r'AppLanguage\("([^"]+)",\s*"([^"]+)"', block))

def scan():
    found = {}
    # Every flavor's UI copy shares one catalog: main plus the play and foss source sets.
    paths = [p for d in ('main', 'play', 'foss') for p in (ROOT / 'app/src' / d / 'java').rglob('*.kt')]
    for path in sorted(paths):
        if '/localization/' in path.as_posix():
            continue
        source = path.read_text(encoding='utf-8')
        # Comments are masked without touching strings, keeping line diagnostics reliable.
        source = re.sub(r'"""[\s\S]*?"""|' + QUOTED + r'|/\*[\s\S]*?\*/|//[^\n]*',
                        lambda m: m[0] if m[0].startswith('"') else re.sub(r'[^\n]', ' ', m[0]), source)
        for match in CALL.finditer(source):
            value = json.loads(match[2].replace('\\$', '$'))
            if not re.search('[A-Za-z]', value):
                continue
            kind = 'raw' if match[1] in {'Text', 'MaterialText'} else 'localized'
            # Raw text built only from interpolated values ("${a} · $b") has no copy to translate.
            if kind == 'raw' and not re.search('[A-Za-z]', INTERPOLATION.sub('', value)):
                continue
            identity = kind + ':' + value
            found[identity] = (value, kind, f'{path.relative_to(ROOT)}:{source[:match.start()].count(chr(10)) + 1}')
    return found

def validate(source, translated):
    if not isinstance(translated, str) or not translated.strip():
        raise ValueError('Translation is blank or not text')
    source_tokens = Counter(TOKENS.findall(source))
    target_tokens = Counter(TOKENS.findall(translated))
    for token, count in source_tokens.items():
        if re.fullmatch(r'[\u0600-\u06ff]+', token):
            # Arabic/Persian targets introduce their own Arabic-script words;
            # only the original devotional runs must remain unchanged.
            if target_tokens[token] < count:
                raise ValueError('Translation changed Arabic source text')
        elif target_tokens[token] != count:
            raise ValueError('Translation changed placeholders')
    if any(count != source_tokens[token] for token, count in target_tokens.items()
           if not re.fullmatch(r'[\u0600-\u06ff]+', token)):
        raise ValueError('Translation changed placeholders')
    if source.count('\n') != translated.count('\n'):
        raise ValueError('Translation changed paragraph structure')

def has_translation_key():
    key = os.environ.get('TRANSLATION_API_KEY', '')
    return bool(key) and not key.startswith(('YOUR_', 'MY_'))

def translate(code, name, texts):
    key = os.environ.get('TRANSLATION_API_KEY', '')
    if not has_translation_key():
        raise ValueError('Set TRANSLATION_API_KEY in your build environment to generate new translations. Never put it in Android .env.')
    model = os.environ.get('TRANSLATION_MODEL', 'gemini-3.8-flash')
    if not re.fullmatch(r'[A-Za-z0-9._-]+', model):
        raise ValueError('Invalid TRANSLATION_MODEL')
    prompt = ('Translate each English UI/content string into ' + name + ' (language code ' + code + '). '
              'For hinglish use Roman Hindi. For Kashmiri/Sindhi use Arabic script, Manipuri Bengali script, '
              'Punjabi Gurmukhi, Santali Ol Chiki. Preserve all {placeholders}, printf tokens, Arabic devotional '
              'text and newline counts exactly. Preserve religious meaning; add no commentary. '
              'Return a JSON array of translated strings in the same order. Input: ' + json.dumps(texts, ensure_ascii=False))
    body = {'contents': [{'parts': [{'text': prompt}]}],
            'generationConfig': {'responseMimeType': 'application/json', 'responseSchema': {'type': 'ARRAY', 'items': {'type': 'STRING'}}}}
    request = urllib.request.Request(f'https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent',
        data=json.dumps(body).encode(), headers={'Content-Type': 'application/json', 'x-goog-api-key': key}, method='POST')
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            payload = json.load(response)
    except urllib.error.HTTPError as exc:
        raise ValueError(f'Translation API HTTP {exc.code}; check credentials, quota and model. Cached entries are retained.') from None
    except urllib.error.URLError:
        raise ValueError('Translation API unavailable; cached entries are retained.') from None
    try:
        parts = payload['candidates'][0]['content']['parts']
        outputs = json.loads(''.join(p.get('text', '') for p in parts if not p.get('thought')))
    except (KeyError, IndexError, TypeError, json.JSONDecodeError):
        raise ValueError('Translation API returned no valid JSON translations') from None
    if not isinstance(outputs, list) or len(outputs) != len(texts):
        raise ValueError('Translation API returned incorrect batch length')
    for source, output in zip(texts, outputs):
        validate(source, output)
    return outputs

def generate(packs, output):
    def write_kotlin(path, body):
        if not path.exists() or path.read_text(encoding='utf-8') != body:
            path.write_text(body, encoding='utf-8')
    def quote(value):
        return json.dumps(value, ensure_ascii=False).replace('$', '\\$')
    output.mkdir(parents=True, exist_ok=True)
    loaders = []
    for code, rows in sorted(packs.items()):
        entries = sorted(rows.items())
        names = []
        for offset in range(0, len(entries), 60):
            name = f'AutoTranslation_{code}_{offset // 60}'
            names.append(name + '.phrases')
            body = 'package com.example.ui.localization\n\ninternal object ' + name + ' {\n    val phrases = mapOf(\n'
            body += ''.join(f'        {quote(k)} to {quote(v)},\n' for k, v in entries[offset:offset + 60])
            body += '    )\n}\n'
            write_kotlin(output / (name + '.kt'), body)
        if names:
            loaders.append(f'        {quote(code)} to {{ {" + ".join(names)} }},\n')
    body = ('package com.example.ui.localization\n\n/** Generated machine drafts; review pending. */\n'
            'internal object AutoTranslationPhrases {\n'
            '    private val loaders: Map<String, () -> Map<String, String>> = mapOf(\n' + ''.join(loaders) + '    )\n'
            '    private val loaded = java.util.concurrent.ConcurrentHashMap<String, Map<String, String>>()\n'
            '    fun lookup(text: String, language: String): String? = loaders[language]?.let { load ->\n'
            '        loaded.getOrPut(language) { load() }[text]\n    }\n}\n')
    write_kotlin(output / 'AutoTranslationPhrases.kt', body)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--initialize-baseline', action='store_true')
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--output', type=Path, default=ROOT / 'app/build/generated/autoTranslations')
    args = parser.parse_args()
    folder = ROOT / 'localization'
    baseline_path = folder / 'legacy-baseline.json'
    found = scan()
    if args.initialize_baseline:
        if baseline_path.exists():
            raise ValueError('Legacy baseline already exists; refusing to hide new untranslated copy')
        write_json(baseline_path, sorted(found))
    if not baseline_path.exists():
        raise ValueError('Missing legacy-baseline.json')
    legacy = set(read_json(baseline_path, []))
    catalog = read_json(folder / 'source.json', [])
    if not isinstance(catalog, list) or any(not isinstance(s, str) or not s.strip() for s in catalog):
        raise ValueError('localization/source.json must be an array of nonblank English strings')
    sources = set(catalog)
    for identity, (value, kind, location) in found.items():
        if identity in legacy:
            continue
        if kind == 'raw':
            raise ValueError(f'{location}: new UI text must use LocalizedText/localizeUiText: {value}')
        if '$' in value:
            raise ValueError(f'{location}: dynamic copy must use localizeUiTemplate with {{placeholders}} registered in localization/source.json')
        sources.add(value)
    supported = languages()
    packs = {}
    untranslated = set()
    for code, name in supported.items():
        if code == 'en':
            continue
        path = folder / 'translations' / f'{code}.json'
        cache = read_json(path, {})
        for source in sources & cache.keys():
            validate(source, cache[source])
        missing = sorted(sources - cache.keys())
        if missing and args.check:
            raise ValueError(f'{code}: {len(missing)} missing automatic translations; run the build with TRANSLATION_API_KEY')
        if missing and not has_translation_key():
            # Without a key the build still succeeds; these phrases show in English until translated.
            untranslated.update(missing)
            missing = []
        for offset in range(0, len(missing), 12):
            batch = missing[offset:offset + 12]
            cache.update(zip(batch, translate(code, name, batch)))
            write_json(path, cache)
            print(f'{code}: translated {min(offset + 12, len(missing))}/{len(missing)}', flush=True)
        packs[code] = {s: cache[s] for s in sources if s in cache}
    if untranslated:
        print(f'Warning: {len(untranslated)} new phrases have no translations and will show in English: '
              + ', '.join(sorted(untranslated)), flush=True)
    if not args.check:
        generate(packs, args.output)
    print(f'Automatic localization: {len(sources)} new phrases covered in {len(supported)} languages; legacy gaps remain separate.')

if __name__ == '__main__':
    try:
        main()
    except (ValueError, OSError) as exc:
        print(f'Automatic localization failed: {exc}', file=sys.stderr)
        sys.exit(1)

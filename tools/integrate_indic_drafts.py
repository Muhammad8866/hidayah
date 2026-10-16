"""Materialize cached IndicTrans2 drafts into Android source after translation completes."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = json.loads((ROOT / 'tools/indic_translation_sources.json').read_text(encoding='utf-8'))
CACHE = ROOT / 'tmp/indic_translation_cache'
MODEL = ROOT / 'app/src/main/java/com/example/domain/model/i18n'
UI = ROOT / 'app/src/main/java/com/example/ui/localization'
NAMES = ROOT / 'app/src/main/java/com/example/names'
CODES = ('as', 'bn', 'brx', 'doi', 'gu', 'kn', 'ks', 'kok', 'mai', 'ml', 'mni', 'mr', 'ne', 'or', 'pa', 'sa', 'sat', 'sd', 'ta', 'te')

def read_translations(path):
    values=json.loads(path.read_text(encoding='utf-8'))
    corrections=ROOT/'localization/quality-review-draft-corrections.json'
    if corrections.exists():
        values.update(json.loads(corrections.read_text(encoding='utf-8')).get(path.stem,{}))
    return values

def quoted(value):
    return json.dumps(value, ensure_ascii=False).replace('$', '\\$')

def existing_ids(code):
    return {m.group(1) for p in MODEL.glob(f'Dua{code.capitalize()}*.kt')
            if 'Draft' not in p.name
            for m in re.finditer(r'"(dua_[^"]+)"\s+to\s+DuaText\(', p.read_text(encoding='utf-8'))}

def chunks(items, size):
    return [items[i:i + size] for i in range(0, len(items), size)]

def write_existing(path, value):
    original = path.read_bytes()
    newline = '\r\n' if b'\r\n' in original else '\n'
    path.write_bytes(value.replace('\n', newline).encode('utf-8'))

def main():
    definitions = []
    name_definitions = []
    # Validate every cache before creating any source files.
    for code in CODES:
        path = CACHE / f'{code}.json'
        if not path.exists():
            raise SystemExit(f'Missing cache: {code}')
        translated = read_translations(path)
        needed = {value for key, row in SOURCE['duas'].items() if key not in existing_ids(code) for value in row}
        needed.update(value for row in SOURCE['features'].values() for value in row)
        missing = needed - translated.keys()
        if missing:
            raise SystemExit(f'{code}: {len(missing)} source phrases untranslated')
    registry = (MODEL / 'DuaTranslations.kt').read_text(encoding='utf-8')
    app = (UI / 'AppLocalization.kt').read_text(encoding='utf-8')
    picker_path = ROOT / 'app/src/main/java/com/example/viewmodel/MainViewModel.kt'
    picker = picker_path.read_text(encoding='utf-8')
    for code in CODES:
        path = CACHE / f'{code}.json'
        if not path.exists():
            raise SystemExit(f'Missing cache: {code}')
        translated = read_translations(path)
        needed = {value for key, row in SOURCE['duas'].items() if key not in existing_ids(code) for value in row}
        needed.update(value for row in SOURCE['features'].values() for value in row)
        missing = needed - translated.keys()
        if missing:
            raise SystemExit(f'{code}: {len(missing)} source phrases untranslated')
        name = code.capitalize()
        dua_rows = [(key, row) for key, row in SOURCE['duas'].items() if key not in existing_ids(code)]
        objects = []
        for index, group in enumerate(chunks(dua_rows, 40), 1):
            object_name = f'Dua{name}Draft{index}'
            objects.append(object_name)
            body = [f'package com.example.domain.model.i18n\n\n/** Machine translated draft; religious and linguistic review pending. */\ninternal object {object_name} {{',
                    '    val MAP: Map<String, DuaText> = mapOf(']
            for key, row in group:
                body.append(f'        {quoted(key)} to DuaText({", ".join(quoted(translated[value]) for value in row)}),')
            body += ['    )', '}', '']
            (MODEL / f'{object_name}.kt').write_text('\n'.join(body), encoding='utf-8')
        if objects:
            registry = re.sub(rf'\s*\+\s*Dua{name}Draft\d+\.MAP', '', registry)
            pattern = rf'("{code}" to \{{)([^}}]+)(\}})'
            registry, count = re.subn(pattern, lambda m: m.group(1) + ' ' + ' + '.join(
                [value for value in re.split(r'\s*\+\s*', m.group(2).strip()) if value and not re.fullmatch(rf'Dua{name}Draft\d+\.MAP', value)]
                + [f'{obj}.MAP' for obj in objects]) + ' ' + m.group(3), registry, count=1)
            if count == 0:
                marker = '    private val loaders: Map<String, () -> Map<String, DuaText>> = mapOf(\n'
                registry = registry.replace(marker, marker + f'        "{code}" to {{ {" + ".join(f"{obj}.MAP" for obj in objects)} }},\n', 1)
        feature_keys = list(dict.fromkeys(value for group, row in SOURCE['features'].items()
                                          if group != 'NamesMeanings' for value in row))
        ui_objects = []
        for index, group in enumerate(chunks(feature_keys, 80), 1):
            object_name = f'Indic{name}Draft{index}'
            ui_objects.append(object_name)
            body = [f'package com.example.ui.localization\n\n/** Machine translated draft; review pending. */\ninternal object {object_name} {{',
                    '    val MAP: Map<String, String> = mapOf(']
            for key in group:
                body.append(f'        {quoted(key)} to {quoted(translated[key])},')
            body += ['    )', '}', '']
            (UI / f'{object_name}.kt').write_text('\n'.join(body), encoding='utf-8')
        all_ui = ' + '.join(f'{obj}.MAP' for obj in ui_objects)
        marker = f'    "{code}" to {{ {all_ui} }},'
        definitions.append(marker)
        name_objects = []
        for index, group in enumerate(chunks(SOURCE['features']['NamesMeanings'], 75), 1):
            object_name = f'Indic{name}NameDraft{index}'
            name_objects.append(object_name)
            body = [f'package com.example.names\n\n/** Machine translated draft; religious and linguistic review pending. */\ninternal object {object_name} {{',
                    '    val MAP: Map<String, String> = mapOf(']
            for key in group:
                body.append(f'        {quoted(key)} to {quoted(translated[key])},')
            body += ['    )', '}', '']
            (NAMES / f'{object_name}.kt').write_text('\n'.join(body), encoding='utf-8')
        name_definitions.append(f'        "{code}" to {{ {" + ".join(f"{obj}.MAP" for obj in name_objects)} }},')
        picker, count = re.subn(rf'(AppLanguage\("{code}", [^\n]*?)(\),)', lambda m: m.group(1) + ('' if 'reviewPending = true' in m.group(1) else ', reviewPending = true') + m.group(2), picker, count=1)
        if count != 1:
            raise SystemExit(f'Missing picker for {code}')
        print(f'{code}: {len(dua_rows)} duas, {len(feature_keys)} UI phrases')
    write_existing(MODEL / 'DuaTranslations.kt', registry)
    write_existing(picker_path, picker)
    definitions_text = ('package com.example.ui.localization\n\n/** Machine translated drafts; linguistic and religious review pending. */\n'
                        'internal object IndicDraftPhrases {\n'
                        '    private val loaders: Map<String, () -> Map<String, String>> = mapOf(\n'
                        + '\n'.join(definitions) + '\n    )\n'
                        '    private val loaded = java.util.concurrent.ConcurrentHashMap<String, Map<String, String>>()\n'
                        '    fun forLanguage(code: String): Map<String, String> =\n'
                        '        loaders[code]?.let { load -> loaded.getOrPut(code) { load() } } ?: emptyMap()\n}\n')
    (UI / 'IndicDraftPhrases.kt').write_text(definitions_text, encoding='utf-8')
    names_text = ('package com.example.names\n\n/** Machine translated name meanings; linguistic review pending. */\n'
                  'internal object NameMeaningTranslations {\n'
                  '    private val loaders: Map<String, () -> Map<String, String>> = mapOf(\n'
                  + '\n'.join(name_definitions) + '\n    )\n'
                  '    private val loaded = java.util.concurrent.ConcurrentHashMap<String, Map<String, String>>()\n'
                  '    fun of(code: String, english: String): String =\n'
                  '        loaders[code]?.let { load -> loaded.getOrPut(code) { load() } }?.get(english) ?: english\n}\n')
    (NAMES / 'NameMeaningTranslations.kt').write_text(names_text, encoding='utf-8')
    app = app.replace(
        'duaScreenPhrases.getValue(code) + mapOf(',
        'IndicDraftPhrases.forLanguage(code).filterKeys { (it !in pack.phrases || pack.phrases[it] == it) && it !in duaScreenPhrases.getValue(code) } + duaScreenPhrases.getValue(code) + mapOf(',
        1 if 'IndicDraftPhrases.forLanguage(code).filterKeys' not in app else 0,
    )
    write_existing(UI / 'AppLocalization.kt', app)

if __name__ == '__main__':
    main()

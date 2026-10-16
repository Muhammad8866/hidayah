"""Complete newly added daily-tool phrases using the existing local Indic model.

Generated translations remain drafts. Existing bundled translations take precedence.
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TARGETS = {
    'as': 'asm_Beng', 'bn': 'ben_Beng', 'brx': 'brx_Deva', 'doi': 'doi_Deva',
    'gu': 'guj_Gujr', 'kn': 'kan_Knda', 'ks': 'kas_Arab', 'kok': 'gom_Deva',
    'mai': 'mai_Deva', 'ml': 'mal_Mlym', 'mni': 'mni_Beng', 'mr': 'mar_Deva',
    'ne': 'npi_Deva', 'or': 'ory_Orya', 'pa': 'pan_Guru', 'sa': 'san_Deva',
    'sat': 'sat_Olck', 'sd': 'snd_Arab', 'ta': 'tam_Taml', 'te': 'tel_Telu',
}
PRESERVE = re.compile(r'(\{[A-Za-z0-9_]+\}|\n+|[\u0600-\u06ff]+(?:[ \t]+[\u0600-\u06ff]+)*)')

def split_source(source):
    result = []
    for part in PRESERVE.split(source):
        if not part:
            continue
        if PRESERVE.fullmatch(part) or not part.strip():
            result.append((part, None, '', ''))
            continue
        bullet = re.match(r'^(\s*(?:[-*]|\d+\.)\s+)', part)
        prefix = bullet.group(1) if bullet else ''
        body = part[len(prefix):]
        lead = body[:len(body) - len(body.lstrip())]
        trail = body[len(body.rstrip()):]
        result.append((part, body.strip(), prefix + lead, trail))
    return result

UI = ROOT / 'app/src/main/java/com/example/ui/localization'

def keys(text):
    return {json.loads(m) for m in re.findall(r'("(?:\\.|[^"\\])*")\s+to\s+', text)}

def main():
    source = (UI / 'DailyToolsTranslations.kt').read_text(encoding='utf-8')
    reference = keys(source.split('"hi" to mapOf(', 1)[1].split('"ur" to mapOf(', 1)[0])
    model = tokenizer = processor = None
    for code, target in TARGETS.items():
        existing = set().union(*(keys(p.read_text(encoding='utf-8')) for p in UI.glob(f'Indic{code.capitalize()}Draft*.kt')))
        missing = sorted(reference - existing)
        path = ROOT / f'tmp/indic_translation_cache/{code}.json'
        cache = json.loads(path.read_text(encoding='utf-8'))
        pending = [key for key in missing if key not in cache]
        print(f'{code}: {len(missing)} missing, {len(pending)} require translation', flush=True)
        if pending and model is None:
            import torch
            from transformers import AutoModelForSeq2SeqLM, AutoTokenizer
            from IndicTransToolkit import IndicProcessor
            torch.set_num_threads(4)
            model_dir = ROOT / 'tmp/indictrans-model'
            tokenizer = AutoTokenizer.from_pretrained(model_dir, trust_remote_code=True, local_files_only=True)
            model = AutoModelForSeq2SeqLM.from_pretrained(model_dir, trust_remote_code=True, local_files_only=True).eval()
            processor = IndicProcessor(inference=True)
        for start in range(0, len(pending), 8):
            batch = pending[start:start + 8]
            # Batch all text fragments while preserving placeholders and source text.
            split = [(key, split_source(key)) for key in batch]
            bodies = [body for _, parts in split for _, body, _, _ in parts if body is not None]
            if bodies:
                prepared = processor.preprocess_batch(bodies, src_lang='eng_Latn', tgt_lang=target)
                tokens = tokenizer(prepared, padding=True, return_tensors='pt')
                with torch.inference_mode():
                    output = model.generate(**tokens, num_beams=1, max_new_tokens=220)
                values = iter(processor.postprocess_batch(tokenizer.batch_decode(output, skip_special_tokens=True), lang=target))
            else:
                values = iter([])
            for key, parts in split:
                cache[key] = ''.join(original if body is None else prefix + next(values).strip() + trail for original, body, prefix, trail in parts)
                if not cache[key].strip():
                    raise ValueError(f'{code}: blank translation for {key}')
            path.write_text(json.dumps(cache, ensure_ascii=False), encoding='utf-8')
            print(f'{code}: {min(start + 8, len(pending))}/{len(pending)}', flush=True)
        if missing:
            def quote(s):
                return json.dumps(s, ensure_ascii=False).replace('$', '\\$')
            # Existing registry already loads Draft29; extend it without altering precedence.
            destination = UI / f'Indic{code.capitalize()}Draft29.kt'
            text = destination.read_text(encoding='utf-8')
            insertion = ''.join(f'        {quote(key)} to {quote(cache[key])},\n' for key in missing)
            marker = '    )'
            assert text.count(marker) == 1, destination
            destination.write_text(text.replace(marker, insertion + marker), encoding='utf-8')
    print('Daily-tool draft coverage complete.', flush=True)

if __name__ == '__main__':
    main()

from pathlib import Path
import re, json
root=Path(__file__).resolve().parents[2]
p=next((root/'app/build/generated/source/buildConfig/debug').rglob('BuildConfig.java'))
s=p.read_text()
m=re.search(r'GEMINI_API_KEY\s*=\s*"([^"]*)"',s)
d={'gemini_field_present':bool(m),'gemini_nonplaceholder':bool(m and m[1] and not m[1].startswith(('MY_','YOUR_'))),'translation_key_field_present':'TRANSLATION_API_KEY' in s}
(Path(__file__).parent/'build-fields.json').write_text(json.dumps(d,indent=2))
print(json.dumps(d))

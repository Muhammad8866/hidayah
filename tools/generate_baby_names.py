"""Generate offline Kotlin data from reviewed official-source name entries.

Run collect_baby_name_sources.py first when
refreshing source evidence. Normal builds use the checked-in Kotlin catalog.
"""
import json
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = "https://www.dawateislami.net/bookslibrary/ur/naam-rakhnay-kay-ahkam"
TITLE = "Naam Rakhne ke Ahkam — Maktaba-tul-Madinah"

ALIASES = {
    "Abdullah": "Abd Allah Abdallah",
    "Abdur Rahman": "Abdul Rahman Abdurrahman Abdulrahman",
    "Aisha": "Ayesha Aishah Aysha",
    "Aaliyah": "Aliya Alia Aliyah Aaliya",
    "Amina": "Aaminah Aminah Aamna",
    "Amin": "Ameen",
    "Bashir": "Basheer",
    "Hasan": "Hasan",
    "Hassan": "Hassaan",
    "Husayn": "Hussain Hussein Husain",
    "Muhammad": "Mohammed Mohammad Muhammed",
    "Mustafa": "Mostafa",
    "Salim": "Saleem Salem",
    "Fatima": "Fatimah Fatma",
    "Habiba": "Habeeba",
    "Jamila": "Jameela",
    "Karima": "Kareema",
    "Karim": "Kareem",
    "Umar": "Omar",
    "Uthman": "Usman Osman Othman",
    "Zayn": "Zain Zein",
    "Zaynab": "Zainab Zeynab",
    "Hudhayfah": "Huzaifa Huzayfa Hudaifa",
    "Muadh": "Muaz Muaaz",
    "Qurrat ul Ayn": "Qurratulain Quratulain Qurrat ul Ain",
    "Rayyan": "Rayan Raiyan",
    "Rumaysa": "Rumaisa Rumaisha Rumaysaa",
    "Sulaiman": "Suleiman Sulayman Solomon",
    "Dawud": "Dawood Daud",
    "Yusuf": "Yousuf Youssef Yousaf",
    "Ilyas": "Ilias Elias",
    "Ishaq": "Ishak Is-haq",
    "Junayd": "Junaid",
    "Zubayr": "Zubair",
    "Abdut Tawwab": "Abdul Tawwab Abdul Tawab Abdut Tawab",
    "As'ad": "Asaad Assad Asad",
    "Haakim": "Haakim",
    "Nur": "Noor Nour",
    "Ziya": "Zia Diya",
    "Maryam": "Mariam Maryum",
    "Khadija": "Khadijah Khadeeja Khadeejah",
    "Hajrah": "Hajar Hajra Hajirah Hajeera",
    "Dhulfiqar": "Zulfiqar Zulfikar Dhul Fiqar",
    "Nasibah": "Naseebah",
}

def q(value):
    return json.dumps(value, ensure_ascii=False).replace("$", "\\$")

def script(value):
    # Display the source's Urdu/Arabic name in unvowelled Arabic letters.
    value = ''.join(c for c in value if unicodedata.category(c) != 'Mn')
    return value.translate(str.maketrans({'ک':'ك', 'ی':'ي', 'ہ':'ه', 'ھ':'ه'}))

def generate():
    candidates = json.loads((ROOT / 'tmp/baby-name-sources/candidates.json').read_text(encoding='utf-8'))
    rows = []
    for line in (ROOT / 'tools/baby_names_reviewed.tsv').read_text(encoding='utf-8').splitlines()[1:]:
        idx, name, english, hinglish = line.split('|')
        c = candidates[int(idx)]
        urdu = c['urdu']
        if int(idx) == 507:
            urdu = urdu.split(')(', 1)[1]  # First parentheses contain an alternate spelling.
        if int(idx) == 515:
            urdu = urdu.split(')مُنْیَہ(', 1)[0].rstrip()  # Next name shares the paragraph.
        rows.append(dict(name=name, arabic=script(c['arabic']), gender='GIRL' if int(idx) >= 391 else 'BOY',
                         english=english, hinglish=hinglish, aliases=ALIASES.get(name,''),
                         sourceUrl=f"{BASE}/page-{c['page']}", sourceTitle=TITLE,
                         sourceSpelling=c['arabic'], sourceMeaningUrdu=urdu))
    # Ahmad is explicitly named in the prose introducing the approved list.
    rows.append(dict(name='Ahmad', arabic='أحمد', gender='BOY',
                     english="A personal name of Prophet Muhammad", hinglish="Nabi Muhammad ka zaati naam",
                     aliases='Ahmed', sourceUrl=f'{BASE}/page-45', sourceTitle=TITLE,
                     sourceSpelling='احمد', sourceMeaningUrdu='حضور کے علَمِ ذات (ذاتی نام) دو ہیں: کتبِ سابقہ میں احمد ہے'))
    # Two names span paragraph boundaries or share a paragraph on page 61.
    for name, spelling, urdu, english, hinglish, aliases in [
        ('Munyah', 'مُنْیَہ', 'تمنّا، خواہش', 'Wish; desire', 'Tamanna aur khwahish', 'Munia Munya'),
        ('Halah', 'ہَالَہ', 'چاند کے چاروں اطراف کا روشن دائرہ', 'Halo around the moon', 'Chand ke gird roshan daira', 'Hala'),
    ]:
        rows.append(dict(name=name, arabic=script(spelling), gender='GIRL', english=english,
                         hinglish=hinglish, aliases=aliases, sourceUrl=f'{BASE}/page-61',
                         sourceTitle=TITLE, sourceSpelling=spelling, sourceMeaningUrdu=urdu))
    # Additional individually researched names; Urdu and Hinglish are editorial
    # translations, not quotations from the English-language reference pages.
    reviewed_lines = []
    for filename in ('baby_names_additional_reviewed.tsv', 'baby_names_expansion_reviewed.tsv', 'baby_names_devotional_reviewed.tsv'):
        reviewed_lines.extend((ROOT / 'tools' / filename).read_text(encoding='utf-8').splitlines()[1:])
    for line in reviewed_lines:
        name, gender, arabic, english, hinglish, urdu, aliases, source = line.split('|')
        for resolved_gender in (('BOY', 'GIRL') if gender == 'BOTH' else (gender,)):
            rows.append(dict(name=name, arabic=arabic, gender=resolved_gender,
                             english=english, hinglish=hinglish, aliases=aliases,
                             sourceUrl=f'https://quranicnames.com/{source}/',
                             sourceTitle='QuranicNames — Arabic name research',
                             sourceSpelling=arabic, sourceMeaningUrdu=urdu))
    # Preserve existing IDs when a newly researched spelling is an alias.
    for row in rows:
        row['aliases'] = ' '.join(dict.fromkeys((row['aliases'] + ' ' + {
            'Rufaydah': 'Rafeeda Rafida Rafeedah',
            'Aqilah': 'Akila Akeela',
            'Suhayl': 'Suhail',
            'Naim': 'Naeem',
            'Humayd': 'Humaid',
            'Dhakwan': 'Zakwan',
            'Safiyyah': 'Safiya Safiyah',
            'Rabiah': 'Rabia Raabia Rabiya',
        }.get(row['name'], '')).split()))
    keys = [(r['gender'],r['name']) for r in rows]
    assert len(keys) == len(set(keys)), 'Duplicate display names within gender'
    assert all(all(r[k].strip() for k in ('name','arabic','english','hinglish','sourceSpelling','sourceMeaningUrdu')) for r in rows)
    rows.sort(key=lambda r:(r['name'],r['gender']))
    doc = dict(source=BASE, publisher='Maktaba-tul-Madinah / Dawat-e-Islami',
               reviewedOn='2026-10-11', reviewEvidence='https://news.dawateislami.net/Naam-Rakhnay-Kay-Ahkam-ka-taaruf',
               additionalSource='https://quranicnames.com/',
               scope='Recommended names section, online pages 43–61, plus individually reviewed QuranicNames Arabic-name research. Meanings are translations/short summaries of the cited source, not independent etymological claims. Additional Urdu and Hinglish meanings are editorial translations.',
               names=rows)
    (ROOT/'docs/baby-name-sources.json').write_text(json.dumps(doc,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    text = ['package com.example.names', '', '// Generated by tools/generate_baby_names.py; review evidence: docs/baby-name-sources.json.',
            'internal object BabyNameCatalog {', '    val names: List<BabyName> = buildList {']
    chunks=[rows[i:i+60] for i in range(0,len(rows),60)]
    text += [f'        addAll(part{i}())' for i in range(len(chunks))]
    text += ['    }', '']
    for i,chunk in enumerate(chunks):
        text += [f'    private fun part{i}(): List<BabyName> = listOf(']
        for r in chunk:
            args=[q(r['name']),q(r['arabic']),f"BabyGender.{r['gender']}",q(r['english']),q(r['hinglish']),q(r['aliases']),q(r['sourceUrl']),q(r['sourceTitle']),q(r['sourceSpelling']),q(r['sourceMeaningUrdu'])]
            text.append('        BabyName('+', '.join(args)+'),')
        text += ['    )', '']
    text += ['}', '']
    (ROOT/'app/src/main/java/com/example/names/BabyNameCatalog.kt').write_text('\n'.join(text),encoding='utf-8')
    print(f"Generated {len(rows)} names: {sum(r['gender']=='BOY' for r in rows)} boys, {sum(r['gender']=='GIRL' for r in rows)} girls")

if __name__ == '__main__':
    generate()

from pathlib import Path
import xml.etree.ElementTree as ET
from collections import Counter
import json
root=Path(__file__).resolve().parents[2]
tree=ET.parse(root/'app/build/reports/lint-results-debug.xml')
rows=[]
for x in tree.getroot().findall('issue'):
    loc=x.find('location')
    rows.append(dict(id=x.attrib['id'],severity=x.attrib['severity'],message=x.attrib.get('message',''),location=loc.attrib if loc is not None else {}))
counts=Counter((x['severity'],x['id']) for x in rows)
d=dict(counts=[dict(severity=k[0],id=k[1],count=v) for k,v in sorted(counts.items())],issues=rows)
(Path(__file__).parent/'lint-results.json').write_text(json.dumps(d,indent=2),encoding='utf-8')
for x in d['counts']: print(x)
for x in rows:
    if x['severity']=='Error' and x['id']!='NewApi': print(json.dumps(x))

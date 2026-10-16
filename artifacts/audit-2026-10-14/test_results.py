from pathlib import Path
import xml.etree.ElementTree as ET
import json
root=Path(__file__).resolve().parents[2]
results=[]
counts=dict(tests=0,failures=0,errors=0,skipped=0)
for p in (root/'app/build/test-results/testDebugUnitTest').glob('*.xml'):
    tree=ET.parse(p).getroot()
    for k in counts: counts[k]+=int(tree.attrib.get(k,0))
    for t in tree.findall('testcase'):
        f=t.find('failure')
        if f is not None:
            results.append(dict(suite=tree.attrib['name'],test=t.attrib['name'],message=f.attrib.get('message',''),detail=(f.text or '')[:3000]))
d=dict(counts=counts,failures=results)
(Path(__file__).parent/'test-results.json').write_text(json.dumps(d,indent=2),encoding='utf-8')
print(json.dumps(d,indent=2))

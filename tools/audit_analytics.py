"""Review the static Analytics control inventory without running or sending telemetry."""
from pathlib import Path
from collections import Counter
import csv
import re

root = Path(__file__).resolve().parents[1]
rows = []
ui = re.compile(r'AppAnalytics\.(interaction|trackAction|trackItemAction|trackOptionalAction)\("([a-z0-9_]+)",\s*"([a-z0-9_]+)"(?:,\s*"([a-z0-9_]+)")?')
observers = re.compile(r'\b(?:Analytics(Screen|Search|Scroll|ReaderSession)|rememberAnalytics(ScrollState|LazyListState))\("([a-z0-9_]+)"')
domain = re.compile(r'AppAnalytics\.(media|mediaState|playbackState|permission|setting|content|milestone|prayerAlert|widget|launch)\((?:if \([^)]*\) )?"([a-z0-9_]+)"(?:,\s*"([a-z0-9_]+)")?')
events = {'media': 'media_action', 'mediaState': 'media_state', 'playbackState': 'media_state',
          'permission': 'permission_result', 'setting': 'settings_change', 'content': 'content_select',
          'milestone': 'feature_milestone', 'prayerAlert': 'prayer_alert', 'widget': 'widget_event',
          'launch': 'app_launch'}
generated = re.compile(r'_(tap|back|dismiss|refresh|toggle|change_complete|search_submit)_\d+$')
for path in sorted(p for d in ('main', 'play') for p in (root / 'app/src' / d / 'java').rglob('*.kt')):
    if 'core/analytics' in path.as_posix(): continue
    source = path.read_text(encoding='utf-8')
    def record(match, event, screen, component='', action=''):
        rows.append((path.relative_to(root).as_posix(), source.count('\n',0,match.start())+1, screen, event, component, action))
    for match in ui.finditer(source):
        record(match, 'ui_interaction', match[2], match[3], match[4] or 'tap')
    for match in observers.finditer(source):
        kind = match[1] or 'Scroll'
        record(match, {'Screen':'screen_view','Search':'search_usage','Scroll':'scroll_progress','ReaderSession':'reader_session'}[kind], match[3])
    for match in domain.finditer(source):
        record(match, events[match[1]], match[2], action=match[3] or '')
rows.sort()
with (root / 'docs/analytics-controls.csv').open('w', newline='', encoding='utf-8') as handle:
    writer = csv.writer(handle)
    writer.writerow(('source','line','screen_or_feature','event','component','action'))
    writer.writerows(rows)
print(f'{len(rows)} static tracking sites across {len(set(r[0] for r in rows))} files')
for event, count in sorted(Counter(r[3] for r in rows).items()): print(f'{event}: {count}')
# Control IDs are written by hand so reports read as actions; a numbered suffix means one was missed.
unnamed = [r for r in rows if generated.search(r[4])]
print(f'generated-looking control IDs: {len(unnamed)}')
for r in unnamed: print(f'  {r[0]}:{r[1]} {r[4]}')
print('Network request/download lifecycle, foreground/background events and user properties are centralized separately.')

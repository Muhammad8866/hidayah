"""Verify the reviewed publisher links return PDF bytes (not HTML error pages)."""
import concurrent.futures
import json
import pathlib
import subprocess

ROOT = pathlib.Path(__file__).resolve().parents[1]
rows = json.loads((ROOT / 'docs/muslimah-book-sources.json').read_text(encoding='utf-8'))['editions']
cache = ROOT / 'tmp/muslimah-pdf-checks'
cache.mkdir(parents=True, exist_ok=True)

def verify(row):
    path = cache / (row['collection'] + '-' + row['language'] + '.pdf')
    subprocess.run(['curl.exe', '-f', '-sS', '-L', '--retry', '2', '--max-time', '60',
                    '--range', '0-1023', row['pdf'], '-o', str(path)], check=True)
    with path.open('rb') as stream:
        if not stream.read(1024).startswith(b'%PDF-'):
            raise ValueError('Not a PDF: ' + row['pdf'])
    return row['collection'] + ' / ' + row['language']

with concurrent.futures.ThreadPoolExecutor(max_workers=5) as pool:
    for result in pool.map(verify, rows):
        print('PDF OK:', result, flush=True)
print(f'All {len(rows)} publisher PDFs verified.')

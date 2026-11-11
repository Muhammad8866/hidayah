"""Audit English UI copy that reaches a localization helper without a whole-phrase translation.

localizeUiText() falls back to a word-by-word substitution when no whole phrase is known, which
renders half-translated text ("Log आज's नमाज़ें"). This tool lists, per language, every English
literal that reaches a helper (directly, through wrapper functions, through if/when branches,
through local vals, or through catalog fields such as QuranPdf.subtitle) and has no exact entry
in the Kotlin language maps or in the generated translations (localization/translations/*.json,
which only load when the phrase is listed in localization/source.json or scanned as new copy).

Usage (Windows: set PYTHONIOENCODING=utf8):
  python -I tools/audit_ui_phrases.py                 # per-language gap counts
  python -I tools/audit_ui_phrases.py --lang hi       # list Hindi gaps with source locations
  python -I tools/audit_ui_phrases.py --dump out.json # union of gaps as a JSON array
  python -I tools/audit_ui_phrases.py --dynamic       # helper calls built from "$interpolation"
  python -I tools/audit_ui_phrases.py --strict hi,hinglish,ur   # exit 1 if those have gaps
"""
import argparse
import bisect
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(Path(__file__).resolve().parent))
import auto_translate  # noqa: E402  (shared scanner, languages() and baseline rules)

LOC_DIR = ROOT / 'app/src/main/java/com/example/ui/localization'
SOURCE_SETS = ('main', 'play')
# Helpers that fall back to word-by-word substitution: an untranslated phrase renders mixed.
MIXING_HELPERS = {
    'localizeUiText': {(0, 'text')}, 'localizeUiTemplate': {(0, 'template')},
    'LocalizedText': {(0, 'text')}, 'localizedUiText': {(0, 'text')},
}
# Whole-phrase helpers show clean English when untranslated (content such as dua meanings).
PHRASE_HELPERS = {
    'localizePhrase': {(0, 'text')}, 'LocalizedPhraseText': {(0, 'text')},
    'localizedMoreSubtitle': {(0, 'text')},
}
BUILTIN_HELPERS = {**MIXING_HELPERS, **PHRASE_HELPERS}
# Field values that are content (not UI copy) or identifiers, never shown through a helper.
# Content catalogs with their own per-language tables (i18n/DuaTranslations, NameMeaningTranslations).
CONTENT_SOURCES = re.compile(r'names/BabyNameCatalog\.kt|domain/model/DuaEnglish\d*\.kt')
SKIP_FIELDS = {'id', 'fileName', 'sha256', 'url', 'key', 'route', 'icon', 'arabicText', 'arabic',
               'transliteration', 'reference', 'source', 'urduTranslation', 'tag', 'event', 'name_ar'}


# ---------------------------------------------------------------- Kotlin lexing

class KtFile:
    """Kotlin source with comments blanked and string literals indexed by their start offset."""

    def __init__(self, path):
        self.path = path
        self.rel = path.relative_to(ROOT).as_posix()
        text = path.read_text(encoding='utf-8')
        self.text = text
        masked = list(text)
        self.literals = {}  # start -> (end, value, dynamic)
        i, n = 0, len(text)
        while i < n:
            c = text[i]
            if text.startswith('//', i):
                j = text.find('\n', i)
                j = n if j < 0 else j
                masked[i:j] = ' ' * (j - i)
                i = j
            elif text.startswith('/*', i):
                j = text.find('*/', i + 2)
                j = n if j < 0 else j + 2
                masked[i:j] = [ch if ch == '\n' else ' ' for ch in text[i:j]]
                i = j
            elif text.startswith('"""', i):
                j = text.find('"""', i + 3)
                j = n if j < 0 else j + 3
                while j < n and text[j] == '"':
                    j += 1
                raw = text[i + 3:j - 3]
                self.literals[i] = (j, raw, '$' in raw)
                masked[i + 1:j - 1] = [ch if ch == '\n' else 'x' for ch in text[i + 1:j - 1]]
                i = j
            elif c == '"':
                j, value, dynamic = self._string(i)
                self.literals[i] = (j, value, dynamic)
                masked[i + 1:j - 1] = 'x' * (j - 1 - (i + 1))
                i = j
            elif c == "'":
                m = re.match(r"'(\\.|\\u[0-9a-fA-F]{4}|[^'\\])'", text[i:])
                if m:
                    masked[i:i + m.end()] = 'x' * m.end()
                    i += m.end()
                else:
                    i += 1
            else:
                i += 1
        self.masked = ''.join(masked)

    def _string(self, start):
        text, i, out, dynamic = self.text, start + 1, [], False
        escapes = {'n': '\n', 't': '\t', 'r': '\r', '"': '"', "'": "'", '\\': '\\', '$': '$', 'b': '\b'}
        while i < len(text):
            c = text[i]
            if c == '\\':
                nxt = text[i + 1]
                if nxt == 'u':
                    out.append(chr(int(text[i + 2:i + 6], 16)))
                    i += 6
                else:
                    out.append(escapes.get(nxt, nxt))
                    i += 2
                continue
            if c == '$' and i + 1 < len(text) and text[i + 1] == '{':
                dynamic = True
                depth, j = 0, i + 1
                while j < len(text):
                    if text[j] == '{':
                        depth += 1
                    elif text[j] == '}':
                        depth -= 1
                        if depth == 0:
                            break
                    elif text[j] == '"':  # nested string inside the template expression
                        j = self._string(j)[0] - 1
                    j += 1
                out.append(text[i:j + 1])
                i = j + 1
                continue
            if c == '$' and i + 1 < len(text) and (text[i + 1].isalpha() or text[i + 1] == '_'):
                dynamic = True
            if c == '"':
                return i + 1, ''.join(out), dynamic
            out.append(c)
            i += 1
        return i, ''.join(out), dynamic

    def line(self, offset):
        return self.text.count('\n', 0, offset) + 1

    def literals_in(self, start, end):
        if not hasattr(self, '_starts'):
            self._starts = sorted(self.literals)
        lo, hi = bisect.bisect_left(self._starts, start), bisect.bisect_left(self._starts, end)
        return [(s, *self.literals[s]) for s in self._starts[lo:hi]]

    def matching(self, open_index):
        """Index of the bracket closing the one at open_index (in the masked source)."""
        pairs = {'(': ')', '[': ']', '{': '}'}
        stack = []
        for j in range(open_index, len(self.masked)):
            ch = self.masked[j]
            if ch in pairs:
                stack.append(pairs[ch])
            elif ch in ')]}':
                if not stack or stack.pop() != ch:
                    return j
                if not stack:
                    return j
        return len(self.masked) - 1

    def split_args(self, open_index):
        """[(name or None, start, end)] for the top-level arguments of a call."""
        close = self.matching(open_index)
        args, depth, start = [], 0, open_index + 1
        for j in range(open_index + 1, close + 1):
            ch = self.masked[j]
            if ch in '([{':
                depth += 1
            elif ch in ')]}' and depth > 0:
                depth -= 1
            elif (ch == ',' and depth == 0) or j == close:
                seg = self.masked[start:j]
                if seg.strip():
                    m = re.match(r'\s*([A-Za-z_]\w*)\s*=(?!=)', seg)
                    if m:
                        args.append((m[1], start + m.end(), j))
                    else:
                        args.append((None, start, j))
                start = j + 1
        return args, close


def source_files():
    for d in SOURCE_SETS:
        for p in sorted((ROOT / 'app/src' / d / 'java').rglob('*.kt')):
            posix = p.as_posix()
            # Translation tables and generated content catalogs carry no helper calls of their own.
            if '/localization/' in posix or '/i18n/' in posix or re.search(r'/names/\w*(Data|Translations)\d*\.kt$', posix):
                continue
            yield p


# ---------------------------------------------------------------- collection

FUN_DEF = re.compile(r'\bfun\s+(?:<[^>]*>\s*)?(?:[A-Za-z_][\w.]*\.)?([A-Za-z_]\w*)\s*\(')


def function_defs(kt):
    """(name, params[(index, name)], body_start, body_end) for each fun in the file."""
    out = []
    for m in FUN_DEF.finditer(kt.masked):
        open_i = m.end() - 1
        args, close = kt.split_args(open_i)
        params = []
        for index, (_, s, e) in enumerate(args):
            pm = re.match(r'\s*(?:@\w+\s+)*(?:vararg\s+|crossinline\s+|noinline\s+)?([A-Za-z_]\w*)\s*:', kt.masked[s:e])
            if pm:
                params.append((index, pm[1]))
        rest = kt.masked[close + 1:close + 400]
        bm = re.match(r'[^{=]*?([{=])', rest)
        if not bm:
            continue
        body_start = close + 1 + bm.end() - 1
        if bm[1] == '{':
            body_end = kt.matching(body_start)
        else:
            nl = kt.masked.find('\n', body_start)
            body_end = len(kt.masked) if nl < 0 else nl
            # Single-expression bodies may continue on indented lines.
            while body_end < len(kt.masked):
                nxt = kt.masked[body_end + 1:kt.masked.find('\n', body_end + 1) if kt.masked.find('\n', body_end + 1) > 0 else len(kt.masked)]
                if re.match(r'\s+[.?+)]|\s{8,}\S', nxt) and not re.match(r'\s*(fun|val|var|@|private|internal|override)\b', nxt):
                    body_end = body_end + 1 + len(nxt)
                else:
                    break
        out.append((m[1], params, body_start, body_end))
    return out


def detect_wrappers(files, builtins):
    """Functions that pass one of their String parameters straight into a helper."""
    helpers = {k: set(v) for k, v in builtins.items()}
    homes = {}  # wrapper name -> files defining it as a wrapper
    defs = [(kt, d) for kt in files for d in function_defs(kt)]
    changed = True
    while changed:
        changed = False
        for kt, (name, params, bs, be) in defs:
            if name in BUILTIN_HELPERS:
                continue
            body = kt.masked[bs:be + 1]
            called = set(re.findall(r'(?<![\w.])([A-Za-z_]\w*)\s*\(', body))
            for index, pname in params:
                if not re.search(r'\b' + re.escape(pname) + r'\b', body):
                    continue
                for hname in [h for h in helpers if h in called]:
                    for hidx, hpname in list(helpers[hname]):
                        pat = (r'(?<![\w.])' + re.escape(hname) + r'\s*\((?:\s*' + re.escape(hpname) + r'\s*=\s*' +
                               re.escape(pname) + r'\s*[,)]|(?:[^(),]*,){' + str(hidx) + r'}\s*' +
                               re.escape(pname) + r'\s*[,)])')
                        if re.search(pat, body):
                            homes.setdefault(name, set()).add(kt.rel)
                            if (index, pname) not in helpers.setdefault(name, set()):
                                helpers[name].add((index, pname))
                                changed = True
    # A wrapper name that also names an unrelated function (t() in SalahModels) is file-scoped.
    scope = {k: None for k in helpers}
    for kt, (name, params, bs, be) in defs:
        if name in homes and kt.rel not in homes[name]:
            scope[name] = homes[name]
    return helpers, scope


def is_condition_literal(kt, start, end):
    after = kt.masked[end:end + 6]
    before = kt.masked[max(0, start - 4):start]
    return bool(re.match(r'\s*(->|==|!=|in\b|\))', after) and not re.match(r'\s*\)', after)) or \
        bool(re.search(r'(==|!=)\s*$', before)) or bool(re.match(r'\s*,[^\n]*->', kt.masked[end:end + 80]) and
                                                      re.search(r'\bwhen\b', kt.masked[max(0, start - 300):start]) and
                                                      not re.search(r'->\s*$', kt.masked[max(0, start - 30):start]))


def expression_literals(kt, start, end, seen_idents=None, depth=0):
    """Literals that can be the value of an argument expression, following local vals."""
    seen_idents = seen_idents or set()
    out = []
    for s, e, value, dynamic in kt.literals_in(start, end):
        if is_condition_literal(kt, s, e):
            continue
        # Skip literals that are arguments of nested calls (keys, formats), except if/when branches.
        prefix = kt.masked[start:s]
        opened = prefix.count('(') - prefix.count(')')
        if opened > 0:
            last_open = prefix.rfind('(')
            callee = re.search(r'([A-Za-z_]\w*)\s*$', kt.masked[start:start + last_open])
            if callee and callee[1] not in {'if', 'when', 'listOf', 'arrayOf', 'setOf', 'Pair', 'to'} | set(BUILTIN_HELPERS):
                continue
        # The app shows (and the phrase maps key) the trimmed text of """...""".trimIndent().
        if kt.text.startswith('"""', s) and kt.masked[e:e + 13] == '.trimIndent()':
            value = trim_indent(value)
        out.append((s, value, dynamic))
    expr = kt.masked[start:end].strip()
    ident = re.fullmatch(r'([a-z_]\w*)(?:\s*\?:\s*"x*")?', expr)
    if ident and depth < 3 and ident[1] not in seen_idents:
        name = ident[1]
        seen_idents.add(name)
        decls = [m for m in re.finditer(r'\b(?:val|var)\s+' + name + r'\b(?:\s*:\s*[\w.<>?, ]+)?\s*=(?!=)', kt.masked)
                 if m.start() < start]
        if decls:
            d = decls[-1]
            ds = d.end()
            de = statement_end(kt, ds)
            out += expression_literals(kt, ds, de, seen_idents, depth + 1)
    return out


def statement_end(kt, start):
    i, n, depth = start, len(kt.masked), 0
    while i < n:
        ch = kt.masked[i]
        if ch in '([{':
            depth += 1
        elif ch in ')]}':
            depth -= 1
            if depth < 0:
                return i
        elif ch in '\n;' and depth == 0:
            nxt = kt.masked[i + 1:i + 120].lstrip(' \t')
            if not re.match(r'(else\b|\?:|\.|\+|->|&&|\|\|)', nxt) and kt.masked[start:i].strip():
                return i
        i += 1
    return n


def collect(files, helpers, scope):
    found = {}       # value -> location
    dynamic = {}     # location -> literal (interpolated helper input)
    fields = set()

    def add(kt, s, value, is_dynamic):
        if is_dynamic:
            dynamic.setdefault(f'{kt.rel}:{kt.line(s)}', value)
            return
        if not re.search(r'[A-Za-z]{2}', value) or re.fullmatch(r'[a-z0-9_.:/-]+', value):
            return
        if value.startswith(('http', 'android.', 'com.')):
            return
        found.setdefault(value, f'{kt.rel}:{kt.line(s)}')

    names = sorted(helpers, key=len, reverse=True)
    call = re.compile(r'(?<![\w.])(' + '|'.join(map(re.escape, names)) + r')\s*\(')
    for kt in files:
        for m in call.finditer(kt.masked):
            name = m[1]
            if scope.get(name) is not None and kt.rel not in scope[name]:
                continue
            if re.search(r'\bfun\s+$', kt.masked[max(0, m.start() - 20):m.start()]):
                continue
            args, _ = kt.split_args(m.end() - 1)
            for index, (aname, s, e) in enumerate(args):
                wanted = any((aname is None and index == hidx) or aname == hp for hidx, hp in helpers[name])
                if not wanted:
                    continue
                for ls, value, dyn in expression_literals(kt, s, e):
                    add(kt, ls, value, dyn)
                fm = re.search(r'\b[a-z]\w*(?:\(\))?\??\.([a-z]\w*)\s*(?:\?:[^,)]*)?$', kt.masked[s:e].strip())
                if fm and fm[1] not in SKIP_FIELDS:
                    fields.add(fm[1])
    return found, dynamic, fields


def collect_fields(files, fields):
    """Catalog values for String fields that are displayed through a helper (item.title)."""
    found = {}
    classes = {}  # class name -> ordered field list
    for kt in files:
        for m in re.finditer(r'\b(?:data\s+|enum\s+)?class\s+([A-Z]\w*)\s*(?:<[^>]*>)?\s*(?:\w+\s+)?(?:constructor\s*)?\(', kt.masked):
            args, close = kt.split_args(m.end() - 1)
            names = []
            for _, s, e in args:
                pm = re.match(r'\s*(?:@\w+\s+)*(?:(?:private|internal|override)\s+)*(?:val|var)?\s*([A-Za-z_]\w*)\s*:\s*([\w.?<>]+)', kt.masked[s:e])
                names.append((pm[1], pm[2]) if pm else (None, None))
            if any(n in fields and (t or '').startswith(('String', 'SalahText')) for n, t in names):
                classes[m[1]] = (names, kt, close, 'enum' in kt.masked[m.start():m.start() + 12])
    for cname, (names, decl_kt, decl_close, is_enum) in classes.items():
        wanted = {i: n for i, (n, t) in enumerate(names) if n in fields and (t or '').startswith('String')}
        if not wanted:
            continue
        sites = []
        for kt in files:
            for m in re.finditer(r'(?<![\w.])' + cname + r'\s*\(', kt.masked):
                if kt is decl_kt and m.start() <= decl_close:
                    continue
                sites.append((kt, m.end() - 1))
        if is_enum:
            body = decl_kt.masked.find('{', decl_close)
            if body > 0:
                body_end = decl_kt.matching(body)
                for m in re.finditer(r'(?m)^\s*([A-Z][A-Z0-9_]*)\s*\(', decl_kt.masked[body:body_end]):
                    sites.append((decl_kt, body + m.end() - 1))
        for kt, open_i in sites:
            args, _ = kt.split_args(open_i)
            for index, (aname, s, e) in enumerate(args):
                field = aname if aname is not None else wanted.get(index)
                if field is None or field not in wanted.values():
                    continue
                for ls, value, dyn in expression_literals(kt, s, e):
                    if not dyn and re.search(r'[A-Za-z]{2}', value) and not re.fullmatch(r'[a-z0-9_.:/-]+', value):
                        found.setdefault(value, f'{kt.rel}:{kt.line(ls)}')
    return found


# ---------------------------------------------------------------- coverage

def language_names(supported):
    names = {}
    for code, label in supported.items():
        for word in re.findall(r'[A-Za-z]+', label):
            names.setdefault(word.lower(), code)
    names.update({'hinglish': 'hinglish', 'hindi': 'hi', 'urdu': 'ur', 'filipino': 'tl', 'tagalog': 'tl',
                  'bodo': 'brx', 'santali': 'sat', 'manipuri': 'mni', 'odia': 'or', 'roman': 'hinglish'})
    for drop in ('bahasa', 'roman'):
        names.pop(drop, None)
    return names


def kotlin_coverage(supported):
    """{language: {english: translation}} from every attributable "en" to "translation" pair."""
    cover = {code: {} for code in supported}  # language -> {english: translation}
    names = language_names(supported)
    name_re = re.compile(r'\b(?:val|var)\s+(' + '|'.join(sorted(names, key=len, reverse=True)) + r')[A-Z]\w*\s*(?::[^=]+)?=')
    code_re = re.compile(r'\s+to\s+(?:mapOf|mutableMapOf|UiLanguagePack|regionalPack|linkedMapOf)\s*\(')
    pair_re = re.compile(r'"(?:x)*"\s+to\s+"')
    for path in sorted(LOC_DIR.glob('*.kt')):
        kt = KtFile(path)
        fm = re.match(r'(?:World|Indic)([A-Z][a-z]+)Draft\d+\.kt$', path.name)
        file_code = None
        if fm:
            file_code = fm[1].lower()
            if file_code not in cover:
                file_code = None
        spans = []
        if file_code is None:
            # String contents are masked, so language keys are read from the literal table.
            for s, (e, value, _) in kt.literals.items():
                if value in cover:
                    m = code_re.match(kt.masked, e)
                    if m:
                        spans.append((m.end() - 1, kt.matching(m.end() - 1), value))
            for m in name_re.finditer(kt.masked):
                code = names[m[1]]
                end = statement_end(kt, m.end())
                spans.append((m.end(), end, code))
        starts = sorted(kt.literals)
        for idx, s in enumerate(starts):
            e, key, dyn = kt.literals[s]
            if not re.match(r'\s+to\s+"', kt.masked[e:e + 8]):
                continue
            nxt = kt.masked.find('"', e)
            if nxt not in kt.literals:
                continue
            if file_code:
                cover[file_code].setdefault(key, kt.literals[nxt][1])
                continue
            inner = [c for a, b, c in spans if a <= s <= b]
            if inner:
                # Innermost span wins.
                best = min(((b - a, c) for a, b, c in spans if a <= s <= b))[1]
                cover[best].setdefault(key, kt.literals[nxt][1])
    return cover


def trim_indent(s):
    """Kotlin's String.trimIndent(): drop blank first/last lines and the common indent."""
    lines = s.split('\n')
    widths = [len(l) - len(l.lstrip()) for l in lines if l.strip()]
    m = min(widths) if widths else 0
    if lines and not lines[0].strip():
        lines = lines[1:]
    if lines and not lines[-1].strip():
        lines = lines[:-1]
    return '\n'.join(l[m:] for l in lines)


def raw_key_coverage(supported):
    """Keys written as \"\"\"...\"\"\".trimIndent() to "...", which kotlin_coverage cannot attribute."""
    cover = {code: {} for code in supported}
    names = language_names(supported)
    name_re = re.compile(r'\b(?:val|var)\s+(' + '|'.join(sorted(names, key=len, reverse=True)) + r')[A-Z]\w*\s*(?::[^=]+)?=')
    code_re = re.compile(r'\s+to\s+(?:mapOf|mutableMapOf|UiLanguagePack|regionalPack|linkedMapOf)\s*\(')
    for path in sorted(LOC_DIR.glob('*.kt')):
        kt = KtFile(path)
        if '.trimIndent() to' not in kt.text:
            continue
        fm = re.match(r'(?:World|Indic)([A-Z][a-z]+)Draft\d+\.kt$', path.name)
        file_code = fm[1].lower() if fm and fm[1].lower() in cover else None
        spans = []
        for s0, (e, value, _) in kt.literals.items():
            if value in cover:
                m = code_re.match(kt.masked, e)
                if m:
                    spans.append((m.end() - 1, kt.matching(m.end() - 1), value))
        for m in name_re.finditer(kt.masked):
            spans.append((m.end(), statement_end(kt, m.end()), names[m[1]]))
        for s0, (e, key, _) in kt.literals.items():
            if not kt.text.startswith('"""', s0):
                continue
            m = re.match(r'\.trimIndent\(\)\s+to\s+"', kt.masked[e:e + 40])
            if not m or (e + m.end() - 1) not in kt.literals:
                continue
            value = kt.literals[e + m.end() - 1][1]
            inner = [(b - a, c) for a, b, c in spans if a <= s0 <= b]
            code = file_code or (min(inner)[1] if inner else None)
            if code:
                cover[code].setdefault(trim_indent(key), value)
    return cover


# Languages whose Qaida/Kalima screens show the Roman (or Urdu) field instead of a translation.
ROMAN_SHOWN = ('hinglish', 'hi', 'ur')


def _arg_value(kt, s, e):
    """A call argument's string value (concatenated literals, trimIndent applied), or None."""
    lits = kt.literals_in(s, e)
    if not lits:
        return None
    parts = []
    for ls, le, value, _ in lits:
        if kt.text.startswith('"""', ls) and kt.masked[le:le + 13] == '.trimIndent()':
            value = trim_indent(value)
        parts.append(value)
    return ''.join(parts)


def _factory_calls(kt, name):
    """[(positional values, named values, line)] for every call of the factory `name`."""
    out = []
    for m in re.finditer(r'(?<![\w.])' + name + r'\s*\(', kt.masked):
        args, _ = kt.split_args(m.end() - 1)
        pos = [_arg_value(kt, s, e) for n, s, e in args if n is None]
        named = {n: _arg_value(kt, s, e) for n, s, e in args if n}
        out.append((pos, named, kt.line(m.end() - 1)))
    return out


def factory_content(phrase_helpers):
    """Content built by factory calls, which helper-call scanning cannot see.

    Qaida text goes through label(en, roman) -> localizeUiText (mixes words when untranslated);
    Kalima text goes through localizePhrase (whole phrase), so it is audited with --phrase-helpers.
    """
    screens = ROOT / 'app/src/main/java/com/example/ui/screens'
    found = {}

    def add(value, kt, line):
        if isinstance(value, str) and re.search(r'[A-Za-z]{2}', value) and '$' not in value:
            found.setdefault(value, f'{kt.rel}:{line}')

    lessons = screens / 'QaidaLessons.kt'
    if lessons.exists():
        kt = KtFile(lessons)
        for pos, named, line in _factory_calls(kt, 'QaidaLesson'):
            for value in [pos[i] for i in (2, 4, 7) if i < len(pos)] + [named.get('tip')]:
                add(value, kt, line)
        for pos, _, line in _factory_calls(kt, 'q'):
            if pos:
                add(pos[0], kt, line)
    for path in sorted(screens.glob('Qaida*.kt')):
        kt = KtFile(path)
        for pos, named, line in _factory_calls(kt, 'label'):
            if len(pos) == 2:
                add(pos[0], kt, line)
    kalimas = screens / 'KalimasContent.kt'
    if phrase_helpers and kalimas.exists():
        kt = KtFile(kalimas)
        for pos, named, line in _factory_calls(kt, 'entry'):
            if len(pos) >= 9 and isinstance(pos[0], str):
                for value in (pos[1], pos[3], pos[8], named.get('noteEnglish')):
                    add(value, kt, line)
        for pos, _, line in _factory_calls(kt, 'KalimaPhrase'):
            if len(pos) == 4:
                add(pos[3], kt, line)
    return found


def json_coverage(supported):
    folder = ROOT / 'localization'
    sources = set(auto_translate.read_json(folder / 'source.json', []))
    legacy = set(auto_translate.read_json(folder / 'legacy-baseline.json', []))
    for identity, (value, kind, _) in auto_translate.scan().items():
        if identity not in legacy and kind != 'raw' and '$' not in value:
            sources.add(value)
    cover = {}
    for code in supported:
        cache = auto_translate.read_json(folder / 'translations' / f'{code}.json', {})
        cover[code] = {k for k in cache if k in sources}
    return cover, sources


def main():
    sys.stdout.reconfigure(encoding="utf-8")
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--lang', help='list gaps for this language code')
    parser.add_argument('--dump', type=Path, help='write the union of gaps (JSON array)')
    parser.add_argument('--dump-langs', help='comma list of languages for --dump (default all)')
    parser.add_argument('--dynamic', action='store_true', help='list helper inputs built by interpolation')
    parser.add_argument('--all-strings', type=Path, help='write every collected string with location (JSON)')
    parser.add_argument('--phrase-helpers', action='store_true',
                        help='also audit whole-phrase helpers (localizePhrase), which never mix languages')
    parser.add_argument('--strict', help='comma list of languages that must have zero gaps')
    args = parser.parse_args()

    files = [KtFile(p) for p in source_files()]
    helpers, scope = detect_wrappers(files, BUILTIN_HELPERS if args.phrase_helpers else MIXING_HELPERS)
    found, dynamic, fields = collect(files, helpers, scope)
    for value, loc in collect_fields(files, fields).items():
        found.setdefault(value, loc)

    supported = {c: n for c, n in auto_translate.languages().items() if c != 'en'}
    # Language self-names ("हिन्दी (Hindi)") are shown as written, never translated.
    labels = set(auto_translate.languages().values())
    found = {v: loc for v, loc in found.items() if v not in labels and not CONTENT_SOURCES.search(loc)}
    # Qaida/Kalima content; in hinglish/hi/ur those screens show the Roman field, not a translation.
    roman_only = set()
    for value, loc in factory_content(args.phrase_helpers).items():
        if value not in found and value not in labels:
            found[value] = loc
            roman_only.add(value)
    kcover = kotlin_coverage(supported)
    for code, pairs in raw_key_coverage(supported).items():
        for key, value in pairs.items():
            kcover[code].setdefault(key, value)
    jcover, _ = json_coverage(supported)
    gaps = {code: sorted(v for v in found if v not in kcover[code] and v not in jcover[code]
                         and not (code in ROMAN_SHOWN and v in roman_only)) for code in supported}

    if args.all_strings:
        args.all_strings.write_text(json.dumps(found, ensure_ascii=False, indent=2, sort_keys=True), encoding='utf-8')
    if args.dynamic:
        for loc, value in sorted(dynamic.items()):
            print(f'{loc} | {value}')
        print(f'{len(dynamic)} interpolated helper inputs (translate the whole sentence via localizeUiTemplate)')
        return 0
    if args.lang:
        for value in sorted(gaps[args.lang], key=lambda v: found[v]):
            print(f'{found[value]} | {value}')
        print(f'{args.lang}: {len(gaps[args.lang])} of {len(found)} UI strings lack a whole-phrase translation')
    else:
        wrappers = sorted(n for n in helpers if n not in BUILTIN_HELPERS)
        print(f'Helpers/wrappers: {len(helpers)} ({", ".join(wrappers)})')
        print(f'Catalog fields shown through helpers: {", ".join(sorted(fields))}')
        print(f'UI strings reaching helpers: {len(found)}')
        for code in supported:
            print(f'  {code:9s} {len(gaps[code]):5d} gaps')
    if args.dump:
        langs = args.dump_langs.split(',') if args.dump_langs else list(supported)
        union = sorted({v for c in langs for v in gaps[c]})
        args.dump.write_text(json.dumps(union, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    if args.strict:
        bad = {c: len(gaps[c]) for c in args.strict.split(',') if gaps[c]}
        if bad:
            print(f'Gaps remain: {bad}', file=sys.stderr)
            return 1
    return 0


if __name__ == '__main__':
    sys.exit(main())

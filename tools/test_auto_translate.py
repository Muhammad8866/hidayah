import contextlib
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

import auto_translate as pipeline


class AutoTranslationTests(unittest.TestCase):
    def test_arabic_script_targets_can_introduce_translated_words(self):
        pipeline.validate('Read {n}', 'بخوانید {n}')
        pipeline.validate('Remember الله', 'به یاد داشته باشید الله')
        with self.assertRaises(ValueError):
            pipeline.validate('Remember الله', 'به یاد داشته باشید اللهه')

    def test_preserves_placeholders_arabic_and_newlines(self):
        pipeline.validate('Read {n}\nالله', 'पढ़ें {n}\nالله')
        for bad in ['Read 3\nالله', 'Read {n}\nال', 'Read {n} الله', '']:
            with self.assertRaises(ValueError):
                pipeline.validate('Read {n}\nالله', bad)

    def test_scanner_ignores_comments_and_finds_named_arguments(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            folder = root / 'app/src/main/java/example'
            folder.mkdir(parents=True)
            (folder / 'Screen.kt').write_text('// Text("Comment")\n'
                'LocalizedText(text = "New tool")\nText("Raw tool")\n', encoding='utf-8')
            with patch.object(pipeline, 'ROOT', root):
                result = pipeline.scan()
            self.assertEqual({'localized:New tool', 'raw:Raw tool'}, set(result))

    def test_new_text_covers_all_languages_and_reuses_cache_offline(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            pipeline.write_json(root / 'localization/legacy-baseline.json', [])
            pipeline.write_json(root / 'localization/source.json', ['New {n} tool'])
            fake = lambda code, name, batch: [f'{code}: {s}' for s in batch]
            supported = pipeline.languages()
            with patch.object(pipeline, 'ROOT', root), patch.object(pipeline, 'scan', return_value={}), \
                 patch.object(pipeline, 'languages', return_value=supported), \
                 patch.object(pipeline, 'translate', side_effect=fake) as translator, \
                 patch('sys.argv', ['auto_translate', '--output', str(root / 'generated')]), \
                 contextlib.redirect_stdout(io.StringIO()):
                pipeline.main()
                self.assertEqual(52, translator.call_count)
                translator.reset_mock()
                pipeline.main()
                translator.assert_not_called()
            for code in supported.keys() - {'en'}:
                self.assertEqual(f'{code}: New {{n}} tool',
                    json.loads((root / f'localization/translations/{code}.json').read_text())['New {n} tool'])
            self.assertTrue((root / 'generated/AutoTranslationPhrases.kt').exists())

    def test_new_raw_copy_fails_before_any_api_request(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            pipeline.write_json(root / 'localization/legacy-baseline.json', [])
            with patch.object(pipeline, 'ROOT', root), \
                 patch.object(pipeline, 'scan', return_value={'raw:New': ('New', 'raw', 'Screen.kt:4')}), \
                 patch('sys.argv', ['auto_translate', '--check']):
                with self.assertRaisesRegex(ValueError, 'Screen.kt:4'):
                    pipeline.main()

    def test_missing_credentials_fail_without_english_fallback(self):
        with patch.dict('os.environ', {'TRANSLATION_API_KEY': ''}):
            with self.assertRaisesRegex(ValueError, 'TRANSLATION_API_KEY'):
                pipeline.translate('hi', 'Hindi', ['New tool'])


if __name__ == '__main__':
    unittest.main()

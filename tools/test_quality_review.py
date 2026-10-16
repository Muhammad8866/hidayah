import json,tempfile,unittest
from pathlib import Path
import apply_quality_review_drafts as fixes
import audit_localization_review as review

class QualityReviewTests(unittest.TestCase):
    def test_identity_translation_fix_preserves_english_key(self):
        source='    "Delete counter" to "Delete counter",'
        result=fixes.replace_targets(source,'"Delete counter"','"Tirtir tiriyaha"')
        self.assertEqual('    "Delete counter" to "Tirtir tiriyaha",',result)

    def test_kotlin_concatenation_decodes_paragraphs(self):
        self.assertEqual('first\n\nsecond',review.decode(r'"first\n\n" + "second"'))

    def test_registered_core_supports_default_benefits_and_long_strings(self):
        bengali,_=review.registered_rows('bn')
        french,_=review.registered_rows('fr')
        self.assertEqual(298,len(bengali))
        self.assertEqual(298,len(french))
        row=bengali['dua_morning_witness'][0]
        self.assertEqual(row[2],row[3])
        self.assertEqual(4,french['dua_muawwidhat'][0][1].count('\n'))

    def test_corrupted_proposal_is_never_applied(self):
        with tempfile.TemporaryDirectory() as folder:
            p=Path(folder)/'report.json'
            p.write_text(json.dumps([dict(language='fa',source='Khatm',current='old',proposed='???',confidence='high')]),encoding='utf-8')
            with self.assertRaises(ValueError): fixes.proposals([p])

if __name__=='__main__': unittest.main()

import unittest
import world_localization as world

class WorldLocalizationTests(unittest.TestCase):
    def test_persian_can_add_script_but_original_arabic_and_templates_survive(self):
        world.validate('Read {n}\nالله', 'بخوانید {n}\nالله')
        for value in ('بخوانید 3\nالله', 'بخوانید {n} الله', 'بخوانید {n}\nال', 'بخوانید {n}\nاللهه', ''):
            with self.assertRaises(ValueError): world.validate('Read {n}\nالله', value)

    def test_masking_preserves_sentence_and_paragraph_boundaries(self):
        source='Read {n} pages. Remember الله.\n\n- Next {day} ﷺ'
        masked,held=world.mask_line(source)
        self.assertEqual(source,world.restore_markers(masked,held,'fa',source))
        self.assertCountEqual(['{n}','الله','{day}','ﷺ'],held.values())

    def test_all_requested_languages_are_supported_in_order(self):
        self.assertEqual(('fa','tr','id','es','ru','zh','it','ja'),world.CODES)

    def test_localized_marker_digits_do_not_change_runtime_tokens(self):
        self.assertEqual('روزه {n}',world.restore_markers('روزه ۱۲۳٬۴۵۶',{'123456':'{n}'},'fa','Fast {n}'))

    def test_devotional_sentence_cannot_be_reordered(self):
        with self.assertRaises(ValueError): world.validate('Remember الله أكبر','به یاد داشته باشید أكبر الله')

    def test_hijri_years_are_not_translated_as_hours(self):
        self.assertEqual('Hicrî 2',world.glossary('tr')['2 AH'])
        self.assertEqual('۲ هجری قمری',world.glossary('fa')['2 AH'])
        self.assertEqual('2 r. hidżry',world.glossary('pl')['2 AH'])

    def test_counting_counter_is_not_a_shop_counter(self):
        self.assertEqual('我的计数器',world.glossary('zh')['My counter'])
        self.assertEqual('شمارنده من',world.glossary('fa')['My counter'])
        self.assertEqual('删除这个计数器',world.normalize_terminology('Delete this counter','删除这个柜台','zh'))
        self.assertEqual('前往柜台',world.normalize_terminology('Visit the reception desk','前往柜台','zh'))

    def test_direction_controls_are_visible_in_kotlin_source(self):
        self.assertEqual('"abc\\u200f"',world.quote('abc\u200f'))

    def test_faith_statement_keeps_the_passive_worship_meaning(self):
        target=world.glossary('fa')['None has the right to be worshipped except Allah.']
        self.assertIn('شایستهٔ پرستش نیست',target)
        self.assertNotIn('حق پرستش ندارد',target)

    def test_many_markers_cannot_match_inside_each_other(self):
        source=' '.join('{value'+str(i)+'}' for i in range(12))
        masked,held=world.mask_line(source)
        self.assertEqual(source,world.restore_markers(masked,held,'tr',source))

if __name__=='__main__': unittest.main()

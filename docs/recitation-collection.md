# Manzil, Ruqyah and Wazaif

The collection screen keeps the existing Quran selection and sourced dua dataset. Quran text is loaded from the bundled Tanzil asset; opening the reader does not require a network connection.

## Reading

- Search by chapter title, Quran reference, Arabic text, dua title, transliteration or meaning.
- Favourites filter the selected collection. Selecting a different tab clears the current search and filter.
- Large Arabic text and dua translation visibility are saved on the device.
- Counters are saved per entry, limited to its existing recommended count, and reset individually. They remain saved until Reset is tapped; they do not automatically reset at midnight.
- Completion progress describes the visible entries. Continue reading scrolls to the first incomplete visible entry. Quran passages shared by Manzil and Ruqyah share their counter and favourite status.

## Audio

Quran passages use Mishary Rashid Alafasy recordings from EveryAyah. A playlist contains exactly the selected passage's first through last ayah, with locale-independent, six-digit filenames. The current ayah is highlighted. Listen toggles playback and pause; Slow selects 0.75x playback. Failed streams expose Retry. Audio uses the app's existing stream cache, audio focus and headphone-disconnect handling.

Duа and Wazaif cards use the existing Arabic text-to-speech helper and explicitly identify the audio as Arabic device voice. Device Arabic voice availability controls whether Listen is enabled. The helper exposes a settings action if an Arabic voice is unavailable.

Starting Quran playback stops device speech and other app audio engines. Starting device speech stops the collection player. Changing tabs or filters, leaving the screen or backgrounding the app stops collection audio. The Quran player is created only when listening starts and released when the screen is disposed.

## Verification

`RecitationCollectionTest` covers the traditional 18-passage, 79-ayah selection, valid source references and counts, exact audio boundaries, and rejection of invalid Quran ranges.

`RecitationCollectionScreenTest` covers reference search, audio control visibility, counter persistence and Urdu reading controls. Run both with `:app:testDebugUnitTest --tests 'com.example.ui.screens.RecitationCollection*Test'`.

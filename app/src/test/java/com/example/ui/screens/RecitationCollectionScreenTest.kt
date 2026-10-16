package com.example.ui.screens

import android.content.Context
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.theme.MyApplicationTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RecitationCollectionScreenTest {
    @get:Rule val compose = createComposeRule()
    @Before fun clearPreferences() {
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("recitation_collection", Context.MODE_PRIVATE).edit().clear().commit()
    }
    @Test fun exactReferenceSearchShowsMatchingAudioAndPersistsCount() {
        compose.setContent { MyApplicationTheme { RecitationCollectionScreen({}) } }
        compose.onNodeWithTag("collection_search").performTextInput("2:255-257")
        compose.waitUntil(30_000) {
            compose.onAllNodesWithText("Continue reading").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("recitation_collection_screen")
            .performScrollToNode(hasTestTag("collection_audio_quran_2_255_257"))
        compose.onNodeWithTag("collection_audio_quran_2_255_257").assertIsDisplayed()
        compose.onNodeWithTag("recitation_collection_screen")
            .performScrollToNode(hasTestTag("collection_count_quran_2_255_257"))
        compose.onNodeWithTag("collection_count_quran_2_255_257").performClick().assertIsNotEnabled()
        val prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("recitation_collection", Context.MODE_PRIVATE)
        org.junit.Assert.assertEquals(1, prefs.getInt("count_quran_2_255_257", 0))
    }
    @Test fun urduControlsRender() {
        compose.setContent {
            CompositionLocalProvider(LocalAppLanguage provides "ur") {
                MyApplicationTheme { RecitationCollectionScreen({}) }
            }
        }
        compose.onNodeWithText("بڑا متن").assertIsDisplayed()
        compose.onNodeWithText("وظائف").assertIsDisplayed()
    }
    @Test fun wazaifTabShowsCategoryFiltersAndIsRemembered() {
        compose.setContent { MyApplicationTheme { RecitationCollectionScreen({}) } }
        compose.onNodeWithTag("collection_tab_2").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("collection_category_${RecitationCollection.wazaifCategories.first()}").assertIsDisplayed()
        val context = ApplicationProvider.getApplicationContext<Context>()
        org.junit.Assert.assertEquals(2, RecitationStore(context).state.value.lastTab)
    }
    @Test fun favouriteToggleIsSavedAndFiltersTheList() {
        compose.setContent { MyApplicationTheme { RecitationCollectionScreen({}) } }
        compose.waitUntil(30_000) {
            compose.onAllNodesWithText("Continue reading").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("recitation_collection_screen")
            .performScrollToNode(hasTestTag("collection_favourite_quran_1_1_7"))
        // Scrolling parks the card under the sticky player bar, so a touch click would land on the bar.
        compose.onNodeWithTag("collection_favourite_quran_1_1_7")
            .performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
        compose.waitForIdle()
        val context = ApplicationProvider.getApplicationContext<Context>()
        org.junit.Assert.assertTrue("quran_1_1_7" in RecitationStore(context).state.value.favorites)
        compose.onNodeWithTag("recitation_collection_screen").performScrollToNode(hasTestTag("collection_favourites_filter"))
        compose.onNodeWithTag("collection_favourites_filter").performClick()
        compose.waitForIdle()
        compose.onAllNodesWithTag("recitation_quran_2_1_5").assertCountEquals(0)
    }
}


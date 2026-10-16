package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w360dp-h800dp-xxhdpi")
class RozaTrackerScreenTest {
    @get:Rule val compose = createComposeRule()
    private lateinit var store: RozaTrackerStore

    @Before fun setup() {
        val prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("daily_tools", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = RozaTrackerStore(prefs)
    }

    @Test fun `qaza disabled when no missed balance and enabled after missed log`() {
        compose.setContent { MaterialTheme { RozaTrackerScreen(onBack = {}) } }
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/roza_tracker.png")
        compose.onNodeWithTag("roza_qaza").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("roza_missed").performScrollTo().performClick()
        compose.onNodeWithTag("roza_missed").assertIsSelected()
        // The one missed entry cannot itself be replaced by a Qaza completion.
        compose.onNodeWithTag("roza_qaza").assertIsNotEnabled()
        compose.runOnIdle { store.addMissed(1) }
        // The "Fast log saved" snackbar can cover the bottom row; trigger the option semantically.
        compose.onNodeWithTag("roza_qaza").assertIsEnabled().performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
        compose.onNodeWithTag("roza_qaza").assertIsSelected()
        compose.runOnIdle {
            org.junit.Assert.assertEquals(0, store.balance)
            org.junit.Assert.assertEquals(RozaMark.QAZA, store.mark(LocalDate.now()))
        }
    }

    @Test fun `logged day is shown on the calendar and today stays selected`() {
        val today = LocalDate.now()
        compose.setContent { MaterialTheme { RozaTrackerScreen(onBack = {}) } }
        compose.onNodeWithTag("roza_nafl").performScrollTo().performClick()
        compose.onNodeWithTag("roza_nafl").assertIsSelected()
        compose.runOnIdle { org.junit.Assert.assertEquals(RozaMark.NAFL, store.mark(today)) }
        compose.onNodeWithTag("roza_list").performScrollToNode(hasTestTag("roza_day_$today"))
        compose.onNodeWithTag("roza_day_$today").assertIsSelected()
            .assert(hasContentDescription(RozaMark.NAFL.label, substring = true))
    }
}


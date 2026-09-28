package com.example.ui.localization

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppLocaleContextTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("deen_app_preferences", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun `localized context uses selected locale without mutating base context`() {
        val baseLanguage = context.resources.configuration.locales[0].language
        val urduContext = context.withAppLocale("ur")

        assertEquals("ur", urduContext.resources.configuration.locales[0].language)
        assertEquals(View.LAYOUT_DIRECTION_RTL, urduContext.resources.configuration.layoutDirection)
        assertEquals(baseLanguage, context.resources.configuration.locales[0].language)
    }

    @Test
    fun `localized context keeps the activity owner chain`() {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java).create()
        val activity = controller.get()
        var current: Context? = activity.withAppLocale("ur")
        var registryOwner: ActivityResultRegistryOwner? = null

        while (current != null) {
            if (current is ActivityResultRegistryOwner) {
                registryOwner = current
                break
            }
            current = (current as? ContextWrapper)?.baseContext
        }

        assertSame(activity, registryOwner)
        controller.destroy()
    }

    @Test
    fun `background components read the persisted app language safely`() {
        assertEquals("en", persistedAppLanguage(context))
        context.getSharedPreferences("deen_app_preferences", Context.MODE_PRIVATE)
            .edit().putString("language_code", "hi").commit()
        assertEquals("hi", persistedAppLanguage(context))

        context.getSharedPreferences("deen_app_preferences", Context.MODE_PRIVATE)
            .edit().putString("language_code", "not-supported").commit()
        assertEquals("en", persistedAppLanguage(context))
    }
}

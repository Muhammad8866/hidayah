package com.example.ui.tour

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TabWalkthroughStepsTest {
    @Test
    fun `missing targets become cards and only optional ones drop`() {
        val steps = listOf(
            TabTourStep("", "Intro", "Overview"),
            TabTourStep("shown", "Shown", "On screen"),
            TabTourStep("scrolled", "Scrolled", "Off screen"),
            TabTourStep("flavor", "Flavor", "Other flavor", optional = true)
        )
        val visible = visibleSteps(steps) { it == "shown" }
        assertEquals(listOf("", "shown", "scrolled"), visible.map { it.target })
        assertEquals(emptyList<TabTourStep>(), visibleSteps(emptyList()) { true })
    }

    @Test
    fun `every tab has a guide`() {
        TabTour.entries.forEach { tour ->
            assertTrue("${tour.name} has no steps", tabTourSteps[tour].orEmpty().isNotEmpty())
        }
    }

    @Test
    fun `every step has copy and no step is repeated`() {
        val all = tabTourSteps.values + screenTourSteps.values
        all.forEach { steps ->
            steps.forEach { assertTrue("blank copy for ${it.target}", it.title.isNotBlank() && it.body.isNotBlank()) }
            val keys = steps.map { it.target to it.title }
            assertEquals("repeated steps $keys", keys.size, keys.toSet().size)
        }
        assertTrue(screenTourSteps.keys.none { it.isBlank() })
    }

    @Test
    fun `all step copy is in the localization catalogue`() {
        val source = generateSequence(File("").absoluteFile) { it.parentFile }
            .map { File(it, "localization/source.json") }.first { it.exists() }.readText()
        (tabTourSteps.values + screenTourSteps.values).flatten().forEach {
            listOf(it.title, it.body).forEach { text ->
                val json = text.replace("\\", "\\\\").replace("\"", "\\\"")
                assertTrue("not in source.json: $text", source.contains("\"$json\""))
            }
        }
    }
}

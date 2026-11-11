package com.example.ui.screens

import com.example.AppDestination
import com.example.ui.tour.TabTour
import com.example.ui.tour.steps.libraryScreenTourSteps
import com.example.ui.tour.steps.libraryTabTourSteps
import com.example.ui.tour.steps.moreTabTourSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Islamic Books and the World Library are one screen, reached from one row of More. */
class IslamicBooksNavigationTest {
    private val providers = Regex(
        "(?i)dawat-?e-?islami|dawateislami|maktaba|madinatul|internet archive|archive\\.org"
    )

    private fun source(path: String): File = generateSequence(File("").absoluteFile) { it.parentFile }
        .map { File(it, "app/src/main/java/com/example/$path") }.first { it.parentFile?.isDirectory == true }

    @Test fun `there is one library destination, Islamic Books`() {
        assertTrue(AppDestination.entries.none { it.name == "WORLD_LIBRARY" })
        assertEquals("Islamic Books", AppDestination.BOOKS.title)
        assertTrue(TabTour.entries.none { it.name == "WORLD_LIBRARY" })
        assertEquals(TabTour.BOOKS, TabTour.of(AppDestination.BOOKS))
    }

    @Test fun `the separate World Library screen is gone`() {
        assertFalse(source("ui/screens/WorldLibraryScreen.kt").exists())
        assertTrue(source("ui/screens/IslamicBooksScreen.kt").exists())
    }

    @Test fun `More lists the library once`() {
        val more = source("ui/screens/MoreScreen.kt").readText()
        assertEquals(1, Regex("\"more_books\"").findAll(more).count())
        assertFalse(more.contains("more_world_library"))
        assertFalse(more.contains("World Library"))
        assertTrue(moreTabTourSteps.values.flatten().none { it.target == "more_world_library_row" })
    }

    @Test fun `the library guide covers the merged screen and has no collection picker`() {
        val steps = libraryTabTourSteps.getValue(TabTour.BOOKS)
        val targets = steps.map { it.target }
        assertTrue(targets.containsAll(listOf("library_books_search", "library_world_category", "library_world_language",
            "library_world_sort", "library_books_shelves", "library_books_cover", "library_books_row", "library_world_item")))
        assertFalse("library_world_source" in targets)
        assertFalse("library_books_footer" in targets)
        assertTrue(steps.none { it.title == "World Library" })
    }

    @Test fun `the library guide and screen name no content provider`() {
        (libraryTabTourSteps.values + libraryScreenTourSteps.values).flatten().forEach {
            assertFalse("tour ${it.target} names a provider", providers.containsMatchIn(it.title + " " + it.body))
        }
        source("ui/screens/IslamicBooksScreen.kt").readLines().forEachIndexed { index, line ->
            assertFalse("IslamicBooksScreen.kt:${index + 1} names a provider: $line", providers.containsMatchIn(line))
        }
    }
}

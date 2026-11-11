package com.example.core.share

import org.junit.Assert.assertEquals
import org.junit.Test

class AppShareLinkTest {
    private val pkg = "com.example.app"
    private val url = "https://play.google.com/store/apps/details?id=com.example.app"

    @Test
    fun `store url uses package name`() = assertEquals(url, AppShareLink.storeUrl(pkg))

    @Test
    fun `link is appended on its own line`() = assertEquals("Dua text\n\n$url", AppShareLink.withLink("Dua text  \n", pkg))

    @Test
    fun `link is not added twice`() = assertEquals("Read\n$url", AppShareLink.withLink("Read\n$url", pkg))

    @Test
    fun `blank text becomes just the link`() = assertEquals(url, AppShareLink.withLink("  ", pkg))
}

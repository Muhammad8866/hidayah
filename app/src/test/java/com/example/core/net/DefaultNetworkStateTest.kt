package com.example.core.net

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rules for the app's online flag, driven the way the default-network callback drives them. Each
 * event carries its own network and capabilities; nothing is re-read from ConnectivityManager.
 * Network keys are plain strings here, compared by equality like android.net.Network (which a
 * callback delivers as a fresh object each time).
 */
class DefaultNetworkStateTest {
    private val wifi = "wifi"
    private val cell = "cell"

    private fun state() = DefaultNetworkState<String>()

    @Test
    fun `starts offline before any network is reported`() {
        assertFalse(state().isOnline)
    }

    @Test
    fun `validated capabilities after an unvalidated seed bring the app online`() {
        // The reported bug: the seed read inside onAvailable is stale (or activeNetwork is null),
        // and the old code re-read that stale state on every later event, so it stayed offline.
        val s = state()
        s.onAvailable(wifi, validatedSeed = false)
        assertFalse(s.isOnline)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = true)
        assertTrue(s.isOnline)
    }

    @Test
    fun `a network that stays validated is still online after the app comes back from background`() {
        // Coming back from background brings no capabilities or loss event for an unchanged network,
        // so nothing may flip the flag; the old code could have stored false from a null activeNetwork.
        val s = state()
        s.onAvailable(wifi, validatedSeed = false)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = true)
        assertTrue(s.isOnline)
    }

    @Test
    fun `internet without validation counts as offline`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = false)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = false)
        assertFalse(s.isOnline)
    }

    @Test
    fun `validation without the internet capability counts as offline`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = true)
        s.onCapabilitiesChanged(wifi, hasInternet = false, isValidated = true)
        assertFalse(s.isOnline)
    }

    @Test
    fun `losing validation goes offline and regaining it comes back`() {
        val s = state()
        s.onAvailable(cell, validatedSeed = true)
        s.onCapabilitiesChanged(cell, hasInternet = true, isValidated = true)
        s.onCapabilitiesChanged(cell, hasInternet = true, isValidated = false)
        assertFalse(s.isOnline)
        s.onCapabilitiesChanged(cell, hasInternet = true, isValidated = true)
        assertTrue(s.isOnline)
    }

    @Test
    fun `capabilities win over a validated seed`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = true)
        assertTrue(s.isOnline)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = false)
        assertFalse(s.isOnline)
    }

    @Test
    fun `a validated seed alone is online until capabilities say otherwise`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = true)
        assertTrue(s.isOnline)
    }

    @Test
    fun `late loss of the old network after a switch keeps the new one online`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = true)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = true)
        s.onAvailable(cell, validatedSeed = false)
        s.onCapabilitiesChanged(cell, hasInternet = true, isValidated = true)
        s.onLost(wifi)
        assertTrue(s.isOnline)
    }

    @Test
    fun `losing the current network goes offline`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = true)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = true)
        s.onLost(wifi)
        assertFalse(s.isOnline)
    }

    @Test
    fun `after a loss the next network starts fresh`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = true)
        s.onLost(wifi)
        assertFalse(s.isOnline)
        s.onAvailable(wifi, validatedSeed = false)
        assertFalse(s.isOnline)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = true)
        assertTrue(s.isOnline)
    }

    @Test
    fun `loss of an unknown network is ignored`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = true)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = true)
        s.onLost("vpn")
        assertTrue(s.isOnline)
    }

    @Test
    fun `a loss before any network does not crash or bring the app online`() {
        val s = state()
        s.onLost(wifi)
        assertFalse(s.isOnline)
    }

    @Test
    fun `capabilities for a not yet reported network adopt it`() {
        val s = state()
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = true)
        assertTrue(s.isOnline)
    }

    @Test
    fun `a new default network replaces the old validation state`() {
        val s = state()
        s.onAvailable(wifi, validatedSeed = false)
        s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = false)
        assertFalse(s.isOnline)
        s.onAvailable(cell, validatedSeed = true)
        assertTrue(s.isOnline)
    }

    @Test
    fun `networks are matched by equality, not identity`() {
        // A callback unparcels a new Network object for every event; equal ids are the same network.
        val s = state()
        s.onAvailable(StringBuilder("wifi").toString(), validatedSeed = false)
        s.onCapabilitiesChanged(StringBuilder("wifi").toString(), hasInternet = true, isValidated = true)
        assertTrue(s.isOnline)
        s.onLost(StringBuilder("wifi").toString())
        assertFalse(s.isOnline)
    }

    @Test
    fun `repeated identical events are harmless`() {
        val s = state()
        repeat(3) { s.onAvailable(wifi, validatedSeed = false) }
        repeat(3) { s.onCapabilitiesChanged(wifi, hasInternet = true, isValidated = true) }
        assertTrue(s.isOnline)
        repeat(3) { s.onLost(wifi) }
        assertFalse(s.isOnline)
    }
}

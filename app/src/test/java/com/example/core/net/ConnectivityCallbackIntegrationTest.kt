package com.example.core.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetwork
import org.robolectric.shadows.ShadowNetworkCapabilities

/**
 * Drives the real default-network callback against Robolectric's ConnectivityManager. The online
 * value must come from what each callback reports, never from activeNetwork, which is null while
 * the app is blocked in the background and left the app showing "offline" after it came back.
 *
 * Connectivity.init is a one-shot singleton, so these tests use trackDefaultNetwork directly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConnectivityCallbackIntegrationTest {

    private lateinit var cm: ConnectivityManager
    private val published = mutableListOf<Boolean>()

    private val wifi: Network = ShadowNetwork.newInstance(100)
    private val mobile: Network = ShadowNetwork.newInstance(101)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        published.clear()
    }

    private fun track(): ConnectivityManager.NetworkCallback = Connectivity.trackDefaultNetwork(cm) { published += it }

    private fun caps(vararg capabilities: Int): NetworkCapabilities =
        ShadowNetworkCapabilities.newInstance().also { c -> capabilities.forEach { shadowOf(c).addCapability(it) } }

    private fun validated() = caps(NetworkCapabilities.NET_CAPABILITY_INTERNET, NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    @Test
    fun registersTheCallbackWithConnectivityManager() {
        val callback = track()
        // The shadow keeps default and per-request callbacks in one set, so this proves registration,
        // not which register method was used.
        assertTrue(shadowOf(cm).networkCallbacks.contains(callback))
    }

    @Test
    fun validatedCapabilitiesMakeTheAppOnlineEvenWhileActiveNetworkIsNull() {
        shadowOf(cm).setActiveNetworkInfo(null)
        assertEquals(null, cm.activeNetwork)
        val callback = track()

        callback.onAvailable(wifi)
        callback.onCapabilitiesChanged(wifi, validated())

        assertTrue(published.last())
    }

    @Test
    fun blockedStatusInTheBackgroundDoesNotFlashOfflineOnResume() {
        // Blocked status is not tracked, so going to the background and back cannot show the
        // offline banner, a "Back online" snackbar or a reconnect retry.
        val callback = track()
        callback.onAvailable(wifi)
        callback.onCapabilitiesChanged(wifi, validated())
        assertTrue(published.last())

        callback.onBlockedStatusChanged(wifi, true)

        assertTrue(published.last())
    }

    @Test
    fun internetWithoutValidationCountsAsOffline() {
        val callback = track()
        callback.onAvailable(wifi)
        callback.onCapabilitiesChanged(wifi, caps(NetworkCapabilities.NET_CAPABILITY_INTERNET))

        assertFalse(published.last())
    }

    @Test
    fun switchingWifiToMobileStaysOnlineWhenTheOldNetworkIsLostLate() {
        val callback = track()
        callback.onAvailable(wifi)
        callback.onCapabilitiesChanged(wifi, validated())
        callback.onAvailable(mobile)
        callback.onCapabilitiesChanged(mobile, validated())

        callback.onLost(wifi)
        assertTrue(published.last())

        callback.onLost(mobile)
        assertFalse(published.last())
    }

    @Test
    fun losingAnUnknownNetworkBeforeAnyDefaultIsHarmless() {
        val callback = track()
        callback.onLost(wifi)

        assertFalse(published.isNotEmpty() && published.last())
    }

    @Test
    fun onAvailableAloneSeedsFromStoredCapabilities() {
        shadowOf(cm).setNetworkCapabilities(wifi, validated())
        val callback = track()

        callback.onAvailable(wifi)

        assertTrue(published.last())
    }
}

package com.example.core.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

/**
 * Load harness for the online flag. Each simulated device (user) gets its own random stream of
 * default-network callbacks over three networks: available (with a random seed), capabilities
 * (internet and validation flags) and lost, aimed mostly at the current network so every matching
 * path runs. Each event goes through [DefaultNetworkState] and through an independent reference
 * model written here; the two must agree after every event. On top of that the core rules are
 * checked directly: validated capabilities are online, anything else is offline, and a loss of the
 * current network is offline. Runs in steps of 10k, 100k and 1M devices; the printed numbers are the
 * measurement and the time bound is generous. Run it alone:
 * `gradlew :app:testPlayDebugUnitTest --tests "com.example.core.net.DefaultNetworkStateScaleTest"`.
 */
class DefaultNetworkStateScaleTest {
    // Ids stay inside the JVM's small-Integer cache, so boxing them for the generic class allocates nothing.
    private val networks: IntArray = intArrayOf(101, 102, 103)

    /** The rules restated as plain fields, kept apart from the production code on purpose. */
    private class Reference {
        var current = NONE
        var validated = false
        val online: Boolean get() = current != NONE && validated

        fun reset() { current = NONE; validated = false }

        fun available(net: Int, seed: Boolean) {
            current = net
            validated = seed
        }

        fun capabilities(net: Int, internet: Boolean, valid: Boolean) {
            current = net
            validated = internet && valid
        }

        fun lost(net: Int) {
            if (current == net) reset()
        }

        companion object { const val NONE = -1 }
    }

    private class StepResult(val devices: Int, val events: Long, val millis: Long, val heapMb: Long, val mismatches: Long, val ruleBreaks: Long, val firstFailure: String?)

    private fun runStep(devices: Int, seed: Long): StepResult {
        val random = Random(seed)
        val reference = Reference()
        var events = 0L
        var mismatches = 0L
        var ruleBreaks = 0L
        var firstFailure: String? = null
        val started = System.nanoTime()
        for (device in 0 until devices) {
            val state = DefaultNetworkState<Int>()
            reference.reset()
            val count = 8 + random.nextInt(13) // 8..20 events per device
            for (event in 0 until count) {
                // Mostly aim at the current network so the "same network" paths are exercised.
                val index = if (reference.current != Reference.NONE && random.nextInt(10) < 7) {
                    reference.current - networks[0]
                } else {
                    random.nextInt(networks.size)
                }
                val net = networks[index]
                val wasCurrent = reference.current == net
                val kind = random.nextInt(3)
                // Expected online state from the core rules, or null when that rule says nothing.
                val rule: Boolean? = when (kind) {
                    0 -> {
                        val seedValid = random.nextBoolean()
                        state.onAvailable(net, seedValid)
                        reference.available(net, seedValid)
                        seedValid
                    }
                    1 -> {
                        val internet = random.nextInt(10) != 0
                        val valid = random.nextInt(4) != 0
                        state.onCapabilitiesChanged(net, internet, valid)
                        reference.capabilities(net, internet, valid)
                        internet && valid
                    }
                    else -> {
                        state.onLost(net)
                        reference.lost(net)
                        if (wasCurrent) false else null
                    }
                }
                events++
                if (state.isOnline != reference.online) {
                    mismatches++
                    if (firstFailure == null) firstFailure = "device $device event $event kind $kind net $net: state=${state.isOnline} reference=${reference.online}"
                }
                if (rule != null && state.isOnline != rule) {
                    ruleBreaks++
                    if (firstFailure == null) firstFailure = "device $device event $event kind $kind net $net: state=${state.isOnline} rule=$rule"
                }
            }
        }
        val millis = (System.nanoTime() - started) / 1_000_000
        val runtime = Runtime.getRuntime()
        val heapMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        return StepResult(devices, events, millis, heapMb, mismatches, ruleBreaks, firstFailure)
    }

    @Test
    fun `online flag holds for a million devices of random network events`() {
        val steps = listOf(10_000, 100_000, 1_000_000)
        for ((n, devices) in steps.withIndex()) {
            val result = runStep(devices, seed = 20261111L + n)
            println(
                "DefaultNetworkState scale: devices=${result.devices} events=${result.events} " +
                    "ms=${result.millis} heapMB=${result.heapMb} mismatches=${result.mismatches} " +
                    "ruleBreaks=${result.ruleBreaks}" + (result.firstFailure?.let { " first=$it" } ?: "")
            )
            assertEquals("state and reference disagree: ${result.firstFailure}", 0L, result.mismatches)
            assertEquals("core rule broken: ${result.firstFailure}", 0L, result.ruleBreaks)
            assertTrue("events counted", result.events >= devices * 8L)
            if (devices == 1_000_000) {
                assertTrue("1M devices took ${result.millis} ms", result.millis < 60_000)
            }
        }
    }
}

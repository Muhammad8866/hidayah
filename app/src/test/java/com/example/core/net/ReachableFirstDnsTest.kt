package com.example.core.net

import okhttp3.Dns
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/** A CDN address that never answers must not stall a connection; only the order of addresses changes. */
class ReachableFirstDnsTest {
    private val pool = ScheduledThreadPoolExecutor(PROBE_THREADS).apply { removeOnCancelPolicy = true }

    @After fun stopProbes() {
        pool.shutdownNow()
    }

    private fun ip(a: Int, b: Int = 0): InetAddress =
        InetAddress.getByAddress("cdn.test", byteArrayOf(10, a.toByte(), b.toByte(), 1))

    private val a = ip(1)
    private val b = ip(2)
    private val c = ip(3)

    /** Live addresses answer after [answerMillis]; refused ones fail at once; any other hangs until its timeout, like a dropped SYN. */
    private class FakeNet(
        private val live: Set<InetAddress>,
        private val answerMillis: Long = 0,
        private val refused: Set<InetAddress> = emptySet()
    ) {
        private val calls = ConcurrentHashMap<InetAddress, AtomicInteger>()
        private val active = AtomicInteger()
        val peak = AtomicInteger()

        fun connect(address: InetAddress, timeoutMillis: Int): Boolean {
            calls.getOrPut(address) { AtomicInteger() }.incrementAndGet()
            peak.accumulateAndGet(active.incrementAndGet()) { x, y -> maxOf(x, y) }
            try {
                return when (address) {
                    in refused -> false
                    in live -> {
                        if (answerMillis > 0) Thread.sleep(answerMillis)
                        true
                    }
                    else -> {
                        Thread.sleep(timeoutMillis.toLong())
                        false
                    }
                }
            } finally {
                active.decrementAndGet()
            }
        }

        fun isLive(address: InetAddress) = address in live
        fun count(address: InetAddress) = calls[address]?.get() ?: 0
        fun total() = calls.values.sumOf { it.get() }
    }

    private fun answers(vararg addresses: InetAddress) = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> = addresses.toList()
    }

    private fun dns(
        net: FakeNet,
        delegate: Dns,
        clock: () -> Long = { 0L },
        stagger: Long = 50,
        wait: Long = 1_000,
        probe: Long = 2_000
    ) = ReachableFirstDns(
        delegate, net::connect, clock, stagger, wait, probe,
        ttlMillis = 120_000, missTtlMillis = 30_000, probes = pool
    )

    private fun <T> timed(block: () -> T): Pair<T, Long> {
        val started = System.nanoTime()
        val value = block()
        return value to TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
    }

    @Test fun deadFirstAddressMovesBehindTheOneThatAnswers() {
        val net = FakeNet(live = setOf(b, c))
        val (order, millis) = timed { dns(net, answers(a, b, c)).lookup("cdn.test") }
        assertEquals(listOf(b, c, a), order)
        // OkHttp alone would wait out its 10 s connect timeout on [a] before trying [b].
        assertTrue("took $millis ms", millis < 800)
    }

    @Test fun healthyFirstAddressKeepsTheOrderAndStopsProbing() {
        val net = FakeNet(live = setOf(a, b, c))
        assertEquals(listOf(a, b, c), dns(net, answers(a, b, c), stagger = 200).lookup("cdn.test"))
        Thread.sleep(500)
        assertEquals(1, net.total())
    }

    @Test fun singleAddressIsNotProbed() {
        val net = FakeNet(live = emptySet())
        assertEquals(listOf(a), dns(net, answers(a)).lookup("cdn.test"))
        assertEquals(0, net.total())
    }

    @Test fun nothingAnsweringInTimeKeepsTheSystemOrder() {
        val net = FakeNet(live = emptySet())
        val (order, millis) = timed { dns(net, answers(a, b, c), wait = 300).lookup("cdn.test") }
        assertEquals(listOf(a, b, c), order)
        assertTrue("took $millis ms", millis in 250L..1_000L)
    }

    @Test fun refusedEverywhereReturnsAtOnceWithTheSystemOrder() {
        val net = FakeNet(live = emptySet(), refused = setOf(a, b, c))
        val (order, millis) = timed { dns(net, answers(a, b, c), stagger = 500).lookup("cdn.test") }
        assertEquals(listOf(a, b, c), order)
        // A fast refusal starts the next address without waiting for the stagger.
        assertTrue("took $millis ms", millis < 400)
        assertEquals(3, net.total())
    }

    @Test fun failingConnectorCountsAsNoAnswer() {
        val connector = { address: InetAddress, _: Int -> if (address == a) throw IOException("boom") else true }
        val dns = ReachableFirstDns(answers(a, b, c), connector, { 0L }, 50, 1_000, 2_000, 120_000, 30_000, pool)
        assertEquals(listOf(b, c, a), dns.lookup("cdn.test"))
    }

    @Test fun lookupFailureStillReachesTheCaller() {
        val failing = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> = throw UnknownHostException(hostname)
        }
        assertThrows(UnknownHostException::class.java) { dns(FakeNet(emptySet()), failing).lookup("cdn.test") }
    }

    @Test fun concurrentLookupsShareOneRace() {
        val net = FakeNet(live = setOf(a), answerMillis = 150)
        val dns = dns(net, answers(a, b, c), stagger = 1_000)
        val start = CountDownLatch(1)
        val workers = Executors.newFixedThreadPool(16)
        try {
            val results = (1..16).map { workers.submit<List<InetAddress>> { start.await(); dns.lookup("cdn.test") } }
            start.countDown()
            results.forEach { assertEquals(listOf(a, b, c), it.get(5, TimeUnit.SECONDS)) }
        } finally {
            workers.shutdownNow()
        }
        assertEquals(1, net.count(a))
        assertEquals(0, net.count(b))
    }

    @Test fun rememberedAnswerIsReusedUntilItExpires() {
        val now = AtomicLong(0)
        val net = FakeNet(live = setOf(b))
        val dns = dns(net, answers(a, b, c), clock = { now.get() })
        assertEquals(listOf(b, c, a), dns.lookup("cdn.test"))
        now.set(60_000)
        assertEquals(listOf(b, c, a), dns.lookup("cdn.test"))
        assertEquals(1, net.count(b))
        now.set(121_000)
        assertEquals(listOf(b, c, a), dns.lookup("cdn.test"))
        assertEquals(2, net.count(b))
    }

    @Test fun clearAndForgetAllDropTheRememberedAnswer() {
        val net = FakeNet(live = setOf(b))
        val dns = dns(net, answers(a, b, c))
        dns.lookup("cdn.test")
        dns.clear()
        dns.lookup("cdn.test")
        assertEquals(2, net.count(b))
        ReachableFirstDns.forgetAll()
        dns.lookup("cdn.test")
        assertEquals(3, net.count(b))
    }

    @Test fun aMissIsRememberedSoTheNextLookupDoesNotWaitAgain() {
        val now = AtomicLong(0)
        val net = FakeNet(live = emptySet(), refused = setOf(a, b, c))
        val dns = dns(net, answers(a, b, c), clock = { now.get() })
        dns.lookup("cdn.test")
        dns.lookup("cdn.test")
        assertEquals(3, net.total())
        now.set(31_000)
        dns.lookup("cdn.test")
        assertEquals(6, net.total())
    }

    @Test fun rememberedAddressMissingFromANewAnswerIsNotUsed() {
        val net = FakeNet(live = setOf(b, c))
        var answer = listOf(a, b, c)
        val delegate = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> = answer
        }
        val dns = dns(net, delegate)
        assertEquals(listOf(b, c, a), dns.lookup("cdn.test"))
        answer = listOf(a, c)
        assertEquals(listOf(c, a), dns.lookup("cdn.test"))
    }

    /** Hosts [deadFirst] of which list an unreachable address first, looked up [lookups] times from 32 threads. */
    private fun scale(hosts: Int, lookups: Int, deadFirst: (Int) -> Boolean, wait: Long, probe: Long): Triple<Long, Int, Int> {
        val net = FakeNet(live = (0 until hosts).flatMap { listOf(ip(101, it), ip(102, it)) }.toSet(), answerMillis = 2)
        val delegate = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                val i = hostname.removePrefix("h").removeSuffix(".test").toInt()
                return if (deadFirst(i)) listOf(ip(100, i), ip(101, i), ip(102, i)) else listOf(ip(101, i), ip(102, i), ip(100, i))
            }
        }
        val dns = ReachableFirstDns(delegate, net::connect, System::currentTimeMillis, 50, wait, probe, 120_000, 30_000, pool)
        val workers = Executors.newFixedThreadPool(32)
        val liveFirst = AtomicInteger()
        val (_, millis) = timed {
            try {
                (0 until lookups).map { n ->
                    workers.submit<Unit> {
                        val order = dns.lookup("h${n % hosts}.test")
                        // Only the order may change: every address is still handed to OkHttp.
                        check(order.size == 3 && order.toSet().size == 3)
                        if (net.isLive(order.first())) liveFirst.incrementAndGet()
                    }
                }.forEach { it.get(120, TimeUnit.SECONDS) }
            } finally {
                workers.shutdownNow()
            }
        }
        assertTrue("peak probes ${net.peak.get()}", net.peak.get() <= PROBE_THREADS)
        return Triple(millis, net.peak.get(), liveFirst.get())
    }

    /** Scale: 10k lookups over 200 hosts, one in ten with a dead first address. */
    @Test fun manyHostsWithSomeDeadAddressesStayFastAndBounded() {
        val (millis, peak, liveFirst) = scale(200, 10_000, { it % 10 == 0 }, wait = 2_000, probe = 4_000)
        println("scale mixed: 10000 lookups, 200 hosts, 32 threads: $millis ms, peak probes $peak, live address first $liveFirst")
        assertEquals(10_000, liveFirst)
    }

    /** Scale, worst case: every host's first address is dead at once; lookups still all return, probes stay capped. */
    @Test fun everyHostDeadFirstDegradesToTheSystemOrderButNeverHangs() {
        val (millis, peak, liveFirst) = scale(200, 10_000, { true }, wait = 500, probe = 1_000)
        println("scale all-dead-first: 10000 lookups, 200 hosts, 32 threads: $millis ms, peak probes $peak, live address first $liveFirst")
        assertTrue("took $millis ms", millis < 60_000)
    }

    private companion object {
        const val PROBE_THREADS = 16
    }
}

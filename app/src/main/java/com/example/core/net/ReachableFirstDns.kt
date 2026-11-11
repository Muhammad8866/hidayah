package com.example.core.net

import okhttp3.Dns
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel
import java.util.Collections
import java.util.Locale
import java.util.WeakHashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Puts the address that answers first at the front of a host's address list.
 *
 * OkHttp 4 tries a host's addresses one after another and waits out the whole connect timeout on
 * an address that never answers. A CDN that lists such an address first then stalls every new
 * connection for that long. Here short TCP handshakes race in the [delegate]'s order, a little
 * apart, and the address that answers first is handed to OkHttp first. Only the order changes:
 * every address is still returned, and when nothing answers in time the delegate's order is kept.
 * A probe never makes a lookup fail.
 *
 * One race per host runs at a time; its answer is remembered briefly and forgotten on
 * [clear] / [forgetAll], which the app calls when the phone switches networks.
 */
internal class ReachableFirstDns(
    private val delegate: Dns,
    /** Tries one TCP handshake within the given milliseconds; true when it was accepted. */
    private val connector: (InetAddress, Int) -> Boolean = ::connectHttps,
    private val nowMillis: () -> Long = System::currentTimeMillis,
    /** Gap before the next address is tried while earlier ones are still silent. */
    private val staggerMillis: Long = STAGGER_MILLIS,
    /** Longest a lookup waits for an answer before it falls back to the delegate's order. */
    private val waitMillis: Long = WAIT_MILLIS,
    /** Longest a probe runs; a late answer is still remembered for the next lookup. */
    private val probeMillis: Long = PROBE_MILLIS,
    private val ttlMillis: Long = TTL_MILLIS,
    /** How long "nothing answered" is remembered, so lookups do not wait again and again. */
    private val missTtlMillis: Long = MISS_TTL_MILLIS,
    private val probes: ScheduledExecutorService = sharedProbes
) : Dns {

    /** [first] answered first and [behind] were tried without answering before it; a null [first] means nothing answered. */
    private class Choice(val first: InetAddress?, val behind: Set<InetAddress>, val at: Long)

    private val remembered = ConcurrentHashMap<String, Choice>()
    private val racing = ConcurrentHashMap<String, Race>()

    init {
        synchronized(instances) { instances += this }
    }

    override fun lookup(hostname: String): List<InetAddress> {
        val addresses = delegate.lookup(hostname)
        if (addresses.size < 2) return addresses
        return try {
            ordered(hostname.lowercase(Locale.ROOT), addresses)
        } catch (interrupted: InterruptedException) {
            Thread.currentThread().interrupt()
            addresses
        } catch (_: Exception) {
            addresses
        }
    }

    /** Forgets every remembered answer, for example after the phone changed networks. */
    fun clear() = remembered.clear()

    private fun ordered(host: String, addresses: List<InetAddress>): List<InetAddress> {
        remembered[host]?.let { choice ->
            val age = nowMillis() - choice.at
            val ttl = if (choice.first == null) missTtlMillis else ttlMillis
            if (age in 0 until ttl && (choice.first == null || choice.first in addresses)) return arrange(addresses, choice)
            remembered.remove(host, choice)
        }
        var created: Race? = null
        val race = racing.compute(host) { _, running ->
            if (running != null && !running.settled) running else Race(host, addresses).also { created = it }
        } ?: return addresses
        created?.start()
        val choice = race.await() ?: return addresses
        return if (choice.first == null || choice.first in addresses) arrange(addresses, choice) else addresses
    }

    private fun arrange(addresses: List<InetAddress>, choice: Choice): List<InetAddress> {
        val first = choice.first ?: return addresses
        val rest = addresses.filter { it != first }
        return listOf(first) + rest.filterNot { it in choice.behind } + rest.filter { it in choice.behind }
    }

    private fun remember(host: String, choice: Choice) {
        if (remembered.size >= MAX_HOSTS) remembered.clear()
        remembered[host] = choice
    }

    /** Happy-eyeballs style: address i starts at i × stagger, or at once when every earlier one has failed. */
    private inner class Race(private val host: String, private val addresses: List<InetAddress>) {
        private val outcome = CompletableFuture<Choice>()
        private val started = System.nanoTime()
        private val launched = AtomicInteger()
        private val finished = AtomicInteger()
        private val tasks: MutableList<Future<*>> = Collections.synchronizedList(ArrayList())

        val settled: Boolean get() = outcome.isDone

        fun start() {
            try {
                for (index in addresses.indices) {
                    tasks += probes.schedule(Runnable { launchNext() }, index * staggerMillis, TimeUnit.MILLISECONDS)
                }
            } catch (_: Exception) {
                // An executor that refuses work must not leave this race waited on forever.
                settle(null)
            }
        }

        /** The answer once something answered or everything failed; null when the lookup's wait ran out first. */
        fun await(): Choice? {
            val left = started + TimeUnit.MILLISECONDS.toNanos(waitMillis) - System.nanoTime()
            return try {
                outcome.get(maxOf(left, 0L), TimeUnit.NANOSECONDS)
            } catch (_: TimeoutException) {
                null
            } catch (_: ExecutionException) {
                null
            }
        }

        private fun launchNext() {
            if (outcome.isDone) return
            val index = launched.getAndIncrement()
            if (index < addresses.size) probe(addresses[index])
        }

        private fun probe(address: InetAddress) {
            val left = TimeUnit.NANOSECONDS.toMillis(started + TimeUnit.MILLISECONDS.toNanos(probeMillis) - System.nanoTime())
            val answered = left > 0 && !outcome.isDone && try {
                connector(address, left.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
            } catch (_: Exception) {
                false
            }
            when {
                answered -> settle(address)
                finished.incrementAndGet() >= addresses.size -> settle(null)
                // A quick refusal starts the next address without waiting for its turn.
                else -> launchNext()
            }
        }

        private fun settle(first: InetAddress?) {
            val tried = addresses.take(minOf(launched.get(), addresses.size))
            val choice = Choice(first, tried.filter { it != first }.toSet(), nowMillis())
            if (!outcome.complete(choice)) return
            remember(host, choice)
            racing.remove(host, this)
            // Stops waiting probes and interrupts running ones, which closes their sockets.
            synchronized(tasks) { tasks.forEach { it.cancel(true) } }
        }
    }

    companion object {
        private const val STAGGER_MILLIS = 250L
        private const val WAIT_MILLIS = 2_000L
        private const val PROBE_MILLIS = 5_000L
        private const val TTL_MILLIS = 120_000L
        private const val MISS_TTL_MILLIS = 30_000L
        private const val MAX_HOSTS = 256
        /** Caps probes running at once across the app; idle threads end after a while. */
        private const val PROBE_THREADS = 16

        private val sharedProbes: ScheduledExecutorService =
            ScheduledThreadPoolExecutor(PROBE_THREADS) { task -> Thread(task, "dns-probe").apply { isDaemon = true } }.apply {
                setKeepAliveTime(30, TimeUnit.SECONDS)
                allowCoreThreadTimeOut(true)
                removeOnCancelPolicy = true
            }

        private val instances: MutableSet<ReachableFirstDns> = Collections.newSetFromMap(WeakHashMap())

        /** Forgets what every instance remembered; call when the phone switches networks. */
        fun forgetAll() {
            synchronized(instances) { instances.toList() }.forEach { it.clear() }
        }
    }
}

private const val HTTPS_PORT = 443

/** One TCP handshake with the HTTPS port, closed at once. Interruptible, so a settled race frees its thread. */
private fun connectHttps(address: InetAddress, timeoutMillis: Int): Boolean = try {
    SocketChannel.open().use { channel ->
        channel.socket().connect(InetSocketAddress(address, HTTPS_PORT), timeoutMillis)
        true
    }
} catch (_: Exception) {
    false
}

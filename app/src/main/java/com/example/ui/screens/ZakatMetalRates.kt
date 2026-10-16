package com.example.ui.screens

import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Live spot prices of gold (XAU) and silver (XAG) per troy ounce in every Zakat currency, from the
 * free open currency API (daily, no key). The last snapshot is kept on the phone so the calculator
 * still works offline.
 */
internal object ZakatMetalRates {
    private const val PREF_KEY = "zakat_live_rates"
    private val TROY_OUNCE_GRAMS = BigDecimal("31.1034768")
    /** Refresh at most this often; the source itself updates once a day. */
    private const val MAX_AGE_MS = 3 * 60 * 60 * 1000L
    private val MIRRORS = listOf(
        "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/%s.json",
        "https://latest.currency-api.pages.dev/v1/currencies/%s.json"
    )
    private val client by lazy {
        com.example.core.net.FastNet.client.newBuilder().connectTimeout(8, TimeUnit.SECONDS).readTimeout(12, TimeUnit.SECONDS).build()
    }

    data class Snapshot(
        val date: LocalDate,
        val fetchedAt: Long,
        val goldPerOunce: Map<String, BigDecimal>,
        val silverPerOunce: Map<String, BigDecimal>
    ) {
        fun isStale(now: Long = System.currentTimeMillis()) = now - fetchedAt > MAX_AGE_MS || now < fetchedAt

        /** Price of one [unit] of pure (24K / 999) metal, rounded to 2 decimals; null if [currency] is missing. */
        fun rate(gold: Boolean, currency: String, unit: MetalUnit): BigDecimal? =
            (if (gold) goldPerOunce else silverPerOunce)[currency]
                ?.multiply(BigDecimal(unit.grams))
                ?.divide(TROY_OUNCE_GRAMS, 2, RoundingMode.HALF_UP)
    }

    suspend fun fetch(currencies: List<String>): Snapshot = coroutineScope {
        val gold = async { fetchMetal("xau", currencies) }
        val silver = async { fetchMetal("xag", currencies) }
        val (goldDate, goldPrices) = gold.await()
        val (_, silverPrices) = silver.await()
        Snapshot(goldDate, System.currentTimeMillis(), goldPrices, silverPrices)
    }

    private suspend fun fetchMetal(code: String, currencies: List<String>): Pair<LocalDate, Map<String, BigDecimal>> =
        withContext(Dispatchers.IO) {
            var failure: Exception = IOException("No rate source answered")
            for (mirror in MIRRORS) {
                try {
                    val body = client.newCall(Request.Builder().url(mirror.format(code)).build()).execute().use { response ->
                        if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                        response.body?.string().orEmpty()
                    }
                    return@withContext parse(body, code, currencies)
                } catch (e: Exception) {
                    failure = e
                }
            }
            throw failure
        }

    internal fun parse(body: String, code: String, currencies: List<String>): Pair<LocalDate, Map<String, BigDecimal>> {
        val json = JSONObject(body)
        val prices = json.getJSONObject(code)
        val map = currencies.mapNotNull { currency ->
            prices.opt(currency.lowercase())?.toString()?.toBigDecimalOrNull()
                ?.takeIf { it.signum() > 0 }?.let { currency to it }
        }.toMap()
        if (map.isEmpty()) throw IOException("No prices for $code")
        return LocalDate.parse(json.getString("date")) to map
    }

    fun load(prefs: SharedPreferences): Snapshot? = runCatching {
        val json = JSONObject(prefs.getString(PREF_KEY, null) ?: return null)
        fun prices(key: String) = json.getJSONObject(key).let { obj ->
            obj.keys().asSequence().associateWith { BigDecimal(obj.getString(it)) }
        }
        Snapshot(LocalDate.parse(json.getString("date")), json.getLong("fetchedAt"), prices("gold"), prices("silver"))
    }.getOrNull()

    fun save(prefs: SharedPreferences, snapshot: Snapshot) {
        fun prices(map: Map<String, BigDecimal>) = JSONObject().apply { map.forEach { (k, v) -> put(k, v.toPlainString()) } }
        val json = JSONObject()
            .put("date", snapshot.date.toString())
            .put("fetchedAt", snapshot.fetchedAt)
            .put("gold", prices(snapshot.goldPerOunce))
            .put("silver", prices(snapshot.silverPerOunce))
        prefs.edit().putString(PREF_KEY, json.toString()).apply()
    }
}

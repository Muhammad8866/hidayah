package com.example.feature.communitymasjid.data.remote

import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmMosque
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Overpass (OpenStreetMap) client owned by the Community Masjid feature. */
class CmOsmDataSource {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Hedged request: the first mirror starts at once and each further mirror starts
     * [HEDGE_DELAY_MS] later only if nothing has answered yet (or right away when one fails).
     * The first successful answer wins and the other calls are cancelled.
     */
    suspend fun nearby(latitude: Double, longitude: Double, radiusMetres: Int): List<CmMosque> = coroutineScope {
        // A bbox filter is served straight from Overpass's spatial index and is several times faster
        // than (around:...); the circle is applied client-side after parsing.
        val dLat = radiusMetres / 111_320.0
        val dLon = radiusMetres / (111_320.0 * cos(Math.toRadians(latitude)).coerceAtLeast(0.01))
        val bbox = "%.5f,%.5f,%.5f,%.5f".format(Locale.US, latitude - dLat, longitude - dLon, latitude + dLat, longitude + dLon)
        val query = """
            [out:json][timeout:10][bbox:$bbox];
            (nwr[amenity=place_of_worship][religion=muslim];
             nwr[building=mosque];);
            out center tags qt;
        """.trimIndent()
        val results = Channel<Result<String>>(ENDPOINTS.size)
        val failedEarly = Channel<Unit>(ENDPOINTS.size)
        val calls = ENDPOINTS.mapIndexed { index, endpoint ->
            launch {
                if (index > 0) {
                    // Start early if an earlier mirror already failed, otherwise after the hedge delay.
                    withTimeoutOrNull(HEDGE_DELAY_MS * index) { failedEarly.receive() }
                }
                val result = runCatching { fetch(endpoint, query) }
                if (result.isFailure) failedEarly.trySend(Unit)
                results.send(result)
            }
        }
        var lastError: Throwable? = null
        repeat(ENDPOINTS.size) {
            val result = results.receive()
            result.onSuccess { body ->
                calls.forEach { it.cancel() }
                return@coroutineScope withContext(Dispatchers.Default) {
                    parse(body, latitude, longitude).filter { (it.distanceMetres ?: Double.MAX_VALUE) <= radiusMetres }
                }
            }
            lastError = result.exceptionOrNull()
        }
        throw lastError ?: IllegalStateException("OpenStreetMap is unavailable")
    }

    /** Cancellable async OkHttp call, so losing mirrors are aborted as soon as one answers. */
    private suspend fun fetch(endpoint: String, query: String): String = suspendCancellableCoroutine { cont ->
        val request = Request.Builder()
            .url(endpoint)
            .header("User-Agent", "DeenCompanion-Android/1.0 (community-masjid)")
            .post(FormBody.Builder().add("data", query).build())
            .build()
        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val outcome = runCatching {
                        if (!it.isSuccessful) error("OpenStreetMap returned ${it.code}")
                        it.body?.string().orEmpty()
                    }
                    if (cont.isActive) outcome.fold(cont::resume, cont::resumeWithException)
                }
            }
        })
    }

    private fun parse(body: String, latitude: Double, longitude: Double): List<CmMosque> {
        val elements = JSONObject(body).getJSONArray("elements")
        return buildList {
            for (index in 0 until elements.length()) {
                val element = elements.getJSONObject(index)
                val type = element.optString("type")
                val osmId = element.optLong("id", -1)
                if (type !in setOf("node", "way", "relation") || osmId <= 0) continue
                val tags = element.optJSONObject("tags") ?: continue
                val center = element.optJSONObject("center")
                val lat = if (element.has("lat")) element.optDouble("lat") else center?.optDouble("lat")
                val lon = if (element.has("lon")) element.optDouble("lon") else center?.optDouble("lon")
                if (lat == null || lon == null || lat.isNaN() || lon.isNaN()) continue
                add(
                    CmMosque(
                        docId = "${type}_$osmId",
                        name = tags.optString("name").ifBlank { tags.optString("name:en") }
                            .ifBlank { "Masjid" }.take(120),
                        address = address(tags),
                        latitude = lat,
                        longitude = lon,
                        distanceMetres = CmTimingRules.distanceMetres(latitude, longitude, lat, lon)
                    )
                )
            }
        }.distinctBy { it.docId }.sortedBy { it.distanceMetres }
    }

    private companion object {
        const val HEDGE_DELAY_MS = 1_200L
        val ENDPOINTS = listOf(
            "https://overpass-api.de/api/interpreter",
            "https://maps.mail.ru/osm/tools/overpass/api/interpreter",
            "https://overpass.private.coffee/api/interpreter",
            "https://overpass.kumi.systems/api/interpreter"
        )
    }

    private fun address(tags: JSONObject): String {
        tags.optString("addr:full").takeIf { it.isNotBlank() }?.let { return it }
        val street = listOf(tags.optString("addr:housenumber"), tags.optString("addr:street"))
            .filter { it.isNotBlank() }.joinToString(" ")
        return listOf(
            street,
            tags.optString("addr:suburb"),
            tags.optString("addr:city").ifBlank { tags.optString("addr:town") }
        ).filter { it.isNotBlank() }.distinct().joinToString(", ")
    }
}

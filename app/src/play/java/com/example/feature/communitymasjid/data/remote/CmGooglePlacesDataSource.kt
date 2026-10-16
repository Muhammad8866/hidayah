package com.example.feature.communitymasjid.data.remote

import android.content.Context
import com.example.BuildConfig
import com.example.feature.communitymasjid.domain.CmTimingRules
import com.example.feature.communitymasjid.model.CmMosque
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Native SDK allows package name + signing certificate restrictions on the API key. */
class CmGooglePlacesDataSource(private val context: Context) {
    val isConfigured: Boolean get() = BuildConfig.GOOGLE_MAPS_API_KEY.isNotBlank() &&
        BuildConfig.GOOGLE_MAPS_API_KEY != "YOUR_GOOGLE_MAPS_API_KEY"

    suspend fun nearby(latitude: Double, longitude: Double, radiusMetres: Int): List<CmMosque> {
        check(isConfigured) { "Nearby Google masjids are not configured in this build." }
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(context.applicationContext, BuildConfig.GOOGLE_MAPS_API_KEY)
        }
        val cancellation = CancellationTokenSource()
        val request = SearchNearbyRequest.builder(
            CircularBounds.newInstance(LatLng(latitude, longitude), radiusMetres.toDouble()),
            listOf(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION)
        ).setIncludedTypes(listOf("mosque"))
            .setRankPreference(SearchNearbyRequest.RankPreference.DISTANCE)
            .setMaxResultCount(20)
            .setCancellationToken(cancellation.token)
            .build()
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { cancellation.cancel() }
            Places.createClient(context.applicationContext).searchNearby(request)
                .addOnSuccessListener { response ->
                    val mosques = response.places.mapNotNull { place ->
                        val id = place.id ?: return@mapNotNull null
                        val point = place.location ?: return@mapNotNull null
                        CmMosque(
                            docId = "google_$id",
                            name = place.displayName?.take(120) ?: "Masjid",
                            address = place.formattedAddress.orEmpty(),
                            latitude = point.latitude,
                            longitude = point.longitude,
                            distanceMetres = CmTimingRules.distanceMetres(latitude, longitude, point.latitude, point.longitude)
                        )
                    }.distinctBy { it.docId }.sortedBy { it.distanceMetres }
                    if (continuation.isActive) continuation.resume(mosques)
                }
                .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }
    }
}

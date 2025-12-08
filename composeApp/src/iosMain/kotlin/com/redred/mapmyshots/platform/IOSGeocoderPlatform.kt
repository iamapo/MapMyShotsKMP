package com.redred.mapmyshots.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import kotlin.coroutines.resume

class IOSGeocoderPlatform : GeocoderPlatform {

    override suspend fun reverseGeocode(lat: Double, lon: Double): String? =
        suspendCancellableCoroutine { cont ->
            val geocoder = CLGeocoder()
            val location = CLLocation(latitude = lat, longitude = lon)

            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                if (error != null) {
                    cont.resume(null)
                    return@reverseGeocodeLocation
                }

                val placemark = (placemarks?.firstOrNull() as? CLPlacemark)
                if (placemark == null) {
                    cont.resume(null)
                    return@reverseGeocodeLocation
                }

                val city = placemark.locality
                val country = placemark.country

                val result = listOfNotNull(city, country).joinToString(", ")
                    .ifBlank { null }

                cont.resume(result)
            }

            cont.invokeOnCancellation {
                geocoder.cancelGeocode()
            }
        }
}
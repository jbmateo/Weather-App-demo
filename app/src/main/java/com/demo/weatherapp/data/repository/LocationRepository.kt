package com.demo.weatherapp.data.repository

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.demo.weatherapp.domain.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient
) {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Result<UserLocation> = runCatching {
        if (!hasLocationPermission()) {
            throw SecurityException("Location permission is required to fetch local weather.")
        }

        val cachedLocation = fusedLocationProviderClient.lastLocation.await()
        val location = cachedLocation ?: fusedLocationProviderClient
            .getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            )
            .await() ?: requestSingleLocationUpdate()

        requireNotNull(location) {
            "Could not determine your current location. Check location services and try again."
        }

        // return user location
        UserLocation(
            latitude = location.latitude,
            longitude = location.longitude
        )
    }

    // Fallback in case fusedLocation returns null
    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocationUpdate(): Location =
        suspendCancellableCoroutine { continuation ->
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1_000L
            )
                .setMaxUpdates(1)
                .build()

            val callback = object : LocationCallback() {

                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    fusedLocationProviderClient.removeLocationUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
            }

            fusedLocationProviderClient.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()
            )

            continuation.invokeOnCancellation {
                fusedLocationProviderClient.removeLocationUpdates(callback)
            }
        }

    private fun hasLocationPermission(): Boolean {
        val fine =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
    }
}

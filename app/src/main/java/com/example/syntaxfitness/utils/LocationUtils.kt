package com.example.syntaxfitness.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationUtils {
    @SuppressLint("MissingPermission")
    fun getLocation(
        context: Context,
        onSuccess: (Location) -> Unit,
    ) {
        val cancellationTokenSource = CancellationTokenSource()

        LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            // Log the location details to Logcat for debugging
            Log.i(
                "LocationUtils",
                "Location retrieved successfully: Latitude=${location.latitude}, Longitude=${location.longitude}, Accuracy=${location.accuracy}m"
            )

            // Also log additional location details if available
            Log.d(
                "LocationUtils",
                "Additional location info: Provider=${location.provider}, Time=${location.time}, Altitude=${location.altitude}"
            )

            // Call the original success callback
            onSuccess(location)
        }.addOnFailureListener { exception ->
            Log.e(
                "LocationUtils",
                "Failed to get location: ${exception.message}"
            )
        }
    }
}
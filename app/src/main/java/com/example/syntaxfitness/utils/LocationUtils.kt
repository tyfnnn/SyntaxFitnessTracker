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
            onSuccess(location)
        }.addOnFailureListener { exception ->
            Log.i(
                "LocationUtils",
                "get location error ${exception.message}"
            )
        }
    }
}
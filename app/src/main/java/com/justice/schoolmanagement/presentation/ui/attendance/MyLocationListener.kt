package com.justice.schoolmanagement.presentation.ui.attendance

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

class MyLocationListener(private val listenerCallbacks: LocationListenerCallbacks, private val mLocationManager: LocationManager?) : LocationListener {
    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged: $location")
        listenerCallbacks.sendLocation(location)
        mLocationManager?.removeUpdates(this)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        Log.d(TAG, "onStatusChanged: ")
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "onProviderEnabled: ")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "onProviderDisabled: ")
        listenerCallbacks.onProviderDisabled()
    }

    interface LocationListenerCallbacks {
        fun onProviderDisabled()
        fun sendLocation(location: Location?)
    }

    companion object {
        private const val TAG = "MyLocationListener"
        var currentLocation: Map<*, *>? = null
    }
}
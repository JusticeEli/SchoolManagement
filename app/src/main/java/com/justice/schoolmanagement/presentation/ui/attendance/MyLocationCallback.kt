package com.justice.schoolmanagement.presentation.ui.attendance

import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class MyLocationCallback( val sendLocation:(Location)->Unit): LocationCallback() {
      companion object {
              private  const val TAG="MyLocationCallback"
          }


    override fun onLocationResult(locationResult: LocationResult) {
        Log.d(TAG, "onLocationResult: ")
        if (locationResult == null) {
            Log.d(TAG, "onLocationResult: location result is null")
            return
        }

        sendLocation(locationResult.lastLocation)


    }
}
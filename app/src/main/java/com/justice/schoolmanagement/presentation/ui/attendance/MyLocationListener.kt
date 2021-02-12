package com.justice.schoolmanagement.presentation.ui.attendance;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.Map;

public class MyLocationListener implements LocationListener {

    private static final String TAG = "MyLocationListener";
    public static Map currentLocation;
    private LocationListenerCallbacks listenerCallbacks;
    private LocationManager mLocationManager;

    public MyLocationListener(LocationListenerCallbacks listenerCallbacks, LocationManager mLocationManager) {
        this.listenerCallbacks = listenerCallbacks;
        this.mLocationManager = mLocationManager;
    }


    @Override
    public void onLocationChanged(final Location location) {
        onLocationChangedUserDefined(location);

    }

    public void onLocationChangedUserDefined(final Location location) {


        Log.d(TAG, "onLocationChangedUserDefined:  sending location " + location);

        listenerCallbacks.sendLocation(location);


        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        listenerCallbacks.onProviderDisabled();

    }

    public interface LocationListenerCallbacks {

        void onProviderDisabled();

        void sendLocation(Location location);

    }
}

package com.hfad.odometer

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.core.content.ContextCompat
import java.util.Random

class OdometerService : Service() {

    private val binder: IBinder = OdometerBinder()
    var listener: LocationListener? = null
    var locManager: LocationManager? = null
    val PERMISSION_STRING = android.Manifest.permission.ACCESS_FINE_LOCATION
    var distanceInMeters: Double = 0.0
    var lastLocation: Location? = null


    override fun onCreate() {
        super.onCreate()

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                if (lastLocation == null) lastLocation = location
                distanceInMeters = distanceInMeters?.plus(location?.distanceTo(lastLocation)!!)
                lastLocation = location
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

            override fun onProviderEnabled(provider: String?) = Unit

            override fun onProviderDisabled(provider: String?) = Unit
        }

        locManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val provider = locManager!!.getBestProvider(Criteria(), true)
            if (provider != null) locManager!!.requestLocationUpdates(provider, 1000, 0f, listener)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (locManager != null && listener != null) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                == PackageManager.PERMISSION_GRANTED
            ) {
                locManager!!.removeUpdates(listener)
            }
            locManager = null
            listener = null
        }
    }

    inner class OdometerBinder : Binder() {
        fun getOdometer(): OdometerService {
            return OdometerService()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun getDistance(): Double {
        return this.distanceInMeters / 1609.344
    }
}



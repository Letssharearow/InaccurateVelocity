package com.example.velocity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.velocity.R

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private var startingLocation: Location? = null
    private var currentLocation: Location? = null
    private var locationListener: LocationListener? = null
    private lateinit var textView: TextView
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity_debug", "onCreate")
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.output)
        findViewById<Button>(R.id.button)
            .setOnClickListener {
                Log.d("MainActivity_debug", "User tapped the Supabutton")
                startingLocation = currentLocation
            }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MainActivity_debug", "checkSelfPermission")
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            Log.d("MainActivity_debug", "checkSelfPermission done")

        } else {
            // Location permission already granted
            Log.d("MainActivity_debug", "getLocation")
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        Log.i("MainActivity_debug", "startLocationUpdates")
        val location: Location? =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        Log.i("MainActivity_debug", location.toString())

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.i("MainActivity_debug", "onLocationChanged")
                currentLocation = location
                if(startingLocation === null){
                    return
                }
                val velocityMps = location.speed // Velocity in meters per second

                val distance = location.distanceTo(startingLocation!!)
                Log.i("MainActivity_debug", "distance: $distance")

                val time = (location.time - startingLocation!!.time) / 1000
                Log.i("MainActivity_debug", "time: $time")


                // Convert velocity from meters per second to minutes per kilometer
                val velocityMpk = if (velocityMps != 0f) 1000 / velocityMps / 60 else 0f

                val velocityText = getString(R.string.velocity_format, velocityMpk)
                textView.text = time.toString() + " " +  distance.toString()
                Log.i("MainActivity_debug", "velocityMpk: $velocityMpk")
            }
            override fun onProviderDisabled(provider: String) {}

            override fun onProviderEnabled(provider: String) {
                Log.i("MainActivity_debug", provider)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        Log.i("MainActivity_debug", locationListener.toString())

        // Register the location listener to receive periodic updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Get the last known location from the provider
            Log.d("MainActivity_debug", "getLocation for real")
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            LOCATION_UPDATE_INTERVAL,
            MIN_DISTANCE_CHANGE_FOR_UPDATES,
            locationListener as LocationListener
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission granted
                startLocationUpdates()
            } else {
                Log.d("MainActivity_debug", "onRequestPermissionsResult")
                // Location permission denied
                // Handle the denial or disable location-related functionality
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Unregister the location listener when the activity is destroyed
        locationManager.removeUpdates(locationListener as LocationListener)
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 123
        private const val LOCATION_UPDATE_INTERVAL: Long = 1000 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 0f // 0 meters
    }
}

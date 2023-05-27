package com.example.velocity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.velocity.R

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private var previousLocation: Location? = null
    private var previousTimestamp: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Location permission already granted
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Get the last known location from the provider
            val location: Location? =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        // Check if there is a previous location
        if (previousLocation != null && location != null) {
            val currentTimestamp = System.currentTimeMillis()

            // Calculate the time difference in seconds
            val timeDifference = (currentTimestamp - previousTimestamp) / 1000.0

            // Calculate the distance between the previous and current location
            val distance = previousLocation!!.distanceTo(location)

            // Calculate the velocity (speed) in meters per second
            val velocity = distance / timeDifference

            Log.i("MainActivity", "Velocity: $velocity m/s")
        }
        }
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
                getLocation()
            } else {
                print("ACCESS DENIED")
                // Location permission denied
                // Handle the denial or disable location-related functionality
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 123
    }
}

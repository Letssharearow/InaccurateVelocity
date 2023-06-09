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
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.velocity.R

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private var startingLocation: Location? = null
    private var prevLocation: Location? = null
    private var currentLocation: Location? = null
    private var locationListener: LocationListener? = null
    private var meters: Double = 0.0
    private lateinit var timeView: TextView
    private lateinit var distanceView: TextView
    private lateinit var velocityView: TextView
    private var running: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity_debug", "onCreate")
        setContentView(R.layout.activity_main)

        timeView = findViewById(R.id.time)
        distanceView = findViewById(R.id.distance)
        velocityView = findViewById(R.id.velocity)
        findViewById<Button>(R.id.button)
            .setOnClickListener {
                Log.d("MainActivity_debug", "User tapped the Supabutton")
                running = !running
                if (running) {
                    // Change button text to "Stop" when running is true
                    findViewById<Button>(R.id.button).text = "Stop"
                    // Set the running logic here
                    startingLocation = currentLocation
                    prevLocation = startingLocation
                    meters = 0.0

                    // Hide distanceView and timeView when running is true
                    distanceView.visibility = View.GONE
                    timeView.visibility = View.GONE
                } else {
                    // Change button text to "Start" when running is false
                    findViewById<Button>(R.id.button).text = "Start"
                    // Set the stopping logic here

                    // Show distanceView and timeView when running is false
                    distanceView.visibility = View.VISIBLE
                    timeView.visibility = View.VISIBLE
                }
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

    private fun getTime(seconds: Long): String {
        val hours = seconds / 3600;
        val minutes = (seconds % 3600) / 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds % 60)
    }

    private fun convertSecondsToMinutesWithSeconds(seconds: Double): String {
        Log.i("MainActivity_debug", "seconds: $seconds")
        if(seconds == Double.POSITIVE_INFINITY){
            return ""
        }
//        val seconds = 239f
        val mins = (seconds / 60)
        Log.i("MainActivity_debug", "mins: $mins")
        val minInt = mins.toInt()
        Log.i("MainActivity_debug", "minInt: $minInt")
        val decimals = mins - minInt
        Log.i("MainActivity_debug", "decimals: $decimals")
        val secs = decimals * 60
        Log.i("MainActivity_debug", "secs: $secs")
        val result = "%d.%02d".format(minInt, secs.toInt())
        Log.i("MainActivity_debug", "result: $result")
        return result
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
                if(startingLocation === null || !running){
                    startingLocation = location
                    return
                }

                val newMeters = prevLocation?.distanceTo(currentLocation!!) ?: 0f
                meters += newMeters - (newMeters * Math.random() * 18 * 0.01)
                prevLocation = currentLocation
                distanceView.text =  "Distanz: " +  getString(R.string.distance, meters / 1000)
                Log.i("MainActivity_debug", "meters: $meters")

                val time = (location.time - startingLocation!!.time) / 1000
                val timeString = "Zeit: " + getTime(time)
                timeView.text = timeString
                Log.i("MainActivity_debug", "time: $timeString")

                val minutes = time / 60f
                Log.i("MainActivity_debug", "minutes: $minutes")
                val kilometers = (meters / 1000f)
                Log.i("MainActivity_debug", "kilometers: $kilometers")
                val velocitySpk = time / kilometers

                velocityView.text = convertSecondsToMinutesWithSeconds(velocitySpk) + " min/km"
                Log.i("MainActivity_debug", "velocityMpk: $velocitySpk")
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
        private const val LOCATION_UPDATE_INTERVAL: Long = 100 // 1 second
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 0f // 0 meters
    }
}

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.velocity.R

class MainActivity : AppCompatActivity() {

    private lateinit var locationManager: LocationManager
    private var locationListener: LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("MainActivity_debug", "ActivityCompat.checkSelfPermission")
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // Location permission already granted
            Log.i("MainActivity_debug", "startLocationUpdates")
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        Log.i("MainActivity_debug", "startLocationUpdates")
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                // Do something with the updated location
                val velocity = location.speed

                Log.i("MainActivity_debug", "$velocity")
            }

            override fun onProviderDisabled(provider: String) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        // Register the location listener to receive periodic updates
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
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
                Log.i("MainActivity_debug", "ACCESS DENIED")
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

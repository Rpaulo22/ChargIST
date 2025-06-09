package pt.ist.cmu.chargist.viewmodel

import pt.ist.cmu.chargist.BuildConfig
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import pt.ist.cmu.chargist.model.data.Charger
import java.util.Locale

class MapViewModel: ViewModel() {

    // State to hold the user's location as LatLng (latitude and longitude)
    private val _userLocation = mutableStateOf<LatLng?>(null)
    val userLocation: State<LatLng?> = _userLocation

    var currentAddress by mutableStateOf("")

    // Based on current location, uses geocoder api to get the address
    fun fetchAddress(context: Context) {
        val location = userLocation.value
        if (location != null) {
            viewModelScope.launch {
                currentAddress = getAddress(context, location)
            }
        }
    }

    suspend fun getAddress(context: Context, location: LatLng): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                listOfNotNull(
                    address.featureName,
                    address.thoroughfare,
                    address.subLocality,
                    address.locality,
                    address.subAdminArea,
                    address.adminArea,
                    address.countryName
                ).joinToString(", ")
            } else {
                "Unknown address"
            }
        } catch (e: Exception) {
            Log.e("Geocoder", "Error: ${e.message}")
            "Error retrieving address"
        }
    }



    // Function to fetch the user's location and update the state
    fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        // Check if the location permission is granted
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                // Fetch the last known location
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        // Update the user's location in the state
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        _userLocation.value = userLatLng
                    }
                }
            } catch (e: SecurityException) {
                Log.d("MapViewModel", "Permission for location access was revoked: ${e.localizedMessage}")
            }
        } else {
            Log.d("MapViewModel", "Location permission is not granted.")
        }
    }

    // Function which determines if charger is expanded
    fun closeTo(charger: Charger): Boolean {
        val results = FloatArray(1)

        if (userLocation.value == null) {
            return false
        }

        Location.distanceBetween(
            userLocation.value!!.latitude, userLocation.value!!.longitude,
            charger.latitude, charger.longitude, results
        )

        return results[0] <= 50
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context, fusedLocationClient: FusedLocationProviderClient) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                location?.let {
                    _userLocation.value = LatLng(it.latitude, it.longitude)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.getMainLooper(),
        )
    }
}
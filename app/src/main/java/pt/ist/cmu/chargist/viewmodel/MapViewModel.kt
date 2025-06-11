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
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
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
            Log.d("Address", "Location being searched: $location")
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
                ).distinct().filterNot {it.isBlank() }.joinToString(separator=", ")
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

    suspend fun getNearbyServices(context: Context, location: LatLng): List<List<String>>? {
        return withContext(Dispatchers.IO) {
            val results = mutableListOf<List<String>>()
            val ignoredTypes = listOf("Locality", "General Contractor", "Insurance Agency",
                "Jewelry Store", "Finance", "Real Estate Agency", "Accounting",
                "Furniture Store", "School", "Neighborhood", "Secondary School", "Parking",
                "Sublocality Level 1", "Point Of Interest", "Moving Company", "Transit Station",
                "Local Government Office")

            val client = OkHttpClient()

            val apiKey = BuildConfig.MAPS_API_KEY

            val url = HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegments("maps/api/place/nearbysearch/json")
                .addQueryParameter("location", "${location.latitude},${location.longitude}")
                .addQueryParameter("radius", "800") // searches in a radius of 800 meters
                .addQueryParameter("key", apiKey)
                .build()

            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string()
                val jsonObject = JSONObject(json)
                val placesArray = jsonObject.getJSONArray("results")

                var typeMap = mutableMapOf<String, Int>()

                for (i in 0 until placesArray.length()) {
                    val placeObj = placesArray.getJSONObject(i)

                    val name = placeObj.getString("name")
                    val vicinity = placeObj.optString("vicinity", "Unknown")

                    val type = placeObj.getJSONArray("types")[0]
                        .toString()
                        .replace("_"," ")
                        .split(" ")
                        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } } // format strings to look presentable

                    Log.d("Services", "Type $type")

                    if (typeMap.getOrDefault(type, 0) >= 2 || type in ignoredTypes) continue // limit services to 2 per type and ignore some types

                    typeMap[type] = typeMap.getOrDefault(type, 0) + 1

                    Log.d("Services", "Adding type $type")

                    val locationObj = placeObj.getJSONObject("geometry").getJSONObject("location")
                    val lat = locationObj.getDouble("lat")
                    val lng = locationObj.getDouble("lng")

                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        location.latitude, location.longitude,
                        lat, lng, distance
                    )

                    val distanceString = String.format(Locale.getDefault(), "%.2f", distance[0]/1000)

                    results.add(listOf(name,type,vicinity,distanceString))
                }
            } else {
                null
            }
            results
        }
    }
}
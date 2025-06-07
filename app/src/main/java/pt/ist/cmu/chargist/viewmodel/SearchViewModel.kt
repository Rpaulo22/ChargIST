package pt.ist.cmu.chargist.viewmodel

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import pt.ist.cmu.chargist.BuildConfig
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.model.repository.ChargerRepository
import pt.ist.cmu.chargist.model.repository.ChargingSlotRepository

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    val locationSearchResults = MutableStateFlow(listOf<Address>())
    private val chargerRepository: ChargerRepository
    private val slotRepository: ChargingSlotRepository
    val allChargers: StateFlow<List<Charger>>
    val allChargingSlots: StateFlow<List<ChargingSlot>>

    init {
        val chargerDao = AppDatabase.getDatabase(application).chargerDao()
        val chargingSlotDao = AppDatabase.getDatabase(application).chargingSlotDao()
        chargerRepository = ChargerRepository(chargerDao)
        slotRepository = ChargingSlotRepository(chargingSlotDao)
        allChargers = chargerRepository.allChargers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allChargingSlots = slotRepository.allChargingSlots.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    // get list of locations based on text input
    fun searchLocation(context: Context, query: String) {
        val geocoder = Geocoder(context)
        val maxResults = 5

        val listener = object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                onSearchLocation(addresses)
            }
            override fun onError(errorMessage: String?) {
                onSearchLocationError(errorMessage)
            }
        }

        geocoder.getFromLocationName(query, maxResults, listener)
    }

    fun onSearchLocationError (errorMessage: String?) {
        Log.e("Search Location", errorMessage?:"Unknown Error")
    }

    fun onSearchLocation (addresses: MutableList<Address>) {
        Log.d("Search Location", "Got " + addresses.size + " results:")
        locationSearchResults.value = addresses.filter { it.hasLatitude() && it.hasLongitude() }.distinctBy { formatAddress(it) }
        locationSearchResults.value.forEach { address -> Log.d("Search Location", "    "+address.toString()) }
    }

    fun formatAddress(address: Address):String {
        val addressParts = listOf(
            address.featureName,
            address.thoroughfare,
            address.subLocality,
            address.locality,
            address.subAdminArea,
            address.adminArea,
            address.countryName
        )
        return addressParts.distinct().filterNot {it.isNullOrBlank() }.joinToString(separator=", ")
    }

    fun searchChargers (
        location: LatLng,
        sortBy: String,
        filterSpeed: Int,
        minDistance: Double,
        maxDistance: Double,
        minPrice: Double,
        maxPrice: Double,
        minTravelTime: Double,
        maxTravelTime: Double,
        requireMbWay: Boolean,
        requireCreditCard: Boolean,
        requireCash: Boolean
    ) {

        // Atualizar carregadores em torno da localização
        // TODO: try to update local DB from firebase, query local DB regardless

        // Filtrar conteúdo da BD local


    }

    private fun calcDistance(loc1: LatLng, loc2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude, results
        )

        return results[0]/1000 // result in km
    }

    val travelTimes = mapOf<LatLng, Pair<LatLng, Float>>() // marker location -> <user location, travel time>

    private fun getTravelTime(origin: LatLng, destinations: List<LatLng>): Float {
        return 0f
    }

    suspend fun calcTravelTimes(
        origin: LatLng,
        destinations: List<LatLng>
    ): List<Int?> = withContext(Dispatchers.IO) {
        // launch a coroutine to execute http request (http requests can't be executed on main thread)
        val client = OkHttpClient()
        val apiKey = BuildConfig.MAPS_API_KEY
        val destParam = destinations.joinToString("|") {
            "${it.latitude},${it.longitude}"
        }

        val url = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                "origins=${origin.latitude},${origin.longitude}&" +
                "destinations=${destParam}&" +
                "mode=driving&" +
                "key=$apiKey"

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val json = JSONObject(response.body?.string() ?: "")
                val rows = json.getJSONArray("rows")
                if (rows.length() > 0) {
                    val elements = rows.getJSONObject(0).getJSONArray("elements")
                    return@withContext List(elements.length()) { i ->
                        val element = elements.getJSONObject(i)
                        if (element.getString("status") == "OK") {
                            Log.d("Search Charger", "Response $i OK")
                            element.getJSONObject("duration").getInt("value") / 60 // in minutes
                        } else null
                    }
                }
                Log.d("Search Charger", "json: $json")
            }
            Log.d("Search Charger", "Response: $response")
            List(destinations.size) { null }
        } catch (e: Exception) {
            Log.e("Network", "Error: ${e.message}")
            List(destinations.size) { null }
        }
    }

    fun ttWrapper(location: LatLng) {
        viewModelScope.launch {
            val a = calcTravelTimes(location, listOf(LatLng(38.73623592881426, -9.160385500848275),LatLng(38.85897702376556, -9.141703572111687),LatLng(39.08149326408465, -9.255457320651415)))
            Log.d("Search Charger", "Travel Times = $a")
        }
    }
}
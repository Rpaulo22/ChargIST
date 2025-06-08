package pt.ist.cmu.chargist.viewmodel

import android.app.Application
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import pt.ist.cmu.chargist.BuildConfig
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargerDao
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.model.repository.ChargerRepository
import pt.ist.cmu.chargist.model.repository.ChargingSlotRepository

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    val locationSearchResults = MutableStateFlow(listOf<Address>())
    private val chargerRepository: ChargerRepository
    private val slotRepository: ChargingSlotRepository
    val allChargers: StateFlow<List<Charger>>

    val travelTimes = mutableMapOf<LatLng, Pair<LatLng, Float>>() // marker location -> <user location, travel time>

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
        onSearch: (List<Charger>) -> Unit,
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
        viewModelScope.launch {
            // Não faço ideia porquê, mas isto faz o primeiro collect com o estado inicial se a lista estiver vazia faz-se duas vezes e está resolvido, se de facto estiver vazio, também não se perde nada
            val n = if (allChargers.value.isEmpty()) 2 else 1
            allChargers.take(n).collect { chargers ->
                // Atualizar carregadores em torno da localização
                // TODO: try to update local DB from firebase, query local DB regardless

                // BD local

                Log.d("Search Charger", "AllChargers=" + chargers)

                // Chamadas à API para obter tempo de viagem
                val pointsToCalc = mutableListOf<LatLng>()
                for (charger in chargers) {
                    val loc = LatLng(charger.latitude, charger.longitude)
                    if (
                        travelTimes.get(loc) == null || // not calculated
                        calcDistance(
                            travelTimes.get(loc)!!.first,
                            location
                        ) > 2  // old calculation (2km away from calculated point)
                    ) {
                        pointsToCalc.add(loc)

                        if (pointsToCalc.size == 25) { // API call supports only 25 locations, needs to be batched
                            calcTravelTimes(location, pointsToCalc)
                            pointsToCalc.clear()
                        }
                    }
                }

                if (pointsToCalc.isNotEmpty()) {
                    calcTravelTimes(location, pointsToCalc)
                }

                Log.d("Search Charger", "TravelTimes=" + travelTimes.toString())

                // Filtrar conteúdo da BD local
                val filteredChargers = chargers
                    // Payment method
                    .filter { c ->
                        !(requireMbWay && !c.mbWay) // require => has
                    }.filter { c ->
                        !(requireCreditCard && !c.creditCard) // require => has
                    }.filter { c ->
                        !(requireCash && !c.cash) // require => has
                    }
                    // Price
                    .filter { c ->
                        val p = priceForSpeed(c, filterSpeed)
                        minPrice <= p && p <= maxPrice
                    }
                    // Distance
                    .filter { c ->
                        val d = calcDistance(LatLng(c.latitude, c.longitude), location)
                        minDistance <= d && d <= maxDistance
                    }
                    // Travel Time
                    .filter { c ->
                        val t = getTravelTime(location, LatLng(c.latitude, c.longitude))
                        minTravelTime <= t && t <= maxTravelTime
                    }
                    // Charging Speed
                    .filter { c ->
                        hasChargingSpeed(c, filterSpeed)
                    }

                Log.d("Search Charger", "FilteredChargers=" + filteredChargers.toString())

                // Ordenar a lista
                val sortedChargers = when (sortBy) {
                    "Distance" -> filteredChargers.sortedBy { c ->
                        calcDistance(
                            LatLng(
                                c.latitude,
                                c.longitude
                            ), location
                        )
                    }

                    "Price" -> filteredChargers.sortedBy { c -> priceForSpeed(c, filterSpeed) }
                    "Travel Time" -> filteredChargers.sortedBy { c ->
                        getTravelTime(
                            location,
                            LatLng(c.latitude, c.longitude)
                        )
                    }
                    //"Availability" -> 0 // TODO: quando/se AVAILIBILITY?
                    else -> filteredChargers
                }

                onSearch(sortedChargers)
            }
        }
    }

    private fun getSlotSpeed(slot: ChargingSlot): Int {
        return when (slot.speed) {
            "Slow" -> 0
            "Medium" -> 1
            "Fast" -> 2
            else -> -1
        }
    }

    private suspend fun hasChargingSpeed(c: Charger, speed: Int): Boolean {
        Log.d("Search Charger", "Charging speed $speed check for charger $c")
        val slotsFlow = slotRepository.getSlots(c.chargingSlots)
        var result = false
        slotsFlow.take(1).collect { slots ->
            Log.d("Search Charger", "Charging speed check for slots $slots")
            for (slot in slots) {
                if (getSlotSpeed(slot) >= speed) {
                    result = true
                    break
                }
            }
        }
        return result
    }

    private fun priceForSpeed(c: Charger, s: Int): Double { // TODO: isto devia estar no âmbito da class Charger, mas paciência, provavelmente não vai lá parar
        when (s) {
            0 -> return c.priceSlow
            1 -> return c.priceMedium
            2 -> return c.priceFast
        }
        return Double.MAX_VALUE
    }

    private fun calcDistance(loc1: LatLng, loc2: LatLng): Float {
        val results = FloatArray(1)
        Location.distanceBetween(
            loc1.latitude, loc1.longitude,
            loc2.latitude, loc2.longitude, results
        )

        return results[0]/1000 // result in km
    }

    private fun getTravelTime(origin: LatLng, destination: LatLng): Float {
        if (
            travelTimes.get(destination) == null || // not calculated
            calcDistance(travelTimes.get(destination)!!.first, origin) > 2  // old calculation (2km away from calculated point)
        ) {
            return Float.MAX_VALUE
        }
        return travelTimes.get(destination)!!.second
    }

    suspend fun calcTravelTimes(origin: LatLng, destinations: List<LatLng>) {
        val times = travelTimeApiCall(origin, destinations)
        for (i in 0..times.size-1) {
            val t = times[i]
            val d = destinations[i]
            if (t != null) {
                travelTimes.put(d, Pair(origin, t))
            }
        }
    }

    suspend fun travelTimeApiCall(
        origin: LatLng,
        destinations: List<LatLng>
    ): List<Float?> = withContext(Dispatchers.IO) {
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
                            element.getJSONObject("duration").getInt("value") / 60f // in minutes
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
}
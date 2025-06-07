package pt.ist.cmu.chargist.viewmodel

import android.content.Context
import android.location.Address
import androidx.lifecycle.ViewModel
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.util.Logger.debug
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchViewModel() : ViewModel() {
    val locationSearchResults = MutableStateFlow(listOf<Address>())

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
}
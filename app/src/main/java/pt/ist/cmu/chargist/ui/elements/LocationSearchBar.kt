package pt.ist.cmu.chargist.ui.elements

import android.R.attr.text
import android.location.Address
import android.util.Log
import android.view.Surface
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationDisabled
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor
import pt.ist.cmu.chargist.viewmodel.MapViewModel
import pt.ist.cmu.chargist.viewmodel.SearchViewModel
import kotlin.math.exp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchBar (
    onLocationUpdate: (LatLng?) -> Unit,
    searchViewModel: SearchViewModel,
    mapViewModel: MapViewModel
) {
    var textFieldState = remember { TextFieldState() }
    var expanded by rememberSaveable { mutableStateOf(false) }
    val locationResults by searchViewModel.locationSearchResults.collectAsState()
    val context = LocalContext.current

    var usingMyLocation by remember { mutableStateOf(true)}
    val myLocation by mapViewModel.userLocation
    if (usingMyLocation) {
        Log.d("Search Location", "Initial location update with current location")
        onLocationUpdate(myLocation) // ensure initial value is passed
    }

    val onSearch = { address: Address? ->
        Log.d("Search Location", "Location update from search of address $address")
        address?.let {
            val text = address.getAddressLine(0)?:address.toString()
            textFieldState.edit { replace(0, length, text) }
            onLocationUpdate(LatLng(address.latitude, address.longitude))
            usingMyLocation =  false
        }
    }
    val setUseSearchLocation = {
        if (locationResults.isNotEmpty()) {
            Log.d("Search Location", "Location update from setting use search location for address ${locationResults[0]}")
            onSearch(locationResults[0])
            expanded = false
        } else
            Toast.makeText(context, "Enter location to  use for searching chargers", Toast.LENGTH_LONG).show()
    }
    val setUseMyLocation = {
        Log.d("Search Location", "Location update with current location")
        onLocationUpdate(myLocation)
        expanded = false
        usingMyLocation = true
    }
    val askForLocation = {
        Toast.makeText(context, "Turn on location", Toast.LENGTH_SHORT).show()
        Log.d("Location Bar", "My Location: $myLocation")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SearchBar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = {
                        textFieldState.edit { replace(0, length, it) }
                        searchViewModel.searchLocation(context, textFieldState.text.toString())
                        Log.d(
                            "Search Location",
                            "Updated query to: «" + textFieldState.text.toString() + "»"
                        )
                    },
                    onSearch = {
                        onSearch(if (locationResults.isNotEmpty()) locationResults[0] else null)
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Search remote location") },
                    trailingIcon = {
                        if (myLocation == null) { // No location
                            Icon(
                                Icons.Default.LocationDisabled,
                                "Use current location",
                                Modifier.clickable { askForLocation() }
                            )
                        } else if (usingMyLocation) { // Using my location
                            Icon(
                                Icons.Default.MyLocation,
                                "Use current location",
                                Modifier.clickable { setUseSearchLocation() },
                                tint = mainColor,
                            )
                        } else { // Using search location
                            Icon(
                                Icons.Default.LocationSearching,
                                "Use current location",
                                Modifier.clickable { setUseMyLocation() }
                            )
                        }
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            Column(
                Modifier.verticalScroll(rememberScrollState())
            ) {
                if (locationResults.isEmpty()) {
                    if (!textFieldState.text.toString().isEmpty())
                        Text(
                            "No location results for '${textFieldState.text}'",
                            modifier = Modifier.padding(8.dp)
                        )
                } else {
                    locationResults.forEach { address ->
                        LocationResultItem(
                            searchViewModel,
                            address,
                            {
                                onSearch(it)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun LocationResultItem(
    searchViewModel: SearchViewModel,
    address: Address,
    onSelect: (Address?) -> Unit?
) {
    val formattedAddress = searchViewModel.formatAddress(address)

    Row (
        Modifier.padding(8.dp)
            .fillMaxWidth()
            .clickable { onSelect(address) }
    ) {
        Text(formattedAddress)
    }
}

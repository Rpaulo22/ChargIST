package pt.ist.cmu.chargist.ui.screens

import android.Manifest
import android.R.attr.navigationIcon
import android.R.attr.text
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheetDefaults.properties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargingSpot
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import pt.ist.cmu.chargist.viewmodel.MapViewModel
import kotlin.reflect.typeOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.google.android.gms.maps.internal.ILocationSourceDelegate
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import pt.ist.cmu.chargist.MainActivity
import java.security.AccessController.getContext

@Composable
fun HomeScreen(
    userId: String,
    onLogoutClick: () -> Unit,
    onAccountClick: (String) -> Unit,
    appViewModel: AppViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {

    val chargers by appViewModel.allChargers.collectAsState()
    val spots by appViewModel.allChargingSpots.collectAsState()

    val context = LocalContext.current

    val userLocation by mapViewModel.userLocation
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions() ,
        onResult = { permissions ->
            if( permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                &&
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
                // Ok can access location
                Toast.makeText(context, "Has Location Permission", Toast.LENGTH_SHORT).show()
                mapViewModel.fetchUserLocation(context, fusedLocationClient)

            } else {
                // ask for permission
                context as MainActivity
                val rationaleRequired =
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION )
                            ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION )
            }
        })

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold (
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        ) },
                        modifier = Modifier.size(96.dp)
                    ) {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                            Text(text = "Home")
                        }
                    }
                    IconButton(
                        onClick = { onAccountClick(userId) },
                        modifier = Modifier.size(96.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                            Text(text = "Account")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Map(paddingValues, context, appViewModel, mapViewModel, userLocation)

        /*LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            for (charger in chargers) {
                item {
                    Text(text = "charger: $charger")
                }
            }

            for (spot in spots) {
                item {
                    Text(text = "spot: $spot")
                }
            }
        }*/
    }
}

@Composable
fun Map(
    paddingValues: PaddingValues,
    context : Context,
    appViewModel: AppViewModel,
    mapViewModel: MapViewModel,
    userLocation: LatLng?
) {
    val chargers by appViewModel.allChargers.collectAsState()
    val istCoords = LatLng(38.736766738322125, -9.139350512479778)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(istCoords, 17f)
    }

    GoogleMap(
        contentPadding = paddingValues,
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
    ) {
        // If the user's location is available, place a marker on the map
        userLocation?.let {
            Marker(
                state = MarkerState(position = it), // Place the marker at the user's location
                title = "Your Location", // Set the title for the marker
                snippet = "This is where you are currently located." // Set the snippet for the marker
            )
            // Move the camera to the user's location with a zoom level of 10f
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }

        for (charger in chargers) {
            Marker(
                state = remember { MarkerState(position=LatLng(charger.latitude,charger.longitude)) },
                title = charger.name
            )
        }
    }
}

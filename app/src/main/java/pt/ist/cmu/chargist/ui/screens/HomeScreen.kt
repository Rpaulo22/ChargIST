package pt.ist.cmu.chargist.ui.screens

import android.Manifest
import android.R.attr.navigationIcon
import android.R.attr.rating
import android.R.attr.text
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalBottomSheetDefaults.properties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTarget
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import pt.ist.cmu.chargist.viewmodel.MapViewModel
import kotlin.reflect.typeOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.google.android.gms.maps.internal.ILocationSourceDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import pt.ist.cmu.chargist.MainActivity
import pt.ist.cmu.chargist.R
import pt.ist.cmu.chargist.ui.elements.BottomNavigationBar
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor
import java.security.AccessController.getContext

@Composable
fun HomeScreen(
    onAccountClick: () -> Unit,
    onCreateCharger: () -> Unit,
    onSearchClick: () -> Unit,
    appViewModel: AppViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    centerPoint: LatLng? = null
) {


    val chargers by appViewModel.allChargers.collectAsState()
    val slots by appViewModel.allChargingSlots.collectAsState()

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
                mapViewModel.fetchUserLocation(context, fusedLocationClient)
                mapViewModel.startLocationUpdates(context, fusedLocationClient)

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
        }
    )

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
            BottomNavigationBar(
                onAccountClick = onAccountClick,
                onSearchClick = onSearchClick,
                currentScreen = "Map"
                )
        }
    ) { paddingValues ->
        Map(paddingValues, chargers, slots, userLocation, onCreateCharger, mapViewModel, appViewModel, centerPoint)
    }
}

@Composable
fun Map(
    paddingValues: PaddingValues,
    chargers: List<Charger>,
    slots: List<ChargingSlot>,
    userLocation: LatLng?,
    onCreateCharger: () -> Unit,
    mapViewModel: MapViewModel,
    appViewModel: AppViewModel,
    centerPoint: LatLng?
) {
    var showChargerInformationPanel by remember { mutableStateOf(false) }

    var selectedCharger by remember { mutableStateOf<Charger?>(null) } // charger whose information panel is showing

    Box(modifier = Modifier.fillMaxSize()) {
        var mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = false)) }
        val colorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM

        val istCoords = LatLng(38.736766738322125, -9.139350512479778)
        var hasMovedCamera by remember { mutableStateOf(false) }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(centerPoint?:istCoords, 15f) // center camera on passed center point/ IST
        }

        // If the app has location access, move the camera to user location
        LaunchedEffect(userLocation) {
            if (!hasMovedCamera && userLocation != null) {
                if (centerPoint == null) { // only go to location if no center point provided
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(userLocation, 15f)
                    )
                }
                mapProperties = mapProperties.copy(isMyLocationEnabled = true)
                hasMovedCamera =
                    true // only do this once so that the camera is not constantly following user
            }
        }

        GoogleMap(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            mapColorScheme = colorScheme
        ) {

            for (charger in chargers) {
                Log.d("Chargers","$charger")
                SimpleMapMarker(
                    charger = charger,
                    onClick = {
                        showChargerInformationPanel = true
                        selectedCharger = charger
                    },
                    mapViewModel = mapViewModel
                )
            }
        }

        IconButton(
            onClick = onCreateCharger,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(paddingValues)
                .padding(start = 6.dp, bottom = 32.dp)
                .background(MaterialTheme.colorScheme.background, shape = CircleShape)
                .border(2.dp, mainColor, CircleShape)
                .size(56.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add New Charger",
                modifier = Modifier.size(40.dp),
                tint = mainColor
            )

        }
    }
    if (showChargerInformationPanel) {
        ChargerInformationPanel(
            onDismiss = { showChargerInformationPanel = false },
            onConfirm = { charger: Charger ->
                appViewModel.updateCharger(charger)},
            charger = selectedCharger,
            mapViewModel = mapViewModel,
            appViewModel = appViewModel
        )
    }
}

@Composable
fun SimpleMapMarker(
    charger: Charger,
    onClick: () -> Unit,
    favourites: List<String> = listOf<String>("Fczz0Yq4WAk8sF4hqq2K"),
    mapViewModel: MapViewModel
) {
    val markerState = remember { MarkerState(position = LatLng(charger.latitude, charger.longitude)) }

    var expandMarker by remember { mutableStateOf(false) } // status that stores whether the marker is expanded

    val favourite = (charger.id in favourites)

    val imgUrl: String? = null // todo actually sacate the image from firebase

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(imgUrl)
            .allowHardware(false)
            .build()
    )

    if (mapViewModel.closeTo(charger)) {
        expandMarker = true
    }

    MarkerComposable(
        keys = arrayOf(charger.name, painter.state, expandMarker),
        state = markerState,
        title = charger.name,
        anchor = Offset(0.5f, 1f),
        onClick = {
            if (expandMarker) onClick()
            expandMarker = !expandMarker

            Log.d(
                "Marker Click",
                "Clicked in marker ${charger.name} and expandMarker has value $expandMarker"
            )
            true
        }
    ) {
        Box(
            modifier = Modifier
                .size(if (expandMarker) 120.dp else 48.dp)
                .clip(if (expandMarker) RoundedCornerShape(12.dp) else CircleShape)
                .background(Color.Black)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!expandMarker) {
                Image(
                    painter = painterResource(
                        id = if (favourite) R.drawable.chargist_without_text_favourite else R.drawable.chargist_without_text
                    ),
                    contentDescription = "ChargIST Logo",
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!imgUrl.isNullOrEmpty()) {
                        Image(
                            painter = painter,
                            contentDescription = "Charger Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Image(
                            painter = painterResource(
                                id = if (favourite) R.drawable.chargist_without_text_favourite else R.drawable.chargist_without_text
                            ),
                            contentDescription = "ChargIST Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp)),
                        )
                    }
                    Text(
                        text = charger.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        color = Color.White
                    )
                    PaymentMethods(
                        mbWay = charger.mbWay,
                        creditCard = charger.creditCard,
                        cash = charger.cash,
                        size = 20
                    )
                }
            }
        }
    }
}

@Composable
fun ChargerInformationPanel(
    charger: Charger?,
    onDismiss: () -> Unit,
    onConfirm: (Charger) -> Unit,
    mapViewModel: MapViewModel,
    appViewModel: AppViewModel,
    favourites: List<String> = listOf<String>("Fczz0Yq4WAk8sF4hqq2K")
) {
    val context = LocalContext.current

    val uid = appViewModel.uid

    if (charger == null) {
        Toast.makeText(context, "Error loading charger information \uD83D\uDE14", Toast.LENGTH_LONG).show()
        return
    }

    var chargerAddress by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) { // launch  coroutine to obtain charger address
        chargerAddress = mapViewModel.getAddress(context, LatLng(charger.latitude,charger.longitude))
    }

    var favourite by remember { mutableStateOf(charger.id in favourites)}

    val slotsFlow = appViewModel.getCorrespondingChargingSlots(charger)
    val slots by slotsFlow.collectAsState(initial = emptyList())

    var personalRating by remember { mutableStateOf(charger.ratings[uid] ?: 0.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(charger.name)
                IconButton(
                    onClick = {favourite = !favourite},
                ) {
                    if (favourite) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Add to favorite",
                            modifier = Modifier.size(30.dp),
                            tint = Color.Yellow
                        )
                    }
                    else {
                        Icon(
                            Icons.Default.StarBorder,
                            contentDescription = "Remove from favorite",
                            modifier = Modifier.size(30.dp),
                            tint = Color.Yellow
                        )
                    }
                }
            }
        },
        text = {
            Column (
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChargerImage(charger, favourite)

                // Address of charger
                Text(chargerAddress, textAlign = TextAlign.Center)

                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                // Payment Methods
                Text("Available payment methods:", textAlign = TextAlign.Center)
                Spacer(Modifier.size(6.dp))
                PaymentMethods(
                    mbWay = charger.mbWay,
                    creditCard = charger.creditCard,
                    cash = charger.cash,
                    size = 30
                )

                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                // Charging Slots
                Text("Charging slots:")
                slots.forEach {
                    ChargingSlotField(it)
                    Spacer(Modifier.size(4.dp))
                }

                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                // Prices
                Text("Slow price: ${charger.priceSlow} €/kWh")
                Spacer(Modifier.size(6.dp))
                Text("Medium price: ${charger.priceMedium} €/kWh")
                Spacer(Modifier.size(6.dp))
                Text("Fast price: ${charger.priceFast} €/kWh")

                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                // Ratings
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rating: ${if (charger.ratingsMean != 0.0) charger.ratingsMean else "None"}")
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        modifier = Modifier.size(24.dp),
                        tint = mainColor
                    )
                }
                Spacer(Modifier.size(6.dp))
                Text("Rate this charger:")
                RateCharger(rating = personalRating, onRatingChange = { newRating -> personalRating = newRating})
                Spacer(Modifier.size(6.dp))
                RatingHistogram(charger.ratings)


                // todo sitios perto, editar
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (personalRating != 0.0) appViewModel.rateCharger(charger, uid, personalRating)
                onConfirm(charger)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ChargerImage(
    charger:Charger,
    favourite: Boolean
) {
    val imgUrl: String? = null // todo actually sacate the image from firebase

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(imgUrl)
            .allowHardware(false)
            .build()
    )

    // Charger Picture
    if (!imgUrl.isNullOrEmpty()) {
        Image(
            painter = painter,
            contentDescription = "Charger Image",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Image(
            painter = painterResource(
                id = if (favourite) R.drawable.chargist_without_text_favourite else R.drawable.chargist_without_text
            ),
            contentDescription = "ChargIST Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
        )
    }
}

@Composable
fun PaymentMethods(
    mbWay: Boolean,
    creditCard: Boolean,
    cash: Boolean,
    size: Int
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (mbWay) {
            Image(
                painter = painterResource(
                    id = R.drawable.mbway
                ),
                contentDescription = "MbWay Available",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size.dp)
            )
        }
        if (creditCard) {
            Icon(
                Icons.Default.CreditCard,
                contentDescription = "Credit card available",
                modifier = Modifier.size(size.dp),
                tint = Color.White
            )
        }
        if (cash) {
            Icon(
                Icons.Default.Payments,
                contentDescription = "Cash available",
                modifier = Modifier.size(size.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun ChargingSlotField(slot: ChargingSlot) {
    Box(
        Modifier
            .background(colorScheme.background, RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Text("${slot.speed} - ${slot.type}")
    }

}

@Composable
fun RateCharger(
    rating: Double,
    onRatingChange: (Double) -> Unit
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        IconButton(
            onClick = {onRatingChange(1.0)},
        ) {
            Icon(
                imageVector = if (rating >= 1) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "1 star",
                modifier = Modifier.size(24.dp),
                tint = mainColor
            )
        }
        IconButton(
            onClick = {onRatingChange(2.0)},
        ) {
            Icon(
                imageVector = if (rating >= 2) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "2 star",
                modifier = Modifier.size(24.dp),
                tint = mainColor
            )
        }
        IconButton(
            onClick = {onRatingChange(3.0)},
        ) {
            Icon(
                imageVector = if (rating >= 3) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "3 star",
                modifier = Modifier.size(24.dp),
                tint = mainColor
            )
        }
        IconButton(
            onClick = {onRatingChange(4.0)},
        ) {
            Icon(
                imageVector = if (rating >= 4) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "4 star",
                modifier = Modifier.size(24.dp),
                tint = mainColor
            )
        }
        IconButton(
            onClick = {onRatingChange(5.0)},
        ) {
            Icon(
                imageVector = if (rating >= 5) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "5 star",
                modifier = Modifier.size(24.dp),
                tint = mainColor
            )
        }
    }
}

@Composable
fun RatingHistogram(ratings: Map<String, Double>) {
    val ratingCounts = (1..5).associateWith { rating ->
        ratings.values
            .map { it.toInt() }
            .count { it == rating }
    }

    val maxCount = ratingCounts.values.maxOrNull() ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        (5 downTo 1).forEach { rating ->
            val count = ratingCounts[rating] ?: 0.0
            val barFraction = if (maxCount > 0) count.toFloat() / maxCount else 0f

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text("$rating ★", modifier = Modifier.width(40.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barFraction * 7/8)
                        .height(24.dp)
                        .background(mainColor, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (count != 0) {
                    Text("$count", modifier = Modifier.width(40.dp))
                }
            }
        }
    }
}
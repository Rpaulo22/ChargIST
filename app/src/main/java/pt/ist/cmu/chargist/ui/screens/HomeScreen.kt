package pt.ist.cmu.chargist.ui.screens

import android.Manifest
import android.R.attr.bitmap
import android.R.attr.navigationIcon
import android.R.attr.rating
import android.R.attr.text
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.flow.flowOf
import pt.ist.cmu.chargist.connectionStatus

@Composable
fun HomeScreen(
    onAccountClick: () -> Unit,
    onCreateCharger: () -> Unit,
    onCreateChargerByHoldingOnMap: (LatLng) -> Unit,
    onEditCharger: (String) -> Unit,
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
        Map(paddingValues, chargers, slots, userLocation, onCreateCharger, onCreateChargerByHoldingOnMap, onEditCharger, mapViewModel, appViewModel, centerPoint)
    }
}

@Composable
fun Map(
    paddingValues: PaddingValues,
    chargers: List<Charger>,
    slots: List<ChargingSlot>,
    userLocation: LatLng?,
    onCreateCharger: () -> Unit,
    onCreateChargerByHoldingOnMap: (LatLng) -> Unit,
    onEditCharger: (String) -> Unit,
    mapViewModel: MapViewModel,
    appViewModel: AppViewModel,
    centerPoint: LatLng?
) {
    val context = LocalContext.current
    val favoriteChargers by appViewModel.favoriteChargers.collectAsState()

    var showChargerInformationPanel by remember { mutableStateOf(false) }

    var showMapLongClickDialog by remember { mutableStateOf(false) }
    var mapLongClickLatLng by remember {mutableStateOf<LatLng?>(null)}

    var connected = connectionStatus()

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
            mapColorScheme = colorScheme,
            onMapLongClick = { latLng ->
                showMapLongClickDialog = true
                mapLongClickLatLng = latLng
            }
        ) {

            for (charger in chargers) {
                Log.d("Chargers", "$charger")
                key(charger.id, favoriteChargers.contains(charger.id)) {
                    SimpleMapMarker(
                        charger = charger,
                        onClick = {
                            showChargerInformationPanel = true
                            selectedCharger = charger
                        },
                        mapViewModel = mapViewModel,
                        favoriteChargers = favoriteChargers,
                    )
                }
            }
        }

        IconButton(
            onClick = {
                if (connected) onCreateCharger()
                else Toast.makeText(context, "Please connect to the internet to create new chargers", Toast.LENGTH_SHORT).show()
            },

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
            onEditCharger = onEditCharger,
            chargerId = selectedCharger?.id ?: "",
            mapViewModel = mapViewModel,
            appViewModel = appViewModel,
            favoriteChargers = favoriteChargers
        )
    }
    if (showMapLongClickDialog) {
        MapLongClickDialog(
            clickedLatLng = mapLongClickLatLng!!,
            onCreateChargerByHoldingOnMap = onCreateChargerByHoldingOnMap,
            onDismiss = { showMapLongClickDialog = false },
        )
    }
}

@Composable
fun SimpleMapMarker(
    charger: Charger,
    onClick: () -> Unit,
    mapViewModel: MapViewModel,
    favoriteChargers: List<String>?,
) {

    val markerState = remember { MarkerState(position = LatLng(charger.latitude, charger.longitude)) }

    var expandMarker by remember { mutableStateOf(false) } // status that stores whether the marker is expanded

    val favourite = (favoriteChargers?.contains(charger.id) == true)

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
    chargerId: String,
    onDismiss: () -> Unit,
    onEditCharger: (String) -> Unit,
    mapViewModel: MapViewModel,
    appViewModel: AppViewModel,
    favoriteChargers: List<String>
) {
    val context = LocalContext.current

    val uid = appViewModel.uid

    val charger by appViewModel.getChargerFlowById(chargerId).collectAsState(initial = null)

    if (charger == null) {
        return
    }

    var chargerAddress by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) { // launch coroutine to obtain charger address
        chargerAddress = mapViewModel.getAddress(context, LatLng(charger!!.latitude,charger!!.longitude))
    }

    var favourite by remember { mutableStateOf(favoriteChargers.contains(charger!!.id) == true )}
    var favouriteChanged by remember { mutableStateOf(false) }

    val slotsFlow = charger?.let { appViewModel.getCorrespondingChargingSlots(it) } ?: flowOf(emptyList())
    val slots by slotsFlow.collectAsState(initial = emptyList())

    LaunchedEffect(slotsFlow) {
        slotsFlow.collect { updatedSlots ->
            Log.d("SlotsDebug", "Updated slots: ${updatedSlots.size}")
        }
    }

    var personalRating by remember { mutableStateOf(charger!!.ratings[uid] ?: 0.0) }
    var personalRatingChanged by remember { mutableStateOf(false) }

    var slotDialog by remember {mutableStateOf(false)}
    var selectedSlot by remember { mutableIntStateOf(0) }

    var titleScrollState = rememberScrollState()

    LaunchedEffect (Unit) {
        while(true) {
            appViewModel.reloadCharger(charger!!.id)
            delay(3000)
        }
    }

    AlertDialog(
        modifier = Modifier
            .padding(vertical = 24.dp),
        onDismissRequest = onDismiss,
        title = {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = charger!!.name,
                    modifier = Modifier
                        .horizontalScroll(titleScrollState)
                        .weight(1f),
                    maxLines = 1
                )
                IconButton(
                    onClick = {
                        favourite = !favourite
                        favouriteChanged = true
                    },
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
                ChargerImage(appViewModel, charger!!, favourite)

                Spacer(Modifier.size(10.dp))

                // Address of charger
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                ){
                    Text(
                        text = chargerAddress,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.size(6.dp))

                    // Directions and Share Button
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                val gmmIntentUri =
                                    "https://www.google.com/maps/dir/?api=1&destination=${charger!!.latitude},${charger!!.longitude}&travelmode=driving".toUri()
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Google Maps app not found.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonColors(
                                Color.Transparent,
                                mainColor,
                                Color.Transparent,
                                Color.Gray
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Directions,
                                    contentDescription = "Directions Button",
                                    modifier = Modifier.size(34.dp)
                                )
                                Text("Directions")
                            }
                        }
                        Button(
                            onClick = {
                                val mapsUrl =
                                    "https://www.google.com/maps/search/?api=1&query=${charger!!.latitude},${charger!!.longitude}"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Using ChargIST, I just found this charger!\nCheck out \"${charger!!.name}\" at:\n $mapsUrl"
                                    )
                                    type = "text/plain"
                                }

                                val shareIntent = Intent.createChooser(sendIntent, "Share via")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonColors(
                                Color.Transparent,
                                mainColor,
                                Color.Transparent,
                                Color.Gray
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share Button",
                                    modifier = Modifier.size(34.dp)
                                )
                                Text("Share")
                            }
                        }
                    }
                }


                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                // Payment Methods
                Text("Available payment methods:", textAlign = TextAlign.Center)
                Spacer(Modifier.size(6.dp))
                PaymentMethods(
                    mbWay = charger!!.mbWay,
                    creditCard = charger!!.creditCard,
                    cash = charger!!.cash,
                    size = 30
                )

                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                // Charging Slots
                if (slots.isEmpty()) {
                    Text("There are no registered Charging Slots here!", textAlign = TextAlign.Center)
                }
                else {
                    Text("Charging slots:")
                    slots.forEach { slot ->
                        ChargingSlotField(
                            slot = slot,
                            onClick = {
                                selectedSlot = slots.indexOf(slot)
                                slotDialog = true
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                // Prices
                Text("Slow price: ${charger!!.priceSlow} €/kWh")
                Spacer(Modifier.size(6.dp))
                Text("Medium price: ${charger!!.priceMedium} €/kWh")
                Spacer(Modifier.size(6.dp))
                Text("Fast price: ${charger!!.priceFast} €/kWh")

                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                // Ratings
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rating: ${if (charger!!.ratingsMean != 0.0) charger!!.ratingsMean else "None"}")
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        modifier = Modifier.size(24.dp),
                        tint = mainColor
                    )
                }
                Spacer(Modifier.size(6.dp))
                Text("Rate this charger:")
                RateCharger(
                    rating = personalRating,
                    onRatingChange = {
                        newRating -> personalRating = newRating
                        personalRatingChanged = true
                    }
                )
                Spacer(Modifier.size(6.dp))
                RatingHistogram(charger!!.ratings)

                HorizontalDivider(Modifier.padding(14.dp), thickness = 1.dp)

                RelevantNearbyServices(
                    context,
                    mapViewModel,
                    charger!!.latitude,
                    charger!!.longitude
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (favouriteChanged) {
                        if (favourite) {
                            appViewModel.favoriteCharger(charger!!)
                        }
                        else {
                            appViewModel.unfavoriteCharger(charger!!)
                        }
                    }
                    if (personalRatingChanged) appViewModel.rateCharger(charger!!, personalRating)
                    onDismiss()
                }) {
                Text("Back")
            }
        },
        dismissButton = {
            if (uid == charger!!.ownerId) {
                TextButton(
                    onClick = {
                        onEditCharger(charger!!.id)
                        onDismiss()
                    }) {
                    Text("Edit")
                }
            }
        }
    )
    if (slotDialog) {
        ChargingSlotDialog(
            slot = slots[selectedSlot],
            number = selectedSlot,
            chargerName = charger!!.name,
            onDismiss = {slotDialog = false},
            appViewModel = appViewModel
        )
    }
}

@Composable
fun ChargerImage(
    appViewModel: AppViewModel,
    charger:Charger,
    favourite: Boolean
) {
    var isLoading by remember { mutableStateOf(true) }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        val photoBytes = appViewModel.downloadChargerPhoto(charger.id)
        isLoading = false
        if (photoBytes != null) {
            bitmap = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.size)
        }
    }

    // Charger Picture
    if (isLoading) {
        Text(
            text="Loading Image...",
            textAlign = TextAlign.Center,
            color = mainColor,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxSize(),
        )
    }
    else if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
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
fun ChargingSlotField(
    slot: ChargingSlot,
    onClick: () -> Unit
) {
    val currentTimestamp = rememberUpdatedState(Instant.now().toEpochMilli())

    val occupied = currentTimestamp.value <= slot.occupiedUntil
    val color = if (occupied) Color(199, 45, 45, 255) else mainColor

    Box(
        Modifier
            .background(colorScheme.background, RoundedCornerShape(8.dp))
            .padding(6.dp)
            .clickable(onClick = onClick)
    ) {
        Text("${slot.speed} - ${slot.type}", color = color)
    }
}

@Composable
fun ChargingSlotDialog(
    slot: ChargingSlot,
    number: Int,
    chargerName: String,
    onDismiss: () -> Unit,
    appViewModel: AppViewModel) {

    val context = LocalContext.current

    val speed = when (slot.speed) {
        "Fast" -> 3
        "Medium" -> 2
        "Slow" -> 1
        else -> 0
    }

    var currentTimestamp by remember { mutableStateOf(Instant.now().toEpochMilli()) }

    var occupied by remember { mutableStateOf(currentTimestamp <= slot.occupiedUntil)}

    LaunchedEffect(slot) {
        currentTimestamp = Instant.now().toEpochMilli()
        occupied = currentTimestamp <= slot.occupiedUntil
    }

    AlertDialog(
        title = {
            Text("Charging slot #${number+1} of $chargerName")
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Speed", fontWeight = FontWeight.Bold, fontSize = 30.sp)
                Spacer(Modifier.size(10.dp))
                Row {
                    Text("(${slot.speed.first()})")
                    Spacer(Modifier.size(4.dp))
                    for (i in 0 until 3) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = "Speed bolt",
                            modifier = Modifier.size(25.dp),
                            tint = if (i < speed) mainColor else mainColor.copy(alpha = 0.3f)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)

                Text("Type", fontWeight = FontWeight.Bold, fontSize = 30.sp)
                Spacer(Modifier.size(10.dp))
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(slot.type)
                    Spacer(Modifier.size(6.dp))
                    if (slot.type == "CCS2")
                        Image(
                            painter = painterResource(
                                id = R.drawable.ccs2
                            ),
                            contentDescription = "CCS2 charger",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(38.dp),
                            colorFilter = ColorFilter.tint(mainColor, blendMode = BlendMode.SrcAtop)
                        )
                    else
                        Image(
                            painter = painterResource(
                                id = R.drawable.type_2
                            ),
                            contentDescription = "Type 2 charger",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(30.dp),
                            colorFilter = ColorFilter.tint(mainColor, blendMode = BlendMode.SrcAtop)
                        )
                }

                HorizontalDivider(Modifier.padding(10.dp), thickness = 1.dp)

                Text(
                    "Status:\n${if (occupied) "Occupied" else "Free"}",
                    textAlign = TextAlign.Center)

                Spacer(Modifier.size(10.dp))

                if (!occupied) {
                    Button(
                        onClick = {
                            try {
                                appViewModel.occupySlot(slot.copy())
                            }
                            catch (e: Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonColors(mainColor, colorScheme.background, Color.Gray, Color.LightGray)
                    ) {Text("Mark as used")}
                }
                else if (slot.occupiedBy == appViewModel.uid) {
                    Button(
                        onClick = {
                            try {
                                appViewModel.freeSlot(slot.copy())
                            }
                            catch (e: Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonColors(Color.Red, Color.White, Color.Gray, Color.LightGray)
                    ) {Text("Stop using")}
                }
                else {
                    Text("Charger is occupied until:\n" +
                            "${Instant.ofEpochMilli(slot.occupiedUntil).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))}",
                        textAlign = TextAlign.Center)
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {}
    )
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
                        .fillMaxWidth(barFraction * 7 / 8)
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

@Composable
fun RelevantNearbyServices(
    context: Context,
    mapViewModel: MapViewModel,
    lat: Double,
    lng: Double
) {
    val location = LatLng(lat, lng)
    val result = remember { mutableStateListOf<List<String>>() }
    val connected = connectionStatus()
    var hasPolled = false

    LaunchedEffect(connected) {
        if (!hasPolled) {
            result.addAll(mapViewModel.getNearbyServices(context, location) ?: listOf())
            result.sortBy {it.getOrNull(3)?.toDoubleOrNull() ?: Double.MAX_VALUE} // sorts services by how close they are
            hasPolled = true
        }
    }

    if (connected) {
        Text("Nearby Services", fontSize = 32.sp)
        Spacer(Modifier.size(20.dp))

        for (info in result) {
            Text(
                info[0], // name of service
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.size(12.dp))

            Text(
                "Type: ${info[1]}", // type of service
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.size(8.dp))

            Text(
                "Location:\n${info[2]}", // address of service
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.size(8.dp))

            Text(
                "Distance from charger : ${info[3]} km", // distance from charger to service
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(Modifier.padding(10.dp), thickness = 1.dp)
        }
    }
    else {
        Text("Connect to the internet to view services near this charger!", textAlign = TextAlign.Center)
    }
}

@Composable
fun MapLongClickDialog(
    clickedLatLng: LatLng,
    onCreateChargerByHoldingOnMap: (LatLng) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create charger here?: $clickedLatLng")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreateChargerByHoldingOnMap(clickedLatLng)
                }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }) {
                Text("No")
            }
        }
    )
}
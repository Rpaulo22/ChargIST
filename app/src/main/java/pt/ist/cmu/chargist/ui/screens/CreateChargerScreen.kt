package pt.ist.cmu.chargist.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.ui.elements.LocationSearchBar
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import pt.ist.cmu.chargist.viewmodel.MapViewModel

@Composable
fun ChargerForm(
    appViewModel: AppViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    onCreateClick: () -> Unit,
    chargerId: String? = null  // if id is not given, a new charger is created, else a given charger is edited
) {
    val context = LocalContext.current
    
    val edit = (chargerId != null)

    val user = FirebaseAuth.getInstance().currentUser
    val uid = user!!.uid

    val scrollState = rememberScrollState()

    val userLocation = mapViewModel.userLocation

    var deletedSlots = remember { mutableStateListOf<ChargingSlot>() }
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember {mutableStateOf<ChargingSlot?>(null)}
    var index by remember {mutableIntStateOf(-1)}

    var chargerName by remember { mutableStateOf("") }
    var chargingSlots = remember { mutableStateListOf<ChargingSlot>() }
    var creditCard by remember { mutableStateOf(false) }
    var cash by remember { mutableStateOf(false) }
    var mbWay by remember { mutableStateOf(false) }
    var priceFastInput by remember { mutableStateOf("0.0") }
    var priceMediumInput by remember { mutableStateOf("0.0") }
    var priceSlowInput by remember { mutableStateOf("0.0") }
    var priceSlow = priceSlowInput.replace(",", ".").toDoubleOrNull()
    var priceMedium = priceMediumInput.replace(",", ".").toDoubleOrNull()
    var priceFast = priceFastInput.replace(",", ".").toDoubleOrNull()
    var latitude by remember { mutableStateOf(userLocation.value?.latitude) }
    var longitude by remember { mutableStateOf(userLocation.value?.longitude) }

    // if charger id != null, then form is for editing charger with said id
    if (edit) {
        val chargerState = remember { mutableStateOf<Charger?>(null) }

        LaunchedEffect(Unit) {
            chargerState.value = appViewModel.getChargerById(chargerId)
        }

        val charger = chargerState.value
        Log.d("Edit Charger", "charger retrieved: $charger")

        LaunchedEffect(charger) {
            if (charger != null) { // in case there is some error retrieving charger from local db
                Log.d("Edit Charger", "charger ${charger.name} is being edited")
                val slots = appViewModel.getCorrespondingChargingSlots(charger).first()

                chargerName = charger.name
                chargingSlots.addAll(slots)
                creditCard = charger.creditCard
                mbWay = charger.mbWay
                cash = charger.cash
                priceFastInput = charger.priceFast.toString()
                priceMediumInput = charger.priceMedium.toString()
                priceSlowInput = charger.priceSlow.toString()
                latitude = charger.latitude
                longitude = charger.longitude
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.size(10.dp))
        Text(
            text = if (!edit) "Creating new Charger" else "Editing Charger",
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(10.dp))
        Text("This charger will be placed at:")
        LocationSearchBar(
            onLocationUpdate = {
                Log.d("LocationUpdate", "$it")
                latitude = it?.latitude
                longitude = it?.longitude
            },
            mapViewModel = mapViewModel,
            initInCurrentLocation = !edit,
            starterCoords = LatLng(latitude!!, longitude!!)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedTextField(
                value = chargerName,
                onValueChange = { chargerName = it },
                label = { Text("Charger Name") }
            )
            Spacer(Modifier.size(20.dp))

            Button(
                onClick = { showDialog = true },
                colors = ButtonColors(mainColor, Color.White, Color.Transparent, Color.LightGray),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "add")
                    Text(text = "Add Charging Slots")
                }
            }
            Text("Current slots:")
            chargingSlots.forEach {
                Box (
                    Modifier.clickable(
                        onClick = {
                            showDialog = true
                            selectedSlot = it.copy()
                            index = chargingSlots.indexOf(it)
                        }
                    )
                        .background(MaterialTheme.colorScheme.onSecondary, RoundedCornerShape(8.dp)),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("   ${it.speed} - ${it.type}")
                        IconButton(
                            onClick = {
                                deletedSlots.add(it)
                                chargingSlots.remove(it)
                            }
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = "Remove Slot",
                                tint = mainColor
                            )
                        }
                    }
                }
                Spacer(Modifier.size(4.dp))
            }

            Spacer(Modifier.size(5.dp))
            HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)
            Spacer(Modifier.size(5.dp))

            Text("Payment Methods", fontSize = 24.sp)
            Spacer(Modifier.size(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("MbWay")
                    Switch(
                        checked = mbWay,
                        onCheckedChange = { mbWay = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = mainColor,
                            checkedTrackColor = mainColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Credit Card")
                    Switch(
                        checked = creditCard,
                        onCheckedChange = { creditCard = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = mainColor,
                            checkedTrackColor = mainColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Cash")
                    Switch(
                        checked = cash,
                        onCheckedChange = { cash = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = mainColor,
                            checkedTrackColor = mainColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(Modifier.size(5.dp))
            HorizontalDivider(modifier = Modifier.padding(16.dp), thickness = 1.dp)
            Spacer(Modifier.size(5.dp))

            Text("Prices", fontSize = 24.sp)
            Spacer(Modifier.size(6.dp))
            TextField(
                value = priceSlowInput,
                onValueChange = { priceSlowInput = it },
                label = { Text("Slow Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("€/kWh") }
            )
            Spacer(Modifier.size(4.dp))
            TextField(
                value = priceMediumInput,
                onValueChange = { priceMediumInput = it },
                label = { Text("Medium Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("€/kWh") }
            )
            Spacer(Modifier.size(4.dp))
            TextField(
                value = priceFastInput,
                onValueChange = { priceFastInput = it },
                label = { Text("Fast Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("€/kWh") }
            )
            Spacer(modifier = Modifier.size(24.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (edit) {
                    Button(
                        onClick = {
                            try {
                                appViewModel.deleteCharger(chargerId)
                                onCreateClick()
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonColors(
                            Color.Red,
                            Color.White,
                            Color.Transparent,
                            Color.LightGray
                        )
                    ) { Text("Delete Charger") }
                }

                Button(
                    onClick = {
                        try {
                            Log.d("LocationUpdate", "lat:$latitude | long:$longitude")
                            if (!edit) {
                                appViewModel.createCharger(
                                    name = chargerName,
                                    ownerId = uid,
                                    slots = chargingSlots,
                                    creditCard = creditCard,
                                    cash = cash,
                                    mbWay = mbWay,
                                    priceFast = priceFast ?: 0.0,
                                    priceMedium = priceMedium ?: 0.0,
                                    priceSlow = priceSlow ?: 0.0,
                                    lat = latitude ?: 0.0,
                                    lng = longitude ?: 0.0
                                )
                            } else {
                                appViewModel.updateCharger(
                                    chargerId = chargerId,
                                    name = chargerName,
                                    slots = chargingSlots,
                                    creditCard = creditCard,
                                    cash = cash,
                                    mbWay = mbWay,
                                    priceFast = priceFast ?: 0.0,
                                    priceMedium = priceMedium ?: 0.0,
                                    priceSlow = priceSlow ?: 0.0,
                                    lat = latitude ?: 0.0,
                                    lng = longitude ?: 0.0,
                                    deletedSlots = deletedSlots
                                )
                            }
                            onCreateClick()
                        } catch (e: Exception) {
                            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonColors(
                        mainColor,
                        Color.White,
                        Color.Transparent,
                        Color.LightGray
                    )
                ) { if (edit) Text("Save")
                    else Text("Create new Charger") }
            }
            Spacer(Modifier.size(30.dp))
        }
        if (showDialog) {
            AddChargingSlotDialog(
                onDismiss = {
                    showDialog = false
                    selectedSlot = null
                    index = -1
                },
                onConfirm = {
                    if (selectedSlot == null) chargingSlots.add(it)
                    else chargingSlots[index] = it
                },
                slot = selectedSlot,
            )
        }
    }
}

@Composable
fun AddChargingSlotDialog(
    onDismiss: () -> Unit,
    onConfirm: (ChargingSlot) -> Unit,
    slot: ChargingSlot?
) {
    var edit = (slot != null)

    var speedOptions = listOf("Slow", "Medium", "Fast")
    val (selectedSpeed, onSpeedSelected) = remember { mutableStateOf(slot?.speed ?: speedOptions[0]) }

    var typeOptions = listOf("CCS2", "Type 2")
    val (selectedType, onTypeSelected) = remember { mutableStateOf(slot?.type ?: typeOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (!edit) "Add new Charging Slot" else "Edit Charging Slot") },
        text = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.size(40.dp))
                Text(
                    "Select charging speed",
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(12.dp))
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    speedOptions.forEach { speed ->
                        Column(
                            modifier = Modifier
                                .selectable(
                                    selected = (speed == selectedSpeed),
                                    onClick = { onSpeedSelected(speed) },
                                    role = Role.RadioButton
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = speed
                            )
                            RadioButton(
                                selected = (speed == selectedSpeed),
                                onClick = null
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                    }
                }
                Spacer(Modifier.size(20.dp))
                Text(
                    "Select connector type",
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(12.dp))
                Row(
                    modifier = Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    typeOptions.forEach { type ->
                        Column(
                            modifier = Modifier
                                .selectable(
                                    selected = (type == selectedType),
                                    onClick = { onTypeSelected(type) },
                                    role = Role.RadioButton
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = type
                            )
                            RadioButton(
                                selected = (type == selectedType),
                                onClick = null
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newSlot = ChargingSlot(
                    id = slot?.id ?: "",
                    speed = selectedSpeed,
                    type = selectedType
                )
                onConfirm(newSlot)
                onDismiss()
            }) {
                Text(if (!edit) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
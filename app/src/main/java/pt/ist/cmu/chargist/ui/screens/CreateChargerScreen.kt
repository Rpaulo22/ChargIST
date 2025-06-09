package pt.ist.cmu.chargist.ui.screens

import android.Manifest
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import okhttp3.Request
import org.json.JSONObject
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.ui.elements.LocationSearchBar
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import pt.ist.cmu.chargist.viewmodel.MapViewModel
import java.nio.file.WatchEvent
import java.util.Locale
import java.util.UUID

@Composable
fun CreateChargerForm(
    appViewModel: AppViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    onCreateClick: () -> Unit
) {
    val context = LocalContext.current

    val user = FirebaseAuth.getInstance().currentUser
    val uid = user!!.uid

    val scrollState = rememberScrollState()

    val userLocation = mapViewModel.userLocation

    var showDialog by remember { mutableStateOf(false) }

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
    var latitude = userLocation.value?.latitude
    var longitude = userLocation.value?.longitude

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.size(10.dp))
        Text(
            text = "Creating new Charger",
            fontWeight = FontWeight.Bold,
            fontSize = 34.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(10.dp))
        Text("This charger will be placed at:")
        LocationSearchBar(
            onLocationUpdate = {
                latitude = it?.latitude
                longitude = it?.longitude
            },
            mapViewModel = mapViewModel
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("• ${it.speed} - ${it.type}")
                    IconButton(
                        onClick = { chargingSlots.remove(it) }
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = "Remove Slot",
                            tint = mainColor
                        )
                    }
                }
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


            Button(
                onClick = {
                    try {
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
                        onCreateClick()
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonColors(mainColor, Color.White, Color.Transparent, Color.LightGray)
            ) { Text("Create new Charger") }
            Spacer(Modifier.size(30.dp))
        }
        if (showDialog) {
            AddChargingSlotDialog(
                onDismiss = { showDialog = false },
                onConfirm = {
                    chargingSlots.add(it)
                    Log.d("Slots", "Added $it")
                }
            )
        }
    }
}

@Composable
fun AddChargingSlotDialog(
    onDismiss: () -> Unit,
    onConfirm: (ChargingSlot) -> Unit
) {
    var speedOptions = listOf("Slow", "Medium", "Fast")
    val (selectedSpeed, onSpeedSelected) = remember { mutableStateOf(speedOptions[0]) }

    var typeOptions = listOf("CCS2", "Type 2")
    val (selectedType, onTypeSelected) = remember { mutableStateOf(typeOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add new Charging Slot") },
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
                    speed = selectedSpeed,
                    type = selectedType
                )
                onConfirm(newSlot)
                onDismiss()
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
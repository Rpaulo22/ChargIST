package pt.ist.cmu.chargist.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import pt.ist.cmu.chargist.model.data.ChargingSpot
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import pt.ist.cmu.chargist.viewmodel.MapViewModel

@Composable
fun CreateChargerForm(
    appViewModel: AppViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    val userLocation = mapViewModel.userLocation

    var chargerName by remember { mutableStateOf("") }
    var chargingSpots = remember { mutableStateListOf<ChargingSpot>() }
    var creditCard by remember { mutableStateOf(false) }
    var cash by remember { mutableStateOf(false) }
    var mbWay by remember { mutableStateOf(false) }
    var priceFastInput by remember { mutableStateOf("0.0") }
    var priceMediumInput by remember { mutableStateOf("0.0") }
    var priceSlowInput by remember { mutableStateOf("0.0") }
    var priceSlow = priceSlowInput.toDoubleOrNull()
    var priceMedium = priceMediumInput.toDoubleOrNull()
    var priceFast = priceFastInput.toDoubleOrNull()
    val latitude = userLocation.value?.latitude
    val longitude= userLocation.value?.longitude

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = chargerName,
            onValueChange = {chargerName = it},
            label = {Text("Charger Name")}
        )

        Spacer(Modifier.size(10.dp))

        Text("Payment Methods", fontSize = 24.sp)
        Spacer(Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Credit Card")
            Switch(
                checked = creditCard,
                onCheckedChange = {creditCard = it}
            )
            Spacer(Modifier.size(2.dp))
            Text("MbWay")
            Switch(
                checked = mbWay,
                onCheckedChange = {mbWay = it}
            )
            Spacer(Modifier.size(2.dp))
            Text("Cash")
            Switch(
                checked = cash,
                onCheckedChange = {cash = it}
            )
        }

        Spacer(Modifier.size(10.dp))

        Text("Prices", fontSize = 24.sp)
        TextField(
            value = priceSlowInput,
            onValueChange = { priceSlowInput = it },
            label = { Text("Slow Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        TextField(
            value = priceMediumInput,
            onValueChange = { priceMediumInput = it },
            label = { Text("Medium Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        TextField(
            value = priceFastInput,
            onValueChange = { priceFastInput = it },
            label = { Text("Fast Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text("Charger coordinates: $userLocation")
        Spacer(Modifier.size(10.dp))

        Button(
            onClick = { appViewModel.createCharger(
                name = chargerName,
                spots = listOf<ChargingSpot>(),
                creditCard = creditCard,
                cash = cash,
                mbWay = mbWay,
                priceFast = priceFast?: 0.0,
                priceMedium = priceMedium?: 0.0,
                priceSlow = priceSlow?: 0.0,
                lat = latitude?:0.0,
                lng = longitude?:0.0
            ) }) { Text("Create new Charger") }
    }

}

package pt.ist.cmu.chargist.ui.screens

import android.Manifest
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import okhttp3.Request
import org.json.JSONObject
import pt.ist.cmu.chargist.model.data.ChargingSpot
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import pt.ist.cmu.chargist.viewmodel.MapViewModel
import java.util.Locale

@Composable
fun CreateChargerForm(
    appViewModel: AppViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel(),
    onCreateClick: () -> Unit
) {
    val context = LocalContext.current

    val userLocation = mapViewModel.userLocation
    mapViewModel.fetchAddress()

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
        Text(text = "Creating new Charger", fontWeight = FontWeight.Bold, fontSize = 38.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.size(20.dp))
        OutlinedTextField(
            value = chargerName,
            onValueChange = {chargerName = it},
            label = {Text("Charger Name")}
        )
        Spacer(Modifier.size(10.dp))

        Button(
            onClick = { Toast.makeText(context, "HAHA querias", Toast.LENGTH_SHORT).show() }
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "add")
                Text(text = "Add Charging Spots")
            }
        }
        Text("Already added ${chargingSpots.size} spots")

        Spacer(Modifier.size(10.dp))

        Text("Payment Methods", fontSize = 24.sp)
        Spacer(Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Credit Card")
            Switch(
                checked = creditCard,
                onCheckedChange = {creditCard = it}
            )
            Spacer(Modifier.size(5.dp))
            Text("MbWay")
            Switch(
                checked = mbWay,
                onCheckedChange = {mbWay = it}
            )
            Spacer(Modifier.size(5.dp))
            Text("Cash")
            Switch(
                checked = cash,
                onCheckedChange = {cash = it}
            )
        }

        Spacer(Modifier.size(10.dp))

        Text("Prices", fontSize = 24.sp)
        Spacer(Modifier.size(6.dp))
        TextField(
            value = priceSlowInput,
            onValueChange = { priceSlowInput = it },
            label = { Text("Slow Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(Modifier.size(4.dp))
        TextField(
            value = priceMediumInput,
            onValueChange = { priceMediumInput = it },
            label = { Text("Medium Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(Modifier.size(4.dp))
        TextField(
            value = priceFastInput,
            onValueChange = { priceFastInput = it },
            label = { Text("Fast Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text("This charger will be placed at: ${mapViewModel.address}")
        Spacer(Modifier.size(10.dp))

        Button(
            onClick = {
                try {
                    appViewModel.createCharger(
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
                    )
                    onCreateClick()
                }
                catch (e: Exception) {
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
            }) { Text("Create new Charger") }
    }
}
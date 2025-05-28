package pt.ist.cmu.chargist.ui.screens

import android.R.attr.navigationIcon
import android.R.attr.text
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargingSpot
import kotlin.reflect.typeOf

@Composable
fun HomeScreen(userId: String, onLogoutClick: () -> Unit) {
    val db = Firebase.firestore
    var chargers = mutableListOf<Charger>()

    db.collection("Charger")
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                val charger = Charger(
                    id = document.id,
                    name = document.data.get("name").toString(),
                    chargingSpots = document.data.get("chargingSpots") as List<String>,
                    creditCard = document.data.get("creditCard") as Boolean,
                    money = document.data.get("money") as Boolean,
                    mbWay = document.data.get("mbWay") as Boolean,
                    latitude = 0.0,
                    longitude = 0.0,
                    priceFast = 0.0,
                    priceMedium = 0.0,
                    priceSlow = 0.0
                )
                chargers.add(charger)
                Log.d("Firebase", "id: ${document.id} | ${(document.data.get("location"))}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w("Firebase", "Error getting documents.", exception)
        }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.094661, -9.261128), 10f)
    }

    Scaffold (
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { onLogoutClick() }
                ) {
                    Text("Logout")
                }
            }
        }
    ) { paddingValues ->
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState)

        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(text = "Home Screen")
                Text(text = "User $userId")
            }
        }
    }
}
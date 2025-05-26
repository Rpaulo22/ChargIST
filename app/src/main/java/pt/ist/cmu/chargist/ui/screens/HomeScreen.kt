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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargingSpot
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import kotlin.reflect.typeOf
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(userId: String, onLogoutClick: () -> Unit, viewModel: AppViewModel = viewModel()) {

    val chargers by viewModel.allChargers.collectAsState()
    //viewModel.updateChargers()

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
            for (charger in chargers) {
                item {
                    Text(text = "charger: $charger")
                }
            }
        }
    }
}
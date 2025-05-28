package pt.ist.cmu.chargist.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargerRepository
import pt.ist.cmu.chargist.model.data.ChargingSpot
import pt.ist.cmu.chargist.model.data.ChargingSpotRepository

class AppViewModel(application: Application) : AndroidViewModel(application)  {
    private val chargerRepository: ChargerRepository
    private val spotRepository: ChargingSpotRepository
    val allChargers: StateFlow<List<Charger>>
    val allChargingSpots: StateFlow<List<ChargingSpot>>


    init {
        val chargerDao = AppDatabase.getDatabase(application).chargerDao()
        val chargingSpotDao = AppDatabase.getDatabase(application).chargingSpotDao()
        chargerRepository = ChargerRepository(chargerDao)
        spotRepository = ChargingSpotRepository(chargingSpotDao)
        allChargers = chargerRepository.allChargers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allChargingSpots = spotRepository.allChargingSpots.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun createCharger() {
        // IMPLEMENT
    }

    fun updateSpots() {
        val db = Firebase.firestore

        db.collection("ChargingSpot")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val spot = ChargingSpot(
                        id = document.id,
                        speed = document.data["speed"] as String,
                        type = document.data["type"] as String
                    )
                    Log.d("Firebase", "id: ${document.id} | ${document.data}")
                    viewModelScope.launch {
                        spotRepository.insert(spot)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents.", exception)
            }
    }

    fun updateChargers() {
        val db = Firebase.firestore

        db.collection("Charger")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val coords = document.data["location"] as GeoPoint
                    val prices = document.data["price"] as Map<String, Number>
                    val charger = Charger(
                        id = document.id,
                        name = document.data["name"].toString(),
                        chargingSpots = document.data["chargingSpots"] as List<String>,
                        creditCard = document.data["creditCard"] as Boolean,
                        money = document.data["money"] as Boolean,
                        mbWay = document.data["mbWay"] as Boolean,
                        latitude = coords.latitude,
                        longitude = coords.longitude,
                        priceFast = prices["fast"]?.toDouble() ?: -1.0,
                        priceMedium = prices["medium"]?.toDouble() ?: -1.0,
                        priceSlow = prices["slow"]?.toDouble() ?: -1.0
                    )

                    Log.d("Firebase", "id: ${document.id} | ${document.data}")
                    viewModelScope.launch {
                        chargerRepository.insert(charger)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents.", exception)
            }

    }

    fun deleteCharger(charger: Charger) {
        viewModelScope.launch {
            chargerRepository.delete(charger)
        }
    }
}

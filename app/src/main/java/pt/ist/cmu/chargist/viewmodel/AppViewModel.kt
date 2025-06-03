package pt.ist.cmu.chargist.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.repository.ChargerRepository
import pt.ist.cmu.chargist.model.data.ChargingSpot
import pt.ist.cmu.chargist.model.repository.ChargingSpotRepository

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

        updateChargers()
    }

    fun createCharger(name:String, spots:List<ChargingSpot>, creditCard: Boolean, mbWay:Boolean, cash:Boolean,
                      lat:Double, lng:Double, priceFast:Double, priceMedium:Double, priceSlow: Double) {

        val data = hashMapOf(
            "name" to name,
            "location" to GeoPoint(lat, lng),
            "cash" to cash,
            "creditCard" to creditCard,
            "mbWay" to mbWay,
            "prices" to hashMapOf<String, Double>(
                "fast" to priceFast,
                "medium" to priceMedium,
                "slow" to priceSlow
            )
        )

        val db = Firebase.firestore
        db.runTransaction { tx ->
            val refs = mutableListOf<DocumentReference>()
            val chargerRef = db.collection("Charger").document()
            refs.add(chargerRef)

            tx.set(chargerRef, data)
            for (spot in spots) {
                val spotData = hashMapOf(
                    "speed" to spot.speed,
                    "type" to spot.type
                )

                val spotRef = db.collection("ChargingSpot").document()
                refs.add(spotRef)

                tx.set(spotRef, spotData)
            }

            // return references to created documents, refs[0] ref do carregador, restantes refs dos spots
            refs
        }.addOnSuccessListener { refs ->
            val finalChargingSpots = mutableListOf<ChargingSpot>()
            var i = 1
            for (spot in spots) {
                val cs = ChargingSpot(refs[i].toString(), spot.speed, spot.type)
                finalChargingSpots.add(cs)
                i++
            }

            val c = Charger(
                refs[0].toString(),
                name,
                refs.subList(1, refs.size).map { r -> r.toString() },
                creditCard,
                cash,
                mbWay,
                lat,
                lng,
                priceFast,
                priceMedium,
                priceSlow
            )
            viewModelScope.launch {
                for (spot in finalChargingSpots) {
                    spotRepository.insert(spot)
                }
                chargerRepository.insert(c)
            }
        }.addOnFailureListener {
            throw Exception("Error creating charger. Please try again")
        }
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
                        cash = document.data["cash"] as Boolean,
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

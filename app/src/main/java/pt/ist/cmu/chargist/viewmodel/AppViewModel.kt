package pt.ist.cmu.chargist.viewmodel

import android.R
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.repository.ChargerRepository
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.model.repository.ChargingSlotRepository
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application)  {
    private val chargerRepository: ChargerRepository
    private val slotRepository: ChargingSlotRepository
    val allChargers: StateFlow<List<Charger>>
    val allChargingSlots: StateFlow<List<ChargingSlot>>

    var lastSlot by mutableStateOf<ChargingSlot?>(null)


    init {
        val chargerDao = AppDatabase.getDatabase(application).chargerDao()
        val chargingSlotDao = AppDatabase.getDatabase(application).chargingSlotDao()
        chargerRepository = ChargerRepository(chargerDao)
        slotRepository = ChargingSlotRepository(chargingSlotDao)
        allChargers = chargerRepository.allChargers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        allChargingSlots = slotRepository.allChargingSlots.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        updateChargers()
    }

    fun createCharger(name:String, slots:List<ChargingSlot>, creditCard: Boolean, mbWay:Boolean, cash:Boolean,
                      lat:Double, lng:Double, priceFast:Double, priceMedium:Double, priceSlow: Double) {

        if (priceFast < 0 || priceMedium < 0 || priceSlow < 0) {
            throw Exception("Invalid price (must be positive).")
        }
        if (name.length < 3) {
            throw Exception("Charger's name must have 3 characters or more.")
        }
        if (!(creditCard || cash || mbWay)) {
            throw Exception("There must be an available payment method.")
        }

        val data = hashMapOf(
            "name" to name,
            "location" to GeoPoint(lat, lng),
            "cash" to cash,
            "creditCard" to creditCard,
            "mbWay" to mbWay,
            "price" to hashMapOf<String, Double>(
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
            for (slot in slots) {
                val slotData = hashMapOf(
                    "speed" to slot.speed,
                    "type" to slot.type
                )

                val slotRef = db.collection("ChargingSlot").document()
                refs.add(slotRef)

                tx.set(slotRef, slotData)
            }

            val slotRefs =  refs.subList(1, refs.size).map { r -> r.id }
            tx.update(chargerRef, "chargingSlots", slotRefs)

            // return references to created documents, refs[0] ref do carregador, restantes refs dos slots
            refs
        }.addOnSuccessListener { refs ->
            val finalChargingSlots = mutableListOf<ChargingSlot>()
            var i = 1
            for (slot in slots) {
                val cs = ChargingSlot(refs[i].id, slot.speed, slot.type)
                finalChargingSlots.add(cs)
                i++
            }

            val c = Charger(
                refs[0].id,
                name,
                refs.subList(1, refs.size).map { r -> r.id },
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
                for (slot in finalChargingSlots) {
                    slotRepository.insert(slot)
                }
                chargerRepository.insert(c)
            }
        }.addOnFailureListener {
            throw Exception("Error creating charger. Please try again")
        }
    }

    fun updateSlots() {
        val db = Firebase.firestore

        db.collection("ChargingSlot")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val slot = ChargingSlot(
                        id = document.id,
                        speed = document.data["speed"] as String,
                        type = document.data["type"] as String
                    )
                    Log.d("Firebase", "id: ${document.id} | ${document.data}")
                    viewModelScope.launch {
                        slotRepository.insert(slot)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents.", exception)
            }
    }

    fun updateChargers() {
        val db = Firebase.firestore

        viewModelScope.launch {
            chargerRepository.deleteRelevantChargers()
        }

        db.collection("Charger")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val coords = document.data["location"] as GeoPoint
                    val prices = document.data["price"] as Map<String, Number>
                    val charger = Charger(
                        id = document.id,
                        name = document.data["name"].toString(),
                        chargingSlots = document.data["chargingSlots"] as List<String>,
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

    fun createChargingSlot(speed: String, type: String) {
        // id is randomized so that we can have multiple slots in the same list, not possible if all of them have the same default id
        val newSlot = ChargingSlot(id = UUID.randomUUID().toString(), speed = speed, type = type)
        lastSlot = newSlot
    }

    fun deleteCharger(charger: Charger) {
        // todo delete on firestore aswell
        viewModelScope.launch {
            chargerRepository.delete(charger)
        }
    }

    fun getCorrespondingChargingSlots(charger: Charger): Flow<List<ChargingSlot>> {
        return slotRepository.getSlots(charger.chargingSlots)
    }
}

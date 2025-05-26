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

class AppViewModel(application: Application) : AndroidViewModel(application)  {
    private val repository: ChargerRepository
    val allChargers: StateFlow<List<Charger>>

    /*
    private val _noteTitle = MutableStateFlow("")
    val noteTitle: StateFlow<String> = _noteTitle

    private val _noteContent = MutableStateFlow("")
    val noteContent: StateFlow<String> = _noteContent
    */

    init {
        val chargerDao = AppDatabase.getDatabase(application).chargerDao()
        repository = ChargerRepository(chargerDao)
        allChargers = repository.allChargers.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun createCharger() {
        // TODO IMPLEMENT
    }

    fun updateChargers() {
        val db = Firebase.firestore

        db.collection("Charger")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val coords = document.data["location"] as GeoPoint
                    val prices = document.data["price"] as Map<*,*>
                    val charger = Charger(
                        id = document.id,
                        name = document.data["name"].toString(),
                        chargingSpots = document.data["chargingSpots"] as List<String>,
                        creditCard = document.data["creditCard"] as Boolean,
                        money = document.data["money"] as Boolean,
                        mbWay = document.data["mbWay"] as Boolean,
                        latitude = coords.latitude,
                        longitude = coords.longitude,
                        priceFast = prices["fast"] as Double,
                        priceMedium = prices["medium"] as Double,
                        priceSlow = prices["slow"] as Double
                    )
                    Log.d("Firebase", "id: ${document.id} | ${document.data}")
                    viewModelScope.launch {
                        repository.insert(charger) // TODO verify if charger in repository first
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting documents.", exception)
            }

    }

    fun deleteCharger(charger: Charger) {
        viewModelScope.launch {
            repository.delete(charger)
        }
    }
}

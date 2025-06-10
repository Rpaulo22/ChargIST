package pt.ist.cmu.chargist.viewmodel

import android.R
import android.R.attr.name
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.repository.ChargerRepository
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.model.data.User
import pt.ist.cmu.chargist.model.repository.ChargingSlotRepository
import pt.ist.cmu.chargist.model.repository.UserRepository
import java.util.UUID
import kotlin.math.round

class AppViewModel(application: Application) : AndroidViewModel(application)  {
    private val userRepository: UserRepository
    private val chargerRepository: ChargerRepository
    private val slotRepository: ChargingSlotRepository
    val allChargers: StateFlow<List<Charger>>
    val allChargingSlots: StateFlow<List<ChargingSlot>>
    val currentUser = MutableStateFlow<User?>(null)
    val favoriteChargers: StateFlow<List<String>> = currentUser
        .map { user -> user?.favoriteChargers ?: emptyList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val user = FirebaseAuth.getInstance().currentUser
    val uid = user!!.uid

    var lastSlot by mutableStateOf<ChargingSlot?>(null)


    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        val chargerDao = AppDatabase.getDatabase(application).chargerDao()
        val chargingSlotDao = AppDatabase.getDatabase(application).chargingSlotDao()
        userRepository = UserRepository(userDao)
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

        // get current user
        viewModelScope.launch {
            val localUser = userRepository.getUserById(uid)
            if (localUser != null) {
                currentUser.value = localUser
                Log.e("test", localUser.toString())
            }
            else {
                val db = Firebase.firestore
                val doc = db
                    .collection("User")
                    .document(uid)
                    .get()
                    .await()
                val data = doc.data
                if (doc.exists() && data != null) {
                    val remoteUser = User(
                        id = doc.id,
                        email = data["email"] as String,
                        name = data["username"] as String,
                        phoneNumber = data["phoneNumber"] as String,
                        favoriteChargers = data["favoriteChargers"] as MutableList<String>
                    )
                    userRepository.insert(remoteUser)
                    currentUser.value = remoteUser
                }
            }
        }
    }

    fun createCharger(name:String, ownerId:String, slots:List<ChargingSlot>, creditCard: Boolean, mbWay:Boolean, cash:Boolean,
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
            "ownerId" to ownerId,
            "location" to GeoPoint(lat, lng),
            "cash" to cash,
            "creditCard" to creditCard,
            "mbWay" to mbWay,
            "price" to hashMapOf<String, Double>(
                "fast" to priceFast,
                "medium" to priceMedium,
                "slow" to priceSlow
            ),
            "ratings" to hashMapOf<String, Double>(),
            "ratingsMean" to 0.0,
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
                ownerId,
                refs.subList(1, refs.size).map { r -> r.id },
                creditCard,
                cash,
                mbWay,
                lat,
                lng,
                priceFast,
                priceMedium,
                priceSlow,
                emptyMap(),
                0.0,
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

    fun updateCharger(chargerId: String, name:String, slots:List<ChargingSlot>, creditCard: Boolean, mbWay:Boolean, cash:Boolean,
                      lat:Double, lng:Double, priceFast:Double, priceMedium:Double, priceSlow: Double, deletedSlots: List<ChargingSlot>) {

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
            val chargerRef = db.collection("Charger").document(chargerId)
            refs.add(chargerRef)

            tx.update(chargerRef, data)

            for (slot in slots) {
                val slotData = mapOf(
                    "speed" to slot.speed,
                    "type" to slot.type
                )

                if (slot.id == "") {
                    val slotRef = db.collection("ChargingSlot").document()
                    refs.add(slotRef)

                    tx.set(slotRef, slotData)
                }
                else {
                    val slotRef = db.collection("ChargingSlot").document(slot.id)
                    tx.update(slotRef, slotData)
                }
            }

            for (slot in deletedSlots) {
                val slotRef = db.collection("ChargingSlot").document(slot.id)
                tx.delete(slotRef)
            }

            val slotRefs =  refs.subList(1, refs.size).map { r -> r.id }
            tx.update(chargerRef, "chargingSlots", slotRefs)

            // return references to created documents, refs[0] ref do carregador, restantes refs dos slots
            refs
        }.addOnSuccessListener { refs ->
            val finalChargingSlots = mutableListOf<ChargingSlot>()
            var i = 1
            for (slot in slots) {
                var cs = slot
                if (slot.id == "") { // slot without ids get id from firebase reference
                    cs = ChargingSlot(refs[i].id, slot.speed, slot.type)
                    i++
                }
                finalChargingSlots.add(cs)
            }

            val slotIds = finalChargingSlots.map { s -> s.id }

            viewModelScope.launch {
                for (slot in finalChargingSlots) {
                    slotRepository.insert(slot)
                }
                for (slot in deletedSlots) {
                    slotRepository.delete(slot)
                }
                chargerRepository.update(chargerId, name, slotIds, creditCard, mbWay, cash, lat, lng, priceFast, priceMedium, priceSlow)
            }
        }.addOnFailureListener {
            throw Exception("Error creating charger. Please try again")
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
                        ownerId = document.data["ownerId"].toString(),
                        chargingSlots = document.data["chargingSlots"] as List<String>,
                        creditCard = document.data["creditCard"] as Boolean,
                        cash = document.data["cash"] as Boolean,
                        mbWay = document.data["mbWay"] as Boolean,
                        latitude = coords.latitude,
                        longitude = coords.longitude,
                        priceFast = prices["fast"]?.toDouble() ?: -1.0,
                        priceMedium = prices["medium"]?.toDouble() ?: -1.0,
                        priceSlow = prices["slow"]?.toDouble() ?: -1.0,
                        ratings = document.data["ratings"] as Map<String, Double>,
                        ratingsMean = document.data["ratingsMean"] as Double,
                    )

                    Log.d("Firebase", "id: ${document.id} | ${document.data}")
                    viewModelScope.launch {
                        chargerRepository.insert(charger)
                    }
                    if (charger.chargingSlots.isNotEmpty()) {
                        db.collection("ChargingSlot")
                            .whereIn(FieldPath.documentId(), charger.chargingSlots)
                            .get()
                            .addOnSuccessListener { result ->
                                for (document in result) {
                                    val slot = ChargingSlot(
                                        id = document.id,
                                        speed = document.data["speed"].toString(),
                                        type = document.data["type"].toString()
                                    )
                                    viewModelScope.launch {
                                        slotRepository.insert(slot)
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.w("Firebase", "Error getting slots.", exception)
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firebase", "Error getting chargers.", exception)
            }

    }

    fun deleteCharger(chargerId: String) {
        viewModelScope.launch {
            val charger = getChargerById(chargerId)  // This will now wait properly

            if (charger == null) {
                Log.e("DeleteCharger", "Charger not found")
                return@launch
            }

            val db = Firebase.firestore

            try {
                val querySnapshot = db.collection("User")
                    .whereArrayContains("favoriteChargers", charger.id)
                    .get()
                    .await()

                val userDocs = querySnapshot.documents

                db.runTransaction { tx ->
                    for (userDoc in userDocs) {
                        val userRef = userDoc.reference
                        tx.update(userRef, "favoriteChargers", FieldValue.arrayRemove(charger.id))
                    }

                    for (slotId in charger.chargingSlots) {
                        val slotRef = db.collection("ChargingSlot").document(slotId)
                        tx.delete(slotRef)
                    }

                    val chargerRef = db.collection("Charger").document(charger.id)
                    tx.delete(chargerRef)
                }.addOnSuccessListener {
                    viewModelScope.launch {
                        for (slotId in charger.chargingSlots) {
                            slotRepository.delete(slotId)
                        }
                        // todo have it deleted in user local info
                        chargerRepository.delete(charger)
                        Log.d("DeleteCharger", "Successfully deleted charger and slots")
                    }
                }.addOnFailureListener {
                    Log.e("DeleteCharger", "Transaction failed", it)
                }

            } catch (e: Exception) {
                Log.e("DeleteCharger", "Error retrieving user documents", e)
                throw e
            }
        }
    }


    suspend fun getChargerById(id: String): Charger? {
        return chargerRepository.getChargerById(id)
    }

    fun getCorrespondingChargingSlots(charger: Charger): Flow<List<ChargingSlot>> {
        return slotRepository.getSlots(charger.chargingSlots)
    }

    fun rateCharger(charger: Charger, uid: String, rating: Double) {
        val db = Firebase.firestore

        // add/update the rating
        db.collection("Charger")
            .document(charger.id)
            .update("ratings.$uid", rating)
            .addOnSuccessListener {
                Log.d("Firebase", "Rating successfully updated")

                viewModelScope.launch {
                    val updatedCharger = chargerRepository.getChargerById(charger.id)
                    val newRatings = updatedCharger.ratings.toMutableMap()
                    newRatings[uid] = rating

                    // calculate the new mean score
                    val newRatingsMean = if (newRatings.isNotEmpty()) {
                        val avg = newRatings.values.average()
                        round(avg * 100) / 100
                    } else {
                        0.0
                    }

                    chargerRepository.update(
                        updatedCharger.copy(
                            ratings = newRatings,
                            ratingsMean = newRatingsMean
                        )
                    )

                    // update the mean rating
                    db.collection("Charger")
                        .document(charger.id)
                        .update("ratingsMean", newRatingsMean)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Mean rating successfully updated")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error updating mean rating", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error updating rating", e)
            }
        return
    }



    fun favoriteCharger(charger: Charger) {
        val db = Firebase.firestore
        val userRef = db.collection("User").document(uid)
        userRef.update("favoriteChargers", FieldValue.arrayUnion(charger.id))
            .addOnSuccessListener {
                viewModelScope.launch {
                    val updatedUser = userRepository.getUserById(uid)
                    val newFavorites = updatedUser!!.favoriteChargers.toMutableList()
                    if (charger.id !in newFavorites) {
                        newFavorites.add(charger.id)
                    }
                    userRepository.update(
                        updatedUser.copy(
                            favoriteChargers = newFavorites
                        )
                    )
                    toggleFavorite(charger.id)
                    Log.e("Firebase", "User {$uid} successfully added charger ${charger.id} to favorites")
                }
            }
            .addOnFailureListener {
                Log.e("Firebase", "User {$uid} had an error adding charger ${charger.id} to favorites")
            }
    }

    fun unfavoriteCharger(charger: Charger) {
        val db = Firebase.firestore
        val userRef = db.collection("User").document(uid)
        userRef.update("favoriteChargers", FieldValue.arrayRemove(charger.id))
            .addOnSuccessListener {
                viewModelScope.launch {
                    val updatedUser = userRepository.getUserById(uid)
                    val newFavorites = updatedUser!!.favoriteChargers.toMutableList()
                    if (charger.id in newFavorites) {
                        newFavorites.remove(charger.id)
                    }
                    userRepository.update(
                        updatedUser.copy(
                            favoriteChargers = newFavorites
                        )
                    )
                    toggleFavorite(charger.id)
                    Log.d("Firebase", "User {$uid} successfully removed charger ${charger.id} to favorites")
                }
            }
            .addOnFailureListener {
                Log.e("Firebase", "User {$uid} had an error removing charger ${charger.id} to favorites")
            }
    }

    fun toggleFavorite(chargerId: String) {
        val user = currentUser.value ?: return
        val newFavorites = user.favoriteChargers.toMutableList().apply {
            if (contains(chargerId)) remove(chargerId) else add(chargerId)
        }
        currentUser.value = user.copy(favoriteChargers = newFavorites)
    }
}


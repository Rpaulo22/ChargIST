package pt.ist.cmu.chargist.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.extension.setLocation
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Auth
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.repository.ChargerRepository
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.model.data.User
import pt.ist.cmu.chargist.model.repository.AuthRepository
import pt.ist.cmu.chargist.model.repository.ChargingSlotRepository
import pt.ist.cmu.chargist.model.repository.UserRepository
import java.time.Instant
import kotlin.math.round

class AppViewModel(application: Application) : AndroidViewModel(application)  {
    private val authRepository: AuthRepository
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
        val firebaseAuth = FirebaseAuth.getInstance()
        val auth = Auth(firebaseAuth)
        val userDao = AppDatabase.getDatabase(application).userDao()
        val chargerDao = AppDatabase.getDatabase(application).chargerDao()
        val chargingSlotDao = AppDatabase.getDatabase(application).chargingSlotDao()
        authRepository = AuthRepository(auth)
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

        reloadChargers()

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

    val isCreatingCharger = MutableStateFlow(false)

    fun createCharger(context:Context, name:String, ownerId:String, slots:List<ChargingSlot>, creditCard: Boolean, mbWay:Boolean, cash:Boolean,
                      lat:Double, lng:Double, priceFast:Double, priceMedium:Double, priceSlow:Double, capturedImageUri:Uri) {

        if (isCreatingCharger.value) {
            Log.d("Create", "Not creating :(")
            return
        }
        else {
            isCreatingCharger.value = true
        }

        if (priceFast < 0 || priceMedium < 0 || priceSlow < 0) {
            throw Exception("Invalid price (must be 0 €/kWh minimum).")
        }
        if (priceFast > 100 || priceMedium > 100 || priceSlow > 100) {
            throw Exception("Invalid price (must be 100 €/kWh maximum).")
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

        Log.d("Create", "Charger is being placed at ${data["location"]}")

        val db = Firebase.firestore
        db.runTransaction { tx ->
            val refs = mutableListOf<DocumentReference>()
            val chargerRef = db.collection("Charger").document()
            refs.add(chargerRef)

            tx.set(chargerRef, data)
            for (slot in slots) {
                val slotData = hashMapOf(
                    "speed" to slot.speed,
                    "type" to slot.type,
                    "occupiedUntil" to 0,
                    "occupiedBy" to ""
                )

                Log.d("SlotData", "Slot being sent to firebase is $slot")

                val slotRef = db.collection("ChargingSlot").document()
                refs.add(slotRef)

                tx.set(slotRef, slotData)
            }

            val slotRefs =  refs.subList(1, refs.size).map { r -> r.id }
            tx.update(chargerRef, "chargingSlots", slotRefs)

            // return references to created documents, refs[0] ref do carregador, restantes refs dos slots
            refs
        }.addOnSuccessListener { refs ->
            // Set location for a document
            val geoFirestore = GeoFirestore(db.collection("charger"))
            geoFirestore.setLocation(refs[0].id, GeoPoint(lat,lng)) { e ->
                if (e != null) {
                    Log.e("GeoFirestore", "Failed to set location", e)
                } else {
                    Log.d("GeoFirestore", "Location set successfully")
                }
            }

            val finalChargingSlots = mutableListOf<ChargingSlot>()
            var i = 1
            for (slot in slots) {
                val cs = ChargingSlot(refs[i].id, slot.speed, slot.type, 0, "")
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
                if (capturedImageUri != Uri.EMPTY)
                    uploadChargerPhoto(context, refs[0].id, capturedImageUri)
                isCreatingCharger.value = false
            }
        }.addOnFailureListener {
            throw Exception("Error creating charger. Please try again")
        }
    }

    fun updateCharger(context:Context, chargerId: String, name:String, slots:List<ChargingSlot>, creditCard: Boolean, mbWay:Boolean, cash:Boolean,
                      lat:Double, lng:Double, priceFast:Double, priceMedium:Double, priceSlow: Double, deletedSlots: List<ChargingSlot>, capturedImageUri: Uri) {

        if (isCreatingCharger.value) {
            Log.d("Create", "Not creating :(")
            return
        }
        else {
            isCreatingCharger.value = true
        }

        if (priceFast < 0 || priceMedium < 0 || priceSlow < 0) {
            throw Exception("Invalid price (must be more or equal than 0 €/kWh).")
        }
        if (priceFast > 100 || priceMedium > 100 || priceSlow > 100) {
            throw Exception("Invalid price (must be less than 100 €/kWh).")
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
                    "type" to slot.type,
                    "occupiedBy" to slot.occupiedBy,
                    "occupiedUntil" to slot.occupiedUntil
                )

                Log.d("SlotData", "Slot being sent to firebase is $slot")

                if (slot.id == "") {
                    val slotRef = db.collection("ChargingSlot").document()
                    refs.add(slotRef)

                    tx.set(slotRef, slotData)
                }
                else {
                    val slotRef = db.collection("ChargingSlot").document(slot.id)
                    refs.add(slotRef)
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
            // Set location for a document
            val geoFirestore = GeoFirestore(db.collection("charger"))
            geoFirestore.setLocation(refs[0].id, GeoPoint(lat,lng)) { e ->
                if (e != null) {
                    Log.e("GeoFirestore", "Failed to set location", e)
                } else {
                    Log.d("GeoFirestore", "Location set successfully")
                }
            }

            val finalChargingSlots = mutableListOf<ChargingSlot>()
            var i = 1
            for (slot in slots) {
                var cs = slot
                if (slot.id == "") { // slot without ids get id from firebase reference
                    cs = ChargingSlot(refs[i].id, slot.speed, slot.type, 0, "")
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
                if (capturedImageUri != Uri.EMPTY)
                    uploadChargerPhoto(context, chargerId, capturedImageUri)
                isCreatingCharger.value = false
            }
        }.addOnFailureListener {
            throw Exception("Error creating charger. Please try again")
        }
    }

    fun reloadChargers() {
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
                                        type = document.data["type"].toString(),
                                        occupiedUntil = document.data["occupiedUntil"] as Long,
                                        occupiedBy = document.data["occupiedBy"].toString()
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
            val charger = getChargerById(chargerId)

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
                    val geoFirestore = GeoFirestore(db.collection("charger"))
                    geoFirestore.removeLocation(chargerId)

                    viewModelScope.launch {
                        for (slotId in charger.chargingSlots) {
                            slotRepository.delete(slotId)
                        }
                        // delete removed charger from all users' favorite chargers
                        val allUsers = userRepository.getAllUsers()
                        for (user in allUsers) {
                            val newFavorites = user.favoriteChargers.toMutableList()
                            if (newFavorites.contains(charger.id)) {
                                newFavorites.remove(charger.id)
                                userRepository.update(
                                    user.copy(
                                        favoriteChargers = newFavorites
                                    )
                                )
                            }
                        }
                        slotRepository.delete(charger.chargingSlots)
                        chargerRepository.delete(charger)
                        // delete photo from Firebase Storage
                        val storage = Firebase.storage
                        val storageRef = storage.reference
                        val chargerPhotoImagesRef = storageRef.child("images/$chargerId/photo_$chargerId.jpg")
                        chargerPhotoImagesRef.delete()

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

    fun rateCharger(charger: Charger, rating: Double) {
        val db = Firebase.firestore

        // delete the rating
        if (rating == 0.0) {
            db.collection("Charger")
                .document(charger.id)
                .update("ratings.$uid", FieldValue.delete())
                .addOnSuccessListener {
                    viewModelScope.launch {
                        val updatedCharger = chargerRepository.getChargerById(charger.id)
                        val newRatings = updatedCharger.ratings.toMutableMap()
                        newRatings.remove(uid)

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
                        Log.d("Firebase", "Rating successfully deleted")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error deleting rating", e)
                }
        }
        else {
            // add/update the rating
            db.collection("Charger")
                .document(charger.id)
                .update("ratings.$uid", rating)
                .addOnSuccessListener {
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
                        Log.d("Firebase", "Rating successfully updated")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error updating rating", e)
                }
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

    fun deleteAccount() {
        try {
            viewModelScope.launch {
                Log.e("AppViewModel", "1")
                val uidAux = currentUser.value!!.id
                val chargerList = allChargers.first()
                Log.e("AppViewModel", allChargers.value.toString())
                chargerList.forEach { charger ->
                    // delete all chargers owned by the to be deleted user
                    if (charger.ownerId == uidAux) {
                        deleteCharger(charger.id)
                    }
                    // delete all ratings owned by the to be deleted user
                    else if (charger.ratings.containsKey(uidAux) ) {
                        rateCharger(charger, 0.0)
                    }
                }
                Log.e("AppViewModel", "2")
                // delete the user
                authRepository.deleteAccount()
                userRepository.delete(currentUser.value!!)
           }
            Log.d("AppViewModel", "Successfully deleted user account")
        } catch (e: Exception) {
            Log.e("AppViewModel", "Caught an exception: $e")
        }
    }

    fun occupySlot(slot: ChargingSlot) {
        slot.occupiedBy = uid
        slot.occupiedUntil = Instant.now().toEpochMilli() + 7200000 // 7200000 ms = 2 hours
        val db = Firebase.firestore
        val slotRef = db.collection("ChargingSlot").document(slot.id)

        val slotData = mapOf(
            "speed" to slot.speed,
            "type" to slot.type,
            "occupiedUntil" to slot.occupiedUntil,
            "occupiedBy" to slot.occupiedBy
        )

        slotRef.update(slotData)
            .addOnSuccessListener {
                viewModelScope.launch {
                    slotRepository.update(slot)
                }
            }
            .addOnFailureListener {
                Log.e("Firebase", "User {$uid} had an error occupying charger ${slot.id}")
                throw Exception("Couldn't occupy charger. Please try again.")
            }
    }

    fun freeSlot(slot: ChargingSlot) {
        slot.occupiedBy = ""
        slot.occupiedUntil = Instant.now().toEpochMilli() // declare occupied until now

        val db = Firebase.firestore
        val slotRef = db.collection("ChargingSlot").document(slot.id)

        val slotData = mapOf(
            "speed" to slot.speed,
            "type" to slot.type,
            "occupiedUntil" to slot.occupiedUntil,
            "occupiedBy" to slot.occupiedBy
        )

        slotRef.update(slotData)
            .addOnSuccessListener {
                viewModelScope.launch {
                    slotRepository.update(slot)
                }
            }
            .addOnFailureListener {
                Log.e("Firebase", "User {$uid} had an error freeing charger ${slot.id}")
                throw Exception("Couldn't free charger. Please try again.")
            }
    }

    suspend fun uploadChargerPhoto(context: Context, chargerId: String, imageUri: Uri) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val chargerPhotoImagesRef = storageRef.child("images/$chargerId/photo_$chargerId.jpg")

        // read bytes from Uri
        val photoBytes = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
            inputStream.readBytes()
        } ?: throw IllegalArgumentException("Unable to open input stream from URI")

        try {
            // Upload bytes to Firebase Storage, suspending until done
            chargerPhotoImagesRef.putBytes(photoBytes).await()
            Log.d("uploadChargerPhoto", "Upload successful for chargerId=$chargerId")
        } catch (e: Exception) {
            Log.e("uploadChargerPhoto", "Upload failed for chargerId=$chargerId", e)
            throw e  // rethrow or handle as needed
        }
    }

    suspend fun downloadChargerPhoto(chargerId: String) : ByteArray? {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val chargerPhotoImagesRef = storageRef.child("images/$chargerId/photo_$chargerId.jpg")

        val maxDownloadSizeBytes = 5 * 1024 * 1024L

        try {
            val photoBytes = chargerPhotoImagesRef.getBytes(maxDownloadSizeBytes).await()
            return photoBytes
        } catch (e: StorageException) {
            // charger has no photo
            return null
        }
    }
}


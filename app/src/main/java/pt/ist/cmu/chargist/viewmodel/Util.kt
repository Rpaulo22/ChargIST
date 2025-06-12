package pt.ist.cmu.chargist.viewmodel

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import org.imperiumlabs.geofirestore.GeoFirestore
import pt.ist.cmu.chargist.model.data.Charger
import pt.ist.cmu.chargist.model.data.ChargerDao
import pt.ist.cmu.chargist.model.data.ChargingSlot
import pt.ist.cmu.chargist.model.data.ChargingSlotDao
import pt.ist.cmu.chargist.model.firebase.queryDocumentsAtLocation

suspend fun reloadChargersOnLocation(center: GeoPoint, radius: Double, chargerDao: ChargerDao, slotDao: ChargingSlotDao) {
    Log.d("GeoFirestore", "Updating chargers in a ${radius}km radius around $center")
    // Atualizar carregadores em torno da localização
    val db = Firebase.firestore
    val chargerRef = db.collection("Charger")
    val geoFirestore = GeoFirestore(chargerRef)

    try {
        val res = geoFirestore.queryDocumentsAtLocation(center, radius)
        Log.d("GeoFirestore", "Geo query results = $res")

        // atualizar BD local
        val chargers = chargerDao.getEveryCharger()

        // delete old values in radius
        for (charger in chargers) {
            if (calcDistance(
                    LatLng(center.latitude,center.longitude),
                    LatLng(charger.latitude, charger.longitude)
                ) <= radius
            ) {
                slotDao.deleteChargingSlots(charger.chargingSlots)
                chargerDao.deleteCharger(charger)
            }
        }

        // put updated values in
        for (snapshot in res) {
            val id = snapshot.id
            val cash = snapshot.get("cash") as Boolean
            val chargingSlots = snapshot.get("chargingSlots") as List<String>
            val creditCard = snapshot.get("creditCard") as Boolean
            val location = snapshot.get("location") as GeoPoint
            val mbWay = snapshot.get("mbWay") as Boolean
            val name = snapshot.get("name") as String
            val ownerId = snapshot.get("ownerId") as String
            val price = snapshot.get("price") as Map<String, Number>
            val ratings = snapshot.get("ratings") as Map<String, Double>
            val ratingsMean = snapshot.get("ratingsMean") as Double

            Log.d("Geo Reload", "Loading charger $name into Room")

            val c = Charger(
                id,
                name,
                ownerId,
                chargingSlots,
                creditCard,
                cash,
                mbWay,
                location.latitude,
                location.longitude,
                price.get("fast")!!.toDouble(),
                price.get("medium")!!.toDouble(),
                price.get("slow")!!.toDouble(),
                ratings,
                ratingsMean
            )
            chargerDao.insertCharger(c)

            if (c.chargingSlots.isNotEmpty()) {
                val slots = db.collection("ChargingSlot")
                    .whereIn(FieldPath.documentId(), c.chargingSlots)
                    .get()
                    .await()
                for (document in slots) {
                    val slot = ChargingSlot(
                        id = document.id,
                        speed = document.data["speed"].toString(),
                        type = document.data["type"].toString(),
                        occupiedUntil = document.data["occupiedUntil"] as Long,
                        occupiedBy = document.data["occupiedBy"].toString()
                    )

                    slotDao.insertChargingSlot(slot)
                }
            }
        }

    } catch (e : Exception) {
        Log.d("GeoFirestore", "Geo query failed with exception: $e")
        e.printStackTrace()
    }
}

// function that verifies if client has connection to internet
@Composable
fun connectionStatus(): Boolean {
    val context = LocalContext.current
    val connected = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connected.value = true
            }

            override fun onLost(network: Network) {
                connected.value = false
            }
        }

        connectivityManager.registerDefaultNetworkCallback(callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return connected.value
}

// function that checks if client is using mobile data
fun isUsingMobileData(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

    val activeNetwork = connectivityManager.activeNetwork
    if (activeNetwork == null) {
        Log.d("NetworkCheck", "No active network")
        return false
    }

    val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
    if (networkCapabilities == null) {
        Log.d("NetworkCheck", "No network capabilities")
        return false
    }

    val isMobile = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    Log.d("NetworkCheck", "Is mobile data: $isMobile")
    return isMobile
}

fun calcDistance(loc1: LatLng, loc2: LatLng): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        loc1.latitude, loc1.longitude,
        loc2.latitude, loc2.longitude, results
    )

    return results[0]/1000 // result in km
}

fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun CharSequence?.isValidUsername() = !isNullOrEmpty() && this.length >= 3

fun CharSequence?.isValidPassword() = !isNullOrEmpty() && this.length >= 6
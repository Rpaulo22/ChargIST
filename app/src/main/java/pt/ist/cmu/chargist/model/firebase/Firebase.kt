package pt.ist.cmu.chargist.model.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.suspendCancellableCoroutine
import org.imperiumlabs.geofirestore.GeoFirestore
import org.imperiumlabs.geofirestore.listeners.GeoQueryDataEventListener
import kotlin.coroutines.resumeWithException

suspend fun GeoFirestore.queryDocumentsAtLocation(
    center: GeoPoint,
    radius: Double
): List<DocumentSnapshot> = suspendCancellableCoroutine { cont ->

    val results = mutableListOf<DocumentSnapshot>()
    val geoQuery = this.queryAtLocation(center, radius)

    val listener = object : GeoQueryDataEventListener {
        override fun onDocumentEntered(documentSnapshot: DocumentSnapshot, location: GeoPoint) {
            Log.d("GeoFirebase", "Got document $documentSnapshot")
            results.add(documentSnapshot)
        }

        override fun onGeoQueryReady() {
            Log.d("GeoFirebase", "Got results $results")
            if (cont.isActive) {
                cont.resume(results, onCancellation = {cause, value, context -> })
            }
            geoQuery.removeAllListeners()
        }

        override fun onGeoQueryError(exception: Exception) {
            if (cont.isActive) {
                cont.resumeWithException(exception)
            }
            geoQuery.removeAllListeners()
        }

        override fun onDocumentExited(documentSnapshot: DocumentSnapshot) {}
        override fun onDocumentMoved(documentSnapshot: DocumentSnapshot, location: GeoPoint) {}
        override fun onDocumentChanged(documentSnapshot: DocumentSnapshot, location: GeoPoint) {}
    }

    geoQuery.addGeoQueryDataEventListener(listener)

    cont.invokeOnCancellation {
        geoQuery.removeAllListeners()
    }
}
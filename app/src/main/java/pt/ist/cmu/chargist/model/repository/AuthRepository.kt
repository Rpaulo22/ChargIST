package pt.ist.cmu.chargist.model.repository

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import pt.ist.cmu.chargist.model.data.Auth
import javax.inject.Inject

class AuthRepository (
    private val authData: Auth
) {
    val currentUser: FirebaseUser? = authData.currentUser
    val currentUserIdFlow: Flow<String?> = authData.currentUserIdFlow

    suspend fun createGuestAccount() {
        authData.createGuestAccount()
    }

    fun isGuest(): Boolean {
        return authData.isGuest()
    }

    suspend fun signIn(email: String, password: String) {
        if (isGuest()) {
            deleteAccount()
        }
        authData.signIn(email, password)
    }

    suspend fun signUp(email: String, password: String) {
        authData.linkAccount(email, password)
    }

    fun signOut() {
        authData.signOut()
    }

    suspend fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user!!.uid
        val db = Firebase.firestore
        // Delete user document
        db.collection("User")
            .document(uid)
            .delete()
            .await()
        // Query all chargers owned by user
        val chargersQuerySnapshot = db.collection("Charger")
            .whereEqualTo("ownerId", uid)
            .get()
            .await()
        // Delete each charger document one by one
        chargersQuerySnapshot.documents.forEach { doc ->
            db.collection("Charger")
                .document(doc.id)
                .delete()
                .await()
        }

        authData.deleteAccount()
    }
}
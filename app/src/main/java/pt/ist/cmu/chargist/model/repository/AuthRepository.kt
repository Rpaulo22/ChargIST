package pt.ist.cmu.chargist.model.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import pt.ist.cmu.chargist.model.data.Auth

class AuthRepository (
    private val authData: Auth
) {
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

        authData.deleteAccount()
    }
}
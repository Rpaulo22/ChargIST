package pt.ist.cmu.chargist.model.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
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

    fun isGuest() : Boolean {
        return authData.isGuest()
    }

    suspend fun signIn(email: String, password: String) {
        // Save the guest user
        val currentUser = FirebaseAuth.getInstance().currentUser
        val wasGuest = currentUser?.isAnonymous == true
        try {
            authData.signIn(email, password)
            // Only delete guest user if the login was successful
            if (wasGuest) {
                currentUser.delete().await()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun signUp(email: String, password: String) {
        authData.linkAccount(email, password)
    }

    fun signOut() {
        authData.signOut()
    }

    suspend fun deleteAccount() {
        authData.deleteAccount()
    }
}
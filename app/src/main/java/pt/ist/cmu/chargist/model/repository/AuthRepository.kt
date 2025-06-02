package pt.ist.cmu.chargist.model.repository

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
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

    suspend fun signIn(email: String, password: String) {
        authData.signIn(email, password)
    }

    suspend fun signUp(email: String, password: String) {
        authData.linkAccount(email, password)
        authData.createGuestAccount()
    }

    fun signOut() {
        authData.signOut()
    }

    suspend fun deleteAccount() {
        authData.deleteAccount()
    }
}
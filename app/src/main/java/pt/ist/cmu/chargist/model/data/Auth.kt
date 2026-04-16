package pt.ist.cmu.chargist.model.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class Auth @Inject constructor(private val auth: FirebaseAuth) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun createGuestAccount() {
        auth.signInAnonymously().await()
    }

    fun isGuest(): Boolean {
        return auth.currentUser?.isAnonymous == true
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun linkAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.linkWithCredential(credential).await() // null pointer exception thrown here
    }

    fun signOut() {
        if (auth.currentUser!!.isAnonymous) {
            auth.currentUser!!.delete()
        }
        auth.signOut()
    }

    suspend fun deleteAccount() {
        auth.currentUser!!.delete().await()
    }
}
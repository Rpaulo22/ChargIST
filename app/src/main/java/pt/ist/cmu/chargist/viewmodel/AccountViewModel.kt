package pt.ist.cmu.chargist.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Auth
import pt.ist.cmu.chargist.model.data.User
import pt.ist.cmu.chargist.model.repository.AuthRepository
import pt.ist.cmu.chargist.model.repository.UserRepository

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository: AuthRepository
    private val userRepository: UserRepository
    var currentUser = MutableStateFlow<User?>(null)

    init {
        val firebaseAuth = FirebaseAuth.getInstance()
        val auth = Auth(firebaseAuth)
        authRepository = AuthRepository(auth)
        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user!!.uid
        viewModelScope.launch {
            currentUser.value = userRepository.getUserById(uid)
        }
    }

    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    fun isGuest() : Boolean {
        return authRepository.isGuest()
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // if user is a guest, don't lose access to the guest account
                if (!isGuest()) {
                    authRepository.signOut()
                }
                _shouldRestartApp.value = true
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Caught an exception: $e")
            }
        }
    }

    fun deleteAccount() {
        try {
            viewModelScope.launch {
                authRepository.deleteAccount()
                userRepository.delete(currentUser.value!!)
            }
            _shouldRestartApp.value = true
            Log.d("AccountViewModel", "Successfully deleted user account")
        } catch (e: Exception) {
            Log.e("AccountViewModel", "Caught an exception: $e")
        }
    }
}
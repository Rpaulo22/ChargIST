package pt.ist.cmu.chargist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ist.cmu.chargist.model.data.Auth
import pt.ist.cmu.chargist.model.repository.AuthRepository

class AccountViewModel() : ViewModel() {
    private val authRepository: AuthRepository

    init {
        val firebaseAuth = FirebaseAuth.getInstance()
        val auth = Auth(firebaseAuth)
        authRepository = AuthRepository(auth)
    }

    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean>
        get() = _shouldRestartApp.asStateFlow()

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _shouldRestartApp.value = true
            } catch (e: Exception) {
                // TODO: handle
                Log.e("AccountViewModel", "Caught an exception: $e")
            }
        }
    }
}
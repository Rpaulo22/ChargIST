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

class LoginViewModel : ViewModel() {

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean>
        get() = _loginSuccess.asStateFlow()

    private val _loginFailure = MutableStateFlow(false)
    val loginFailure: StateFlow<Boolean>
        get() = _loginFailure.asStateFlow()

    private val _isLoadingUser = MutableStateFlow(true)
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()

    private val authRepository: AuthRepository

    init {
        val firebaseAuth = FirebaseAuth.getInstance()
        val auth = Auth(firebaseAuth)
        authRepository = AuthRepository(auth)
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                if (authRepository.currentUser == null) {
                    authRepository.createGuestAccount()
                }

                _isLoadingUser.value = false
            } catch (e: Exception) {
                // TODO: handle
                Log.e("LoginViewModel", "loadCurrentUser(): caught an exception: $e")
            }
        }
    }

    fun signIn(
        email: String,
        password: String,
    ) {
        viewModelScope.launch {
            try {
                // TODO: add proper sanitization and verification
                authRepository.signIn(email, password)
                _loginSuccess.value = true
            } catch (e: Exception) {
                // TODO: handle
                _loginFailure.value = true
                Log.e("LoginViewModel", "signIn(): caught an exception: $e")
            }
        }
    }
}
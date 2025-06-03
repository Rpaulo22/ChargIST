package pt.ist.cmu.chargist.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ist.cmu.chargist.model.data.Auth
import pt.ist.cmu.chargist.model.repository.AuthRepository

class RegisterViewModel () : ViewModel() {
    private val authRepository: AuthRepository

    init {
        val firebaseAuth = FirebaseAuth.getInstance()
        val auth = Auth(firebaseAuth)
        authRepository = AuthRepository(auth)
    }

    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean> get() = _shouldRestartApp.asStateFlow()

    private val _invalidEmail = MutableStateFlow(false)
    val invalidEmail: StateFlow<Boolean> get() = _invalidEmail.asStateFlow()

    private val _invalidPassword = MutableStateFlow(false)
    val invalidPassword: StateFlow<Boolean> get() = _invalidPassword.asStateFlow()

    private val _invalidRepeatPassword = MutableStateFlow(false)
    val invalidRepeatPassword: StateFlow<Boolean> get() = _invalidRepeatPassword.asStateFlow()

    fun signUp(
        email: String,
        password: String,
        repeatPassword: String,
    ) {
        viewModelScope.launch {
            try {
                if (!email.isValidEmail()) {
                    _invalidEmail.value = true
                    return@launch
                }
                _invalidEmail.value = false
                if (!password.isValidPassword()) {
                    _invalidPassword.value = true
                    return@launch
                }
                _invalidPassword.value = false
                if (repeatPassword != password) {
                    _invalidRepeatPassword.value = true
                    return@launch
                }
                _invalidRepeatPassword.value = false

                authRepository.signUp(email, password)
                _shouldRestartApp.value = true
            } catch (e: Exception) {
                // TODO: handle
                Log.e("RegisterViewModel", "Caught an exception: $e")
            }
        }
    }

    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun CharSequence?.isValidPassword() = !isNullOrEmpty() && this.length >= 6
}
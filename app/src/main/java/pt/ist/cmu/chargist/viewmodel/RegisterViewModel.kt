package pt.ist.cmu.chargist.viewmodel

import android.R.attr.phoneNumber
import android.app.Application
import android.util.Log
import android.util.Patterns
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

class RegisterViewModel (application: Application) : AndroidViewModel(application) {
    private val authRepository: AuthRepository
    private val userRepository: UserRepository

    init {
        val firebaseAuth = FirebaseAuth.getInstance()
        val auth = Auth(firebaseAuth)
        authRepository = AuthRepository(auth)

        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    private val _shouldRestartApp = MutableStateFlow(false)
    val shouldRestartApp: StateFlow<Boolean> get() = _shouldRestartApp.asStateFlow()

    private val _invalidEmail = MutableStateFlow(false)
    val invalidEmail: StateFlow<Boolean> get() = _invalidEmail.asStateFlow()

    private val _invalidUsername = MutableStateFlow(false)
    val invalidUsername: StateFlow<Boolean> get() = _invalidUsername.asStateFlow()

    private val _invalidPassword = MutableStateFlow(false)
    val invalidPassword: StateFlow<Boolean> get() = _invalidPassword.asStateFlow()

    private val _invalidRepeatPassword = MutableStateFlow(false)
    val invalidRepeatPassword: StateFlow<Boolean> get() = _invalidRepeatPassword.asStateFlow()

    private val _isSigningUp = MutableStateFlow(false)
    val isSigningUp: StateFlow<Boolean> get() = _isSigningUp.asStateFlow()

    fun signUp(
        email: String,
        username: String,
        phoneNumber: String,
        password: String,
        repeatPassword: String,
    ) {
        if (_isSigningUp.value) return
        viewModelScope.launch {
            _isSigningUp.value = true
            try {
                if (!email.isValidEmail()) {
                    _invalidEmail.value = true
                    _isSigningUp.value = false
                    return@launch
                }
                _invalidEmail.value = false
                if (!username.isValidUsername()) {
                    _invalidUsername.value = true
                    _isSigningUp.value = false
                    return@launch
                }
                _invalidUsername.value = false
                if (!password.isValidPassword()) {
                    _invalidPassword.value = true
                    _isSigningUp.value = false
                    return@launch
                }
                _invalidPassword.value = false
                if (repeatPassword != password) {
                    _invalidRepeatPassword.value = true
                    _isSigningUp.value = false
                    return@launch
                }
                _invalidRepeatPassword.value = false

                authRepository.createGuestAccount()

                // add User to Firestore
                val user = FirebaseAuth.getInstance().currentUser
                val uid = user!!.uid

                // if user was a guest before, keep his favorite chargers
                val favoriteChargers = userRepository.getUserById(uid)?.favoriteChargers ?: mutableListOf<String>()

                val userData = hashMapOf(
                    "email" to email,
                    "username" to username,
                    "phoneNumber" to phoneNumber,
                    "favoriteChargers" to favoriteChargers,
                )
                val db = Firebase.firestore
                db.collection("User")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        // add User to local db
                        val user = User(
                            uid,
                            email,
                            username,
                            phoneNumber,
                            favoriteChargers,
                        )
                        viewModelScope.launch {
                            userRepository.insert(user)
                        }
                        Log.d("RegisterViewModel", "User created with ID: $uid")
                    }
                    .addOnFailureListener { e ->
                        Log.e("RegisterViewModel", "Error creating user", e)
                    }

                authRepository.signUp(email, password)

                _isSigningUp.value = false
                _shouldRestartApp.value = true
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Caught an exception: $e")
            }
        }
    }
}
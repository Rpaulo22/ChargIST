package pt.ist.cmu.chargist.viewmodel

import android.R.attr.phoneNumber
import android.app.Application
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.ist.cmu.chargist.model.data.AppDatabase
import pt.ist.cmu.chargist.model.data.Auth
import pt.ist.cmu.chargist.model.data.User
import pt.ist.cmu.chargist.model.repository.AuthRepository
import pt.ist.cmu.chargist.model.repository.UserRepository

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean>
        get() = _loginSuccess.asStateFlow()

    private val _loginFailure = MutableStateFlow(false)
    val loginFailure: StateFlow<Boolean>
        get() = _loginFailure.asStateFlow()

    private val _isLoadingUser = MutableStateFlow(true)
    val isLoadingUser: StateFlow<Boolean>
        get() = _isLoadingUser.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn: StateFlow<Boolean> get() = _isLoggingIn.asStateFlow()

    private val _isLoggingInAsGuest = MutableStateFlow(false)
    val isLoggingInAsGuest: StateFlow<Boolean> get() = _isLoggingInAsGuest.asStateFlow()

    private val authRepository: AuthRepository
    private val userRepository: UserRepository

    val user get() = Firebase.auth.currentUser

    init {
        val firebaseAuth = FirebaseAuth.getInstance()
        val auth = Auth(firebaseAuth)
        authRepository = AuthRepository(auth)

        val userDao = AppDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                if (user != null && !authRepository.isGuest()) {
                    _loginSuccess.value = true
                }
                _isLoadingUser.value = false
            } catch (e: Exception) {
                Log.e("LoginViewModel", "loadCurrentUser(): caught an exception: $e")
            }
        }
    }

    fun signIn(
        email: String,
        password: String,
    ) {
        if (_isLoggingIn.value) return
        viewModelScope.launch {
            _isLoggingIn.value = true
            try {
                if (!email.isValidEmail()) {
                    throw IllegalArgumentException("Invalid email format")
                }
                if (!password.isValidPassword()) {
                    throw IllegalArgumentException("Invalid password format")
                }
                authRepository.signIn(email, password)
                _isLoggingIn.value = false
                _loginSuccess.value = true
            } catch (e: Exception) {
                _isLoggingIn.value = false
                _loginFailure.value = true
                Log.e("LoginViewModel", "signIn(): caught an exception: $e")
            }
        }
    }

    fun continueAsGuest(
        username: String
    ) {
        if (_isLoggingInAsGuest.value) return
        viewModelScope.launch {
            _isLoggingInAsGuest.value = true
            try {
                // if username is "" it means that the guest account has already been created
                if (username != "") {
                    authRepository.createGuestAccount()
                    addUserToDatabase("", username, "")
                }
                _isLoggingInAsGuest.value = false
                _loginSuccess.value = true
            } catch (e: Exception) {
                _isLoggingInAsGuest.value = false
                Log.e("LoginViewModel", "continueAsGuest(): caught an exception: $e")
            }
        }
    }

    fun addUserToDatabase(
        email: String,
        username: String,
        phoneNumber: String,
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user!!.uid
        val userData = hashMapOf(
            "email" to email,
            "username" to username,
            "phoneNumber" to phoneNumber,
            "favoriteChargers" to arrayListOf<String>(),
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
                    mutableListOf<String>()
                )
                viewModelScope.launch {
                    userRepository.insert(user)
                }
                Log.d("Firestore", "User created with ID: $uid")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error creating user", e)
            }
    }
}
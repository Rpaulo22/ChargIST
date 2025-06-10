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

    private val _invalidPassword = MutableStateFlow(false)
    val invalidPassword: StateFlow<Boolean> get() = _invalidPassword.asStateFlow()

    private val _invalidRepeatPassword = MutableStateFlow(false)
    val invalidRepeatPassword: StateFlow<Boolean> get() = _invalidRepeatPassword.asStateFlow()

    fun signUp(
        email: String,
        username: String,
        phoneNumber: String,
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

                authRepository.createGuestAccount()

                // add User to Firestore
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
                            emptyList(),
                        )
                        viewModelScope.launch {
                            userRepository.insert(user)
                        }
                        Log.d("Firestore", "User created with ID: $uid")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error creating user", e)
                    }

                authRepository.signUp(email, password)

                _shouldRestartApp.value = true
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Caught an exception: $e")
            }
        }
    }

    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun CharSequence?.isValidPassword() = !isNullOrEmpty() && this.length >= 6
}
package pt.ist.cmu.chargist.ui.screens

import android.graphics.Color.red
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ist.cmu.chargist.R
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor
import pt.ist.cmu.chargist.viewmodel.RegisterViewModel
import kotlin.math.sign

@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel,
    goToLoginScreen: () -> Unit,
) {
    val context = LocalContext.current
    val invalidEmail by registerViewModel.invalidEmail.collectAsStateWithLifecycle()
    val invalidUsername by registerViewModel.invalidUsername.collectAsStateWithLifecycle()
    val invalidPassword by registerViewModel.invalidPassword.collectAsStateWithLifecycle()
    val invalidRepeatPassword by registerViewModel.invalidRepeatPassword.collectAsStateWithLifecycle()
    val shouldRestartApp by registerViewModel.shouldRestartApp.collectAsStateWithLifecycle()
    if (shouldRestartApp) {
        Toast.makeText(context, "Registered successfully", Toast.LENGTH_SHORT).show()
        goToLoginScreen()
    } else {
        RegisterScreenContent(
            signUp = registerViewModel::signUp,
            invalidEmail = invalidEmail,
            invalidUsername = invalidUsername,
            invalidPassword = invalidPassword,
            invalidRepeatPassword = invalidRepeatPassword,
        )
    }

}

@Composable
private fun RegisterScreenContent (
    signUp: (String, String, String, String, String) -> Unit,
    invalidEmail: Boolean,
    invalidUsername: Boolean,
    invalidPassword: Boolean,
    invalidRepeatPassword: Boolean,
) {

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    val visualTransformation = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }
    var showPasswordConfirm by remember { mutableStateOf(false) }
    val visualTransformationConfirm = if (showPasswordConfirm) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    Column (
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(60.dp)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.chargist_without_text),
            contentDescription = "logo",
            modifier = Modifier.size(100.dp),
        )
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = "Register",
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.size(20.dp))

        Text(
            text = "Personal Information",
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.size(10.dp))
        // Email
        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            label = {Text("Email")},
        )
        if (invalidEmail) {
            Text(
                text = "Invalid email",
                color = Color.Red,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.Start),
            )
        }
        else {
            Spacer(modifier = Modifier.size(10.dp))
        }
        // Username
        OutlinedTextField(
            value = username,
            onValueChange = {username = it},
            singleLine = true,
            label = {Text("Username")},
        )
        if (invalidUsername) {
            Text(
                text = "Invalid Username (must have at least 3 characters)",
                color = Color.Red,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.Start),
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        // Phone
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {phoneNumber = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            label = {Text("Phone Number (optional)")},
        )
        Spacer(modifier = Modifier.size(20.dp))

        Text(
            text = "Password",
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.size(10.dp))
        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            singleLine = true,
            label = {Text("Password")},
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) {
                            Icons.Default.Visibility
                        }
                        else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = "Toggle password visibility",
                    )
                }
            }
        )
        if (invalidPassword) {
            Text(
                text = "Invalid Password (must have at least 6 characters)",
                color = Color.Red,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.Start),
            )
        }
        else {
            Spacer(modifier = Modifier.size(10.dp))
        }
        // Confirm Password
        OutlinedTextField(
            value = passwordConfirm,
            onValueChange = {passwordConfirm = it},
            singleLine = true,
            label = {Text("Confirm Password")},
            visualTransformation = visualTransformationConfirm,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPasswordConfirm = !showPasswordConfirm }) {
                    Icon(
                        if (showPasswordConfirm) {
                            Icons.Default.Visibility
                        }
                        else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = "Toggle password visibility",
                    )
                }
            }
        )
        if (invalidRepeatPassword) {
            Text(
                text = "Passwords don't match",
                color = Color.Red,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.Start),
            )
        }
        else {
            Spacer(modifier = Modifier.size(10.dp))
        }
        Spacer(modifier = Modifier.size(20.dp))
        OutlinedButton(
            onClick = {
                signUp(
                    email,
                    username,
                    phoneNumber,
                    password,
                    passwordConfirm,
                )
            },
            colors = ButtonColors(Color.Transparent, mainColor, Color.Transparent, Color.LightGray),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, mainColor)
        ) { Text("Register") }
    }
}


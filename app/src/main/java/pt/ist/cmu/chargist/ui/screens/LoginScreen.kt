package pt.ist.cmu.chargist.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import pt.ist.cmu.chargist.R
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor
import pt.ist.cmu.chargist.viewmodel.LoginViewModel
import pt.ist.cmu.chargist.viewmodel.connectionStatus
import pt.ist.cmu.chargist.viewmodel.isValidUsername

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    goToHomeScreen: () -> Unit,
    goToRegisterScreen: () -> Unit,
) {
    val context = LocalContext.current
    val connected = connectionStatus()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val visualTransformation = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

    LaunchedEffect(true) {
        loginViewModel.loadCurrentUser()
    }

    val loginSuccess by loginViewModel.loginSuccess.collectAsState()
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            goToHomeScreen()
        }
    }
    val loginFailure by loginViewModel.loginFailure.collectAsStateWithLifecycle()

    var showGuestUsernameDialog by remember { mutableStateOf(false) }

    Column (
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.chargist),
            contentDescription = "logo"
        )

        Text(
            text = "Welcome to ChargIST",
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.size(20.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            label = {Text("Email")},
        )
        Spacer(modifier = Modifier.size(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            label = {Text("Password")},
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
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
        if (loginFailure) {
            Text(
                text = "Incorrect email or password",
                color = Color.Red,
                fontSize = 10.sp,
            )
        }
        else {
            Spacer(modifier = Modifier.size(20.dp))
        }
        OutlinedButton(
            onClick = {
                if (connected) loginViewModel.signIn(email, password)
                else Toast.makeText(context, "Please connect to the internet to log in", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonColors(Color.Transparent, mainColor, Color.Transparent, Color.LightGray),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, mainColor)
        ) { Text("Log In") }
        Spacer(modifier = Modifier.size(5.dp))
        OutlinedButton(
            onClick = {
                if (loginViewModel.user == null) {
                    if (connected) showGuestUsernameDialog = true
                    else Toast.makeText(context, "Please connect to the internet to create guest account", Toast.LENGTH_SHORT).show()
                } else {
                    loginViewModel.continueAsGuest("")
                }
            },
            colors = ButtonColors(Color.Transparent, mainColor, Color.Transparent, Color.LightGray),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, mainColor)
        ) { Text("Continue as Guest") }
        Spacer(Modifier.size(5.dp))
        TextButton(

            onClick = {
                if (connected) goToRegisterScreen()
                else Toast.makeText(context, "Please connect to the internet to register", Toast.LENGTH_SHORT).show()},
        ) { Text("Create Account") }
    }
    if (showGuestUsernameDialog) {
        GuestUsernameDialog(
            onDismiss = { showGuestUsernameDialog = false },
            loginViewModel = loginViewModel
        )
    }
}

@Composable
fun GuestUsernameDialog(
    onDismiss: () -> Unit,
    loginViewModel: LoginViewModel
) {
    var username by remember { mutableStateOf("") }
    var invalidUsername by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Choose an Username:")
        },
        text = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
            ) {
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (username.isValidUsername()) {
                        loginViewModel.continueAsGuest(username)
                    }
                    else {
                        invalidUsername = true
                    }
                }) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }) {
                Text("Back")
            }
        }
    )
}
package pt.ist.cmu.chargist.ui.screens

import android.R.attr.contentDescription
import android.R.attr.visibility
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ist.cmu.chargist.R
import pt.ist.cmu.chargist.appColor

@Composable
fun LoginScreen(
    onLoginClick: (String) -> Unit
) {
    var userId = "abcd1234"
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state = remember { TextFieldState() }
    var showPassword by remember { mutableStateOf(false) }
    val visualTransformation = if (showPassword) {
        VisualTransformation.None
    } else {
        PasswordVisualTransformation()
    }

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
            value = user,
            onValueChange = {user = it},
            label = {Text("Username")},
        )
        Spacer(modifier = Modifier.size(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
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
        Spacer(modifier = Modifier.size(10.dp))
        OutlinedButton(
            onClick = {onLoginClick(userId)},
            colors = ButtonColors(Color.Transparent, appColor, Color.Transparent, Color.LightGray),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, appColor)
        ) { Text("Login") }
        Spacer(Modifier.size(5.dp))
        TextButton(
            onClick = {Log.d("HomePage","User: $user and password: $password")}, // log for user credentials
        ) { Text("Create Account") }
    }
}
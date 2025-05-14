package pt.ist.cmu.chargist.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.ist.cmu.chargist.R
import pt.ist.cmu.chargist.appColor

@Composable
fun LoginScreen(
    onLoginClick: (Int) -> Unit
) {
    var userId = 1
    var user by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


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
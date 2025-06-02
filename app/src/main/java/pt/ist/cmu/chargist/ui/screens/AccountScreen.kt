package pt.ist.cmu.chargist.ui.screens

import android.R.attr.text
import android.R.attr.thickness
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ist.cmu.chargist.viewmodel.AccountViewModel
import pt.ist.cmu.chargist.viewmodel.AppViewModel

@Composable
fun AccountScreen(
    accountViewModel: AccountViewModel,
    userId: String,
    goToLoginScreen: () -> Unit,
    goToRegisterScreen: () -> Unit,
    goToHomeScreen: (String) -> Unit,
    appViewModel: AppViewModel = viewModel()
) {
    val context = LocalContext.current
    val shouldRestartApp by accountViewModel.shouldRestartApp.collectAsStateWithLifecycle()
    if (shouldRestartApp) {
        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
        goToLoginScreen()
    } else {
        AccountScreenContent(
            userId = userId,
            isGuest = accountViewModel::isGuest,
            signOut = accountViewModel::signOut,
            goToHomeScreen = goToHomeScreen,
            goToRegisterScreen = goToRegisterScreen,
        )
    }
}

@Composable
fun AccountScreenContent (
    userId: String,
    isGuest: () -> Boolean,
    signOut: () -> Unit,
    goToRegisterScreen: () -> Unit,
    goToHomeScreen: (String) -> Unit,
) {
    Scaffold (
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { goToHomeScreen(userId) },
                        modifier = Modifier.size(96.dp)
                    ) {
                        Column (
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                            Text(text = "Home")
                        }
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(96.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                            Text(text = "Account")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            item {
                if (isGuest()) {
                    Text(text = "You're a guest", fontSize = 24.sp)
                }
                Text(text = "User $userId", fontSize = 24.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = "Random Row 1")
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = "Random Row 2")
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                if (isGuest()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                    ) {
                        Text(text = "Sign In")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { goToRegisterScreen() }
                    ) {
                        Text(text = "Sign Up")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { signOut() }
                ) {
                    Text(text = "Logout")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
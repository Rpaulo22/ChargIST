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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ist.cmu.chargist.model.data.User
import pt.ist.cmu.chargist.ui.elements.BottomNavigationBar
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor
import pt.ist.cmu.chargist.viewmodel.AccountViewModel
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import pt.ist.cmu.chargist.ui.theme.AppColors.mainColor

@Composable
fun AccountScreen(
    accountViewModel: AccountViewModel,
    goToLoginScreen: () -> Unit,
    goToRegisterScreen: () -> Unit,
    goToHomeScreen: () -> Unit,
    goToSearchScreen: () -> Unit,
    appViewModel: AppViewModel = viewModel()
) {
    val currentUser by appViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val shouldRestartApp by accountViewModel.shouldRestartApp.collectAsStateWithLifecycle()
    if (shouldRestartApp) {
        goToLoginScreen()
    } else {
        AccountScreenContent(
            currentUser = currentUser,
            isGuest = accountViewModel::isGuest,
            signOut = accountViewModel::signOut,
            deleteAccount = accountViewModel::deleteAccount,
            goToHomeScreen = goToHomeScreen,
            goToLoginScreen = goToLoginScreen,
            goToRegisterScreen = goToRegisterScreen,
            goToSearchScreen = goToSearchScreen,
        )
    }
}

@Composable
private fun AccountScreenContent (
    currentUser: User?,
    isGuest: () -> Boolean,
    signOut: () -> Unit,
    deleteAccount: () -> Unit,
    goToLoginScreen: () -> Unit,
    goToRegisterScreen: () -> Unit,
    goToHomeScreen: () -> Unit,
    goToSearchScreen: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold (
        bottomBar = {
            BottomNavigationBar(
                onHomeClick = goToHomeScreen,
                onSearchClick = goToSearchScreen,
                currentScreen = "Account"
            )
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = 24.dp,
                    end = 24.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column (
            ) {
                if (isGuest()) {
                    Text(
                        text = "You're a guest",
                        modifier = Modifier.padding(4.dp),
                        fontSize = 24.sp
                    )
                }
                Text(
                    text = buildAnnotatedString {
                        append("Welcome, ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = mainColor
                            )
                        ) {
                            append(currentUser?.name ?: "")
                        }
                    },
                    modifier = Modifier.padding(4.dp),
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isGuest()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable { goToLoginScreen() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Log in",
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    HorizontalDivider(thickness = 1.dp)
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable { goToRegisterScreen() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Register",
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    HorizontalDivider(thickness = 1.dp)
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { signOut() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Out",
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable { showDeleteDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Delete Account",
                    modifier = Modifier.padding(end = 4.dp),
                    color = Color.Red
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Account") },
                text = { Text("Are you sure you want to permanently delete your account? This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        deleteAccount()
                        showDeleteDialog = false
                    }) {
                        Text("Delete", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
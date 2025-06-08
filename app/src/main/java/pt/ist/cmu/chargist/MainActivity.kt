package pt.ist.cmu.chargist

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import pt.ist.cmu.chargist.ui.screens.HomeScreen
import pt.ist.cmu.chargist.ui.screens.LoginScreen
import pt.ist.cmu.chargist.ui.screens.AccountScreen
import pt.ist.cmu.chargist.ui.screens.CreateChargerForm
import pt.ist.cmu.chargist.ui.screens.RegisterScreen
import pt.ist.cmu.chargist.ui.screens.SearchScreen
import pt.ist.cmu.chargist.ui.theme.ChargISTTheme
import pt.ist.cmu.chargist.viewmodel.AccountViewModel
import pt.ist.cmu.chargist.viewmodel.AppViewModel
import pt.ist.cmu.chargist.viewmodel.LoginViewModel
import pt.ist.cmu.chargist.viewmodel.MapViewModel
import pt.ist.cmu.chargist.viewmodel.RegisterViewModel
import pt.ist.cmu.chargist.viewmodel.SearchViewModel
import kotlin.collections.listOf

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        enableEdgeToEdge()
        setContent {
            ChargISTTheme {
                Surface() {
                    AppNavigation()
                }
            }
        }
    }
}

private fun reload() {
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(
            Screen.Login.route
        ) { backStackEntry ->
            val loginViewModel = viewModel<LoginViewModel>(backStackEntry)
            LoginScreen(
                loginViewModel,
                goToHomeScreen = {
                    navController.navigate(Screen.Home.route)
                },
                goToRegisterScreen = {
                    navController.navigate(Screen.Register.route)
                },
            )
        }
        composable(
            route = Screen.Register.route
        ) { backStackEntry ->
            val registerViewModel = viewModel<RegisterViewModel>(backStackEntry)

            RegisterScreen(
                registerViewModel = registerViewModel,
                goToLoginScreen = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Home.route,
        ) { backStackEntry ->

            // Scope the ViewModel to the "home" route
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }

            // these are the shared view models
            val appViewModel = viewModel<AppViewModel>(parentEntry)
            val mapViewModel = viewModel<MapViewModel>(parentEntry)

            HomeScreen(
                onAccountClick = {
                    navController.navigate(Screen.Account.route)
                },
                onCreateCharger = {
                    navController.navigate(Screen.CreateCharger.route)
                },
                appViewModel = appViewModel,
                mapViewModel = mapViewModel,
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }
        composable(
            route = Screen.Search.route,
        ) { backStackEntry ->

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }

            val searchViewModel = viewModel<SearchViewModel>(parentEntry)
            val mapViewModel = viewModel<MapViewModel>(parentEntry)

            SearchScreen(
                onAccountClick = {
                    navController.navigate(Screen.Account.route)
                },
                onHomeClick = {
                    navController.navigate(Screen.Home.route)
                },
                searchViewModel,
                mapViewModel,
                onResultClick = { centerPoint: LatLng ->
                    navController.navigate(Screen.MapPoint.createRoute(centerPoint))
                }
            )
        }
        composable (
            route = Screen.MapPoint.route,
            arguments = listOf(
                navArgument("lat") { type=NavType.FloatType },
                navArgument("lng") { type=NavType.FloatType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat")
            val lng = backStackEntry.arguments?.getFloat("lng")
            val centerPoint = if (lat == null || lng == null) null else LatLng(lat.toDouble(),lng.toDouble())

            // Scope the ViewModel to the "home" route
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }

            // these are the shared view models
            val appViewModel = viewModel<AppViewModel>(parentEntry)
            val mapViewModel = viewModel<MapViewModel>(parentEntry)

            HomeScreen(
                onAccountClick = {
                    navController.navigate(Screen.Account.route)
                },
                onCreateCharger = {
                    navController.navigate(Screen.CreateCharger.route)
                },
                appViewModel = appViewModel,
                mapViewModel = mapViewModel,
                onSearchClick = {
                    navController.navigate(Screen.Search.route)
                },
                centerPoint = centerPoint
            )
        }
        composable(
            route = Screen.Account.route,
        ) { backStackEntry ->
            val accountViewModel = viewModel<AccountViewModel>(backStackEntry)
            AccountScreen(
                accountViewModel = accountViewModel,
                goToLoginScreen = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                goToRegisterScreen = {
                    navController.navigate(Screen.Register)
                },
                goToHomeScreen = {
                    navController.navigate(Screen.Home.route)
                },
                goToSearchScreen = {
                    navController.navigate(Screen.Search.route)
                }
            )
        }

        composable(Screen.CreateCharger.route) {
            backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val appViewModel = viewModel<AppViewModel>(parentEntry)
            val mapViewModel = viewModel<MapViewModel>(parentEntry)
            CreateChargerForm(
                appViewModel = appViewModel,
                mapViewModel = mapViewModel,
                onCreateClick = {navController.popBackStack()}
            )
        }
    }
}
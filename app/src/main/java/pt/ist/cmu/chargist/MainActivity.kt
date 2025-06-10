package pt.ist.cmu.chargist

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.maps.model.LatLng
import pt.ist.cmu.chargist.ui.screens.HomeScreen
import pt.ist.cmu.chargist.ui.screens.LoginScreen
import pt.ist.cmu.chargist.ui.screens.AccountScreen
import pt.ist.cmu.chargist.ui.screens.ChargerForm
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
import com.google.android.libraries.places.api.Places

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        }
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
                onEditCharger = { id: String ->
                    navController.navigate(Screen.EditCharger.createRoute(id))
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
                onEditCharger = { id: String ->
                    navController.navigate(Screen.EditCharger.createRoute(id))
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
                    navController.navigate(Screen.Register.route)
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
            ChargerForm(
                appViewModel = appViewModel,
                mapViewModel = mapViewModel,
                onCreateClick = {navController.popBackStack()}
            )
        }

        composable (
            route = Screen.EditCharger.route,
            arguments = listOf(
                navArgument("id") { type=NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Home.route)
            }
            val appViewModel = viewModel<AppViewModel>(parentEntry)
            val mapViewModel = viewModel<MapViewModel>(parentEntry)
            ChargerForm(
                appViewModel = appViewModel,
                mapViewModel = mapViewModel,
                onCreateClick = {navController.popBackStack()},
                chargerId = id
            )
        }
    }
}
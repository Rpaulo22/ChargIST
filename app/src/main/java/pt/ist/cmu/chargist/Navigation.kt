package pt.ist.cmu.chargist

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home/{userId}") {
        fun createRoute(userId: String) = "home/$userId"
    }
    object Settings : Screen("settings")
}
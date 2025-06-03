package pt.ist.cmu.chargist

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register") {
        fun createRoute() = "register"
    }
    object Home : Screen("home/{userId}") {
        fun createRoute(userId: String) = "home/$userId"
    }
    object Account : Screen("account/{userId}") {
        fun createRoute(userId: String) = "account/$userId"
    }
    object Settings : Screen("settings")

    object CreateCharger: Screen("createCharger") {
        fun createRoute() = "createCharger"
    }

}
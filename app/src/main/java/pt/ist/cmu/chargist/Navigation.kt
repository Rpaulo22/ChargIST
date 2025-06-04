package pt.ist.cmu.chargist

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Account : Screen("account")
    object Settings : Screen("settings")
    object CreateCharger: Screen("createCharger")
    object Search: Screen("search")

}
package pt.ist.cmu.chargist

import com.google.android.gms.maps.model.LatLng

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object MapPoint : Screen("home/{lat}/{lng}") {
        fun createRoute(centerPoint: LatLng) = "home/${centerPoint.latitude}/${centerPoint.longitude}"
    }
    object Account : Screen("account")
    object Settings : Screen("settings")
    object CreateCharger: Screen("createCharger")
    object EditCharger: Screen("editCharger/{id}") {
        fun createRoute(id: String) = "editCharger/${id}"
    }
    object Search: Screen("search")

}
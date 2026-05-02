package com.project.apppetstore.navigation

import android.net.Uri
import com.project.apppetstore.R

sealed class AppDestination(
    val route: String,
    val titleRes: Int,
    val iconRes: Int? = null,
) {
    data object Home : AppDestination("home", R.string.nav_home, iconRes = R.drawable.ic_house)
    data object Services : AppDestination("services", R.string.nav_services, iconRes = R.drawable.ic_briefcase)
    data object DeliverySchedule : AppDestination(
        "deliverySchedule?serviceId={serviceId}&serviceCategory={serviceCategory}",
        R.string.nav_services
    ) {
        fun createRoute(serviceId: String, serviceCategory: String): String {
            val encodedCategory = Uri.encode(serviceCategory)
            return "deliverySchedule?serviceId=$serviceId&serviceCategory=$encodedCategory"
        }
    }
    data object Map : AppDestination(
        "map?deliveryRequested={deliveryRequested}&serviceCategory={serviceCategory}",
        R.string.nav_map,
        iconRes = R.drawable.ic_map_pin
    ) {
        fun createRoute(deliveryRequested: Boolean, serviceCategory: String? = null): String {
            val encodedCategory = Uri.encode(serviceCategory.orEmpty())
            return "map?deliveryRequested=$deliveryRequested&serviceCategory=$encodedCategory"
        }
    }
    data object Products : AppDestination("products", R.string.nav_products, iconRes = R.drawable.ic_shopping_bag)
    data object Adoption : AppDestination("adoption?petId={petId}", R.string.nav_adoption, iconRes = R.drawable.ic_heart)
    data object Profile : AppDestination("profile", R.string.nav_profile, iconRes = R.drawable.ic_user_round)
    data object Register : AppDestination("register", R.string.login_register)
}

val BottomDestinations = listOf(
    AppDestination.Home,
    AppDestination.Services,
    AppDestination.Products,
    AppDestination.Adoption,
    AppDestination.Profile
)

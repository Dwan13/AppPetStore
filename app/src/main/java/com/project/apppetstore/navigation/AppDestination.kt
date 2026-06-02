package com.project.apppetstore.navigation

import android.net.Uri
import com.project.apppetstore.R

sealed class AppDestination(
    val route: String,
    val titleRes: Int,
    val iconRes: Int? = null,
) {
    data object Home : AppDestination("home", R.string.nav_home, iconRes = R.drawable.ic_house)
    data object Services :
        AppDestination("services", R.string.nav_services, iconRes = R.drawable.ic_briefcase)

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
        "map?deliveryRequested={deliveryRequested}&serviceCategory={serviceCategory}" +
                "&destLat={destLat}&destLng={destLng}&originLat={originLat}&originLng={originLng}",
        R.string.nav_map,
        iconRes = R.drawable.ic_map_pin
    ) {
        fun createRoute(
            deliveryRequested: Boolean,
            serviceCategory: String? = null,
            destLat: Double? = null,
            destLng: Double? = null,
            originLat: Double? = null,
            originLng: Double? = null
        ): String {
            val encodedCategory = Uri.encode(serviceCategory.orEmpty())
            val base = "map?deliveryRequested=$deliveryRequested&serviceCategory=$encodedCategory"
            val latPart = destLat?.let { "&destLat=$it" } ?: ""
            val lngPart = destLng?.let { "&destLng=$it" } ?: ""
            val origLatPart = originLat?.let { "&originLat=$it" } ?: ""
            val origLngPart = originLng?.let { "&originLng=$it" } ?: ""
            return base + latPart + lngPart + origLatPart + origLngPart
        }
    }

    data object Products :
        AppDestination("products", R.string.nav_products, iconRes = R.drawable.ic_shopping_bag)

    data object ProductDetail : AppDestination("product/{productId}", R.string.nav_products) {
        fun createRoute(productId: String) = "product/$productId"
    }

    data object Checkout :
        AppDestination("checkout/{productId}/{quantity}", R.string.nav_products) {
        fun createRoute(productId: String, quantity: Int) = "checkout/$productId/$quantity"
    }

    data object Adoption : AppDestination(
        "adoption?petId={petId}",
        R.string.nav_adoption,
        iconRes = R.drawable.ic_heart
    )

    data object Appointment : AppDestination(
        "appointment?serviceId={serviceId}&serviceCategory={serviceCategory}",
        R.string.nav_services
    ) {
        fun createRoute(serviceId: String, serviceCategory: String): String {
            val encodedCategory = Uri.encode(serviceCategory)
            return "appointment?serviceId=$serviceId&serviceCategory=$encodedCategory"
        }
    }

    data object Profile :
        AppDestination("profile", R.string.nav_profile, iconRes = R.drawable.ic_user_round)

    data object Favorites :
        AppDestination("favorites", R.string.nav_profile, iconRes = R.drawable.ic_heart)

    data object Orders :
        AppDestination("orders", R.string.nav_profile, iconRes = R.drawable.ic_shopping_bag)

    data object Notifications : AppDestination("notifications", R.string.nav_profile)
    data object Settings : AppDestination("settings", R.string.nav_profile)
    data object MisMascotas : AppDestination("mis_mascotas", R.string.nav_profile)
    data object Register : AppDestination("register", R.string.login_register)
    data object Login : AppDestination("login", R.string.nav_profile)
}

val BottomDestinations = listOf(
    AppDestination.Home,
    AppDestination.Services,
    AppDestination.Products,
    AppDestination.Adoption,
    AppDestination.Profile
)

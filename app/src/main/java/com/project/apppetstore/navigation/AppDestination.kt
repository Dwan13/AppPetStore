package com.project.apppetstore.navigation


import com.project.apppetstore.R

sealed class AppDestination(
    val route: String,
    val titleRes: Int,
    val iconRes: Int? = null,
) {
    data object Home : AppDestination("home", R.string.nav_home, iconRes = R.drawable.ic_house)
    data object Services : AppDestination("services", R.string.nav_services, iconRes = R.drawable.ic_briefcase)
    data object Products : AppDestination("products", R.string.nav_products, iconRes = R.drawable.ic_shopping_bag)
    data object Adoption : AppDestination("adoption?petId={petId}", R.string.nav_adoption, iconRes = R.drawable.ic_heart)
    data object Profile : AppDestination("profile", R.string.nav_profile, iconRes = R.drawable.ic_user_round)
}

val BottomDestinations = listOf(
    AppDestination.Home,
    AppDestination.Services,
    AppDestination.Products,
    AppDestination.Adoption,
    AppDestination.Profile
)

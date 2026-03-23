package com.project.apppetstore
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.project.apppetstore.ui.components.AppBottomNavigationBar
import androidx.navigation.compose.currentBackStackEntryAsState
import com.project.apppetstore.navigation.AppDestination
import com.project.apppetstore.navigation.BottomDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPetShopApp() {

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.Home.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AdoPetShop") }
            )
        }, bottomBar = {
            AppBottomNavigationBar(
                currentRoute = currentRoute,
                onTabSelected = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {

            composable("home") {
                navPath("Home") {
                    navController.navigate("profile")
                }
            }

            composable("profile") {
                navPath("Profile") {
                    navController.navigate("home")
                }
            }
        }
    }
}

@Composable
fun navPath(title: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(text = title)

        androidx.compose.material3.Button(
            onClick = onClick
        ) {
            Text("Ir a otra pantalla")
        }
    }
}
package com.project.apppetstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.project.apppetstore.navigation.AppDestination
import com.project.apppetstore.ui.components.AppBottomNavigationBar
import com.project.apppetstore.ui.components.AppDrawerContent
import com.project.apppetstore.ui.feature.adoption.AdoptionScreen
import com.project.apppetstore.ui.feature.adoption.AdoptionViewModel
import com.project.apppetstore.ui.feature.home.HomeScreen
import com.project.apppetstore.ui.feature.home.HomeViewModel
import com.project.apppetstore.ui.feature.products.ProductsScreen
import com.project.apppetstore.ui.feature.products.ProductsViewModel
import com.project.apppetstore.ui.feature.profile.LoginBottomSheet
import com.project.apppetstore.ui.feature.profile.ProfileScreen
import com.project.apppetstore.ui.feature.profile.ProfileScreenEnter
import com.project.apppetstore.ui.feature.profile.ProfileViewModel
import com.project.apppetstore.ui.feature.services.ServicesScreen
import com.project.apppetstore.ui.feature.services.ServicesViewModel

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPetShopApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.Home.route

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val profileViewModel: ProfileViewModel = viewModel()
    var showLoginSheet by rememberSaveable { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                AppDrawerContent(
                    currentRoute = currentRoute,
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),

                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AP",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Texto
                            Text(
                                text = "AppPetStore",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    },

                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },

                    actions = {
                        IconButton(onClick = { }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_bell_dot),
                                contentDescription = "Notificaciones",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                )
            },
            bottomBar = {
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
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(AppDestination.Home.route) {
                    val viewModel: HomeViewModel = viewModel()
                    HomeScreen(
                        uiState = viewModel.uiState,
                        onPetClick = { petId ->
                            navController.navigate("adoption?petId=$petId") {
                                popUpTo(AppDestination.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onFilterSelected = viewModel::onFilterSelected
                    )
                }
                composable(AppDestination.Services.route) {
                    val viewModel: ServicesViewModel = viewModel()
                    ServicesScreen(
                        uiState = viewModel.uiState,
                        onFilterSelected = viewModel::onFilterSelected
                    )
                }
                composable(AppDestination.Products.route) {
                    val viewModel: ProductsViewModel = viewModel()
                    ProductsScreen(
                        uiState = viewModel.uiState,
                        onFilterSelected = viewModel::onFilterSelected
                    )
                }
                composable(
                    route = "adoption?petId={petId}",
                    arguments = listOf(navArgument("petId") { type = NavType.StringType; nullable = true })
                ) { backStackEntry ->
                    val viewModel: AdoptionViewModel = viewModel()
                    val petId = backStackEntry.arguments?.getString("petId")
                    AdoptionScreen(
                        uiState = viewModel.uiState,
                        onInputChange = viewModel::onInputChange,
                        onSendMessage = viewModel::sendMessage,
                        selectedPetId = petId,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(AppDestination.Profile.route) {
                    val user = profileViewModel.uiState.user
                    if (user != null) {
                        ProfileScreenEnter(
                            userName = user.fullName,
                            userEmail = user.email,
                            onEditProfile = { /* TODO: Acción editar perfil */ },
                            onLogout = profileViewModel::logout
                        )
                    } else {
                        ProfileScreen(
                            onOpenLoginSheet = { showLoginSheet = true },
                        )
                    }
                }
            }

            if (showLoginSheet) {
                LoginBottomSheet(
                    onDismiss = { showLoginSheet = false },
                    onLogin = { fullName, email ->
                        profileViewModel.login(fullName, email)
                        showLoginSheet = false
                    }
                )
            }
        }
    }
}

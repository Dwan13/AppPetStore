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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.project.apppetstore.ui.feature.delivery.DeliveryScheduleScreen
import com.project.apppetstore.ui.feature.map.MapScreen
import com.project.apppetstore.ui.feature.products.ProductsScreen
import com.project.apppetstore.ui.feature.products.ProductsViewModel
import com.project.apppetstore.ui.feature.profile.LoginBottomSheet
import com.project.apppetstore.ui.feature.profile.ProfileScreen
import com.project.apppetstore.ui.feature.profile.ProfileScreenEnter
import com.project.apppetstore.ui.feature.profile.ProfilePhotoCameraDialog
import com.project.apppetstore.ui.feature.profile.ProfileViewModel
import com.project.apppetstore.ui.feature.profile.RegisterScreen
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
    var showProfilePhotoCamera by rememberSaveable { mutableStateOf(false) }
    var showPetPhotoCamera by rememberSaveable { mutableStateOf(false) }

    val petImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { profileViewModel.updatePetPhoto(it.toString()) }
        }
    )

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
                        onScheduleService = { service ->
                            navController.navigate(
                                AppDestination.DeliverySchedule.createRoute(
                                    serviceId = service.id,
                                    serviceCategory = service.category
                                )
                            )
                        },
                        onPetClick = { petId ->
                            navController.navigate("adoption?petId=$petId") {
                                popUpTo(AppDestination.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onFilterSelected = viewModel::onFilterSelected
                    )
                }
                composable(
                    route = AppDestination.DeliverySchedule.route,
                    arguments = listOf(
                        navArgument("serviceId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("serviceCategory") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val serviceId = backStackEntry.arguments?.getString("serviceId")
                    val serviceCategory = backStackEntry.arguments?.getString("serviceCategory")
                    DeliveryScheduleScreen(
                        serviceId = serviceId,
                        serviceCategory = serviceCategory,
                        onConfirm = {
                            navController.navigate(
                                AppDestination.Map.createRoute(
                                    deliveryRequested = true,
                                    serviceCategory = serviceCategory
                                )
                            )
                        }
                    )
                }
                composable(
                    route = AppDestination.Map.route,
                    arguments = listOf(
                        navArgument("deliveryRequested") {
                            type = NavType.BoolType
                            defaultValue = false
                        },
                        navArgument("serviceCategory") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val deliveryRequested = backStackEntry.arguments?.getBoolean("deliveryRequested") ?: false
                    val serviceCategory = backStackEntry.arguments?.getString("serviceCategory")
                    MapScreen(
                        deliveryRequested = deliveryRequested,
                        serviceCategory = serviceCategory
                    )
                }
                composable(AppDestination.Services.route) {
                    val viewModel: ServicesViewModel = viewModel()
                    ServicesScreen(
                        uiState = viewModel.uiState,
                        onFilterSelected = viewModel::onFilterSelected,
                        onScheduleService = { service ->
                            navController.navigate(
                                AppDestination.DeliverySchedule.createRoute(
                                    serviceId = service.id,
                                    serviceCategory = service.category
                                )
                            )
                        }
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
                        onAttachMedia = viewModel::attachMedia,
                        onRemovePendingAttachment = viewModel::removePendingAttachment,
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
                            profilePhotoUri = user.profilePhotoUri,
                            petName = user.petProfile.name,
                            petSpecies = user.petProfile.species,
                            petAge = user.petProfile.age,
                            petPhotoUri = user.petProfile.photoUri,
                            petTraits = user.petProfile.traits,
                            onEditProfile = { showProfilePhotoCamera = true },
                            onTakePetPhoto = { showPetPhotoCamera = true },
                            onPickPetPhoto = {
                                petImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onSavePetCharacteristics = profileViewModel::updatePetCharacteristics,
                            onLogout = profileViewModel::logout
                        )
                    } else {
                        ProfileScreen(
                            onOpenLoginSheet = { showLoginSheet = true },
                        )
                    }
                }

                composable(AppDestination.Register.route) {
                    RegisterScreen(
                        onRegister = { fullName, email ->
                            profileViewModel.register(fullName, email)
                            navController.navigate(AppDestination.Profile.route) {
                                popUpTo(AppDestination.Register.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onBackToLogin = {
                            navController.popBackStack()
                            showLoginSheet = true
                        }
                    )
                }
            }

            if (showLoginSheet) {
                LoginBottomSheet(
                    onDismiss = { showLoginSheet = false },
                    onLogin = { fullName, email ->
                        profileViewModel.login(fullName, email)
                        showLoginSheet = false
                    },
                    onRegisterClick = {
                        showLoginSheet = false
                        navController.navigate(AppDestination.Register.route)
                    }
                )
            }

            if (showProfilePhotoCamera && profileViewModel.uiState.user != null) {
                ProfilePhotoCameraDialog(
                    onDismiss = { showProfilePhotoCamera = false },
                    onPhotoTaken = { photoUri ->
                        profileViewModel.updateProfilePhoto(photoUri.toString())
                    }
                )
            }

            if (showPetPhotoCamera && profileViewModel.uiState.user != null) {
                ProfilePhotoCameraDialog(
                    onDismiss = { showPetPhotoCamera = false },
                    onPhotoTaken = { photoUri ->
                        profileViewModel.updatePetPhoto(photoUri.toString())
                    }
                )
            }
        }
    }
}

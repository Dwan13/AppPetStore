package com.project.apppetstore

import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import com.project.apppetstore.ui.feature.adoption.AdoptionScreen
import com.project.apppetstore.ui.feature.adoption.AdoptionViewModel
import com.project.apppetstore.data.repository.MockPetShopRepository
import com.project.apppetstore.ui.feature.home.HomeScreen
import com.project.apppetstore.ui.feature.home.HomeViewModel
import com.project.apppetstore.ui.feature.delivery.DeliveryScheduleScreen
import com.project.apppetstore.ui.feature.map.MapScreen
import com.project.apppetstore.ui.feature.notifications.NotificationsScreen
import com.project.apppetstore.ui.feature.notifications.NotificationsViewModel
import com.project.apppetstore.ui.feature.products.CheckoutScreen
import com.project.apppetstore.ui.feature.products.ProductDetailScreen
import com.project.apppetstore.ui.feature.products.ProductsScreen
import com.project.apppetstore.ui.feature.products.ProductsViewModel
import com.project.apppetstore.ui.feature.profile.LoginScreen
import com.project.apppetstore.ui.feature.profile.MisMascotasScreen
import com.project.apppetstore.ui.feature.profile.PetsViewModel
import com.project.apppetstore.ui.feature.profile.ProfileScreen
import com.project.apppetstore.ui.feature.profile.ProfileScreenEnter
import com.project.apppetstore.ui.feature.profile.ProfilePhotoCameraDialog
import com.project.apppetstore.ui.feature.profile.ProfileViewModel
import com.project.apppetstore.ui.feature.profile.RegisterScreen
import com.project.apppetstore.ui.feature.appointment.AppointmentScreen
import com.project.apppetstore.ui.feature.favorites.FavoritesScreen
import com.project.apppetstore.ui.feature.favorites.FavoritesViewModel
import com.project.apppetstore.ui.feature.orders.OrdersScreen
import com.project.apppetstore.ui.feature.orders.OrdersViewModel
import com.project.apppetstore.ui.feature.services.ServicesScreen
import com.project.apppetstore.ui.feature.services.ServicesViewModel
import com.project.apppetstore.ui.feature.settings.SettingsScreen
import com.project.apppetstore.ui.feature.settings.SettingsViewModel
import com.project.apppetstore.ui.theme.AppPetStoreTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPetShopApp() {

    // ── ViewModels de nivel app ───────────────────────────────────────────────
    val profileViewModel      : ProfileViewModel       = viewModel()
    val favoritesViewModel    : FavoritesViewModel     = viewModel()
    val ordersViewModel       : OrdersViewModel        = viewModel()
    val petsViewModel         : PetsViewModel          = viewModel()
    val settingsViewModel     : SettingsViewModel      = viewModel()
    val notificationsViewModel: NotificationsViewModel = viewModel()

    val authState    by profileViewModel.uiState.collectAsState()
    val settingsState = settingsViewModel.uiState
    val notifsState   = notificationsViewModel.uiState

    // ── Tema dinámico ─────────────────────────────────────────────────────────
    AppPetStoreTheme(
        darkTheme = settingsState.darkTheme ?: isSystemInDarkTheme()
    ) {

        val navController  = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute   = backStackEntry?.destination?.route ?: ""

        // ── Solicitar permiso POST_NOTIFICATIONS (Android 13+ / API 33) ──────
        val notifPermLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { /* El usuario decidió; no se fuerza nada */ }
        )
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        var showProfilePhotoCamera by rememberSaveable { mutableStateOf(false) }

        // Punto de entrada: Home si hay sesión activa, Profile (pantalla guest)
        // si no la hay. El ViewModel ya resuelve auth.currentUser sincrónicamente.
        val startDestination = if (authState.isLoggedIn) AppDestination.Home.route
                               else AppDestination.Profile.route

        // ── Guard de navegación ───────────────────────────────────────────────
        LaunchedEffect(authState.isLoggedIn) {
            val onAuthScreen  = currentRoute == AppDestination.Login.route ||
                                currentRoute == AppDestination.Register.route
            val onGuestScreen = currentRoute == AppDestination.Profile.route

            when {
                // Recién autenticado en Login/Register → ir a Home
                authState.isLoggedIn && onAuthScreen -> {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                // Sesión cerrada (logout) o no autenticado en ruta protegida → Profile guest
                !authState.isLoggedIn && !onAuthScreen && !onGuestScreen
                        && currentRoute.isNotEmpty() -> {
                    navController.navigate(AppDestination.Profile.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }

        val profileImagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> uri?.let { profileViewModel.uploadProfilePhoto(it) } }
        )

        val isAuthRoute = currentRoute == AppDestination.Login.route ||
                          currentRoute == AppDestination.Register.route

        // La barra superior y la navegación inferior solo se muestran cuando
        // el usuario está autenticado Y no está en una pantalla de flujo específico
        // que usa su propia barra (detail, checkout, mapa, etc.).
        val hideScaffoldBars = !authState.isLoggedIn ||
                          isAuthRoute ||
                          currentRoute.startsWith("product/") ||
                          currentRoute.startsWith("checkout/") ||
                          currentRoute.startsWith("appointment") ||
                          currentRoute.startsWith("deliverySchedule") ||
                          currentRoute.startsWith("map") ||
                          currentRoute.startsWith("favorites") ||
                          currentRoute.startsWith("orders") ||
                          currentRoute.startsWith("notifications") ||
                          currentRoute.startsWith("settings") ||
                          currentRoute.startsWith("mis_mascotas")

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (!hideScaffoldBars) {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = "AP",
                                        color      = MaterialTheme.colorScheme.onPrimary,
                                        fontSize   = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text       = "AppPetStore",
                                    color      = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 16.sp
                                )
                            }
                        },
                        actions = {
                            // ── Campana con badge de no leídos ────────────────
                            IconButton(
                                onClick = {
                                    navController.navigate(AppDestination.Notifications.route) {
                                        launchSingleTop = true
                                    }
                                }
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (notifsState.unreadCount > 0) {
                                            Badge {
                                                Text(
                                                    text  = if (notifsState.unreadCount > 9) "9+" else "${notifsState.unreadCount}",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter            = painterResource(R.drawable.ic_bell_dot),
                                        contentDescription = "Notificaciones",
                                        tint               = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (!hideScaffoldBars) {
                    AppBottomNavigationBar(
                        currentRoute = currentRoute,
                        onTabSelected = { destination ->
                            val navRoute = destination.route.substringBefore("?")
                            navController.navigate(navRoute) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController    = navController,
                startDestination = startDestination,
                modifier         = Modifier.padding(innerPadding)
            ) {

                // ── Login ─────────────────────────────────────────────────────
                composable(AppDestination.Login.route) {
                    LoginScreen(
                        uiState              = authState,
                        onLoginWithEmail     = profileViewModel::loginWithEmail,
                        onSignInWithGoogle   = profileViewModel::signInWithGoogle,
                        onNavigateToRegister = { navController.navigate(AppDestination.Register.route) },
                        onClearError         = profileViewModel::clearError
                    )
                }

                // ── Register ──────────────────────────────────────────────────
                composable(AppDestination.Register.route) {
                    RegisterScreen(
                        uiState            = authState,
                        onRegister         = { fullName, email, password ->
                            profileViewModel.registerWithEmail(fullName, email, password)
                        },
                        onSignInWithGoogle = profileViewModel::signInWithGoogle,
                        onBackToLogin      = { navController.popBackStack() },
                        onClearError       = profileViewModel::clearError
                    )
                }

                // ── Home ──────────────────────────────────────────────────────
                composable(AppDestination.Home.route) {
                    val viewModel: HomeViewModel = viewModel()
                    HomeScreen(
                        uiState             = viewModel.uiState,
                        onLocationAvailable = viewModel::updateUserLocation,
                        favoritePetIds      = favoritesViewModel.uiState.favoritePetIds,
                        onFavoriteToggle    = favoritesViewModel::toggleFavorite,
                        onScheduleService   = { service ->
                            if (service.supportsDelivery) {
                                navController.navigate(AppDestination.DeliverySchedule.createRoute(service.id, service.category))
                            } else {
                                navController.navigate(AppDestination.Appointment.createRoute(service.id, service.category))
                            }
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

                // ── Delivery Schedule ─────────────────────────────────────────
                composable(
                    route     = AppDestination.DeliverySchedule.route,
                    arguments = listOf(
                        navArgument("serviceId")       { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("serviceCategory") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStack ->
                    val serviceId       = backStack.arguments?.getString("serviceId")
                    val serviceCategory = backStack.arguments?.getString("serviceCategory")
                    DeliveryScheduleScreen(
                        serviceId       = serviceId,
                        serviceCategory = serviceCategory,
                        onConfirm       = { destLat, destLng ->
                            val service = MockPetShopRepository.getServices().find { it.id == serviceId }
                            navController.navigate(
                                AppDestination.Map.createRoute(
                                    deliveryRequested = true,
                                    serviceCategory   = serviceCategory,
                                    destLat           = destLat,
                                    destLng           = destLng,
                                    originLat         = service?.lat?.takeIf { it != 0.0 },
                                    originLng         = service?.lng?.takeIf { it != 0.0 }
                                )
                            )
                        }
                    )
                }

                // ── Map ───────────────────────────────────────────────────────
                composable(
                    route     = AppDestination.Map.route,
                    arguments = listOf(
                        navArgument("deliveryRequested") { type = NavType.BoolType;   defaultValue = false },
                        navArgument("serviceCategory")   { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("destLat")           { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("destLng")           { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("originLat")         { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("originLng")         { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStack ->
                    MapScreen(
                        deliveryRequested = backStack.arguments?.getBoolean("deliveryRequested") ?: false,
                        serviceCategory   = backStack.arguments?.getString("serviceCategory"),
                        destLat           = backStack.arguments?.getString("destLat")?.toDoubleOrNull(),
                        destLng           = backStack.arguments?.getString("destLng")?.toDoubleOrNull(),
                        originLat         = backStack.arguments?.getString("originLat")?.toDoubleOrNull(),
                        originLng         = backStack.arguments?.getString("originLng")?.toDoubleOrNull(),
                        onBookAppointment = { service ->
                            navController.navigate(
                                AppDestination.Appointment.createRoute(service.id, service.category)
                            )
                        },
                        onRequestDelivery = { service ->
                            navController.navigate(
                                AppDestination.DeliverySchedule.createRoute(service.id, service.category)
                            )
                        }
                    )
                }

                // ── Services ──────────────────────────────────────────────────
                composable(AppDestination.Services.route) {
                    val viewModel: ServicesViewModel = viewModel()
                    ServicesScreen(
                        uiState           = viewModel.uiState,
                        onFilterSelected  = viewModel::onFilterSelected,
                        onScheduleService = { service ->
                            if (service.supportsDelivery) {
                                navController.navigate(AppDestination.DeliverySchedule.createRoute(service.id, service.category))
                            } else {
                                navController.navigate(AppDestination.Appointment.createRoute(service.id, service.category))
                            }
                        }
                    )
                }

                // ── Appointment ───────────────────────────────────────────────
                composable(
                    route     = AppDestination.Appointment.route,
                    arguments = listOf(
                        navArgument("serviceId")       { type = NavType.StringType; nullable = true; defaultValue = null },
                        navArgument("serviceCategory") { type = NavType.StringType; nullable = true; defaultValue = null }
                    )
                ) { backStack ->
                    AppointmentScreen(
                        serviceId       = backStack.arguments?.getString("serviceId"),
                        serviceCategory = backStack.arguments?.getString("serviceCategory"),
                        onBack          = { navController.popBackStack() }
                    )
                }

                // ── Products ──────────────────────────────────────────────────
                composable(AppDestination.Products.route) {
                    val viewModel: ProductsViewModel = viewModel()
                    ProductsScreen(
                        uiState          = viewModel.uiState,
                        onFilterSelected = viewModel::onFilterSelected,
                        onProductClick   = { navController.navigate(AppDestination.ProductDetail.createRoute(it)) }
                    )
                }

                // ── Product Detail ────────────────────────────────────────────
                composable(
                    route     = AppDestination.ProductDetail.route,
                    arguments = listOf(navArgument("productId") { type = NavType.StringType })
                ) { backStack ->
                    val viewModel = viewModel<ProductsViewModel>()
                    val productId = backStack.arguments?.getString("productId") ?: return@composable
                    val product   = viewModel.getProductById(productId)         ?: return@composable
                    ProductDetailScreen(
                        product = product,
                        onBack  = { navController.popBackStack() },
                        onBuyNow = { quantity ->
                            navController.navigate(AppDestination.Checkout.createRoute(productId, quantity))
                        }
                    )
                }

                // ── Checkout ──────────────────────────────────────────────────
                composable(
                    route     = AppDestination.Checkout.route,
                    arguments = listOf(
                        navArgument("productId") { type = NavType.StringType },
                        navArgument("quantity")  { type = NavType.IntType }
                    )
                ) { backStack ->
                    val viewModel = viewModel<ProductsViewModel>()
                    val productId = backStack.arguments?.getString("productId") ?: return@composable
                    val quantity  = backStack.arguments?.getInt("quantity")     ?: 1
                    val product   = viewModel.getProductById(productId)         ?: return@composable
                    CheckoutScreen(
                        product          = product,
                        quantity         = quantity,
                        onBack           = { navController.popBackStack() },
                        onPlaceOrder     = ordersViewModel::placeOrder,
                        onOrderConfirmed = {
                            navController.navigate(AppDestination.Products.route) {
                                popUpTo(AppDestination.Products.route) { inclusive = true }
                            }
                        }
                    )
                }

                // ── Adoption ──────────────────────────────────────────────────
                composable(
                    route     = "adoption?petId={petId}",
                    arguments = listOf(navArgument("petId") { type = NavType.StringType; nullable = true })
                ) { backStack ->
                    val viewModel: AdoptionViewModel = viewModel()
                    val petId = backStack.arguments?.getString("petId")
                    LaunchedEffect(petId) { viewModel.setCurrentPet(petId) }
                    AdoptionScreen(
                        uiState                   = viewModel.uiState,
                        onInputChange             = viewModel::onInputChange,
                        onSendMessage             = viewModel::sendMessage,
                        onAttachMedia             = viewModel::attachMedia,
                        onRemovePendingAttachment = viewModel::removePendingAttachment,
                        selectedPetId             = petId,
                        // UC-25: navegar al perfil de la mascota sorpresa
                        onNavigateToPet           = { discoveredPetId ->
                            navController.navigate("adoption?petId=$discoveredPetId") {
                                launchSingleTop = true
                            }
                        },
                        onBack                    = { navController.popBackStack() }
                    )
                }

                // ── Profile ───────────────────────────────────────────────────
                composable(AppDestination.Profile.route) {
                    val user = authState.user
                    if (user != null) {
                        ProfileScreenEnter(
                            userName           = user.fullName,
                            userEmail          = user.email,
                            profilePhotoUri    = user.profilePhotoUri,
                            pets               = petsViewModel.uiState.pets,
                            onTakeProfilePhoto = { showProfilePhotoCamera = true },
                            onPickProfilePhoto = {
                                profileImagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onMisMascotasClick = {
                                navController.navigate(AppDestination.MisMascotas.route)
                            },
                            onOrdersClick    = { navController.navigate(AppDestination.Orders.route) },
                            onFavoritesClick = { navController.navigate(AppDestination.Favorites.route) },
                            onSettingsClick  = { navController.navigate(AppDestination.Settings.route) },
                            onLogout         = profileViewModel::logout
                        )
                    } else {
                        // Pantalla de invitación al login para usuarios no autenticados.
                        // Navegamos a Login apilado (sin borrar Home) para que el Back
                        // funcione y la sesión recién iniciada lleve de vuelta a Home.
                        ProfileScreen(onOpenLoginSheet = {
                            navController.navigate(AppDestination.Login.route) {
                                launchSingleTop = true
                            }
                        })
                    }
                }

                // ── Favorites ─────────────────────────────────────────────────
                composable(AppDestination.Favorites.route) {
                    FavoritesScreen(
                        uiState          = favoritesViewModel.uiState,
                        onBack           = { navController.popBackStack() },
                        onFavoriteToggle = favoritesViewModel::toggleFavorite,
                        onPetClick       = { petId ->
                            navController.navigate("adoption?petId=$petId") { launchSingleTop = true }
                        }
                    )
                }

                // ── Orders ────────────────────────────────────────────────────
                composable(AppDestination.Orders.route) {
                    OrdersScreen(
                        uiState = ordersViewModel.uiState,
                        onBack  = { navController.popBackStack() }
                    )
                }

                // ── Notifications ─────────────────────────────────────────────
                composable(AppDestination.Notifications.route) {
                    NotificationsScreen(
                        uiState           = notifsState,
                        onBack            = { navController.popBackStack() },
                        onMarkAllSeen     = notificationsViewModel::markAllSeen,
                        lastSeenTimestamp = notifsState.lastSeenTs
                    )
                }

                // ── Settings ──────────────────────────────────────────────────
                composable(AppDestination.Settings.route) {
                    SettingsScreen(
                        uiState             = settingsState,
                        onBack              = { navController.popBackStack() },
                        onSetSearchRadius   = settingsViewModel::setSearchRadius,
                        onSetDarkTheme      = settingsViewModel::setDarkTheme,
                        onSetNotifications  = settingsViewModel::setNotificationsEnabled
                    )
                }

                // ── Mis Mascotas ──────────────────────────────────────────────
                composable(AppDestination.MisMascotas.route) {
                    MisMascotasScreen(
                        uiState       = petsViewModel.uiState,
                        onAddPet      = { name, species, age, traits, photoUri ->
                            petsViewModel.addPet(name, species, age, traits, photoUri)
                            notificationsViewModel.addLocalNotification(
                                title   = "🐾 Mascota registrada",
                                message = "$name ha sido añadida a tu perfil correctamente.",
                                type    = "pet"
                            )
                        },
                        onUpdatePet   = petsViewModel::updatePet,
                        onDeletePet   = { petId ->
                            val petName = petsViewModel.uiState.pets
                                .find { it.id == petId }?.name ?: "La mascota"
                            petsViewModel.deletePet(petId)
                            notificationsViewModel.addLocalNotification(
                                title   = "🗑️ Mascota eliminada",
                                message = "$petName ha sido eliminada de tu perfil.",
                                type    = "pet"
                            )
                        },
                        onClearResult = petsViewModel::clearOperationResult,
                        onBack        = { navController.popBackStack() }
                    )
                }
            }

            // ── Diálogo de cámara de perfil ───────────────────────────────────
            if (showProfilePhotoCamera && authState.user != null) {
                ProfilePhotoCameraDialog(
                    onDismiss    = { showProfilePhotoCamera = false },
                    onPhotoTaken = { photoUri ->
                        profileViewModel.uploadProfilePhoto(photoUri)
                        showProfilePhotoCamera = false
                    }
                )
            }
        }
    }
}

# AppPetStore

Aplicación Android nativa (Kotlin + Jetpack Compose) para servicios y productos de mascotas: clínicas veterinarias, spas, adopción, domicilios, tienda de productos, perfil de usuario con mascota, mapa interactivo y uso real de sensores del dispositivo.

---

## Tabla de contenido

1. [Tecnologías principales](#1-tecnologías-principales)
2. [Configuración local](#2-configuración-local)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Módulos y pantallas implementadas](#4-módulos-y-pantallas-implementadas)
5. [Sensores del dispositivo](#5-sensores-del-dispositivo)
6. [Autenticación y navegación protegida](#6-autenticación-y-navegación-protegida)
7. [Flujo de navegación](#7-flujo-de-navegación)
8. [Firebase y base de datos](#8-firebase-y-base-de-datos)
9. [Casos de uso e historias de usuario](#9-casos-de-uso-e-historias-de-usuario)
10. [Flujo Git recomendado](#10-flujo-git-recomendado)
11. [Troubleshooting](#11-troubleshooting)

---

## 1. Tecnologías principales

| Área | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navegación | Navigation Compose |
| Arquitectura | ViewModel + StateFlow / mutableStateOf |
| Autenticación | Firebase Auth (email/contraseña + Google Sign-In) |
| Base de datos | Firebase Firestore |
| Almacenamiento | Firebase Storage |
| Mapas | Google Maps Compose + Directions API |
| Ubicación | Google Play Services FusedLocationProvider |
| Sensores | Android SensorManager (acelerómetro, giroscopio, luz) |
| Imágenes | Coil 3 (AsyncImage) |
| Credenciales Google | Credential Manager (GetSignInWithGoogleOption) |
| Preferencias | Jetpack DataStore |
| Mínimo SDK | 26 (Android 8.0) |
| Target SDK | 35 |

---

## 2. Configuración local

### 2.1 Requisitos previos

- Android Studio Hedgehog o superior
- JDK 11+
- Emulador o dispositivo físico con Android 8+
- Cuenta de Firebase con proyecto configurado
- API Key de Google Maps (con Maps SDK + Directions API habilitados)

### 2.2 Pasos

1. Clona el repositorio:

```bash
git clone URL_DEL_REPOSITORIO
cd AppPetStore
```

2. Copia tu `google-services.json` (descargado desde Firebase Console) en:
   `app/google-services.json`

3. Edita `local.properties`:

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=TU_API_KEY_DE_GOOGLE_MAPS
```

4. En Firebase Console activa:
   - **Authentication** → Proveedores: Email/Contraseña y Google
   - **Firestore** → Colecciones: `users`, `chats`, `services`, `products`, `pets`
   - **Storage** → Carpetas: `users/`, `chats/`

5. Para Google Sign-In, agrega la huella SHA-1 en Firebase Console:

```bash
# En la terminal del proyecto (Git Bash o PowerShell)
./gradlew signingReport
```

   Copia la huella `SHA1` y agrégala en Firebase Console → Configuración del proyecto → Huella digital.  
   Luego vuelve a descargar `google-services.json` y reemplázalo en `app/`.

6. Sincroniza Gradle y ejecuta la app (`▶ Run`).

---

## 3. Estructura del proyecto

```
app/src/main/java/com/project/apppetstore/
├── navigation/
│   └── AppDestination.kt          # Rutas de navegación
├── data/
│   ├── model/                     # Pet, Service, Product, Order, UserProfile, PetProfile...
│   ├── network/                   # DirectionsService (Directions API)
│   ├── repository/                # Firestore (pets, services, products), Mock, Settings
│   └── util/                      # ImageUploadUtils
├── ui/
│   ├── components/                # PetCard, ServiceCard, ProductCard, ChatBubble, Shimmer...
│   ├── feature/
│   │   ├── home/                  # HomeScreen + HomeViewModel
│   │   ├── map/                   # MapScreen (exploración + simulación de domicilio)
│   │   ├── services/              # ServicesScreen + ServicesViewModel
│   │   ├── appointment/           # AppointmentScreen (agendamiento de cita)
│   │   ├── delivery/              # DeliveryScheduleScreen (programar domicilio)
│   │   ├── products/              # ProductsScreen, ProductDetailScreen, CheckoutScreen
│   │   ├── adoption/              # AdoptionScreen, PetDetailScreen, AdoptionViewModel
│   │   ├── profile/               # LoginScreen, RegisterScreen, ProfileScreenEnter,
│   │   │                          # MisMascotasScreen, PetsViewModel, ProfileViewModel,
│   │   │                          # GoogleAuthHelper
│   │   ├── favorites/             # FavoritesScreen + FavoritesViewModel
│   │   ├── orders/                # OrdersScreen + OrdersViewModel
│   │   ├── notifications/         # NotificationsScreen + NotificationsViewModel
│   │   └── settings/              # SettingsScreen + SettingsViewModel
│   ├── viewmodels/
│   │   ├── SensorViewModel.kt     # Acelerómetro, giroscopio, luz
│   │   └── LocationViewModel.kt   # GPS y actualizaciones de ubicación
│   ├── screens/                   # GalleryScreen, BasicCamera, InAppCamera
│   └── theme/                     # Color, Type, Theme
├── utils/
│   ├── AppNotificationHelper.kt   # Notificaciones push del sistema
│   └── FileUtils.kt
├── AppPetStoreApp.kt              # NavHost principal + Scaffold con guards de autenticación
├── AppPetStoreApplication.kt      # Application
└── MainActivity.kt
```

---

## 4. Módulos y pantallas implementadas

### Home
- Banner de acceso al mapa de servicios cercanos.
- Carrusel animado de mascotas en adopción (Material 3 `HorizontalMultiBrowseCarousel`).
- Lista de servicios filtrados por distancia y categoría (Clínicas, Spa, A domicilio).
- Solicitud de permiso de ubicación en primer uso.
- Skeleton loader animado mientras carga datos.

### Mapa de servicios (`MapScreen`)
- Marcadores diferenciados por categoría (🏥 Clínica, ✂️ Spa, 🛵 Domicilio).
- Datos reales desde Firestore via `ServicesViewModel`; fallback a puntos estáticos si no hay coordenadas.
- Filtros por categoría con chips.
- `ServiceActionCard`: al tocar un marcador muestra detalle con botones "Solicitar cita" y "A domicilio".
- **Modo domicilio**: animación en tiempo real del domiciliario sobre el mapa con ruta real (Directions API) y ETA actualizado.
- **Sensor de luz (UC-27)**: cuando la luminosidad baja de 30 lux, el mapa cambia automáticamente a estilo nocturno.

### Agendamiento de cita (`AppointmentScreen`)
- Selector de días disponibles (próxima semana).
- Selector de horarios por slots.
- Confirmación con animación de éxito.

### Servicio a domicilio (`DeliveryScheduleScreen`)
- Ingreso de dirección libre o captura automática via GPS.
- `DatePicker` y `TimePicker` Material 3.
- Selección de franja horaria (mañana, tarde, noche).

### Catálogo de productos (`ProductsScreen / ProductDetailScreen`)
- Grid de 2 columnas con skeleton loader.
- Filtros por categoría en tiempo real.
- Detalle con calificaciones, stock, badge de descuento y selector de cantidad.
- **Efecto parallax (UC-26)**: inclinar el teléfono desplaza la imagen hero sutilmente (giroscopio).

### Checkout (`CheckoutScreen`)
- Resumen de compra con foto, cantidad y total.
- Integración con `OrdersViewModel` para persistir en historial.

### Adopción (`AdoptionScreen / PetDetailScreen`)
- Carrusel táctil de mascotas disponibles.
- Detalle completo: foto, breed, edad, género, tamaño, salud, vacunas, personalidad, requisitos.
- Chat real en tiempo real con Firestore.
- Adjuntos: foto (cámara/galería), video (captura/galería), audio (grabación/galería).
- **Shake to discover (UC-25)**: agitar el teléfono muestra una mascota aleatoria en un BottomSheet.

### Perfil de usuario (`ProfileScreenEnter`)
- Foto de perfil (cámara o galería) almacenada en Firebase Storage.
- Edición de nombre.
- Perfil de mascota con nombre, especie, edad, rasgos como chips seleccionables y foto.
- Datos sincronizados con Firestore.

### Mis mascotas (`MisMascotasScreen`)
- Registro, edición y eliminación de mascotas propias.
- Notificación local al agregar o eliminar una mascota.

### Favoritos (`FavoritesScreen`)
- Grid de mascotas guardadas.
- Persistencia de favoritos entre sesiones.

### Pedidos (`OrdersScreen`)
- Historial de compras con foto, nombre, cantidad, total y fecha.

### Notificaciones (`NotificationsScreen`)
- Centro de notificaciones in-app.
- Badge dinámico en la campana de la TopBar.
- Notificaciones automáticas: login, registro, cierre de sesión, mascota agregada/eliminada.

### Configuración (`SettingsScreen`)
- Tema: Sistema / Claro / Oscuro (persiste con `DataStore`).
- Radio de búsqueda: 2, 5, 10 o 20 km.
- Switch de notificaciones.

---

## 5. Autenticación y navegación protegida

> **Regla de oro:** la barra de navegación inferior y la barra superior están **completamente ocultas** mientras el usuario no esté autenticado. No hay forma de acceder a ninguna pestaña sin iniciar sesión.

### Flujo no autenticado
1. La app arranca en `ProfileScreen` (modo guest): branding, ilustración y botón "Iniciar sesión".
2. Pulsar "Iniciar sesión" apila `LoginScreen` — el botón **Back** regresa a la pantalla de bienvenida.
3. Desde `LoginScreen` se puede navegar a `RegisterScreen`.
4. Tras autenticarse correctamente, la app navega a `Home` limpiando toda la pila (`popUpTo(0) { inclusive = true }`) y habilita la navegación completa.

### Guard de rutas protegidas
Un `LaunchedEffect(authState.isLoggedIn)` en `AppPetStoreApp` protege todas las rutas:
- Si la sesión se cierra estando en cualquier pantalla protegida → redirige a `ProfileScreen` (guest) y limpia la pila.
- El `startDestination` del `NavHost` es condicional: `Profile` si no hay sesión activa, `Home` si hay sesión (resuelto síncronamente desde `FirebaseAuth.currentUser`).

### Login con email y contraseña (`LoginScreen`)
- Validación en tiempo real al perder el foco de cada campo y al enviar.
- Errores amigables mapeados desde Firebase Auth.

### Registro (`RegisterScreen`)
- Validaciones: nombre (≥ 3 caracteres), email (regex RFC 5322), contraseña (≥ 8 caracteres + número + mayúscula), confirmación de contraseña.
- Indicador de fortaleza de contraseña animado (4 niveles: Muy corta → Fuerte).
- Todos los campos validan al perder el foco (`onFocusChanged`) sin necesidad de pulsar "Registrarse" primero.

### Google Sign-In (Login y Registro)
- Estrategia dos pasos: `GetSignInWithGoogleOption` → fallback `GetGoogleIdOption`.
- Manejo diferenciado: cancelación silenciosa, sin cuentas en dispositivo, error de red, error genérico.
- Requiere SHA-1 registrada en Firebase Console y `google-services.json` actualizado.

---

## 6. Flujo de navegación

```
[Sin sesión — TopBar y BottomNav OCULTOS]
  ProfileScreen (guest)
        │
        └── "Iniciar sesión" ──→ LoginScreen ──→ RegisterScreen
                                      │
                                      └── Login/Registro exitoso
                                                   ↓
[Con sesión — TopBar y BottomNav HABILITADOS]

  Home ──────────────────── (pestaña principal)
   ├── Mapa (BottomSheet) → ServiceActionCard → Cita / Domicilio
   ├── Mascota en adopción → AdoptionScreen
   └── Servicio → AppointmentScreen o DeliveryScheduleScreen

  Services ──────────────── (pestaña)
   └── Servicio → AppointmentScreen o DeliveryScheduleScreen → MapScreen

  Products ──────────────── (pestaña)
   └── Producto → ProductDetailScreen (parallax UC-26) → CheckoutScreen

  Adoption ──────────────── (pestaña)
   ├── Shake del teléfono → BottomSheet mascota aleatoria (UC-25)
   └── Mascota → PetDetailScreen (chat + adopción + parallax UC-26)

  Profile ────────────────── (pestaña)
   ├── Mis Mascotas
   ├── Pedidos
   ├── Favoritos
   ├── Configuración
   └── Cerrar sesión ──→ ProfileScreen (guest) [TopBar y BottomNav OCULTOS]
```

---

## 7. Firebase y base de datos

### Colecciones Firestore

| Colección | Descripción |
|---|---|
| `users/{uid}` | Perfil de usuario: fullName, email, profilePhotoUrl, petProfile |
| `chats/{uid_petId}/messages` | Mensajes del chat de adopción (texto + adjuntos) |
| `services` | Catálogo de servicios: nombre, categoría, lat, lng, supportsDelivery |
| `products` | Catálogo de productos: nombre, precio, categoría, stock, descuento |
| `pets` | Mascotas en adopción: nombre, raza, edad, salud, fotos |

### Firebase Storage

| Ruta | Contenido |
|---|---|
| `users/{uid}/profile.jpg` | Foto de perfil del usuario |
| `users/{uid}/pet.jpg` | Foto de la mascota del usuario |
| `chats/{chatId}/{timestamp}.{ext}` | Adjuntos del chat (imagen `.jpg`, video `.mp4`, audio `.aac`) |

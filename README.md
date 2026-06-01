# AppPetStore

Aplicación Android nativa (Kotlin + Jetpack Compose) para servicios y productos de mascotas: clínicas veterinarias, spas, adopción, domicilios, tienda de productos, perfil de usuario con mascota, mapa interactivo y uso real de sensores del dispositivo.

---

## Tabla de contenido

1. [Tecnologías principales](#1-tecnologías-principales)
2. [Configuración local](#2-configuración-local)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Módulos y pantallas implementadas](#4-módulos-y-pantallas-implementadas)
5. [Sensores del dispositivo](#5-sensores-del-dispositivo)
6. [Autenticación](#6-autenticación)
7. [Flujo de navegación](#7-flujo-de-navegación)
8. [Firebase y base de datos](#8-firebase-y-base-de-datos)
9. [Flujo Git recomendado](#9-flujo-git-recomendado)
10. [Troubleshooting](#10-troubleshooting)

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
├── AppPetStoreApp.kt              # NavHost principal + Scaffold
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

## 5. Sensores del dispositivo

El proyecto usa `SensorViewModel` (acelerómetro, giroscopio, sensor de luz) con tres casos de uso reales:

### UC-25 — Shake to Discover (Acelerómetro)
**Pantalla:** `AdoptionScreen`  
Agitar el teléfono (magnitud > 15.5 m/s², debounce 2 s) muestra un `ModalBottomSheet` con una mascota aleatoria disponible para adopción. El usuario puede ver su perfil completo o agitar de nuevo para ver otra.

### UC-26 — Efecto Parallax (Giroscopio)
**Pantallas:** `PetDetailScreen`, `ProductDetailScreen`  
Inclinar el teléfono desplaza la imagen principal ±14 dp en X y ±10 dp en Y, creando un efecto de profundidad 3D. El movimiento es suavizado con interpolación exponencial (lerp) y `animateFloatAsState`. La imagen tiene zoom 6% para evitar bordes visibles.

### UC-27 — Mapa nocturno automático (Sensor de luz)
**Pantalla:** `MapScreen`  
El sensor de luz detecta ambientes oscuros (< 30 lux) y aplica automáticamente el estilo de mapa nocturno (`map_style_dark.json`). Usa histéresis: se activa por debajo de 30 lux y se desactiva por encima de 80 lux.

---

## 6. Autenticación

### Flujo de usuario no autenticado
- La app inicia en la **pantalla de bienvenida** (`ProfileScreen` guest): branding, ilustración y botón "Iniciar sesión".
- La **barra de navegación inferior y la barra superior están ocultas** — el usuario no puede navegar entre tabs.
- El botón "Iniciar sesión" lleva a `LoginScreen` (apilado, el Back regresa a la pantalla de bienvenida).
- Tras autenticarse, se redirige automáticamente a `Home` con la pila limpia y la navegación habilitada.
- Si el usuario cierra sesión, la app regresa automáticamente a la pantalla de bienvenida y oculta la navegación.

### Login con email y contraseña (`LoginScreen`)
- Validación en tiempo real al salir de cada campo (blur) y al enviar.
- Errores amigables mapeados desde Firebase Auth.

### Registro (`RegisterScreen`)
- Validaciones: nombre (≥ 3 caracteres), email (regex RFC 5322), contraseña (≥ 8 caracteres + número + mayúscula), confirmación.
- Indicador de fortaleza de contraseña animado (4 niveles: Muy corta → Fuerte).
- Requisitos mostrados antes del primer intento.

### Google Sign-In (Login y Registro)
- Estrategia dos pasos: `GetSignInWithGoogleOption` → fallback `GetGoogleIdOption`.
- Manejo diferenciado: cancelación silenciosa, sin cuentas en dispositivo, error de red, error genérico.
- Requiere SHA-1 registrada en Firebase Console y `google-services.json` actualizado.

---

## 7. Flujo de navegación

```
[Sin sesión]
  ProfileScreen (guest) ──→ LoginScreen ──→ RegisterScreen
        ↑                         │
        └── Back                  └── Login exitoso
                                         ↓
[Con sesión — barra superior y bottom nav habilitadas]
  Home ──────────────────── (pestaña principal)
   ├── Mapa (BottomSheet) → ServiceActionCard → Cita / Domicilio
   ├── Mascota en adopción → AdoptionScreen (detalle + chat)
   └── Servicio → Cita o Domicilio (según supportsDelivery)

  Services ──────────────── (pestaña)
   └── Agendar → AppointmentScreen o DeliveryScheduleScreen → MapScreen

  Products ──────────────── (pestaña)
   └── Producto → ProductDetailScreen → CheckoutScreen

  Adoption ──────────────── (pestaña)
   └── Pet → PetDetailScreen (chat + adopción)

  Profile ────────────────── (pestaña)
   ├── Mis Mascotas
   ├── Pedidos
   ├── Favoritos
   ├── Configuración
   └── Cerrar sesión → ProfileScreen (guest) [sin nav]
```

---

## 8. Firebase y base de datos

### Colecciones Firestore

| Colección | Descripción |
|---|---|
| `users/{uid}` | Perfil de usuario: fullName, email, profilePhotoUrl, petProfile |
| `chats/{uid_petId}/messages` | Mensajes del chat de adopción |
| `services` | Catálogo de servicios: nombre, categoría, lat, lng, supportsDelivery |
| `products` | Catálogo de productos: nombre, precio, categoría, stock, descuento |
| `pets` | Mascotas en adopción: nombre, raza, edad, salud, fotos |

### Firebase Storage

| Ruta | Contenido |
|---|---|
| `users/{uid}/profile.jpg` | Foto de perfil del usuario |
| `users/{uid}/pet.jpg` | Foto de la mascota del usuario |
| `chats/{chatId}/{timestamp}.{ext}` | Adjuntos del chat (imagen, video, audio) |

---

## 9. Flujo Git recomendado

```bash
# Clonar y posicionarse en develop
git clone URL_DEL_REPOSITORIO
cd AppPetStore
git checkout develop

# Crear rama de trabajo
git checkout -b feature/nombre-corto

# Guardar cambios
git add .
git commit -m "feat: descripcion breve del cambio"
git push origin feature/nombre-corto

# Actualizar desde develop
git pull origin develop
```

### Convención de commits

| Prefijo | Uso |
|---|---|
| `feat:` | Nueva funcionalidad |
| `fix:` | Corrección de bug |
| `refactor:` | Refactorización sin cambio funcional |
| `ui:` | Cambios visuales / de diseño |
| `docs:` | Documentación |
| `chore:` | Tareas de mantenimiento (deps, config) |

---

## 10. Troubleshooting

### Google Sign-In no hace nada al pulsar el botón
1. Verifica que la huella SHA-1 de debug esté en Firebase Console:
   ```bash
   ./gradlew signingReport   # copia SHA1 de debug
   ```
2. Descarga nuevamente `google-services.json` y reemplázalo en `app/`.
3. Haz **Build → Clean Project** y luego **Run**.

### Error de instalación en emulador: `Broken pipe (32)`
1. Device Manager → **Cold Boot Now**.
2. Si persiste, **Wipe Data** del emulador.
3. Verifica aceleración de virtualización (AMD: WHPX / Intel: HAXM).

### El mapa no carga (pantalla gris)
- Verifica que `MAPS_API_KEY` en `local.properties` sea válida.
- Confirma que **Maps SDK for Android** y **Directions API** estén habilitados en Google Cloud Console.

### Fotos/videos no se cargan en el chat
- Verifica las reglas de Firebase Storage (permisos de escritura para usuarios autenticados).
- Comprueba que el archivo `google-services.json` sea el correcto para tu proyecto.

### Comandos ADB útiles (Windows)

```powershell
$adb = "C:\Users\TU_USUARIO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb kill-server
& $adb start-server
```

---

> Documentación generada con el estado actual del proyecto (2026-05-31).  
> Para casos de uso detallados e historias de usuario ver [`CASOS_DE_USO.md`](./CASOS_DE_USO.md).

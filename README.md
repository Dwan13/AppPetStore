# AppPetStore

Aplicacion Android (Kotlin + Jetpack Compose) para servicios y productos de mascotas, con mapa, seguimiento de domicilios, perfil de usuario, camara y chat con adjuntos.

## Tabla de contenido

- [1. Requisitos](#1-requisitos)
- [2. Configuracion local](#2-configuracion-local)
- [3. Flujo Git recomendado](#3-flujo-git-recomendado)
- [4. Flujo rapido: guardar cambios](#4-flujo-rapido-guardar-cambios)
- [5. Estructura principal del proyecto](#5-estructura-principal-del-proyecto)
- [6. Que ya existe en el proyecto](#6-que-ya-existe-en-el-proyecto)
- [7. Que falta y como mejorarlo](#7-que-falta-y-como-mejorarlo)
- [8. Troubleshooting](#8-troubleshooting)

## 1. Requisitos

- Android Studio reciente (Hedgehog o superior recomendado)
- JDK 11+
- SDK Android instalado
- Emulador o dispositivo fisico
- Git

## 2. Configuracion local

1) Clona el repositorio y entra a la carpeta:

```bash
git clone URL_DEL_REPOSITORIO
cd AppPetStore
```

2) Crea/ajusta `local.properties` con tu SDK y API key de Maps:

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=TU_API_KEY_DE_GOOGLE_MAPS
```

3) Sincroniza Gradle desde Android Studio y ejecuta la app.

## 3. Flujo Git recomendado

### 3.1 Clonar y cambiar a `develop`

```bash
git clone URL_DEL_REPOSITORIO
cd AppPetStore
git checkout develop
```

### 3.2 Actualizar cambios de `develop`

```bash
git pull origin develop
```

### 3.3 Crear rama de trabajo (recomendado)

```bash
git checkout -b feature/nombre-corto
```

## 4. Flujo rapido: guardar cambios

### 4.1 Revisar estado

```bash
git status
```

### 4.2 Agregar cambios

```bash
git add .
```

### 4.3 Commit

```bash
git commit -m "feat: descripcion breve del cambio"
```

### 4.4 Push

Si estas en `develop`:

```bash
git push origin develop
```

Si estas en una rama feature:

```bash
git push origin feature/nombre-corto
```

## 5. Estructura principal del proyecto

```text
app/
  src/main/java/com/project/apppetstore/
	navigation/
	ui/
	  components/
	  feature/
		home/
		services/
		products/
		map/
		delivery/
		adoption/
		profile/
	  viewmodels/
	data/model/
	utils/
```

## 6. Que ya existe en el proyecto

- Navegacion Compose con tabs principales (`Home`, `Services`, `Products`, `Adoption`, `Profile`) y rutas internas.
- Mapa con Google Maps Compose, POIs mock por categoria y ruta de domiciliario.
- Seguimiento de ubicacion con `LocationViewModel` y permisos de localizacion.
- Flujo de agendamiento antes de iniciar recorrido (`DeliverySchedule`).
- Perfil con login local, edicion de foto de perfil y datos de mascota.
- Mascota con foto (camara/galeria) y caracteristicas editables.
- Registro basico (`RegisterScreen`) integrado al flujo de perfil.
- Chat de adopcion con adjuntos (imagen, video, audio) y fallback cuando no hay app de grabacion.
- Sensores extra integrados (luz, acelerometro, giroscopio), incluido cambio de estilo del mapa segun luz ambiente.

## 7. Que falta y como mejorarlo

### Prioridad alta

- Persistencia real de datos (Room/DataStore) para no perder perfil/chat al cerrar app.
- Autenticacion real (Firebase Auth o backend propio) para login/registro.
- Endurecer manejo de errores de multimedia en todos los dispositivos.
- Tests unitarios de ViewModels y tests de navegacion/permisos criticos.

### Prioridad media

- Mover todos los textos a `strings.xml` (i18n completa).
- Mejorar arquitectura por capas (`domain`, `data`, `ui`) en features nuevas.
- Mejorar telemetria/logs para depurar fallos de emulador/dispositivo.

### Prioridad baja

- Refinamiento visual (estados vacios, loaders, microinteracciones).
- Documentacion tecnica por feature y convenciones de commits/branches.

## 8. Troubleshooting

### Error de instalacion en emulador: `Broken pipe (32)`

Acciones recomendadas:

1. Device Manager -> `Cold Boot Now`.
2. Si persiste, `Wipe Data` del emulador.
3. Verifica aceleracion de virtualizacion (AMD: WHPX/Hypervisor Platform).
4. Reinicia Android Studio y vuelve a instalar.

### Comandos utiles de depuracion ADB (Windows)

```powershell
$adb = "C:\Users\TU_USUARIO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb devices -l
& $adb kill-server
& $adb start-server
```

---

Si vas a contribuir al proyecto, usa commits pequenos y descriptivos para facilitar revisiones y rollback.


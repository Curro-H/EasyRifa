# EasyRifa — Contexto para Claude

## Qué es este proyecto
App Android nativa para organizar sorteos/rifas. Permite definir un rango numérico, asignar números a participantes (manual o automáticamente), realizar sorteos animados y compartir el estado de la rifa como imagen para WhatsApp.

**Repositorio**: https://github.com/Curro-H/EasyRifa

---

## Stack técnico
- **Kotlin + Jetpack Compose** (UI)
- **Room** (base de datos SQLite local, sin backend)
- **Hilt** (inyección de dependencias)
- **ViewModel + StateFlow** (gestión de estado)
- **Navigation Compose** (navegación)
- **Material Design 3** (tema)
- **Coil** (carga de imágenes)
- `compileSdk = 35`, `minSdk = 26`, `targetSdk = 35`

---

## Estructura del proyecto

```
app/src/main/java/com/easyrifa/
├── data/
│   ├── db/
│   │   ├── entity/         Room entities (5 tablas)
│   │   ├── dao/            Room DAOs (5 DAOs)
│   │   ├── model/          Relation data classes (Room @Relation)
│   │   └── AppDatabase.kt
│   └── repository/         Interfaces + implementaciones (4 repos)
├── domain/
│   └── usecase/
│       ├── draw/           ConductDrawUseCase
│       ├── participant/    AssignNumbersAutoUseCase
│       └── share/          CopyRaffleImageUseCase, GenerateShareImageUseCase
├── di/                     DatabaseModule, RepositoryModule (Hilt)
├── ui/
│   ├── navigation/         Screen.kt (rutas), AppNavHost.kt
│   ├── theme/              Color, Theme, Typography, Shape
│   ├── component/          Componentes reutilizables
│   └── screen/
│       ├── home/           Lista de sorteos
│       ├── raffle/         Crear/editar sorteo (+ imagen del premio)
│       ├── detail/         Detalle: participantes, grid de números, compartir
│       ├── participant/    Añadir/editar participante (manual o automático)
│       ├── draw/           Sorteo animado → ganadores
│       └── history/        Historial de sorteos anteriores
├── EasyRifaApp.kt          @HiltAndroidApp
└── MainActivity.kt
```

---

## Base de datos (Room)

| Tabla | Campos clave |
|-------|-------------|
| `raffles` | id, name, minNumber, maxNumber, imagePath?, createdAt |
| `participants` | id, raffleId (FK→CASCADE), name |
| `assigned_numbers` | id, participantId (FK→CASCADE), raffleId (FK→CASCADE), number — **UNIQUE(raffleId, number)** |
| `draw_results` | id, raffleId (FK→CASCADE), timestamp, numberOfWinners |
| `drawn_numbers` | id, drawResultId (FK→CASCADE), number, participantId, participantName |

La restricción `UNIQUE(raffleId, number)` impide asignar el mismo número a dos personas. Room lanzará una excepción si se intenta (OnConflictStrategy.ABORT).

---

## Patrones y convenciones

### Arquitectura por capa
- Cada pantalla tiene su propio `ViewModel` + `Screen` composable en `ui/screen/<nombre>/`
- Los ViewModels exponen `StateFlow<UiState>` y funciones que lanzan coroutines con `viewModelScope.launch`
- Las pantallas usan `collectAsStateWithLifecycle()` para observar el estado
- Las operaciones de BD son siempre `suspend` y se ejecutan en el ViewModel, nunca en el composable

### Añadir una nueva pantalla
1. Añadir entrada en `Screen.kt` (sealed class)
2. Añadir `composable(...)` en `AppNavHost.kt`
3. Crear `NombreViewModel.kt` con `@HiltViewModel`
4. Crear `NombreScreen.kt` con `@Composable`
5. Conectar navegación en la pantalla que la lanza

### Añadir un nuevo campo a una entidad Room
- Incrementar `version` en `AppDatabase.kt`
- Añadir una `Migration` o usar `.fallbackToDestructiveMigration()` (solo en desarrollo)
- **No usar** `fallbackToDestructiveMigration` en producción: se pierden los datos del usuario

### Inyección de dependencias
- Todo se inyecta vía Hilt. Nunca instanciar repositorios o use cases manualmente
- Repositorios: `@Singleton` en `RepositoryModule`
- DAOs: provistos en `DatabaseModule`
- Use cases: `@Inject constructor(...)` sin scope (se crean por ViewModel)

### Imágenes del sorteo
- Se copian al almacenamiento interno (`filesDir/raffle_images/`) al seleccionarlas → `CopyRaffleImageUseCase`
- `RaffleEntity.imagePath` guarda la ruta absoluta del archivo interno (no la URI de galería)
- Al borrar/reemplazar una imagen, llamar `CopyRaffleImageUseCase.deleteImage(path)` para limpiar el archivo

### Compartir imagen de estado
- `GenerateShareImageUseCase` genera un `Bitmap` usando Canvas (no Compose Graphics Layer) con el grid de números y estadísticas
- El archivo se guarda en `cacheDir/shared/` y se comparte vía `FileProvider` + `Intent.ACTION_SEND`
- El `FileProvider` está configurado en `AndroidManifest.xml` con authority `${applicationId}.fileprovider`

### NumberGrid (componente clave)
- Muestra el rango completo de números con 3 estados: AVAILABLE (verde), SELECTED_MINE (naranja), TAKEN_BY_OTHER (rojo)
- En modo edición: `onNumberToggle` añade/quita del set de `selectedNumbers`
- En modo solo lectura: pasar `readOnly = true` o usar `ReadOnlyNumberGrid`

---

## Comandos de desarrollo

```bash
# Compilar debug
./gradlew assembleDebug

# Instalar en dispositivo conectado
./gradlew installDebug

# Ejecutar tests
./gradlew test

# Build de release (requiere signing configurado)
./gradlew bundleRelease
```

> **Nota**: El archivo `gradle/wrapper/gradle-wrapper.jar` es un binario generado por Android Studio. Si no existe, abrir el proyecto en Android Studio lo descarga automáticamente, o ejecutar `gradle wrapper --gradle-version 8.10.2` si Gradle está instalado localmente.

---

## Publicación en Play Store

### Signing
```bash
# Generar keystore (solo una vez, guardar en lugar seguro)
keytool -genkeypair -v -keystore easyrifa-release.jks -alias easyrifa -keyalg RSA -keysize 2048 -validity 10000
```
- Nunca commitear `.jks` al repositorio (ya está en `.gitignore`)
- Configurar en `local.properties` (también en `.gitignore`):
  ```
  KEYSTORE_PATH=../easyrifa-release.jks
  KEYSTORE_PASSWORD=...
  KEY_ALIAS=easyrifa
  KEY_PASSWORD=...
  ```

### Build de release
```bash
./gradlew bundleRelease   # genera app/build/outputs/bundle/release/app-release.aab
```

### Checklist Play Console
- App icon 512×512 PNG sin canal alpha
- Feature graphic 1024×500 PNG
- Mínimo 2 screenshots de teléfono
- Política de privacidad (obligatoria aunque no se recojan datos)
- `targetSdk >= 35` (requerido para nuevas apps desde ago 2024)
- Activar Play App Signing al subir el primer AAB

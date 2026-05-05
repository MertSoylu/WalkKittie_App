# AGENTS.md

Guidance for OpenCode sessions working in this repository.

## Build & Test Commands

```bash
./gradlew assembleDebug           # Build debug APK
./gradlew test                    # Run unit tests
./gradlew test --tests "FQDN"     # Run single test class
./gradlew connectedAndroidTest    # Run instrumented tests
```

Do NOT run build commands unless explicitly asked.

## Architecture

Clean Architecture + MVVM, strict layer separation:

```
data/
  local/dao/          # Room DAOs
  local/entity/       # Room entities
  local/Mappers.kt    # Entity ↔ domain conversions
  repository/         # Repository implementations
  ads/AdManager.kt    # Ad management
domain/
  model/              # Domain models
  repository/         # Interfaces
ui/
  navigation/Screen.kt        # Routes (sealed class)
  screens/<feature>/          # Screen + ViewModel + UiState per feature
  components/                 # Shared composables
  theme/
di/
  DatabaseModule.kt   # Room + DataStore
  RepositoryModule.kt # Interface → Impl
worker/               # WorkManager + ReminderScheduler
```

**Flow:** Room/DataStore → Repository → ViewModel (StateFlow) → Compose UI

## Key Conventions

### Room (Database)
- Schema version: **14**. Every schema change requires a named Migration in `PatiCatDatabase.kt` companion object (inline).
- `CatEntity` is single-row (always `id = 1`). Never insert row 2+.
- `exportSchema = false`; no JSON schema files are generated.
- Migration pattern: `val MIGRATION_X_Y = Migration(X, Y) { db → … }`

### Dependency Injection
- All Hilt modules in `di/`. Interface→Impl bindings in `RepositoryModule.kt`; DB/DAO providers in `DatabaseModule.kt`.
- ViewModels use `@HiltViewModel`; screens use `hiltViewModel()`.

### State & UI
- ViewModels expose `StateFlow<UiState>`.
- Screens collect with `collectAsStateWithLifecycle()` (NOT `collectAsState()`).
- Each feature owns its `*UiState` data class.

### Navigation
- Routes defined in `Screen.kt` (sealed class objects).
- Bottom tabs: `home`, `cat`, `statistics`, `profile`.
- Other routes: `splash`, `welcome`, `language`, `setup`, `games`, `main_app`, `level_info`.

### Secrets & Config
- AdMob IDs (`ADMOB_APP_ID`, `NATIVE_AD_ID`, `FOOD_AD_ID`, `SLEEP_AD_ID`) in `local.properties`.
- Never hardcode secrets; injected via `BuildConfig` / `manifestPlaceholders`.

### Dependency Versions
- All library versions in `gradle/libs.versions.toml`. **Only update there**, never inline in `build.gradle.kts`.

### Background Work
- Step counting: `StepCounterService` (foreground) + `StepCounterManager`.
- Periodic tasks: `CatStatusWorker` (decay), `WaterReminderWorker` (notifications) via `ReminderScheduler`.
- `BootReceiver` restarts services after reboot.

### Mappers
- Entity ↔ domain conversions in `data/local/Mappers.kt`. No mapping logic in DAOs or ViewModels.

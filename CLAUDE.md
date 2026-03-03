# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## About the App

**WalkKittie** (package: `com.mert.paticat`) is a virtual pet Android app that gamifies daily health tracking. Users care for a digital cat whose hunger, happiness, and energy depend on real-world step counts and water intake. Published on Google Play.

- MinSdk 26, TargetSdk/CompileSdk 35, JVM 17
- Current version: v1.0.6 (versionCode 8)

## Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build (with minification + ProGuard)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Clean build
./gradlew clean build

# Install on connected device
./gradlew installDebug
```

Secrets (AdMob IDs, etc.) live in `local.properties` which is not tracked in git.

## Architecture

**Clean Architecture + MVVM** with three strict layers:

```
UI Layer       → screens/, components/, navigation/, theme/
Domain Layer   → domain/model/, domain/repository/ (interfaces only)
Data Layer     → data/repository/ (impls), data/local/ (Room + DataStore), data/ads/
```

**Dependency Injection**: Hilt. DI modules are in `di/`:
- `DatabaseModule` — provides Room DB, all DAOs
- `RepositoryModule` — binds domain repository interfaces to data implementations

**State management**: Each screen has a `*UiState` data class exposed via `StateFlow` from its `ViewModel`. Screens collect state with `collectAsStateWithLifecycle()`.

**Navigation**: Jetpack Navigation Compose. Routes defined as sealed class in `ui/navigation/Screen.kt`. Root graph in `PatiCatNavHost.kt`. Bottom nav has 4 tabs (Home, Cat, Games, Profile) plus onboarding/welcome flow.

**Background work**:
- `StepCounterService` — foreground service for continuous step counting via Android sensor API
- `StepCounterManager` — manages sensor binding, Health Connect fallback, and calorie calculation
- `CatStatusWorker` — WorkManager task every 30 min for hunger/energy decay
- `WaterReminderWorker` — periodic water intake notifications
- `BootReceiver` — restarts services after device reboot

## Key Files & Locations

| Concern | Location |
|---|---|
| Domain models | `domain/model/` — `Cat.kt`, `GameModels.kt`, `ShopItem.kt`, etc. |
| Cat game logic | `ui/screens/cat/CatViewModel.kt` + `GameDelegate.kt` |
| Home (steps + water) | `ui/screens/home/HomeViewModel.kt` |
| Room database | `data/local/PatiCatDatabase.kt` (v11, migrations in same file) |
| DataStore prefs | `data/local/preferences/UserPreferencesRepository.kt` |
| Ad management | `data/ads/AdManager.kt` |
| App-level init | `PatiCatApp.kt` (Hilt application, WorkManager init) |

## Database

Room database `PatiCatDatabase` at schema version 11. Entities: `CatEntity`, `DailyStatsEntity`, `MissionEntity`, `UserProfileEntity`, `ReminderSettingsEntity`, `MealEntity`, `InventoryEntity`, `CatInteractionEntity`.

Explicit migrations are defined for versions 8→9, 9→10, 10→11. Destructive migration is used as fallback for earlier versions. When adding new columns/tables, always add a named migration rather than bumping `fallbackToDestructiveMigration`.

## Domain Model: Cat

The `Cat` domain model drives all game logic:
- `hunger`, `happiness`, `energy` — 0–100 float values that decay over time
- `xp`, `level`, `coins` — progression system
- `mood: CatMood` enum — IDLE, HAPPY, HUNGRY, SLEEPING, EXCITED
- Cat state is persisted as a single `CatEntity` row (id = 1)

## Dependency Versions

Centrally managed in `gradle/libs.versions.toml`. Always update versions there, not directly in `build.gradle.kts`.

## Localization

Turkish (`values/strings.xml`) and English (`values-en/strings.xml`). Language preference stored in DataStore; applied via `AppCompatDelegate.setApplicationLocales()`.

## Ads

AdMob integration via `AdManager`. Two ad types:
- **Native ads** — shown on Home and Cat screens
- **Rewarded ads** — for mini-game rewards, reducing cat sleep time, promotions

GDPR consent handled by Google UMP SDK, initialized in `PatiCatApp`.

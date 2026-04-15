# Coach.AI Android — Foundation Guide

This document explains the app skeleton so feature developers can plug in screens
without touching shared infrastructure.

---

## Package layout

```
com.coachai
├── CoachAIApp.kt              — @HiltAndroidApp Application
├── MainActivity.kt            — single Activity, hosts NavHost
├── data/
│   ├── api/
│   │   ├── CoachAIApi.kt      — Retrofit service interface
│   │   ├── AuthInterceptor.kt — Basic-Auth OkHttp interceptor
│   │   ├── DynamicBaseUrlInterceptor.kt — reads server URL from DataStore at runtime
│   │   └── NetworkResult.kt   — sealed class: Success / Error / Loading
│   ├── model/                 — response + request DTOs
│   └── repository/            — one repository per domain (food, workout, …)
├── di/
│   └── NetworkModule.kt       — Hilt singletons: Moshi, OkHttp, Retrofit, CoachAIApi
├── ui/
│   ├── base/
│   │   ├── UiState.kt         — sealed class: Idle / Loading / Success / Error
│   │   └── BaseViewModel.kt   — optional base for state-managed ViewModels
│   ├── navigation/
│   │   ├── NavRoutes.kt       — string route constants
│   │   └── AppNavigation.kt   — NavHost + BottomNavigationBar
│   ├── screens/
│   │   ├── auth/              — LoginScreen + LoginViewModel
│   │   ├── food/              — FoodScreen + FoodViewModel
│   │   ├── workout/           — WorkoutScreen + WorkoutViewModel
│   │   ├── weight/            — WeightScreen + WeightViewModel
│   │   ├── chat/              — ChatScreen + ChatViewModel
│   │   ├── files/             — FilesScreen + FilesViewModel
│   │   └── settings/          — SettingsScreen + SettingsViewModel
│   ├── components/            — shared Composables (LoadingIndicator, ErrorMessage, …)
│   └── theme/                 — Theme.kt (Material3 light/dark)
└── util/
    └── PreferencesManager.kt  — DataStore wrapper for server URL + credentials
```

---

## Adding a new screen

1. Create `ui/screens/<feature>/MyScreen.kt` and `MyViewModel.kt`
2. Add a route constant to `NavRoutes`
3. Add a `composable(NavRoutes.MY_ROUTE) { MyScreen() }` block in `AppNavigation`
4. Optionally add a `BottomNavItem` entry in `AppNavigation` to expose it in the nav bar
5. Inject your Repository via Hilt constructor injection in the ViewModel

---

## Networking

All network calls should use `safeApiCall { }` from `NetworkResult.kt`:

```kotlin
val result = safeApiCall { repository.getFoodItems() }
when (result) {
    is NetworkResult.Success -> /* use result.data */
    is NetworkResult.Error   -> /* show result.message */
    is NetworkResult.Loading -> /* handled internally */
}
```

The server base URL is read from `PreferencesManager.serverUrl` (DataStore) by
`DynamicBaseUrlInterceptor` on every request — no need to recreate Retrofit when
the user changes the server URL in Settings.

---

## Auth

Basic Auth credentials are stored in `PreferencesManager` (DataStore) and injected
into every request by `AuthInterceptor`.

Default credentials (`coach` / `changeme`) match the backend's default
`appsettings.json` placeholders — change them in the Settings screen before use.

---

## State conventions

| Pattern | When to use |
|---------|-------------|
| `UiState<T>` (Idle/Loading/Success/Error) | Primary screen data |
| `data class FooUiState(isLoading, error, ...)` | Complex screens with multiple sub-states |

Both patterns coexist; choose whichever fits the screen complexity.

---

## Assumptions / deferred work

- No token-based auth yet — `LoginViewModel` saves Basic Auth credentials only.
  TODO: replace with `POST /api/auth/login` once endpoint is stable.
- `DynamicBaseUrlInterceptor` uses a blocking `runBlocking` to read from DataStore
  on the OkHttp thread. For v1 this is acceptable; replace with a cached value if
  latency becomes a concern.

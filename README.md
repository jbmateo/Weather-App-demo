# Weather App Demo

Android weather demo built with Kotlin, Jetpack Compose, MVVM, Room, Hilt, Retrofit, and the OpenWeather API.

## Features

- Optional login and signup screens backed by a local Room database.
- Last logged-in user state is preserved locally with SharedPreferences.
- Current device location lookup with runtime location permission.
- Main screen with two tabs:
    - Current weather: city/country, Celsius temperature, sunrise, sunset, and weather icon.
    - History: every weather response fetched when the app opens or when refresh is tapped.
- Weather icon rules:
    - Rain/drizzle conditions show a rain icon.
    - Clear/sunny conditions show a sun icon before 6 PM.
    - Clear/sunny conditions show a moon icon from 6 PM onward.
- Room is used as the local database and source for fetch history.
- Hilt provides dependency injection.
- Navigation Compose provides a `NavHost` for main and login routes. The app starts on the main weather screen, and login/signup is optional.
- Unit tests cover auth repository behavior, OpenWeather response parsing, weather mapping, and weather icon selection.

## OpenWeather API key

Live API calls will fail until you add your official key.

Create or update `local.properties` in the project root:

```properties
sdk.dir=/Users/your-name/Library/Android/sdk
openWeatherApiKey=YOUR_OFFICIAL_OPENWEATHER_KEY
```

`app/build.gradle.kts` reads `openWeatherApiKey` and exposes it as `BuildConfig.OPEN_WEATHER_API_KEY`.

## Run

Open the project in Android Studio, let Gradle sync, then run the `app` configuration.

Useful tasks once Gradle is available:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

## Architecture

- `data/local`: Room entities, DAOs, and database.
- `data/remote`: Retrofit service and OpenWeather response models.
- `data/repository`: Auth and weather repositories.
- `data/repository/LocationRepository.kt`: Current-location lookup through fused location provider.
- `domain`: UI-independent weather icon logic and domain models.
- `ui/auth`: Login/signup Compose screen and ViewModel.
- `ui/main`: Weather tabs, fetch history, and ViewModel.
- `ui/WeatherApp.kt`: Navigation Compose `NavHost` for optional login and the main weather screen.
- `di`: Hilt providers for Room, Retrofit, Moshi, and OkHttp.

## Author

Created by John Benedict Mateo.
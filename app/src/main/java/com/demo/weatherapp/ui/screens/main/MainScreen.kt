package com.demo.weatherapp.ui.screens.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.demo.weatherapp.domain.model.WeatherIconType
import com.demo.weatherapp.domain.model.WeatherInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    isLoggedIn: Boolean,
    currentUserName: String?,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.refreshWeatherForCurrentLocation()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            viewModel.refreshWeatherForCurrentLocation()
        } else {
            // Ask for Permission to access location services
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    MainContent(
        state = state,
        isLoggedIn = isLoggedIn,
        currentUserName = currentUserName,
        onLoginClick = onLoginClick,
        onLogout = onLogout,
        onRefreshByCity = viewModel::refreshWeather,
        onRefreshCurrentLocation = {
            if (context.hasLocationPermission()) {
                viewModel.refreshWeatherForCurrentLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        },
        onCityQueryChange = viewModel::onCityQueryChange,
        onTabSelected = viewModel::onTabSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainContent(
    state: MainUiState,
    isLoggedIn: Boolean,
    currentUserName: String?,
    onLoginClick: () -> Unit,
    onLogout: () -> Unit,
    onRefreshByCity: () -> Unit,
    onRefreshCurrentLocation: () -> Unit,
    onCityQueryChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(currentUserName?.let { "Hi, $it" } ?: "Weather Demo")
                },
                actions = {
                    IconButton(onClick = onRefreshCurrentLocation, enabled = !state.isLoading) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Use current location")
                    }
                    if (isLoggedIn) {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                        }
                    } else {
                        IconButton(onClick = onLoginClick) {
                            Icon(Icons.Outlined.Person, contentDescription = "Log in")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Hello! this search bar is not included in the requirements,
            // but I wanted to try out OpenWeather's api call that uses City and Country
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                value = state.cityQuery,
                placeholder = { Text("Manila, PH") },
                onValueChange = onCityQueryChange,
                label = { Text("City, country code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onRefreshByCity()
                    }
                )
            )
            TabRow(selectedTabIndex = state.selectedTab) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    text = { Text("Current") }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    text = { Text("History") }
                )
            }

            when (state.selectedTab) {
                0 -> CurrentWeatherTab(state)
                else -> WeatherHistoryTab(state.history)
            }
        }
    }
}

@Composable
private fun CurrentWeatherTab(state: MainUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        val weather = state.currentWeather
        if (weather == null) {
            Text(
                text = when {
                    state.isLocating -> "Finding your current location..."
                    state.isLoading -> "Fetching weather..."
                    else -> "No weather fetched yet."
                },
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(weather.location, style = MaterialTheme.typography.headlineSmall)
                            Text(
                                weather.conditionDescription.replaceFirstChar { it.titlecase() },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = state.currentIcon.toImageVector(),
                            contentDescription = state.currentIcon.name,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "${weather.temperatureCelsius.roundToInt()}°C",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeatherTime(label = "Sunrise", millis = weather.sunriseMillis)
                        WeatherTime(label = "Sunset", millis = weather.sunsetMillis)
                    }
                }
            }
        }
    }
}

private fun Context.hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
    return fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun WeatherHistoryTab(history: List<WeatherInfo>) {
    if (history.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No fetch history yet.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(history) { weather ->
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(weather.location, fontWeight = FontWeight.SemiBold)
                        Text(
                            formatDateTime(weather.fetchedAtMillis),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${weather.temperatureCelsius.roundToInt()}°C",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherTime(label: String, millis: Long) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(formatTime(millis), style = MaterialTheme.typography.titleMedium)
    }
}

// Get material icon for weather condition
private fun WeatherIconType.toImageVector(): ImageVector = when (this) {
    WeatherIconType.Sun -> Icons.Outlined.WbSunny
    WeatherIconType.Moon -> Icons.Outlined.DarkMode
    WeatherIconType.Rain -> Icons.Default.WaterDrop
    WeatherIconType.Cloud -> Icons.Default.Cloud
    WeatherIconType.Storm -> Icons.Default.Thunderstorm
    WeatherIconType.Snow -> Icons.Outlined.AcUnit
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault())
        .format(Date(millis))

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        .format(Date(millis))
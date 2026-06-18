package com.demo.weatherapp.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.weatherapp.data.repository.LocationRepository
import com.demo.weatherapp.data.repository.WeatherRepository
import com.demo.weatherapp.domain.WeatherIconResolver
import com.demo.weatherapp.domain.model.WeatherIconType
import com.demo.weatherapp.domain.model.WeatherInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val cityQuery: String = "",
    val currentWeather: WeatherInfo? = null,
    val history: List<WeatherInfo> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val isLocating: Boolean = false,
    val errorMessage: String? = null,
    val currentIcon: WeatherIconType = WeatherIconType.Sun
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val weatherRepository: WeatherRepository,
    private val iconResolver: WeatherIconResolver
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    init {
        observeHistory()
    }

    fun onCityQueryChange(value: String) = _uiState.update {
        it.copy(cityQuery = value, errorMessage = null)
    }

    fun onTabSelected(index: Int) = _uiState.update { it.copy(selectedTab = index) }

    // Uses city + country code query to fetch weather
    fun refreshWeather() {
        if (_uiState.value.cityQuery.isBlank()) return
        val query = _uiState.value.cityQuery
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = weatherRepository.fetchCurrentWeather(query)

            _uiState.update { state ->
                val weather = result.getOrNull() ?: state.currentWeather
                state.copy(
                    currentWeather = weather,
                    currentIcon = weather?.let {
                        iconResolver.resolve(
                            conditionId = it.conditionId,
                            conditionMain = it.conditionMain,
                            nowMillis = System.currentTimeMillis()
                        )
                    } ?: state.currentIcon,
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    // Uses current location (lat, lang) to fetch weather
    fun refreshWeatherForCurrentLocation() {
        viewModelScope.launch {
            onCityQueryChange("")
            _uiState.update { it.copy(isLocating = true, isLoading = true, errorMessage = null) }
            val locationResult = locationRepository.getCurrentLocation()
            val weatherResult = locationResult.fold(
                onSuccess = { location ->
                    weatherRepository.fetchCurrentWeather(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                },
                onFailure = { Result.failure(it) }
            )

            _uiState.update { state ->
                val weather = weatherResult.getOrNull() ?: state.currentWeather
                state.copy(
                    currentWeather = weather,
                    currentIcon = weather?.let {
                        iconResolver.resolve(
                            conditionId = it.conditionId,
                            conditionMain = it.conditionMain,
                            nowMillis = System.currentTimeMillis()
                        )
                    } ?: state.currentIcon,
                    isLocating = false,
                    isLoading = false,
                    errorMessage = weatherResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                isLocating = false,
                isLoading = false,
                errorMessage = "Location permission was denied. You can still search by city."
            )
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            weatherRepository.observeHistory().collect { records ->
                _uiState.update { state ->
                    val current = records.firstOrNull() ?: state.currentWeather
                    state.copy(
                        history = records,
                        currentWeather = current,
                        currentIcon = current?.let {
                            iconResolver.resolve(
                                conditionId = it.conditionId,
                                conditionMain = it.conditionMain,
                                nowMillis = System.currentTimeMillis()
                            )
                        } ?: state.currentIcon
                    )
                }
            }
        }
    }
}

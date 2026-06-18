package com.demo.weatherapp.data.repository

import com.demo.weatherapp.BuildConfig
import com.demo.weatherapp.data.local.dao.WeatherDao
import com.demo.weatherapp.data.mapper.toDomain
import com.demo.weatherapp.data.mapper.toEntity
import com.demo.weatherapp.data.remote.OpenWeatherService
import com.demo.weatherapp.domain.model.WeatherInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherDao: WeatherDao,
    private val openWeatherService: OpenWeatherService
) {
    fun observeHistory(): Flow<List<WeatherInfo>> =
        weatherDao.observeHistory().map { rows -> rows.map { it.toDomain() } }

    suspend fun fetchCurrentWeather(cityQuery: String): Result<WeatherInfo> {
        if (BuildConfig.OPEN_WEATHER_API_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException("Add your OpenWeather API key in local.properties to fetch live weather.")
            )
        }

        return runCatching {
            val entity = openWeatherService
                .getCurrentWeather(cityQuery = cityQuery, apiKey = BuildConfig.OPEN_WEATHER_API_KEY)
                .toEntity()

            // Insert response to local db
            weatherDao.insert(entity)

            // return result to ui
            entity.toDomain()
        }
    }

    suspend fun fetchCurrentWeather(latitude: Double, longitude: Double): Result<WeatherInfo> {
        if (BuildConfig.OPEN_WEATHER_API_KEY.isBlank()) {
            return Result.failure(
                IllegalStateException("Add your OpenWeather API key in local.properties to fetch live weather.")
            )
        }

        return runCatching {
            val entity = openWeatherService
                .getCurrentWeatherByCoordinates(
                    latitude = latitude,
                    longitude = longitude,
                    apiKey = BuildConfig.OPEN_WEATHER_API_KEY
                )
                .toEntity()

            weatherDao.insert(entity)
            entity.toDomain()
        }
    }
}

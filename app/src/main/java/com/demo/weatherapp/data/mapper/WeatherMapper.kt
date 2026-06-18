package com.demo.weatherapp.data.mapper

import com.demo.weatherapp.data.local.entity.WeatherRecordEntity
import com.demo.weatherapp.data.remote.OpenWeatherResponse
import com.demo.weatherapp.domain.model.WeatherInfo

fun OpenWeatherResponse.toEntity(fetchedAtMillis: Long = System.currentTimeMillis()): WeatherRecordEntity {
    val condition = weather.firstOrNull()
    return WeatherRecordEntity(
        fetchedAtMillis = fetchedAtMillis,
        city = name,
        country = sys.country,
        temperatureCelsius = main.temp,
        sunriseMillis = sys.sunrise * 1000,
        sunsetMillis = sys.sunset * 1000,
        conditionId = condition?.id ?: 800,
        conditionMain = condition?.main.orEmpty(),
        conditionDescription = condition?.description.orEmpty()
    )
}

fun WeatherRecordEntity.toDomain(): WeatherInfo = WeatherInfo(
    fetchedAtMillis = fetchedAtMillis,
    city = city,
    country = country,
    temperatureCelsius = temperatureCelsius,
    sunriseMillis = sunriseMillis,
    sunsetMillis = sunsetMillis,
    conditionId = conditionId,
    conditionMain = conditionMain,
    conditionDescription = conditionDescription
)

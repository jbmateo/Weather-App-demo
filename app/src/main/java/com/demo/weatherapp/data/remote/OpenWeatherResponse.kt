package com.demo.weatherapp.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenWeatherResponse(
    val name: String,
    val sys: OpenWeatherSys,
    val main: OpenWeatherMain,
    val weather: List<OpenWeatherCondition>,
    @Json(name = "dt") val measuredAtSeconds: Long
)

@JsonClass(generateAdapter = true)
data class OpenWeatherSys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

@JsonClass(generateAdapter = true)
data class OpenWeatherMain(
    val temp: Double
)

@JsonClass(generateAdapter = true)
data class OpenWeatherCondition(
    val id: Int,
    val main: String,
    val description: String
)

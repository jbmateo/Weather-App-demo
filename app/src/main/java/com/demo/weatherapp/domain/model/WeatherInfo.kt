package com.demo.weatherapp.domain.model

data class WeatherInfo(
    val fetchedAtMillis: Long,
    val city: String,
    val country: String,
    val temperatureCelsius: Double,
    val sunriseMillis: Long,
    val sunsetMillis: Long,
    val conditionId: Int,
    val conditionMain: String,
    val conditionDescription: String
) {
    val location: String = "$city, $country"
}
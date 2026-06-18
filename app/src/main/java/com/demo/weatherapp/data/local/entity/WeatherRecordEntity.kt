package com.demo.weatherapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_records")
data class WeatherRecordEntity(
    @PrimaryKey val fetchedAtMillis: Long,
    val city: String,
    val country: String,
    val temperatureCelsius: Double,
    val sunriseMillis: Long,
    val sunsetMillis: Long,
    val conditionId: Int,
    val conditionMain: String,
    val conditionDescription: String
)

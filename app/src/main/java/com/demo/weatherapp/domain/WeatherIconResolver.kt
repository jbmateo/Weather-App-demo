package com.demo.weatherapp.domain

import com.demo.weatherapp.domain.model.WeatherIconType
import java.util.Calendar
import javax.inject.Inject

class WeatherIconResolver @Inject constructor() {
    fun resolve(
        conditionId: Int,
        conditionMain: String,
        nowMillis: Long = System.currentTimeMillis()
    ): WeatherIconType {
        val main = conditionMain.lowercase()

        return when {
            conditionId in 200..232 || "thunder" in main -> WeatherIconType.Storm
            conditionId in 300..599 || "rain" in main || "drizzle" in main -> WeatherIconType.Rain
            conditionId in 600..699 || "snow" in main -> WeatherIconType.Snow
            conditionId in 801..899 || "cloud" in main -> WeatherIconType.Cloud
            isPastSixPm(nowMillis) -> WeatherIconType.Moon
            else -> WeatherIconType.Sun
        }
    }

    private fun isPastSixPm(nowMillis: Long): Boolean {

        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
        }

        return calendar.get(Calendar.HOUR_OF_DAY) >= 18
    }
}

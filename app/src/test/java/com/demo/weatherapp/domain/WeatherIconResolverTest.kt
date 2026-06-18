package com.demo.weatherapp.domain

import com.demo.weatherapp.domain.model.WeatherIconType
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar

class WeatherIconResolverTest {
    private val resolver = WeatherIconResolver()

    @Test
    fun clearWeatherBeforeSixPmUsesSun() {
        val icon = resolver.resolve(
            conditionId = 800,
            conditionMain = "Clear",
            nowMillis = millisAt(17, 59)
        )

        assertThat(icon).isEqualTo(WeatherIconType.Sun)
    }

    @Test
    fun clearWeatherAfterSixPmUsesMoon() {
        val icon = resolver.resolve(
            conditionId = 800,
            conditionMain = "Clear",
            nowMillis = millisAt(18, 0)
        )

        assertThat(icon).isEqualTo(WeatherIconType.Moon)
    }

    @Test
    fun rainWeatherUsesRainIconEvenAfterSixPm() {
        val icon = resolver.resolve(
            conditionId = 501,
            conditionMain = "Rain",
            nowMillis = millisAt(20, 0)
        )

        assertThat(icon).isEqualTo(WeatherIconType.Rain)
    }

    private fun millisAt(hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JUNE)
            set(Calendar.DAY_OF_MONTH, 18)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}

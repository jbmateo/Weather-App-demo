package com.demo.weatherapp.mapper

import com.demo.weatherapp.data.mapper.toEntity
import com.demo.weatherapp.data.remote.OpenWeatherCondition
import com.demo.weatherapp.data.remote.OpenWeatherMain
import com.demo.weatherapp.data.remote.OpenWeatherResponse
import com.demo.weatherapp.data.remote.OpenWeatherSys
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeatherMappersTest {
    @Test
    fun openWeatherResponseMapsToWeatherRecord() {
        val response = OpenWeatherResponse(
            name = "Manila",
            sys = OpenWeatherSys(
                country = "PH",
                sunrise = 1718658984,
                sunset = 1718706046
            ),
            main = OpenWeatherMain(temp = 30.4),
            weather = listOf(
                OpenWeatherCondition(
                    id = 501,
                    main = "Rain",
                    description = "moderate rain"
                )
            ),
            measuredAtSeconds = 1718685600
        )

        val entity = response.toEntity(fetchedAtMillis = 123_456L)

        assertThat(entity.fetchedAtMillis).isEqualTo(123_456L)
        assertThat(entity.city).isEqualTo("Manila")
        assertThat(entity.country).isEqualTo("PH")
        assertThat(entity.temperatureCelsius).isEqualTo(30.4)
        assertThat(entity.sunriseMillis).isEqualTo(1_718_658_984_000L)
        assertThat(entity.sunsetMillis).isEqualTo(1_718_706_046_000L)
        assertThat(entity.conditionId).isEqualTo(501)
        assertThat(entity.conditionMain).isEqualTo("Rain")
        assertThat(entity.conditionDescription).isEqualTo("moderate rain")
    }

    @Test
    fun openWeatherResponseWithNoConditionUsesClearDefault() {
        val response = OpenWeatherResponse(
            name = "Manila",
            sys = OpenWeatherSys(
                country = "PH",
                sunrise = 1718658984,
                sunset = 1718706046
            ),
            main = OpenWeatherMain(temp = 30.4),
            weather = emptyList(),
            measuredAtSeconds = 1718685600
        )

        val entity = response.toEntity(fetchedAtMillis = 123_456L)

        assertThat(entity.conditionId).isEqualTo(800)
        assertThat(entity.conditionMain).isEmpty()
        assertThat(entity.conditionDescription).isEmpty()
    }
}
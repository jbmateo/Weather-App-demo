package com.demo.weatherapp.remote

import com.demo.weatherapp.data.remote.OpenWeatherResponse
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Test

class OpenWeatherResponseTest {
    private val adapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(OpenWeatherResponse::class.java)

    @Test
    fun parsesCurrentWeatherResponse() {
        val response = adapter.fromJson(OPEN_WEATHER_JSON)

        assertThat(response).isNotNull()
        assertThat(response?.name).isEqualTo("Manila")
        assertThat(response?.sys?.country).isEqualTo("PH")
        assertThat(response?.main?.temp).isEqualTo(30.4)
        assertThat(response?.sys?.sunrise).isEqualTo(1718658984)
        assertThat(response?.sys?.sunset).isEqualTo(1718706046)
        assertThat(response?.weather?.first()?.id).isEqualTo(501)
        assertThat(response?.weather?.first()?.main).isEqualTo("Rain")
        assertThat(response?.weather?.first()?.description).isEqualTo("moderate rain")
    }

    private companion object {
        const val OPEN_WEATHER_JSON = """
            {
              "weather": [
                {
                  "id": 501,
                  "main": "Rain",
                  "description": "moderate rain"
                }
              ],
              "main": {
                "temp": 30.4
              },
              "dt": 1718685600,
              "sys": {
                "country": "PH",
                "sunrise": 1718658984,
                "sunset": 1718706046
              },
              "name": "Manila"
            }
        """
    }
}
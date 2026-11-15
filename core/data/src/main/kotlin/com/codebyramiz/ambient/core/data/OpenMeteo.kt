package com.codebyramiz.ambient.core.data

import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.Weather
import com.codebyramiz.ambient.core.model.WeatherCondition
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Fetches the body of a URL. A seam so the weather source is testable without a network. */
fun interface HttpFetcher {
    @Throws(IOException::class)
    suspend fun get(url: String): String
}

class UrlConnectionFetcher @Inject constructor() : HttpFetcher {
    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw IOException("HTTP $code")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
    }
}

/** Open-Meteo "current conditions" source. No API key needed. */
class OpenMeteoWeatherSource @Inject constructor(private val fetcher: HttpFetcher) {
    suspend fun fetch(city: City): Weather {
        val body = fetcher.get(urlFor(city))
        val current = try {
            JSON.decodeFromString<ForecastResponse>(body).current
        } catch (e: kotlinx.serialization.SerializationException) {
            throw IOException("Unreadable forecast response", e)
        }
        return Weather(
            cityId = city.id,
            temperatureCelsius = current.temperature,
            condition = WeatherCondition.fromWmoCode(current.weatherCode),
            isDay = current.isDay != 0,
        )
    }

    internal fun urlFor(city: City): String =
        "https://api.open-meteo.com/v1/forecast?latitude=${city.latitude}&longitude=${city.longitude}" +
            "&current=temperature_2m,weather_code,is_day&timezone=auto"

    @Serializable
    private data class ForecastResponse(val current: Current)

    @Serializable
    private data class Current(
        @SerialName("temperature_2m") val temperature: Double,
        @SerialName("weather_code") val weatherCode: Int,
        @SerialName("is_day") val isDay: Int = 1,
    )

    private companion object {
        val JSON = Json { ignoreUnknownKeys = true }
    }
}

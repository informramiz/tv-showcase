package com.codebyramiz.ambient.core.data

import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.TemperatureUnit
import com.codebyramiz.ambient.core.model.WeatherState
import java.io.IOException
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(initial: AmbientSettings = AmbientSettings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: Flow<AmbientSettings> = state

    override suspend fun setCity(city: City) = state.update { it.copy(city = city) }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) = state.update { it.copy(temperatureUnit = unit) }

    override suspend fun setSlideIntervalSeconds(seconds: Int) = state.update {
        it.copy(slideIntervalSeconds = seconds)
    }

    override suspend fun setUse24HourClock(enabled: Boolean) = state.update { it.copy(use24HourClock = enabled) }
}

class ScriptedFetcher(private val responses: MutableList<Result<String>>) : HttpFetcher {
    val requested = mutableListOf<String>()

    override suspend fun get(url: String): String {
        requested += url
        val next = if (responses.size > 1) responses.removeAt(0) else responses.first()
        return next.getOrElse { throw it as? IOException ?: IOException(it) }
    }
}

fun forecastJson(temperature: Double, code: Int, isDay: Int = 1) =
    """{"latitude":1.0,"current":{"time":"2026-11-15T10:00","temperature_2m":$temperature,""" +
        """"weather_code":$code,"is_day":$isDay}}"""

class FakeClockRepository(time: LocalDateTime = LocalDateTime.of(2026, 11, 15, 9, 41)) : ClockRepository {
    override val minutes: Flow<LocalDateTime> = flowOf(time)
}

class FakeWeatherRepository : WeatherRepository {
    val requestedCities = mutableListOf<City>()

    override fun observe(city: City): Flow<WeatherState> {
        requestedCities += city
        return flowOf(WeatherState.Loading)
    }
}

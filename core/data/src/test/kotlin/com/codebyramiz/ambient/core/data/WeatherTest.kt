package com.codebyramiz.ambient.core.data

import app.cash.turbine.test
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.WeatherCondition
import com.codebyramiz.ambient.core.model.WeatherState
import java.io.IOException
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherTest {
    private val city = City.ALL.first { it.id == "tokyo" }

    private fun repository(fetcher: ScriptedFetcher) =
        OpenMeteoWeatherRepository(OpenMeteoWeatherSource(fetcher), 15.minutes, 1.minutes)

    @Test
    fun parsesCurrentConditionsAndBuildsTheRequestFromTheCity() = runTest {
        val fetcher = ScriptedFetcher(mutableListOf(Result.success(forecastJson(18.6, 61, isDay = 0))))
        val weather = OpenMeteoWeatherSource(fetcher).fetch(city)

        assertEquals(18.6, weather.temperatureCelsius, 0.0)
        assertEquals(WeatherCondition.RAIN, weather.condition)
        assertEquals(false, weather.isDay)
        assertTrue(fetcher.requested.single().contains("latitude=35.6762&longitude=139.6503"))
    }

    @Test(expected = IOException::class)
    fun malformedBodyIsAnIoFailure() = runTest {
        OpenMeteoWeatherSource(ScriptedFetcher(mutableListOf(Result.success("{}")))).fetch(city)
    }

    @Test
    fun emitsLoadingThenWeatherThenRefreshes() = runTest {
        val fetcher = ScriptedFetcher(
            mutableListOf(Result.success(forecastJson(10.0, 0)), Result.success(forecastJson(12.0, 3))),
        )
        repository(fetcher).observe(city).test {
            assertEquals(WeatherState.Loading, awaitItem())
            val first = awaitItem() as WeatherState.Available
            assertEquals(10.0, first.weather.temperatureCelsius, 0.0)
            val second = awaitItem() as WeatherState.Available
            assertEquals(WeatherCondition.CLOUDY, second.weather.condition)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, fetcher.requested.size)
    }

    @Test
    fun failureWithoutAReadingIsUnavailable() = runTest {
        val fetcher = ScriptedFetcher(mutableListOf(Result.failure(IOException("offline"))))
        repository(fetcher).observe(city).test {
            assertEquals(WeatherState.Loading, awaitItem())
            assertEquals(WeatherState.Unavailable, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun failureAfterASuccessKeepsTheLastReadingAsStale() = runTest {
        val fetcher = ScriptedFetcher(
            mutableListOf(Result.success(forecastJson(10.0, 0)), Result.failure(IOException("offline"))),
        )
        repository(fetcher).observe(city).test {
            assertEquals(WeatherState.Loading, awaitItem())
            assertTrue(!(awaitItem() as WeatherState.Available).isStale)
            val stale = awaitItem() as WeatherState.Available
            assertTrue(stale.isStale)
            assertEquals(10.0, stale.weather.temperatureCelsius, 0.0)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

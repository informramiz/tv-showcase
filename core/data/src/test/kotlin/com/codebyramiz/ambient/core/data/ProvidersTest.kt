package com.codebyramiz.ambient.core.data

import app.cash.turbine.test
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.TemperatureUnit
import java.time.LocalDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProvidersTest {
    @Test
    fun clockEmitsNowThenAlignsToTheNextMinute() = runTest {
        val times = ArrayDeque(listOf(LocalDateTime.of(2026, 1, 1, 10, 0, 45), LocalDateTime.of(2026, 1, 1, 10, 1, 0)))
        val repo = SystemClockRepository { times.removeFirst() }
        repo.minutes.test {
            assertEquals(0, awaitItem().minute)
            // 45 s into the minute, so the next emission is due in 15 s.
            expectNoEvents()
            testScheduler.advanceTimeBy(14_999)
            expectNoEvents()
            testScheduler.advanceTimeBy(2)
            assertEquals(1, awaitItem().minute)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun infoReflectsSettingsAndOnlyCityChangesRestartWeather() = runTest {
        val settings = FakeSettingsRepository()
        val weather = FakeWeatherRepository()
        val provider = AmbientInfoProvider(settings, FakeClockRepository(), weather)

        provider.info.test {
            assertEquals(City.DEFAULT, awaitItem().city)

            settings.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
            assertEquals(TemperatureUnit.FAHRENHEIT, awaitItem().temperatureUnit)
            assertEquals(1, weather.requestedCities.size)

            val tokyo = City.ALL.first { it.id == "tokyo" }
            settings.setCity(tokyo)
            assertEquals(tokyo, awaitItem().city)
            assertEquals(listOf(City.DEFAULT, tokyo), weather.requestedCities)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

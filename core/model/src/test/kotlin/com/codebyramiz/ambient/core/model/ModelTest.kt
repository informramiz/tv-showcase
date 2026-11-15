package com.codebyramiz.ambient.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {
    @Test
    fun wmoCodesMapToConditions() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromWmoCode(0))
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromWmoCode(45))
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromWmoCode(81))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmoCode(75))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmoCode(99))
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromWmoCode(1234))
    }

    @Test
    fun temperatureFormatsInBothUnits() {
        assertEquals("21°C", TemperatureUnit.CELSIUS.format(21.4))
        assertEquals("70°F", TemperatureUnit.FAHRENHEIT.format(21.0))
        assertEquals("-4°F", TemperatureUnit.FAHRENHEIT.format(-20.0))
        assertEquals(TemperatureUnit.FAHRENHEIT, TemperatureUnit.CELSIUS.next)
        assertEquals(TemperatureUnit.CELSIUS, TemperatureUnit.FAHRENHEIT.next)
    }

    @Test
    fun unknownCityIdFallsBackToDefault() {
        assertEquals(City.DEFAULT, City.fromIdOrDefault("atlantis"))
        assertEquals("tokyo", City.fromIdOrDefault("tokyo").id)
        assertEquals(City.ALL.size, City.ALL.map { it.id }.toSet().size)
    }

    @Test
    fun slideIntervalCyclesAndRecoversFromUnknownValues() {
        assertEquals(10, AmbientSettings.nextInterval(5))
        assertEquals(5, AmbientSettings.nextInterval(30))
        assertEquals(AmbientSettings.DEFAULT_SLIDE_INTERVAL_SECONDS, AmbientSettings.nextInterval(7))
        assertTrue(AmbientSettings.DEFAULT_SLIDE_INTERVAL_SECONDS in AmbientSettings.SLIDE_INTERVAL_OPTIONS_SECONDS)
    }
}

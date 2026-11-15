package com.codebyramiz.ambient.core.model

import kotlin.math.roundToInt

enum class WeatherCondition {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM,
    ;

    val label: String
        get() = when (this) {
            CLEAR -> "Clear"
            PARTLY_CLOUDY -> "Partly cloudy"
            CLOUDY -> "Cloudy"
            FOG -> "Fog"
            DRIZZLE -> "Drizzle"
            RAIN -> "Rain"
            SNOW -> "Snow"
            THUNDERSTORM -> "Thunderstorm"
        }

    companion object {
        /** Maps a WMO weather interpretation code, as returned by Open-Meteo, to a condition. */
        fun fromWmoCode(code: Int): WeatherCondition = when (code) {
            0, 1 -> CLEAR
            2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else -> CLOUDY
        }
    }
}

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F"),
    ;

    fun format(celsius: Double): String {
        val value = if (this == CELSIUS) celsius else celsius * 9.0 / 5.0 + 32.0
        return "${value.roundToInt()}$symbol"
    }

    val next: TemperatureUnit get() = entries[(ordinal + 1) % entries.size]
}

/** Current conditions for [cityId]. The temperature is always Celsius; [TemperatureUnit] formats it. */
data class Weather(
    val cityId: String,
    val temperatureCelsius: Double,
    val condition: WeatherCondition,
    val isDay: Boolean,
)

sealed interface WeatherState {
    data object Loading : WeatherState

    /** [isStale] is true when the latest refresh failed and [weather] is the last good reading. */
    data class Available(val weather: Weather, val isStale: Boolean = false) : WeatherState

    data object Unavailable : WeatherState
}

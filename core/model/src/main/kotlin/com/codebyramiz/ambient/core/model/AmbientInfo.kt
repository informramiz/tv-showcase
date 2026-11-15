package com.codebyramiz.ambient.core.model

import java.time.LocalDateTime

/** Everything the clock and weather overlay needs, already resolved against the user's settings. */
data class AmbientInfo(
    val time: LocalDateTime,
    val city: City,
    val temperatureUnit: TemperatureUnit,
    val use24HourClock: Boolean,
    val weather: WeatherState,
)

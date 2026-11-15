package com.codebyramiz.ambient.core.data

import com.codebyramiz.ambient.core.model.AmbientInfo
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/** Joins settings, clock and weather into the single stream the overlay renders. */
class AmbientInfoProvider @Inject constructor(
    private val settings: SettingsRepository,
    private val clock: ClockRepository,
    private val weather: WeatherRepository,
) {
    // Only a city change restarts the weather flow; unit and clock-format changes just re-render.
    @OptIn(ExperimentalCoroutinesApi::class)
    val info: Flow<AmbientInfo> = settings.settings
        .map { it.city }
        .distinctUntilChanged()
        .flatMapLatest { city ->
            combine(settings.settings, clock.minutes, weather.observe(city)) { current, time, state ->
                AmbientInfo(
                    time = time,
                    city = current.city,
                    temperatureUnit = current.temperatureUnit,
                    use24HourClock = current.use24HourClock,
                    weather = state,
                )
            }
        }
}

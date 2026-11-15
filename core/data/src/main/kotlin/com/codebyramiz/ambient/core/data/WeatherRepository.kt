package com.codebyramiz.ambient.core.data

import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.Weather
import com.codebyramiz.ambient.core.model.WeatherState
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface WeatherRepository {
    /** Emits [WeatherState.Loading], then a fresh reading, and keeps refreshing while collected. */
    fun observe(city: City): Flow<WeatherState>
}

class OpenMeteoWeatherRepository(
    private val source: OpenMeteoWeatherSource,
    private val refreshInterval: Duration,
    private val retryInterval: Duration,
) : WeatherRepository {
    @Inject
    constructor(source: OpenMeteoWeatherSource) : this(source, REFRESH_INTERVAL, RETRY_INTERVAL)

    override fun observe(city: City): Flow<WeatherState> = flow {
        emit(WeatherState.Loading)
        var lastGood: Weather? = null
        while (true) {
            val wait = try {
                val weather = source.fetch(city)
                lastGood = weather
                emit(WeatherState.Available(weather))
                refreshInterval
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                val previous = lastGood
                emit(
                    if (previous !=
                        null
                    ) {
                        WeatherState.Available(previous, isStale = true)
                    } else {
                        WeatherState.Unavailable
                    },
                )
                retryInterval
            }
            delay(wait)
        }
    }

    companion object {
        val REFRESH_INTERVAL = 15.minutes
        val RETRY_INTERVAL = 1.minutes
    }
}

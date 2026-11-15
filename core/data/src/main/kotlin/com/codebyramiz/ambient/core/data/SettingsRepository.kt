package com.codebyramiz.ambient.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.TemperatureUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val settings: Flow<AmbientSettings>

    suspend fun setCity(city: City)

    suspend fun setTemperatureUnit(unit: TemperatureUnit)

    suspend fun setSlideIntervalSeconds(seconds: Int)

    suspend fun setUse24HourClock(enabled: Boolean)
}

class DataStoreSettingsRepository @Inject constructor(private val store: DataStore<Preferences>) : SettingsRepository {
    override val settings: Flow<AmbientSettings> = store.data
        .map { prefs ->
            AmbientSettings(
                city = City.fromIdOrDefault(prefs[KEY_CITY]),
                temperatureUnit = TemperatureUnit.entries.firstOrNull { it.name == prefs[KEY_UNIT] }
                    ?: TemperatureUnit.CELSIUS,
                slideIntervalSeconds = prefs[KEY_INTERVAL]
                    ?.takeIf { it in AmbientSettings.SLIDE_INTERVAL_OPTIONS_SECONDS }
                    ?: AmbientSettings.DEFAULT_SLIDE_INTERVAL_SECONDS,
                use24HourClock = prefs[KEY_24H] ?: true,
            )
        }
        .distinctUntilChanged()

    override suspend fun setCity(city: City) {
        store.edit { it[KEY_CITY] = city.id }
    }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        store.edit { it[KEY_UNIT] = unit.name }
    }

    override suspend fun setSlideIntervalSeconds(seconds: Int) {
        store.edit { it[KEY_INTERVAL] = seconds }
    }

    override suspend fun setUse24HourClock(enabled: Boolean) {
        store.edit { it[KEY_24H] = enabled }
    }

    private companion object {
        val KEY_CITY = stringPreferencesKey("city_id")
        val KEY_UNIT = stringPreferencesKey("temperature_unit")
        val KEY_INTERVAL = intPreferencesKey("slide_interval_seconds")
        val KEY_24H = booleanPreferencesKey("use_24_hour_clock")
    }
}

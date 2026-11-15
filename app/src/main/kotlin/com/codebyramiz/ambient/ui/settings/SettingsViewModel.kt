package com.codebyramiz.ambient.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebyramiz.ambient.core.data.SettingsRepository
import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AmbientSettings> =
        repository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            AmbientSettings(),
        )

    fun selectCity(city: City) {
        viewModelScope.launch { repository.setCity(city) }
    }

    fun toggleTemperatureUnit() {
        viewModelScope.launch { repository.setTemperatureUnit(current().temperatureUnit.next) }
    }

    fun cycleSlideInterval() {
        viewModelScope.launch {
            repository.setSlideIntervalSeconds(AmbientSettings.nextInterval(current().slideIntervalSeconds))
        }
    }

    fun toggle24HourClock() {
        viewModelScope.launch { repository.setUse24HourClock(!current().use24HourClock) }
    }

    // Read the stored value, not the UI mirror, so a toggle never works from a not-yet-loaded default.
    private suspend fun current(): AmbientSettings = repository.settings.first()

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

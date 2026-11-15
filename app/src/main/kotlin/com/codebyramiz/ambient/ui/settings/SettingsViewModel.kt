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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    val settings: StateFlow<AmbientSettings> =
        repository.settings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            AmbientSettings(),
        )

    // The read-modify-write toggles run one at a time, so rapid presses never start from the same value.
    private val writes = Mutex()

    /** Suspends until the city is stored, so the caller can navigate away without cancelling the write. */
    suspend fun selectCity(city: City) = repository.setCity(city)

    fun toggleTemperatureUnit() = serialized { repository.setTemperatureUnit(current().temperatureUnit.next) }

    fun cycleSlideInterval() = serialized {
        repository.setSlideIntervalSeconds(AmbientSettings.nextInterval(current().slideIntervalSeconds))
    }

    fun toggle24HourClock() = serialized { repository.setUse24HourClock(!current().use24HourClock) }

    private fun serialized(block: suspend () -> Unit) {
        viewModelScope.launch { writes.withLock { block() } }
    }

    // Read the stored value, not the UI mirror, so a toggle never works from a not-yet-loaded default.
    private suspend fun current(): AmbientSettings = repository.settings.first()

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

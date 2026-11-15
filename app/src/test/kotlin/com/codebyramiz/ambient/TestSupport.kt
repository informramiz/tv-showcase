package com.codebyramiz.ambient

import com.codebyramiz.ambient.core.data.SettingsRepository
import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.TemperatureUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = StandardTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeSettingsRepository(initial: AmbientSettings = AmbientSettings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: Flow<AmbientSettings> = state

    override suspend fun setCity(city: City) = state.update { it.copy(city = city) }

    override suspend fun setTemperatureUnit(unit: TemperatureUnit) = state.update { it.copy(temperatureUnit = unit) }

    override suspend fun setSlideIntervalSeconds(seconds: Int) = state.update {
        it.copy(slideIntervalSeconds = seconds)
    }

    override suspend fun setUse24HourClock(enabled: Boolean) = state.update { it.copy(use24HourClock = enabled) }
}

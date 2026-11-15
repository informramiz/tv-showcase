package com.codebyramiz.ambient.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.TemperatureUnit
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun file() = File(folder.root, "settings.preferences_pb")

    private fun open(scope: CoroutineScope) =
        DataStoreSettingsRepository(PreferenceDataStoreFactory.create(scope = scope) { file() })

    @Test
    fun defaultsWhenNothingIsStored() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        assertEquals(AmbientSettings(), open(scope).settings.first())
        scope.cancel()
    }

    @Test
    fun choicesSurviveReopeningTheStore() = runBlocking {
        val first = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val tokyo = City.ALL.first { it.id == "tokyo" }
        open(first).apply {
            setCity(tokyo)
            setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
            setSlideIntervalSeconds(30)
            setUse24HourClock(false)
        }
        first.cancel()

        // A new store over the same file stands in for the process coming back after death.
        val second = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        assertEquals(
            AmbientSettings(tokyo, TemperatureUnit.FAHRENHEIT, 30, use24HourClock = false),
            open(second).settings.first(),
        )
        second.cancel()
    }

    @Test
    fun unsupportedStoredIntervalFallsBackToDefault() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val repo = open(scope)
        repo.setSlideIntervalSeconds(7)
        assertEquals(AmbientSettings.DEFAULT_SLIDE_INTERVAL_SECONDS, repo.settings.first().slideIntervalSeconds)
        scope.cancel()
    }
}

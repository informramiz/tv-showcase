package com.codebyramiz.ambient.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.TemperatureUnit
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Keeps preferences in memory: the logic tests need no file, so no platform file-rename quirks. */
private class InMemoryPreferences : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

class SettingsRepositoryTest {
    @get:Rule
    val folder = TemporaryFolder()

    @Test
    fun defaultsWhenNothingIsStored() = runBlocking {
        assertEquals(AmbientSettings(), DataStoreSettingsRepository(InMemoryPreferences()).settings.first())
    }

    @Test
    fun everyChoiceIsStoredAndRead() = runBlocking {
        val repo = DataStoreSettingsRepository(InMemoryPreferences())
        val tokyo = City.ALL.first { it.id == "tokyo" }
        repo.setCity(tokyo)
        repo.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        repo.setSlideIntervalSeconds(30)
        repo.setUse24HourClock(false)
        assertEquals(
            AmbientSettings(tokyo, TemperatureUnit.FAHRENHEIT, 30, use24HourClock = false),
            repo.settings.first(),
        )
    }

    @Test
    fun unsupportedStoredIntervalFallsBackToDefault() = runBlocking {
        val repo = DataStoreSettingsRepository(InMemoryPreferences())
        repo.setSlideIntervalSeconds(7)
        assertEquals(AmbientSettings.DEFAULT_SLIDE_INTERVAL_SECONDS, repo.settings.first().slideIntervalSeconds)
    }

    @Test
    fun aChoiceOnDiskSurvivesReopeningTheStore() = runBlocking {
        val file = File(folder.root, "settings.preferences_pb")
        val tokyo = City.ALL.first { it.id == "tokyo" }
        val firstJob: Job = SupervisorJob()
        DataStoreSettingsRepository(
            PreferenceDataStoreFactory.create(scope = CoroutineScope(firstJob + Dispatchers.IO)) {
                file
            },
        )
            .setCity(tokyo)
        firstJob.cancelAndJoin()

        // A new store over the same file stands in for the process coming back after death.
        val secondJob: Job = SupervisorJob()
        val reopened = DataStoreSettingsRepository(
            PreferenceDataStoreFactory.create(scope = CoroutineScope(secondJob + Dispatchers.IO)) { file },
        )
        assertEquals(tokyo, reopened.settings.first().city)
        secondJob.cancelAndJoin()
    }
}

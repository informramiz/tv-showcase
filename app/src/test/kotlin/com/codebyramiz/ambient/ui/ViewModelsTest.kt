package com.codebyramiz.ambient.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.codebyramiz.ambient.FakeSettingsRepository
import com.codebyramiz.ambient.MainDispatcherRule
import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.TemperatureUnit
import com.codebyramiz.ambient.ui.settings.SettingsViewModel
import com.codebyramiz.ambient.ui.slideshow.SlideshowViewModel
import com.codebyramiz.ambient.ui.video.VideoViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SlideshowViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private fun viewModel(
        seconds: Int = 10,
        handle: SavedStateHandle = SavedStateHandle(),
        settings: FakeSettingsRepository = FakeSettingsRepository(AmbientSettings(slideIntervalSeconds = seconds)),
    ) = SlideshowViewModel(settings, handle).also { created += it }

    private val created = mutableListOf<SlideshowViewModel>()

    /** The auto-advance timer never ends, so scopes are cancelled before runTest drains its scheduler. */
    private fun slideshowTest(body: suspend TestScope.() -> Unit) = runTest(main.dispatcher) {
        try {
            body()
        } finally {
            created.forEach { it.viewModelScope.cancel() }
        }
    }

    @Test
    fun advancesOnTheConfiguredInterval() = slideshowTest {
        val vm = viewModel(seconds = 5)
        runCurrent()
        assertEquals(0, vm.state.value.position)
        advanceTimeBy(5_001)
        assertEquals(1, vm.state.value.position)
        advanceTimeBy(5_000)
        assertEquals(2, vm.state.value.position)
    }

    @Test
    fun manualNavigationRestartsTheTimerAndWrapsBackwards() = slideshowTest {
        val vm = viewModel(seconds = 10)
        runCurrent()
        advanceTimeBy(9_000)
        vm.previous()
        runCurrent()
        assertEquals(-1, vm.state.value.position)
        // 9 s after the press is still inside the restarted 10 s interval.
        advanceTimeBy(9_000)
        assertEquals(-1, vm.state.value.position)
        advanceTimeBy(1_500)
        assertEquals(0, vm.state.value.position)
    }

    @Test
    fun pausingStopsAdvancingAndResumingContinues() = slideshowTest {
        val vm = viewModel(seconds = 5)
        runCurrent()
        vm.togglePause()
        runCurrent()
        assertTrue(vm.state.value.paused)
        advanceTimeBy(60_000)
        assertEquals(0, vm.state.value.position)
        vm.togglePause()
        runCurrent()
        advanceTimeBy(5_001)
        assertEquals(1, vm.state.value.position)
    }

    @Test
    fun positionAndPauseSurviveProcessDeathThroughSavedState() = slideshowTest {
        val handle = SavedStateHandle()
        val first = viewModel(handle = handle)
        runCurrent()
        first.next()
        first.next()
        first.togglePause()
        runCurrent()

        // A new ViewModel over the same saved state stands in for the restored process.
        val restored = viewModel(handle = SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) }))
        runCurrent()
        assertEquals(2, restored.state.value.position)
        assertTrue(restored.state.value.paused)
    }

    @Test
    fun changingTheIntervalInSettingsTakesEffectImmediately() = slideshowTest {
        val settings = FakeSettingsRepository(AmbientSettings(slideIntervalSeconds = 30))
        val vm = viewModel(settings = settings)
        runCurrent()
        settings.setSlideIntervalSeconds(5)
        runCurrent()
        advanceTimeBy(5_001)
        assertEquals(1, vm.state.value.position)
    }
}

class VideoViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun togglesPlaybackAndSoundAndRestoresThem() = runTest(main.dispatcher) {
        val handle = SavedStateHandle()
        val vm = VideoViewModel(handle)
        assertTrue(vm.state.value.playing)
        vm.togglePlaying()
        vm.toggleMuted()
        runCurrent()
        assertFalse(vm.state.value.playing)
        assertTrue(vm.state.value.muted)

        val restored = VideoViewModel(SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) }))
        runCurrent()
        assertFalse(restored.state.value.playing)
        assertTrue(restored.state.value.muted)
    }
}

class SettingsViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    @Test
    fun everyChoiceIsWrittenToTheRepository() = runTest(main.dispatcher) {
        val repository = FakeSettingsRepository()
        val vm = SettingsViewModel(repository)
        vm.settings.test {
            assertEquals(AmbientSettings(), awaitItem())
            val tokyo = City.ALL.first { it.id == "tokyo" }
            vm.selectCity(tokyo)
            assertEquals(tokyo, awaitItem().city)
            vm.toggleTemperatureUnit()
            assertEquals(TemperatureUnit.FAHRENHEIT, awaitItem().temperatureUnit)
            vm.cycleSlideInterval()
            assertEquals(15, awaitItem().slideIntervalSeconds)
            vm.toggle24HourClock()
            assertFalse(awaitItem().use24HourClock)
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(15, repository.settings.first().slideIntervalSeconds)
    }

    @Test
    fun rapidTogglesApplyOneAfterAnother() = runTest(main.dispatcher) {
        val repository = FakeSettingsRepository()
        val vm = SettingsViewModel(repository)
        vm.toggle24HourClock()
        vm.toggle24HourClock()
        vm.toggle24HourClock()
        advanceUntilIdle()
        assertFalse(repository.settings.first().use24HourClock)
    }
}

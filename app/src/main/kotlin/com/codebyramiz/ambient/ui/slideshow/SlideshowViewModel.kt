package com.codebyramiz.ambient.ui.slideshow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebyramiz.ambient.core.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SlideshowUiState(val position: Int = 0, val paused: Boolean = false)

/**
 * Drives the slideshow. The position is an ever-growing counter that the UI maps onto the scene
 * list, and both position and pause state live in [SavedStateHandle] so process death keeps them.
 */
@HiltViewModel
class SlideshowViewModel @Inject constructor(settings: SettingsRepository, private val savedState: SavedStateHandle) :
    ViewModel() {
    private val position = savedState.getStateFlow(KEY_POSITION, 0)
    private val paused = savedState.getStateFlow(KEY_PAUSED, false)

    /** Bumped on manual navigation so the auto-advance timer starts a full interval again. */
    private val timerResets = MutableStateFlow(0)

    val state: StateFlow<SlideshowUiState> = combine(position, paused, ::SlideshowUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, SlideshowUiState())

    init {
        val intervals = settings.settings.map { it.slideIntervalSeconds }.distinctUntilChanged()
        viewModelScope.launch {
            combine(intervals, paused, timerResets) { interval, isPaused, _ -> interval to isPaused }
                .collectLatest { (interval, isPaused) ->
                    if (isPaused) return@collectLatest
                    while (true) {
                        delay(interval * MILLIS_PER_SECOND)
                        advance(1)
                    }
                }
        }
    }

    fun next() = navigate(1)

    fun previous() = navigate(-1)

    fun togglePause() {
        savedState[KEY_PAUSED] = !paused.value
    }

    private fun navigate(delta: Int) {
        advance(delta)
        timerResets.update { it + 1 }
    }

    private fun advance(delta: Int) {
        savedState[KEY_POSITION] = position.value + delta
    }

    private companion object {
        const val KEY_POSITION = "position"
        const val KEY_PAUSED = "paused"
        const val MILLIS_PER_SECOND = 1_000L
    }
}

package com.codebyramiz.ambient.ui.video

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class VideoUiState(val playing: Boolean = true, val muted: Boolean = false)

/** Holds what the user chose (play/pause, sound); the player itself is owned by the composable. */
@HiltViewModel
class VideoViewModel @Inject constructor(private val savedState: SavedStateHandle) : ViewModel() {
    private val playing = savedState.getStateFlow(KEY_PLAYING, true)
    private val muted = savedState.getStateFlow(KEY_MUTED, false)

    val state: StateFlow<VideoUiState> = combine(playing, muted, ::VideoUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, VideoUiState())

    fun togglePlaying() {
        savedState[KEY_PLAYING] = !playing.value
    }

    fun toggleMuted() {
        savedState[KEY_MUTED] = !muted.value
    }

    private companion object {
        const val KEY_PLAYING = "playing"
        const val KEY_MUTED = "muted"
    }
}

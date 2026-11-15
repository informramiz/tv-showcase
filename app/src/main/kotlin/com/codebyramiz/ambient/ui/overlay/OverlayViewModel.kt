package com.codebyramiz.ambient.ui.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codebyramiz.ambient.core.data.AmbientInfoProvider
import com.codebyramiz.ambient.core.model.AmbientInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Feeds the clock and weather overlay shared by Home, Slideshow and Video. */
@HiltViewModel
class OverlayViewModel @Inject constructor(provider: AmbientInfoProvider) : ViewModel() {
    /** Null until the first clock tick, which is immediate in practice. */
    val info: StateFlow<AmbientInfo?> =
        provider.info.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

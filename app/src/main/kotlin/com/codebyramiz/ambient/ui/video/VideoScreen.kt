package com.codebyramiz.ambient.ui.video

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import com.codebyramiz.ambient.R
import com.codebyramiz.ambient.core.model.AmbientInfo
import com.codebyramiz.ambient.ui.components.KeepScreenOn
import com.codebyramiz.ambient.ui.components.Pill
import com.codebyramiz.ambient.ui.components.RemoteAction
import com.codebyramiz.ambient.ui.components.toRemoteAction
import com.codebyramiz.ambient.ui.overlay.ClockBlock
import com.codebyramiz.ambient.ui.overlay.OverlayViewModel
import com.codebyramiz.ambient.ui.overlay.WeatherBlock
import com.codebyramiz.ambient.ui.theme.ScreenPaddingHorizontal
import com.codebyramiz.ambient.ui.theme.ScreenPaddingVertical
import kotlinx.coroutines.delay

const val TAG_VIDEO = "video"
const val TAG_VIDEO_STATUS = "video-status"
private const val HINT_VISIBLE_MILLIS = 4_000L

@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    viewModel: VideoViewModel = hiltViewModel(),
    overlay: OverlayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val info by overlay.info.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val player = remember { createAmbientPlayer(context) }
    DisposableEffect(player) { onDispose { player.release() } }

    // The activity leaving the foreground (Home pressed, screensaver) must silence the loop.
    LifecycleStartEffect(player, state.playing) {
        player.playWhenReady = state.playing
        onStopOrDispose { player.pause() }
    }
    LaunchedEffect(player, state.muted) { player.volume = if (state.muted) 0f else 1f }

    VideoContent(
        player = player,
        state = state,
        info = info,
        onAction = { action ->
            when (action) {
                RemoteAction.Select, RemoteAction.PlayPause -> viewModel.togglePlaying()
                RemoteAction.Up, RemoteAction.Down -> viewModel.toggleMuted()
                else -> Unit
            }
        },
        modifier = modifier,
    )
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun VideoContent(
    player: Player,
    state: VideoUiState,
    info: AmbientInfo?,
    onAction: (RemoteAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeepScreenOn()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    var keyPresses by remember { mutableStateOf(1) }
    var hintVisible by remember { mutableStateOf(true) }
    LaunchedEffect(keyPresses) {
        hintVisible = true
        delay(HINT_VISIBLE_MILLIS)
        hintVisible = false
    }

    Box(
        modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag(TAG_VIDEO)
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                val action = event.toRemoteAction() ?: return@onKeyEvent false
                keyPresses++
                onAction(action)
                true
            }
            .focusable(),
    ) {
        ContentFrame(player = player, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(0.6f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.55f)),
                ),
        )

        if (info != null) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = ScreenPaddingHorizontal, vertical = ScreenPaddingVertical),
                verticalAlignment = Alignment.Bottom,
            ) {
                ClockBlock(info, Modifier.weight(1f), large = false)
                WeatherBlock(info)
            }
        }

        val status = statusText(state, hintVisible)
        if (status != null) {
            Pill(
                status,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = ScreenPaddingHorizontal, vertical = ScreenPaddingVertical)
                    .testTag(TAG_VIDEO_STATUS),
            )
        }
    }
}

internal fun statusText(state: VideoUiState, hintVisible: Boolean): String? = when {
    !state.playing -> if (state.muted) "Paused · Muted" else "Paused"
    state.muted -> "Muted"
    hintVisible -> "OK pause   ▲▼ sound   Back exit"
    else -> null
}

@androidx.annotation.OptIn(UnstableApi::class)
private fun createAmbientPlayer(context: Context): ExoPlayer = ExoPlayer.Builder(context)
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build(),
        /* handleAudioFocus = */
        true,
    )
    .build()
    .apply {
        setMediaItem(MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(R.raw.ambient_loop)))
        repeatMode = Player.REPEAT_MODE_ALL
        prepare()
    }

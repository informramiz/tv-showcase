package com.codebyramiz.ambient.ui.slideshow

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codebyramiz.ambient.core.model.AmbientInfo
import com.codebyramiz.ambient.ui.components.KeepScreenOn
import com.codebyramiz.ambient.ui.components.Pill
import com.codebyramiz.ambient.ui.components.RemoteAction
import com.codebyramiz.ambient.ui.components.SceneCatalog
import com.codebyramiz.ambient.ui.components.toRemoteAction
import com.codebyramiz.ambient.ui.overlay.ClockBlock
import com.codebyramiz.ambient.ui.overlay.OverlayViewModel
import com.codebyramiz.ambient.ui.overlay.WeatherBlock
import com.codebyramiz.ambient.ui.theme.ScreenPaddingHorizontal
import com.codebyramiz.ambient.ui.theme.ScreenPaddingVertical
import kotlinx.coroutines.delay

const val TAG_SLIDESHOW = "slideshow"
const val TAG_PAUSED = "slideshow-paused"
private const val HINT_VISIBLE_MILLIS = 4_000L
private const val FADE_MILLIS = 1_500
private const val KEN_BURNS_MILLIS = 14_000
private const val KEN_BURNS_END_SCALE = 1.08f

@Composable
fun SlideshowScreen(
    modifier: Modifier = Modifier,
    viewModel: SlideshowViewModel = hiltViewModel(),
    overlay: OverlayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val info by overlay.info.collectAsStateWithLifecycle()
    SlideshowContent(
        state = state,
        info = info,
        onAction = { action ->
            when (action) {
                RemoteAction.Left, RemoteAction.Previous -> viewModel.previous()
                RemoteAction.Right, RemoteAction.Next -> viewModel.next()
                RemoteAction.Select, RemoteAction.PlayPause -> viewModel.togglePause()
                else -> Unit
            }
        },
        modifier = modifier,
    )
}

@Composable
fun SlideshowContent(
    state: SlideshowUiState,
    info: AmbientInfo?,
    onAction: (RemoteAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeepScreenOn()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    // Any remote press reveals the hint pill for a few seconds, then it fades away again.
    var keyPresses by remember { mutableIntStateOf(1) }
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
            .testTag(TAG_SLIDESHOW)
            .focusRequester(focusRequester)
            .onKeyEvent { event ->
                val action = event.toRemoteAction() ?: return@onKeyEvent false
                keyPresses++
                onAction(action)
                true
            }
            .focusable(),
    ) {
        Crossfade(
            targetState = state.position,
            animationSpec = tween(FADE_MILLIS),
            label = "scene",
        ) { position -> KenBurnsImage(SceneCatalog.at(position).image) }

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.7f),
                    ),
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
                ClockBlock(info, Modifier.weight(1f))
                WeatherBlock(info)
            }
        }

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = ScreenPaddingHorizontal, vertical = ScreenPaddingVertical),
        ) {
            if (state.paused) {
                Pill("Paused", Modifier.testTag(TAG_PAUSED))
            } else if (hintVisible) {
                Pill("◀ ▶ browse   OK pause   Back exit")
            }
        }
    }
}

@Composable
private fun KenBurnsImage(imageRes: Int) {
    // A slow push-in keeps a still frame feeling alive without distracting from the room.
    val scale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        scale.animateTo(KEN_BURNS_END_SCALE, tween(KEN_BURNS_MILLIS, easing = LinearEasing))
    }
    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
    )
}

package com.codebyramiz.ambient.ui.components

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

enum class RemoteAction { Left, Right, Up, Down, Select, PlayPause, Next, Previous }

/** Translates a remote-control key press into a [RemoteAction]; null for keys we do not handle. */
fun KeyEvent.toRemoteAction(): RemoteAction? {
    if (type != KeyEventType.KeyDown) return null
    val action = when (key) {
        Key.DirectionLeft -> RemoteAction.Left
        Key.DirectionRight -> RemoteAction.Right
        Key.DirectionUp -> RemoteAction.Up
        Key.DirectionDown -> RemoteAction.Down
        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> RemoteAction.Select
        Key.MediaPlayPause -> RemoteAction.PlayPause
        Key.MediaNext, Key.MediaFastForward -> RemoteAction.Next
        Key.MediaPrevious, Key.MediaRewind -> RemoteAction.Previous
        else -> null
    }
    // Holding a key repeats it. Scene navigation may repeat; toggles must fire once per press.
    return action?.takeUnless { nativeKeyEvent.repeatCount > 0 && it in TOGGLES }
}

private val TOGGLES = setOf(RemoteAction.Select, RemoteAction.PlayPause, RemoteAction.Up, RemoteAction.Down)

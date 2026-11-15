package com.codebyramiz.ambient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

/** Small translucent pill used for remote hints and status chips on top of full-bleed media. */
@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
            .padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

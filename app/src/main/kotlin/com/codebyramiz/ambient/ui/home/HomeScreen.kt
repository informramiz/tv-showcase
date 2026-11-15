package com.codebyramiz.ambient.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.codebyramiz.ambient.R
import com.codebyramiz.ambient.core.model.AmbientInfo
import com.codebyramiz.ambient.ui.components.SceneCatalog
import com.codebyramiz.ambient.ui.overlay.ClockBlock
import com.codebyramiz.ambient.ui.overlay.OverlayViewModel
import com.codebyramiz.ambient.ui.overlay.WeatherBlock
import com.codebyramiz.ambient.ui.theme.ScreenPaddingHorizontal
import com.codebyramiz.ambient.ui.theme.ScreenPaddingVertical

const val TAG_HOME_SLIDESHOW = "home-slideshow"
const val TAG_HOME_VIDEO = "home-video"
const val TAG_HOME_SETTINGS = "home-settings"

private const val CARD_COUNT = 3

@Composable
fun HomeScreen(
    onOpenSlideshow: () -> Unit,
    onOpenVideo: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    overlay: OverlayViewModel = hiltViewModel(),
) {
    val info by overlay.info.collectAsStateWithLifecycle()
    HomeContent(info, onOpenSlideshow, onOpenVideo, onOpenSettings, modifier)
}

@Composable
fun HomeContent(
    info: AmbientInfo?,
    onOpenSlideshow: () -> Unit,
    onOpenVideo: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backdrop = SceneCatalog.forHour(info?.time?.hour ?: 12)
    val cards = remember { List(CARD_COUNT) { FocusRequester() } }
    // Survives leaving Home, so Back lands on the card that was opened.
    var lastCard by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { cards[lastCard].requestFocus() }

    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(backdrop.image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(Brush.horizontalGradient(0f to Color.Black.copy(alpha = 0.6f), 0.7f to Color.Transparent))
                    drawRect(Brush.verticalGradient(0.45f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.85f)))
                },
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = ScreenPaddingHorizontal, vertical = ScreenPaddingVertical),
        ) {
            Text("Ambient", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            if (info != null) {
                ClockBlock(info, large = false)
                Spacer(Modifier.height(8.dp))
                WeatherBlock(info)
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.focusRestorer(cards[lastCard]),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                FeatureCard(
                    title = "Slideshow",
                    subtitle = "Scenes, clock and weather",
                    onClick = onOpenSlideshow,
                    modifier = Modifier.cardFocus(cards[0]) { lastCard = 0 }.testTag(TAG_HOME_SLIDESHOW),
                ) {
                    Image(
                        painterResource(R.drawable.scene_dusk),
                        null,
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                FeatureCard(
                    title = "Ambient video",
                    subtitle = "Slow colour and soft sound",
                    onClick = onOpenVideo,
                    modifier = Modifier.cardFocus(cards[1]) { lastCard = 1 }.testTag(TAG_HOME_VIDEO),
                ) {
                    Image(
                        painterResource(R.drawable.video_poster),
                        null,
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
                FeatureCard(
                    title = "Settings",
                    subtitle = "City, units, timing",
                    onClick = onOpenSettings,
                    modifier = Modifier.cardFocus(cards[2]) { lastCard = 2 }.testTag(TAG_HOME_SETTINGS),
                ) {
                    Box(
                        Modifier.fillMaxSize().drawWithContent {
                            drawRect(Brush.linearGradient(listOf(Color(0xFF232A63), Color(0xFF6A3D9A))))
                            drawContent()
                        },
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = null,
                            modifier = Modifier.padding(top = 28.dp).size(64.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.cardFocus(requester: FocusRequester, onFocused: () -> Unit): Modifier =
    focusRequester(requester).onFocusChanged { if (it.isFocused) onFocused() }

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    artwork: @Composable () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(width = 260.dp, height = 160.dp),
        shape = CardDefaults.shape(RoundedCornerShape(16.dp)),
        scale = CardDefaults.scale(focusedScale = 1.08f),
        border = CardDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.border),
                shape = RoundedCornerShape(16.dp),
            ),
        ),
    ) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))) {
            artwork()
            Box(
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.8f)),
                        )
                    },
            )
            Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

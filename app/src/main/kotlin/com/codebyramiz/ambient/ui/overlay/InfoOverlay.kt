package com.codebyramiz.ambient.ui.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Dehaze
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.codebyramiz.ambient.core.model.AmbientInfo
import com.codebyramiz.ambient.core.model.WeatherCondition
import com.codebyramiz.ambient.core.model.WeatherState

const val TAG_CLOCK = "overlay-clock"
const val TAG_WEATHER = "overlay-weather"

@Composable
fun ClockBlock(info: AmbientInfo, modifier: Modifier = Modifier, large: Boolean = true) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatClock(info.time, info.use24HourClock),
                style = if (large) MaterialTheme.typography.displayLarge else MaterialTheme.typography.displayMedium,
                modifier = Modifier.testTag(TAG_CLOCK),
            )
            val meridiem = formatMeridiem(info.time, info.use24HourClock)
            if (meridiem.isNotEmpty()) {
                Spacer(Modifier.width(12.dp))
                Text(meridiem, style = MaterialTheme.typography.headlineMedium)
            }
        }
        Text(
            text = formatDate(info.time),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun WeatherBlock(info: AmbientInfo, modifier: Modifier = Modifier) {
    Row(modifier.testTag(TAG_WEATHER), verticalAlignment = Alignment.CenterVertically) {
        when (val state = info.weather) {
            WeatherState.Loading -> WeatherText(icon = Icons.Rounded.Cloud, headline = "--", detail = info.city.name)
            WeatherState.Unavailable ->
                WeatherText(
                    icon = Icons.Rounded.CloudOff,
                    headline = "Offline",
                    detail = "${info.city.name} · no weather",
                )
            is WeatherState.Available -> {
                val weather = state.weather
                val detail = buildString {
                    append(weather.condition.describe(weather.isDay)).append(" · ").append(info.city.name)
                    if (state.isStale) append(" · offline")
                }
                WeatherText(
                    icon = weather.condition.icon(weather.isDay),
                    headline = info.temperatureUnit.format(weather.temperatureCelsius),
                    detail = detail,
                )
            }
        }
    }
}

@Composable
private fun WeatherText(icon: ImageVector, headline: String, detail: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.width(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(headline, style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Normal))
        Text(
            detail,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun WeatherCondition.icon(isDay: Boolean): ImageVector = when (this) {
    WeatherCondition.CLEAR -> if (isDay) Icons.Rounded.WbSunny else Icons.Rounded.NightsStay
    WeatherCondition.PARTLY_CLOUDY -> Icons.Rounded.WbCloudy
    WeatherCondition.CLOUDY -> Icons.Rounded.Cloud
    WeatherCondition.FOG -> Icons.Rounded.Dehaze
    WeatherCondition.DRIZZLE -> Icons.Rounded.WaterDrop
    WeatherCondition.RAIN -> Icons.Rounded.Umbrella
    WeatherCondition.SNOW -> Icons.Rounded.AcUnit
    WeatherCondition.THUNDERSTORM -> Icons.Rounded.Bolt
}

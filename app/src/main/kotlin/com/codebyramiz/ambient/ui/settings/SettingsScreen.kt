package com.codebyramiz.ambient.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Switch
import androidx.tv.material3.Text
import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.ui.theme.ScreenPaddingHorizontal
import com.codebyramiz.ambient.ui.theme.ScreenPaddingVertical
import kotlinx.coroutines.launch

const val TAG_SETTING_CITY = "setting-city"
const val TAG_SETTING_UNIT = "setting-unit"
const val TAG_SETTING_INTERVAL = "setting-interval"
const val TAG_SETTING_24H = "setting-24h"
fun cityTag(city: City) = "city-${city.id}"

@Composable
fun SettingsScreen(
    onPickCity: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsContent(
        settings = settings,
        onPickCity = onPickCity,
        onToggleUnit = viewModel::toggleTemperatureUnit,
        onCycleInterval = viewModel::cycleSlideInterval,
        onToggle24h = viewModel::toggle24HourClock,
        modifier = modifier,
    )
}

@Composable
fun SettingsContent(
    settings: AmbientSettings,
    onPickCity: () -> Unit,
    onToggleUnit: () -> Unit,
    onCycleInterval: () -> Unit,
    onToggle24h: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstRow = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstRow.requestFocus() }

    ScreenFrame(
        title = "Settings",
        subtitle = "Choose what the ambient screen shows. Changes apply immediately.",
        modifier = modifier,
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SettingRow(
                    title = "City",
                    detail = "${settings.city.name}, ${settings.city.country}",
                    onClick = onPickCity,
                    modifier = Modifier.focusRequester(firstRow).testTag(TAG_SETTING_CITY),
                )
            }
            item {
                SettingRow(
                    title = "Temperature unit",
                    detail = if (settings.temperatureUnit.symbol == "°C") "Celsius (°C)" else "Fahrenheit (°F)",
                    onClick = onToggleUnit,
                    modifier = Modifier.testTag(TAG_SETTING_UNIT),
                )
            }
            item {
                SettingRow(
                    title = "Slide duration",
                    detail = "${settings.slideIntervalSeconds} seconds per scene",
                    onClick = onCycleInterval,
                    modifier = Modifier.testTag(TAG_SETTING_INTERVAL),
                )
            }
            item {
                SettingRow(
                    title = "24-hour clock",
                    detail = if (settings.use24HourClock) "On" else "Off, showing AM/PM",
                    onClick = onToggle24h,
                    trailing = { Switch(checked = settings.use24HourClock, onCheckedChange = null) },
                    modifier = Modifier.testTag(TAG_SETTING_24H),
                )
            }
        }
    }
}

@Composable
fun CityPickerScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    CityPickerContent(
        selected = settings.city,
        onSelect = { city ->
            scope.launch {
                viewModel.selectCity(city)
                onDone()
            }
        },
        modifier = modifier,
    )
}

@Composable
fun CityPickerContent(selected: City, onSelect: (City) -> Unit, modifier: Modifier = Modifier) {
    val selectedRequester = remember { FocusRequester() }
    val selectedIndex = City.ALL.indexOfFirst { it.id == selected.id }.coerceAtLeast(0)
    val listState = rememberLazyListState()
    // The stored city can arrive after first composition. Scroll it into composition before focusing,
    // since a focus request on a row that is not composed would throw.
    LaunchedEffect(selectedIndex) {
        listState.scrollToItem((selectedIndex - 2).coerceAtLeast(0))
        selectedRequester.requestFocus()
    }

    ScreenFrame(
        title = "City",
        subtitle = "Weather comes from Open-Meteo for the city you pick.",
        modifier = modifier,
    ) {
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(City.ALL, key = { it.id }) { city ->
                val isSelected = city.id == selected.id
                SettingRow(
                    title = city.name,
                    detail = city.country,
                    onClick = { onSelect(city) },
                    trailing = {
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    modifier = Modifier
                        .then(if (isSelected) Modifier.focusRequester(selectedRequester) else Modifier)
                        .testTag(cityTag(city)),
                )
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    ListItem(
        selected = false,
        onClick = onClick,
        headlineContent = { Text(title, style = MaterialTheme.typography.titleLarge) },
        supportingContent = { Text(detail, style = MaterialTheme.typography.bodyLarge) },
        trailingContent = trailing,
        modifier = modifier,
    )
}

/** Two-pane frame: a title column on the left, the interactive list on the right. */
@Composable
private fun ScreenFrame(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPaddingHorizontal, vertical = ScreenPaddingVertical),
    ) {
        Column(Modifier.width(360.dp).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            Text(title, style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(48.dp))
        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.CenterStart) { content() }
    }
}

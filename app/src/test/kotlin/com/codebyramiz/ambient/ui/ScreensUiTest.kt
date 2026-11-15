package com.codebyramiz.ambient.ui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.codebyramiz.ambient.core.model.AmbientInfo
import com.codebyramiz.ambient.core.model.AmbientSettings
import com.codebyramiz.ambient.core.model.City
import com.codebyramiz.ambient.core.model.TemperatureUnit
import com.codebyramiz.ambient.core.model.Weather
import com.codebyramiz.ambient.core.model.WeatherCondition
import com.codebyramiz.ambient.core.model.WeatherState
import com.codebyramiz.ambient.ui.components.RemoteAction
import com.codebyramiz.ambient.ui.settings.CityPickerContent
import com.codebyramiz.ambient.ui.settings.SettingsContent
import com.codebyramiz.ambient.ui.settings.TAG_SETTING_24H
import com.codebyramiz.ambient.ui.settings.TAG_SETTING_CITY
import com.codebyramiz.ambient.ui.settings.TAG_SETTING_UNIT
import com.codebyramiz.ambient.ui.settings.cityTag
import com.codebyramiz.ambient.ui.slideshow.SlideshowContent
import com.codebyramiz.ambient.ui.slideshow.SlideshowUiState
import com.codebyramiz.ambient.ui.slideshow.TAG_PAUSED
import com.codebyramiz.ambient.ui.slideshow.TAG_SLIDESHOW
import com.codebyramiz.ambient.ui.theme.AmbientTheme
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w960dp-h540dp-xhdpi")
@OptIn(ExperimentalTestApi::class)
class ScreensUiTest {
    @get:Rule val compose = createComposeRule()

    private val info = AmbientInfo(
        time = LocalDateTime.of(2026, 11, 15, 21, 5),
        city = City.DEFAULT,
        temperatureUnit = TemperatureUnit.CELSIUS,
        use24HourClock = true,
        weather = WeatherState.Available(Weather("zurich", 7.4, WeatherCondition.CLOUDY, isDay = false)),
    )

    @Test
    fun settingsShowCurrentValuesAndForwardClicks() {
        var unitToggles = 0
        var clockToggles = 0
        var cityOpened = 0
        compose.setContent {
            AmbientTheme {
                SettingsContent(
                    settings = AmbientSettings(use24HourClock = true),
                    onPickCity = { cityOpened++ },
                    onToggleUnit = { unitToggles++ },
                    onCycleInterval = {},
                    onToggle24h = { clockToggles++ },
                )
            }
        }
        compose.onNodeWithText("Zurich, Switzerland").assertIsDisplayed()
        compose.onNodeWithText("Celsius (°C)").assertIsDisplayed()
        compose.onNodeWithTag(TAG_SETTING_CITY).performClick()
        compose.onNodeWithTag(TAG_SETTING_UNIT).performClick()
        compose.onNodeWithTag(TAG_SETTING_24H).performClick()
        assertEquals(listOf(1, 1, 1), listOf(cityOpened, unitToggles, clockToggles))
    }

    @Test
    fun cityPickerListsCitiesAndReportsTheChoice() {
        var chosen: City? = null
        compose.setContent {
            AmbientTheme { CityPickerContent(selected = City.DEFAULT, onSelect = { chosen = it }) }
        }
        val london = City.ALL.first { it.id == "london" }
        compose.onNodeWithTag(cityTag(london)).performClick()
        assertEquals(london, chosen)
    }

    @Test
    fun slideshowTranslatesDpadKeysAndShowsOverlayAndPauseState() {
        val actions = mutableListOf<RemoteAction>()
        compose.setContent {
            AmbientTheme {
                SlideshowContent(SlideshowUiState(position = 3, paused = true), info, onAction = { actions += it })
            }
        }
        compose.onNodeWithText("21:05").assertIsDisplayed()
        compose.onNodeWithText("7°C").assertIsDisplayed()
        compose.onNodeWithTag(TAG_PAUSED).assertIsDisplayed()
        compose.onNodeWithTag(TAG_SLIDESHOW).performKeyInput {
            pressKey(Key.DirectionRight)
            pressKey(Key.DirectionLeft)
            pressKey(Key.DirectionCenter)
        }
        assertEquals(listOf(RemoteAction.Right, RemoteAction.Left, RemoteAction.Select), actions)
    }
}

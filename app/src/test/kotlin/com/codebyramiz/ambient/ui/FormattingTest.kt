package com.codebyramiz.ambient.ui

import com.codebyramiz.ambient.core.model.WeatherCondition
import com.codebyramiz.ambient.ui.components.SceneCatalog
import com.codebyramiz.ambient.ui.overlay.describe
import com.codebyramiz.ambient.ui.overlay.formatClock
import com.codebyramiz.ambient.ui.overlay.formatDate
import com.codebyramiz.ambient.ui.overlay.formatMeridiem
import com.codebyramiz.ambient.ui.video.VideoUiState
import com.codebyramiz.ambient.ui.video.statusText
import java.time.LocalDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormattingTest {
    private val afternoon = LocalDateTime.of(2026, 11, 15, 15, 7)

    @Test
    fun clockFollowsTheChosenFormat() {
        assertEquals("15:07", formatClock(afternoon, use24Hour = true, Locale.ENGLISH))
        assertEquals("3:07", formatClock(afternoon, use24Hour = false, Locale.ENGLISH))
        assertEquals("PM", formatMeridiem(afternoon, use24Hour = false, Locale.ENGLISH))
        assertEquals("", formatMeridiem(afternoon, use24Hour = true, Locale.ENGLISH))
        assertEquals("Sunday, 15 November", formatDate(afternoon, Locale.ENGLISH))
    }

    @Test
    fun clearNightIsNamedAsSuch() {
        assertEquals("Clear night", WeatherCondition.CLEAR.describe(isDay = false))
        assertEquals("Clear", WeatherCondition.CLEAR.describe(isDay = true))
        assertEquals("Rain", WeatherCondition.RAIN.describe(isDay = false))
    }

    @Test
    fun scenesWrapInBothDirectionsAndFollowTheHour() {
        val size = SceneCatalog.scenes.size
        assertEquals(SceneCatalog.scenes[0], SceneCatalog.at(size))
        assertEquals(SceneCatalog.scenes[size - 1], SceneCatalog.at(-1))
        assertEquals(SceneCatalog.scenes[5], SceneCatalog.forHour(23))
        assertEquals(SceneCatalog.scenes[5], SceneCatalog.forHour(3))
        assertEquals(SceneCatalog.scenes[0], SceneCatalog.forHour(6))
    }

    @Test
    fun videoStatusPrefersStateOverTheHint() {
        assertEquals("Paused", statusText(VideoUiState(playing = false), hintVisible = true))
        assertEquals("Paused · Muted", statusText(VideoUiState(playing = false, muted = true), hintVisible = false))
        assertEquals("Muted", statusText(VideoUiState(muted = true), hintVisible = true))
        assertEquals("OK pause   ▲▼ sound   Back exit", statusText(VideoUiState(), hintVisible = true))
        assertNull(statusText(VideoUiState(), hintVisible = false))
    }
}

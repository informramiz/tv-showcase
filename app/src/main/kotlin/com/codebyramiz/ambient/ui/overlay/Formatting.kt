package com.codebyramiz.ambient.ui.overlay

import com.codebyramiz.ambient.core.model.WeatherCondition
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatClock(time: LocalDateTime, use24Hour: Boolean, locale: Locale = Locale.getDefault()): String {
    val pattern = if (use24Hour) "HH:mm" else "h:mm"
    return DateTimeFormatter.ofPattern(pattern, locale).format(time)
}

/** "AM"/"PM" for the 12-hour clock, empty for the 24-hour clock. */
fun formatMeridiem(time: LocalDateTime, use24Hour: Boolean, locale: Locale = Locale.getDefault()): String =
    if (use24Hour) "" else DateTimeFormatter.ofPattern("a", locale).format(time)

fun formatDate(time: LocalDateTime, locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofPattern("EEEE, d MMMM", locale).format(time)

fun WeatherCondition.describe(isDay: Boolean): String =
    if (this == WeatherCondition.CLEAR && !isDay) "Clear night" else label

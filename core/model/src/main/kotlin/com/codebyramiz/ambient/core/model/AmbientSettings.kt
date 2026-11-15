package com.codebyramiz.ambient.core.model

data class AmbientSettings(
    val city: City = City.DEFAULT,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val slideIntervalSeconds: Int = DEFAULT_SLIDE_INTERVAL_SECONDS,
    val use24HourClock: Boolean = true,
) {
    companion object {
        const val DEFAULT_SLIDE_INTERVAL_SECONDS = 10
        val SLIDE_INTERVAL_OPTIONS_SECONDS = listOf(5, 10, 15, 30)

        /** Next option after [current], wrapping around; unknown values restart at the default. */
        fun nextInterval(current: Int): Int {
            val index = SLIDE_INTERVAL_OPTIONS_SECONDS.indexOf(current)
            return if (index < 0) {
                DEFAULT_SLIDE_INTERVAL_SECONDS
            } else {
                SLIDE_INTERVAL_OPTIONS_SECONDS[(index + 1) % SLIDE_INTERVAL_OPTIONS_SECONDS.size]
            }
        }
    }
}

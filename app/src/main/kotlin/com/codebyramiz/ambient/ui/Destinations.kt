package com.codebyramiz.ambient.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Home : NavKey

@Serializable
data object Slideshow : NavKey

@Serializable
data object Video : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object CityPicker : NavKey

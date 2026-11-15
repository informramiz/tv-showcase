package com.codebyramiz.ambient.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.codebyramiz.ambient.ui.home.HomeScreen
import com.codebyramiz.ambient.ui.settings.CityPickerScreen
import com.codebyramiz.ambient.ui.settings.SettingsScreen
import com.codebyramiz.ambient.ui.slideshow.SlideshowScreen
import com.codebyramiz.ambient.ui.theme.AmbientTheme
import com.codebyramiz.ambient.ui.video.VideoScreen

@Composable
fun AmbientApp() {
    AmbientTheme {
        // rememberNavBackStack is saveable, so the screen the user was on comes back after process death.
        val backStack = rememberNavBackStack(Home)
        val goBack: () -> Unit = { backStack.removeLastOrNull() }
        val open: (NavKey) -> Unit = { backStack.add(it) }
        NavDisplay(
            backStack = backStack,
            onBack = goBack,
            // Each entry owns its ViewModels, so leaving a screen releases them.
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<Home> {
                    HomeScreen(
                        onOpenSlideshow = { open(Slideshow) },
                        onOpenVideo = { open(Video) },
                        onOpenSettings = { open(Settings) },
                    )
                }
                entry<Slideshow> { SlideshowScreen() }
                entry<Video> { VideoScreen() }
                entry<Settings> { SettingsScreen(onPickCity = { open(CityPicker) }) }
                entry<CityPicker> { CityPickerScreen(onDone = goBack) }
            },
        )
    }
}

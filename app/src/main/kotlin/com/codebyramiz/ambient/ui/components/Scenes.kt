package com.codebyramiz.ambient.ui.components

import androidx.annotation.DrawableRes
import com.codebyramiz.ambient.R

data class Scene(@DrawableRes val image: Int, val title: String)

/** The bundled, generated scenes (see tools/generate_assets.py). */
object SceneCatalog {
    val scenes: List<Scene> = listOf(
        Scene(R.drawable.scene_dawn, "Dawn ridges"),
        Scene(R.drawable.scene_forest, "Morning valley"),
        Scene(R.drawable.scene_ocean, "Blue horizon"),
        Scene(R.drawable.scene_desert, "Desert glow"),
        Scene(R.drawable.scene_dusk, "Dusk"),
        Scene(R.drawable.scene_night, "Moonlit night"),
    )

    /** Position-based lookup that wraps in both directions, so next/previous never run out. */
    fun at(position: Int): Scene = scenes[Math.floorMod(position, scenes.size)]

    /** The scene that matches the time of day, used for the Home backdrop. */
    fun forHour(hour: Int): Scene = when (hour) {
        in 5..7 -> scenes[0]
        in 8..11 -> scenes[1]
        in 12..16 -> scenes[2]
        in 17..19 -> scenes[4]
        else -> scenes[5]
    }
}

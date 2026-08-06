package com.shrikantbadwaik.landingkit.feature.kits.presentation.navigation

internal sealed interface KitScreen {
    data object Home : KitScreen
    data object Create : KitScreen
    data class Detail(val tripId: String) : KitScreen
}

internal val KitScreen.depth: Int
    get() = when (this) {
        KitScreen.Home -> 0
        KitScreen.Create -> 1
        is KitScreen.Detail -> 2
    }

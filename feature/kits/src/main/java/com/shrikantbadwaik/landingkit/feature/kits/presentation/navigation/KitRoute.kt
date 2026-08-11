package com.shrikantbadwaik.landingkit.feature.kits.presentation.navigation

import kotlinx.serialization.Serializable

internal sealed interface KitRoute {
    @Serializable
    data object Home : KitRoute

    @Serializable
    data object Create : KitRoute

    @Serializable
    data class Detail(val tripId: String) : KitRoute

    @Serializable
    data class ArrivalMode(val tripId: String) : KitRoute

    @Serializable
    data class OfflineCheck(val tripId: String) : KitRoute

    @Serializable
    data class Edit(val tripId: String) : KitRoute
}

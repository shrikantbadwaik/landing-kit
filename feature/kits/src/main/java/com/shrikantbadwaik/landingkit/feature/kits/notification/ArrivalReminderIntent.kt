package com.shrikantbadwaik.landingkit.feature.kits.notification

import android.content.Intent

object ArrivalReminderIntent {
    const val ACTION_OPEN_ARRIVAL_MODE =
        "com.shrikantbadwaik.landingkit.action.OPEN_ARRIVAL_MODE"
    const val EXTRA_TRIP_ID = "arrival_reminder_trip_id"

    fun tripId(intent: Intent?): String? = intent
        ?.takeIf { it.action == ACTION_OPEN_ARRIVAL_MODE }
        ?.getStringExtra(EXTRA_TRIP_ID)
        ?.takeIf(String::isNotBlank)
}

package com.shrikantbadwaik.landingkit.feature.kits.entry

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.notification.ArrivalReminderIntent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArrivalEntryPointPublisher @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val shortcutManager = context.getSystemService(ShortcutManager::class.java)

    fun sync(trips: List<Trip>, today: LocalDate = LocalDate.now()) {
        if (trips.nextRelevantArrival(today) == null) {
            shortcutManager.removeDynamicShortcuts(listOf(SHORTCUT_ID))
            return
        }

        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.setAction(ArrivalReminderIntent.ACTION_OPEN_NEXT_ARRIVAL_MODE)
            ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            ?: return
        val shortcut = arrivalModeShortcut(context, launchIntent)
        shortcutManager.dynamicShortcuts = listOf(shortcut)
    }

    internal companion object {
        const val SHORTCUT_ID = "next_arrival_mode"
    }
}

internal fun arrivalModeShortcut(context: Context, launchIntent: Intent): ShortcutInfo =
    ShortcutInfo.Builder(context, ArrivalEntryPointPublisher.SHORTCUT_ID)
        .setShortLabel(context.getString(R.string.arrival_entry_short_label))
        .setLongLabel(context.getString(R.string.arrival_entry_long_label))
        .setIcon(Icon.createWithResource(context, R.drawable.ic_arrival_entry))
        .setIntent(launchIntent)
        .setRank(0)
        .build()

internal fun List<Trip>.nextRelevantArrival(today: LocalDate): Trip? =
    asSequence()
        .filter { !it.arrivalDate.isBefore(today) }
        .minByOrNull(Trip::arrivalDate)

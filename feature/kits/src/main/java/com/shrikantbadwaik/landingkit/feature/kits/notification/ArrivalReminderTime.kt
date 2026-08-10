package com.shrikantbadwaik.landingkit.feature.kits.notification

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

internal fun arrivalReminderTriggerAt(
    arrivalDate: LocalDate,
    zoneId: ZoneId,
    now: Instant,
): Instant? {
    val localToday = now.atZone(zoneId).toLocalDate()
    if (arrivalDate.isBefore(localToday)) return null

    val planned = arrivalDate
        .atTime(ARRIVAL_REMINDER_LOCAL_TIME)
        .atZone(zoneId)
        .toInstant()
    return planned.takeIf { it.isAfter(now) } ?: now.plusSeconds(1)
}

internal val ARRIVAL_REMINDER_LOCAL_TIME: LocalTime = LocalTime.of(8, 0)

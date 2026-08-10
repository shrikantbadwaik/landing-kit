package com.shrikantbadwaik.landingkit.feature.kits.notification

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArrivalReminderTimeTest {
    private val zone = ZoneId.of("Asia/Kolkata")

    @Test
    fun `future arrival is scheduled for eight in the morning`() {
        val trigger = arrivalReminderTriggerAt(
            arrivalDate = LocalDate.of(2026, 8, 12),
            zoneId = zone,
            now = Instant.parse("2026-08-10T04:00:00Z"),
        )

        assertEquals(Instant.parse("2026-08-12T02:30:00Z"), trigger)
    }

    @Test
    fun `arrival later today is scheduled for eight in the morning`() {
        val trigger = arrivalReminderTriggerAt(
            arrivalDate = LocalDate.of(2026, 8, 10),
            zoneId = zone,
            now = Instant.parse("2026-08-10T01:00:00Z"),
        )

        assertEquals(Instant.parse("2026-08-10T02:30:00Z"), trigger)
    }

    @Test
    fun `arrival today after eight becomes available immediately`() {
        val now = Instant.parse("2026-08-10T05:00:00Z")

        val trigger = arrivalReminderTriggerAt(
            arrivalDate = LocalDate.of(2026, 8, 10),
            zoneId = zone,
            now = now,
        )

        assertEquals(now.plusSeconds(1), trigger)
    }

    @Test
    fun `past arrival is not scheduled`() {
        val trigger = arrivalReminderTriggerAt(
            arrivalDate = LocalDate.of(2026, 8, 9),
            zoneId = zone,
            now = Instant.parse("2026-08-10T05:00:00Z"),
        )

        assertNull(trigger)
    }
}

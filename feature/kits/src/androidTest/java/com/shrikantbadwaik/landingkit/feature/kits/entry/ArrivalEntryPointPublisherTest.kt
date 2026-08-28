package com.shrikantbadwaik.landingkit.feature.kits.entry

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.notification.ArrivalReminderIntent
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArrivalEntryPointPublisherTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun shortcut_opens_next_arrival_without_exposing_trip_data() {
        val privateTrip = Trip(
            id = "private-trip-id",
            destination = "Private destination",
            arrivalDate = LocalDate.now(),
            arrivalCard = ArrivalCard(
                accommodationName = "Private stay",
                address = "Private address",
            ),
            items = emptyList(),
        )

        val launchIntent = Intent(ArrivalReminderIntent.ACTION_OPEN_NEXT_ARRIVAL_MODE)
        val shortcut = arrivalModeShortcut(context, launchIntent)
        val intent = checkNotNull(shortcut.intent)
        assertEquals("Arrival", shortcut.shortLabel.toString())
        assertEquals("Open Arrival Mode", shortcut.longLabel.toString())
        assertEquals(
            ArrivalReminderIntent.ACTION_OPEN_NEXT_ARRIVAL_MODE,
            intent.action,
        )
        assertNull(intent.getStringExtra(ArrivalReminderIntent.EXTRA_TRIP_ID))
        assertFalse(shortcut.toString().contains(privateTrip.destination))
        assertFalse(shortcut.toString().contains(privateTrip.arrivalCard.accommodationName))
        assertFalse(shortcut.toString().contains(privateTrip.arrivalCard.address))
    }
}

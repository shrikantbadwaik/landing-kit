package com.shrikantbadwaik.landingkit.feature.kits.sample

import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.defaultChecklist
import java.time.LocalDate

internal fun sampleTrip() = Trip(
    destination = "Kyoto, Japan",
    arrivalDate = LocalDate.now().plusDays(12),
    arrivalCard = ArrivalCard(
        accommodationName = "Machiya Stay Gion",
        address = "2F, 570-123 Gionmachi Minamigawa, Higashiyama-ku, Kyoto",
        localAddress = "京都市東山区祇園町南側 570-123 2F",
        bookingReference = "LK-4821",
        transferInstructions = "Take the JR Haruka to Kyoto Station, then taxi to Gion. Show the local-language address.",
        currencyNote = "¥1,000 ≈ ₹570 · Keep ¥5,000 for the first evening",
        emergencyContact = "Japan emergency: 110 police · 119 ambulance/fire",
        dietaryPhrase = "Watashi wa tamago to nattsu o taberaremasen — I cannot eat eggs or nuts.",
    ),
    items = defaultChecklist(),
)

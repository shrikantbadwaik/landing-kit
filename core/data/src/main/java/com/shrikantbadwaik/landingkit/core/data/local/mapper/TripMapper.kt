package com.shrikantbadwaik.landingkit.core.data.local.mapper

import com.shrikantbadwaik.landingkit.core.data.local.entity.ArrivalItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.TripEntity
import com.shrikantbadwaik.landingkit.core.data.local.model.TripWithItems
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.Trip
import java.time.LocalDate
import java.time.LocalDateTime

internal fun TripWithItems.toModel() = Trip(
    id = trip.id,
    destination = trip.destination,
    arrivalDate = trip.arrivalAt.toArrivalDate(),
    arrivalCard = ArrivalCard(
        accommodationName = trip.stayName,
        address = trip.address,
        localAddress = trip.localAddress,
        bookingReference = trip.bookingReference,
        transferInstructions = trip.transfer,
        currencyNote = trip.currencyNote,
        emergencyContact = trip.emergencyContact,
        dietaryPhrase = trip.dietaryPhrase,
    ),
    items = items.sortedBy(ArrivalItemEntity::sortOrder).map(ArrivalItemEntity::toModel),
)

internal fun Trip.toEntity() = TripEntity(
    id = id,
    destination = destination,
    arrivalAt = arrivalDate.toString(),
    stayName = arrivalCard.accommodationName,
    address = arrivalCard.address,
    localAddress = arrivalCard.localAddress,
    bookingReference = arrivalCard.bookingReference,
    transfer = arrivalCard.transferInstructions,
    currencyNote = arrivalCard.currencyNote,
    emergencyContact = arrivalCard.emergencyContact,
    dietaryPhrase = arrivalCard.dietaryPhrase,
)

private fun String.toArrivalDate(): LocalDate = runCatching(LocalDate::parse)
    .getOrElse { LocalDateTime.parse(this).toLocalDate() }

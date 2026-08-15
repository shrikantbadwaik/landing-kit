package com.shrikantbadwaik.landingkit.core.data.local.mapper

import com.shrikantbadwaik.landingkit.core.data.local.entity.ArrivalItemEntity
import com.shrikantbadwaik.landingkit.core.data.local.entity.TripEntity
import com.shrikantbadwaik.landingkit.core.data.local.model.TripWithItems
import com.shrikantbadwaik.landingkit.core.model.ArrivalCard
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckStatus
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
        fallbackTransport = trip.fallbackTransport,
        lastServiceInfo = trip.lastServiceInfo,
        checkInCutoff = trip.checkInCutoff,
        lateArrivalInstructions = trip.lateArrivalInstructions,
        accommodationContact = trip.accommodationContact,
        expectedCostNote = trip.expectedCostNote,
        alternateStayNote = trip.alternateStayNote,
    ),
    items = items.sortedBy(ArrivalItemEntity::sortOrder).map(ArrivalItemEntity::toModel),
    arrivalReminderEnabled = trip.arrivalReminderEnabled,
    offlineCheckStatus = OfflineArrivalCheckStatus.valueOf(trip.offlineCheckStatus),
    offlineCheckedAtMillis = trip.offlineCheckedAtMillis,
    offlineCheckContentFingerprint = trip.offlineCheckContentFingerprint,
    planBCheckStatus = OfflineArrivalCheckStatus.valueOf(trip.planBCheckStatus),
    planBCheckedAtMillis = trip.planBCheckedAtMillis,
    planBContentFingerprint = trip.planBContentFingerprint,
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
    arrivalReminderEnabled = arrivalReminderEnabled,
    offlineCheckStatus = offlineCheckStatus.name,
    offlineCheckedAtMillis = offlineCheckedAtMillis,
    offlineCheckContentFingerprint = offlineCheckContentFingerprint,
    fallbackTransport = arrivalCard.fallbackTransport,
    lastServiceInfo = arrivalCard.lastServiceInfo,
    checkInCutoff = arrivalCard.checkInCutoff,
    lateArrivalInstructions = arrivalCard.lateArrivalInstructions,
    accommodationContact = arrivalCard.accommodationContact,
    expectedCostNote = arrivalCard.expectedCostNote,
    alternateStayNote = arrivalCard.alternateStayNote,
    planBCheckStatus = planBCheckStatus.name,
    planBCheckedAtMillis = planBCheckedAtMillis,
    planBContentFingerprint = planBContentFingerprint,
)

private fun String.toArrivalDate(): LocalDate = runCatching(LocalDate::parse)
    .getOrElse { LocalDateTime.parse(this).toLocalDate() }

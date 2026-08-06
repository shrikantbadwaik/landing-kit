package com.shrikantbadwaik.landingkit.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val destination: String,
    @ColumnInfo(name = "arrival_at") val arrivalAt: String,
    @ColumnInfo(name = "stay_name") val stayName: String,
    val address: String,
    @ColumnInfo(name = "local_address") val localAddress: String,
    @ColumnInfo(name = "booking_reference") val bookingReference: String,
    val transfer: String,
    @ColumnInfo(name = "currency_note") val currencyNote: String,
    @ColumnInfo(name = "emergency_contact") val emergencyContact: String,
    @ColumnInfo(name = "dietary_phrase") val dietaryPhrase: String,
)

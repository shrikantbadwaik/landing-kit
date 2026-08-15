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
    @ColumnInfo(name = "arrival_reminder_enabled") val arrivalReminderEnabled: Boolean,
    @ColumnInfo(name = "offline_check_status") val offlineCheckStatus: String,
    @ColumnInfo(name = "offline_checked_at") val offlineCheckedAtMillis: Long?,
    @ColumnInfo(name = "offline_check_content_fingerprint")
    val offlineCheckContentFingerprint: String?,
    @ColumnInfo(name = "fallback_transport") val fallbackTransport: String,
    @ColumnInfo(name = "last_service_info") val lastServiceInfo: String,
    @ColumnInfo(name = "check_in_cutoff") val checkInCutoff: String,
    @ColumnInfo(name = "late_arrival_instructions") val lateArrivalInstructions: String,
    @ColumnInfo(name = "accommodation_contact") val accommodationContact: String,
    @ColumnInfo(name = "expected_cost_note") val expectedCostNote: String,
    @ColumnInfo(name = "alternate_stay_note") val alternateStayNote: String,
    @ColumnInfo(name = "plan_b_check_status") val planBCheckStatus: String,
    @ColumnInfo(name = "plan_b_checked_at") val planBCheckedAtMillis: Long?,
    @ColumnInfo(name = "plan_b_content_fingerprint") val planBContentFingerprint: String?,
)

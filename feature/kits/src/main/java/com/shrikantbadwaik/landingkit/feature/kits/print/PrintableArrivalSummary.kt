package com.shrikantbadwaik.landingkit.feature.kits.print

import com.shrikantbadwaik.landingkit.core.model.Trip

internal data class PrintableArrivalSummary(
    val destination: String,
    val arrivalDate: String,
    val sections: List<PrintableSummarySection>,
)

internal data class PrintableSummarySection(
    val title: String,
    val entries: List<PrintableSummaryEntry>,
)

internal data class PrintableSummaryEntry(
    val label: String,
    val value: String,
    val checked: Boolean? = null,
)

internal data class PrintableSummaryLabels(
    val staySection: String,
    val stay: String,
    val address: String,
    val localAddress: String,
    val bookingReference: String,
    val gettingThere: String,
    val usefulOnArrival: String,
    val money: String,
    val emergency: String,
    val dietaryPhrase: String,
    val ifPlansChange: String,
    val alternateTransport: String,
    val lastDeparture: String,
    val checkInCloses: String,
    val lateArrival: String,
    val stayContact: String,
    val expectedCost: String,
    val alternateStay: String,
    val beforeLanding: String,
)

internal fun Trip.toPrintableArrivalSummary(
    arrivalDateText: String,
    labels: PrintableSummaryLabels,
    checklistLimit: Int = PRINTABLE_CHECKLIST_LIMIT,
): PrintableArrivalSummary {
    val essential = listOfNotBlank(
        labels.stay to arrivalCard.accommodationName,
        labels.address to arrivalCard.address,
        labels.localAddress to arrivalCard.localAddress,
        labels.bookingReference to arrivalCard.bookingReference,
        labels.gettingThere to arrivalCard.transferInstructions,
    )
    val useful = listOfNotBlank(
        labels.money to arrivalCard.currencyNote,
        labels.emergency to arrivalCard.emergencyContact,
        labels.dietaryPhrase to arrivalCard.dietaryPhrase,
    )
    val planB = listOfNotBlank(
        labels.alternateTransport to arrivalCard.fallbackTransport,
        labels.lastDeparture to arrivalCard.lastServiceInfo,
        labels.checkInCloses to arrivalCard.checkInCutoff,
        labels.lateArrival to arrivalCard.lateArrivalInstructions,
        labels.stayContact to arrivalCard.accommodationContact,
        labels.expectedCost to arrivalCard.expectedCostNote,
        labels.alternateStay to arrivalCard.alternateStayNote,
    )
    val checklist = items
        .sortedBy { it.isComplete }
        .take(checklistLimit.coerceAtLeast(0))
        .map { item ->
            PrintableSummaryEntry(
                label = item.title.trim(),
                value = item.detail.trim(),
                checked = item.isComplete,
            )
        }
        .filter { it.label.isNotBlank() }

    return PrintableArrivalSummary(
        destination = destination.trim(),
        arrivalDate = arrivalDateText,
        sections = buildList {
            if (essential.isNotEmpty()) add(PrintableSummarySection(labels.staySection, essential))
            if (useful.isNotEmpty()) add(PrintableSummarySection(labels.usefulOnArrival, useful))
            if (planB.isNotEmpty()) add(PrintableSummarySection(labels.ifPlansChange, planB))
            if (checklist.isNotEmpty()) add(PrintableSummarySection(labels.beforeLanding, checklist))
        },
    )
}

private fun listOfNotBlank(vararg values: Pair<String, String>): List<PrintableSummaryEntry> =
    values.mapNotNull { (label, value) ->
        value.trim().takeIf(String::isNotBlank)?.let {
            PrintableSummaryEntry(label = label, value = it)
        }
    }

internal const val PRINTABLE_CHECKLIST_LIMIT = 6

package com.shrikantbadwaik.landingkit.feature.kits.share

import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.print.PrintableSummaryLabels

internal enum class ShareSummarySection {
    STAY,
    USEFUL,
    PLAN_B,
    CHECKLIST,
}

internal data class ShareableArrivalSummary(
    val destination: String,
    val arrivalDate: String,
    val sections: List<ShareableArrivalSection>,
)

internal data class ShareableArrivalSection(
    val type: ShareSummarySection,
    val title: String,
    val lines: List<String>,
)

internal fun Trip.toShareableArrivalSummary(
    arrivalDateText: String,
    labels: PrintableSummaryLabels,
): ShareableArrivalSummary =
    ShareableArrivalSummary(
        destination = destination.trim(),
        arrivalDate = arrivalDateText,
        sections = buildList {
            section(
                type = ShareSummarySection.STAY,
                title = labels.staySection,
                lines = listOfNotBlank(
                    labels.stay to arrivalCard.accommodationName,
                    labels.address to arrivalCard.address,
                    labels.localAddress to arrivalCard.localAddress,
                    labels.gettingThere to arrivalCard.transferInstructions,
                ),
            )
            section(
                type = ShareSummarySection.USEFUL,
                title = labels.usefulOnArrival,
                lines = listOfNotBlank(
                    labels.money to arrivalCard.currencyNote,
                    labels.emergency to arrivalCard.emergencyContact,
                    labels.dietaryPhrase to arrivalCard.dietaryPhrase,
                ),
            )
            section(
                type = ShareSummarySection.PLAN_B,
                title = labels.ifPlansChange,
                lines = listOfNotBlank(
                    labels.alternateTransport to arrivalCard.fallbackTransport,
                    labels.lastDeparture to arrivalCard.lastServiceInfo,
                    labels.checkInCloses to arrivalCard.checkInCutoff,
                    labels.lateArrival to arrivalCard.lateArrivalInstructions,
                    labels.stayContact to arrivalCard.accommodationContact,
                    labels.expectedCost to arrivalCard.expectedCostNote,
                    labels.alternateStay to arrivalCard.alternateStayNote,
                ),
            )
            section(
                type = ShareSummarySection.CHECKLIST,
                title = labels.beforeLanding,
                lines = items.mapNotNull { item ->
                    item.title.trim().takeIf(String::isNotBlank)?.let { title ->
                        val detail = item.detail.trim()
                        if (detail.isBlank()) "• $title" else "• $title — $detail"
                    }
                },
            )
        },
    )

internal fun ShareableArrivalSummary.asPlainText(selected: Set<ShareSummarySection>): String =
    buildString {
        append(destination)
        append('\n')
        append(arrivalDate)
        sections.filter { it.type in selected }.forEach { section ->
            append("\n\n")
            append(section.title)
            section.lines.forEach { line ->
                append('\n')
                append(line)
            }
        }
        append("\n\nShared from Landing Kit")
    }

internal fun ShareableArrivalSummary.asTrustedPersonCopy(selected: Set<ShareSummarySection>): String =
    buildString {
        append("Arrival plan for ")
        append(destination)
        append('\n')
        append(arrivalDate)
        append("\n\nYou do not need Landing Kit to use this copy. Please keep it private.")
        sections.filter { it.type in selected }.forEach { section ->
            append("\n\n")
            append(section.title)
            section.lines.forEach { line ->
                append('\n')
                append(line)
            }
        }
    }

private fun MutableList<ShareableArrivalSection>.section(
    type: ShareSummarySection,
    title: String,
    lines: List<String>,
) {
    if (lines.isNotEmpty()) add(ShareableArrivalSection(type, title, lines))
}

private fun listOfNotBlank(vararg values: Pair<String, String>): List<String> =
    values.mapNotNull { (label, value) ->
        value.trim().takeIf(String::isNotBlank)?.let { "$label: $it" }
    }

package com.shrikantbadwaik.landingkit.feature.kits.presentation.print

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.print.PrintableArrivalSummary
import com.shrikantbadwaik.landingkit.feature.kits.print.PrintableSummaryEntry
import com.shrikantbadwaik.landingkit.feature.kits.print.PrintableSummaryLabels
import com.shrikantbadwaik.landingkit.feature.kits.print.printArrivalSummary
import com.shrikantbadwaik.landingkit.feature.kits.print.toPrintableArrivalSummary
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import java.time.format.DateTimeFormatter

@Composable
internal fun PrintableSummaryScreen(
    trip: Trip,
    onBack: () -> Unit,
) {
    val dateFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.date_format_detail))
    val labels = printableSummaryLabels()
    val summary = remember(trip, labels, dateFormatter) {
        trip.toPrintableArrivalSummary(
            arrivalDateText = trip.arrivalDate.format(dateFormatter),
            labels = labels,
        )
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPrivacyWarning by remember { mutableStateOf(false) }

    if (showPrivacyWarning) {
        AlertDialog(
            onDismissRequest = { showPrivacyWarning = false },
            title = { Text(stringResource(R.string.print_summary_privacy_title)) },
            text = { Text(stringResource(R.string.print_summary_privacy_description)) },
            dismissButton = {
                TextButton(onClick = { showPrivacyWarning = false }) {
                    Text(stringResource(R.string.print_summary_privacy_cancel))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPrivacyWarning = false
                        printArrivalSummary(context, summary)
                    },
                ) {
                    Text(stringResource(R.string.print_summary_privacy_continue))
                }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.navigationBarsPadding(),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                Button(
                    onClick = { showPrivacyWarning = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_print_summary),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(stringResource(R.string.print_summary_action))
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 24.dp),
        ) {
            item {
                ScreenHeader(
                    onBack = onBack,
                    label = stringResource(R.string.print_summary_header),
                    badge = stringResource(R.string.print_summary_badge),
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.print_summary_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))
                PaperPreview(summary)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.print_summary_checklist_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun PrintableSummaryEntryCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(LandingKitColors.Sun, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_print_summary),
                    contentDescription = null,
                    tint = LandingKitColors.Ink,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.print_summary_entry_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.print_summary_entry_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.print_summary_entry_action),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PaperPreview(summary: PrintableArrivalSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        contentColor = LandingKitColors.Ink,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFE3DED5)),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.print_summary_document_label),
                style = MaterialTheme.typography.labelSmall,
                color = LandingKitColors.Moss,
            )
            Spacer(Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LandingKitColors.Forest,
                contentColor = Color.White,
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .padding(14.dp)
                            .size(24.dp)
                            .background(LandingKitColors.Sun, CircleShape)
                            .align(Alignment.TopEnd),
                    )
                    Column(Modifier.padding(18.dp)) {
                        Text(summary.destination, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            summary.arrivalDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = LandingKitColors.Seafoam,
                        )
                    }
                }
            }
            summary.sections.forEach { section ->
                Spacer(Modifier.height(18.dp))
                Text(
                    text = section.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = LandingKitColors.Forest,
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = 7.dp, bottom = 4.dp),
                    color = Color(0xFFE3DED5),
                )
                section.entries.forEach { entry -> PreviewEntry(entry) }
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = Color(0xFFE3DED5))
            Spacer(Modifier.height(9.dp))
            Text(
                text = stringResource(R.string.print_summary_private_footer),
                style = MaterialTheme.typography.labelSmall,
                color = LandingKitColors.Moss,
            )
        }
    }
}

@Composable
private fun PreviewEntry(entry: PrintableSummaryEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.Top,
    ) {
        entry.checked?.let { checked ->
            val description = stringResource(
                if (checked) R.string.print_summary_completed_description
                else R.string.print_summary_not_completed_description,
            )
            Surface(
                modifier = Modifier
                    .size(18.dp)
                    .semantics { contentDescription = description },
                shape = RoundedCornerShape(5.dp),
                color = if (checked) LandingKitColors.Seafoam else Color.Transparent,
                border = BorderStroke(1.dp, LandingKitColors.Forest),
            ) {
                if (checked) Box(contentAlignment = Alignment.Center) {
                    Text("✓", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LandingKitColors.Moss,
            )
            if (entry.value.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(entry.value, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun printableSummaryLabels() = PrintableSummaryLabels(
    staySection = stringResource(R.string.print_summary_stay_section),
    stay = stringResource(R.string.detail_stay),
    address = stringResource(R.string.print_summary_address),
    localAddress = stringResource(R.string.print_summary_local_address),
    bookingReference = stringResource(R.string.detail_booking_reference),
    gettingThere = stringResource(R.string.arrival_mode_transfer),
    usefulOnArrival = stringResource(R.string.print_summary_useful_section),
    money = stringResource(R.string.detail_money),
    emergency = stringResource(R.string.detail_emergency),
    dietaryPhrase = stringResource(R.string.detail_dietary_phrase),
    ifPlansChange = stringResource(R.string.print_summary_plan_b_section),
    alternateTransport = stringResource(R.string.plan_b_fallback_transport_label),
    lastDeparture = stringResource(R.string.plan_b_last_service_label),
    checkInCloses = stringResource(R.string.plan_b_check_in_cutoff_label),
    lateArrival = stringResource(R.string.plan_b_late_arrival_label),
    stayContact = stringResource(R.string.plan_b_accommodation_contact_label),
    expectedCost = stringResource(R.string.plan_b_expected_cost_label),
    alternateStay = stringResource(R.string.plan_b_alternate_stay_label),
    beforeLanding = stringResource(R.string.print_summary_checklist_section),
)

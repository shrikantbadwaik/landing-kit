package com.shrikantbadwaik.landingkit.feature.kits.presentation.share

import android.content.Intent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.print.PrintableSummaryLabels
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.journalDoodles
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalBackground
import com.shrikantbadwaik.landingkit.feature.kits.share.ShareSummarySection
import com.shrikantbadwaik.landingkit.feature.kits.share.asTrustedPersonCopy
import com.shrikantbadwaik.landingkit.feature.kits.share.toShareableArrivalSummary
import java.time.format.DateTimeFormatter

@Composable
internal fun ShareSummaryScreen(
    trip: Trip,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern(stringResource(R.string.date_format_detail))
    val labels = shareSummaryLabels()
    val summary = remember(trip, labels, dateFormatter) {
        trip.toShareableArrivalSummary(trip.arrivalDate.format(dateFormatter), labels)
    }
    var selected by remember(summary) {
        mutableStateOf(
            summary.sections.map { it.type }
                .filter { it in TRUSTED_COPY_DEFAULTS }
                .toSet(),
        )
    }
    var showPrivacyWarning by remember { mutableStateOf(false) }

    if (showPrivacyWarning) {
        AlertDialog(
            onDismissRequest = { showPrivacyWarning = false },
            title = { Text(stringResource(R.string.share_summary_warning_title)) },
            text = { Text(stringResource(R.string.share_summary_warning_description)) },
            dismissButton = {
                TextButton(onClick = { showPrivacyWarning = false }) {
                    Text(stringResource(R.string.share_summary_warning_cancel))
                }
            },
            confirmButton = {
                Button(
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        showPrivacyWarning = false
                        val intent = Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(
                                Intent.EXTRA_TEXT,
                                summary.asTrustedPersonCopy(selected),
                            )
                            .putExtra(
                                Intent.EXTRA_TITLE,
                                context.getString(R.string.share_summary_chooser_title),
                            )
                        context.startActivity(
                            Intent.createChooser(
                                intent,
                                context.getString(R.string.share_summary_chooser_title),
                            ),
                        )
                    },
                ) {
                    Text(stringResource(R.string.share_summary_warning_continue))
                }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier.navigationBarsPadding(),
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.background,
            ) {
                Button(
                    shape = MaterialTheme.shapes.medium,
                    onClick = { showPrivacyWarning = true },
                    enabled = selected.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_share_summary),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(stringResource(R.string.share_summary_action))
                }
            }
        },
    ) { padding ->
        val listState = rememberLazyListState()
        JournalBackground(
            seed = "share:${trip.id}",
            listState = listState,
            count = 0,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    ScreenHeader(
                        onBack = onBack,
                        label = stringResource(R.string.share_summary_header),
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = stringResource(R.string.share_summary_intro),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                }
                items(summary.sections.size, key = { summary.sections[it].type.name }) { index ->
                    val section = summary.sections[index]
                    ShareOptionCard(
                        doodleSeed = if (index == 0 || index == summary.sections.lastIndex ||
                            (summary.sections.size > 4 && index == summary.sections.size / 2)) {
                            "share:${trip.id}:${section.type}"
                        } else null,
                        title = section.title,
                        detail = section.lines.joinToString("\n"),
                        selected = section.type in selected,
                        onSelectedChange = { isSelected ->
                            selected = if (isSelected) selected + section.type else selected - section.type
                        },
                    )
                }
            }
        }
    }
}

@Composable
internal fun ShareSummaryEntryCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(LandingKitColors.Seafoam, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    painter = painterResource(R.drawable.ic_share_summary),
                    contentDescription = null,
                    tint = LandingKitColors.Ink,
                )
            }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.share_summary_entry_title), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(3.dp))
                Text(
                    stringResource(R.string.share_summary_entry_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                stringResource(R.string.share_summary_entry_action),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private val TRUSTED_COPY_DEFAULTS = setOf(ShareSummarySection.STAY, ShareSummarySection.PLAN_B)

@Composable
private fun ShareOptionCard(
    doodleSeed: String?,
    title: String,
    detail: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
) {
    Surface(
        onClick = { onSelectedChange(!selected) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .journalDoodles(doodleSeed.orEmpty(), count = if (doodleSeed != null) 1 else 0)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(checked = selected, onCheckedChange = onSelectedChange)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(5.dp))
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun shareSummaryLabels() = PrintableSummaryLabels(
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

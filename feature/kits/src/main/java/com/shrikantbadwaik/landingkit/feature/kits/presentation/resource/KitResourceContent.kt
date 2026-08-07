package com.shrikantbadwaik.landingkit.feature.kits.presentation.resource

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplateItem
import com.shrikantbadwaik.landingkit.core.model.ItemCategory
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.DefaultChecklistContent
import com.shrikantbadwaik.landingkit.feature.kits.sample.SampleTripContent

@Composable
internal fun defaultChecklistContent() = DefaultChecklistContent(
    downloadTickets = stringResource(R.string.checklist_download_tickets),
    saveAddress = stringResource(R.string.checklist_save_address),
    confirmTransfer = stringResource(R.string.checklist_confirm_transfer),
    localCurrency = stringResource(R.string.checklist_local_currency),
)

@Composable
internal fun sampleTripContent(checklist: DefaultChecklistContent) = SampleTripContent(
    destination = stringResource(R.string.sample_destination),
    stayName = stringResource(R.string.sample_stay_name),
    address = stringResource(R.string.sample_address),
    localAddress = stringResource(R.string.sample_local_address),
    bookingReference = stringResource(R.string.sample_booking_reference),
    transfer = stringResource(R.string.sample_transfer),
    currency = stringResource(R.string.sample_currency),
    emergency = stringResource(R.string.sample_emergency),
    dietary = stringResource(R.string.sample_dietary),
    checklist = checklist,
)

@Composable
internal fun curatedChecklistTemplates() = listOf(
    ChecklistTemplate(
        id = "curated_international_arrival",
        name = stringResource(R.string.template_international_name),
        items = listOf(
            ChecklistTemplateItem(
                title = stringResource(R.string.checklist_download_tickets),
                category = ItemCategory.DOCUMENT,
            ),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_international_map),
                category = ItemCategory.TRANSPORT,
            ),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_international_connectivity),
            ),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_international_insurance),
                category = ItemCategory.SAFETY,
            ),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_international_emergency),
                category = ItemCategory.SAFETY,
            ),
        ),
    ),
    ChecklistTemplate(
        id = "curated_solo_steady",
        name = stringResource(R.string.template_solo_name),
        items = listOf(
            ChecklistTemplateItem(
                title = stringResource(R.string.template_solo_share_stay),
                category = ItemCategory.SAFETY,
            ),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_solo_check_in),
                category = ItemCategory.SAFETY,
            ),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_solo_transport),
                category = ItemCategory.TRANSPORT,
            ),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_solo_backup),
                category = ItemCategory.DOCUMENT,
            ),
        ),
    ),
    ChecklistTemplate(
        id = "curated_carry_on_ready",
        name = stringResource(R.string.template_carry_on_name),
        items = listOf(
            ChecklistTemplateItem(title = stringResource(R.string.template_carry_on_power)),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_carry_on_medication),
                category = ItemCategory.SAFETY,
            ),
            ChecklistTemplateItem(title = stringResource(R.string.template_carry_on_pen)),
            ChecklistTemplateItem(
                title = stringResource(R.string.template_carry_on_baggage),
                category = ItemCategory.DOCUMENT,
            ),
        ),
    ),
)

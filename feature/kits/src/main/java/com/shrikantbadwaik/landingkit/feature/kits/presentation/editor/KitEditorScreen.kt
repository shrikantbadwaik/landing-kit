package com.shrikantbadwaik.landingkit.feature.kits.presentation.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.MotionReveal
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalBackground
import com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component.ArrivalDateField
import com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component.ChecklistEditor
import com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component.ChecklistTemplatePanel
import com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component.FormSection
import com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component.FormValidationSummary
import com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component.LandingTextField
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.DefaultChecklistContent
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.KitForm
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.isArrivalDateSelectable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KitEditorScreen(
    trip: Trip?,
    defaultChecklistContent: DefaultChecklistContent,
    curatedTemplates: List<ChecklistTemplate>,
    personalTemplates: List<ChecklistTemplate>,
    animateEntrance: Boolean,
    onEntrancePlayed: () -> Unit,
    onBack: () -> Unit,
    onSaveTemplate: (ChecklistTemplate) -> Unit,
    onDeleteTemplate: (ChecklistTemplate) -> Unit,
    onSave: (KitForm) -> Unit,
) {
    val isEditing = trip != null
    var form by remember(trip?.id) {
        mutableStateOf(
            trip?.let(KitForm::fromTrip) ?: KitForm.newKit(defaultChecklistContent),
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var templateFeedback by remember(trip?.id) {
        mutableStateOf<TemplateFeedback?>(null)
    }
    var templateFeedbackEvent by remember(trip?.id) { mutableIntStateOf(0) }
    var hasAttemptedSave by remember(trip?.id) { mutableStateOf(false) }
    val hasPlanBData = form.fallbackTransport.isNotBlank() ||
        form.lastServiceInfo.isNotBlank() ||
        form.checkInCutoff.isNotBlank() ||
        form.lateArrivalInstructions.isNotBlank() ||
        form.accommodationContact.isNotBlank() ||
        form.expectedCostNote.isNotBlank() ||
        form.alternateStayNote.isNotBlank()
    var planBExpanded by remember(trip?.id) { mutableStateOf(hasPlanBData) }
    var planBMoreExpanded by remember(trip?.id) {
        mutableStateOf(
            form.lastServiceInfo.isNotBlank() ||
                form.expectedCostNote.isNotBlank() ||
                form.alternateStayNote.isNotBlank(),
        )
    }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val validation = form.validate()
    val today = remember { LocalDate.now() }
    val isPastArrival = validation.arrivalDate?.isBefore(today) == true

    LaunchedEffect(animateEntrance) {
        if (animateEntrance) {
            delay(760)
            onEntrancePlayed()
        }
    }

    LaunchedEffect(templateFeedbackEvent) {
        if (templateFeedback != null) {
            delay(TEMPLATE_FEEDBACK_DURATION_MILLIS)
            templateFeedback = null
        }
    }

    if (showDatePicker) {
        val initialDate = runCatching { LocalDate.parse(form.arrivalDate) }
            .getOrElse { today.plusDays(7) }
        val initialDateMillis = initialDate.toUtcMillis()
        val initialDisplayedDateMillis = if (initialDate.isBefore(today)) {
            today.toUtcMillis()
        } else {
            initialDateMillis
        }
        val selectableDates = remember(today) { ArrivalSelectableDates(today) }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDateMillis,
            initialDisplayedMonthMillis = initialDisplayedDateMillis,
            selectableDates = selectableDates,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        }?.let {
                            form = form.copy(arrivalDate = it.toString())
                        }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.editor_date_done)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.editor_date_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = .97f),
                shadowElevation = 2.dp,
            ) {
                Button(
                    onClick = {
                        if (validation.isValid) {
                            onSave(form)
                        } else {
                            hasAttemptedSave = true
                            coroutineScope.launch {
                                listState.animateScrollToItem(validation.firstInvalidSectionIndex)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 22.dp, vertical = 14.dp)
                        .heightIn(min = 58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            if (isEditing) {
                                R.string.editor_save_changes
                            } else {
                                R.string.editor_save_offline_kit
                            },
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { scaffoldPadding ->
        JournalBackground(
            seed = "editor:${trip?.id}",
            listState = listState,
            count = 0,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = scaffoldPadding.calculateBottomPadding()),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 22.dp, top = 18.dp, end = 22.dp, bottom = 30.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item(key = "editor_header") {
                    MotionReveal(index = 0, animate = animateEntrance) {
                        ScreenHeader(
                            onBack = onBack,
                            label = stringResource(
                                if (isEditing) R.string.editor_header_edit else R.string.editor_header_new,
                            ),
                        )
                    }
                }
                item(key = "editor_intro") {
                    MotionReveal(index = 1, animate = animateEntrance) {
                        Column {
                            Text(
                                text = stringResource(
                                    if (isEditing) {
                                        R.string.editor_edit_headline
                                    } else {
                                        R.string.editor_create_headline
                                    },
                                ),
                                style = MaterialTheme.typography.headlineLarge,
                            )
                            Spacer(Modifier.height(9.dp))
                            Text(
                                text = stringResource(
                                    if (isEditing) R.string.editor_edit_intro else R.string.editor_create_intro,
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AnimatedVisibility(
                                visible = hasAttemptedSave && !validation.isValid,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                Column {
                                    Spacer(Modifier.height(14.dp))
                                    FormValidationSummary(errorCount = validation.errorCount)
                                }
                            }
                        }
                    }
                }
                item(key = "editor_trip_details") {
                    MotionReveal(index = 2, animate = animateEntrance) {
                        FormSection(
                            number = stringResource(R.string.editor_step_trip),
                            title = stringResource(R.string.editor_trip_details),
                            accent = LandingKitColors.Sky,
                        ) {
                            LandingTextField(
                                label = stringResource(R.string.editor_destination_label),
                                value = form.destination,
                                placeholder = stringResource(R.string.editor_destination_placeholder),
                                error = if (hasAttemptedSave && validation.destinationMissing) {
                                    stringResource(R.string.editor_destination_required)
                                } else {
                                    null
                                },
                                onChange = { form = form.copy(destination = it) },
                            )
                            ArrivalDateField(
                                value = form.arrivalDate,
                                error = when {
                                    !hasAttemptedSave -> null
                                    validation.arrivalDateMissing -> stringResource(
                                        R.string.editor_arrival_date_required_error,
                                    )

                                    validation.arrivalDateInvalid -> stringResource(
                                        R.string.editor_arrival_date_invalid,
                                    )

                                    else -> null
                                },
                                supporting = if (isPastArrival) {
                                    stringResource(R.string.editor_past_arrival_notice)
                                } else {
                                    null
                                },
                                onClick = { showDatePicker = true },
                            )
                        }
                    }
                }
                item(key = "editor_stay") {
                    MotionReveal(index = 3, animate = animateEntrance) {
                        FormSection(
                            number = stringResource(R.string.editor_step_stay),
                            title = stringResource(R.string.editor_stay_section),
                            accent = LandingKitColors.Seafoam,
                        ) {
                            LandingTextField(
                                label = stringResource(R.string.editor_stay_name_label),
                                value = form.stayName,
                                placeholder = stringResource(R.string.editor_stay_name_placeholder),
                                error = if (hasAttemptedSave && validation.stayNameMissing) {
                                    stringResource(R.string.editor_stay_name_required)
                                } else {
                                    null
                                },
                                onChange = { form = form.copy(stayName = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.editor_address_label),
                                value = form.address,
                                placeholder = stringResource(R.string.editor_address_placeholder),
                                error = if (hasAttemptedSave && validation.addressMissing) {
                                    stringResource(R.string.editor_address_required)
                                } else {
                                    null
                                },
                                multiline = true,
                                onChange = { form = form.copy(address = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.editor_local_address_label),
                                value = form.localAddress,
                                placeholder = stringResource(R.string.editor_local_address_placeholder),
                                multiline = true,
                                onChange = { form = form.copy(localAddress = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.editor_booking_reference_label),
                                value = form.bookingReference,
                                placeholder = stringResource(R.string.editor_optional_placeholder),
                                onChange = { form = form.copy(bookingReference = it) },
                            )
                        }
                    }
                }
                item(key = "editor_first_90_minutes") {
                    MotionReveal(index = 4, animate = animateEntrance) {
                        FormSection(
                            number = stringResource(R.string.editor_step_first_minutes),
                            title = stringResource(R.string.editor_first_minutes_section),
                            accent = LandingKitColors.Lavender,
                        ) {
                            LandingTextField(
                                label = stringResource(R.string.editor_transfer_label),
                                value = form.transfer,
                                placeholder = stringResource(R.string.editor_transfer_placeholder),
                                multiline = true,
                                onChange = { form = form.copy(transfer = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.editor_currency_label),
                                value = form.currency,
                                placeholder = stringResource(R.string.editor_currency_placeholder),
                                multiline = true,
                                onChange = { form = form.copy(currency = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.editor_emergency_label),
                                value = form.emergencyContact,
                                placeholder = stringResource(R.string.editor_emergency_placeholder),
                                multiline = true,
                                onChange = { form = form.copy(emergencyContact = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.editor_dietary_label),
                                value = form.dietaryPhrase,
                                placeholder = stringResource(R.string.editor_dietary_placeholder),
                                multiline = true,
                                onChange = { form = form.copy(dietaryPhrase = it) },
                            )
                        }
                    }
                }
                item(key = "editor_checklist") {
                    MotionReveal(index = 5, animate = animateEntrance) {
                        FormSection(
                            number = stringResource(R.string.editor_step_checklist),
                            title = stringResource(R.string.editor_checklist_section),
                            accent = LandingKitColors.Sun,
                        ) {
                            val feedbackText = when (val feedback = templateFeedback) {
                                is TemplateFeedback.Applied -> if (feedback.addedCount == 0) {
                                    stringResource(
                                        R.string.template_already_applied,
                                        feedback.templateName,
                                    )
                                } else {
                                    androidx.compose.ui.res.pluralStringResource(
                                        R.plurals.template_tasks_added,
                                        feedback.addedCount,
                                        feedback.addedCount,
                                        feedback.templateName,
                                    )
                                }

                                is TemplateFeedback.Saved -> stringResource(
                                    R.string.template_saved,
                                    feedback.templateName,
                                )

                                is TemplateFeedback.Deleted -> stringResource(
                                    R.string.template_deleted,
                                    feedback.templateName,
                                )

                                null -> null
                            }
                            ChecklistTemplatePanel(
                                curatedTemplates = curatedTemplates,
                                personalTemplates = personalTemplates,
                                currentChecklist = form.checklist,
                                feedback = feedbackText,
                                onApply = { template ->
                                    val result = form.applyTemplate(template)
                                    form = result.form
                                    templateFeedback = TemplateFeedback.Applied(
                                        templateName = template.name,
                                        addedCount = result.addedCount,
                                    )
                                    templateFeedbackEvent++
                                },
                                onSave = { name ->
                                    val template = form.toChecklistTemplate(name)
                                    onSaveTemplate(template)
                                    templateFeedback = TemplateFeedback.Saved(template.name)
                                    templateFeedbackEvent++
                                },
                                onDelete = { template ->
                                    onDeleteTemplate(template)
                                    templateFeedback = TemplateFeedback.Deleted(template.name)
                                    templateFeedbackEvent++
                                },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            ChecklistEditor(
                                items = form.checklist,
                                onTitleChange = { itemId, title ->
                                    form = form.updateChecklistTitle(itemId, title)
                                },
                                onCheckedChange = { itemId, checked ->
                                    form = form.setChecklistComplete(itemId, checked)
                                },
                                onMoveUp = { itemId ->
                                    form = form.moveChecklistItem(itemId, offset = -1)
                                },
                                onMoveDown = { itemId ->
                                    form = form.moveChecklistItem(itemId, offset = 1)
                                },
                                onRemove = { itemId ->
                                    form = form.removeChecklistItem(itemId)
                                },
                                onAdd = { title ->
                                    form = form.addChecklistItem(title)
                                },
                            )
                            if (form.checklist.any { it.title.isBlank() }) {
                                Text(
                                    text = stringResource(R.string.editor_checklist_title_required),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
                item(key = "editor_plan_b") {
                    MotionReveal(index = 6, animate = animateEntrance) {
                        FormSection(
                            number = stringResource(R.string.editor_step_plan_b),
                            title = stringResource(R.string.plan_b_section_title),
                            accent = LandingKitColors.Moss,
                            expanded = planBExpanded,
                            onToggle = { planBExpanded = !planBExpanded },
                        ) {
                            Text(
                                text = stringResource(R.string.plan_b_section_description),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(R.string.plan_b_required_guidance),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            LandingTextField(
                                label = stringResource(R.string.plan_b_fallback_transport_label),
                                value = form.fallbackTransport,
                                placeholder = stringResource(R.string.plan_b_fallback_transport_hint),
                                multiline = true,
                                onChange = { form = form.copy(fallbackTransport = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.plan_b_accommodation_contact_label),
                                value = form.accommodationContact,
                                placeholder = stringResource(
                                    R.string.plan_b_accommodation_contact_hint,
                                ),
                                onChange = { form = form.copy(accommodationContact = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.plan_b_check_in_cutoff_label),
                                value = form.checkInCutoff,
                                placeholder = stringResource(R.string.plan_b_check_in_cutoff_hint),
                                onChange = { form = form.copy(checkInCutoff = it) },
                            )
                            LandingTextField(
                                label = stringResource(R.string.plan_b_late_arrival_label),
                                value = form.lateArrivalInstructions,
                                placeholder = stringResource(R.string.plan_b_late_arrival_hint),
                                multiline = true,
                                onChange = { form = form.copy(lateArrivalInstructions = it) },
                            )
                            TextButton(onClick = { planBMoreExpanded = !planBMoreExpanded }) {
                                Text(
                                    stringResource(
                                        if (planBMoreExpanded) {
                                            R.string.plan_b_fewer_details
                                        } else {
                                            R.string.plan_b_more_details
                                        },
                                    ),
                                )
                            }
                            AnimatedVisibility(visible = planBMoreExpanded) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    LandingTextField(
                                        label = stringResource(R.string.plan_b_last_service_label),
                                        value = form.lastServiceInfo,
                                        placeholder = stringResource(R.string.plan_b_last_service_hint),
                                        onChange = { form = form.copy(lastServiceInfo = it) },
                                    )
                                    LandingTextField(
                                        label = stringResource(R.string.plan_b_expected_cost_label),
                                        value = form.expectedCostNote,
                                        placeholder = stringResource(R.string.plan_b_expected_cost_hint),
                                        onChange = { form = form.copy(expectedCostNote = it) },
                                    )
                                    LandingTextField(
                                        label = stringResource(R.string.plan_b_alternate_stay_label),
                                        value = form.alternateStayNote,
                                        placeholder = stringResource(
                                            R.string.plan_b_alternate_stay_hint,
                                        ),
                                        multiline = true,
                                        onChange = { form = form.copy(alternateStayNote = it) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed interface TemplateFeedback {
    data class Applied(
        val templateName: String,
        val addedCount: Int,
    ) : TemplateFeedback

    data class Saved(val templateName: String) : TemplateFeedback
    data class Deleted(val templateName: String) : TemplateFeedback
}

private const val TEMPLATE_FEEDBACK_DURATION_MILLIS = 4_000L

@OptIn(ExperimentalMaterial3Api::class)
private class ArrivalSelectableDates(private val today: LocalDate) : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean = isArrivalDateSelectable(
        candidate = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate(),
        today = today,
    )

    override fun isSelectableYear(year: Int): Boolean = year >= today.year
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

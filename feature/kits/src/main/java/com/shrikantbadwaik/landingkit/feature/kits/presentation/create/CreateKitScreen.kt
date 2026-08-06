package com.shrikantbadwaik.landingkit.feature.kits.presentation.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.MotionReveal
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.ScreenHeader
import com.shrikantbadwaik.landingkit.feature.kits.presentation.create.component.ArrivalDateField
import com.shrikantbadwaik.landingkit.feature.kits.presentation.create.component.FormSection
import com.shrikantbadwaik.landingkit.feature.kits.presentation.create.component.LandingTextField
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.KitForm
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateKitScreen(
    animateEntrance: Boolean,
    onEntrancePlayed: () -> Unit,
    onBack: () -> Unit,
    onSave: (KitForm) -> Unit,
) {
    var form by remember { mutableStateOf(KitForm()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val requiredReady = form.destination.isNotBlank() &&
        form.stayName.isNotBlank() &&
        form.address.isNotBlank()

    LaunchedEffect(animateEntrance) {
        if (animateEntrance) {
            delay(760)
            onEntrancePlayed()
        }
    }

    if (showDatePicker) {
        val initialDate = runCatching { LocalDate.parse(form.arrivalDate) }
            .getOrElse { LocalDate.now().plusDays(7) }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
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
                ) { Text("Done") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
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
                shadowElevation = 12.dp,
            ) {
                Button(
                    onClick = { onSave(form) },
                    enabled = requiredReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 22.dp, vertical = 14.dp)
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LandingKitColors.Forest,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Text("Save offline kit  →", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = scaffoldPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(start = 22.dp, top = 18.dp, end = 22.dp, bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item(key = "create_header") {
                MotionReveal(index = 0, animate = animateEntrance) {
                    ScreenHeader(onBack = onBack, label = "NEW ARRIVAL KIT", badge = "3 steps")
                }
            }
            item(key = "create_intro") {
                MotionReveal(index = 1, animate = animateEntrance) {
                    Column {
                        Text(
                            text = "Build a calmer\narrival.",
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Spacer(Modifier.height(9.dp))
                        Text(
                            "Just the details you’ll need when your battery, signal or brain is running low.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item(key = "create_trip_details") {
                MotionReveal(index = 2, animate = animateEntrance) {
                    FormSection(number = "01", title = "Trip details", accent = LandingKitColors.Sky) {
                        LandingTextField(
                            label = "Destination *",
                            value = form.destination,
                            placeholder = "e.g. Kyoto, Japan",
                            onChange = { form = form.copy(destination = it) },
                        )
                        ArrivalDateField(
                            value = form.arrivalDate,
                            onClick = { showDatePicker = true },
                        )
                    }
                }
            }
            item(key = "create_stay") {
                MotionReveal(index = 3, animate = animateEntrance) {
                    FormSection(
                        number = "02",
                        title = "Where you’re staying",
                        accent = LandingKitColors.Seafoam,
                    ) {
                        LandingTextField(
                            label = "Stay name *",
                            value = form.stayName,
                            placeholder = "Hotel, hostel or host",
                            onChange = { form = form.copy(stayName = it) },
                        )
                        LandingTextField(
                            label = "Address *",
                            value = form.address,
                            placeholder = "Street address",
                            multiline = true,
                            onChange = { form = form.copy(address = it) },
                        )
                        LandingTextField(
                            label = "Address in local language",
                            value = form.localAddress,
                            placeholder = "The version to show a driver",
                            multiline = true,
                            onChange = { form = form.copy(localAddress = it) },
                        )
                        LandingTextField(
                            label = "Booking reference",
                            value = form.bookingReference,
                            placeholder = "Optional",
                            onChange = { form = form.copy(bookingReference = it) },
                        )
                    }
                }
            }
            item(key = "create_first_90_minutes") {
                MotionReveal(index = 4, animate = animateEntrance) {
                    FormSection(
                        number = "03",
                        title = "First 90 minutes",
                        accent = LandingKitColors.Lavender,
                    ) {
                        LandingTextField(
                            label = "Transfer instructions",
                            value = form.transfer,
                            placeholder = "Train, taxi, meeting point…",
                            multiline = true,
                            onChange = { form = form.copy(transfer = it) },
                        )
                        LandingTextField(
                            label = "Currency cheat sheet",
                            value = form.currency,
                            placeholder = "e.g. ¥1,000 ≈ ₹570",
                            multiline = true,
                            onChange = { form = form.copy(currency = it) },
                        )
                        LandingTextField(
                            label = "Emergency contact",
                            value = form.emergencyContact,
                            placeholder = "Local emergency or trusted person",
                            multiline = true,
                            onChange = { form = form.copy(emergencyContact = it) },
                        )
                        LandingTextField(
                            label = "Dietary phrase",
                            value = form.dietaryPhrase,
                            placeholder = "A phrase you may need to show",
                            multiline = true,
                            onChange = { form = form.copy(dietaryPhrase = it) },
                        )
                    }
                }
            }
            item(key = "create_required_note") {
                Text(
                    "Required: destination, stay name and address. Everything else can be added when you have it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

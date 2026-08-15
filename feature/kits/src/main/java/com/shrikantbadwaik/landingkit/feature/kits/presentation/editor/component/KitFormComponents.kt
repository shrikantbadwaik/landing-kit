package com.shrikantbadwaik.landingkit.feature.kits.presentation.editor.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.feature.kits.R
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
internal fun FormValidationSummary(errorCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = .55f),
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = .25f),
        ),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Text(
                text = stringResource(R.string.editor_validation_title),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = pluralStringResource(
                    R.plurals.editor_validation_summary,
                    errorCount,
                    errorCount,
                ),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
internal fun FormSection(
    number: String,
    title: String,
    accent: Color,
    expanded: Boolean? = null,
    onToggle: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val collapsible = expanded != null && onToggle != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (collapsible) Modifier.clickable { onToggle() } else Modifier),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = accent,
                    contentColor = LandingKitColors.Ink,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        number,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    title,
                    modifier = if (collapsible) Modifier.weight(1f) else Modifier,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (collapsible) {
                    Text(
                        text = if (expanded) "−" else "+",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (collapsible) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        content()
                    }
                }
            } else {
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun LandingTextField(
    label: String,
    value: String,
    placeholder: String,
    supporting: String? = null,
    error: String? = null,
    multiline: Boolean = false,
    onChange: (String) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(7.dp))
        TextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f))
            },
            minLines = if (multiline) 2 else 1,
            maxLines = if (multiline) 4 else 1,
            isError = error != null,
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
        )
        val supportingMessage = error ?: supporting
        if (supportingMessage != null) {
            Spacer(Modifier.height(5.dp))
            Text(
                supportingMessage,
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (error != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
internal fun ArrivalDateField(
    value: String,
    error: String? = null,
    supporting: String? = null,
    onClick: () -> Unit,
) {
    val datePattern = stringResource(R.string.date_format_card)
    val displayValue = remember(value, datePattern) {
        runCatching {
            LocalDate.parse(value).format(DateTimeFormatter.ofPattern(datePattern))
        }.getOrDefault("")
    }
    Column {
        Text(
            stringResource(R.string.editor_arrival_date_required),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(7.dp))
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            color = if (error != null) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = .45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)
            },
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 17.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = displayValue.ifBlank {
                        stringResource(R.string.editor_choose_arrival_date)
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (displayValue.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .72f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    stringResource(R.string.symbol_forward),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        val supportingMessage = error ?: supporting
        if (supportingMessage != null) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = supportingMessage,
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (error != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

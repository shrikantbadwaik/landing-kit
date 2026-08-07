package com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.feature.kits.R

@Composable
internal fun CoralButton(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        color = LandingKitColors.Coral,
        contentColor = LandingKitColors.Ink,
        shape = RoundedCornerShape(50),
        shadowElevation = 3.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 17.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
internal fun EmptyKitCard(onCreate: () -> Unit, onSample: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LandingKitColors.Lavender.copy(alpha = .55f)),
        shape = RoundedCornerShape(30.dp),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                stringResource(R.string.symbol_sparkle),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.home_empty_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.home_empty_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onCreate,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(17.dp),
            ) { Text(stringResource(R.string.home_create_first_kit)) }
            Spacer(Modifier.height(8.dp))
            Surface(
                onClick = onSample,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    stringResource(R.string.home_explore_sample),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
internal fun NoUpcomingArrivalsCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = LandingKitColors.Seafoam.copy(alpha = .28f),
        ),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.home_no_upcoming_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(R.string.home_no_upcoming_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

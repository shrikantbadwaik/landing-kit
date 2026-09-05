package com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalArtwork
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.JournalScene

@Composable
internal fun CoralButton(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
        color = LandingKitColors.Coral,
        contentColor = LandingKitColors.Ink,
        shape = MaterialTheme.shapes.small,
        shadowElevation = 1.dp,
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(24.dp)) {
            JournalArtwork(
                JournalScene.Travelers,
                Modifier.fillMaxWidth().height(112.dp),
                monochrome = true,
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.home_empty_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.home_empty_description),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .76f),
            )
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = onCreate,
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                shape = MaterialTheme.shapes.medium,
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
    Column(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
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

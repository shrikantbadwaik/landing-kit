package com.shrikantbadwaik.landingkit.feature.kits.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.feature.kits.R

@Composable
internal fun MissingKitScreen(onHome: () -> Unit) {
    UnavailableContentScreen(
        header = stringResource(R.string.missing_kit_header),
        title = stringResource(R.string.missing_kit_title),
        description = stringResource(R.string.missing_kit_description),
        onHome = onHome,
    )
}

@Composable
internal fun MissingTicketScreen(onHome: () -> Unit) {
    UnavailableContentScreen(
        header = stringResource(R.string.missing_ticket_header),
        title = stringResource(R.string.missing_ticket_title),
        description = stringResource(R.string.missing_ticket_description),
        onHome = onHome,
    )
}

@Composable
private fun UnavailableContentScreen(
    header: String,
    title: String,
    description: String,
    onHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .journalDoodles("missing")
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(22.dp),
    ) {
        ScreenHeader(
            onBack = onHome,
            label = header,
        )
        Spacer(Modifier.height(34.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Column(Modifier.padding(24.dp)) {
                JournalArtwork(
                    JournalScene.Travelers,
                    Modifier.fillMaxWidth().height(112.dp),
                    monochrome = true,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .76f),
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.missing_kit_home_action))
                }
            }
        }
    }
}

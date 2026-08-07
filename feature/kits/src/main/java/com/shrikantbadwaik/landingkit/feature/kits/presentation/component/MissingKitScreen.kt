package com.shrikantbadwaik.landingkit.feature.kits.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.feature.kits.R

@Composable
internal fun MissingKitScreen(onHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(22.dp),
    ) {
        ScreenHeader(
            onBack = onHome,
            label = stringResource(R.string.missing_kit_header),
            badge = stringResource(R.string.missing_kit_badge),
        )
        Spacer(Modifier.height(34.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = LandingKitColors.Lavender.copy(alpha = .55f),
            ),
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.symbol_sparkle),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.missing_kit_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(Modifier.height(9.dp))
                Text(
                    text = stringResource(R.string.missing_kit_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onHome,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                ) {
                    Text(stringResource(R.string.missing_kit_home_action))
                }
            }
        }
    }
}

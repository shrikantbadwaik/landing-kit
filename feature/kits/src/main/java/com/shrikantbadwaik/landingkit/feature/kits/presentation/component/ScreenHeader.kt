package com.shrikantbadwaik.landingkit.feature.kits.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.feature.kits.R

@Composable
internal fun ScreenHeader(onBack: () -> Unit, label: String, badge: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier.size(46.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = CircleShape,
            shadowElevation = 2.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.symbol_back), style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(Modifier.width(13.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
        Surface(color = LandingKitColors.Sun.copy(alpha = .55f), shape = CircleShape) {
            Text(
                badge,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

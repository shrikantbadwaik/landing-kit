package com.shrikantbadwaik.landingkit.feature.kits.presentation.home.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.feature.kits.R

@Composable
internal fun BrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        BrandMark()
        Spacer(Modifier.width(11.dp))
        Column {
            Text(
                text = stringResource(R.string.brand_name),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.brand_tagline),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(LandingKitColors.Forest),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(23.dp)) {
            drawCircle(color = LandingKitColors.Sun, radius = size.minDimension * .18f, center = center)
            drawLine(
                color = Color.White,
                start = Offset(center.x, 0f),
                end = Offset(center.x, size.height),
                strokeWidth = 2.dp.toPx(),
            )
            drawLine(
                color = Color.White,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = 2.dp.toPx(),
            )
        }
    }
}

@Composable
internal fun PrivacyNote() {
    Surface(
        color = LandingKitColors.Seafoam.copy(alpha = .42f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(color = LandingKitColors.Forest, shape = CircleShape) {
                Text(
                    text = stringResource(R.string.symbol_check),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(11.dp))
            Column {
                Text(stringResource(R.string.privacy_title), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.privacy_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

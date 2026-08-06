package com.shrikantbadwaik.landingkit.feature.kits.presentation.detail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.model.ArrivalItem

@Composable
internal fun AnimatedChecklistItem(item: ArrivalItem, onCheck: (Boolean) -> Unit) {
    val containerColor by animateColorAsState(
        targetValue = if (item.isComplete) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = .42f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "Checklist completion color",
    )
    val textColor by animateColorAsState(
        targetValue = if (item.isComplete) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),
        label = "Checklist text color",
    )

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.animateContentSize(tween(320, easing = FastOutSlowInEasing)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Checkbox(checked = item.isComplete, onCheckedChange = onCheck)
            Column(Modifier.padding(vertical = 7.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall, color = textColor)
                if (item.detail.isNotBlank()) {
                    Text(
                        item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun DetailCard(label: String, value: String, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
    ) {
        Row(Modifier.padding(18.dp)) {
            Box(Modifier.width(5.dp).height(44.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(6.dp))
                Text(value, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

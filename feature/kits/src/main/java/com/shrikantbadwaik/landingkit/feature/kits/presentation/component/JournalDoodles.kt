package com.shrikantbadwaik.landingkit.feature.kits.presentation.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.feature.kits.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val doodleResources = listOf(
    R.drawable.journal_doodle_glasses, R.drawable.journal_doodle_plane,
    R.drawable.journal_doodle_globe, R.drawable.journal_doodle_passport,
    R.drawable.journal_doodle_camera, R.drawable.journal_doodle_pin,
    R.drawable.journal_doodle_map, R.drawable.journal_doodle_ticket,
    R.drawable.journal_doodle_photos, R.drawable.journal_doodle_signpost,
    R.drawable.journal_doodle_compass, R.drawable.journal_doodle_lifebuoy,
    R.drawable.journal_doodles,
)

private data class DoodlePlacement(
    val resource: Int,
    val anchor: Float,
    val horizontal: Float,
    val vertical: Float,
    val tilt: Float,
    val sizeDp: Int,
)

/** Draw-only decoration: never adds a layout item, hit target, or semantic node.
 * A page seed keeps the shuffle stable. Reading item geometry during drawing
 * follows drag, fling, animated scroll, and changing text sizes without estimating
 * accumulated scroll distances or triggering per-frame recomposition.
 */
@Composable
internal fun Modifier.journalDoodles(
    seed: String,
    listState: LazyListState? = null,
    count: Int = 2,
    ink: Color = MaterialTheme.colorScheme.primary,
): Modifier {
    if (count <= 0) return this
    val placements = remember(seed, count) {
        val random = Random(seed.hashCode())
        val elementCount = count.coerceIn(1, 3)
        val startOnLeft = random.nextBoolean()
        doodleResources.shuffled(random).take(elementCount).mapIndexed { slot, resource ->
            DoodlePlacement(
                resource = resource,
                anchor = (slot + .35f + random.nextFloat() * .3f) / elementCount,
                horizontal = if ((slot % 2 == 0) == startOnLeft) {
                    random.nextFloat() * .25f
                } else {
                    .7f + random.nextFloat() * .3f
                },
                vertical = .25f + random.nextFloat() * .65f,
                tilt = random.nextFloat() * 24f - 12f,
                sizeDp = random.nextInt(112, 157),
            )
        }
    }
    val painters = placements.map { painterResource(it.resource) }
    val colorFilter = ColorFilter.tint(ink)
    return drawWithContent {
        val layout = listState?.layoutInfo
        placements.forEachIndexed { slot, placement ->
            val item = if (layout != null) {
                if (layout.totalItemsCount == 0) return@forEachIndexed
                val anchorIndex = (layout.totalItemsCount * placement.anchor).toInt()
                    .coerceAtMost(layout.totalItemsCount - 1)
                layout.visibleItemsInfo.firstOrNull { it.index == anchorIndex }
                    ?: return@forEachIndexed
            } else {
                null
            }
            val side = placement.sizeDp.dp.toPx().coerceAtMost(size.width * .45f)
            val painter = painters[slot]
            val intrinsic = painter.intrinsicSize
            val ratio = intrinsic.width / intrinsic.height
            val naturalSize = if (ratio >= 1f) Size(side, side / ratio) else Size(side * ratio, side)
            val angle = placement.tilt * (Math.PI / 180).toFloat()
            val cosine = abs(cos(angle))
            val sine = abs(sin(angle))
            val naturalHeight = naturalSize.width * sine + naturalSize.height * cosine
            // Keep the rotated motif inside its anchor. Lazy item culling then
            // never removes an illustration while a portion remains on screen.
            val availableHeight = item?.size?.toFloat() ?: size.height
            val scale = ((availableHeight - 2.dp.toPx()).coerceAtLeast(0f) / naturalHeight)
                .coerceAtMost(1f)
            val artSize = naturalSize * scale
            val rotatedHeight = artSize.width * sine + artSize.height * cosine
            val rotatedWidth = artSize.width * cosine + artSize.height * sine
            val top = if (item != null) {
                // A single long item (print preview) gets spaced motifs within that item.
                val withinItem = if (layout?.totalItemsCount == 1) placement.anchor else placement.vertical
                item.offset + (item.size - rotatedHeight).coerceAtLeast(0f) * withinItem
            } else {
                (size.height - rotatedHeight).coerceAtLeast(0f) * placement.anchor
            } + (rotatedHeight - artSize.height) / 2
            val left = (size.width - rotatedWidth) * placement.horizontal +
                (rotatedWidth - artSize.width) / 2
            translate(left, top) {
                rotate(placement.tilt, pivot = androidx.compose.ui.geometry.Offset(artSize.width / 2, artSize.height / 2)) {
                    with(painter) { draw(artSize, alpha = .10f, colorFilter = colorFilter) }
                }
            }
        }
        drawContent()
    }
}

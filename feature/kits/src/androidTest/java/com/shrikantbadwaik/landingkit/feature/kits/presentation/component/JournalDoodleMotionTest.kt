package com.shrikantbadwaik.landingkit.feature.kits.presentation.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JournalDoodleMotionTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun doodle_follows_animated_scroll_without_spacing_changes_or_reshuffling() {
        lateinit var state: LazyListState
        lateinit var scope: CoroutineScope
        var density = 1f
        val revision = mutableIntStateOf(0)
        composeRule.setContent {
            LandingKitTheme(darkTheme = false) {
                state = rememberLazyListState()
                scope = rememberCoroutineScope()
                density = LocalDensity.current.density
                // Reading unrelated state simulates a form or selection recomposition.
                revision.intValue
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxWidth().height(600.dp)
                        .background(Color.White)
                        .journalDoodles("motion-test", state, count = 1)
                        .testTag("page"),
                ) {
                    items(10) { Box(Modifier.fillMaxWidth().height(220.dp)) }
                }
            }
        }

        // Find a frame where the complete motif is visible, without coupling the
        // check to which resource or anchor the seeded shuffle happens to choose.
        var before: InkBounds? = null
        for (index in 0..7) {
            composeRule.runOnIdle { scope.launch { state.scrollToItem(index) } }
            composeRule.waitForIdle()
            val ink = inkBounds()
            if (ink != null && ink.top > 60 * density && ink.bottom < 540 * density) {
                before = ink
                break
            }
        }
        val initial = checkNotNull(before) { "Doodle was never visible in the content background" }
        val geometry = state.layoutInfo.visibleItemsInfo.map { it.index to (it.offset to it.size) }
        composeRule.runOnIdle { revision.intValue++ }
        composeRule.waitForIdle()
        assertEquals(initial, inkBounds())
        assertEquals(geometry, state.layoutInfo.visibleItemsInfo.map { it.index to (it.offset to it.size) })
        assertEquals(10, state.layoutInfo.totalItemsCount)
        assertTrue(state.layoutInfo.visibleItemsInfo.all { it.size == (220 * density).toInt() })

        composeRule.runOnIdle { scope.launch { state.animateScrollBy(40 * density) } }
        composeRule.waitForIdle()
        val moved = checkNotNull(inkBounds())
        assertEquals(initial.centroidY - 40 * density, moved.centroidY, 2.0)
        assertEquals(initial.pixelCount, moved.pixelCount)
    }

    private fun inkBounds(): InkBounds? {
        val pixels = composeRule.onNodeWithTag("page").captureToImage().toPixelMap()
        var top = pixels.height
        var bottom = 0
        var sumY = 0L
        var count = 0
        for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
            if (pixels[x, y].red < .98f) {
                top = minOf(top, y)
                bottom = maxOf(bottom, y)
                sumY += y
                count++
            }
        }
        return if (count == 0) null else InkBounds(top, bottom, sumY.toDouble() / count, count)
    }

    private data class InkBounds(val top: Int, val bottom: Int, val centroidY: Double, val pixelCount: Int)
}

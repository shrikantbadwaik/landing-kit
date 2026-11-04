package com.shrikantbadwaik.landingkit.feature.kits.presentation.ticket

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitTheme
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TicketPreviewLifecycleTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()
    private val files = CopyOnWriteArrayList<File>()
    private val showing = mutableStateOf(true)
    private val unlocked = mutableStateOf(true)
    private val attachment = KitAttachment("ticket", "trip", "ticket.png", "image/png", 100, 1)

    @After fun cleanUp() {
        files.forEach { it.delete() }
    }

    @Test fun exit_releases_the_presented_plaintext_copy() {
        showPresenter()
        composeRule.waitUntil { files.size == 1 }
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.ticket_presenter_exit_description),
        ).performClick()
        composeRule.runOnIdle { assertFalse(files.single().exists()) }
    }

    @Test fun background_releases_copy_and_foreground_prepares_a_fresh_one() {
        showPresenter()
        composeRule.waitUntil { files.size == 1 }
        val first = files.single()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        assertFalse(first.exists())
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitUntil { files.size == 2 }
        assertTrue(files.last().exists())
    }

    @Test fun authentication_expiry_releases_copy() {
        showPresenter()
        composeRule.waitUntil { files.size == 1 }
        composeRule.runOnIdle { unlocked.value = false }
        composeRule.waitForIdle()
        assertFalse(files.single().exists())
    }

    @Test fun preparation_finishing_after_exit_is_released_without_displaying() {
        var pending: ((Result<PreparedAttachment>) -> Unit)? = null
        showPresenter { _, complete -> pending = complete }
        composeRule.runOnIdle {
            showing.value = false
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle { checkNotNull(pending)(Result.success(copy())) }
        assertFalse(files.single().exists())
    }

    @Test fun pdf_background_and_exit_release_copies_safely() {
        showPresenter(
            document = attachment.copy(mimeType = "application/pdf", displayName = "ticket.pdf"),
            prepare = { _, complete -> complete(Result.success(pdfCopy())) },
        )
        composeRule.waitUntil { files.size == 1 }
        val pageDescription = composeRule.activity.getString(
            R.string.ticket_presenter_pdf_page_description, 1, 1,
        )
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithContentDescription(pageDescription).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithContentDescription(pageDescription).assertExists()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        assertFalse(files.first().exists())
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitUntil { files.size == 2 }
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.ticket_presenter_exit_description),
        ).performClick()
        composeRule.runOnIdle { assertTrue(files.none { it.exists() }) }
    }

    private fun showPresenter(
        document: KitAttachment = attachment,
        prepare: (String, (Result<PreparedAttachment>) -> Unit) -> Unit = { _, complete ->
            complete(Result.success(copy()))
        },
    ) {
        composeRule.setContent {
            LandingKitTheme {
                if (showing.value) TicketPresentationScreen(
                    attachments = listOf(document),
                    initialAttachmentId = document.id,
                    attachmentProtectionEnabled = true,
                    isAttachmentAccessUnlocked = unlocked.value,
                    onPrepareAttachment = prepare,
                    onExit = { showing.value = false },
                )
            }
        }
    }

    private fun copy(): PreparedAttachment {
        val file = File.createTempFile("ticket-lifecycle-", ".png", composeRule.activity.cacheDir)
        Bitmap.createBitmap(24, 36, Bitmap.Config.ARGB_8888).also { bitmap ->
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        files += file
        return PreparedAttachment(file, "image/png", onClose = { file.delete(); Unit })
    }

    private fun pdfCopy(): PreparedAttachment {
        val file = File.createTempFile("ticket-lifecycle-", ".pdf", composeRule.activity.cacheDir)
        val pdf = PdfDocument()
        try {
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(300, 400, 1).create())
            page.canvas.drawColor(android.graphics.Color.WHITE)
            pdf.finishPage(page)
            file.outputStream().use { pdf.writeTo(it) }
        } finally {
            pdf.close()
        }
        files += file
        return PreparedAttachment(file, "application/pdf", onClose = { file.delete(); Unit })
    }
}

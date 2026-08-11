package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.ByteArrayInputStream
import java.io.File
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedAttachmentStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val store = EncryptedAttachmentStore(context)

    @After
    fun cleanUp() {
        store.deleteTrip(TRIP_ID)
    }

    @Test
    fun verifyDecryptable_reads_entire_ciphertext_without_creating_preview() {
        store.encrypt(
            TRIP_ID,
            ATTACHMENT_ID,
            ByteArrayInputStream("offline ticket".toByteArray()),
        )

        assertTrue(store.exists(TRIP_ID, ATTACHMENT_ID))
        assertTrue(store.verifyDecryptable(TRIP_ID, ATTACHMENT_ID))
        assertFalse(previewDirectory().listFiles().orEmpty().any {
            it.name.startsWith("$TRIP_ID-$ATTACHMENT_ID.")
        })
    }

    @Test
    fun verifyDecryptable_returns_false_for_missing_or_corrupted_ciphertext() {
        assertFalse(store.verifyDecryptable(TRIP_ID, ATTACHMENT_ID))
        store.encrypt(
            TRIP_ID,
            ATTACHMENT_ID,
            ByteArrayInputStream("offline ticket".toByteArray()),
        )
        encryptedFile().apply {
            val bytes = readBytes()
            bytes[bytes.lastIndex] = (bytes.last() + 1).toByte()
            writeBytes(bytes)
        }

        assertFalse(store.verifyDecryptable(TRIP_ID, ATTACHMENT_ID))
    }

    private fun encryptedFile() = File(
        context.filesDir,
        "encrypted_attachments/$TRIP_ID/$ATTACHMENT_ID.lka",
    )

    private fun previewDirectory() = File(context.cacheDir, "attachment_previews")

    companion object {
        private const val TRIP_ID = "offline-check-trip"
        private const val ATTACHMENT_ID = "ticket"
    }
}

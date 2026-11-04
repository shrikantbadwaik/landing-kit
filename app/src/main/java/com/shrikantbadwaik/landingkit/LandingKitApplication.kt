package com.shrikantbadwaik.landingkit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.shrikantbadwaik.landingkit.core.data.local.attachment.EncryptedAttachmentStore
import javax.inject.Inject

@HiltAndroidApp
class LandingKitApplication : Application() {
    @Inject lateinit var attachmentStore: EncryptedAttachmentStore

    override fun onCreate() {
        super.onCreate()
        attachmentStore.cleanInactiveViewingPreviews()
    }
}

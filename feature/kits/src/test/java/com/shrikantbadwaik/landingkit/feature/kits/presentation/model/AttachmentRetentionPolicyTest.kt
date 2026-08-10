package com.shrikantbadwaik.landingkit.feature.kits.presentation.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttachmentRetentionPolicyTest {
    @Test
    fun `past arrival with evidence offers cleanup`() {
        assertTrue(shouldOfferAttachmentCleanup(ArrivalPhase.PAST, attachmentCount = 2))
    }

    @Test
    fun `past arrival without evidence does not offer cleanup`() {
        assertFalse(shouldOfferAttachmentCleanup(ArrivalPhase.PAST, attachmentCount = 0))
    }

    @Test
    fun `upcoming arrival never offers retention cleanup`() {
        assertFalse(shouldOfferAttachmentCleanup(ArrivalPhase.UPCOMING, attachmentCount = 2))
    }
}

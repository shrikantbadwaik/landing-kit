package com.shrikantbadwaik.landingkit.feature.kits

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttachmentProtectionPolicyTest {
    @Test
    fun `protected attachment requires authentication while session is locked`() {
        assertTrue(
            requiresAttachmentAuthentication(
                protectionEnabled = true,
                accessUnlocked = false,
            ),
        )
    }

    @Test
    fun `protected attachment opens during authenticated session`() {
        assertFalse(
            requiresAttachmentAuthentication(
                protectionEnabled = true,
                accessUnlocked = true,
            ),
        )
    }

    @Test
    fun `unprotected attachment never requires authentication`() {
        assertFalse(
            requiresAttachmentAuthentication(
                protectionEnabled = false,
                accessUnlocked = false,
            ),
        )
    }
}

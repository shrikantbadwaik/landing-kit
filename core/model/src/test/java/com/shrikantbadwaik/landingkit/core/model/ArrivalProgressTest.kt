package com.shrikantbadwaik.landingkit.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ArrivalProgressTest {
    @Test
    fun `items retain stable ids and completion state when copied`() {
        val item = ArrivalItem(id = "ticket", title = "Download ticket")
        val completed = item.copy(isComplete = true)

        assertEquals("ticket", completed.id)
        assertEquals(true, completed.isComplete)
    }
}

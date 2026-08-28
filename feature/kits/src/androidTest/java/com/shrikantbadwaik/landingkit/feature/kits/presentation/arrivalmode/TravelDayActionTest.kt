package com.shrikantbadwaik.landingkit.feature.kits.presentation.arrivalmode

import android.content.ClipDescription
import android.content.Intent
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TravelDayActionTest {
    @Test
    fun actions_build_user_controlled_system_intents_without_sending() {
        val map = TravelDayAction.OpenMap("Machiya Stay Gion, Kyoto").externalIntent()
        val call = TravelDayAction.CallStay("+81 75 555 1234").externalIntent()
        val message = TravelDayAction.MessageStay("+81 75 555 1234").externalIntent()

        assertEquals(Intent.ACTION_VIEW, map.action)
        assertEquals("geo", map.data?.scheme)
        assertEquals("geo:0,0?q=Machiya%20Stay%20Gion%2C%20Kyoto", map.dataString)
        assertEquals(Intent.ACTION_DIAL, call.action)
        assertEquals("tel:%2B81%2075%20555%201234", call.dataString)
        assertEquals(Intent.ACTION_SENDTO, message.action)
        assertEquals("smsto:%2B81%2075%20555%201234", message.dataString)
        assertTrue(call.getStringExtra(Intent.EXTRA_TEXT) == null)
        assertTrue(message.getStringExtra(Intent.EXTRA_TEXT) == null)
    }

    @Test
    fun copy_address_stays_local_and_marks_clipboard_content_sensitive() {
        val clip = sensitiveAddressClip("京都市東山区\n2F, 570 Gionmachi, Kyoto")

        assertEquals(
            "京都市東山区\n2F, 570 Gionmachi, Kyoto",
            clip.getItemAt(0).text.toString(),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            assertTrue(
                clip.description.extras?.getBoolean(
                    ClipDescription.EXTRA_IS_SENSITIVE,
                ) == true,
            )
        }
    }
}

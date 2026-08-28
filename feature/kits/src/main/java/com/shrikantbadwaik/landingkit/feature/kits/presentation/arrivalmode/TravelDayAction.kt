package com.shrikantbadwaik.landingkit.feature.kits.presentation.arrivalmode

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle

internal sealed interface TravelDayAction {
    data class CopyAddress(val address: String) : TravelDayAction

    data class OpenMap(val query: String) : TravelDayAction

    data class CallStay(val phoneNumber: String) : TravelDayAction

    data class MessageStay(val phoneNumber: String) : TravelDayAction
}

internal enum class TravelDayActionResult {
    Started,
    AddressCopied,
    MapUnavailable,
    CallingUnavailable,
    MessagingUnavailable,
}

internal class AndroidTravelDayActionHandler(
    private val context: Context,
) {
    fun perform(action: TravelDayAction): TravelDayActionResult = when (action) {
        is TravelDayAction.CopyAddress -> copyAddress(action.address)
        is TravelDayAction.OpenMap -> start(
            intent = action.externalIntent(),
            unavailable = TravelDayActionResult.MapUnavailable,
        )
        is TravelDayAction.CallStay -> start(
            intent = action.externalIntent(),
            unavailable = TravelDayActionResult.CallingUnavailable,
        )
        is TravelDayAction.MessageStay -> start(
            intent = action.externalIntent(),
            unavailable = TravelDayActionResult.MessagingUnavailable,
        )
    }

    private fun copyAddress(address: String): TravelDayActionResult {
        val clip = sensitiveAddressClip(address)
        context.getSystemService(ClipboardManager::class.java).setPrimaryClip(clip)
        return TravelDayActionResult.AddressCopied
    }

    private fun start(
        intent: Intent,
        unavailable: TravelDayActionResult,
    ): TravelDayActionResult {
        if (intent.resolveActivity(context.packageManager) == null) return unavailable
        return try {
            context.startActivity(intent)
            TravelDayActionResult.Started
        } catch (_: ActivityNotFoundException) {
            unavailable
        }
    }
}

internal fun sensitiveAddressClip(address: String): ClipData =
    ClipData.newPlainText(STAY_ADDRESS_LABEL, address).also { clip ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
    }

internal fun TravelDayAction.externalIntent(): Intent = when (this) {
    is TravelDayAction.CopyAddress -> error("Copying stays inside Landing Kit")
    is TravelDayAction.OpenMap -> Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=${Uri.encode(query)}"),
    )
    is TravelDayAction.CallStay -> Intent(
        Intent.ACTION_DIAL,
        Uri.fromParts("tel", phoneNumber, null),
    )
    is TravelDayAction.MessageStay -> Intent(
        Intent.ACTION_SENDTO,
        Uri.fromParts("smsto", phoneNumber, null),
    )
}

private const val STAY_ADDRESS_LABEL = "Stay address"

package com.shrikantbadwaik.landingkit.feature.kits.notification

import com.shrikantbadwaik.landingkit.core.data.local.reminder.ReminderCancellationStore

/** Called under the scheduler's lock. External effects precede durable acknowledgement. */
internal class ReminderCancellationProcessor(
    private val store: ReminderCancellationStore,
    private val cancelAlarm: (String) -> Unit,
    private val cancelNotification: (String) -> Unit,
    private val checkpoint: (ReminderCancellationCheckpoint) -> Unit = {},
) {
    suspend fun drain(tripId: String? = null) {
        val requests = store.pending().filter { tripId == null || it.tripId == tripId }
        requests.forEach { request ->
            cancelAlarm(request.tripId)
            checkpoint(ReminderCancellationCheckpoint.ALARM_CANCELLED)
            cancelNotification(request.tripId)
            checkpoint(ReminderCancellationCheckpoint.NOTIFICATION_CANCELLED)
        }
        // One durable write for the batch; any interruption safely replays prior cancellations.
        store.acknowledgeAll(requests)
        if (requests.isNotEmpty()) checkpoint(ReminderCancellationCheckpoint.ACKNOWLEDGED)
    }
}

internal enum class ReminderCancellationCheckpoint { ALARM_CANCELLED, NOTIFICATION_CANCELLED, ACKNOWLEDGED }

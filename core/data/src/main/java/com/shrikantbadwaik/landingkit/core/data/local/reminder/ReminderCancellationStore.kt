package com.shrikantbadwaik.landingkit.core.data.local.reminder

import android.util.AtomicFile
import com.shrikantbadwaik.landingkit.core.data.local.backup.BackupRecordId
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Pending external effects, independent of trip rows and excluded from travel backups. */
class ReminderCancellationStore(private val file: File) {
    private val journal = AtomicFile(file)
    private val mutex = Mutex()

    suspend fun enqueue(tripId: String) = withContext(Dispatchers.IO) {
        require(BackupRecordId.isValid(tripId))
        mutex.withLock {
            val requests = read()
            if (requests.none { it.tripId == tripId }) {
                require(requests.size < MAX_REQUESTS)
                write(requests + ReminderCancellation(tripId, UUID.randomUUID().toString()))
            }
        }
    }

    suspend fun pending(): List<ReminderCancellation> = withContext(Dispatchers.IO) {
        mutex.withLock { read() }
    }

    suspend fun acknowledge(request: ReminderCancellation) = acknowledgeAll(listOf(request))

    suspend fun acknowledgeAll(completed: List<ReminderCancellation>) = withContext(Dispatchers.IO) {
        if (completed.isEmpty()) return@withContext
        mutex.withLock {
            val requests = read()
            // A stale acknowledgement must not erase a later deletion of the same restored ID.
            val expected = completed.toSet()
            val remaining = requests.filterNot { it in expected }
            if (remaining.size != requests.size) write(remaining)
        }
    }

    private fun read(): List<ReminderCancellation> {
        if (!file.exists() && !File("${file.path}.bak").exists()) return emptyList()
        val bytes = journal.openRead().use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (output.size() <= MAX_BYTES) {
                val count = input.read(buffer, 0, minOf(buffer.size, MAX_BYTES + 1 - output.size()))
                if (count == -1) break
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        require(bytes.size <= MAX_BYTES) { "Oversized reminder cancellation record" }
        val json = JSONObject(bytes.toString(Charsets.UTF_8))
        require(json.getInt("version") == 1)
        val array = json.getJSONArray("requests")
        require(array.length() <= MAX_REQUESTS)
        return List(array.length()) { index ->
            val record = array.getJSONObject(index)
            ReminderCancellation(record.getString("trip"), record.getString("token")).also {
                require(BackupRecordId.isValid(it.tripId))
                require(it.token.length == 36 && UUID.fromString(it.token).toString() == it.token)
            }
        }.also { require(it.map(ReminderCancellation::tripId).distinct().size == it.size) }
    }

    private fun write(requests: List<ReminderCancellation>) {
        // Keep a durable empty state too: an interrupted acknowledgement replays cancellation.
        file.parentFile?.let { check(it.isDirectory || it.mkdirs()) }
        val records = JSONArray()
        requests.forEach { records.put(JSONObject().put("trip", it.tripId).put("token", it.token)) }
        val output = journal.startWrite()
        try {
            output.write(JSONObject().put("version", 1).put("requests", records)
                .toString().toByteArray(Charsets.UTF_8))
            journal.finishWrite(output)
        } catch (error: Throwable) {
            journal.failWrite(output)
            throw error
        }
    }

    private companion object {
        const val MAX_REQUESTS = 10_000
        const val MAX_BYTES = 2 * 1024 * 1024
    }
}

data class ReminderCancellation(val tripId: String, val token: String)

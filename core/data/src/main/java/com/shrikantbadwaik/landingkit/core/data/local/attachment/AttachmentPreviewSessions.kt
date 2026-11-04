package com.shrikantbadwaik.landingkit.core.data.local.attachment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.util.AtomicFile
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

/** Owns plaintext copies independently of encrypted attachments and of each other. */
internal class AttachmentPreviewSessions(
    private val context: Context,
    private val root: File = File(context.cacheDir, "attachment_previews"),
    private val hasGrant: (Uri, Int) -> Boolean = { uri, uid ->
        context.checkUriPermission(uri, -1, uid, Intent.FLAG_GRANT_READ_URI_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED
    },
    private val legacyRecipients: () -> List<Int> = {
        listOf("image/*", "application/pdf").flatMap { mimeType ->
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType("content://preview/file".toUri(), mimeType)
            context.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY,
            ).map { it.activityInfo.applicationInfo.uid } + listOfNotNull(
                intent.resolveActivityInfo(context.packageManager, PackageManager.MATCH_DEFAULT_ONLY)
                    ?.applicationInfo?.uid,
            )
        }.distinct().filter { it != Process.myUid() }
    },
) {
    private val lock = Any()
    private val owned = mutableSetOf<String>()
    private var generation = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var polling = false

    init {
        cleanInactive()
    }

    fun create(prefix: String, extension: String, decrypt: (File) -> Unit): File {
        val file = File(root, "$prefix.${UUID.randomUUID()}.$extension")
        require(file.canonicalFile.parentFile == root.canonicalFile)
        val temporary = File(root, "${file.name}.tmp")
        val version = synchronized(lock) {
            check(root.isDirectory || root.mkdirs()) { "Could not create preview directory" }
            owned += file.name
            generation
        }
        try {
            decrypt(temporary)
            synchronized(lock) {
                check(version == generation && file.name in owned) { "Viewing session ended" }
                check(temporary.renameTo(file)) { "Could not prepare attachment preview" }
            }
            return file
        } catch (error: Throwable) {
            release(file)
            throw error
        } finally {
            temporary.delete()
        }
    }

    fun retainExternally(file: File, uri: Uri, recipientUids: List<Int>) = synchronized(lock) {
        require(file.canonicalFile.parentFile == root.canonicalFile && file.name in owned)
        require(recipientUids.isNotEmpty())
        val record = JSONObject()
            .put("uri", uri.toString())
            .put("uids", JSONArray(recipientUids.distinct()))
        val journal = AtomicFile(File(root, "${file.name}.lease"))
        val output = journal.startWrite()
        try {
            output.write(record.toString().toByteArray(Charsets.UTF_8))
            journal.finishWrite(output)
        } catch (error: Throwable) {
            journal.failWrite(output)
            throw error
        }
    }

    fun release(file: File) = synchronized(lock) {
        require(file.canonicalFile.parentFile == root.canonicalFile)
        owned -= file.name
        cleanFile(file)
        scheduleGrantCleanup()
    }

    fun endSession() = synchronized(lock) {
        generation++
        owned.clear()
        cleanInactive()
    }

    fun releaseMatching(prefix: String) = synchronized(lock) {
        owned.removeAll { it.startsWith(prefix) }
        root.listFiles().orEmpty().filter { it.name.startsWith(prefix) }
            .filterNot { it.name.endsWith(".lease") || it.name.endsWith(".lease.bak") }
            .forEach(::cleanFile)
        scheduleGrantCleanup()
    }

    fun cleanInactive() = synchronized(lock) {
        root.listFiles().orEmpty()
            .filterNot { it.name.endsWith(".lease") || it.name.endsWith(".lease.bak") }
            .filterNot { it.name in owned || it.name.removeSuffix(".tmp") in owned }
            .forEach(::cleanFile)
        // A crash after deleting a copy but before its journal must not leave orphan records.
        root.listFiles().orEmpty().filter { it.name.endsWith(".lease") || it.name.endsWith(".lease.bak") }
            .filter { !File(root, it.name.removeSuffix(".bak").removeSuffix(".lease")).exists() }
            .forEach(::delete)
        scheduleGrantCleanup()
    }

    private fun cleanFile(file: File) {
        if (!file.exists()) return
        if (!file.name.endsWith(".tmp") && externallyGranted(file)) return
        if (!delete(file)) return
        AtomicFile(File(root, "${file.name}.lease")).delete()
    }

    private fun externallyGranted(file: File): Boolean {
        return try {
            val journal = AtomicFile(File(root, "${file.name}.lease"))
            if (journal.baseFile.exists() || File(root, "${file.name}.lease.bak").exists()) {
                val record = try {
                    JSONObject(journal.openRead().bufferedReader().use { it.readText() })
                } catch (_: JSONException) {
                    return legacyGrantActive(file)
                } catch (_: IOException) {
                    return legacyGrantActive(file)
                }
                val uids = record.getJSONArray("uids")
                val uri = Uri.parse(record.getString("uri"))
                (0 until uids.length()).any { hasGrant(uri, uids.getInt(it)) }
            } else {
                // New copies can only be granted after their journal is durably written.
                if (runCatching {
                        UUID.fromString(file.name.substringBeforeLast('.').substringAfterLast('.'))
                    }.isSuccess
                ) return false
                // Upgrade recovery: old FileProvider copies have no session journal.
                legacyGrantActive(file)
            }
        } catch (error: Exception) {
            // Uncertain permission state must not break an active viewer. Retry on the next sweep.
            Log.w("AttachmentPreviews", "Could not verify a preview grant", error)
            true
        }
    }

    private fun legacyGrantActive(file: File): Boolean {
        val recipients = legacyRecipients()
        if (recipients.isEmpty()) return false
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return recipients.any { hasGrant(uri, it) }
    }

    private fun needsCleanup() = root.listFiles().orEmpty().any {
        it.name !in owned && it.name.removeSuffix(".tmp") !in owned
    }

    private fun scheduleGrantCleanup() {
        if (polling || !needsCleanup()) return
        polling = true
        scope.launch {
            try {
                while (true) {
                    delay(GRANT_RECHECK_MILLIS.milliseconds)
                    synchronized(lock) {
                        cleanInactive()
                        if (!needsCleanup()) return@launch
                    }
                }
            } finally {
                synchronized(lock) { polling = false }
            }
        }
    }

    private fun delete(file: File): Boolean {
        if (file.exists() && !file.delete()) {
            Log.w("AttachmentPreviews", "Could not delete attachment preview; cleanup will retry")
            return false
        }
        return true
    }

    private companion object {
        const val GRANT_RECHECK_MILLIS = 5_000L
    }
}

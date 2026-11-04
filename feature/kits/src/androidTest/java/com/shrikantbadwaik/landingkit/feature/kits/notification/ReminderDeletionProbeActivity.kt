package com.shrikantbadwaik.landingkit.feature.kits.notification

import android.app.Activity
import android.os.Bundle
import android.os.Process
import android.widget.TextView
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Test APK only. Posts real synthetic notifications/alarms, then really terminates the process. */
class ReminderDeletionProbeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this).apply { textSize = 21f; setPadding(40, 200, 40, 40) }
        setContentView(text)
        val action = intent.getStringExtra("action") ?: "recover"
        val root = File(filesDir, "reminder-deletion-process-probe")
        text.text = "Thikana reminder verification: $action"
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var fixture: ReminderDeletionFixture? = null
            try {
                if (action == "interrupt") {
                    check(!root.exists() || root.deleteRecursively())
                    val f = ReminderDeletionFixture(this@ReminderDeletionProbeActivity, root)
                    fixture = f
                    f.seed()
                    val stage = checkNotNull(intent.getStringExtra("stage"))
                    fun terminate() {
                        File(root, "reached").outputStream().use {
                            it.write(stage.toByteArray()); it.fd.sync()
                        }
                        Process.killProcess(Process.myPid())
                    }
                    if (stage == "DATABASE_DELETED") {
                        f.repository(f.recovery { terminate() }).deleteTrip(f.tripId)
                    } else {
                        f.repository().deleteTrip(f.tripId)
                        if (stage == "DELETED_BEFORE_CANCEL") terminate()
                        val scheduler = f.scheduler()
                        ReminderCancellationProcessor(f.queue, scheduler::cancelAlarm,
                            scheduler::cancelNotification) { if (it.name == stage) terminate() }.drain()
                    }
                    error("Did not terminate at $stage")
                } else {
                    val stage = File(root, "reached").readText()
                    val f = ReminderDeletionFixture(this@ReminderDeletionProbeActivity, root)
                    fixture = f
                    f.verifyRecovered()
                    f.cleanupEffects()
                    val result = "PASS $stage; deleted alarm/notification cancelled; other reminder preserved; queue empty"
                    File(root, "result").writeText(result)
                    runOnUiThread { text.text = "Thikana reminder verification\n\n$result" }
                }
            } catch (error: Throwable) {
                val failure = "FAIL: ${error.stackTraceToString()}"
                root.mkdirs(); File(root, "result").writeText(failure)
                runOnUiThread { text.text = failure }
            } finally { fixture?.close() }
        }
    }
}

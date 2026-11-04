package com.shrikantbadwaik.landingkit.core.data.local.backup

import android.app.Activity
import android.os.Bundle
import android.os.Process
import android.widget.TextView
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Test APK only. Owns isolated synthetic data, and really terminates its process at a boundary. */
class RestoreProcessProbeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this).apply { textSize = 21f; setPadding(40, 200, 40, 40) }
        setContentView(text)
        val action = intent.getStringExtra("action") ?: "recover"
        val root = File(filesDir, "restore-process-probe")
        text.text = "Thikana restore verification: $action"
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (action == "interrupt") {
                    check(!root.exists() || root.deleteRecursively())
                    val fixture = RestoreTestFixture(this@RestoreProcessProbeActivity, root)
                    val stage = RestoreCheckpoint.valueOf(checkNotNull(intent.getStringExtra("stage")))
                    fixture.seed()
                    fixture.repository { checkpoint ->
                        if (checkpoint == stage) {
                            File(root, "reached").outputStream().use {
                                it.write(stage.name.toByteArray()); it.fd.sync()
                            }
                            Process.killProcess(Process.myPid())
                        }
                    }.restoreBackup(fixture.uri, RestoreTestFixture.PASSWORD.copyOf())
                    error("Did not terminate at $stage")
                } else {
                    val stage = RestoreCheckpoint.valueOf(File(root, "reached").readText())
                    val fixture = RestoreTestFixture(this@RestoreProcessProbeActivity, root)
                    try {
                        fixture.recovery.recover()
                        fixture.recovery.recover()
                        fixture.verifyExisting()
                        val committed = stage >= RestoreCheckpoint.COMMITTED
                        fixture.verifyRestored(committed)
                        check(!File(root, "restore.json").exists() && !File(root, "work").exists())
                        val retry = fixture.repository().restoreBackup(fixture.uri,
                            RestoreTestFixture.PASSWORD.copyOf())
                        check(retry.restoredTripIds.size == if (committed) 0 else 1)
                        fixture.verifyExisting(); fixture.verifyRestored(true)
                        val result = "PASS $stage: ${if (committed) "committed restore retained" else "interrupted restore rolled back"}; retry safe; existing journey/document intact"
                        File(root, "result").writeText(result)
                        runOnUiThread { text.text = "Thikana restore verification\n\n$result" }
                    } finally { fixture.close() }
                }
            } catch (error: Throwable) {
                val failure = "FAIL: ${error.stackTraceToString()}"
                root.mkdirs(); File(root, "result").writeText(failure)
                runOnUiThread { text.text = failure }
            }
        }
    }
}

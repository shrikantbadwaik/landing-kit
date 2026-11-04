package com.shrikantbadwaik.landingkit.feature.kits.presentation.security

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** A different-UID viewer for rehearsing real URI grants across sender process death. */
class PreviewGrantTestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val status = TextView(this).apply { textSize = 22f }
        fun readAgain() {
            status.text = runCatching {
                val bytes = contentResolver.openInputStream(checkNotNull(intent.data))!!.use { it.readBytes() }
                "Read succeeded: ${bytes.size} bytes"
            }.getOrElse { "Read failed: ${it.javaClass.simpleName}" }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 180, 40, 40)
            addView(TextView(this@PreviewGrantTestActivity).apply {
                text = "Thikana verification viewer"
                textSize = 24f
            })
            addView(status)
            addView(Button(this@PreviewGrantTestActivity).apply {
                text = "Read again"
                setOnClickListener { readAgain() }
            })
            addView(Button(this@PreviewGrantTestActivity).apply {
                text = "Finish viewer"
                setOnClickListener { finish() }
            })
        })
        readAgain()
    }
}

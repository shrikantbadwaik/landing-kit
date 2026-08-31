package com.shrikantbadwaik.landingkit.feature.kits.print

import android.print.PrintAttributes
import android.print.pdf.PrintedPdfDocument
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrintableSummaryPdfTest {
    @Test
    fun kyotoSummaryRendersAsOneLetterPage() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
            .setMinMargins(PrintAttributes.Margins(500, 500, 500, 500))
            .setResolution(PrintAttributes.Resolution("qa", "QA", 300, 300))
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .build()
        val output = File(context.getExternalFilesDir(null), "landing-kit-arrival-summary.pdf")
        val summary = PrintableArrivalSummary(
            destination = "Kyoto, Japan",
            arrivalDate = "Saturday, 29 August 2026",
            sections = listOf(
                PrintableSummarySection(
                    "Stay and arrival",
                    listOf(
                        PrintableSummaryEntry("Stay", "Machiya Stay Gion"),
                        PrintableSummaryEntry("Address", "2F, 570-123 Gionmachi Minamigawa, Higashiyama-ku, Kyoto"),
                        PrintableSummaryEntry("Local-language address", "京都市東山区祇園町南側 570-123 2F"),
                        PrintableSummaryEntry("Booking reference", "LK-4821"),
                        PrintableSummaryEntry("Getting there", "Take the JR Haruka to Kyoto Station, then taxi to Gion. Show the local-language address."),
                    ),
                ),
                PrintableSummarySection(
                    "If plans change",
                    listOf(PrintableSummaryEntry("Stay contact", "+81 75 555 1234")),
                ),
                PrintableSummarySection(
                    "Before landing",
                    listOf(
                        PrintableSummaryEntry("Download tickets and booking", "", true),
                        PrintableSummaryEntry("Save the stay address offline", "", true),
                        PrintableSummaryEntry("Confirm airport transfer", "", false),
                        PrintableSummaryEntry("Keep a little local currency", "", false),
                    ),
                ),
            ),
        )

        val document = PrintedPdfDocument(context, attributes)
        try {
            val rect = document.pageContentRect
            val renderer = ArrivalSummaryRenderer(summary, rect.width(), rect.height())
            assertEquals(1, renderer.pageCount)
            val page = document.startPage(0)
            page.canvas.save()
            renderer.drawPage(page.canvas, 0)
            page.canvas.restore()
            document.finishPage(page)
            FileOutputStream(output).use(document::writeTo)
        } finally {
            document.close()
        }
        assertTrue(output.length() > 10_000)
    }
}

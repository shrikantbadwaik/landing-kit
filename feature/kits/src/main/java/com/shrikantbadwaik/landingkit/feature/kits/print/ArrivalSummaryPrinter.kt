package com.shrikantbadwaik.landingkit.feature.kits.print

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream
import kotlin.math.max

internal fun printArrivalSummary(
    context: Context,
    summary: PrintableArrivalSummary,
) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    printManager.print(
        "Landing Kit - ${summary.destination}",
        ArrivalSummaryPrintAdapter(context, summary),
        PrintAttributes.Builder()
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .build(),
    )
}

internal class ArrivalSummaryPrintAdapter(
    private val context: Context,
    private val summary: PrintableArrivalSummary,
) : PrintDocumentAdapter() {
    private var attributes: PrintAttributes? = null
    private var pageCount: Int = 0

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle?,
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        attributes = newAttributes
        val layoutDocument = PrintedPdfDocument(context, newAttributes)
        try {
            val document = layoutDocument
            val rect = document.pageContentRect
            pageCount = ArrivalSummaryRenderer(summary, rect.width(), rect.height()).pageCount
        } finally {
            layoutDocument.close()
        }
        callback.onLayoutFinished(
            PrintDocumentInfo.Builder("landing-kit-arrival-summary.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pageCount)
                .build(),
            oldAttributes != newAttributes,
        )
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback,
    ) {
        val printAttributes = attributes
        if (printAttributes == null) {
            callback.onWriteFailed("Print layout is unavailable.")
            return
        }
        val requestedPages = requestedPageIndexes(pageCount, pages)
        try {
            val printDocument = PrintedPdfDocument(context, printAttributes)
            try {
                val document = printDocument
                val rect = document.pageContentRect
                val renderer = ArrivalSummaryRenderer(summary, rect.width(), rect.height())
                requestedPages.forEach { pageIndex ->
                    if (cancellationSignal.isCanceled) {
                        callback.onWriteCancelled()
                        return
                    }
                    val page = document.startPage(pageIndex)
                    page.canvas.save()
                    renderer.drawPage(page.canvas, pageIndex)
                    page.canvas.restore()
                    document.finishPage(page)
                }
                FileOutputStream(destination.fileDescriptor).use(document::writeTo)
            } finally {
                printDocument.close()
            }
            callback.onWriteFinished(requestedPages.toPageRanges())
        } catch (error: Exception) {
            callback.onWriteFailed(error.message ?: "The summary couldn't be created.")
        }
    }
}

internal fun requestedPageIndexes(
    pageCount: Int,
    ranges: Array<out PageRange>,
): List<Int> = requestedPageIndexesForRanges(
    pageCount = pageCount,
    ranges = if (ranges.any { it == PageRange.ALL_PAGES }) {
        listOf(0 until pageCount)
    } else {
        ranges.map { it.start..it.end }
    },
)

internal fun requestedPageIndexesForRanges(
    pageCount: Int,
    ranges: List<IntRange>,
): List<Int> = (0 until pageCount).filter { page -> ranges.any { page in it } }

private fun List<Int>.toPageRanges(): Array<PageRange> =
    map { page -> PageRange(page, page) }.toTypedArray()

internal class ArrivalSummaryRenderer(
    private val summary: PrintableArrivalSummary,
    private val pageWidth: Int,
    private val pageHeight: Int,
) {
    private val colors = PrintColors()
    private val textWidth = pageWidth - horizontalInset * 2
    private val blocks = buildBlocks()
    private val pages = paginate(blocks)
    val pageCount: Int = pages.size

    fun drawPage(canvas: Canvas, pageIndex: Int) {
        canvas.drawColor(Color.WHITE)
        drawPageHeader(canvas, pageIndex)
        pages[pageIndex].forEach { placed -> placed.block.draw(canvas, horizontalInset, placed.y) }
        drawFooter(canvas, pageIndex)
    }

    private fun buildBlocks(): List<RenderBlock> = buildList {
        add(HeroBlock(summary.destination, summary.arrivalDate))
        summary.sections.forEach { section ->
            add(SectionTitleBlock(section.title))
            section.entries.forEach { entry -> add(EntryBlock(entry)) }
        }
    }

    private fun paginate(source: List<RenderBlock>): List<List<PlacedBlock>> {
        val result = mutableListOf<MutableList<PlacedBlock>>()
        var current = mutableListOf<PlacedBlock>()
        var y = contentTop
        source.forEachIndexed { index, block ->
            val nextHeight = source.getOrNull(index + 1)?.height ?: 0f
            val required = if (block is SectionTitleBlock) block.height + nextHeight else block.height
            if (current.isNotEmpty() && y + required > contentBottom) {
                result += current
                current = mutableListOf()
                y = contentTop
            }
            current += PlacedBlock(block, y)
            y += block.height
        }
        if (current.isNotEmpty()) result += current
        return result.ifEmpty { listOf(emptyList()) }
    }

    private fun drawPageHeader(canvas: Canvas, pageIndex: Int) {
        val labelPaint = paint(8.5f, Paint.Style.FILL, colors.muted, Typeface.BOLD).apply {
            letterSpacing = .12f
        }
        canvas.drawText("LANDING KIT  /  ARRIVAL SUMMARY", horizontalInset, pt(12f), labelPaint)
        if (pageIndex > 0) {
            val continued = "${summary.destination} - continued"
            canvas.drawText(continued, horizontalInset, pt(24f), paint(9f, color = colors.ink))
        }
        val pageText = "${pageIndex + 1} / $pageCount"
        canvas.drawText(
            pageText,
            pageWidth - horizontalInset - paint(8.5f, color = colors.muted).measureText(pageText),
            pt(12f),
            paint(8.5f, color = colors.muted),
        )
    }

    private fun drawFooter(canvas: Canvas, pageIndex: Int) {
        val y = pageHeight - pt(9f)
        canvas.drawLine(horizontalInset, y - pt(10f), pageWidth - horizontalInset, y - pt(10f), paint(1f, color = colors.line))
        canvas.drawText("PRIVATE TRAVEL DETAILS - KEEP THIS COPY SAFE", horizontalInset, y, paint(7.5f, color = colors.muted, typefaceStyle = Typeface.BOLD))
    }

    private inner class HeroBlock(destination: String, arrivalDate: String) : RenderBlock {
        private val destinationLines = wrap(destination, paint(25f, color = colors.ink, typefaceStyle = Typeface.BOLD), textWidth - pt(28f))
        private val dateLines = wrap(arrivalDate, paint(10.5f, color = colors.ink), textWidth - pt(28f))
        override val height = pt(26f) + destinationLines.size * pt(27f) + dateLines.size * pt(15f) + pt(18f)

        override fun draw(canvas: Canvas, x: Float, y: Float) {
            val bottom = y + height - pt(8f)
            canvas.drawRoundRect(RectF(x, y, x + textWidth, bottom), pt(8f), pt(8f), paint(1f, color = colors.green))
            canvas.drawCircle(x + textWidth - pt(20f), y + pt(18f), pt(12f), paint(1f, color = colors.sun))
            var baseline = y + pt(27f)
            destinationLines.forEach {
                canvas.drawText(it, x + pt(14f), baseline, paint(25f, color = Color.WHITE, typefaceStyle = Typeface.BOLD))
                baseline += pt(27f)
            }
            dateLines.forEach {
                canvas.drawText(it, x + pt(14f), baseline + pt(3f), paint(10.5f, color = colors.pale))
                baseline += pt(15f)
            }
        }
    }

    private inner class SectionTitleBlock(private val title: String) : RenderBlock {
        override val height = pt(28f)
        override fun draw(canvas: Canvas, x: Float, y: Float) {
            canvas.drawText(title.uppercase(), x, y + pt(18f), paint(9f, color = colors.green, typefaceStyle = Typeface.BOLD))
            canvas.drawLine(x, y + pt(24f), x + textWidth, y + pt(24f), paint(.8f, color = colors.line))
        }
    }

    private inner class EntryBlock(private val entry: PrintableSummaryEntry) : RenderBlock {
        private val labelPaint = paint(8.5f, color = colors.muted, typefaceStyle = Typeface.BOLD)
        private val valuePaint = paint(10.5f, color = colors.ink)
        private val entryTextWidth = textWidth - if (entry.checked != null) pt(22f) else 0f
        private val labelLines = wrap(entry.label, labelPaint, entryTextWidth)
        private val valueLines = wrap(entry.value, valuePaint, entryTextWidth)
        override val height = pt(9f) +
            labelLines.size * pt(11f) +
            if (valueLines.isEmpty()) pt(9f) else pt(2f) + valueLines.size * pt(14f) + pt(9f)

        override fun draw(canvas: Canvas, x: Float, y: Float) {
            val textX = x + if (entry.checked != null) pt(22f) else 0f
            entry.checked?.let { checked ->
                val box = RectF(x, y + pt(10f), x + pt(12f), y + pt(22f))
                canvas.drawRoundRect(box, pt(2f), pt(2f), paint(1f, Paint.Style.STROKE, colors.green))
                if (checked) canvas.drawText("x", x + pt(3f), y + pt(20f), paint(9f, color = colors.green, typefaceStyle = Typeface.BOLD))
            }
            var baseline = y + pt(14f)
            labelLines.forEach {
                canvas.drawText(it.uppercase(), textX, baseline, labelPaint)
                baseline += pt(11f)
            }
            if (valueLines.isEmpty()) return
            baseline += pt(2f)
            valueLines.forEach {
                canvas.drawText(it, textX, baseline, valuePaint)
                baseline += pt(14f)
            }
        }
    }

    private fun wrap(text: String, paint: Paint, width: Float): List<String> {
        if (text.isBlank()) return emptyList()
        return text.lines().flatMap { paragraph ->
            val words = paragraph.trim().split(Regex("\\s+"))
            buildList {
                var line = ""
                words.forEach { word ->
                    val candidate = if (line.isEmpty()) word else "$line $word"
                    if (paint.measureText(candidate) <= width || line.isEmpty()) {
                        line = candidate
                    } else {
                        add(line)
                        line = word
                    }
                }
                if (line.isNotEmpty()) add(line)
            }
        }
    }

    private fun paint(
        sizePoints: Float,
        style: Paint.Style = Paint.Style.FILL,
        color: Int,
        typefaceStyle: Int = Typeface.NORMAL,
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = pt(sizePoints)
        this.style = style
        this.color = color
        strokeWidth = pt(max(.6f, sizePoints))
        typeface = Typeface.create("sans-serif", typefaceStyle)
    }

    // PrintedPdfDocument canvases use typographic points regardless of printer DPI.
    private fun pt(value: Float) = value
    private val horizontalInset get() = pt(22f)
    private val contentTop get() = pt(34f)
    private val contentBottom get() = pageHeight - pt(28f)

    private interface RenderBlock {
        val height: Float
        fun draw(canvas: Canvas, x: Float, y: Float)
    }

    private data class PlacedBlock(val block: RenderBlock, val y: Float)
}

private data class PrintColors(
    val ink: Int = Color.rgb(24, 51, 49),
    val green: Int = Color.rgb(22, 74, 69),
    val muted: Int = Color.rgb(96, 111, 107),
    val line: Int = Color.rgb(220, 225, 221),
    val sun: Int = Color.rgb(244, 201, 93),
    val pale: Int = Color.rgb(218, 238, 232),
)

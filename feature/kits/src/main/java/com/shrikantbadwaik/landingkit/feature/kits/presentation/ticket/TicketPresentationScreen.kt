package com.shrikantbadwaik.landingkit.feature.kits.presentation.ticket

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.designsystem.LandingKitColors
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.feature.kits.presentation.component.journalDoodles
import com.shrikantbadwaik.landingkit.feature.kits.R
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

@Composable
internal fun TicketPresentationScreen(
    attachments: List<KitAttachment>,
    initialAttachmentId: String,
    attachmentProtectionEnabled: Boolean,
    isAttachmentAccessUnlocked: Boolean,
    onPrepareAttachment: (String, (Result<PreparedAttachment>) -> Unit) -> Unit,
    onExit: () -> Unit,
) {
    val orderedAttachments = remember(attachments) {
        attachments.sortedWith(
            compareByDescending<KitAttachment> { it.isQuickAccess }
                .thenByDescending { it.createdAtMillis }
                .thenBy { it.id },
        )
    }
    val firstAttachmentId = initialAttachmentId.takeIf { candidate ->
        orderedAttachments.any { it.id == candidate }
    }
        ?: orderedAttachments.firstOrNull { it.isQuickAccess }?.id
        ?: orderedAttachments.first().id
    var selectedAttachmentId by rememberSaveable { mutableStateOf(firstAttachmentId) }
    val attachment = orderedAttachments.firstOrNull { it.id == selectedAttachmentId }
        ?: orderedAttachments.first()
    val attachmentIndex = orderedAttachments.indexOfFirst { it.id == attachment.id }
    var prepared by remember(attachment.id) { mutableStateOf<PreparedAttachment?>(null) }
    var preparationFailed by remember(attachment.id) { mutableStateOf(false) }
    var focusedPdfPage by remember { mutableStateOf<Int?>(null) }
    var fitRequest by remember { mutableIntStateOf(0) }
    val screenTitle = stringResource(R.string.ticket_presenter_pane_description)
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    LaunchedEffect(orderedAttachments, firstAttachmentId) {
        if (orderedAttachments.none { it.id == selectedAttachmentId }) {
            selectedAttachmentId = firstAttachmentId
        }
    }

    BackHandler {
        if (focusedPdfPage != null) focusedPdfPage = null else onExit()
    }

    LaunchedEffect(attachmentProtectionEnabled, isAttachmentAccessUnlocked) {
        if (attachmentProtectionEnabled && !isAttachmentAccessUnlocked) onExit()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(attachment.id, attachmentProtectionEnabled, isAttachmentAccessUnlocked, lifecycleOwner) {
        var generation = 0
        var active = false
        fun stop() {
            active = false
            generation++
            prepared?.close()
            prepared = null
        }
        fun start() {
            if (active || (attachmentProtectionEnabled && !isAttachmentAccessUnlocked)) return
            active = true
            preparationFailed = false
            val requestGeneration = ++generation
            onPrepareAttachment(attachment.id) { result ->
                if (!active || requestGeneration != generation) {
                    result.getOrNull()?.close()
                } else {
                    result.onSuccess {
                        prepared?.close()
                        prepared = it
                    }.onFailure { preparationFailed = true }
                }
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> start()
                Lifecycle.Event.ON_STOP -> stop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stop()
        }
    }

    DisposableEffect(activity) {
        val window = activity?.window
        val insetsController = window?.let { WindowInsetsControllerCompat(it, it.decorView) }
        val usedLightStatusBars = insetsController?.isAppearanceLightStatusBars
        val previousBrightness = window?.attributes?.screenBrightness
        val wasKeepingScreenOn = window?.attributes?.flags
            ?.and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window?.attributes = window?.attributes?.apply { screenBrightness = 1f }
        insetsController?.isAppearanceLightStatusBars = false
        onDispose {
            if (!wasKeepingScreenOn) {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            if (previousBrightness != null) {
                window.attributes = window.attributes.apply {
                    screenBrightness = previousBrightness
                }
            }
            if (usedLightStatusBars != null) {
                insetsController.isAppearanceLightStatusBars = usedLightStatusBars
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PresenterBackground)
            .journalDoodles("ticket", ink = LandingKitColors.Seafoam)
            .navigationBarsPadding()
            .semantics { paneTitle = screenTitle },
    ) {
        PresenterHeader(
            onExit = onExit,
            onFit = { fitRequest++ },
            onAllPages = if (focusedPdfPage != null) {
                { focusedPdfPage = null }
            } else {
                null
            },
        )

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                preparationFailed -> PresenterMessage(
                    text = stringResource(R.string.ticket_presenter_open_failed),
                )
                prepared == null -> CircularProgressIndicator(color = Color.White)
                prepared?.mimeType == PDF_MIME_TYPE -> PdfEvidence(
                    file = checkNotNull(prepared).file,
                    focusedPage = focusedPdfPage,
                    fitRequest = fitRequest,
                    onFocusPage = { focusedPdfPage = it },
                    onFailure = { preparationFailed = true },
                )
                else -> ImageEvidence(
                    file = checkNotNull(prepared).file,
                    fitRequest = fitRequest,
                    onFailure = { preparationFailed = true },
                )
            }
        }

        AttachmentNavigation(
            currentIndex = attachmentIndex,
            attachmentCount = orderedAttachments.size,
            onPrevious = {
                focusedPdfPage = null
                fitRequest++
                selectedAttachmentId = orderedAttachments[attachmentIndex - 1].id
            },
            onNext = {
                focusedPdfPage = null
                fitRequest++
                selectedAttachmentId = orderedAttachments[attachmentIndex + 1].id
            },
        )
    }
}

@Composable
private fun PresenterHeader(
    onExit: () -> Unit,
    onFit: () -> Unit,
    onAllPages: (() -> Unit)?,
) {
    val closeDescription = stringResource(R.string.ticket_presenter_exit_description)
    Surface(
        modifier = Modifier.statusBarsPadding(),
        color = PresenterChrome,
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onExit,
                modifier = Modifier.size(48.dp).semantics {
                    contentDescription = closeDescription
                },
                shape = MaterialTheme.shapes.small,
                color = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
            ) {
                Canvas(Modifier.padding(15.dp)) {
                    drawLine(Color.White, Offset(1f, 1f), Offset(size.width - 1f, size.height - 1f), 3f)
                    drawLine(Color.White, Offset(size.width - 1f, 1f), Offset(1f, size.height - 1f), 3f)
                }
            }
            Spacer(Modifier.weight(1f))
            onAllPages?.let {
                TextButton(onClick = it) {
                    Text(stringResource(R.string.ticket_presenter_all_pages), color = Color.White)
                }
            }
            TextButton(onClick = onFit) {
                Text(stringResource(R.string.ticket_presenter_fit), color = LandingKitColors.Sun)
            }
        }
    }
}

@Composable
private fun ImageEvidence(
    file: File,
    fitRequest: Int,
    onFailure: () -> Unit,
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, file) {
        value = withContext(Dispatchers.IO) { decodeImage(file) }
        if (value == null) onFailure()
    }
    bitmap?.let {
        ZoomableBitmap(
            bitmap = it,
            fitRequest = fitRequest,
            description = stringResource(R.string.ticket_presenter_image_description),
        )
    }
}

@Composable
private fun PdfEvidence(
    file: File,
    focusedPage: Int?,
    fitRequest: Int,
    onFocusPage: (Int) -> Unit,
    onFailure: () -> Unit,
) {
    val document by produceState<PdfDocument?>(initialValue = null, file) {
        var opened: PdfDocument? = null
        try {
            value = withContext(Dispatchers.IO) {
                PdfDocument(file).also { opened = it }
            }
            awaitDispose { opened?.close() }
        } catch (cancellation: CancellationException) {
            opened?.close()
            throw cancellation
        } catch (_: Exception) {
            opened?.close()
            onFailure()
        }
    }
    val pdf = document ?: return

    if (focusedPage != null) {
        PdfPageBitmap(pdf = pdf, pageIndex = focusedPage, renderWidth = 1800) { bitmap ->
            ZoomableBitmap(
                bitmap = bitmap,
                fitRequest = fitRequest,
                description = stringResource(
                    R.string.ticket_presenter_pdf_page_description,
                    focusedPage + 1,
                    pdf.pageCount,
                ),
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items((0 until pdf.pageCount).toList(), key = { it }) { pageIndex ->
                val size = pdf.pageSizes[pageIndex]
                Surface(
                    onClick = { onFocusPage(pageIndex) },
                    modifier = Modifier.fillMaxWidth().aspectRatio(size.first / size.second),
                    color = Color.White,
                    shadowElevation = 6.dp,
                ) {
                    PdfPageBitmap(pdf, pageIndex, 1400) { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(
                                R.string.ticket_presenter_pdf_page_description,
                                pageIndex + 1,
                                pdf.pageCount,
                            ),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageBitmap(
    pdf: PdfDocument,
    pageIndex: Int,
    renderWidth: Int,
    content: @Composable (Bitmap) -> Unit,
) {
    val bitmap by produceState<Bitmap?>(null, pdf, pageIndex, renderWidth) {
        value = withContext(Dispatchers.IO) { pdf.render(pageIndex, renderWidth) }
    }
    if (bitmap != null) {
        content(checkNotNull(bitmap))
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Gray)
        }
    }
}

@Composable
private fun ZoomableBitmap(
    bitmap: Bitmap,
    fitRequest: Int,
    description: String,
) {
    var scale by remember(bitmap) { mutableFloatStateOf(1f) }
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }
    LaunchedEffect(fitRequest) {
        scale = 1f
        offset = Offset.Zero
    }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = description,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            }
            .pointerInput(bitmap) {
                detectTapGestures(onDoubleTap = {
                    scale = 1f
                    offset = Offset.Zero
                })
            }
            .pointerInput(bitmap) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val nextScale = (scale * zoom).coerceIn(1f, 5f)
                    offset = if (nextScale == 1f) Offset.Zero else offset + pan
                    scale = nextScale
                }
            },
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun AttachmentNavigation(
    currentIndex: Int,
    attachmentCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    if (attachmentCount <= 1) return
    Surface(color = PresenterChrome, contentColor = Color.White) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AttachmentArrowButton(
                pointsForward = false,
                enabled = currentIndex > 0,
                description = stringResource(R.string.ticket_presenter_previous_description),
                onClick = onPrevious,
            )
            Text(
                text = stringResource(
                    R.string.ticket_presenter_position,
                    currentIndex + 1,
                    attachmentCount,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
            AttachmentArrowButton(
                pointsForward = true,
                enabled = currentIndex < attachmentCount - 1,
                description = stringResource(R.string.ticket_presenter_next_description),
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun AttachmentArrowButton(
    pointsForward: Boolean,
    enabled: Boolean,
    description: String,
    onClick: () -> Unit,
) {
    val arrowColor = Color.White.copy(alpha = if (enabled) 1f else 0.32f)
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(48.dp).semantics { contentDescription = description },
        shape = CircleShape,
        color = Color.White.copy(alpha = if (enabled) 0.12f else 0.05f),
        contentColor = arrowColor,
    ) {
        Canvas(Modifier.padding(14.dp)) {
            val startX = if (pointsForward) size.width * 0.18f else size.width * 0.82f
            val endX = if (pointsForward) size.width * 0.82f else size.width * 0.18f
            val middleY = size.height * 0.5f
            drawLine(arrowColor, Offset(startX, middleY), Offset(endX, middleY), 3f)
            val headX = if (pointsForward) size.width * 0.58f else size.width * 0.42f
            drawLine(arrowColor, Offset(headX, size.height * 0.24f), Offset(endX, middleY), 3f)
            drawLine(arrowColor, Offset(headX, size.height * 0.76f), Offset(endX, middleY), 3f)
        }
    }
}

@Composable
private fun PresenterMessage(text: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = PresenterChrome,
        contentColor = Color.White,
    ) {
        Text(text, Modifier.padding(24.dp), style = MaterialTheme.typography.bodyLarge)
    }
}

private fun decodeImage(file: File): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
            val longestEdge = maxOf(info.size.width, info.size.height)
            if (longestEdge > MAX_IMAGE_EDGE) {
                val scale = MAX_IMAGE_EDGE.toFloat() / longestEdge
                decoder.setTargetSize(
                    (info.size.width * scale).toInt().coerceAtLeast(1),
                    (info.size.height * scale).toInt().coerceAtLeast(1),
                )
            }
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_IMAGE_EDGE) sample *= 2
        BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })
    }
}.getOrNull()

private class PdfDocument(file: File) : AutoCloseable {
    private val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = try {
        PdfRenderer(descriptor)
    } catch (error: Throwable) {
        descriptor.close()
        throw error
    }
    private val mutex = Mutex()
    private val closeRequested = AtomicBoolean(false)
    val pageCount: Int = renderer.pageCount
    val pageSizes: List<Pair<Float, Float>> = (0 until pageCount).map { index ->
        renderer.openPage(index).use { page -> page.width.toFloat() to page.height.toFloat() }
    }

    suspend fun render(index: Int, width: Int): Bitmap? = mutex.withLock {
        if (closeRequested.get()) return@withLock null
        renderer.openPage(index).use { page ->
            val height = (width * page.height.toFloat() / page.width).toInt().coerceAtLeast(1)
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            }
        }
    }

    override fun close() {
        if (closeRequested.compareAndSet(false, true)) {
            closeScope.launch {
                mutex.withLock {
                    try {
                        renderer.close()
                    } finally {
                        descriptor.close()
                    }
                }
            }
        }
    }

    private companion object {
        val closeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}

private const val PDF_MIME_TYPE = "application/pdf"
private const val MAX_IMAGE_EDGE = 4096
private val PresenterBackground = Color(0xFF171E19)
private val PresenterChrome = LandingKitColors.Forest

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return current as? Activity
}

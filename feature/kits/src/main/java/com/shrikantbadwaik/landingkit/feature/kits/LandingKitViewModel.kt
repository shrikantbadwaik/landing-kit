package com.shrikantbadwaik.landingkit.feature.kits

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shrikantbadwaik.landingkit.core.data.ChecklistTemplateRepository
import com.shrikantbadwaik.landingkit.core.data.AttachmentRepository
import com.shrikantbadwaik.landingkit.core.data.AttachmentTooLargeException
import com.shrikantbadwaik.landingkit.core.data.PreparedAttachment
import com.shrikantbadwaik.landingkit.core.data.UnsupportedAttachmentException
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.model.ChecklistTemplate
import com.shrikantbadwaik.landingkit.core.model.KitAttachment
import com.shrikantbadwaik.landingkit.core.model.Trip
import com.shrikantbadwaik.landingkit.core.data.preferences.UserPreferences
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.KitForm
import com.shrikantbadwaik.landingkit.feature.kits.sample.SampleTripContent
import com.shrikantbadwaik.landingkit.feature.kits.sample.sampleTrip
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@HiltViewModel
class LandingKitViewModel @Inject constructor(
    private val repository: TripRepository,
    private val templateRepository: ChecklistTemplateRepository,
    private val attachmentRepository: AttachmentRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {
    private val _pendingSharedAttachments = MutableStateFlow<PendingSharedAttachments?>(null)
    private val sharedAttachmentImports = ConcurrentHashMap.newKeySet<String>()
    private val _isAttachmentAccessUnlocked = MutableStateFlow(false)
    private val _pendingAttachmentAuthentication =
        MutableStateFlow<PendingAttachmentAuthentication?>(null)
    private var attachmentRelockJob: Job? = null
    val pendingSharedAttachments: StateFlow<PendingSharedAttachments?> =
        _pendingSharedAttachments.asStateFlow()
    internal val attachmentProtectionEnabled: StateFlow<Boolean> =
        userPreferences.attachmentProtectionEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )
    internal val isAttachmentAccessUnlocked: StateFlow<Boolean> =
        _isAttachmentAccessUnlocked.asStateFlow()
    internal val pendingAttachmentAuthentication: StateFlow<PendingAttachmentAuthentication?> =
        _pendingAttachmentAuthentication.asStateFlow()

    internal val trips: StateFlow<TripCollectionState> = repository.observeTrips()
        .map<List<Trip>, TripCollectionState>(TripCollectionState::Ready)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripCollectionState.Loading,
        )
    val templates: StateFlow<List<ChecklistTemplate>> =
        templateRepository.observeTemplates().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    internal fun saveKit(
        form: KitForm,
        existingTrip: Trip? = null,
        onSaved: (String) -> Unit,
    ) {
        val trip = form.toTrip(existingTrip)
        viewModelScope.launch {
            repository.saveTrip(trip)
            onSaved(trip.id)
        }
    }

    internal fun addSample(content: SampleTripContent, onSaved: (String) -> Unit) {
        val trip = sampleTrip(content)
        viewModelScope.launch {
            repository.saveTrip(trip)
            onSaved(trip.id)
        }
    }

    internal fun setComplete(tripId: String, itemId: String, complete: Boolean) {
        viewModelScope.launch { repository.setItemComplete(tripId, itemId, complete) }
    }

    internal fun deleteKit(tripId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTrip(tripId)
            onDeleted()
        }
    }

    internal fun restoreKit(trip: Trip) {
        viewModelScope.launch { repository.saveTrip(trip) }
    }

    internal fun saveTemplate(template: ChecklistTemplate) {
        viewModelScope.launch { templateRepository.saveTemplate(template) }
    }

    internal fun deleteTemplate(templateId: String) {
        viewModelScope.launch { templateRepository.deleteTemplate(templateId) }
    }

    internal fun attachments(tripId: String): Flow<List<KitAttachment>> =
        attachmentRepository.observeAttachments(tripId)

    internal suspend fun attachmentThumbnail(attachmentId: String): ByteArray? =
        attachmentRepository.loadThumbnail(attachmentId)

    internal fun importAttachments(
        tripId: String,
        sources: List<Uri>,
        onComplete: (AttachmentImportResult) -> Unit,
    ) {
        viewModelScope.launch {
            onComplete(importAttachmentSources(tripId, sources))
        }
    }

    internal fun setAttachmentProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setAttachmentProtectionEnabled(enabled) }
    }

    internal fun requestAttachmentAuthentication(action: ProtectedAttachmentAction) {
        if (_pendingAttachmentAuthentication.value != null) return
        _pendingAttachmentAuthentication.value = PendingAttachmentAuthentication(
            id = UUID.randomUUID().toString(),
            action = action,
        )
    }

    internal fun finishAttachmentAuthentication(requestId: String) {
        _pendingAttachmentAuthentication.compareAndSet(
            expect = _pendingAttachmentAuthentication.value?.takeIf { it.id == requestId },
            update = null,
        )
    }

    internal fun unlockAttachmentAccess() {
        attachmentRelockJob?.cancel()
        _isAttachmentAccessUnlocked.value = true
        attachmentRelockJob = viewModelScope.launch {
            delay(ATTACHMENT_UNLOCK_DURATION_MILLIS)
            _isAttachmentAccessUnlocked.value = false
        }
    }

    fun lockAttachmentAccess() {
        attachmentRelockJob?.cancel()
        attachmentRelockJob = null
        _isAttachmentAccessUnlocked.value = false
        _pendingAttachmentAuthentication.value = null
    }

    fun acceptSharedAttachments(sources: List<Uri>) {
        if (sources.isEmpty()) return
        _pendingSharedAttachments.value = PendingSharedAttachments(
            id = UUID.randomUUID().toString(),
            sources = sources.distinct(),
        )
    }

    internal fun dismissSharedAttachments(pendingId: String) {
        _pendingSharedAttachments.compareAndSet(
            expect = _pendingSharedAttachments.value?.takeIf { it.id == pendingId },
            update = null,
        )
    }

    internal fun importSharedAttachments(
        pendingId: String,
        tripId: String,
        onComplete: (AttachmentImportResult) -> Unit,
    ) {
        val pending = _pendingSharedAttachments.value?.takeIf { it.id == pendingId } ?: return
        if (!sharedAttachmentImports.add(pendingId)) return
        viewModelScope.launch {
            try {
                val result = importAttachmentSources(tripId, pending.sources)
                _pendingSharedAttachments.compareAndSet(expect = pending, update = null)
                onComplete(result)
            } finally {
                sharedAttachmentImports.remove(pendingId)
            }
        }
    }

    private suspend fun importAttachmentSources(
        tripId: String,
        sources: List<Uri>,
    ): AttachmentImportResult {
        var imported = 0
        var unsupported = 0
        var tooLarge = 0
        var failed = 0
        sources.forEach { source ->
            try {
                attachmentRepository.importAttachment(tripId, source)
                imported++
            } catch (_: UnsupportedAttachmentException) {
                unsupported++
            } catch (_: AttachmentTooLargeException) {
                tooLarge++
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Log.e(ATTACHMENT_LOG_TAG, "Attachment import failed", error)
                failed++
            }
        }
        return AttachmentImportResult(imported, unsupported, tooLarge, failed)
    }

    internal fun prepareAttachment(
        attachmentId: String,
        onPrepared: (Result<PreparedAttachment>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = try {
                Result.success(attachmentRepository.prepareForViewing(attachmentId))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Result.failure(error)
            }
            onPrepared(result)
        }
    }

    internal fun deleteAttachment(
        attachmentId: String,
        onDeleted: (Result<Unit>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = try {
                Result.success(attachmentRepository.deleteAttachment(attachmentId))
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Result.failure(error)
            }
            onDeleted(result)
        }
    }
}

internal data class AttachmentImportResult(
    val imported: Int,
    val unsupported: Int,
    val tooLarge: Int,
    val failed: Int,
)

data class PendingSharedAttachments(
    val id: String,
    val sources: List<Uri>,
)

internal data class PendingAttachmentAuthentication(
    val id: String,
    val action: ProtectedAttachmentAction,
)

internal sealed interface ProtectedAttachmentAction {
    data class Open(val attachmentId: String) : ProtectedAttachmentAction
    data class SetProtection(val enabled: Boolean) : ProtectedAttachmentAction
}

internal fun requiresAttachmentAuthentication(
    protectionEnabled: Boolean,
    accessUnlocked: Boolean,
): Boolean = protectionEnabled && !accessUnlocked

private const val ATTACHMENT_UNLOCK_DURATION_MILLIS = 5 * 60 * 1_000L

private const val ATTACHMENT_LOG_TAG = "LandingKitAttachment"

internal sealed interface TripCollectionState {
    data object Loading : TripCollectionState
    data class Ready(val trips: List<Trip>) : TripCollectionState
}

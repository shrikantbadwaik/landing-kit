package com.shrikantbadwaik.landingkit.feature.kits

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
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.KitForm
import com.shrikantbadwaik.landingkit.feature.kits.sample.SampleTripContent
import com.shrikantbadwaik.landingkit.feature.kits.sample.sampleTrip
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class LandingKitViewModel @Inject constructor(
    private val repository: TripRepository,
    private val templateRepository: ChecklistTemplateRepository,
    private val attachmentRepository: AttachmentRepository,
) : ViewModel() {
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
        sources: List<android.net.Uri>,
        onComplete: (AttachmentImportResult) -> Unit,
    ) {
        viewModelScope.launch {
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
            onComplete(AttachmentImportResult(imported, unsupported, tooLarge, failed))
        }
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

private const val ATTACHMENT_LOG_TAG = "LandingKitAttachment"

internal sealed interface TripCollectionState {
    data object Loading : TripCollectionState
    data class Ready(val trips: List<Trip>) : TripCollectionState
}

package com.shrikantbadwaik.landingkit.feature.kits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import com.shrikantbadwaik.landingkit.feature.kits.presentation.model.KitForm
import com.shrikantbadwaik.landingkit.feature.kits.sample.sampleTrip
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LandingKitViewModel @Inject constructor(
    private val repository: TripRepository,
) : ViewModel() {
    val trips: StateFlow<List<Trip>> = repository.observeTrips().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    internal fun createKit(form: KitForm, onSaved: (String) -> Unit) {
        val trip = form.toTrip()
        viewModelScope.launch {
            repository.saveTrip(trip)
            onSaved(trip.id)
        }
    }

    internal fun addSample(onSaved: (String) -> Unit) {
        val trip = sampleTrip()
        viewModelScope.launch {
            repository.saveTrip(trip)
            onSaved(trip.id)
        }
    }

    internal fun setComplete(tripId: String, itemId: String, complete: Boolean) {
        viewModelScope.launch { repository.setItemComplete(tripId, itemId, complete) }
    }
}

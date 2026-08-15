package com.shrikantbadwaik.landingkit.feature.kits

import com.shrikantbadwaik.landingkit.core.data.TripRepository
import com.shrikantbadwaik.landingkit.core.model.OfflineArrivalCheckEngine
import com.shrikantbadwaik.landingkit.core.model.PlanBCheckResult
import com.shrikantbadwaik.landingkit.core.model.PlanBContentFingerprint
import com.shrikantbadwaik.landingkit.core.model.Trip

internal class PlanBCheckRunner(
    private val tripRepository: TripRepository,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun run(trip: Trip): PlanBCheckResult {
        val result = OfflineArrivalCheckEngine.evaluatePlanB(trip)
        tripRepository.recordPlanBCheck(
            tripId = trip.id,
            status = result.status,
            checkedAtMillis = currentTimeMillis(),
            contentFingerprint = PlanBContentFingerprint.derive(trip),
        )
        return result
    }
}

package com.mnowo.transportationalarmclock.domain.use_case

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CalculateDistanceUseCase @Inject constructor() {

    operator fun invoke(userLocation: Location, markerLocation: Location) : Flow<Float> = flow {
        emit(userLocation.distanceTo(markerLocation))
    }
}
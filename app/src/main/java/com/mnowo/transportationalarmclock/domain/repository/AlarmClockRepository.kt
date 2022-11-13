package com.mnowo.transportationalarmclock.domain.repository

import com.mnowo.transportationalarmclock.data.MAPS_API_KEY
import com.mnowo.transportationalarmclock.domain.models.GooglePredictionsResponse
import com.mnowo.transportationalarmclock.domain.models.PlaceIdLocation
import com.mnowo.transportationalarmclock.domain.models.Resource
import retrofit2.http.Query

interface AlarmClockRepository {

    suspend fun getPredictions(input: String): Resource<GooglePredictionsResponse>

    suspend fun getPlaceIdDetails(placeId: String) : PlaceIdLocation
}
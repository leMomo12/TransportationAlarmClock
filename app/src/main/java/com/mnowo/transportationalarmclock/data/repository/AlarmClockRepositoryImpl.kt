package com.mnowo.transportationalarmclock.data.repository

import android.util.Log
import com.mnowo.transportationalarmclock.data.GooglePlacesApi
import com.mnowo.transportationalarmclock.domain.models.GooglePredictionsResponse
import com.mnowo.transportationalarmclock.domain.models.PlaceIdLocation
import com.mnowo.transportationalarmclock.domain.models.Resource
import com.mnowo.transportationalarmclock.domain.repository.AlarmClockRepository
import javax.inject.Inject

class AlarmClockRepositoryImpl @Inject constructor(
    private val googlePlacesApi: GooglePlacesApi
) : AlarmClockRepository {

    override suspend fun getPredictions(input: String): Resource<GooglePredictionsResponse> {
        val response = try {
            googlePlacesApi.getPredictions(input = input)
        } catch (e: Exception) {
            Log.d("Rently", "Exception: ${e}")
            return Resource.Error("Failed prediction")
        }

        return Resource.Success(response)
    }

    override suspend fun getPlaceIdDetails(placeId: String): PlaceIdLocation {
        return googlePlacesApi.getPlaceIdDetails(placeId = placeId)
    }


}
package com.mnowo.transportationalarmclock.data

import com.mnowo.transportationalarmclock.domain.models.GooglePredictionsResponse
import com.mnowo.transportationalarmclock.domain.models.PlaceIdLocation
import retrofit2.http.GET
import retrofit2.http.Query

const val MAPS_API_KEY = "AIzaSyCsZshxSPA7mVAZ3B5zX6FIB48bAmF2nMw"

interface GooglePlacesApi {
    @GET("maps/api/place/autocomplete/json")
    suspend fun getPredictions(
        @Query("key") key: String = MAPS_API_KEY,
    @Query("types") types: String = "",
    @Query("input") input: String
    ): GooglePredictionsResponse

    @GET("maps/api/place/details/json")
    suspend fun getPlaceIdDetails(
        @Query("key") key: String = MAPS_API_KEY,
        @Query("fields") fields: String = "geometry",
        @Query("placeid") placeId: String
    ) : PlaceIdLocation
}
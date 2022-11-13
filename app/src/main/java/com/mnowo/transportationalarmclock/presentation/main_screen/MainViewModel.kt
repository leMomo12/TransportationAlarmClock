package com.mnowo.transportationalarmclock.presentation.main_screen

import android.location.Location
import android.util.Log.d
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.mnowo.transportationalarmclock.domain.models.GooglePredictions
import com.mnowo.transportationalarmclock.domain.models.Resource
import com.mnowo.transportationalarmclock.domain.repository.AlarmClockRepository
import com.mnowo.transportationalarmclock.domain.use_case.CalculateDistanceUseCase
import com.mnowo.transportationalarmclock.domain.use_case.GetPredictionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val calculateDistanceUseCase: CalculateDistanceUseCase,
    private val alarmClockRepository: AlarmClockRepository
    ) : ViewModel() {

    private val _mapPropertiesState =
        mutableStateOf<MapProperties>(value = MapProperties(isMyLocationEnabled = true))
    val mapPropertiesState: State<MapProperties> = _mapPropertiesState

    fun setMapPropertiesState(value: MapProperties) {
        _mapPropertiesState.value = value
    }

    private val _locationFromGPS = mutableStateOf<Location?>(null)
    val locationFromGPS: State<Location?> = _locationFromGPS

    fun setLocationFromGPS(value: Location?) {
        _locationFromGPS.value = value
    }

    private val _markerState = mutableStateOf<LatLng?>(null)
    val markerState: State<LatLng?> = _markerState

    fun setMarkerState(value: LatLng) {
        _markerState.value = value
    }

    private val _distanceState = mutableStateOf<Float?>(null)
    val distanceState: State<Float?> = _distanceState

    fun setDistanceState(value: Float?) {
        _distanceState.value = value
    }

    private val _isAlarmClockActive = mutableStateOf(false)
    val isAlarmClockActive: State<Boolean> = _isAlarmClockActive

    fun setIsAlarmClockActive(value: Boolean) {
        _isAlarmClockActive.value = value
    }

    private val _googleSearchState = mutableStateOf("")
    val googleSearchState: State<String> = _googleSearchState

    fun setGoogleSearchState(value: String) {
        _googleSearchState.value = value
        getPrediction()
    }

    private val _predictionsListState = mutableStateOf(ArrayList<GooglePredictions>())
    val predictionsListState: State<ArrayList<GooglePredictions>> = _predictionsListState

    fun setPredictionsListState(value: ArrayList<GooglePredictions>) {
        _predictionsListState.value = value
    }

    fun calculateDistanceLoop() = viewModelScope.launch(Dispatchers.IO) {
        while (isAlarmClockActive.value) {
            calculateDistance()
            delay(10000)
        }
    }

    private suspend fun calculateDistance() {
        markerToLocation()?.let { markerLocation ->
            locationFromGPS.value?.let { userLocation ->
                calculateDistanceUseCase.invoke(userLocation, markerLocation).collect() {
                    setDistanceState(it)
                }
            }
        }
    }

    private fun markerToLocation(): Location? {
        val markerLocation = Location("MarkerLocation")
        markerState.value?.let {
            markerLocation.latitude = it.latitude
            markerLocation.longitude = it.longitude
            return markerLocation
        }
        return null
    }

    private fun getPrediction() = viewModelScope.launch(Dispatchers.IO) {
        val response = alarmClockRepository.getPredictions(input = googleSearchState.value)
        when (response) {
            is Resource.Success -> {
                response.data?.let { setPredictionsListState(value = it.predictions) }
            }
            else -> {
            }
        }
    }

    fun getLocationFromPlaceId(placeId: String) = viewModelScope.launch(Dispatchers.IO) {
        val response = alarmClockRepository.getPlaceIdDetails(placeId = placeId)
        val lat = response.result.geometry.location.lat
        val lng = response.result.geometry.location.lng
        val latLng = LatLng(lat, lng)
        setMarkerState(value = latLng)
    }

}
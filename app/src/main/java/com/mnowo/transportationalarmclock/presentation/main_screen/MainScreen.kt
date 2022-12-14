package com.mnowo.transportationalarmclock.presentation.main_screen

import android.content.Context
import android.util.Log.d
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mnowo.transportationalarmclock.domain.LocationProviderImpl
import com.mnowo.transportationalarmclock.domain.models.GooglePredictions
import com.mnowo.transportationalarmclock.domain.util.AlarmService
import com.mnowo.transportationalarmclock.presentation.ui.theme.lightError
import com.mnowo.transportationalarmclock.presentation.util.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private lateinit var fusedLocationClient: FusedLocationProviderClient
private lateinit var locationRequest: LocationRequest
private lateinit var locationCallback: LocationCallback

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    context: Context,
    navController: NavController
) {
    val bottomState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    val locationPermissionState =
        rememberPermissionState(permission = android.Manifest.permission.ACCESS_FINE_LOCATION)

    val cameraPositionState = rememberCameraPositionState()

    GPSPermission(locationPermissionState = locationPermissionState)

    LaunchedEffect(key1 = true) {
        viewModel.markerState.value?.let {
            delay(100)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(CameraPosition(it, 15f, 0f, 0f))
            )
        }
        getUserLocation(
            viewModel = viewModel,
            locationPermissionState = locationPermissionState,
            context = context
        )
    }

    if (locationPermissionState.status.isGranted) {
        Scaffold { contentPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    properties = viewModel.mapPropertiesState.value,
                    onMapClick = {
                        if (!viewModel.isAlarmClockActive.value && viewModel.mapsBounds.value == null) {
                            viewModel.setMarkerState(it)
                        }
                    },
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = false,
                        zoomControlsEnabled = false
                    ),
                    cameraPositionState = cameraPositionState
                ) {
                    viewModel.markerState.value?.let { location ->
                        Marker(MarkerState(position = location))
                    }
                    viewModel.mapsBounds.value?.let {
                        Polyline(
                            points = listOf<LatLng>(
                                viewModel.markerState.value!!,
                                LatLng(
                                    viewModel.locationFromGPS.value!!.latitude,
                                    viewModel.locationFromGPS.value!!.longitude
                                )
                            )
                        )
                    }
                }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top
                    ) {

                        if (!viewModel.isAlarmClockActive.value && viewModel.mapsBounds.value == null) {
                            d(
                                "MapBounds",
                                "MapBounds: ${viewModel.mapsBounds.value.toString()} , alarmClock: ${viewModel.isAlarmClockActive.value}"
                            )
                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "",
                                        tint = Color.Blue
                                    )
                                },
                                placeholder = {
                                    Text(text = "Search in google maps")
                                },
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 30.dp, end = 30.dp)
                                    .background(
                                        color = Color(
                                            red = 255,
                                            green = 255,
                                            blue = 255,
                                            alpha = 245
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        navController.navigate(Screen.SearchScreen.route)
                                    },
                                enabled = false
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 15.dp, top = 20.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        MyLocationButton {
                            coroutineScope.launch {
                                viewModel.locationFromGPS.value?.let { userLocation ->
                                    moveCameraToUserLocation(
                                        cameraState = cameraPositionState,
                                        userLocation = LatLng(
                                            userLocation.latitude,
                                            userLocation.longitude
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 40.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    BottomInformationPanel(
                        viewModel = viewModel,
                        coroutineScope = coroutineScope,
                        cameraState = cameraPositionState,
                        onAddAlarmClicked = {
                            coroutineScope.launch {
                                viewModel.calculateLatLngForLocationAndMarkerInView()
                                delay(100)
                                moveCameraToLocationAndMarkerInView(viewModel, cameraPositionState)
                            }
                        }
                    )
                }
            }
            if (viewModel.progressIndicatorState.value) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

suspend fun moveCameraToUserLocation(
    cameraState: CameraPositionState,
    userLocation: LatLng
) {
    cameraState.animate(
        update = CameraUpdateFactory.newCameraPosition(CameraPosition(userLocation, 15f, 0f, 0f))
    )
}

suspend fun moveCameraToLocationAndMarkerInView(
    viewModel: MainViewModel,
    cameraState: CameraPositionState
) {
    viewModel.mapsBounds.value?.let { mapBounds ->
        cameraState.animate(
            update = CameraUpdateFactory.newLatLngBounds(mapBounds, 60)
        )
    }
}

@Composable
fun PredictionsItem(item: GooglePredictions, viewModel: MainViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .background(Color.White)
            .clickable { viewModel.getLocationFromPlaceId(placeId = item.place_id) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            Text(text = item.description)
            Spacer(modifier = Modifier.padding(vertical = 4.dp))
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
        }
    }
}

@Composable
fun BottomInformationPanel(
    coroutineScope: CoroutineScope,
    cameraState: CameraPositionState,
    viewModel: MainViewModel,
    onAddAlarmClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(start = 30.dp, end = 30.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.isAlarmClockActive.value) {
                ActiveAlarmClockBottomInformationPanel(viewModel = viewModel)
            } else if (viewModel.mapsBounds.value != null) {
                MapBoundsBottomInformationPanel(onYesClicked = {

                    viewModel.setIsAlarmClockActive(true)
                    viewModel.calculateDistanceLoop()

                    coroutineScope.launch {
                        viewModel.locationFromGPS.value?.let { userLocation ->
                            moveCameraToUserLocation(
                                cameraState = cameraState,
                                userLocation = LatLng(
                                    userLocation.latitude,
                                    userLocation.longitude
                                )
                            )
                        }
                    }
                }, onCancelClicked = {
                    viewModel.setMapsBounds(null)
                })
            } else {
                BasicBottomInformationPanel(
                    onAddAlarmClicked = { onAddAlarmClicked() },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun BasicBottomInformationPanel(onAddAlarmClicked: () -> Unit, viewModel: MainViewModel) {
    Button(
        onClick = { onAddAlarmClicked() },
        enabled = viewModel.markerState.value != null
    ) {
        Text(
            text = "Add alarm"
        )
    }
    Spacer(modifier = Modifier.padding(vertical = 2.dp))
    Text(
        text = "Add a marker first before adding a alarm clock",
        color = Color.Gray,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun MapBoundsBottomInformationPanel(onYesClicked: () -> Unit, onCancelClicked: () -> Unit) {
    Text(text = "Are you sure to set a new alarm clock?")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp), horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = { onYesClicked() }) {
            Text(text = "Yes")
        }
        OutlinedButton(onClick = { onCancelClicked() }) {
            Text(text = "Cancel")
        }
    }
}

@Composable
fun ActiveAlarmClockBottomInformationPanel(viewModel: MainViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        ActiveAlarmClockBottomInformationPanelDistance(weight = .6f, viewModel = viewModel)
        ActiveAlarmClockBottomInformationPanelCancel(weight = .4f, onCancelClicked = {
            viewModel.cancelAlarmClock()
        })
    }
}

@Composable
fun RowScope.ActiveAlarmClockBottomInformationPanelDistance(
    weight: Float,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .weight(weight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "linear distance:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.padding(vertical = 0.dp))
        Text(text = "${viewModel.distanceState.value}km")
    }
}

@Composable
fun RowScope.ActiveAlarmClockBottomInformationPanelCancel(
    weight: Float,
    onCancelClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .weight(weight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp), color = Color.LightGray
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(onClick = { onCancelClicked() }) {
                    Text(text = "Cancel")
                }
            }
        }
    }
}

@Composable
fun MyLocationButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(48.dp)
            .width(48.dp)
            .clickable { onClick() },
        shape = CircleShape,
        backgroundColor = Color(red = 255, green = 255, blue = 255, alpha = 233)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "", tint = Color.Gray)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun getUserLocation(
    viewModel: MainViewModel,
    locationPermissionState: PermissionState,
    context: Context
) {
    if (locationPermissionState.status.isGranted) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    viewModel.setLocationFromGPS(location)
                }
            }
        }
        val locationProviderImpl = LocationProviderImpl(context = context)
        locationProviderImpl.createLocationRequest(
            settingsLauncher = null,
            locationCallback,
            context = context
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GPSPermission(locationPermissionState: PermissionState) {

    when (locationPermissionState.status) {
        // If the camera permission is granted, then show screen with the feature enabled
        PermissionStatus.Granted -> {

        }
        is PermissionStatus.Denied -> {
            SideEffect {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }
}
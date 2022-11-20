package com.mnowo.transportationalarmclock.presentation.main_screen

import android.content.Context
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
                        viewModel.setMarkerState(it)
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
                }
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top
                    ) {
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
                                    color = Color(red = 255, green = 255, blue = 255, alpha = 245),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    navController.navigate(Screen.SearchScreen.route)
                                },
                            enabled = false
                        )
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
                        onAddAlarmClicked = {
                            viewModel.setIsAlarmClockActive(true)
                            viewModel.calculateDistanceLoop()
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
fun BottomInformationPanel(viewModel: MainViewModel, onAddAlarmClicked: () -> Unit) {
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
                Text(
                    text = "Your linear distance:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.padding(vertical = 5.dp))
                Text(text = "${viewModel.distanceState.value}km")
            } else {
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
            //modifier = Modifier.padding(2.dp)
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
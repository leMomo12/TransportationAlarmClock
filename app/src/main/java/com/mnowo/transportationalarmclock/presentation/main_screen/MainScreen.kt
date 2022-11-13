package com.mnowo.transportationalarmclock.presentation.main_screen

import android.content.Context
import android.util.Log.d
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.mnowo.transportationalarmclock.domain.LocationProviderImpl
import com.mnowo.transportationalarmclock.domain.models.GooglePredictions
import com.mnowo.transportationalarmclock.presentation.AlarmBottomSheet
import com.mnowo.transportationalarmclock.presentation.util.Screen
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val bottomState = rememberBottomSheetScaffoldState()
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
        BottomSheetScaffold(
            sheetPeekHeight = 100.dp,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            scaffoldState = bottomState,
            sheetContent = {
                AlarmBottomSheet()
            }) { contentPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
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
                                color = Color.White,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                navController.navigate(Screen.SearchScreen.route)
                            },
                        enabled = false
                    )
                }
            }
        }
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
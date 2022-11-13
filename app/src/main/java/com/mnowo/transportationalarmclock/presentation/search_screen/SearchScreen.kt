package com.mnowo.transportationalarmclock.presentation.search_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mnowo.transportationalarmclock.domain.models.GooglePredictions
import com.mnowo.transportationalarmclock.presentation.main_screen.MainViewModel
import com.mnowo.transportationalarmclock.presentation.util.Screen

@Composable
fun SearchScreen(viewModel: MainViewModel, navController: NavController) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(key1 = true) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            OutlinedTextField(
                value = viewModel.googleSearchState.value,
                onValueChange = {
                    viewModel.setGoogleSearchState(value = it)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "",
                        tint = Color.Blue
                    )
                },
                trailingIcon = {
                    if (viewModel.googleSearchState.value.isNotBlank()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "",
                            modifier = Modifier.clickable {
                                viewModel.setGoogleSearchState("")
                            })
                    }
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
                    .focusRequester(focusRequester = focusRequester)
            )
        }
        Spacer(modifier = Modifier.padding(vertical = 15.dp))
        LazyColumn {
            items(viewModel.predictionsListState.value) {
                SearchListItem(item = it, viewModel = viewModel, navigate = {
                    navController.navigate(
                        Screen.MainScreen.route
                    )
                })
            }
        }
    }
}

@Composable
fun SearchListItem(item: GooglePredictions, viewModel: MainViewModel, navigate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, bottom = 5.dp)
            .background(Color.White)
            .clickable {
                viewModel.getLocationFromPlaceId(placeId = item.place_id)
                navigate()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = item.structured_formatting.main_text ?: "Something went wrong",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 5.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = item.structured_formatting.secondary_text ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
        }
    }
}
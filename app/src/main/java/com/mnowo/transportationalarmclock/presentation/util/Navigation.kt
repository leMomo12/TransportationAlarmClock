package com.mnowo.transportationalarmclock.presentation.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mnowo.transportationalarmclock.presentation.main_screen.MainScreen
import com.mnowo.transportationalarmclock.presentation.main_screen.MainViewModel
import com.mnowo.transportationalarmclock.presentation.search_screen.SearchScreen

@Composable
fun Navigation(navHostController: NavHostController, context: Context) {
    val mainViewModel: MainViewModel = hiltViewModel()
    NavHost(navController = navHostController, startDestination = Screen.MainScreen.route) {
        composable(Screen.MainScreen.route) {
            MainScreen(context = context, navController = navHostController, viewModel = mainViewModel)
        }
        composable(Screen.SearchScreen.route) {
            SearchScreen(navController = navHostController, viewModel = mainViewModel)
        }
    }
}
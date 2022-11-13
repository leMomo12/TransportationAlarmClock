package com.mnowo.transportationalarmclock.presentation.util

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object SearchScreen : Screen("search_screen")
}

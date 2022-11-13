package com.mnowo.transportationalarmclock.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.mnowo.transportationalarmclock.presentation.main_screen.MainScreen
import com.mnowo.transportationalarmclock.presentation.ui.theme.TransportationAlarmClockTheme
import com.mnowo.transportationalarmclock.presentation.util.Navigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TransportationAlarmClockTheme {
                val navHostController = rememberNavController()
                Navigation(navHostController = navHostController, context = this)
            }
        }
    }
}
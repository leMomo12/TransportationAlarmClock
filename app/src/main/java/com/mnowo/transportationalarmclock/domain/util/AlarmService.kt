package com.mnowo.transportationalarmclock.domain.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log.d
import androidx.lifecycle.LifecycleCoroutineScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class AlarmService @Inject constructor() :
    Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        val CHANNEL_ID = "Foreground Alarm Service ID"

        //val channel: NotificationChannel =
        //    NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)

        //startForeground(1001, channel)

        return super.onStartCommand(intent, flags, startId)
    }
}
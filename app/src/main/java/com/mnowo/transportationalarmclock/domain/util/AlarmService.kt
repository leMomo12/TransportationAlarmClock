package com.mnowo.transportationalarmclock.domain.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import com.mnowo.transportationalarmclock.R
import javax.inject.Inject

class AlarmService @Inject constructor() :
    Service() {

    var userLocation: Location? = null
    var targetLocation: Location? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var distance: Float? = null

        userLocation?.let { userLoc ->
            targetLocation?.let { targetLoc ->
                distance = userLoc.distanceTo(targetLoc)
            }
        }

        distance?.let { distance ->
            if (distance <= 1000) {
                // Wake the user up
                playRingtone()

                // Show an alarm notification
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationId = 1
                val channelId = "alarm_channel"
                val channelName = "Alarm Channel"
                val importance = NotificationManager.IMPORTANCE_HIGH

                val notification = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_alarm_on)
                    .setContentTitle("Alarm")
                    .setContentText("Wake up!")
                    .setAutoCancel(true)
                    .build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(channelId, channelName, importance)
                    notificationManager.createNotificationChannel(channel)
                }
                notificationManager.notify(notificationId, notification)

            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun playRingtone() {
        var alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        if(alert == null) {
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            if(alert == null) {
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
        }
        val ringtone = RingtoneManager.getRingtone(this, alert)
        ringtone.play()
    }



}
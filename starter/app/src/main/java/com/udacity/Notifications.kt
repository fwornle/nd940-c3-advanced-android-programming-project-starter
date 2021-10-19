package com.udacity

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.udacity.MainActivity.Companion.CHANNEL_ID
import com.udacity.MainActivity.Companion.NOTIFICATION_ID


// define extension function for the sending of notifications
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {

    // configure notification builder
    val builder = NotificationCompat.Builder(
        applicationContext,
        CHANNEL_ID,
    )
        .setSmallIcon(R.drawable.ic_cloud_download)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)

    // deliver notification
    notify(NOTIFICATION_ID, builder.build())

}
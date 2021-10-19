package com.udacity

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.udacity.MainActivity.Companion.CHANNEL_ID
import com.udacity.MainActivity.Companion.NOTIFICATION_ID


// define extension function for the sending of notifications
fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context) {

    // create content intent for the notification to launch activity 'DetailActivity'
    val contentIntent = Intent(applicationContext, DetailActivity::class.java)

    // create pending intend
    // ... note: removing the 'pending intend mutability flag' warning:
    // https://stackoverflow.com/questions/67045607/how-to-resolve-missing-pendingintent-mutability-flag-lint-warning-in-android-a
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    // style notification
    val cloudImage = BitmapFactory.decodeResource(
        applicationContext.resources,
        R.drawable.download_symbol
    )
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(cloudImage)
        .bigLargeIcon(null)

    // configure notification builder
    val builder = NotificationCompat.Builder(
        applicationContext,
        CHANNEL_ID,
    )
        .setSmallIcon(R.drawable.ic_cloud_download)
        .setContentTitle(applicationContext.getString(R.string.notification_title))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setStyle(bigPicStyle)
        .setLargeIcon(cloudImage)

    // deliver notification
    notify(NOTIFICATION_ID, builder.build())

}

// extension function for the cancelling of all notifications
fun NotificationManager.cancelNotifications() {
    cancelAll()
}

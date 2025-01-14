package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.udacity.databinding.ActivityMainBinding
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var selUrl: String? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    //private lateinit var action: NotificationCompat.Action

    // view binding
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding for main activity (incl. content)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)

        // inflate layout (this includes 'content')
        activityMainBinding.root.apply { setContentView(this) }

        // use material ActionBar
        setSupportActionBar(activityMainBinding.toolbar)

        // register BroadcastReceiver for 'download complete' events
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // onClick listener for download button
        // note: included layout are reachable via their ID (see activity_main.xml)
        // REF: https://chetangupta.net/viewbinding/#includeMerge
        activityMainBinding.includes.customButton.setOnClickListener {

            // cancel all notifications
            notificationManager.cancelNotifications()

            // anything selected yet?
            selUrl?.let {

                // activate animation in LoadingButton
                activityMainBinding.includes.customButton.setActive(true)

                // yes --> activate button and initiate download
                download(it)

            } ?: run {

                // nothing selected yet --> display toast
                Toast.makeText(this,"Please select the file to download",
                    Toast.LENGTH_SHORT).show()

            }  // no URL chosen --> Toast

        }  // OnClickListener


        // get instance of NotificationManager
        // ... used in the 'broadcast handler' (receiver) for DownloadManager
        notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        // create channel for notifications
        notificationManager.createChannel(
            CHANNEL_ID,
            getString(R.string.notification_title)
        )

    }  // onCreate


    // broadcast receiver for DownloadManager
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) Timber.i( "DONE")

            // switch-off loading state of LoadingButton
            activityMainBinding.includes.customButton.setState(ButtonState.Completed)

            // send notification to indicate the completion of the download
            context?.let {
                notificationManager.sendNotification(
                    applicationContext.getString(R.string.notification_description),
                    it)
            }
        }
    }


    // do da downloading...
    private fun download(daUrl: String) {

        val request =
            DownloadManager.Request(Uri.parse(daUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                //.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        Timber.i("starting download from URL ${daUrl}")
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

    }  // download()


    // handle radioButton click events
    fun onRadioButtonClicked(view: View) {

        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {

                R.id.rbLink1 ->
                    if (checked) {
                        // Option #1
                        selUrl = URL1
                        Timber.i("Option #1 - downloading from ${URL1}")
                    }
                R.id.rbLink2 ->
                    if (checked) {
                        // Option #2
                        Timber.i("Option #2 - downloading from ${URL2}")
                        selUrl = URL2
                    }
                R.id.rbLink3 ->
                    if (checked) {
                        // Option #3
                        Timber.i("Option #3 - downloading from ${URL3}")
                        selUrl = URL3
                    }

            }  // when (view.ID)

        }  // if (view is RadioButton)

    } // onRadioButtonClicked


    // establish notification channel (as extension function to NotificationManager)
    private fun NotificationManager.createChannel(channelId: String, channelName: String) {

        // create channel (if supported by Android)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )//.apply { setShowBadge(false) }

            // configure notification channel
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)

            // get an instance of the NotificationManager and create the channel
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }  // createChannel

    // define extension function to NotificationManager for the sending of notifications
    private fun NotificationManager.sendNotification(
        messageBody: String,
        applicationContext: Context) {

        // create content intent for the notification to launch activity 'DetailActivity'
        val contentIntent = Intent(applicationContext, DetailActivity::class.java).apply {

            // add a "details button"
            // ... removed, as default action is more comfortable to use (just click the notif.)
            //action = Intent.ACTION_DEFAULT

            // key-value pair to communicate info from MainActivity to DetailActivity
            putExtra(REPO_URL_KEY, selUrl)
        }

        // create pending intend (to associate the above described intend with the notification channel)
        // ... note: removing the 'pending intend mutability flag' warning:
        // https://stackoverflow.com/questions/67045607/how-to-resolve-missing-pendingintent-mutability-flag-lint-warning-in-android-a
        pendingIntent = PendingIntent.getActivity(
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
            .setStyle(bigPicStyle)
            .setLargeIcon(cloudImage)
            .setContentIntent(pendingIntent)
            //.addAction(R.drawable.details, getString(R.string.details), pendingIntent)
            .setAutoCancel(true)

        // deliver notification
        notify(NOTIFICATION_ID, builder.build())

    }

    // extension function for the cancelling of all notifications
    private fun NotificationManager.cancelNotifications() {
        cancelAll()
    }

    // some constants...
    companion object {
        const val URL1 =
            "https://github.com/bumptech/glide"
        const val URL2 =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        const val URL3 =
            "https://github.com/square/retrofit"

        const val CHANNEL_ID = "channelId"
        const val NOTIFICATION_ID = 42
        const val REPO_URL_KEY = "repo_url"
    }

}

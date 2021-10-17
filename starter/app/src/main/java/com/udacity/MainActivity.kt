package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.udacity.databinding.ActivityMainBinding
import com.udacity.databinding.ContentMainBinding
import timber.log.Timber


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var selUrl: String? = null

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    // view binding
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var contentMainBinding: ContentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding for main activity (incl. content)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        contentMainBinding = ContentMainBinding.inflate(layoutInflater)

        // inflate layout (this includes 'content')
        activityMainBinding.root.apply { setContentView(this) }

        // use material ActionBar
        setSupportActionBar(activityMainBinding.toolbar)

        // register BroadcastReceiver
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // onClick listener for download button
        contentMainBinding.customButton.setOnClickListener {
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadID) Timber.i( "DONE")
        }
    }


    // do da downloading...
    private fun download() {

        // anything selected yet?
        selUrl?.let {

            // yes --> initiate download
            val request =
                DownloadManager.Request(Uri.parse(it))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            Timber.i("starting download from URL ${it}")
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        } ?: run {

            // nothing selected yet --> display toast
            Toast.makeText(this,"Please select the file to download",
                Toast.LENGTH_SHORT).show()

        }

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

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val URL1 =
            "https://github.com/bumptech/glide"
            //"https://github.com/bumptech/glide/releases/download/v3.6.0/glide-3.6.0.jar"
        private const val URL2 =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        private const val URL3 =
            "https://github.com/square/retrofit"
        private const val CHANNEL_ID = "channelId"
    }

}

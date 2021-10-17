package com.udacity

import android.app.Application
import timber.log.Timber
import java.util.concurrent.TimeUnit


// app wide initializations / tasks
class LoadingApplication: Application() {

    // other app-wide initializations, needed immediately --> on creation of the app
    override fun onCreate() {
        super.onCreate()

        // initialize Timber library (logging)
        Timber.plant(Timber.DebugTree())

    }

}


package com.clarifai.notkotdog

import android.app.Application
import timber.log.Timber

/**
 * Base application class for the app.
 *
 * Created by adam.mcneilly on 5/24/17.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        // Static strings used in the whole app
        val PREFS_NAME = "NotKotDogPrefs"
        val AUTH_TOKEN_KEY = "AuthToken"
    }
}
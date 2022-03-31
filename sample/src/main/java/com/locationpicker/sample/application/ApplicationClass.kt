package com.locationpicker.sample.application

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import com.locationpicker.sample.di.LanguageUseCaseProvider
import com.locationpicker.sample.di.applicationModule
import com.locationpicker.sample.di.useCaseModule
import com.locationpicker.sample.features.splash.di.splashModule
import com.locationpicker.sample.data.sources.remote.di.remoteModule
import com.softartch_lib.BuildConfig
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class ApplicationClass : Application() {

    companion object {
        var currentActivity: ComponentName? = null
        var isTestVersion:Boolean=true
        lateinit var appContext:Application
        lateinit var application: com.locationpicker.sample.application.ApplicationClass
    }

    override fun onCreate() {
        super.onCreate()
        com.locationpicker.sample.application.ApplicationClass.Companion.appContext =this



        startKoin(this, listOf(applicationModule,
                        remoteModule,
                        useCaseModule,
                        splashModule
                ))

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {

            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
                com.locationpicker.sample.application.ApplicationClass.Companion.currentActivity = activity?.componentName
            }

            override fun onActivityStarted(activity: Activity?) {
            }

            override fun onActivityDestroyed(activity: Activity?) {
                com.locationpicker.sample.application.ApplicationClass.Companion.currentActivity = null
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity?) {
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            }

        })
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LanguageUseCaseProvider.getLanguageUseCase(base).execute(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LanguageUseCaseProvider.getLanguageUseCase(this).execute(this)
    }

    var versionCode = BuildConfig.VERSION_CODE
    var versionName = BuildConfig.VERSION_NAME

}
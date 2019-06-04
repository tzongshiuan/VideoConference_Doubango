package com.gorilla.vc

import android.app.Activity
import android.app.Application
import com.gorilla.vc.di.AppInjector
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import org.doubango.ngn.NgnApplication
import javax.inject.Inject

class VCApp  : NgnApplication(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
//        if (BuildConfig.DEBUG) {
//            Timber.plant(Timber.DebugTree())
//            Stetho.initializeWithDefaults(this)
//        }

        // Dagger2
        AppInjector.init(this)
//        Fabric.with(this, Crashlytics())
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }
}
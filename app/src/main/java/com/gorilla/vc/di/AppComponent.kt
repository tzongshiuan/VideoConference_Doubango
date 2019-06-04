package com.gorilla.vc.di

import android.app.Application
import com.gorilla.vc.VCApp
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, AppModule::class, ActivityBuildersModule::class])
interface AppComponent {
    fun application(): Application
    fun inject(app: VCApp)
}
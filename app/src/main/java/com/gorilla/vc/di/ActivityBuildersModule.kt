package com.gorilla.vc.di

import com.gorilla.vc.view.ui.concall.ConcallActivity
import com.gorilla.vc.view.ui.login.LogInActivity
import com.gorilla.vc.view.ui.session.SessionActivity
import com.gorilla.vc.view.ui.setting.SettingActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeLogInActivity(): LogInActivity

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeSettingActivity(): SettingActivity

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeSessionActivity(): SessionActivity

    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeConcallActivity(): ConcallActivity
}
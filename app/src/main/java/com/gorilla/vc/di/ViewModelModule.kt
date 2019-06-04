package com.gorilla.vc.di

import android.arch.lifecycle.ViewModel
import com.gorilla.vc.view.ui.concall.ConcallViewModel
import com.gorilla.vc.view.ui.login.LogInViewModel
import com.gorilla.vc.view.ui.session.OnGoing.OnGoingViewModel
import com.gorilla.vc.view.ui.session.reserve.ReserveViewModel
import com.gorilla.vc.view.ui.setting.SettingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(LogInViewModel::class)
    abstract fun bindLogInViewModel(logInViewModel: LogInViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    abstract fun bindSettingViewModel(settingViewModel: SettingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ReserveViewModel::class)
    abstract fun bindReserveViewModel(reserveViewModel: ReserveViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OnGoingViewModel::class)
    abstract fun bindOnGoingViewModel(onGoingViewModel: OnGoingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConcallViewModel::class)
    abstract fun bindConcallViewModel(concallViewModel: ConcallViewModel): ViewModel
}
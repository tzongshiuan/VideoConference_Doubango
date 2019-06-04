package com.gorilla.vc.di

import com.gorilla.vc.view.ui.concall.RtspList.RtspListFragment
import com.gorilla.vc.view.ui.concall.information.InformationFragment
import com.gorilla.vc.view.ui.concall.member.MemberFragment
import com.gorilla.vc.view.ui.concall.message.MessageFragment
import com.gorilla.vc.view.ui.concall.whiteboard.WhiteBoardFragment
import com.gorilla.vc.view.ui.session.OnGoing.OnGoingFragment
import com.gorilla.vc.view.ui.session.reserve.ReserveFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeReserveFragment(): ReserveFragment
    @ContributesAndroidInjector
    abstract fun contributeOnGoingFragment(): OnGoingFragment
    @ContributesAndroidInjector
    abstract fun contributeMessageFragment(): MessageFragment
    @ContributesAndroidInjector
    abstract fun contributeMemberFragment(): MemberFragment
    @ContributesAndroidInjector
    abstract fun contributeInformationFragment(): InformationFragment
    @ContributesAndroidInjector
    abstract fun contributeRtspListFragment(): RtspListFragment
    @ContributesAndroidInjector
    abstract fun contributeWhiteBoardFragment(): WhiteBoardFragment
}
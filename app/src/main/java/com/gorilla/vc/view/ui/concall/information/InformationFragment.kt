package com.gorilla.vc.view.ui.concall.information

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gorilla.vc.databinding.ConcallInfoFramentBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.PresentMode
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.view.ui.concall.ConcallViewModel
import javax.inject.Inject

class InformationFragment : Fragment(), Injectable {

    private var mBinding : ConcallInfoFramentBinding? = null

    @Inject
    lateinit var vcManager: VcManager
    @Inject
    lateinit var factory: VcViewModelFactory

    private var concallViewModel : ConcallViewModel? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        concallViewModel = ViewModelProviders.of(this, factory).get(ConcallViewModel::class.java)
        concallViewModel?.lastUpdateStatusLiveData?.observe(this, Observer {
            mBinding?.host = getName(it!!.host)
            mBinding?.presenter = when(it.presentMode){
                PresentMode.SINGLE_HOST -> getName(it.presenter1)
                PresentMode.DUAL_HOST   -> "${getName(it.presenter1)} / ${getName(it.presenter2)}"
                else                    -> ""
            }
            mBinding?.people = it.participants?.size.toString()
        })
        super.onActivityCreated(savedInstanceState)
    }

    private fun getName(id:Int?) = vcManager.getNameFromId(id.toString())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ConcallInfoFramentBinding.inflate(inflater, container, false)
        mBinding?.session = vcManager.sessionInfo
        return mBinding?.root
    }
}